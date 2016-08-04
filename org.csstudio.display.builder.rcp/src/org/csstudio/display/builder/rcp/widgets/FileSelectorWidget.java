/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.csstudio.display.builder.model.widgets.VisibleWidget;
import org.csstudio.display.builder.rcp.Messages;
import org.diirt.vtype.VType;

/** Widget for selecting a file
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FileSelectorWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("fileselector", WidgetCategory.CONTROL,
            "File Selector",
            "platform:/plugin/org.csstudio.display.builder.rcp/icons/open_file.png",
            "Select a file, writing path and/or filename to PV")
    {
        @Override
        public Widget createWidget()
        {
            return new FileSelectorWidget();
        }
    };


    enum Filespace
    {
        WORKSPACE("Workspace"),
        FILESYSTEM("Local File System");

        private final String name;
        private Filespace(final String name)  { this.name = name; }
        @Override  public String toString()   { return name;      }
    }

    enum FileComponent
    {
        FULL("Full Path"),
        DIRECTORY("Directory"),
        FULLNAME("Name & Extension"),
        BASENAME("Base Name");

        private final String name;
        private FileComponent(final String name)  { this.name = name; }
        @Override  public String toString()       { return name;      }
    }

    private static final WidgetPropertyDescriptor<Filespace> behaviorFilespace =
            new WidgetPropertyDescriptor<Filespace>(
                WidgetPropertyCategory.BEHAVIOR, "filespace", Messages.WidgetProperties_Filespace)
    {
        @Override
        public EnumWidgetProperty<Filespace> createProperty(final Widget widget,
                                                            final Filespace default_value)
        {
            return new EnumWidgetProperty<Filespace>(this, widget, default_value);
        }
    };

    private static final WidgetPropertyDescriptor<FileComponent> behaviorFilecomponent =
            new WidgetPropertyDescriptor<FileComponent>(
                WidgetPropertyCategory.BEHAVIOR, "component", Messages.WidgetProperties_Filecomponent)
    {
        @Override
        public EnumWidgetProperty<FileComponent> createProperty(final Widget widget,
                                                               final FileComponent default_value)
        {
            return new EnumWidgetProperty<FileComponent>(this, widget, default_value);
        }
    };

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<Filespace> filespace;
    private volatile WidgetProperty<FileComponent> component;
    private volatile WidgetProperty<VType> value;


    public FileSelectorWidget()
    {
        // Set initial size close to the minimum required
        // by the JFX representation of button with icon
        super(WIDGET_DESCRIPTOR.getType(), 40, 25);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(filespace = behaviorFilespace.createProperty(this, Filespace.WORKSPACE));
        properties.add(component = behaviorFilecomponent.createProperty(this, FileComponent.FULL));
        properties.add(value = runtimeValue.createProperty(this, null));
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Behavior 'filespace' */
    public WidgetProperty<Filespace> behaviorFilespace()
    {
        return filespace;
    }

    /** @return Behavior 'component' */
    public WidgetProperty<FileComponent> behaviorComponent()
    {
        return component;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }
}
