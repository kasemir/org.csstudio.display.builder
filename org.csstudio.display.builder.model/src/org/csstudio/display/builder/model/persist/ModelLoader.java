/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.persist;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetClassSupport;
import org.csstudio.display.builder.model.util.ModelResourceUtil;

/** Helper for loading a display model
 *
 *  <p>Resolves display path relative to parent display,
 *  then loads the model
 *  and updates the model's input file.
 *
 *  @author Kay Kasemir
 */
public class ModelLoader
{
    /** Load model for runtime, i.e. apply class information
     *
     *  @param parent_display Path to a 'parent' file, may be <code>null</code>
     *  @param display_file Model file
     *  @return {@link DisplayModel}
     *  @throws Exception on error
     */
    public static DisplayModel loadModel(final String parent_display, final String display_file) throws Exception
    {
        final String resolved_name = ModelResourceUtil.resolveResource(parent_display, display_file);
        final ModelReader reader = new ModelReader(ModelResourceUtil.openResourceStream(resolved_name));
        final DisplayModel model = reader.readModel();
        model.setUserData(DisplayModel.USER_DATA_INPUT_FILE, resolved_name);

        // Models from version 2 on support classes
        if (reader.getVersion().getMajor() >= 2)
        {
            final WidgetClassSupport classes = WidgetClassesService.getWidgetClasses();
            if (classes != null)
                classes.apply(model);
        }
        return model;
    }
}
