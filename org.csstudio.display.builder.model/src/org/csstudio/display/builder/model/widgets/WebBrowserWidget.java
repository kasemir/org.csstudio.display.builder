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

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;

/** Widget that displays a static rectangle
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class WebBrowserWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("webbrowser", WidgetCategory.MISC,
            "Web Browser",
            "platform:/plugin/org.csstudio.display.builder.model/icons/web_browser.png",
            "A simple embedded web browser",
            Arrays.asList("org.csstudio.opibuilder.widgets.webbrowser"))
    {
        @Override
        public Widget createWidget()
        {
            return new WebBrowserWidget();
        }
    };

    public static final WidgetPropertyDescriptor<String> widgetURL =
        CommonWidgetProperties.newStringPropertyDescriptor(WidgetPropertyCategory.WIDGET, "url", Messages.WebBrowser_URL);
    public static final WidgetPropertyDescriptor<Boolean> displayShowToolbar =
        CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_toolbar", Messages.WebBrowser_showToolbar);

    private volatile WidgetProperty<String> url;
    private volatile WidgetProperty<Boolean> show_toolbar;

    public WebBrowserWidget()
    {
        super(WIDGET_DESCRIPTOR.getType(), 800, 600);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(url = widgetURL.createProperty(this, ""));
        properties.add(show_toolbar = displayShowToolbar.createProperty(this, true));
    }

    /** @return Widget 'url' */
    public WidgetProperty<String> widgetURL()
    {
        return url;
    }

    /** @return Display 'show_toolbar' */
    public WidgetProperty<Boolean> displayShowToolbar()
    {
        return show_toolbar;
    }
}
