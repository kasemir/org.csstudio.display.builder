/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import static org.csstudio.display.builder.editor.rcp.Plugin.logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.rcp.actions.CopyAction;
import org.csstudio.display.builder.editor.rcp.actions.PasteAction;
import org.csstudio.display.builder.editor.rcp.actions.RedoAction;
import org.csstudio.display.builder.editor.rcp.actions.UndoAction;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.util.undo.UndoRedoListener;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
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
    final private static String FILE_EXTENSION = "bob";

    private final JFXRepresentation toolkit = new JFXRepresentation(true);

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

    private final WidgetPropertyListener<String> model_name_listener = (property, old_value, new_value) ->
    {
        toolkit.execute(() ->  setPartName(property.getValue()));
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

        // Observed UI freeze in this call.
        // Unsure what to do.
        // Scene could be created in background,
        // but setting the canvas' scene has to be on UI thread
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
            final String ws_location = file.getFullPath().toOSString();
            final ModelReader reader = new ModelReader(ModelResourceUtil.openResourceStream(ws_location));
            final DisplayModel model = reader.readModel();
            model.setUserData(DisplayModel.USER_DATA_INPUT_FILE, ws_location);
            return model;
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
        final DisplayModel old_model = editor.getModel();
        if (old_model != null)
            old_model.widgetName().removePropertyListener(model_name_listener);
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
        model.widgetName().addPropertyListener(model_name_listener);
    }

    private void createActions()
    {
        actions.put(ActionFactory.UNDO.getId(), new UndoAction(editor));
        actions.put(ActionFactory.REDO.getId(), new RedoAction(editor));
        actions.put(ActionFactory.COPY.getId(), new CopyAction(editor));
        actions.put(ActionFactory.PASTE.getId(), new PasteAction(editor));
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

    // Called from ExecuteDisplayAction,
    // which expects monitor.done() to be called on UI thread
    // when successful
    @Override
    public void doSave(final IProgressMonitor monitor)
    {
        IFile file = getInputFile();
        if (file != null   &&  FILE_EXTENSION.equals(file.getFileExtension()))
            saveModelToFile(monitor, file);
        else
        {   // No file name, or using legacy file extension -> prompt for name
            file = promptForFile();
            if (file == null)
            {
                monitor.setCanceled(true);
                monitor.done();
            }
            else
                saveModelToFile(monitor, file);
        }
    }

    @Override
    public void doSaveAs()
    {
        final IFile file = promptForFile();
        if (file != null)
            saveModelToFile(new NullProgressMonitor(), file);
    }

    /** Save model to file, using background thread
     *  @param save_monitor On success, <code>done</code> will be called <u>on UI thread</u>. Otherwise cancelled.
     *  @param file File to save
     */
    private void saveModelToFile(final IProgressMonitor save_monitor, final IFile file)
    {   // Save on background thread
        final Job job = new Job("Save")
        {
            @Override
            protected IStatus run(final IProgressMonitor monitor)
            {
                final SubMonitor progress = SubMonitor.convert(monitor, 100);
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
                    progress.worked(40);

                    // .. then write file from buffer
                    final ByteArrayInputStream stream = new ByteArrayInputStream(tmp.toByteArray());
                    if (file.exists())
                        file.setContents(stream, true, false, monitor);
                    else
                        file.create(stream, true, monitor);
                    progress.worked(40);
                }
                catch (Exception ex)
                {
                    logger.log(Level.SEVERE, "Cannot save as " + file, ex);
                    save_monitor.setCanceled(true);
                    save_monitor.done();
                    return Status.OK_STATUS;
                }

                // Back on UI thread..
                final IEditorInput input = new FileEditorInput(file);
                final Future<Object> update_input = toolkit.submit(() ->
                {   // Update editor input to current file name
                    setInput(input);
                    setPartName(model.getName());
                    setTitleToolTip(input.getToolTipText());

                    // Clear 'undo'
                    editor.getUndoableActionManager().clear();

                    // Signal success
                    save_monitor.done();
                    return null;
                });
                // Wait for the UI task to complete
                try
                {
                    update_input.get();
                }
                catch (Exception ex)
                {
                    logger.log(Level.WARNING, "Cannot update editor input", ex);
                    save_monitor.setCanceled(true);
                    save_monitor.done();
                }
                progress.done();

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
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        final SaveAsDialog dlg = new SaveAsDialog(getSite().getShell());
        dlg.setBlockOnOpen(true);
        final IEditorInput input = getEditorInput();
        if (input instanceof FileEditorInput)
        {
            IPath orig_path = ((FileEditorInput)input).getFile().getFullPath();
            // Propose new file extension
            if (! FILE_EXTENSION.equals(orig_path.getFileExtension()))
                orig_path = orig_path.removeFileExtension().addFileExtension(FILE_EXTENSION);
            dlg.setOriginalFile(root.getFile(orig_path));
        }
        if (dlg.open() != Window.OK)
            return null;

        // Path to the new resource relative to the workspace
        IPath path = dlg.getResult();
        if (path == null)
            return null;
        // Assert correct file extension
        if (! FILE_EXTENSION.equals(path.getFileExtension()))
            path = path.removeFileExtension().addFileExtension(FILE_EXTENSION);
        return root.getFile(path);
    }

    /** @return {@link DisplayInfo} for file in editor */
    public DisplayInfo getDisplayInfo()
    {
        final IFile file = getInputFile();
        // Providing workspace location, which is handled in ModelResourceUtil
        return new DisplayInfo(file.getFullPath().toOSString(), editor.getModel().getName(), new Macros());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object getAdapter(final Class adapter)
    {
        if (adapter == IContentOutlinePage.class)
        {
            outline_page = new OutlinePage(editor.getWidgetSelectionHandler());
            outline_page.setModel(editor.getModel());
            return outline_page;
        }
        else if (adapter == IPropertySheetPage.class)
            return new PropertyPage(editor);
        return super.getAdapter(adapter);
    }

    @Override
    public void dispose()
    {
        editor.getUndoableActionManager().removeListener(undo_redo_listener);
        editor.dispose();
        super.dispose();
    }
}
