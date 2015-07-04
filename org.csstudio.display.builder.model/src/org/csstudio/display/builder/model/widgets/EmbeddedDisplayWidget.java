/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
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

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that shows another display inside itself
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EmbeddedDisplayWidget extends BaseWidget
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
    public static final WidgetDescriptor WIDGET_DESCRIPTOR
        = new WidgetDescriptor("embedded", WidgetCategory.STRUCTURE,
                Messages.EmbeddedDisplayWidget_Name,
                "platform:/plugin/org.csstudio.display.builder.model/icons/embedded.png",
                Messages.EmbeddedDisplayWidget_Description,
                Arrays.asList("org.csstudio.opibuilder.widgets.linkingContainer"))
        {
            @Override
            public Widget createWidget()
            {
                return new EmbeddedDisplayWidget();
            }
        };

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

    public EmbeddedDisplayWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(widgetMacros.createProperty(this, new Macros()));
        properties.add(displayFile.createProperty(this, ""));
    }

    /** @return Widget 'macros' */
    public WidgetProperty<Macros> widgetMacros()
    {
        return getProperty(widgetMacros);
    }

    /** @return Display 'file' */
    public WidgetProperty<String> displayFile()
    {
        return getProperty(displayFile);
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
        final Macros my_macros = getPropertyValue(widgetMacros);
        return Macros.merge(base, my_macros);
    }
}
