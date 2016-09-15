/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.javafx;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;

/** Create screenshot of a JavaFX scene
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Screenshot
{
    private final File file;

    public static File createTempFile(final String file_prefix) throws Exception
    {
        try
        {
            final File file = File.createTempFile(file_prefix, ".png");
            file.deleteOnExit();
            return file;
        }
        catch (Exception ex)
        {
            throw new Exception("Cannot create tmp. file:\n" + ex.getMessage());
        }
    }

    public Screenshot(final Scene scene, final String file_prefix) throws Exception
    {
        this(scene, createTempFile(file_prefix));
    }

    public Screenshot(final Scene scene, final File file) throws Exception
    {
        this.file = file;

        // Create snapshot file
        final WritableImage jfx = scene.snapshot(null);
        final BufferedImage image = new BufferedImage((int)jfx.getWidth(),
                                                       (int)jfx.getHeight(),
                                                       BufferedImage.TYPE_INT_ARGB);
        SwingFXUtils.fromFXImage(jfx, image);

        // Write to file
        try
        {
            ImageIO.write(image, "png", file);
        }
        catch (Exception ex)
        {
            throw new Exception("Cannot create screenshot " + getFilename(), ex);
        }
    }


    /** @return File that contains the screenshot */
    public File getFile()
    {
        return file;
    }

    /** @return Name of file that contains the screenshot */
    public String getFilename()
    {
        return file.getAbsolutePath();
    }


}
