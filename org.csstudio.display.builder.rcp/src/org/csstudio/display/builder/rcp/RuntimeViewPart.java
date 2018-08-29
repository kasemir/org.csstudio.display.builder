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
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.DisplayMacroExpander;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.ModelLoader;
import org.csstudio.display.builder.model.widgets.KnobWidget;
import org.csstudio.display.builder.model.widgets.ScaledSliderWidget;
import org.csstudio.display.builder.model.widgets.ScrollBarWidget;
import org.csstudio.display.builder.model.widgets.TableWidget;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.csstudio.display.builder.rcp.run.ContextMenuSupport;
import org.csstudio.display.builder.rcp.run.DisplayNavigation;
import org.csstudio.display.builder.rcp.run.NavigationAction;
import org.csstudio.display.builder.rcp.run.RCP_JFXRepresentation;
import org.csstudio.display.builder.rcp.run.ZoomAction;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.runtime.ActionUtil;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.javafx.swt.JFXCursorFix;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.fx.ui.workbench3.FXViewPart;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;

/** Part that hosts display builder runtime and JFX scene in SWT
 *
 *  <p>This is a 'view' because views can be saved/switched with Perspectives.
 *  This view does need display information (path, macros),
 *  similar to an RCP 'editor' with input.
 *  The view memento is used to persist the display information
 *  (see comment on <code>persist()</code> for detail).
 *
 *  <p>CS-Studio further extends the perspective handling such
 *  that all the views stored in saved perspectives include the
 *  memento information
 *  (https://github.com/dls-controls/cs-studio/blob/master/core/ui/ui-plugins/org.csstudio.perspectives/src/org/csstudio/perspectives/PerspectiveSaver.java).
 *  The saved perspectives contain MPlaceholders for the views,
 *  which have been extended to include the memento information,
 *  so this code checks for a memento in a placeholder.
 *
 *  @author Kay Kasemir
 *  @author Will Rogers - code related to reading memento from placeholders
 */
@SuppressWarnings("nls")
public class RuntimeViewPart extends FXViewPart
{
	// e4view would allow E4-like POJO, but unclear how representation
	// would then best find the newly created RuntimeViewPart to set its input etc.
	// --> Using E3 FXViewPart
	public static final String ID = "org.csstudio.display.builder.rcp.run.RuntimeViewPart";

    /** Property on the 'root' Group of the JFX scene that holds RuntimeViewPart */
    public static final String ROOT_RUNTIME_VIEW_PART = "_runtime_view_part";

    /** Memento key for DisplayInfo */
    private static final String MEMENTO_DISPLAY_INFO = "DISPLAY_INFO";

    /** Memento key for Zoom */
    private static final String MEMENTO_ZOOM = "ZOOM";

    /** Key for Memento in E4 model */
    private static final String TAG_MEMENTO = "memento";

    /** Back/forward navigation */
    private final DisplayNavigation navigation = new DisplayNavigation();

    /** Display info that may have been received from memento */
    private volatile Optional<DisplayInfo> display_info = Optional.empty();

    /** Zoom, might have been saved in memento */
    private volatile int initial_zoom = 0;

    /** Widget that triggered a context menu */
    private volatile WeakReference<Widget> active_widget = null;

    private RCP_JFXRepresentation representation;

    private Scene scene;

    private Parent root;

    private Consumer<DisplayModel> close_handler = ActionUtil::handleClose;

	private DisplayModel active_model;

	// Life cycle:
	// View is created with a unique ID.
	// This prevents re-use of the same view in multiple perspectives.
	// If a view is closed, RCP disposes it for good,
	// no chance same view is still in another perspective.
	// onDispose() will stop the runtime etc.
	//
	// View can become hidden when moved behind other tab, minimized,
	// selecting a different perspective.
	//
	// Stopping & restarting the runtime when view is hidden/revealed
	// would save the most CPU, but restart takes enough time for user
	// to notice initial disconnect state, and certain widgets (plots)
	// would loose their history.
	// Profiling revealed that FX Canvas updates are already suppressed
	// by the framework, and pausing the representation skips the JFX node updates,
	// resulting in significant CPU reduction while hidden.
	private final IPartListener2 show_hide_listener = new IPartListener2()
    {
        @Override
        public void partHidden(final IWorkbenchPartReference ref)
        {
            if (ref.getPart(false) == RuntimeViewPart.this)
                representation.enable(false);
        }

        @Override
        public void partVisible(final IWorkbenchPartReference ref)
        {
            if (ref.getPart(false) == RuntimeViewPart.this)
                representation.enable(true);
        }

        @Override public void partOpened(IWorkbenchPartReference ref)       { /* Ignore */ }
        @Override public void partInputChanged(IWorkbenchPartReference ref) { /* Ignore */ }
        @Override public void partDeactivated(IWorkbenchPartReference ref)  { /* Ignore */ }
        @Override public void partClosed(IWorkbenchPartReference ref)       { /* Ignore */ }
        @Override public void partBroughtToTop(IWorkbenchPartReference ref) { /* Ignore */ }
        @Override public void partActivated(IWorkbenchPartReference ref)    { /* Ignore */ }
    };

    /** Open a runtime display
     *
     *  <p>Either opens a new display, or if there is already an existing view
     *  for that input, "activate" it, which pops a potentially hidden view to the top.
     *
     *  @param page Page to use. <code>null</code> for 'active' page
     *  @param close_handler Code to call when part is closed
     *  @param info DisplayInfo (to compare with currently open displays)
     *  @return {@link RuntimeViewPart}
     *  @throws Exception on error
     */
    public static RuntimeViewPart open(IWorkbenchPage page, final Consumer<DisplayModel> close_handler, final DisplayInfo info)
            throws Exception
    {
        if (page == null)
            page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
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

    @Override
    public void init(final IViewSite site, IMemento memento) throws PartInitException
    {
    	super.init(site, memento);

    	// Check if previous run persisted DisplayInfo
    	String serialized_info = null;

    	if (memento == null)
    	    memento = findMementoFromPlaceholder();

    	if (memento != null)
    	{
    	    serialized_info = memento.getString(MEMENTO_DISPLAY_INFO);
    	    final Integer mem_zoom = memento.getInteger(MEMENTO_ZOOM);
    	    if (mem_zoom != null)
    	        initial_zoom = mem_zoom;
    	}

    	if (serialized_info != null)
    	{
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

        persist();
    }

    /** Retrieve memento persisted in MPlaceholder if present.
     *  @return {@link IMemento} persisted in the placeholder.
     */
    private IMemento findMementoFromPlaceholder()
    {
        IMemento memento = null;
        MPlaceholder placeholder = findPlaceholder();
        if (placeholder != null) {
            if (placeholder.getPersistedState().containsKey(TAG_MEMENTO))
            {
                String mementoString = placeholder.getPersistedState().get(TAG_MEMENTO);
                memento = loadMemento(mementoString);
            }
        }
        return memento;
    }

    /** @param mementoString String with serialized mememto
     *  @return {@link IMemento} parsed from string
     */
    private IMemento loadMemento(String mementoString)
    {
        StringReader reader = new StringReader(mementoString);
        try
        {
            return XMLMemento.createReadRoot(reader);
        }
        catch (WorkbenchException e)
        {
            logger.log(Level.WARNING, "Failed to load memento", e);
            return null;
        }
    }

    /** Find the MPlaceholder corresponding to this MPart in the MPerspective.  This
     *  may have persisted information relevant to loading this view.
     *  @return corresponding placeholder or <code>null</code>
     */
    private MPlaceholder findPlaceholder()
    {
        final IEclipseContext localContext = getViewSite().getService(IEclipseContext.class);
        final MPart part = localContext.get(MPart.class);
        final EModelService service = PlatformUI.getWorkbench().getService(EModelService.class);
        final IEclipseContext globalContext = PlatformUI.getWorkbench().getService(IEclipseContext.class);
        final MApplication app = globalContext.get(MApplication.class);
        final List<MPlaceholder> phs = service.findElements(app, null, MPlaceholder.class, null);
        for (MPlaceholder ph : phs)
            if (ph.getRef() == part)
                return ph;
        return null;
    }

    @Override
    public void createPartControl(final Composite parent)
    {
        RCPHacks.hideUnrelatedUI(getSite().getPage());
        parent.setLayout(new FillLayout());

        // This calls createFxScene()
        super.createPartControl(parent);

        // The child added last should be the new FXCanvas
        final Control[] children = parent.getChildren();
        final Control fx_canvas = children[children.length - 1];
        if (!  fx_canvas.getClass().getName().contains("FXCanvas"))
            throw new IllegalStateException("Expected FXCanvas, got " + fx_canvas);

        JFXCursorFix.apply(scene, parent.getDisplay());

        createToolbarItems();

        new ContextMenuSupport(this, fx_canvas, representation);


        if (! representation.isEditMode())
        {
            // DnD hack
            // Want to allow dragging widget's PV name,
            // so it can for example be dropped into data browser.
            // In principle, that should be done via JavaFX onDrag*() calls
            // on the widget's representation - as it is in the pure JFX Phobus version.
            // But when running inside FXCanvas, those drag events are
            // delivered to an outside editor or to another SWT target.
            // They are not, however, delivered to another JFX drop target
            // inside another FXCanvas
            // -> Use SWT drag & drop, configured on the FXCanvas.
            //    This needs to know which widget to use,
            //    which is done via getActiveWidget(),
            //    updated by a 'click' filter in JFXBaseRepresentation.
            // This might require first clicking, then click-dragging
            // to be 100% certain that the correct widget is dragged...
            final DragSource source = new DragSource(fx_canvas, DND.DROP_COPY);
            source.setTransfer(new Transfer[] { TextTransfer.getInstance() });
            source.addDragListener(new DragSourceListener()
            {
                @Override
                public void dragStart(final DragSourceEvent event)
                {
                    // More hack: SWT DnD would start from anywhere within the JFX scene,
                    // but that scene might include scrollbars on the right and/or bottom.
                    // A little hard to tell if they are active right now,
                    // and their exact size is also not known.
                    // As a hack, simply avoid dragging from some BORDER at right and bottom.
                    final Point size = fx_canvas.getSize();
                    final int BORDER = 20;
                    if (event.x > size.x - BORDER   ||
                        event.y > size.y - BORDER)
                    {
                        event.doit = false;
                        return;
                    }

                    final Widget widget = getActiveWidget();
                    // Only drag from widgets that have a "pv_name".
                    // Skip slider type widgets where we want to operate the widget by dragging.
                    // Skip table widget because we drag to resize columns or operate its scroll bars.
                    if (widget == null  ||
                        !widget.checkProperty("pv_name").isPresent() ||
                        widget instanceof ScaledSliderWidget ||
                        widget instanceof ScrollBarWidget ||
                        widget instanceof TableWidget ||
                        widget instanceof ImageWidget ||
                        widget instanceof KnobWidget)
                        event.doit = false;
                }

                @Override
                public void dragSetData(final DragSourceEvent event)
                {
                    final Widget widget = getActiveWidget();
                    if (widget == null)
                        return;
                    Optional<WidgetProperty<String>> prop = widget.checkProperty("pv_name");
                    if (prop.isPresent())
                        event.data = prop.get().getValue();
                }

                @Override
                public void dragFinished(final DragSourceEvent event)
                {
                    // NOP
                }
            });
        }

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
        scene.setOnContextMenuRequested(event ->
        {
            final DisplayModel model = active_model;
            if (model != null)
            {
                event.consume();
                representation.fireContextMenu(model);
            }
        });

        // Track when view is hidden/restored
        // Only add once, so remove previous one
        getSite().getPage().removePartListener(show_hide_listener);
        getSite().getPage().addPartListener(show_hide_listener);
    }

    @Override
    protected Scene createFxScene()
    {
        representation = new RCP_JFXRepresentation(this);
        scene = new Scene(representation.createModelRoot());
        JFXRepresentation.setSceneStyle(scene);
        root = representation.getModelParent();
        root.getProperties().put(ROOT_RUNTIME_VIEW_PART, this);
        return scene;
    }

	@Override
    protected void setFxFocus()
    {
	    if (root != null)
	        root.requestFocus();
    }

    public RCP_JFXRepresentation getRepresentation()
	{
	    return representation;
	}

	/** Persist the view's input "on demand".
     *
     *  <p>Framework only persists to memento on exit,
     *  and only for the currently visible views.
     *
     *  <p>Display info for views in other perspectives
     *  which are hidden on application shutdown will be lost.
     *  By forcing a persist for each view while the app is still running,
     *  each view can be restored when a perspective is later re-activated.
     *
     *
     *  <p>Memento is saved in the
     *  .metadata/.plugins/org.eclipse.e4.workbench/workbench.xmi
     *  inside a "persistedState" element of the E4 model element.
     *
     *  <p>This method places it in the model just as the framework
     *  does by calling saveState() on shutdown,
     *  but allows saving the state at any time.
     */
	private void persist()
	{
        try
        {
            // Obtain E4 model element for E3 view,
            // based on http://www.vogella.com/tutorials/EclipsePlugIn/article.html#eclipsecontext
            final IEclipseContext context = getViewSite().getService(IEclipseContext.class);
            final MPart part = context.get(MPart.class);

            // Based on org.eclipse.ui.internal.ViewReference#persist():
            //
            // XML version of memento is written to E4 model.
            // If compatibility layer changes its memento persistence,
            // this will break...
            final XMLMemento root = XMLMemento.createWriteRoot("view"); //$NON-NLS-1$
            saveState(root);
            final StringWriter writer = new StringWriter();
            root.save(writer);
            part.getPersistedState().put(TAG_MEMENTO, writer.toString());
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot persist " + display_info, ex);
        }
	}

    @Override
	public void saveState(final IMemento memento)
    {
        // Persist DisplayInfo so it's loaded on application restart
		final DisplayInfo info = display_info.orElse(null);
		if (info == null)
		    return;
		try
		{
		    memento.putString(MEMENTO_DISPLAY_INFO, DisplayInfoXMLUtil.toXML(info));
		    memento.putInteger(MEMENTO_ZOOM, (int) (representation.getZoom() * 100));
		}
		catch (Exception ex)
		{
		    logger.log(Level.WARNING, "Cannot persist " + display_info, ex);
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
            final TextArea text = new TextArea(message);
            text.setEditable(false);
            text.setPrefSize(1000, 800);
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

    /** @return Info about current display model or <code>null</code> */
    public DisplayModel getDisplayModel()
    {
        return active_model;
    }

    /** @return Info about current display or <code>null</code> */
    public DisplayInfo getDisplayInfo()
    {
        return display_info.orElse(null);
    }

    /** @return Widget that's active when opening a context menu. May be <code>null</code> */
    public Widget getActiveWidget()
    {
        final WeakReference<Widget> ref = active_widget;
        if (ref == null)
            return null;
        return ref.get();
    }

    /** Set active widget
     *
     *  <p>Only to be called from {@link ContextMenuSupport}.
     *
     *  @param widget Widget on which the context menu was called
     */
    public void setActiveWidget(final Widget widget)
    {
        // Keeps a weak reference.
        // Keeping a strong reference would prevent the widget from
        // being GC'ed when the display changes and no other
        // context menu is ever invoked.
        // Ideally, menu would clear the active widget when hidden,
        // but lifecycle is as follows:
        // 1) menu opens
        // 2) menu item is selected, setting the active widget
        // 3) menu closes
        // 4) handler for selected item is invoked
        // .. and the handler wants to get the active widget,
        // so it can't be cleared in step 3.
        if (widget == null)
            active_widget = null;
        else
            active_widget = new WeakReference<Widget>(widget);
    }

    /** Load display model, schedule representation
     *  @param info Display to load
     */
    private void loadModel(final DisplayInfo info)
    {
        try
        {
            final DisplayModel model = info.shouldResolve()
                ? ModelLoader.resolveAndLoadModel(null, info.getPath())
                : ModelLoader.loadModel(info.getPath());

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

            // For runtime, expand macros
            if (! representation.isEditMode())
                DisplayMacroExpander.expandDisplayMacros(model);

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
            if (initial_zoom > 0)
            {
                representation.requestZoom(initial_zoom + "%");
                initial_zoom = 0;
            }
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
        // No longer track when view is hidden/restored
        getSite().getPage().removePartListener(show_hide_listener);
    }
}
