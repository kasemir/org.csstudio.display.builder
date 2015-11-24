/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Group;
import javafx.scene.Scene;

/** Part that hosts display builder runtime
 *
 *  <p>Hosts FXCanvas in SWT
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RuntimeViewPart extends ViewPart
{
    // TODO Update part name with model name
    // TODO back/forward navigation
    // TODO Open "new" view
    // TODO Zoom, scrollbars

	// FXViewPart could save a tiny bit code, but this may allow more control.
	// e4view would allow E4-like POJO, but unclear how representation
	// would then best find the newly created RuntimeViewPart to set its input etc.
	// --> Using E3 ViewPart
    public final static String ID = "org.csstudio.display.builder.rcp.run.RuntimeViewPart";

    private final Logger logger = Logger.getLogger(getClass().getName());

    private FXCanvas fx_canvas;

    /** Display file for the active display
     *  Only accessed on UI thread
     */
    private String active_display_file = null;

    /** Model for the active display
     *  Only accessed on UI thread
     */
    private DisplayModel active_model = null;

    /** Open a runtime display
     *
     *  @param display_file
     *  @return {@link RuntimeViewPart}
     *  @throws Exception on error
     */
    public static RuntimeViewPart open(final String display_file) throws Exception
    {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final RuntimeViewPart part = (RuntimeViewPart) page.showView(ID, UUID.randomUUID().toString(), IWorkbenchPage.VIEW_ACTIVATE);
        part.setDisplayFile(display_file);
        return part;
    }

    @Override
    public void createPartControl(final Composite parent)
    {
        fx_canvas = new FXCanvas(parent, SWT.NONE);

        // TODO Get model from saved memento
        // setDisplayFile("https://webopi.sns.gov/webopi/opi/Instruments.opi");

        createContextMenu(parent);

        parent.addDisposeListener(e -> disposeModel());
    }

    /** Load display file, represent it, start runtime
     *  @param display_file Display file to load
     */
    private void setDisplayFile(final String display_file)
    {
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
            final DisplayModel model = RuntimeUtil.loadModel(active_display_file , display_file);
            active_display_file = display_file;
            final RCP_JFXRepresentation representation = RCP_JFXRepresentation.getInstance();
            // Schedule representation on UI thread
            representation.execute(() -> representModel(model));
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot load " + display_file, ex);
            // TODO Show error in part
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

            final Scene scene = representation.createScene(model);
            final Group root = representation.getSceneRoot(scene);
            representation.representModel(root, model);
            fx_canvas.setScene(scene);
            active_model = model;
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Cannot represent model", ex);
        }

        // Start runtimes in background
        RuntimeUtil.getExecutor().execute(() -> RuntimeUtil.startRuntime(model));
    }

    /** Dummy SWT context menu to test interaction of SWT and JFX context menues */
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

    /*** Dispose representation and runtime of active model */
    private void disposeModel()
    {
        if (active_model != null)
        {
            RuntimeUtil.stopRuntime(active_model);
            RCP_JFXRepresentation.getInstance().disposeRepresentation(active_model);
            active_model = null;
        }
    }

	@Override
    public void setFocus()
    {
	    fx_canvas.setFocus();
    }
}
