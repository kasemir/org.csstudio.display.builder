/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.widgets.PictureWidget;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** Creates JavaFX item for model widget
 *  @author Megan Grodowitz
 */
public class PictureRepresentation extends JFXBaseRepresentation<ImageView, PictureWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private Pos pos;
    private Image img;
    private double rotation;

    @Override
    public ImageView createJFXNode() throws Exception
    {
        return new ImageView();
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayTransparent().addUntypedPropertyListener(this::contentChanged);
        model_widget.displayFile().addUntypedPropertyListener(this::contentChanged);
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_style.checkAndClear())
        {
            jfx_node.setFitHeight(model_widget.positionHeight().getValue());
            jfx_node.setFitWidth(model_widget.positionWidth().getValue());
            jfx_node.setRotate(rotation);
        }
        if (dirty_content.checkAndClear())
        {
            String base_path = model_widget.displayFile().getValue();
            //final String base_path = "platform:/plugin/org.csstudio.display.builder.model/icons/picture.gif";  //$NON-NLS-1$
            //final String base_path = "platform:/plugin/org.csstudio.display.representation.javafx/icons/add.png"; //$NON-NLS-1$

            try
            {
                // expand macros in the file name
                final String expanded_path = MacroHandler.replace(model_widget.getEffectiveMacros(), base_path);

                // Resolve new image file relative to the source widget model (not 'top'!)
                // Get the display model from the widget tied to this representation
                final DisplayModel widget_model = model_widget.getDisplayModel();
                // Get the parent file path from the display model
                final String parent_file = widget_model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
                // Resolve the image path using the parent model file path
                final String resolved_name = ModelResourceUtil.resolveResource(parent_file, expanded_path);

                // Open the image from the stream created from the resource file
                img = new Image(ModelResourceUtil.openResourceStream(resolved_name));
                //img = new Image(ResourceUtil.openPlatformResource(expanded_path));
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (model_widget.displayTransparent().getValue())
            {   // Image invisible
                jfx_node.setImage(null);
            }
            else
            {   // Image visible
                jfx_node.setImage(img);
            }
            jfx_node.setPreserveRatio(false);
            jfx_node.setCache(true);
        }
    }
}
