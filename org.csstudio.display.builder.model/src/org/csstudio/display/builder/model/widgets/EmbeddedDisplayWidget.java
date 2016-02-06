/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFile;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that shows another display inside itself
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EmbeddedDisplayWidget extends Widget
{
    /** Reserved widget user data key for representation container.
     *
     *  <p>The representation of the {@link EmbeddedDisplayWidget} sets this
     *  to the toolkit-specific container item which becomes the 'parent'
     *  of the representations for the embedded content.
     *
     *  <p>The embedded widget runtime later reads this whenever it needs
     *  to create a toolkit representation for the embedded model.
     */
    public static final String USER_DATA_EMBEDDED_DISPLAY_CONTAINER = "_embedded_widget_container";


    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("embedded", WidgetCategory.STRUCTURE,
            "Embedded Display",
            "platform:/plugin/org.csstudio.display.builder.model/icons/embedded.png",
            "Widget that embeds another display",
            Arrays.asList("org.csstudio.opibuilder.widgets.linkingContainer"))
    {
        @Override
        public Widget createWidget()
        {
            return new EmbeddedDisplayWidget();
        }
    };

    private static final WidgetPropertyDescriptor<Double> runtimeScale =
        CommonWidgetProperties.newDoublePropertyDescriptor(
            WidgetPropertyCategory.RUNTIME, "scale", Messages.WidgetProperties_ScaleFactor);

    
    /** Custom configurator to read legacy *.opi files */
    private static class EmbeddedDisplayWidgetConfigurator extends WidgetConfigurator
    {
        public EmbeddedDisplayWidgetConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public void configureFromXML(Widget widget, Element xml)
                throws Exception
        {
            super.configureFromXML(widget, xml);

            // Fall back to legacy opi_file for display file
            if (XMLUtil.getChildElement(xml, displayFile.getName()) == null)
            {
                final Optional<String> opi_file = XMLUtil.getChildString(xml, "opi_file");
                if (opi_file.isPresent())
                    widget.setPropertyValue(displayFile, opi_file.get());
            }
        }
    }

    private volatile WidgetProperty<Macros> macros;
    private volatile WidgetProperty<String> file;
    // TODO Add scale mode (none=scroll bars, resize content, size to content)
    private volatile WidgetProperty<Double> scale;

    public EmbeddedDisplayWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 400, 300);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(macros = widgetMacros.createProperty(this, new Macros()));
        properties.add(file = displayFile.createProperty(this, ""));
        properties.add(scale = runtimeScale.createProperty(this, 1.0));

        // Initial size
        positionWidth().setValue(300);
        positionHeight().setValue(200);
    }

    /** @return Widget 'macros' */
    public WidgetProperty<Macros> widgetMacros()
    {
        return macros;
    }

    /** @return Display 'file' */
    public WidgetProperty<String> displayFile()
    {
        return file;
    }

    /** @return Runtime 'scale' */
    public WidgetProperty<Double> runtimeScale()
    {
        return scale;
    }

    @Override
    public WidgetConfigurator getConfigurator(Version persisted_version)
            throws Exception
    {
        return new EmbeddedDisplayWidgetConfigurator(persisted_version);
    }

    /** Embedded widget adds/replaces parent macros
     *  @return {@link Macros}
     */
    @Override
    public Macros getEffectiveMacros()
    {
        final Macros base = super.getEffectiveMacros();
        final Macros my_macros = widgetMacros().getValue();
        return base == null ? my_macros : Macros.merge(base, my_macros);
    }
}
