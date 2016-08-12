/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorLimitsFromPV;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMaximum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorMinimum;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFillColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.diirt.vtype.VType;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/** Widget that displays a progress bar
 *  @author Kay Kasemir
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class ProgressBarWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("progressbar", WidgetCategory.MONITOR,
            "Progress Bar",
            "platform:/plugin/org.csstudio.display.builder.model/icons/progressbar.png",
            "Bar graph widget that 'fills' relative to numeric value of a PV",
            Arrays.asList("org.csstudio.opibuilder.widgets.progressbar", "org.csstudio.opibuilder.widgets.tank"))
    {
        @Override
        public Widget createWidget()
        {
            return new ProgressBarWidget();
        }
    };

    //TODO: BOY thermometer where show bulb property false
    /** Widget configurator to read legacy *.opi files*/
    private static class ProgressBarConfigurator extends WidgetConfigurator
    {
        public ProgressBarConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget, final Element xml)
                throws Exception
        {
            //Legacy tank widget was always vertical; needs horizontal=false
            if (xml_version.getMajor() < 2 && XMLUtil.getChildElement(xml, displayHorizontal.getName()) == null)
            {
                final Document doc = xml.getOwnerDocument();
                final Element new_el = doc.createElement(displayHorizontal.getName());
                final Text falze = doc.createTextNode("false");
                new_el.appendChild(falze);
                xml.appendChild(new_el);
            }

            super.configureFromXML(model_reader, widget, xml);
            return true;
        }
    }

    @Override
    public WidgetConfigurator getConfigurator(final Version persisted_version)
            throws Exception
    {
        return new ProgressBarConfigurator(persisted_version);
    }

    /** Display 'horizontal': Change whether orientation is horizontal or, if false, vertical */
    public static final WidgetPropertyDescriptor<Boolean> displayHorizontal =
        newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "horizontal", Messages.Horizontal);

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<Boolean> limits_from_pv;
    private volatile WidgetProperty<Double> minimum;
    private volatile WidgetProperty<Double> maximum;
    private volatile WidgetProperty<WidgetColor> fill_color;
    private volatile WidgetProperty<VType> value;
    private volatile WidgetProperty<Boolean> horizontal;

    public ProgressBarWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(fill_color = displayFillColor.createProperty(this, new WidgetColor(60, 255, 60)));
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(limits_from_pv = behaviorLimitsFromPV.createProperty(this, true));
        properties.add(minimum = behaviorMinimum.createProperty(this, 0.0));
        properties.add(maximum = behaviorMaximum.createProperty(this, 100.0));
        properties.add(value = runtimeValue.createProperty(this, null));
        properties.add(horizontal = displayHorizontal.createProperty(this, true));
    }

    /** @return Display 'fill_color' */
    public WidgetProperty<WidgetColor> displayFillColor()
    {
        return fill_color;
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Behavior 'limits_from_pv' */
    public WidgetProperty<Boolean> behaviorLimitsFromPV()
    {
        return limits_from_pv;
    }

    /** @return Behavior 'minimum' */
    public WidgetProperty<Double> behaviorMinimum()
    {
        return minimum;
    }

    /** @return Behavior 'maximum' */
    public WidgetProperty<Double> behaviorMaximum()
    {
        return maximum;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }

    /** @return 'horizontal' */
    public WidgetProperty<Boolean> displayHorizontal()
    {
        return horizontal;
    }
}
