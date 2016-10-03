/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.ModelPlugin.logger;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFile;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMacros;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.RuntimeWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Widget that shows another display inside itself
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EmbeddedDisplayWidget extends VisibleWidget
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

    /** Resize behavior */
    public enum Resize
    {
        /** No resize, add scroll bars if content too large for container */
        None(Messages.Resize_None),

        /** Size *.opi to fit the container */
        ResizeContent(Messages.Resize_Content),

        /** Size the container to fit the linked *.opi */
        SizeToContent(Messages.Resize_Container);

        private final String label;

        private Resize(final String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

    private static final WidgetPropertyDescriptor<Resize> propResize =
        new WidgetPropertyDescriptor<Resize>(
            WidgetPropertyCategory.DISPLAY, "resize", Messages.WidgetProperties_ResizeBehavior)
    {
        @Override
        public EnumWidgetProperty<Resize> createProperty(final Widget widget,
                                                         final Resize default_value)
        {
            return new EnumWidgetProperty<Resize>(this, widget, default_value);
        }
    };

    private static final WidgetPropertyDescriptor<String> propGroupName =
        CommonWidgetProperties.newStringPropertyDescriptor(
            WidgetPropertyCategory.DISPLAY, "group_name", Messages.EmbeddedDisplayWidget_GroupName);

    private static final WidgetPropertyDescriptor<DisplayModel> runtimeModel =
        new WidgetPropertyDescriptor<DisplayModel>(WidgetPropertyCategory.RUNTIME, "embedded_model", "Embedded Model")
        {
            @Override
            public WidgetProperty<DisplayModel> createProperty(final Widget widget, DisplayModel default_value)
            {
                return new RuntimeWidgetProperty<DisplayModel>(runtimeModel, widget, default_value)
                {
                    @Override
                    public void setValueFromObject(final Object value)
                            throws Exception
                    {
                        if (! (value instanceof DisplayModel))
                            throw new IllegalArgumentException("Expected DisplayModel, got " + Objects.toString(value));
                        doSetValue((DisplayModel)value, true);
                    }
                };
            }
        };

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

    /** Custom configurator to read legacy *.opi files */
    private static class EmbeddedDisplayWidgetConfigurator extends WidgetConfigurator
    {
        public EmbeddedDisplayWidgetConfigurator(final Version xml_version)
        {
            super(xml_version);
        }

        @Override
        public boolean configureFromXML(final ModelReader model_reader, final Widget widget, final Element xml)
                throws Exception
        {
            if (! super.configureFromXML(model_reader, widget, xml))
                return false;

            // Fall back to legacy "opi_file" for display file
            if (XMLUtil.getChildElement(xml, propFile.getName()) == null)
            {
                final Optional<String> opi_file = XMLUtil.getChildString(xml, "opi_file");
                if (opi_file.isPresent())
                    widget.setPropertyValue(propFile, opi_file.get());
            }

            // Transition legacy "resize_behaviour"
            final Element element = XMLUtil.getChildElement(xml, "resize_behaviour");
            if (element != null)
            {
                try
                {   // 0=SIZE_OPI_TO_CONTAINER, 1=SIZE_CONTAINER_TO_OPI, 2=CROP_OPI, 3=SCROLL_OPI
                    final int old_resize = Integer.parseInt(XMLUtil.getString(element));
                    if (old_resize == 0)
                        widget.setPropertyValue(propResize, Resize.ResizeContent);
                    else if (old_resize == 1)
                        widget.setPropertyValue(propResize, Resize.SizeToContent);
                    else
                        widget.setPropertyValue(propResize, Resize.None);
                }
                catch (NumberFormatException ex)
                {
                    logger.log(Level.WARNING, "Cannot decode legacy resize_behavior");
                }
            }
            return true;
        }
    }

    private volatile WidgetProperty<Macros> macros;
    private volatile WidgetProperty<String> file;
    private volatile WidgetProperty<Resize> resize;
    private volatile WidgetProperty<String> group_name;
    private volatile WidgetProperty<DisplayModel> embedded_model;

    public EmbeddedDisplayWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 400, 300);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(file = propFile.createProperty(this, ""));
        properties.add(macros = propMacros.createProperty(this, new Macros()));
        properties.add(resize = propResize.createProperty(this, Resize.None));
        properties.add(group_name = propGroupName.createProperty(this, ""));
        properties.add(embedded_model = runtimeModel.createProperty(this, null));

        // Initial size
        propWidth().setValue(300);
        propHeight().setValue(200);
    }

    /** @return 'macros' property */
    public WidgetProperty<Macros> propMacros()
    {
        return macros;
    }

    /** @return 'file' property */
    public WidgetProperty<String> propFile()
    {
        return file;
    }

    /** @return 'resize' property */
    public WidgetProperty<Resize> propResize()
    {
        return resize;
    }

    /** @return 'group_name' property */
    public WidgetProperty<String> propGroupName()
    {
        return group_name;
    }

    /** @return Runtime 'model' property for the embedded display */
    public WidgetProperty<DisplayModel> runtimePropEmbeddedModel()
    {
        return embedded_model;
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
        final Macros my_macros = propMacros().getValue();
        return base == null ? my_macros : Macros.merge(base, my_macros);
    }
}
