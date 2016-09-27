/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.rcp.run.ContextMenuSupport;
import org.csstudio.display.builder.rcp.run.DisplayNavigation;
import org.csstudio.display.builder.rcp.run.NavigationAction;
import org.csstudio.display.builder.rcp.run.RCP_JFXRepresentation;
import org.csstudio.display.builder.rcp.run.ZoomAction;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.runtime.ActionUtil;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;

/** Part that hosts display builder runtime
 *
 *  <p>Hosts FXCanvas in SWT
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RuntimeViewPart extends ViewPart
{
	// FXViewPart saves a tiny bit of code, but this allow more control over the FXCanvas.
	// e4view would allow E4-like POJO, but unclear how representation
	// would then best find the newly created RuntimeViewPart to set its input etc.
	// --> Using E3 ViewPart
	public static final String ID = "org.csstudio.display.builder.rcp.run.RuntimeViewPart";

    /** Property on the 'root' Group of the JFX scene that holds RuntimeViewPart */
    public static final String ROOT_RUNTIME_VIEW_PART = "_runtime_view_part";

    /** Memento key for DisplayInfo */
    private static final String MEMENTO_DISPLAY_INFO = "DISPLAY_INFO";

    /** Back/forward navigation */
    private final DisplayNavigation navigation = new DisplayNavigation();

    /** Display info that may have been received from memento */
    private volatile Optional<DisplayInfo> display_info = Optional.empty();

    private FXCanvas fx_canvas;

    private RCP_JFXRepresentation representation;

    private Parent root;

    private Consumer<DisplayModel> close_handler = ActionUtil::handleClose;

	private DisplayModel active_model;

    /** Open a runtime display
     *
     *  <p>Either opens a new display, or if there is already an existing view
     *  for that input, "activate" it, which pops a potentially hidden view to the top.
     *
     *  @param close_handler Code to call when part is closed
     *  @param info DisplayInfo (to compare with currently open displays)
     *  @return {@link RuntimeViewPart}
     *  @throws Exception on error
     */
    public static RuntimeViewPart open(final Consumer<DisplayModel> close_handler, final DisplayInfo info)
            throws Exception
    {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (info != null)
            for (IViewReference view_ref : page.getViewReferences())
                if (view_ref.getId().startsWith(ID))
                {
                    final IViewPart view = view_ref.getView(true);
                    if (view instanceof RuntimeViewPart)
                    {
                        final RuntimeViewPart runtime_view = (RuntimeViewPart) view;
                        if (info.equals(runtime_view.getDisplayInfo())) // Allow for runtime_view.getDisplayInfo() == null
                        {
                            page.showView(view_ref.getId(), view_ref.getSecondaryId(), IWorkbenchPage.VIEW_ACTIVATE);
                            return runtime_view;
                        }
                    }
                }
        final RuntimeViewPart part = (RuntimeViewPart) page.showView(ID, UUID.randomUUID().toString(), IWorkbenchPage.VIEW_ACTIVATE);
        part.close_handler = close_handler;
        return part;
    }

    /** Locate the currently active display
     *  @return {@link RuntimeViewPart} or <code>null</code> when nothing found
     */
    public static RuntimeViewPart getActiveDisplay()
    {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IWorkbenchPart part = page.getActivePart();
        if (part instanceof RuntimeViewPart)
            return(RuntimeViewPart) part;
        return null;
    }

    public Parent getRoot()
    {
        return root;
    }

    /** @param name Name of the part */
    public void trackCurrentModel(final DisplayModel model)
    {
        final DisplayInfo old_info = display_info.orElse(null);
        final DisplayInfo info = DisplayInfo.forModel(model);
        // A display might be loaded without macros,
        // but the DisplayModel may then have macros configured in the display itself.
        //
        // This can later result in not recognizing an existing display:
        // Display X is running, and it contained macros.
        // Now somehow we open X again, without macros, but
        // all the executing displays have X with macros,
        // so we open yet another one instead of showing the existing instance.
        //
        // To avoid this problem:
        //
        // When first loading a display, set display_info to the received info.
        //
        // When this is later updated, only replace the display_info
        // if there was none,
        // or the new one has a different path,
        // or different macros _and_ there were original macros.
    	if ( old_info == null  ||
    	    !old_info.getPath().equals(info.getPath()) ||
    	  ( !old_info.getMacros().equals(info.getMacros())  &&  !old_info.getMacros().isEmpty()))
    	    display_info = Optional.of(info);


        setPartName(info.getName());
        setTitleToolTip(info.getPath());
        navigation.setCurrentDisplay(info);
        active_model = model;
    }

    @Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException
    {
		super.init(site, memento);
		// Check if previous run persisted DisplayInfo
		if (memento == null)
			return;
		final String serialized_info = memento.getString(MEMENTO_DISPLAY_INFO);
		if (serialized_info == null)
			return;
		try
		{
			display_info = Optional.of(DisplayInfoXMLUtil.fromXML(serialized_info));
		}
		catch (Exception ex)
		{
			logger.log(Level.WARNING,
					   "Cannot parse model info from " + serialized_info, ex);
		}
	}

    @Override
    public void createPartControl(final Composite parent)
    {
	    RCPHacks.hideUnrelatedUI(getSite().getPage());

        parent.setLayout(new FillLayout());
        fx_canvas = new FXCanvas(parent, SWT.NONE);

        representation = new RCP_JFXRepresentation(this);
        final Scene scene = new Scene(representation.createModelRoot());
        JFXRepresentation.setSceneStyle(scene);
        root = representation.getModelParent();
        root.getProperties().put(ROOT_RUNTIME_VIEW_PART, this);
        fx_canvas.setScene(scene);

        JFXCursorFix.apply(scene, fx_canvas);

        createToolbarItems();

        new ContextMenuSupport(getSite(), fx_canvas, representation);

        parent.addDisposeListener(e -> onDispose());

        // Load persisted DisplayInfo?
        if (display_info.isPresent())
        {
        	loadDisplayFile(display_info.get());
        	// This view was restored by Eclipse after a restart.
        	// It's not opened from an action,
        	// so nobody else will hook the runtime listener:
            RuntimeUtil.hookRepresentationListener(representation);
        }

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.csstudio.display.builder.editor.rcp.display_builder");

        // Representation for each widget adds a context menu just for the widget.
        // Add context menu to scene, tied to the model.
        fx_canvas.getScene().setOnContextMenuRequested(event ->
        {
            final DisplayModel model = active_model;
            if (model != null)
            {
                event.consume();
                representation.fireContextMenu(model);
            }
        });
    }

	public RCP_JFXRepresentation getRepresentation()
	{
	    return representation;
	}

	/** @param zoom Zoom level, 1.0 for 100%, -1 to 'fit'
	 *  @return Zoom level actually used
	 */
	public double setZoom(final double zoom)
    {
        return representation.setZoom(zoom);
    }

    @Override
	public void saveState(final IMemento memento)
    {	// Persist DisplayInfo so it's loaded on application restart
		final DisplayInfo info = display_info.orElse(null);
		if (info == null)
		    return;
		try
		{
		    memento.putString(MEMENTO_DISPLAY_INFO, DisplayInfoXMLUtil.toXML(info));
		}
		catch (Exception ex)
		{
		    logger.log(Level.WARNING, "Cannot persist display info", ex);
		}
	}

	/** Replace UI content with (error) message
     *  @param message Message to show in the part
     */
    private void showMessage(final String message)
    {
        // Assert UI update on UI thread
        representation.execute(() ->
        {
            final Rectangle bounds = fx_canvas.getBounds();

            final TextArea text = new TextArea(message);
            text.setEditable(false);
            text.setPrefSize(bounds.width, bounds.height);

            JFXRepresentation.getChildren(root).setAll(text);
        });
    }

    /** @param message Message to show in the part
     *  @param error Stack trace of error is added to the message
     */
    private void showMessage(final String message, final Throwable error)
    {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        error.printStackTrace(new PrintStream(buf));
        showMessage(message + "\n" + buf.toString());
    }

    /** Load display file, represent it, start runtime
     *  @param info Display file to load
     */
    public void loadDisplayFile(final DisplayInfo info)
    {
        // If already executing another display, shut it down
        disposeModel();

        // Now that old model is no longer represented,
        // show info.
        // Showing this info before disposeModel()
        // would result in old representation not being able
        // to traverse its expected widget tree
        showMessage("Loading " + info);

        // Note the path & macros, then
        display_info = Optional.of(info);
        // load model off UI thread
        RuntimeUtil.getExecutor().execute(() -> loadModel(info));
    }

    /** @return Info about current display or <code>null</code> */
    public DisplayInfo getDisplayInfo()
    {
        return display_info.orElse(null);
    }

    /** Load display model, schedule representation
     *  @param info Display to load
     */
    private void loadModel(final DisplayInfo info)
    {
        try
        {
            final DisplayModel model = RuntimeUtil.loadModel(null, info.getPath());

            // This code is called
            // 1) From OpenDisplayAction
            // No macros in info.
            // 2) On application restart with DisplayInfo from memento
            // info contains snapshot of macros from last run
            // Could simply use info's macros if they are non-empty,
            // but merging macros with those loaded from model file
            // allows for newly added macros in the display file.
            final Macros macros = Macros.merge(model.propMacros().getValue(), info.getMacros());
            model.propMacros().setValue(macros);

            // Schedule representation on UI thread
            representation.execute(() -> representModel(model));
        }
        catch (Exception ex)
        {
            final String message = "Cannot load " + info;
            logger.log(Level.SEVERE, message, ex);
            showMessage(message, ex);
        }
    }

    /** Represent model, schedule start of runtime
     *  @param model Model to represent
     */
    private void representModel(final DisplayModel model)
    {
        try
        {
            JFXRepresentation.getChildren(root).clear();
            representation.representModel(root, model);
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot represent model", ex);
            showMessage("Cannot represent model", ex);
        }

        // Start runtimes in background
        RuntimeUtil.getExecutor().execute(() -> RuntimeUtil.startRuntime(model));
    }

    /** Create tool bar entries */
    private void createToolbarItems()
    {
		final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();

		toolbar.add(new ZoomAction(this));
	    toolbar.add(NavigationAction.createBackAction(this, navigation));
	    toolbar.add(NavigationAction.createForwardAction(this, navigation));
	}

    /** Invoke close_handler for model */
    private void disposeModel()
    {
        final DisplayModel model = active_model;
        active_model = null;
        if (model != null  &&  close_handler != null)
            close_handler.accept(model);
    }

    /** View is closed. Dispose model and toolkit representation */
    private void onDispose()
    {
        disposeModel();
        representation.shutdown();
    }

	@Override
    public void setFocus()
    {
	    fx_canvas.setFocus();
    }
}
