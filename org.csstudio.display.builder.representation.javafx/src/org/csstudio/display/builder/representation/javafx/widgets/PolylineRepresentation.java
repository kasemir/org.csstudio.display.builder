/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.PolylineWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class PolylineRepresentation extends JFXBaseRepresentation<Group, PolylineWidget>
{
    private final DirtyFlag dirty_display = new DirtyFlag();

    @Override
    public Group createJFXNode() throws Exception
    {
        final Polyline polyline = new Polyline();
        polyline.setStrokeLineJoin(StrokeLineJoin.ROUND);
        polyline.setStrokeLineCap(StrokeLineCap.BUTT);
        return new Group(polyline, new Arrow(), new Arrow());
    }

    public static class Arrow extends Polygon
    {
        private final int arrowLength = 20; //make non-final if arrow_length property added

        Arrow() { super(); }

        /**
         * Adjust points of arrow
         * 
         * @param x1 x-coordinate for base of arrow (end of line)
         * @param y1 y-coordinate for base of arrow (end of line)
         * @param x2 x-coordinate for line extending from arrow
         * @param y2 y-coordinate for line extending from arrow
         */
        public void adjustPoints(final double x1, final double y1, final double x2, final double y2)
        {
            getPoints().clear();
            getPoints().addAll(points(x1, y1, x2, y2));
        }

        //calculates points from coordinates of arrow's extending line
        private List<Double> points(final double x1, final double y1, final double x2, final double y2)
        {
            //calculate lengths (x-projection, y-projection, and magnitude) of entire arrow, including extending line
            final double dx = x1 - x2;
            final double dy = y1 - y2;
            final double d = Math.sqrt(dx * dx + dy * dy);
            //calculate x- and y-coordinates for midpoint of arrow base
            final double x0 = (d != 0) ? x1 - dx * arrowLength / d : x1;
            final double y0 = (d != 0) ? y1 - dy * arrowLength / d : y1;
            //calculate offset between midpoint and ends of arrow base
            final double x_ = (y1 - y0) / 4;
            final double y_ = (x1 - x0) / 4;
            //return result
            return Arrays.asList(x0 + x_, y0 - y_, x1, y1, x0 - x_, y0 + y_);
        }
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
        model_widget.displayArrows().addUntypedPropertyListener(this::displayChanged);
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
                final List<Node> children = jfx_node.getChildrenUnmodifiable();
                final Color color = JFXUtil.convert(model_widget.displayLineColor().getValue());
                final int line_width = model_widget.displayLineWidth().getValue();
                final int arrows_val = model_widget.displayArrows().getValue().ordinal();
                int i = 0;
                for (Node child : children)
                {
                    if (child instanceof Polyline)
                        ((Polyline) child).getPoints().setAll(points);
                    else //child instanceof Arrow
                    {
                        Arrow arrow = (Arrow)child;
                        arrow.setFill(color);
                        if ((i & arrows_val) != 0 && points.length > 3)
                        {
                            arrow.setVisible(true);
                            if (i == 2) //to-arrow (pointing towards original point)
                                arrow.adjustPoints(points[0], points[1], points[2], points[3]);
                            else //i == 1 //from-arrow (point away from original point)
                            {
                                final int len = points.length;
                                arrow.adjustPoints(points[len-2], points[len-1], points[len-4], points[len-3]);
                            }
                        }
                        else
                            arrow.setVisible(false);
                    }
                    ((Shape) child).setStroke(color);
                    ((Shape) child).setStrokeWidth(line_width);
                    i++;
                }
            }
            else
                jfx_node.setVisible(false);
        }
    }
}
