/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetFactory;

import javafx.scene.image.Image;

/** Cache of widget icons
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetIcons
{
    private static final Logger logger = Logger.getLogger(WidgetIcons.class.getName());

    /** Cache of icon images by widget type */
    private static final Map<String, Image> icons = new ConcurrentHashMap<>();

    /** Get icon for widget type
     *  @param type Widget type
     *  @return Icon image, may be <code>null</code>
     */
    public static Image getIcon(final String type)
    {
        return icons.computeIfAbsent(type, WidgetIcons::loadIcon);
    }

    private static Image loadIcon(final String type)
    {
        final WidgetDescriptor descriptor = WidgetFactory.getInstance().getWidgetDescriptor(type);
        try
        {
            logger.log(Level.FINE, "Obtaining icon for widget type " + type);
            return new Image(descriptor.getIconStream());
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot obtain widget for " + type, ex);
        }
        return null;
    }
}
