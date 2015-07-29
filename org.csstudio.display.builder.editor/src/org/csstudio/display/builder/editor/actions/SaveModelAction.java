/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import java.io.File;
import java.util.function.Consumer;

import org.csstudio.display.builder.editor.Messages;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/** Prompt for file name to save model
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SaveModelAction extends ActionDescription
{
    private final Window window;
    private final Consumer<File> save_handler;
    private String last_directory = null;
    private String last_file = null;

    /** @param save_handler Will be invoked with file name */
    public SaveModelAction(final Consumer<File> save_handler)
    {
        super("platform:/plugin/org.csstudio.display.builder.editor/icons/save.png",
              Messages.SaveDisplay_TT);
        this.window = null;
        this.save_handler = save_handler;
    }

    @Override
    public void run(final boolean selected)
    {
        final FileChooser dialog = new FileChooser();
        dialog.setTitle(Messages.SaveDisplay);
        if (last_directory != null)
            dialog.setInitialDirectory(new File(last_directory));
        if (last_file != null)
            dialog.setInitialFileName(last_file);
        dialog.getExtensionFilters().addAll(new ExtensionFilter(Messages.FileTypeDisplays, "*.opi"),
                                            new ExtensionFilter(Messages.FileTypeAll, "*.*"));
        File file = dialog.showSaveDialog(window);
        if (file == null)
            return;

        // If file has no extension, use *.opi.
        // Check only the filename, not the complete path for '.'!
        int sep = file.getName().lastIndexOf('.');
        if (sep < 0)
            file = new File(file.getPath() + ".opi");

        // Remember last dir. and name
        final File parent = file.getParentFile();
        if (parent != null)
            last_directory = parent.getAbsolutePath();
        last_file = file.getName();

        save_handler.accept(file);
    }
}
