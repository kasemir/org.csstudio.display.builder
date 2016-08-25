/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.diirt.vtype.VType;

/** Widget that writes to PV from selection of items
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class RadioWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
            new WidgetDescriptor("radio", WidgetCategory.CONTROL,
                    "Radio Button",
                    "platform:/plugin/org.csstudio.display.builder.model/icons/radiobutton.gif",
                    "Selects one of multiple items using radio buttons",
                    Arrays.asList("org.csstudio.opibuilder.widgets.radioBox",
                                  "org.csstudio.opibuilder.widgets.choiceButton"))
    {
        @Override
        public Widget createWidget()
        {
            return new RadioWidget();
        }
    };

    /** Behavior 'item': element for list of 'items' property */
    private static final WidgetPropertyDescriptor<String> behaviorItem =
            CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "item", Messages.ComboWidget_Item);

    /** Behavior 'items': list of items (string properties) for combo box */
    public static final WidgetPropertyDescriptor< List<WidgetProperty<String>> > behaviorItems =
            new ArrayWidgetProperty.Descriptor< WidgetProperty<String> >(WidgetPropertyCategory.BEHAVIOR, "items", Messages.ComboWidget_Items,
                                                                         (widget, index) -> behaviorItem.createProperty(widget, "Item " + index));

    /** Behavior 'items_from_pv': If PV is enum PV, get items from PV? */
    public static final WidgetPropertyDescriptor<Boolean> behaviorItemsFromPV =
        CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "items_from_pv", Messages.ComboWidget_ItemsFromPV);

    /** Display 'horizontal': Change whether orientation is horizontal or, if false, vertical */
    public static final WidgetPropertyDescriptor<Boolean> displayHorizontal = newBooleanPropertyDescriptor(
            WidgetPropertyCategory.DISPLAY, "horizontal", Messages.Horizontal);

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<WidgetColor> foreground;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<List<WidgetProperty<String>>> items;
    private volatile WidgetProperty<Boolean> items_from_pv;
    private volatile WidgetProperty<Boolean> horizontal;

    public RadioWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 100, 30);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(displayBorderAlarmSensitive.createProperty(this, true));
        properties.add(foreground = displayForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(value = runtimeValue.createProperty(this, null));
        properties.add(
                items = behaviorItems.createProperty(this, Arrays.asList(behaviorItem.createProperty(this, "Item"))));
        properties.add(items_from_pv = behaviorItemsFromPV.createProperty(this, true));
        properties.add(horizontal = displayHorizontal.createProperty(this, true));
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Display 'foreground_color' */
    public WidgetProperty<WidgetColor> displayForegroundColor()
    {
        return foreground;
    }

    /** @return Display 'font' */
    public WidgetProperty<WidgetFont> displayFont()
    {
        return font;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }

    /** @return Behavior 'items_from_PV' */
    public WidgetProperty<Boolean> behaviorItemsFromPV()
    {
        return items_from_pv;
    }

    /** @return Behavior 'items' */
    public WidgetProperty< List<WidgetProperty<String>> > behaviorItems()
    {
        return items;
    }

    /** @return Display 'horizontal' */
    public WidgetProperty<Boolean> displayHorizontal()
    {
        return horizontal;
    }

}
