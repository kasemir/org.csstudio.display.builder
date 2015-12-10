/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import java.io.File;

import org.csstudio.display.builder.editor.EditorDemoGUI;
import org.csstudio.display.builder.editor.Messages;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/** Prompt for file name to save model
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class LoadModelAction extends ActionDescription
{
    private final Window window;
    private final EditorDemoGUI editor;

    /** @param save_handler Will be invoked with file name */
    public LoadModelAction(final EditorDemoGUI editor)
    {
        super("platform:/plugin/org.csstudio.display.builder.editor/icons/open.png",
              Messages.LoadDisplay_TT);
        this.window = null;
        this.editor = editor;
    }

    @Override
    public void run(final boolean selected)
    {
        final FileChooser dialog = new FileChooser();
        dialog.setTitle(Messages.LoadDisplay);

        File file = editor.getFile();
        if (file != null)
        {
            dialog.setInitialDirectory(file.getParentFile());
            dialog.setInitialFileName(file.getName());
        }
        dialog.getExtensionFilters().addAll(new ExtensionFilter(Messages.FileTypeDisplays, "*.opi"),
                                            new ExtensionFilter(Messages.FileTypeAll, "*.*"));
        file = dialog.showOpenDialog(window);
        if (file == null)
            return;

        // If file has no extension, use *.opi.
        // Check only the filename, not the complete path for '.'!
        int sep = file.getName().lastIndexOf('.');
        if (sep < 0)
            file = new File(file.getPath() + ".opi");

        editor.loadModel(file);
    }
}
