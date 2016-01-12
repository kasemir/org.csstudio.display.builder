/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
    private static final Map<String, Image> icons = Collections.synchronizedMap(new HashMap<>());

    /** Get icon for widget type
     *  @param type Widget type
     *  @return Icon image, may be <code>null</code>
     */
    public static Image getIcon(final String type)
    {
        // Favoring no locks over locking for each access.
        // Keeping lock on 'icons' while in this method would prevent two threads
        // from concurrently requesting a missing icon, both adding it to the map.
        // -> Pity, but not fatal, and not observed in reality because 'Palette'
        //    requests all icons once on startup.
        Image icon = icons.get(type);
        if (icon == null)
        {
            final Optional<WidgetDescriptor> descriptor = WidgetFactory.getInstance().getWidgetDescriptor(type);
            try
            {
                if (descriptor.isPresent())
                {
                    logger.log(Level.FINE, "Obtaining icon for widget type " + type);
                    icon = new Image(descriptor.get().getIconStream());
                    icons.put(type, icon);
                }
                else
                    logger.log(Level.WARNING, "Unknown widget type " + type);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot obtain widget for " + type, ex);
            }
        }
        return icon;
    }
}
