/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.io.File;
import java.util.function.Function;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.properties.FilenameWidgetProperty;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;

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

    /** Default file prompt that uses local file system */
    private static final Function<FilenameWidgetProperty, String> local_file_prompt = file_prop ->
    {
        final Widget widget = file_prop.getWidget();
        final FileChooser dialog = new FileChooser();
        try
        {
            final DisplayModel model = widget.getDisplayModel();
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
        final Window window = JFXBaseRepresentation.getJFXNode(widget).getScene().getWindow();
        final File selected = dialog.showOpenDialog(window);
        if (selected == null)
            return null;
        return selected.getPath();
    };

    private static Function<FilenameWidgetProperty, String> file_prompt = local_file_prompt;

    /** Install function that will be called to prompt for files
     *
     *  Default uses local file system.
     *  RCP plugin can install a workspace-based file dialog.
     *
     *  @param prompt Function that receives original file name property,
     *                prompts user for new file (via file dialog),
     *                and returns selected new file or <code>null</code>
     */
    public static void setFilePrompt(Function<FilenameWidgetProperty, String> prompt)
    {
        file_prompt = prompt;
    }

    /** Prompt for file name
     *  @param file_prop {@link FilenameWidgetProperty} that provides initial value
     *  @return Selected file name or <code>null</code>
     */
    public static String promptForFilename(final FilenameWidgetProperty file_prop)
    {
        return file_prompt.apply(file_prop);
    }
}
