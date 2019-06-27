/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import static org.csstudio.display.builder.editor.rcp.Plugin.logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.rcp.actions.CopyAction;
import org.csstudio.display.builder.editor.rcp.actions.CreateGroupAction;
import org.csstudio.display.builder.editor.rcp.actions.CutDeleteAction;
import org.csstudio.display.builder.editor.rcp.actions.ExecuteDisplayAction;
import org.csstudio.display.builder.editor.rcp.actions.ExecuteDisplayButton;
import org.csstudio.display.builder.editor.rcp.actions.PasteAction;
import org.csstudio.display.builder.editor.rcp.actions.RedoAction;
import org.csstudio.display.builder.editor.rcp.actions.ReloadClassesAction;
import org.csstudio.display.builder.editor.rcp.actions.ReloadDisplayAction;
import org.csstudio.display.builder.editor.rcp.actions.RemoveGroupAction;
import org.csstudio.display.builder.editor.rcp.actions.SelectAllAction;
import org.csstudio.display.builder.editor.rcp.actions.UndoAction;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetClassSupport;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.ModelLoader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.persist.WidgetClassesService;
import org.csstudio.display.builder.model.util.ModelThreadPool;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.Preferences;
import org.csstudio.display.builder.representation.javafx.AutocompleteMenu;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.csstudio.display.builder.util.undo.UndoRedoListener;
import org.csstudio.javafx.swt.JFXCursorFix;
import org.csstudio.javafx.swt.JFX_SWT_Wrapper;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.csstudio.ui.util.perspective.OpenPerspectiveAction;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;

/** RCP 'Editor' for the display builder
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayEditorPart extends EditorPart
{
    /** Editor ID registered in plugin.xml */
    public static final String ID = "org.csstudio.display.builder.editor.rcp.editor";

    private final JFXRepresentation toolkit = new JFXRepresentation(true);

    private DisplayEditor editor;

    /** Modification time marker of the resource currently loaded by the editor */
    private volatile long modification_marker;

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


    /** Open editor on a file
     *
     *  @param file Workspace file
     *  @return Editor part
     *  @throws Exception on error
     */
    public static DisplayEditorPart openDisplayFile(final IFile file) throws Exception
    {
        final IEditorInput input = new FileEditorInput(file);
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        return (DisplayEditorPart) page.openEditor(input, DisplayEditorPart.ID);
    }

    public DisplayEditorPart()
    {
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

        final JFX_SWT_Wrapper wrapper = new JFX_SWT_Wrapper(parent, () ->
        {
            editor = new DisplayEditor(toolkit, Preferences.getUndoSize());
            final Parent root = editor.create();

            editor.getToolBar().getItems().add(new ExecuteDisplayButton(this));

            return new Scene(root);
        });

        final Scene scene = wrapper.getScene();

        EditorUtil.setSceneStyle(scene);
        JFXCursorFix.apply(scene, parent.getDisplay());

        final AutocompleteMenu ac_menu = editor.getSelectedWidgetUITracker().getAutocompleteMenu();
        ac_menu.setUpdater(new AutoCompleteUpdater(ac_menu));


        editor.getUndoableActionManager().addListener(undo_redo_listener);

        final Control fx_canvas = wrapper.getFXCanvas();
        fx_canvas.setMenu(createContextMenu(fx_canvas));
        createRetargetableActionHandlers(fx_canvas);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.csstudio.display.builder.editor.rcp.display_builder");

        loadModel();
    }

    private Menu createContextMenu(final Control parent)
    {
        final MenuManager mm = new MenuManager();
        final Action execute = new ExecuteDisplayAction(this);
        final MenuManager morph = new MorphWidgetMenuSupport(editor).getMenuManager();
        final ImageDescriptor icon = AbstractUIPlugin.imageDescriptorFromPlugin(ModelPlugin.ID, "icons/display.png");
        final Action perspective = new OpenPerspectiveAction(icon, Messages.OpenEditorPerspective, EditorPerspective.ID);
        final Action reload = new ReloadDisplayAction(this);

        mm.setRemoveAllWhenShown(true);
        mm.addMenuListener(manager ->
        {

            final DisplayModel model = editor.getModel();
            final List<Widget> selection = editor.getWidgetSelectionHandler().getSelection();
            final int selectionSize = selection.size();

            manager.add(new UndoAction(editor));
            manager.add(new RedoAction(editor));
            manager.add(new Separator());

            final CutDeleteAction cutAction = new CutDeleteAction(editor, true);
            final CopyAction copyAction = new CopyAction(editor);
            final PasteAction pasteAction = new PasteAction(parent, editor);
            final String xml = Clipboard.getSystemClipboard().getString();

            cutAction.setEnabled(selectionSize >= 1);
            copyAction.setEnabled(selectionSize >= 1);
            pasteAction.setEnabled(xml != null && xml.startsWith("<?xml")  && xml.contains("<display"));

            manager.add(cutAction);
            manager.add(copyAction);
            manager.add(pasteAction);
            manager.add(new Separator());

            final CreateGroupAction createGroupAction = new CreateGroupAction(editor);
            final RemoveGroupAction removeGroupAction = new RemoveGroupAction(editor);

            createGroupAction.setEnabled(selectionSize >= 1);
            removeGroupAction.setEnabled(selectionSize == 1  &&  selection.get(0) instanceof GroupWidget);

            manager.add(createGroupAction);
            manager.add(removeGroupAction);
            manager.add(new Separator());

            if ( selectionSize >= 1 ) {
                manager.add(morph);
            } else {
                manager.add(new Action(Messages.ReplaceWith) {{
                    setImageDescriptor(Plugin.getIcon("replace.png"));
                    setEnabled(false);
                }});
            }

            final SetDisplaySize setDisplaySize = new SetDisplaySize(editor);

            setDisplaySize.setEnabled(model != null  &&  !model.isClassModel() && selectionSize == 0);

            manager.add(setDisplaySize);
            manager.add(new Separator());

            final ReloadClassesAction reloadClassesAction = new ReloadClassesAction(this);

            reloadClassesAction.setEnabled(model != null  &&  !model.isClassModel());

            manager.add(execute);
            manager.add(reload);
            manager.add(reloadClassesAction);
            manager.add(new Separator());

            if ( selection.size() == 1 && selection.get(0) instanceof EmbeddedDisplayWidget ) {
                manager.add(new EditEmbeddedDisplayAction((EmbeddedDisplayWidget) selection.get(0)));
            } else {
                manager.add(new EditEmbeddedDisplayAction(null));
            }

            manager.add(perspective);

        });

        return mm.createContextMenu(parent);
    }

    /** (Re-)load model specified in editor input */
    public void loadModel()
    {
        final IEditorInput input = getEditorInput();
        final IFile file = input.getAdapter(IFile.class);
        if (file != null)
            // Load model in background thread, then set it
            CompletableFuture.supplyAsync(() -> doLoadModel(file), EditorUtil.getExecutor())
                             .thenAccept(this::setModel);
    }

    private DisplayModel doLoadModel(final IFile file)
    {
        try
        {
            final String ws_location = file.getFullPath().toOSString();
            final DisplayModel new_model = ModelLoader.loadModel(ws_location);
            modification_marker = file.getModificationStamp();
            return new_model;
        }
        catch(FileNotFoundException ex) {
        	final String message = "Cannot load display " + file + " (location: " + ex.getMessage() + ").\n" +
        			"Reason: file not found.\n" +
        			"Please close the tab/window, refresh the Navigator view (F5), locate the file and open it again.";
        	logger.log(Level.WARNING, message, ex);
        	final Shell shell = getSite().getShell();
        	shell.getDisplay().asyncExec(() ->
            ExceptionDetailsErrorDialog.openError(shell, "Cannot load display " + file, message, ex));
        	return null;
        }
        catch(AccessDeniedException ex) {
        	final String message = "Cannot load display " + file + " (location: " + ex.getMessage() + ").\n" +
        			"Reason: current user does not have read access to the file.\n" + 
        			"Please close the tab/window, locate the file, update its access rights and/or ownership, and open it again.";
        	final Shell shell = getSite().getShell();
        	shell.getDisplay().asyncExec(() ->
            ExceptionDetailsErrorDialog.openError(shell, "Cannot load display " + file, message, ex));
        	return null;
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

    /** Re-load widget classes and apply to model */
    public void loadWidgetClasses()
    {
        // Trigger re-load of classes
        org.csstudio.display.builder.rcp.Plugin.reloadConfigurationFiles();
        // On separate thread..
        ModelThreadPool.getExecutor().execute(() ->
        {
            // get widget classes and apply to model
            final DisplayModel model = editor.getModel();
            if (model != null)
                WidgetClassesService.getWidgetClasses().apply(model);
        });
    }

    private void setModel(final DisplayModel model)
    {
        final DisplayModel old_model = editor.getModel();
        if (old_model != null)
            old_model.propName().removePropertyListener(model_name_listener);
        if (model == null)
            return;
        // In UI thread..
        toolkit.execute(() ->
        {
            setPartName(model.getDisplayName());
            editor.setModel(model);
            if (outline_page != null)
                outline_page.setModel(model);
        });
        model.propName().addPropertyListener(model_name_listener);
    }

    private void createRetargetableActionHandlers(final Control fx_canvas)
    {
        actions.put(ActionFactory.UNDO.getId(), new UndoAction(editor));
        actions.put(ActionFactory.REDO.getId(), new RedoAction(editor));
        actions.put(ActionFactory.CUT.getId(), new CutDeleteAction(editor, true));
        actions.put(ActionFactory.COPY.getId(), new CopyAction(editor));
        actions.put(ActionFactory.PASTE.getId(), new PasteAction(fx_canvas, editor));
        actions.put(ActionFactory.DELETE.getId(), new CutDeleteAction(editor, false));
        actions.put(ActionFactory.SELECT_ALL.getId(), new SelectAllAction(editor));
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
    public IAction getRetargetActionHandler(final String id)
    {
        return actions.get(id);
    }

    @Override
    public void setFocus()
    {
        // NOP: fx_canvas.setFocus();
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
        if (file != null   &&
            (DisplayModel.FILE_EXTENSION.equals(file.getFileExtension()) ||
             WidgetClassSupport.FILE_EXTENSION.equals(file.getFileExtension())))
        {

            if (file.exists()  &&  file.isReadOnly())
            {
                if (MessageDialog.openQuestion(getSite().getShell(), "File is read only",
                                               "The file\n" + file.getFullPath() +
                                               "\nis read-only.\nSave under a different name?"))
                    doSaveAs();
                return;
            }
            saveModelToFile(monitor, file);
            return;
        }

        // No file name, or using legacy file extension -> prompt for name
        file = promptForFile(getSite().getShell(), getEditorInput());
        if (file == null)
        {
            monitor.setCanceled(true);
            monitor.done();
        }
        else
            saveModelToFile(monitor, file);
    }

    @Override
    public void doSaveAs()
    {
        final IFile file = promptForFile(getSite().getShell(), getEditorInput());
        if (file != null)
            saveModelToFile(new NullProgressMonitor(), file);
    }

    /** Save model to file, using background thread
     *  @param save_monitor On success, <code>done</code> will be called <u>on UI thread</u>. Otherwise cancelled.
     *  @param file File to save
     */
    private void saveModelToFile(final IProgressMonitor save_monitor, final IFile file)
    {
        // Save on background thread
        final Job job = new Job("Save")
        {
            @Override
            protected IStatus run(final IProgressMonitor monitor)
            {
                final SubMonitor progress = SubMonitor.convert(monitor, 100);

                // Refresh file to detect modification outside of workspace
                try
                {
                    file.refreshLocal(IResource.DEPTH_ONE, progress);
                }
                catch (CoreException ex)
                {
                    logger.log(Level.WARNING, "Cannot refresh " + file, ex);
                }

                // Check if the file has changed
                final long mod = file.getModificationStamp();
                if (mod != IResource.NULL_STAMP  &&
                    modification_marker != IResource.NULL_STAMP  &&
                    mod != modification_marker)
                {
                    // Prompt on UI thread
                    final Future<Boolean> prompt = toolkit.submit(() ->
                    {
                        return MessageDialog.openConfirm(getSite().getShell(),
                                "File has changed",
                                "The file\n   " + file.getFullPath().toString() + "\n" +
                                "has been changed while you were editing it.\n\n" +
                                "'OK' to save and thus overwrite what somebody else has written,\n" +
                                "or\n" +
                                "'Cancel' and then re-load the file or save it under a different name.");
                    });
                    // Wait for the UI task to complete
                    try
                    {
                        // On cancel, return without calling save_monitor.done()
                        if (! prompt.get())
                            return Status.OK_STATUS;
                    }
                    catch (Exception ex)
                    {
                        logger.log(Level.WARNING, "Cannot prompt about changed file", ex);
                        return Status.OK_STATUS;
                    }
                }

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
                    modification_marker = file.getModificationStamp();
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
                    setPartName(model.getDisplayName());
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

                // If this was a class file, load it so from now on
                // displays will use it.
                if (editor.getModel().isClassModel())
                    org.csstudio.display.builder.rcp.Plugin.reloadConfigurationFiles();

                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    /** Prompt for file name used to 'save'
     *  @param shell Shell
     *  @param orig_input Original input
     *  @return File in workspace or <code>null</code>
     */
    public static IFile promptForFile(final Shell shell, final IEditorInput orig_input)
    {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        final SaveAsDialog dlg = new SaveAsDialog(shell);
        dlg.setBlockOnOpen(true);
        if (orig_input instanceof FileEditorInput)
        {
            IPath orig_path = ((FileEditorInput)orig_input).getFile().getFullPath();
            // Propose new file extension
            if (! DisplayModel.FILE_EXTENSION.equals(orig_path.getFileExtension()))
                orig_path = orig_path.removeFileExtension().addFileExtension(DisplayModel.FILE_EXTENSION);
            dlg.setOriginalFile(root.getFile(orig_path));
        }
        if (dlg.open() != Window.OK)
            return null;

        // Path to the new resource relative to the workspace
        IPath path = dlg.getResult();
        if (path == null)
            return null;
        // Assert correct file extension.
        // If not display or class file, make it a display file.
        final String ext = path.getFileExtension();
        if (! (DisplayModel.FILE_EXTENSION.equals(ext) ||
               WidgetClassSupport.FILE_EXTENSION.equals(ext)))
            path = path.removeFileExtension().addFileExtension(DisplayModel.FILE_EXTENSION);
        return root.getFile(path);
    }

    /** @return {@link DisplayInfo} for file in editor */
    public DisplayInfo getDisplayInfo()
    {
        final IFile file = getInputFile();
        // Providing workspace location, which is handled in ModelResourceUtil
        return new DisplayInfo(file.getFullPath().toOSString(), editor.getModel().getDisplayName(), new Macros());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object getAdapter(final Class adapter)
    {
        if (adapter == IContentOutlinePage.class)
        {
            outline_page = new OutlinePage(editor);
            outline_page.setModel(editor.getModel());
            return outline_page;
        }
        else if (adapter == IPropertySheetPage.class)
            return new PropertyPage(this);
        return super.getAdapter(adapter);
    }

    @Override
    public void dispose()
    {
        editor.getUndoableActionManager().removeListener(undo_redo_listener);
        editor.dispose();
        toolkit.shutdown();
        super.dispose();
    }
}
