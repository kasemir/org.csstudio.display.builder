/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.bobwidget;

import java.io.InputStream;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.util.ModelThreadPool;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;
import org.csstudio.trends.databrowser3.model.Model;
import org.csstudio.trends.databrowser3.persistence.XMLPersistence;
import org.csstudio.trends.databrowser3.ui.ControllerJFX;
import org.csstudio.trends.databrowser3.ui.ModelBasedPlot;
import org.eclipse.swt.widgets.Shell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;

/** OPI Figure that displays data browser plot on screen,
 *  holds a Data Browser Plot
 *
 *  @author Kay Kasemir
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

    /** Model with data to display */
    private volatile Model model = new Model();

    private volatile ControllerJFX controller;

    //TODO: Do we need to write out the file when user changes the databrowser options, etc?
    private volatile String file_path;
    private volatile InputStream stream;

    //TODO: Add border?
    private static Color line_color = Color.GRAY;
    //private static final int inset = 0;
    //private static final int border_width = 1;
    private volatile Rectangle border = new Rectangle();

    @Override
    public Group createJFXNode() throws Exception
    {
        // Plot is only active in runtime mode, not edit mode
        //plot = new RTValuePlot(! toolkit.isEditMode());
        //TODO: Remove need for shell in javafx widget
        border.setFill(Color.TRANSPARENT);
        plot = new ModelBasedPlot(new Shell());
        Group gr = new Group(border, plot.getPlot());
        return gr;
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
        controller = new ControllerJFX(model, plot);

        model_widget.propWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propShowToolbar().addUntypedPropertyListener(this::optsChanged);

        model_widget.propLineColor().addUntypedPropertyListener(this::lineChanged);
        model_widget.propLineColor().addUntypedPropertyListener(this::lineChanged);
        model_widget.propLineColor().addUntypedPropertyListener(this::lineChanged);

        final String img_name = model_widget.propFile().getValue();
        model_widget.propFile().addPropertyListener(this::fileChanged);
        ModelThreadPool.getExecutor().execute(() -> fileChanged(null, null, img_name));

        ModelThreadPool.getExecutor().execute(() -> fileChanged(null, null, null));

        try
        {
            //TODO: stop this when not visible?
            controller.start();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        line_color = JFXUtil.convert(model_widget.propLineColor().getValue());
        dirty_line.mark();
        toolkit.scheduleUpdate(this);
    }


    private void fileChanged(final WidgetProperty<String> property, final String old_value, final String new_value)
    {
        String base_path = new_value;
        boolean load_failed = false;

        try
        {
            // expand macros in the file name
            final String expanded_path = MacroHandler.replace(model_widget.getMacrosOrProperties(), base_path);
            // Resolve new image file relative to the source widget model (not 'top'!)
            // Get the display model from the widget tied to this representation
            final DisplayModel widget_model = model_widget.getDisplayModel();
            // Resolve the path using the parent model file path
            file_path = ModelResourceUtil.resolveResource(widget_model, expanded_path);
        }
        catch (Exception e)
        {
            System.out.println("Failure resolving image path from base path: " + base_path);
            e.printStackTrace();
            load_failed = true;
        }

        if (!load_failed)
        {
            try
            {
                stream = ModelResourceUtil.openResourceStream(file_path);
                //new XMLPersistence().load(model, stream);
            }
            catch (Exception e)
            {
                //System.out.println("Failure loading plot file:" + file_path);
                e.printStackTrace();
                load_failed = true;
            }
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
                new XMLPersistence().load(model, stream);
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
            border.setStrokeWidth(model_widget.propLineWidth().getValue());
        }
    }
}
