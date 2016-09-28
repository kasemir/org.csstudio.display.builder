/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.util;

import java.io.File;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.properties.FilenameWidgetProperty;
import org.csstudio.display.builder.model.util.ModelResourceUtil;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;;

/** Helper for handling {@link FilenameWidgetProperty}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FilenameSupport
{
    public static final ExtensionFilter[] file_extensions = new ExtensionFilter[]
    {
        new ExtensionFilter(Messages.FileTypeAll, "*.*"),
        new ExtensionFilter(Messages.FileTypeDisplays, "*.bob")
    };

    public static String promptForFilename(final Window window, final FilenameWidgetProperty file_prop)
    {
        // TODO By default, use dialog for local file system
        // TODO With RCP, allow installation of workspace resource dialog
        final FileChooser dialog = new FileChooser();
        try
        {
            final DisplayModel model = file_prop.getWidget().getDisplayModel();
            final File initial = new File(ModelResourceUtil.resolveResource(model, file_prop.getValue()));
            if (initial.exists())
            {
                dialog.setInitialDirectory(initial.getParentFile());
                dialog.setInitialFileName(initial.getName());
            }
        }
        catch (Exception ex)
        {
            // Can't set initial file name, ignore.
        }
        dialog.getExtensionFilters().addAll(file_extensions);
        final File selected = dialog.showOpenDialog(window);
        if (selected == null)
            return null;
        return selected.getPath();
    }
}
