/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFile;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayTransparent;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newDoublePropertyDescriptor;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;

/** Widget that displays an image loaded from a file
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class PictureWidget extends Widget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("picture", WidgetCategory.GRAPHIC,
            "Picture",
            "platform:/plugin/org.csstudio.display.builder.model/icons/picture.gif",
            "Display a picture from a file",
            Arrays.asList("org.csstudio.opibuilder.widgets.Image"))
    {
        @Override
        public Widget createWidget()
        {
            return new PictureWidget();
        }
    };

    /** Position 'rotation': What is the rotation of the picture */
    public static final WidgetPropertyDescriptor<Double> positionRotation =
        newDoublePropertyDescriptor(WidgetPropertyCategory.POSITION, "rotation", Messages.WidgetProperties_Rotation);


    private volatile WidgetProperty<String> filename;
    private volatile WidgetProperty<Boolean> transparent;
    private volatile WidgetProperty<Double> rotation;

    public PictureWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(filename = displayFile.createProperty(this, Messages.WidgetProperties_File));
        properties.add(transparent = displayTransparent.createProperty(this, false));
        properties.add(rotation = positionRotation.createProperty(this, 0.0));
    }

    /** @return Position 'rotation' */
    public WidgetProperty<Double> positionRotation()
    {
        return rotation;
    }

    /** @return Display 'text' */
    public WidgetProperty<String> displayFile()
    {
        return filename;
    }

    /** @return Display 'transparent' */
    public WidgetProperty<Boolean> displayTransparent()
    {
        return transparent;
    }

}
