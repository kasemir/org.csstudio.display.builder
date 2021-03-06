/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.model;

import java.io.PrintWriter;
import java.time.Instant;

import org.csstudio.apputil.xml.DOMHelper;
import org.csstudio.apputil.xml.XMLWriter;
import org.csstudio.trends.databrowser3.persistence.XMLPersistence;
import org.w3c.dom.Element;

import javafx.geometry.Point2D;

/** Information about a Plot Annotation
 *
 *  <p>In the current implementation the plot library actually
 *  tracks the currently active annotations.
 *
 *  <p>This class is used by the model to read initial annotations
 *  from the {@link Model}'s XML file,
 *  and to later read them back from the {@link ModelBasedPlotSWT} for writing
 *  them to the XML file.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class AnnotationInfo
{
    final private boolean internal;
    final int item_index;
    final private Instant time;
    final private double value;
    final private Point2D offset;
    final private String text;

    public AnnotationInfo(final boolean internal, final int item_index, final Instant time, final double value, final Point2D point2d, final String text)
    {
        this.internal = internal;
        this.item_index = item_index;
        this.time = time;
        this.value = value;
        this.offset = point2d;
        this.text = text;
    }

    /** @return Internal annotation, created by databrowser and not user? */
    public boolean isInternal()
    {
        return internal;
    }

    /** @return Index of item */
    public int getItemIndex()
    {
        return item_index;
    }

    /** @return Time stamp */
    public Instant getTime()
    {
        return time;
    }

    /** @return Value */
    public double getValue()
    {
        return value;
    }

    /** @return Offset */
    public Point2D getOffset()
    {
        return offset;
    }

    /** @return Text */
    public String getText()
    {
        return text;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = internal ? prime : 0;
        result = prime * result + item_index;
        result = prime * result + offset.hashCode();
        result = prime * result + text.hashCode();
        result = prime * result + time.hashCode();
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (! (obj instanceof AnnotationInfo))
            return false;
        final AnnotationInfo other = (AnnotationInfo) obj;
        return internal == other.internal      &&
               item_index == other.item_index  &&
               offset.equals(other.offset)     &&
               text.equals(other.text)         &&
               time.equals(other.time)         &&
               Double.doubleToLongBits(value) == Double.doubleToLongBits(other.value);
    }

    /** Write XML formatted annotation configuration
     *  @param writer PrintWriter
     */
    public void write(final PrintWriter writer)
    {
        if (internal)
            return;
        XMLWriter.start(writer, 2, XMLPersistence.TAG_ANNOTATION);
        writer.println();
        XMLWriter.XML(writer, 3, XMLPersistence.TAG_PV, item_index);
        XMLWriter.XML(writer, 3, XMLPersistence.TAG_TIME, TimeHelper.format(time));
        XMLWriter.XML(writer, 3, XMLPersistence.TAG_VALUE, value);
        XMLWriter.start(writer, 3, XMLPersistence.TAG_OFFSET);
        writer.println();
        XMLWriter.XML(writer, 4, XMLPersistence.TAG_X, offset.getX());
        XMLWriter.XML(writer, 4, XMLPersistence.TAG_Y, offset.getY());
        XMLWriter.end(writer, 3, XMLPersistence.TAG_OFFSET);
        writer.println();
        XMLWriter.XML(writer, 3, XMLPersistence.TAG_TEXT, text);
        XMLWriter.end(writer, 2, XMLPersistence.TAG_ANNOTATION);
        writer.println();
    }

    /** Create {@link AnnotationInfo} from XML document
     *  @param node XML node with item configuration
     *  @return PVItem
     *  @throws Exception on error
     */
    public static AnnotationInfo fromDocument(final Element node) throws Exception
    {
        final int item_index = DOMHelper.getSubelementInt(node, XMLPersistence.TAG_PV, -1);
        final String timetext = DOMHelper.getSubelementString(node, XMLPersistence.TAG_TIME);
        final Instant time = TimeHelper.parse(timetext);
        final double value = DOMHelper.getSubelementDouble(node, XMLPersistence.TAG_VALUE, 0.0);
        final String text = DOMHelper.getSubelementString(node, XMLPersistence.TAG_TEXT);

        double x = 20;
        double y = 20;
        Element offset = DOMHelper.findFirstElementNode(node.getFirstChild(), XMLPersistence.TAG_OFFSET);
        if (offset != null)
        {
            x = DOMHelper.getSubelementDouble(offset, XMLPersistence.TAG_X, x);
            y = DOMHelper.getSubelementDouble(offset, XMLPersistence.TAG_Y, y);
        }
        return new AnnotationInfo(false, item_index, time, value, new Point2D(x, y), text);
    }

    @Override
    public String toString()
    {
        return (internal ? "Internal Annotation for item " : "Annotation for item ") + item_index + ": '" +
               text + "' @ " + TimeHelper.format(time) + ", " + value;
    }
}
