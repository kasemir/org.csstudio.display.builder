/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.bobwidget;

import static org.csstudio.trends.databrowser3.Activator.logger;

import java.io.InputStream;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.ModelThreadPool;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;
import org.csstudio.trends.databrowser3.persistence.XMLPersistence;
import org.csstudio.trends.databrowser3.ui.ControllerJFX;
import org.csstudio.trends.databrowser3.ui.ModelBasedPlot;

import javafx.scene.layout.Pane;

/** OPI Figure that displays data browser plot on screen,
 *  holds a Data Browser Plot
 *
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class DataBrowserWidgetJFX extends JFXBaseRepresentation<Pane, DataBrowserWidget>
{
    /** Change the size */
    private final DirtyFlag dirty_size = new DirtyFlag();
    /** Change the visible of sub elements (toolbar, etc) */
    private final DirtyFlag dirty_opts = new DirtyFlag();
    /** Change the file */
    private final DirtyFlag dirty_file = new DirtyFlag();

    /** Data Browser plot */
    private volatile ModelBasedPlot plot;

    private volatile ControllerJFX controller = null;

    private volatile InputStream model_file_stream = null;

    @Override
    public Pane createJFXNode() throws Exception
    {
        plot = new ModelBasedPlot(! toolkit.isEditMode());
        return plot.getPlot();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        if (controller != null)
            controller.stop();
    }

    /** @return Data Browser Plot */
    public ModelBasedPlot getDataBrowserPlot()
    {
        return plot;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        if (! toolkit.isEditMode())
            controller = new ControllerJFX(model_widget.getDataBrowserModel(), plot);
        model_widget.propWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propShowToolbar().addUntypedPropertyListener(this::optsChanged);

        final String img_name = model_widget.propFile().getValue();
        model_widget.propFile().addPropertyListener(this::fileChanged);
        ModelThreadPool.getExecutor().execute(() -> fileChanged(null, null, img_name));

        if (controller != null)
        {
            try
            {
                controller.start();
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot start controller", ex);
            }
        }
    }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    private void optsChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_opts.mark();
        toolkit.scheduleUpdate(this);
    }

    private void fileChanged(final WidgetProperty<String> property, final String old_value, final String new_value)
    {
        String base_path = new_value;
        try
        {
            model_file_stream = model_widget.getFileInputStream(base_path);
            if (model_file_stream == null)
            {
                System.out.println("Null stream for base path: " + base_path);
                return;
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Failure resolving image path from base path: " + base_path, ex);
            return;
        }

        dirty_file.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_file.checkAndClear())
        {
            //This is being done here because the load function causes effects that need to happen on the FxUserThread
            final InputStream safe_stream = model_file_stream;
            model_file_stream = null;
            if (safe_stream != null)
                try
                {
                    new XMLPersistence().load(model_widget.getDataBrowserModel(), safe_stream);
                }
                catch (Exception ex)
                {
                    logger.log(Level.WARNING, "Error loading data browser content", ex);
                }
        }
        if (dirty_opts.checkAndClear())
        {
            plot.getPlot().showToolbar(model_widget.propShowToolbar().getValue());
        }
        if (dirty_size.checkAndClear())
        {
            plot.getPlot().setPrefWidth(model_widget.propWidth().getValue());
            plot.getPlot().setPrefHeight(model_widget.propHeight().getValue());
        }
    }
}
