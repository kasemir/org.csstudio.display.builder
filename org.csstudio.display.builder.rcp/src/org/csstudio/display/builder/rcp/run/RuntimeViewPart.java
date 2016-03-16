/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.DisplayInfoXMLUtil;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.runtime.ActionUtil;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
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
    static final String ROOT_RUNTIME_VIEW_PART = "_runtime_view_part";

    /** Memento key for DisplayInfo */
    private static final String MEMENTO_DISPLAY_INFO = "DISPLAY_INFO";

    /** Back/forward navigation */
    private final DisplayNavigation navigation = new DisplayNavigation();

    private final Logger logger = Logger.getLogger(getClass().getName());

    /** Display info that may have been received from memento */
    private Optional<DisplayInfo> display_info = Optional.empty();

    private FXCanvas fx_canvas;

    private RCP_JFXRepresentation representation;

    private Parent root;

    private Consumer<DisplayModel> close_handler = ActionUtil::handleClose;

	private DisplayModel active_model;


    /** Open a runtime display
     *  @param close_handler Code to call when part is closed
     *  @return {@link RuntimeViewPart}
     *  @throws Exception on error
     */
    public static RuntimeViewPart open(final Consumer<DisplayModel> close_handler) throws Exception
    {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final RuntimeViewPart part = (RuntimeViewPart) page.showView(ID, UUID.randomUUID().toString(), IWorkbenchPage.VIEW_ACTIVATE);
        part.close_handler = close_handler;
        return part;
    }

    public Parent getRoot()
    {
        return root;
    }

    /** @param name Name of the part */
    public void trackCurrentModel(final DisplayModel model)
    {
    	final DisplayInfo info = new DisplayInfo(model.getUserData(DisplayModel.USER_DATA_INPUT_FILE),
    			                                 model.getName(),
    			                                 model.widgetMacros().getValue());
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
        parent.setLayout(new FillLayout());
        fx_canvas = new FXCanvas(parent, SWT.NONE);

        representation = new RCP_JFXRepresentation();
        final Scene scene = representation.createScene();
        root = representation.getSceneRoot(scene);
        root.getProperties().put(ROOT_RUNTIME_VIEW_PART, this);
        fx_canvas.setScene(scene);

        createToolbarItems();

        new ContextMenuSupport(getSite(), fx_canvas, representation);

        parent.addDisposeListener(e -> disposeModel());

        // Load persisted DisplayInfo
        if (display_info.isPresent())
        	loadDisplayFile(display_info.get());
    }

	/** @param zoom Zoom level, 1.0 for 100%, -1 to 'fit'
	 *  @return Zoom level actually used
	 */
	public double setZoom(final double zoom)
    {
        final Scene scene = fx_canvas.getScene();
        return representation.setSceneZoom(scene, zoom);
    }

    @Override
	public void saveState(final IMemento memento)
    {	// Persist DisplayInfo so it's loaded on application restart
    	final DisplayModel model = active_model;
    	if (model == null)
    		return;
		final DisplayInfo info = new DisplayInfo(model.getUserData(DisplayModel.USER_DATA_INPUT_FILE),
				                                 model.getName(),
				                                 model.widgetMacros().getValue());
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
        showMessage("Loading " + info);
        // If already executing another display, shut it down
        disposeModel();
        // Load model off UI thread
        RuntimeUtil.getExecutor().execute(() -> loadModel(info));
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
            final Macros macros = Macros.merge(model.widgetMacros().getValue(), info.getMacros());
            model.widgetMacros().setValue(macros);

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

    /*** Invoke close_handler for model */
    private void disposeModel()
    {
        final DisplayModel model = active_model;
        if (model != null  &&  close_handler != null)
            close_handler.accept(model);
    }

	@Override
    public void setFocus()
    {
	    fx_canvas.setFocus();
    }
}
