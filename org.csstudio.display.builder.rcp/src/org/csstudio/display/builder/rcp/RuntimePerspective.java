/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import org.csstudio.display.builder.rcp.run.PlaceHolderView;
import org.csstudio.display.builder.rcp.run.RuntimeViewPart;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** Perspective for display runtime
 *
 *  <p>Meant for executing display runtimes,
 *  hiding the 'editorss' area so that displays
 *  can open in the center of the window.
 *
 *  <p>Includes placeholders for legacy *.opi runtimes.
 *
 *  @author Xihui Chen - Original author of OPIRunnerPerspective
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RuntimePerspective implements IPerspectiveFactory
{
    /** Perspective ID registered in plugin.xml */
    public static final String ID = "org.csstudio.display.builder.rcp.runtimeperspective";

    public static final String NEW_FOLDER_WIZARD_ID = "org.eclipse.ui.wizards.new.folder";

    public static final String LEGACY_VIEW_ID = "org.csstudio.opibuilder.opiView";

    public static final String ID_CONSOLE_VIEW = "org.eclipse.ui.console.ConsoleView";

    /** ID of navigator view.
     *  This one is deprecated, but don't know what else to use.
     */
    @SuppressWarnings("deprecation")
    public static final String ID_NAVIGATOR = IPageLayout.ID_RES_NAV;

    @Override
    public void createInitialLayout(final IPageLayout layout)
    {
        final String editor = layout.getEditorArea();

        // To debug layout: Install "Eclipse 4 Tools: Application Model Editor"
        // The "E4 Model Spy" can then be started via Alt-Shift-F9.
        // For css, add org.eclipse.e4.tools.emf.liveeditor and dependencies

        // Folders that surround the central area
        final IFolderLayout left = layout.createFolder("LEFT", IPageLayout.LEFT, 0.25f, editor);
        final IFolderLayout right = layout.createFolder("RIGHT", IPageLayout.RIGHT, 0.75f, editor);
        final IFolderLayout top = layout.createFolder("TOP", IPageLayout.TOP, 0.25f, editor);
        final IFolderLayout bottom = layout.createFolder("BOTTOM", IPageLayout.BOTTOM, 0.75f, editor);

        // Placeholders for legacy views
        left.addPlaceholder(LEGACY_VIEW_ID + "LEFT:*");
        right.addPlaceholder(LEGACY_VIEW_ID + "RIGHT:*");
        top.addPlaceholder(LEGACY_VIEW_ID + "TOP:*");
        bottom.addPlaceholder(LEGACY_VIEW_ID + "BOTTOM:*");

        // Create ordinary view stack for 'DEFAULT_VIEW' close to editor area
        // Alternative hack using internal API:
        // Adds view stack in the editor area, so 'DEFAULT_VIEW' appears
        // similar to editor
        //
        // ModeledPageLayout real_layout = (ModeledPageLayout) layout;
        // real_layout.stackView(OPIView.ID + SECOND_ID, editor, false);
        //
        // .. but such OPIViews are then in the IPageLayout.ID_EDITOR_AREA="org.eclipse.ui.editorss"(!) part,
        // which is linked to Shared Elements/Area, ignored by the perspective,
        // since it's meant for "Editors".
        final IFolderLayout center = layout.createFolder("Default", IPageLayout.RIGHT, 0.5f, editor);
        // 'Center' is mapped to the plain View ID, so it's used by default
        center.addPlaceholder(RuntimeViewPart.ID + ":*");
        center.addPlaceholder(LEGACY_VIEW_ID + ":*");

        // Hide the "editor" part
        layout.setEditorAreaVisible(false);

        if (Preferences.showPlaceholders())
        {
            center.addView(PlaceHolderView.ID + ":CENTER");
            left.addView(PlaceHolderView.ID + ":LEFT");
            right.addView(PlaceHolderView.ID + ":RIGTH");
            top.addView(PlaceHolderView.ID + ":TOP");
            bottom.addView(PlaceHolderView.ID + ":BOTTOM");
        }

        bottom.addPlaceholder(ID_CONSOLE_VIEW);
        left.addPlaceholder(ID_NAVIGATOR);

        // Populate menu entries for "Window/Views..." etc.
        layout.addNewWizardShortcut(RuntimePerspective.NEW_FOLDER_WIZARD_ID);

        layout.addPerspectiveShortcut("org.csstudio.display.builder.editor.rcp.perspective");

        layout.addShowViewShortcut(ID_NAVIGATOR);
        layout.addShowViewShortcut(ID_CONSOLE_VIEW);
    }
}
