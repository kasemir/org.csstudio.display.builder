/*******************************************************************************
 * Copyright (c) 2014-2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Optional;

import org.csstudio.javafx.rtplot.Annotation;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.data.PlotDataItem;
import org.csstudio.javafx.rtplot.internal.util.GraphicsUtils;
import org.eclipse.osgi.util.NLS;

import javafx.geometry.Point2D;

/** Annotation that's displayed on a YAxis
 *  @param <XTYPE> Data type used for the {@link PlotDataItem}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class AnnotationImpl<XTYPE extends Comparable<XTYPE>> extends Annotation<XTYPE>
{
    /** 'X' marks the spot, and this is it's radius. */
    final private static int X_RADIUS = 4;

    /** What part of this annotation has been selected by the mouse? */
    public static enum Selection
    {   /** Nothing */
        None,
        /** The reference point, i.e. the location on the trace */
        Reference,
        /** The body of the annotation */
        Body
    };

    private Selection selected = Selection.None;

    /** Screen location of reference point, set when painted */
    private Optional<Point> screen_pos = Optional.empty();

    /** Screen location of annotation body, set when painted */
    private Optional<Rectangle> screen_box = Optional.empty();

    /** Constructor */
    public AnnotationImpl(final Trace<XTYPE> trace, final XTYPE position, final double value, final Point2D offset, final String text)
    {
        super(trace, position, value, offset, text);
    }

    /** Set to new location
     *  @param position
     *  @param value
     */
    public void setLocation(final XTYPE position, final double value)
    {
        this.position = position;
        this.value = value;
    }

    /** @param offset New offset from reference point to body of annotation */
    public void setOffset(final Point2D offset)
    {
        this.offset = offset;
    }

    /** @param text New annotation text, may include '\n' */
    public void setText(final String text)
    {
        this.text = text;
    }

    /** Check if the provided mouse location would select the annotation
     *  @param point Location of mouse on screen
     *  @return <code>true</code> if this annotation gets selected at that mouse location
     */
    boolean isSelected(final Point2D point)
    {
        final Optional<Rectangle> rect = screen_box;
        if (rect.isPresent()  &&  rect.get().contains(point.getX(), point.getY()))
        {
            selected = Selection.Body;
            return true;
        }

        if (areWithinDistance(screen_pos, point))
        {
            selected = Selection.Reference;
            return true;
        }

        return false;
    }

    /** @return Current selection state */
    Selection getSelection()
    {
        return selected;
    }

    void deselect()
    {
        selected = Selection.None;
    }

    private boolean areWithinDistance(final Optional<Point> pos, final Point2D point)
    {
        if (pos.isPresent())
        {
            final double dx = Math.abs(pos.get().x - point.getX());
            final double dy = Math.abs(pos.get().y - point.getY());
            return dx*dx + dy*dy <= X_RADIUS*X_RADIUS;
        }
        return false;
    }

    /** Paint the annotation on given gc and axes. */
    void paint(final Graphics2D gc, final AxisPart<XTYPE> xaxis, final YAxisImpl<XTYPE> yaxis)
    {
        final int x = xaxis.getScreenCoord(position);
        final int y = Double.isFinite(value) ? yaxis.getScreenCoord(value) : yaxis.getScreenRange().getLow();
        screen_pos = Optional.of(new Point(x, y));

        String value_text = yaxis.getTicks().format(value);
        final String units = trace.getUnits();
        if (! units.isEmpty())
            value_text += " " + units;
        final String label = NLS.bind(text,
                new Object[]
                {
                    trace.getName(),
                    xaxis.getTicks().format(position),
                    value_text
                });

        // Layout like this:
        //
        //    Text
        //    Blabla
        //    Yaddi yaddi
        //    ___________
        //   /
        //  O
        final Rectangle metrics = GraphicsUtils.measureText(gc, label);
        final int tx = (int) (x + offset.getX()), ty = (int) (y + offset.getY());
        final int txt_top = ty - metrics.height;
        // Update the screen position so that we can later 'select' this annotation.
        final Rectangle rect = new Rectangle(tx, txt_top, metrics.width, metrics.height);
        screen_box = Optional.of(rect);

        // Marker 'O' around the actual x/y point, line to annotation.
        // Line first from actual point, will then paint the 'O' over it
        final int line_x = (x <= tx + metrics.width/2) ? tx : tx+metrics.width;
        final int line_y = (y > ty - metrics.height/2) ? ty : ty-metrics.height;

        final Color o_col = gc.getColor();
        // Text
        gc.setColor(new Color(255, 255, 255, 170));
        gc.fillRect(rect.x, rect.y, rect.width, rect.height);
        gc.setColor(GraphicsUtils.convert(trace.getColor()));
        GraphicsUtils.drawMultilineText(gc, tx, txt_top + metrics.y, label);

        // Fill white, then draw around to get higher-contrast 'O'
        gc.setColor(Color.BLACK);
        gc.drawLine(x, y, line_x, line_y);
        gc.setColor(Color.WHITE);
        gc.fillOval(x-X_RADIUS, y-X_RADIUS, 2*X_RADIUS, 2*X_RADIUS);
        gc.setColor(Color.BLACK);
        gc.drawOval(x-X_RADIUS, y-X_RADIUS, 2*X_RADIUS, 2*X_RADIUS);
        // Line over or under the text
        if (selected != Selection.None)
            gc.drawRect(rect.x, rect.y, rect.width, rect.height);
        else // '___________'
            gc.drawLine(tx, line_y, tx+metrics.width, line_y);


        gc.setColor(o_col);
    }
}
