/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.StructuredWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.NamedWidgetColor;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that displays an LED which reflects the enumerated state of a PV
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MultiStateLEDWidget extends BaseLEDWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("multi_state_led", WidgetCategory.MONITOR,
            "LED (Multi State)",
            "platform:/plugin/org.csstudio.display.builder.model/icons/led.png",
            "LED that represents multiple states",
            Arrays.asList("org.csstudio.opibuilder.widgets.LED"))
    {
        @Override
        public Widget createWidget()
        {
            return new MultiStateLEDWidget();
        }
    };

    /** Custom configurator to read legacy *.opi files */
    private static class LEDConfigurator extends WidgetConfigurator
    {
        public LEDConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final Widget widget, final Element xml)
                throws Exception
        {
            // Legacy XML with off_color, on_color identifies plain boolean LED
            if (XMLUtil.getChildElement(xml, "off_color") != null ||
                XMLUtil.getChildElement(xml, "on_color") != null)
                return false;

            super.configureFromXML(widget, xml);

            // TODO Handle legacy states and colors

            BaseLEDWidget.handle_legacy_position(widget, xml_version, xml);
            return true;
        }
    }

    // Elements of the 'state' structure
    private static final WidgetPropertyDescriptor<Integer> state_value =
        CommonWidgetProperties.newIntegerPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "value", "Value");

    private static final WidgetPropertyDescriptor<WidgetColor> state_color =
        CommonWidgetProperties.newColorPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "color", "Color");

    private static final WidgetPropertyDescriptor<WidgetColor> fallback_color =
            CommonWidgetProperties.newColorPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "fallback", "Fallback Color");

    // 'state' structure that describes one state
    private static final StructuredWidgetProperty.Descriptor behaviorState =
        new StructuredWidgetProperty.Descriptor(WidgetPropertyCategory.BEHAVIOR, "state", "State");

    /** Property that describes one state of the LED */
    public static class StateWidgetProperty extends StructuredWidgetProperty
    {
        public StateWidgetProperty(final Widget widget, final int state)
        {
            super(behaviorState, widget,
                  Arrays.asList(state_value.createProperty(widget, state),
                                state_color.createProperty(widget, getDefaultColor(state))));
        }
        public WidgetProperty<Integer> state()      { return getElement(0); }
        public WidgetProperty<WidgetColor> color()  { return getElement(1); }
    };

    // Helper for obtaining initial color for each state
    private static WidgetColor getDefaultColor(final int state)
    {   // TODO Handle these named colors which aren't defined
        if (state == 0)
            return new NamedWidgetColor("Off", 60, 100, 60);
        if (state == 1)
            return new NamedWidgetColor("On", 0, 255, 0);
        if (state == 2)
            return new NamedWidgetColor("Error", 255, 0, 0);
        // Shade of blue for remaining states
        return new NamedWidgetColor("State " + state, 0, 0, Math.max(0, 255 - 10*state));
    }

    // 'states' array
    private static final ArrayWidgetProperty.Descriptor<StateWidgetProperty> behaviorStates =
            new ArrayWidgetProperty.Descriptor<>(WidgetPropertyCategory.BEHAVIOR, "states", "States",
                    (widget, state) -> new StateWidgetProperty(widget, state));

    private volatile ArrayWidgetProperty<StateWidgetProperty> states;
    private volatile WidgetProperty<WidgetColor> fallback;

    public MultiStateLEDWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(states = behaviorStates.createProperty(this,
                                                              Arrays.asList(new StateWidgetProperty(this, 0),
                                                                            new StateWidgetProperty(this, 1))));
        properties.add(fallback = fallback_color.createProperty(this,
                                                                WidgetColorService.getColor(NamedWidgetColors.ALARM_INVALID)));
    }

    /** @return 'states' */
    public ArrayWidgetProperty<StateWidgetProperty> states()
    {
        return states;
    }

    /** @return 'fallback' */
    public WidgetProperty<WidgetColor> fallback()
    {
        return fallback;
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version)
            throws Exception
    {
        return new LEDConfigurator(persisted_version);
    }
}
