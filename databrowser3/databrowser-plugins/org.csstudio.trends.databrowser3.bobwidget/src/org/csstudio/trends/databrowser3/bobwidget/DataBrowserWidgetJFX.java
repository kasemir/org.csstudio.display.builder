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
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
import org.csstudio.trends.databrowser3.model.Model;
import org.csstudio.trends.databrowser3.persistence.XMLPersistence;
import org.csstudio.trends.databrowser3.ui.JFXController;
import org.csstudio.trends.databrowser3.ui.ModelBasedPlot;
import org.eclipse.swt.widgets.Shell;

import javafx.scene.layout.Pane;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

/** OPI Figure that displays data browser plot on screen,
 *  holds a Data Browser Plot
 *
 *  @author Kay Kasemir
 */
public class DataBrowserWidgetJFX extends RegionBaseRepresentation<Pane, DataBrowserWidget>
{
    /** Change the position */
    private final DirtyFlag dirty_pos = new DirtyFlag();
    /** Change the size */
    private final DirtyFlag dirty_size = new DirtyFlag();

    /** Data Browser plot */
    private volatile ModelBasedPlot plot;

    /** Model with data to display */
    private volatile Model model = new Model();

    private volatile JFXController controller;

    private volatile String file_path;

    @Override
    public Pane createJFXNode() throws Exception
    {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        // Plot is only active in runtime mode, not edit mode
        //plot = new RTValuePlot(! toolkit.isEditMode());
        plot = new ModelBasedPlot(new Shell());
        //plot.showToolbar(false);
        //plot.showCrosshair(false);
        return plot.getPlot();

        //return hbox;
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
        controller = new JFXController(model, plot);

        model_widget.propWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propX().addUntypedPropertyListener(this::posChanged);
        model_widget.propY().addUntypedPropertyListener(this::posChanged);

        final String img_name = model_widget.propFile().getValue();
        model_widget.propFile().addPropertyListener(this::fileChanged);
        ModelThreadPool.getExecutor().execute(() -> fileChanged(null, null, img_name));

        try
        {
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

    private void posChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_pos.mark();
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
                final InputStream stream = ModelResourceUtil.openResourceStream(file_path);
                new XMLPersistence().load(model, stream);
            }
            catch (Exception e)
            {
                System.out.println("Failure loading plot file:" + file_path);
                e.printStackTrace();
                load_failed = true;
            }
        }
        //toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        if (dirty_size.checkAndClear())
        {
            plot.getPlot().setPrefWidth(model_widget.propWidth().getValue());
            plot.getPlot().setPrefHeight(model_widget.propHeight().getValue());
        }
        if (dirty_pos.checkAndClear())
        {
            plot.getPlot().setLayoutX(model_widget.propX().getValue());
            plot.getPlot().setLayoutY(model_widget.propY().getValue());
        }
    }
}
