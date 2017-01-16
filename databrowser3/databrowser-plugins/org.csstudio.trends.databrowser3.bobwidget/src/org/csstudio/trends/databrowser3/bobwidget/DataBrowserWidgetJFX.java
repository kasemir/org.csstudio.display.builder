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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties.WidgetLineStyle;
import org.csstudio.display.builder.model.util.ModelThreadPool;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;
import org.csstudio.trends.databrowser3.persistence.XMLPersistence;
import org.csstudio.trends.databrowser3.ui.ControllerJFX;
import org.csstudio.trends.databrowser3.ui.ModelBasedPlot;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** OPI Figure that displays data browser plot on screen,
 *  holds a Data Browser Plot
 *
 *  @author Megan Grodowitz
 */
public class DataBrowserWidgetJFX extends JFXBaseRepresentation<Group, DataBrowserWidget>
{
    /** Change the size */
    private final DirtyFlag dirty_size = new DirtyFlag();
    /** Change the visible of sub elements (toolbar, etc) */
    private final DirtyFlag dirty_opts = new DirtyFlag();
    /** Change the file */
    private final DirtyFlag dirty_file = new DirtyFlag();
    /** Change the border */
    private final DirtyFlag dirty_line = new DirtyFlag();

    /** Data Browser plot */
    private volatile ModelBasedPlot plot;

    private volatile ControllerJFX controller = null;

    private volatile InputStream stream;

    //TODO: Add border?
    private volatile Color line_color = Color.GRAY;
    //private static final int inset = 0;
    private volatile int line_width = 1;
    private volatile Rectangle border = new Rectangle();
    private volatile List<Double> dash_array = new ArrayList<Double>();

    @Override
    public Group createJFXNode() throws Exception
    {
        // Plot is only active in runtime mode, not edit mode
        //plot = new RTValuePlot(! toolkit.isEditMode());
        border.setFill(Color.TRANSPARENT);
        plot = new ModelBasedPlot(! toolkit.isEditMode());
        Group gr = new Group(border, plot.getPlot());
        return gr;
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
            controller = new ControllerJFX(model_widget.getModel(), plot);
        model_widget.propWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propShowToolbar().addUntypedPropertyListener(this::optsChanged);

        model_widget.propLineColor().addUntypedPropertyListener(this::lineChanged);
        model_widget.propLineStyle().addUntypedPropertyListener(this::lineChanged);
        model_widget.propLineWidth().addUntypedPropertyListener(this::lineChanged);

        final String img_name = model_widget.propFile().getValue();
        model_widget.propFile().addPropertyListener(this::fileChanged);
        ModelThreadPool.getExecutor().execute(() -> fileChanged(null, null, img_name));

        ModelThreadPool.getExecutor().execute(() -> lineChanged(null, null, null));

        if (controller != null)
            try
            {
                // XXX: stop this when not visible?
                controller.start();
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot start controller", ex);
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

    private void lineChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        if (model_widget.propLineStyle().getValue() == WidgetLineStyle.NONE)
        {
            line_color = Color.TRANSPARENT;
        }
        else {
            line_color = JFXUtil.convert(model_widget.propLineColor().getValue());
        }
        line_width = model_widget.propLineWidth().getValue();
        dash_array.clear();
        dash_array.add((double)line_width);
        dash_array.add((double)line_width * 2);

        dirty_line.mark();
        toolkit.scheduleUpdate(this);
    }


    private void fileChanged(final WidgetProperty<String> property, final String old_value, final String new_value)
    {
        String base_path = new_value;

        try
        {
            stream = model_widget.getFileInputStream(base_path);
            if (stream == null)
            {
                System.out.println("Null stream for base path: " + base_path);
                return;
            }
        }
        catch (Exception e)
        {
            //TODO: change to logging message
            System.out.println("Failure resolving image path from base path: " + base_path);
            e.printStackTrace();
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
            try
            {
                new XMLPersistence().load(model_widget.getModel(), stream);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
            border.setWidth(model_widget.propWidth().getValue());
            border.setHeight(model_widget.propHeight().getValue());
        }
        if (dirty_line.checkAndClear())
        {
            border.setStroke(line_color);
            border.setStrokeWidth(line_width);
            border.getStrokeDashArray().clear();
            if (model_widget.propLineStyle().getValue() == WidgetLineStyle.DASH)
            {
                border.getStrokeDashArray().addAll(dash_array);
            }
        }
    }
}
