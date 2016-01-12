/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.PolylineWidget;
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
        model_widget.displayLineColor().addUntypedPropertyListener(this::displayChanged);
        model_widget.displayLineWidth().addUntypedPropertyListener(this::displayChanged);
        model_widget.displayPoints().addUntypedPropertyListener(this::displayChanged);
    }

    private void displayChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
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
                // Change points x/y relative to widget location into
                // on-screen location
                final int x = model_widget.positionX().getValue();
                final int y = model_widget.positionY().getValue();
                final Double[] points = model_widget.displayPoints().getValue().asDoubleArray();
                for (int i=0; i<points.length; i+= 2)
                {
                    points[i] += x;
                    points[i+1] += y;
                }
                jfx_node.getPoints().setAll(points);
                jfx_node.setStroke(JFXUtil.convert(model_widget.displayLineColor().getValue()));
                jfx_node.setStrokeWidth(model_widget.displayLineWidth().getValue());
            }
            else
                jfx_node.setVisible(false);
        }
    }
}
