/*******************************************************************************
 * Copyright (c) 2010-2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.ui;

import java.time.Instant;
import java.util.function.Consumer;

import org.csstudio.csdata.ProcessVariable;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.csstudio.trends.databrowser3.Messages;
import org.csstudio.trends.databrowser3.archive.ArchiveFetchJob;
import org.csstudio.trends.databrowser3.imports.FileImportDialog;
import org.csstudio.trends.databrowser3.imports.ImportArchiveReaderFactory;
import org.csstudio.trends.databrowser3.model.ArchiveDataSource;
import org.csstudio.trends.databrowser3.model.AxisConfig;
import org.csstudio.trends.databrowser3.model.Model;
import org.csstudio.trends.databrowser3.model.ModelItem;
import org.csstudio.trends.databrowser3.model.PVItem;
import org.csstudio.trends.databrowser3.preferences.Preferences;
import org.csstudio.trends.databrowser3.propsheet.AddArchiveCommand;
import org.csstudio.trends.databrowser3.propsheet.AddAxisCommand;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/** Controller that interfaces the {@link Model} with the {@link ModelBasedPlotSWT}:
 *  <ul>
 *  <li>For each item in the Model, create a trace in the plot.
 *  <li>Perform scrolling of the time axis.
 *  <li>When the plot is interactively zoomed, update the Model's time range.
 *  <li>Get archived data whenever the time axis changes.
 *  </ul>
 *  @author Kay Kasemir
 */
public class ControllerSWT extends ControllerBase
{
    /** Optional shell used to track shell state */
    final private Shell shell;

    /** Display used for dialog boxes etc. */
    final private Display display;

    final private ShellListener shell_listener = new ShellAdapter()
    {
        //Remove Override annotation, because this method does not exist in RAP
        //@Override
        public void shellIconified(ShellEvent e)
        {
            window_is_iconized = true;
        }

        //Remove Override annotation, because this method does not exist in RAP
        //@Override
        public void shellDeiconified(ShellEvent e)
        {
            window_is_iconized = false;
        }
    };

    class SWTArchiveFetchJobListener extends BaseArchiveFetchJobListener
    {
        @Override
        protected void displayError(final String message, final Exception error)
        {
            executeOnUIThread(e -> ExceptionDetailsErrorDialog.openError(shell, Messages.Information, message, error));
        }

        @Override
        protected void executeOnUIThread(Consumer<Void> consumer)
        {
            if (!display.isDisposed())
                display.asyncExec(() ->
                {
                    if (!display.isDisposed())
                        consumer.accept(null);
                });
        }
    };
    final private SWTArchiveFetchJobListener archive_fetch_listener;

    class SWTPlotListener extends BasePlotListener
    {
        @Override
        protected void executeOnUIThread(Runnable func)
        {
            if (!display.isDisposed())
                display.asyncExec(func);
        }

        @Override
        public void timeConfigRequested()
        {
            StartEndTimeAction.run(shell, model, plot.getPlot().getUndoableActionManager());
        }

        @Override
        public void droppedNames(final String[] names)
        {
            // Offer potential PV name in dialog so user can edit/cancel
            final AddPVAction add = new AddPVAction(plot.getPlot().getUndoableActionManager(), shell, model, false);
            for (String one_name : names)
                if (! add.runWithSuggestedName(one_name, null))
                    break;
        }

        @Override
        public void droppedPVNames(final ProcessVariable[] names, final ArchiveDataSource[] archives)
        {
            if (names == null)
            {
                if (archives == null)
                    return;
                // Received only archives. Add to all PVs
                for (ArchiveDataSource archive : archives)
                    for (ModelItem item : model.getItems())
                    {
                        if (! (item instanceof PVItem))
                            continue;
                        final PVItem pv = (PVItem) item;
                        if (pv.hasArchiveDataSource(archive))
                            continue;
                        new AddArchiveCommand(plot.getPlot().getUndoableActionManager(), pv, archive);
                    }
            }
            else
            {   // Received PV names, maybe with archive
                final UndoableActionManager operations_manager = plot.getPlot().getUndoableActionManager();

                // When multiple PVs are dropped, assert that there is at least one axis.
                // Otherwise dialog cannot offer adding all PVs onto the same axis.
                if (names.length > 1  &&  model.getAxisCount() <= 0)
                    new AddAxisCommand(operations_manager, model);

                final AddPVDialog dlg = new AddPVDialog(shell, names.length, model, false);
                for (int i=0; i<names.length; ++i)
                    dlg.setName(i, names[i].getName());
                if (dlg.open() != Window.OK)
                    return;

                for (int i=0; i<names.length; ++i)
                {
                    final AxisConfig axis;
                    if (dlg.getAxisIndex(i) >= 0)
                        axis = model.getAxis(dlg.getAxisIndex(i));
                    else // Use first empty axis, or create a new one
                        axis = model.getEmptyAxis().orElseGet(() -> new AddAxisCommand(operations_manager, model).getAxis());

                    // Add new PV
                    final ArchiveDataSource archive =
                            (archives == null || i>=archives.length) ? null : archives[i];
                    AddModelItemCommand.forPV(shell, operations_manager,
                            model, dlg.getName(i), dlg.getScanPeriod(i),
                            axis, archive);
                }
                return;
            }
        }

        @Override
        public void droppedFilename(String file_name)
        {
            final FileImportDialog dlg = new FileImportDialog(shell, file_name);
            if (dlg.open() != Window.OK)
                return;

            final UndoableActionManager operations_manager = plot.getPlot().getUndoableActionManager();

            // Add to first empty axis, or create new axis
            final AxisConfig axis = model.getEmptyAxis().orElseGet(
                    () -> new AddAxisCommand(operations_manager, model).getAxis() );

            // Add archivedatasource for "import:..." and let that load the file
            final String type = dlg.getType();
            file_name = dlg.getFileName();
            final String url = ImportArchiveReaderFactory.createURL(type, file_name);
            final ArchiveDataSource imported = new ArchiveDataSource(url, 1, type);
            // Add PV Item with data to model
            AddModelItemCommand.forPV(shell, operations_manager,
                    model, dlg.getItemName(), Preferences.getScanPeriod(),
                    axis, imported);
        }

    };

    /** Initialize
     *  @param shell Shell
     *  @param model Model that has the data
     *  @param plot Plot for displaying the Model
     *  @throws Error when called from non-UI thread
     */
    public ControllerSWT(final Shell shell, final Model model, final ModelBasedPlot plot)
    {
        super (model, plot);
        this.shell = shell;
        archive_fetch_listener = new SWTArchiveFetchJobListener();

        if (shell == null)
        {
            display = Display.getCurrent();
            if (display == null)
                throw new Error("Must be called from UI thread"); //$NON-NLS-1$
        }
        else
        {
            display = shell.getDisplay();
            // Update 'iconized' state from shell
            shell.addShellListener(shell_listener);
            window_is_iconized = shell.getMinimized();
        }

        // Listen to user input from Plot UI, update model
        plot.addListener(new SWTPlotListener());
    }

    @Override
    protected ArchiveFetchJob makeArchiveFetchJob(PVItem pv_item, Instant start, Instant end)
    {
        return new ArchiveFetchJob(pv_item, start, end, archive_fetch_listener);
    }

    @Override
    public void stop()
    {
        if (shell != null)
            shell.removeShellListener(shell_listener);
        super.stop();
    }
}
