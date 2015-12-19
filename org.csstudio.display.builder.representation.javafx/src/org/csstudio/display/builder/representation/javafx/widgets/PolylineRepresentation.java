/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.widgets.PolylineWidget;
import org.csstudio.display.builder.model.widgets.PolylineWidget.PointWidgetProperty;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class PolylineRepresentation extends JFXBaseRepresentation<Polyline, PolylineWidget>
{
    private final DirtyFlag dirty_display = new DirtyFlag();

    // Need known instance of this listener, not "this::pointChanged",
    // to be able to _remove_ one that's previously added
    private WidgetPropertyListener<Integer> pointChanged =
        (final WidgetProperty<Integer> property,
         final Integer old_value, final Integer new_value) ->
    {
        dirty_display.mark();
        toolkit.scheduleUpdate(this);
    };


    @Override
    public Polyline createJFXNode() throws Exception
    {
        final Polyline polyline = new Polyline();
        polyline.setStrokeLineJoin(StrokeLineJoin.ROUND);
        polyline.setStrokeLineCap(StrokeLineCap.BUTT);
        return polyline;
    }

    @Override
    protected void registerListeners()
    {
        // Polyline can't use the default x/y handling from super.registerListeners();
        model_widget.positionVisible().addUntypedPropertyListener(this::displayChanged);
        model_widget.positionX().addUntypedPropertyListener(this::displayChanged);
        model_widget.positionY().addUntypedPropertyListener(this::displayChanged);
        model_widget.positionWidth().addUntypedPropertyListener(this::displayChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::displayChanged);
        model_widget.displayLineColor().addUntypedPropertyListener(this::displayChanged);
        model_widget.displayLineWidth().addUntypedPropertyListener(this::displayChanged);

        // Listen to list for added/removed points
        model_widget.displayPoints().addPropertyListener(this::pointsChanged);
        // For each point, listen to coordinate changes
        for (PointWidgetProperty point : model_widget.displayPoints().getValue())
        {
            point.x().addPropertyListener(pointChanged);
            point.y().addPropertyListener(pointChanged);
        }
    }

    private void displayChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_display.mark();
        toolkit.scheduleUpdate(this);
    }

    private void pointsChanged(final WidgetProperty<List<PointWidgetProperty>> property,
                               final List<PointWidgetProperty> removed, final List<PointWidgetProperty> added)
    {
        if (removed != null)
            for (PointWidgetProperty point : removed)
            {
                point.x().removePropertyListener(pointChanged);
                point.y().removePropertyListener(pointChanged);
            }
        if (added != null)
            for (PointWidgetProperty point : added)
            {
                point.x().addPropertyListener(pointChanged);
                point.y().addPropertyListener(pointChanged);
            }
        dirty_display.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        // Not using default handling of X/Y super.updateChanges();
        if (dirty_display.checkAndClear())
        {
            if (model_widget.positionVisible().getValue())
            {
                jfx_node.setVisible(true);

                final int x = model_widget.positionX().getValue();
                final int y = model_widget.positionY().getValue();
                final List<PointWidgetProperty> points = model_widget.displayPoints().getValue();
                final int N = points.size();
                final List<Double> coords = new ArrayList<>(2*N);
                for (int i=0; i<N; ++i)
                {
                    coords.add(x + points.get(i).x().getValue().doubleValue());
                    coords.add(y + points.get(i).y().getValue().doubleValue());
                }
                jfx_node.getPoints().setAll(coords);

                jfx_node.setStroke(JFXUtil.convert(model_widget.displayLineColor().getValue()));
                jfx_node.setStrokeWidth(model_widget.displayLineWidth().getValue());
            }
            else
                jfx_node.setVisible(false);
        }
    }
}
