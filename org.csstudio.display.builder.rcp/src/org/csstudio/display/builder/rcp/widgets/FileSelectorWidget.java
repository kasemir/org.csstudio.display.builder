/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.widgets;

import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.csstudio.display.builder.model.widgets.PVWidget;
import org.csstudio.display.builder.rcp.Messages;

/** Widget for selecting a file
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class FileSelectorWidget extends PVWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("fileselector", WidgetCategory.CONTROL,
            "File Selector",
            "platform:/plugin/org.csstudio.display.builder.rcp/icons/file-selector.png",
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

    private static final WidgetPropertyDescriptor<Filespace> propFilespace =
            new WidgetPropertyDescriptor<Filespace>(
                WidgetPropertyCategory.WIDGET, "filespace", Messages.WidgetProperties_Filespace)
    {
        @Override
        public EnumWidgetProperty<Filespace> createProperty(final Widget widget,
                                                            final Filespace default_value)
        {
            return new EnumWidgetProperty<Filespace>(this, widget, default_value);
        }
    };

    private static final WidgetPropertyDescriptor<FileComponent> propFilecomponent =
            new WidgetPropertyDescriptor<FileComponent>(
                WidgetPropertyCategory.WIDGET, "component", Messages.WidgetProperties_Filecomponent)
    {
        @Override
        public EnumWidgetProperty<FileComponent> createProperty(final Widget widget,
                                                               final FileComponent default_value)
        {
            return new EnumWidgetProperty<FileComponent>(this, widget, default_value);
        }
    };

    private volatile WidgetProperty<Filespace> filespace;
    private volatile WidgetProperty<FileComponent> component;


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
        properties.add(filespace = propFilespace.createProperty(this, Filespace.WORKSPACE));
        properties.add(component = propFilecomponent.createProperty(this, FileComponent.FULL));
    }

    /** @return 'filespace' property */
    public WidgetProperty<Filespace> propFilespace()
    {
        return filespace;
    }

    /** @return 'component' property */
    public WidgetProperty<FileComponent> propComponent()
    {
        return component;
    }

}
