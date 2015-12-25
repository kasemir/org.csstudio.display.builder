/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayPoints;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;

import java.util.List;
import java.util.Optional;

import org.csstudio.display.builder.editor.poly.PointsEditor;
import org.csstudio.display.builder.editor.poly.PointsEditorListener;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.Points;

import javafx.geometry.Point2D;
import javafx.scene.Group;

/** Bind "points" property of selected widget to {@link PointsEditor}
 *  @author Kay Kasemir
 */
public class PointsBinding implements WidgetSelectionListener, PointsEditorListener, UntypedWidgetPropertyListener
{
    private final Group parent;
    private Widget widget;
    private PointsEditor editor;

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
                widget = w;
                // Turn points from widget into absolute screen coords for editor
                final Points screen_points = prop.get().getValue().clone();
                final Point2D offset = GeometryTools.getDisplayOffset(widget);
                final double x0 = widget.getProperty(positionX).getValue() + offset.getX();
                final double y0 = widget.getProperty(positionY).getValue() + offset.getY();
                for (int i=screen_points.size()-1; i>=0; --i)
                {
                    screen_points.setX(i, x0 + screen_points.getX(i));
                    screen_points.setY(i, y0 + screen_points.getY(i));
                }
                createEditor(screen_points);
                return;
            }
        }
        // Not exactly one widget with "points" -> No editor
        disposeEditor();
    }

    /** @param screen_points Points for which to create editor */
    private void createEditor(final Points screen_points)
    {
        disposeEditor();
        editor = new PointsEditor(parent, screen_points, this);
        widget.getProperty(positionX).addUntypedPropertyListener(this);
        widget.getProperty(positionY).addUntypedPropertyListener(this);
    }

    private void disposeEditor()
    {
        if (editor == null)
            return;
        widget.getProperty(positionY).removePropertyListener(this);
        widget.getProperty(positionX).removePropertyListener(this);
        editor.dispose();
        editor = null;
        widget = null;
    }

    // UntypedWidgetPropertyListener:
    // Delete editor since position has changed, and editor's points
    // are thus invalid
    @Override
    public void propertyChanged(WidgetProperty<?> property, Object old_value,
            Object new_value)
    {
        disposeEditor();
    }


    @Override
    public void pointsChanged(final Points screen_points)
    {
        // Update widget with transformed points
        final Points points = screen_points.clone();
        final Point2D offset = GeometryTools.getDisplayOffset(widget);
        final double x0 = widget.getProperty(positionX).getValue() + offset.getX();
        final double y0 = widget.getProperty(positionY).getValue() + offset.getY();
        for (int i=points.size()-1; i>=0; --i)
        {
            points.setX(i, points.getX(i) - x0);
            points.setY(i, points.getY(i) - y0);
        }
        widget.setPropertyValue(displayPoints, points);
    }

    @Override
    public void done()
    {
        disposeEditor();
    }
}
