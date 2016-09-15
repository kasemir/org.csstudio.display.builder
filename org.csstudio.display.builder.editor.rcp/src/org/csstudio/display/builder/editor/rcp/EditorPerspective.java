/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.rcp;

import org.csstudio.display.builder.rcp.RuntimePerspective;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** Perspective for display editor
*
*  @author Xihui Chen - Original author of OPIEditorPerspective
*  @author Kay Kasemir
*/
@SuppressWarnings("nls")
public class EditorPerspective implements IPerspectiveFactory
{
    /** Perspective ID registered in plugin.xml */
    public static final String ID = "org.csstudio.display.builder.editor.rcp.perspective";

    private static final String ID_HELP_VIEW = "org.eclipse.help.ui.HelpView";

    @Override
    public void createInitialLayout(final IPageLayout layout)
    {
        final String editor = layout.getEditorArea();

        // Folders that surround the central area
        final IFolderLayout left = layout.createFolder("LEFT", IPageLayout.LEFT, 0.15f, editor);
        final IFolderLayout right = layout.createFolder("RIGHT", IPageLayout.RIGHT, 0.7f, editor);
        final IFolderLayout bottom = layout.createFolder("BOTTOM", IPageLayout.BOTTOM, 0.8f, editor);
        final IFolderLayout left_bottom = layout.createFolder("LEFT_BOTTOM", IPageLayout.BOTTOM, 0.8f, "LEFT");

        // Stuff for 'left'
        left.addView(RuntimePerspective.ID_NAVIGATOR);
        left_bottom.addView(IPageLayout.ID_OUTLINE);

        // Stuff for 'right'
        right.addView(IPageLayout.ID_PROP_SHEET);

        //Stuff for 'bottom'
        bottom.addPlaceholder(RuntimePerspective.ID_CONSOLE_VIEW);
        bottom.addPlaceholder(IPageLayout.ID_PROGRESS_VIEW);

        // Populate menu entries for "New", "Window/Views..." etc.
        layout.addNewWizardShortcut(RuntimePerspective.NEW_FOLDER_WIZARD_ID);
        layout.addNewWizardShortcut(NewDisplayWizard.ID);

        layout.addPerspectiveShortcut(RuntimePerspective.ID);

        layout.addShowViewShortcut(RuntimePerspective.ID_NAVIGATOR);
        layout.addShowViewShortcut(RuntimePerspective.ID_CONSOLE_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(ID_HELP_VIEW);
    }
}
