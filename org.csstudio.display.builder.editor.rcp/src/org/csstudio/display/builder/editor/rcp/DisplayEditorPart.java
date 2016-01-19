/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.rcp.actions.RedoAction;
import org.csstudio.display.builder.editor.rcp.actions.UndoAction;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.util.undo.UndoRedoListener;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Parent;
import javafx.scene.Scene;

/** RCP 'Editor' for the display builder
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayEditorPart extends EditorPart
{
    /** File extension used to save files */
    final private static String FILE_EXTENSION = "opi";

    private final JFXRepresentation toolkit = new JFXRepresentation();

    private final Logger logger = Logger.getLogger(getClass().getName());

    private FXCanvas fx_canvas;

    private final DisplayEditor editor = new DisplayEditor(toolkit);

    private OutlinePage outline_page = null;

    /** Actions by ID */
    private Map<String, IAction> actions = new HashMap<>();

    private UndoRedoListener undo_redo_listener = (undo, redo) ->
    {
        actions.get(ActionFactory.UNDO.getId()).setEnabled(undo != null);
        actions.get(ActionFactory.REDO.getId()).setEnabled(redo != null);
        firePropertyChange(IEditorPart.PROP_DIRTY);
    };


    public DisplayEditorPart()
    {
        // TODO Context menu
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input)
            throws PartInitException
    {
        setSite(site);
        // TODO setInput(new NoResourceEditorInput(input));
        setInput(input);
    }

    @Override
    public void createPartControl(final Composite parent)
    {
        parent.setLayout(new FillLayout());

        // Must first create FXCanvas, because that initializes JFX.
        // When creating FXCanvas later, there will be JFX errors
        // like "Not on FX application thread", "Toolkit not initialized"
        fx_canvas = new FXCanvas(parent, SWT.NONE);

        final Parent root = editor.create();
        final Scene scene = new Scene(root);
        EditorUtil.setSceneStyle(scene);

        fx_canvas.setScene(scene);

        createActions();

        final IEditorInput input = getEditorInput();
        final IFile file = input.getAdapter(IFile.class);
        if (file != null)
            loadModel(file);

        editor.getUndoableActionManager().addListener(undo_redo_listener);
    }

    private void loadModel(final IFile file)
    {
        // Load model in background thread, then set it
        CompletableFuture.supplyAsync(() -> doLoadModel(file), EditorUtil.getExecutor())
                         .thenAccept(this::setModel);
    }

    private DisplayModel doLoadModel(final IFile file)
    {
        try
        {
            final ModelReader reader = new ModelReader(new FileInputStream(file.getLocation().toFile()));
            return reader.readModel();
        }
        catch (Exception ex)
        {
            final String message = "Cannot load display " + file;
            logger.log(Level.WARNING, message, ex);
            final Shell shell = getSite().getShell();
            // Also show error to user
            shell.getDisplay().asyncExec(() ->
                ExceptionDetailsErrorDialog.openError(shell, message, ex));
            return null;
        }
    }

    private void setModel(final DisplayModel model)
    {
        if (model == null)
            return;
        // In UI thread..
        toolkit.execute(() ->
        {
            setPartName(model.getName());
            editor.setModel(model);
            if (outline_page != null)
                outline_page.setModel(model);
        });
    }

    private void createActions()
    {
        final UndoableActionManager undo = editor.getUndoableActionManager();
        actions.put(ActionFactory.UNDO.getId(), new UndoAction(undo));
        actions.put(ActionFactory.REDO.getId(), new RedoAction(undo));
    }

    /** @return {@link DisplayEditor} */
    public DisplayEditor getDisplayEditor()
    {
        return editor;
    }

    /** Get action resp. handler for retargetable actions
     *  @param id Action ID
     *  @return Action for that ID
     */
    public IAction getAction(final String id)
    {
        return actions.get(id);
    }

    @Override
    public void setFocus()
    {
        fx_canvas.setFocus();
    }

    @Override
    public boolean isDirty()
    {
        return editor.getUndoableActionManager().canUndo();
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return true;
    }

    /** @return Workspace file for current editor input */
    private IFile getInputFile()
    {
        IEditorInput input = getEditorInput();
        if (input instanceof FileEditorInput)
            return ((FileEditorInput)input).getFile();
        return null;
    }

    @Override
    public void doSave(final IProgressMonitor monitor)
    {
        final IFile file = getInputFile();
        if (file != null)
            saveModelToFile(file);
        else
            doSaveAs();
    }

    @Override
    public void doSaveAs()
    {
        final IFile file = promptForFile();
        if (file != null)
            saveModelToFile(file);
    }

    /** Save model to file, using background thread
     *  @param file File to save
     */
    private void saveModelToFile(final IFile file)
    {   // Save on background thread
        Job job = new Job("Save")
        {
            @Override
            protected IStatus run(final IProgressMonitor monitor)
            {
                final DisplayModel model = editor.getModel();
                logger.log(Level.FINE, "Save as {0}", file);

                // Want to use IFile API to get automated workspace update,
                // but that requires a stream. So first persist into memory buffer..
                try
                {
                    final ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                    final ModelWriter writer = new ModelWriter(tmp);
                    writer.writeModel(model);
                    writer.close();

                    // .. then write file from buffer
                    final ByteArrayInputStream stream = new ByteArrayInputStream(tmp.toByteArray());
                    if (file.exists())
                        file.setContents(stream, true, false, monitor);
                    else
                        file.create(stream, true, monitor);
                }
                catch (Exception ex)
                {
                    logger.log(Level.SEVERE, "Cannot save as " + file, ex);
                    return Status.OK_STATUS;
                }

                // Back on UI thread..
                final IEditorInput input = new FileEditorInput(file);
                toolkit.execute(() ->
                {   // Update editor input to current file name
                    setInput(input);
                    setPartName(model.getName());
                    setTitleToolTip(input.getToolTipText());

                    // Clear 'undo'
                    editor.getUndoableActionManager().clear();
                });
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    /** Prompt for file name used to 'save'
     *  @return File in workspace or <code>null</code>
     */
    private IFile promptForFile()
    {
        final SaveAsDialog dlg = new SaveAsDialog(getSite().getShell());
        dlg.setBlockOnOpen(true);
        final IEditorInput input = getEditorInput();
        if (input instanceof FileEditorInput)
            dlg.setOriginalFile(((FileEditorInput)input).getFile());
        if (dlg.open() != Window.OK)
            return null;

        // Path to the new resource relative to the workspace
        IPath path = dlg.getResult();
        if (path == null)
            return null;
        // Assert it's an '.opi' file
        final String ext = path.getFileExtension();
        if (ext == null  ||  !ext.equals(FILE_EXTENSION))
            path = path.removeFileExtension().addFileExtension(FILE_EXTENSION);
        // Get the file for the new resource's path.
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        return root.getFile(path);
    }

    /** @return {@link DisplayInfo} for file in editor */
    public DisplayInfo getDisplayInfo()
    {
        final IFile file = getInputFile();

        // TODO Use workspace location, file.getFullPath(),
        // and have org.csstudio.display.builder.model.util.ResourceUtil handle it
        return new DisplayInfo(file.getLocation().toOSString(), editor.getModel().getName(), new Macros());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object getAdapter(final Class adaptable)
    {
        if (adaptable == IContentOutlinePage.class)
        {
            outline_page = new OutlinePage(editor.getWidgetSelectionHandler());
            outline_page.setModel(editor.getModel());
            return outline_page;
        }
        else if (adaptable == IPropertySheetPage.class)
            return new PropertyPage(editor.getWidgetSelectionHandler(),
                                           editor.getUndoableActionManager());
        return super.getAdapter(adaptable);
    }

    @Override
    public void dispose()
    {
        editor.getUndoableActionManager().removeListener(undo_redo_listener);
        editor.dispose();
        super.dispose();
    }
}
