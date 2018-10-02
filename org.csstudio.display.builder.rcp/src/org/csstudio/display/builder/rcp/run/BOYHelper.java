/*******************************************************************************
 * Copyright (c) 2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.logging.Level;

import org.csstudio.openfile.DisplayUtil;
import org.eclipse.swt.widgets.Display;

/** Helper for opening BOY displays
 *  @author Kay Kasemir
 */
public class BOYHelper
{
    /** Open display in BOY
     *  @param display_path Path within workspace
     *  @param macros "macro=value,macro=value"
     */
    public static void openLegacyRuntime(final String display_path, final String macros)
    {
        Display.getDefault().asyncExec(() -> doOpenLegacyDisplay(display_path, macros));
    }

    private static void doOpenLegacyDisplay(final String display_path, final String macros)
    {
        try
        {
            // Depends on BOY installing a IOpenDisplayAction for *.opi
            // Display Builder also installs one, but not as primary
            // -> This will call BOY, if installed, for *.opi,
            //    but fall back to display builder
            DisplayUtil.getInstance().openDisplay(display_path, macros);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot open BOY runtime for " + display_path, ex);
        }
    }

    // Could also open the OPIView by ID,
    // which is more direct than depending on the DisplayUtil.
    // But then no way to pass macros unless adding dependency
    // to the opibuilder and its RunnerInput
//    private final static String OPIViewID = "org.csstudio.opibuilder.opiView";
//    private static void doOpenLegacyRuntime(final String display_path)
//    {
//        try
//        {
//            final String secondary = UUID.randomUUID().toString();
//
//            final IWorkbench workbench = PlatformUI.getWorkbench();
//            final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
//            final IWorkbenchPage page = window.getActivePage();
//            final IViewPart view = page.showView(OPIViewID, secondary, IWorkbenchPage.VIEW_ACTIVATE);
//
//            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//            final IFile file = root.getFile(new Path(display_path));
//            final IEditorInput input = new FileEditorInput(file);
//
//            // view.setOPIInput(input);
//            final Method method = view.getClass().getMethod("setOPIInput", IEditorInput.class);
//            method.invoke(view, input);
//        }
//        catch (Exception ex)
//        {
//            logger.log(Level.WARNING, "Cannot open BOY runtime for " + display_path, ex);
//        }
//    }
}
