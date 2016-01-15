/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
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
        img = new Image("icons/add.png"); //$NON-NLS-1$
        ImageView iv = new ImageView();
        iv.setImage(img);
        iv.setPreserveRatio(false);
        iv.setCache(true);
        return iv;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayTransparent().addUntypedPropertyListener(this::styleChanged);
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
            if (model_widget.displayTransparent().getValue())
            {   // No fill
                jfx_node.setImage(null);
            }
            else
            {   // Fill background
                jfx_node.setImage(img);
            }
            //jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));
        }
        //if (dirty_content.checkAndClear())
            //jfx_node.setText(model_widget.displayFile().getValue());
    }
}
