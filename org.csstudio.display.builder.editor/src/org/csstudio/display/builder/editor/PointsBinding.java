/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayPoints;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.csstudio.display.builder.editor.poly.PointsEditor;
import org.csstudio.display.builder.editor.poly.PointsEditorListener;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.properties.Points;

import javafx.geometry.Point2D;
import javafx.scene.Group;

/** Bind "points" property of selected widget to {@link PointsEditor}
 *
 *  <p>Also updates widget's X, Y, Width, Height to coordinate range of points.
 *  @author Kay Kasemir
 */
public class PointsBinding implements WidgetSelectionListener, PointsEditorListener, WidgetPropertyListener<Integer>
{
    private final Group parent;
    private Widget widget;
    private PointsEditor editor;
    /** Flag to prevent loop when this binding is changing the widget */
    private boolean changing_widget = false;

    public PointsBinding(final Group parent, final WidgetSelectionHandler selection)
    {
        this.parent = parent;
        selection.addListener(this);
    }

    @Override
    public void selectionChanged(final List<Widget> widgets)
    {
        if (widgets != null  &&  widgets.size() == 1)
        {
            final Widget w = widgets.get(0);
            Optional<WidgetProperty<Points>> prop = w.checkProperty(displayPoints);
            if (prop.isPresent())
            {
                createEditor(w);
                return;
            }
        }
        // Not exactly one widget with "points" -> No editor
        disposeEditor();
    }

    /** @param widget Widget for which to create editor */
    private void createEditor(final Widget widget)
    {
        disposeEditor();

        this.widget = Objects.requireNonNull(widget);

        // Turn points from widget into absolute screen coords for editor
        final Points screen_points = widget.getProperty(displayPoints).getValue().clone();
        final Point2D offset = GeometryTools.getDisplayOffset(widget);
        final double x0 = widget.getProperty(positionX).getValue() + offset.getX();
        final double y0 = widget.getProperty(positionY).getValue() + offset.getY();
        final int N=screen_points.size();
        for (int i=0; i<N; ++i)
        {
            screen_points.setX(i, x0 + screen_points.getX(i));
            screen_points.setY(i, y0 + screen_points.getY(i));
        }

        editor = new PointsEditor(parent, screen_points, this);
        widget.getProperty(positionX).addPropertyListener(this);
        widget.getProperty(positionY).addPropertyListener(this);
        widget.getProperty(positionWidth).addPropertyListener(this);
        widget.getProperty(positionHeight).addPropertyListener(this);
    }

    private void disposeEditor()
    {
        if (editor == null)
            return;
        widget.getProperty(positionHeight).removePropertyListener(this);
        widget.getProperty(positionWidth).removePropertyListener(this);
        widget.getProperty(positionY).removePropertyListener(this);
        widget.getProperty(positionX).removePropertyListener(this);
        editor.dispose();
        editor = null;
        widget = null;
    }

    // WidgetPropertyListener
    @Override
    public void propertyChanged(final WidgetProperty<Integer> property,
                                final Integer old_value, final Integer new_value)
    {   // Ignore changes performed by this class
        if (changing_widget)
            return;
        // Delete editor since position has changed, and editor's points
        // are thus invalid
        final Widget active_widget = this.widget;
        disposeEditor();

        // TODO ScalePoints
        if (property.getName().equals(positionWidth.getName()))
        {
            System.out.println("Need to scale width from " + old_value + " to " + new_value);
        }
        else if (property.getName().equals(positionHeight.getName()))
        {
            System.out.println("Need to scale height from " + old_value + " to " + new_value);
        }

        // Re-create editor for changed widget
        createEditor(active_widget);
    }

    @Override
    public void pointsChanged(final Points screen_points)
    {
        // Update widget with edited points
        final Points points = screen_points.clone();
        final int N = points.size();
        // Widget may be inside a container
        final Point2D offset = GeometryTools.getDisplayOffset(widget);
        // Determine coordinate range of points
        double x0 = Double.MAX_VALUE, y0 = Double.MAX_VALUE;
        double x1 = 0, y1 = 0;
        for (int i=0; i<N; ++i)
        {
            final double x = points.getX(i) - offset.getX();
            final double y = points.getY(i) - offset.getY();
            x0 = Math.min(x, x0);
            y0 = Math.min(y, y0);
            x1 = Math.max(x, x1);
            y1 = Math.max(y, y1);
        }
        // Adjust points relative to x0, y0 and widget's container offset
        for (int i=0; i<N; ++i)
        {
            final double x = points.getX(i) - offset.getX();
            final double y = points.getY(i) - offset.getY();
            points.setX(i, x - x0);
            points.setY(i, y - y0);
        }

        changing_widget = true;
        try
        {
            widget.setPropertyValue(displayPoints, points);
            if (N > 0)
            {
                widget.setPropertyValue(positionX, (int)x0);
                widget.setPropertyValue(positionY, (int)y0);
                widget.setPropertyValue(positionWidth, (int)(x1 - x0));
                widget.setPropertyValue(positionHeight, (int)(y1 - y0));
            }
        }
        finally
        {
            changing_widget = false;
        }
    }

    @Override
    public void done()
    {
        disposeEditor();
    }
}
