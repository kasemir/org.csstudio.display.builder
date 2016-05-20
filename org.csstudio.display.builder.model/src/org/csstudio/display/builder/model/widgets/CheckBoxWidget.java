/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorBit;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.diirt.vtype.VType;

@SuppressWarnings("nls")
/** Widget that can read/write a bit in a PV
 *  @author Amanda Carpenter
 */
public class CheckBoxWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("checkbox", WidgetCategory.CONTROL,
            "Check Box",
            "platform:/plugin/org.csstudio.display.builder.model/icons/checkbox.gif",
            "Read/write a bit in a PV",
            Arrays.asList("org.csstudio.opibuilder.widgets.checkbox") )
    {
        @Override
        public Widget createWidget()
        {
            return new CheckBoxWidget();
        }
    };
    /** Display 'label': Text for label */
    public static final WidgetPropertyDescriptor<String> displayLabel =
        newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "label", Messages.Checkbox_Label);

    /** Display 'auto_size': Automatically adjust size of widget */
    public static final WidgetPropertyDescriptor<Boolean> displayAutoSize =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "auto_size", Messages.AutoSize);

    private volatile WidgetProperty<Integer> bit;
    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<String> label;
    private volatile WidgetProperty<Boolean> auto_size;

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(bit = behaviorBit.createProperty(this, 0));
        properties.add(value = runtimeValue.createProperty(this, null));
        properties.add(label = displayLabel.createProperty(this, Messages.Checkbox_Label));
        properties.add(auto_size = displayAutoSize.createProperty(this, false));
    }

    public CheckBoxWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    /** @return Behavior 'bit' */
    public WidgetProperty<Integer> behaviorBit()
    {
        return bit;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }

    /** @return Display 'label' */
    public WidgetProperty<String> displayLabel()
    {
        return label;
    }

    /** @return Display 'auto_size' */
    public WidgetProperty<Boolean> displayAutoSize()
    {
        return auto_size;
    }

}
