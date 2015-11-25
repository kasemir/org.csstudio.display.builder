/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Group;
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
    /** Property on the 'root' Group of the JFX scene that holds RuntimeViewPart */
    static final String ROOT_RUNTIME_VIEW_PART = "_runtime_view_part";

    // TODO back/forward navigation
    // TODO Zoom, scrollbars

	// FXViewPart saves a tiny bit of code, but this allow more control over the FXCanvas.
	// e4view would allow E4-like POJO, but unclear how representation
	// would then best find the newly created RuntimeViewPart to set its input etc.
	// --> Using E3 ViewPart
    public final static String ID = "org.csstudio.display.builder.rcp.run.RuntimeViewPart";

    private final Logger logger = Logger.getLogger(getClass().getName());

    private FXCanvas fx_canvas;

    private Group root;

    private Consumer<DisplayModel> close_handler = null;

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

    public Group getRoot()
    {
        return root;
    }

    /** @param name Name of the part */
    public void trackCurrentModel(final DisplayModel model)
    {   // TODO Might need model input file plus macro params
        //      to allow forward/backward navigation
        super.setPartName(model.getName());
    }

    @Override
    public void createPartControl(final Composite parent)
    {
        parent.setLayout(new FillLayout());
        fx_canvas = new FXCanvas(parent, SWT.NONE);

        // TODO Get model from saved memento
        // setDisplayFile("https://webopi.sns.gov/webopi/opi/Instruments.opi");

        RCP_JFXRepresentation representation = RCP_JFXRepresentation.getInstance();
        final Scene scene = representation.createScene();
        root = representation.getSceneRoot(scene);
        root.getProperties().put(ROOT_RUNTIME_VIEW_PART, this);
        fx_canvas.setScene(scene);

        createContextMenu(parent);

        parent.addDisposeListener(e -> disposeModel());
    }

    /** Replace UI content with (error) message
     *  @param message Message to show in the part
     */
    private void showMessage(final String message)
    {
        // Assert UI update on UI thread
        RCP_JFXRepresentation.getInstance().execute(() ->
        {
            final Rectangle bounds = fx_canvas.getBounds();

            final TextArea text = new TextArea(message);
            text.setEditable(false);
            text.setPrefSize(bounds.width, bounds.height);

            root.getChildren().setAll(text);
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
     *  @param display_file Display file to load
     */
    public void loadDisplayFile(final String display_file)
    {
        showMessage("Loading " + display_file);
        // Load model off UI thread
        RuntimeUtil.getExecutor().execute(() -> loadModel(display_file));
    }

    /** Load display model, schedule representation
     *  @param display_file Display file to load
     */
    private void loadModel(final String display_file)
    {
        try
        {
            final DisplayModel model = RuntimeUtil.loadModel(null, display_file);

            // Schedule representation on UI thread
            final RCP_JFXRepresentation representation = RCP_JFXRepresentation.getInstance();
            representation.execute(() -> representModel(model));
        }
        catch (Exception ex)
        {
            final String message = "Cannot load " + display_file;
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
            final RCP_JFXRepresentation representation = RCP_JFXRepresentation.getInstance();
            root.getChildren().clear();
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

    /** Dummy SWT context menu to test interaction of SWT and JFX context menus */
    private void createContextMenu(final Control parent)
    {
    	final MenuManager mm = new MenuManager();
    	mm.add(new Action("SWT Test Menu")
    	{
			@Override
			public void run()
			{
				System.out.println("Invoked SWT Context menu");
			}
		});
    	final Menu menu = mm.createContextMenu(parent);
    	parent.setMenu(menu);
    }

    /*** Invoke close_handler for model */
    private void disposeModel()
    {
        final DisplayModel model = (DisplayModel) getRoot().getProperties().get(JFXRepresentation.ACTIVE_MODEL);
        if (model != null  &&  close_handler != null)
            close_handler.accept(model);
    }

	@Override
    public void setFocus()
    {
	    fx_canvas.setFocus();
    }
}
