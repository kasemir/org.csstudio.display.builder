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

import org.csstudio.trends.databrowser3.archive.ArchiveFetchJob;
import org.csstudio.trends.databrowser3.model.Model;
import org.csstudio.trends.databrowser3.model.PVItem;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
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
@SuppressWarnings("nls")
public class Controller extends ControllerBase
{
    /** Optional shell used to track shell state */
    final private Shell shell;

    /** Display used for dialog boxes etc. */
    final private Display display;


    class SWTArchiveFetchJobListener extends BaseArchiveFetchJobListener
    {
        @Override
        protected Shell getShell() { return shell; }

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
        protected Shell getShell() { return shell; }

        @Override
        protected void executeOnUIThread(Runnable func)
        {
            if (!display.isDisposed())
                display.asyncExec(func);
        }
    };

    @Override
    protected ArchiveFetchJob makeArchiveFetchJob(PVItem pv_item, Instant start, Instant end)
    {
        return new ArchiveFetchJob(pv_item, start, end, archive_fetch_listener);
    }

    /** Initialize
     *  @param shell Shell
     *  @param model Model that has the data
     *  @param plot Plot for displaying the Model
     *  @throws Error when called from non-UI thread
     */
    public Controller(final Shell shell, final Model model, final ModelBasedPlot plot)
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
            shell.addShellListener(new ShellAdapter()
            {
                //Remove Override annotation, because this method does not exist in RAP
                //@Override
                @Override
                public void shellIconified(ShellEvent e)
                {
                    window_is_iconized = true;
                }

                //Remove Override annotation, because this method does not exist in RAP
                //@Override
                @Override
                public void shellDeiconified(ShellEvent e)
                {
                    window_is_iconized = false;
                }
            });
            window_is_iconized = shell.getMinimized();
        }

        // Listen to user input from Plot UI, update model
        plot.addListener(new SWTPlotListener());
    }

}
