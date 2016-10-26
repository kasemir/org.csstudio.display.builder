/*******************************************************************************
 * Copyright (c) 2014-2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.util.ResourceUtil;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.jface.resource.ImageDescriptor;
import javafx.scene.image.Image;

/** Not an actual Plugin Activator, but providing plugin-related helpers
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Activator
{
    /** Plugin ID defined in MANIFEST.MF */
    final public static String ID = "org.csstudio.javafx.rtplot";

    final public static Logger logger =  Logger.getLogger(ID);

    /** @param base_name Icon base name (no path, no extension)
     *  @return Image
     *  @throws Exception on error
     */
    public static Image getIcon(final String base_name) throws Exception
    {
        String path = "platform:/plugin/org.csstudio.javafx.rtplot/icons/" + base_name + ".png";
        return new Image(ResourceUtil.openPlatformResource(path));
    }

    public static ImageDescriptor getIconID(final String base_name)
    {
        String path = "platform:/plugin/org.csstudio.javafx.rtplot/icons/" + base_name + ".png";
        try
        {
            return ImageDescriptor.createFromImageData(new ImageData(ResourceUtil.openPlatformResource(path)));
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Cannot load image '" + path + "'", e);
            e.printStackTrace();
        }
        return null;
    }
}
