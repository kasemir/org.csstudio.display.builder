/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.persist;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetFactory;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Read model from XML.
 *
 *  Stream (SAX, StAX) or DOM (JAXB, JDOM, org.w3c.dom)?
 *  ==============================================
 *  JAXB would allow direct mapping of XML elements into widget properties.
 *  But widgets evolve, properties change.
 *  "hidden=true" may turn into "visible=false".
 *  Multiple properties can be combined into a new one,
 *  or one original property can change into multiple new ones.
 *  Only a DOM allows the new widget to inspect _all_ the properties saved in
 *  a legacy file and transform them into new properties.
 *  Doing this via SAX/StAX can result in not knowing all necessary properties
 *  when trying to transform a specific one.
 *
 *  JDOM, version 2.x, is a little nicer than org.w3c.dom.
 *  org.w3c.dom on the other hand is in JRE. XMLUtil makes it usable.
 *
 *  How to get from XML to widgets?
 *  ===============================
 *  1) As before:
 *  Read <widget type="xy">,
 *  create xyWidget,
 *  then for each property <x>,
 *  use widget.getProperty("x").readFromXML(reader)
 *
 *  + Properties know how to read their own data
 *  + Widget A Property X of type Integer,
 *    Widget B Property X of type String?
 *    No problem, since xyWidget.getProperty("x") returns the correct one,
 *    which can then read its own data.
 *  - As widget version changes, what if xyWidget no longer has "x"?
 *
 *  2) Each widget registers a WidgetConfigurator.
 *  Default implementation behaves as above:
 *  For each property <x> in XML,
 *  use widget.getProperty("x").readFromXML(reader).
 *  .. but widget can provide a custom WidgetConfigurator
 *  and handle legacy properties in a different way.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ModelReader
{
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Element root;
    private final Version version;

    /** Parse display from XML
     *  @param xml XML text
     *  @return DisplayModel
     *  @throws Exception on error
     */
    public static DisplayModel parseXML(final String xml) throws Exception
    {
        final ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        final ModelReader reader = new ModelReader(stream);
        return reader.readModel();
    }

    /** Create reader.
     *  @param stream Input stream to read, will be closed
     *  @throws Exception on error
     */
    public ModelReader(final InputStream stream) throws Exception
    {
        root = XMLUtil.openXMLDocument(stream, XMLTags.DISPLAY);
        version = readVersion(root);
    }

    /** Read model from XML.
     *  @return Model
     *  @throws Exception on error
     */
    public DisplayModel readModel() throws Exception
    {
        final DisplayModel model = new DisplayModel();

        // Read display's own properties
        model.getConfigurator(version).configureFromXML(model, root);
        // Read widgets of model
        readChildWidgets(model, model.runtimeChildren(), root);
        return model;
    }

    final private Set<String> unknown_widget_type = new HashSet<>();

    /** Read all <widget>.. child entries
     *  @param parent_widget Parent widget
     *  @param children 'children' property of the parent widget
     *  @param parent_xml XML of the parent widget from which child entries are read
     */
    private void readChildWidgets(final Widget parent_widget, final ChildrenProperty children, final Element parent_xml)
    {
        for (final Element widget_xml : XMLUtil.getChildElements(parent_xml, XMLTags.WIDGET))
        {
            try
            {
                final Widget widget = readWidget(widget_xml);
                children.addChild(widget);
            }
            catch (final Throwable ex)
            {
                // Mention missing widget only once per reader
                final String message = ex.getMessage();
                if (message != null  &&  message.startsWith("Unknown widget type"))
                {
                    if (! unknown_widget_type.contains(message))
                    {
                        logger.log(Level.WARNING, message + ", line " + XMLUtil.getLineInfo(widget_xml));
                        unknown_widget_type.add(message);
                    }
                }
                else
                    logger.log(Level.WARNING,
                        "Widget configuration file error, line " + XMLUtil.getLineInfo(widget_xml), ex);
                // Continue with next widget
            }
        }
    }

    /** Read widget from XML
     *  @param widget_xml Widget's XML element
     *  @return Widget
     *  @throws Exception on error
     */
    private Widget readWidget(final Element widget_xml) throws Exception
    {
        String type = widget_xml.getAttribute(XMLTags.TYPE);
        if (type.isEmpty())
        {
            // Fall back to legacy opibuilder:
            // <widget typeId="org.csstudio.opibuilder.widgets.Label" version="1.0.0">
            type = widget_xml.getAttribute("typeId");
            if (type.isEmpty())
                throw new Exception("Missing widget type");
        }
        final Widget widget = createWidget(type, widget_xml);

        final ChildrenProperty children = ChildrenProperty.getChildren(widget);
        if (children != null)
            readChildWidgets(widget, children, widget_xml);

        return widget;
    }

    /** Create widget
     *
     *  <p>Cycles through available implementations,
     *  primary and alternate,
     *  returning the first one that accepts the XML.
     *
     *  @param type Widget type ID
     *  @param widget_xml XML with widget configuration
     *  @return Widget
     *  @throws Exception on error
     */
    private Widget createWidget(final String type, final Element widget_xml) throws Exception
    {
        final Version xml_version = readVersion(widget_xml);
        for (WidgetDescriptor desc : WidgetFactory.getInstance().getAllWidgetDescriptors(type))
        {
            final Widget widget = desc.createWidget();
            if (widget.getConfigurator(xml_version).configureFromXML(widget, widget_xml))
                return widget;
        }
        throw new Exception("No suitable widget for " + type);
    }

    /** @param element Element
     *  @return {@link Version} from element attribute
     *  @throws IllegalArgumentException on parse error
     */
    private static Version readVersion(final Element element)
    {
        final String text = element.getAttribute(XMLTags.VERSION);
        if (text.isEmpty())
            return new Version(0, 0, 0);
        return new Version(text);
    }
}
