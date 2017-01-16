/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.editor;

import java.io.File;
import java.util.logging.Level;

import org.csstudio.javafx.Activator;
import org.csstudio.javafx.Screenshot;
import org.csstudio.javafx.rtplot.RTTimePlot;
import org.eclipse.osgi.util.NLS;

/** Helper for creating Screen-shot of XYGraph
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TempScreenshot
{
    private File file;

    /** Create screen-shot
     *  @param graph XYGraph from which to create the screen-shot
     *  @throws Exception on I/O error
     */
    public TempScreenshot(final RTTimePlot graph) throws Exception
    {
        // Get name for snapshot file
        try
        {
            file = File.createTempFile("DataBrowser", ".png");
            file.deleteOnExit();
        }
        catch (Exception ex)
        {
            throw new Exception("Cannot create tmp. file:\n" + ex.getMessage());
        }

        final Screenshot screenshot;
        try
        {
            screenshot = new Screenshot(graph.getPlotNode());
        }
        catch (Exception ex)
        {
            Activator.logger.log(Level.SEVERE, "Could not create temporary screenshot for email", ex);
            return;
        }

        // Create snapshot file
        try
        {
            screenshot.writeToFile(new File(getFilename()));
        }
        catch (Exception ex)
        {
            throw new Exception(
                    NLS.bind("Cannot create snapshot in {0}:\n{1}",
                            getFilename(), ex.getMessage()));
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
