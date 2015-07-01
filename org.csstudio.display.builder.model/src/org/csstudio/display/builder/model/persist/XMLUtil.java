/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.persist;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** XML Utility.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class XMLUtil
{
    /** Text encoding used for the XML */
    public static final String ENCODING = "UTF-8";

    /** Open XML document, locate root element
     *  @param stream XML stream
     *  @param expected_root Desired name of root element
     *  @return That root element
     *  @throws Exception on error, including document with wrong root
     */
    public static Element openXMLDocument(final InputStream stream,
            final String expected_root) throws Exception
    {
        // Parse XML
        final Document doc = PositionalXMLReader.readXML(stream);
        doc.getDocumentElement().normalize();

        // Check root element
        final Element root_node = doc.getDocumentElement();
        if (! expected_root.equals(root_node.getNodeName()))
            throw new Exception("Wrong document type. Expected <" +
                    expected_root + "> but found <" +
                    root_node.getNodeName() + ">");
        return root_node;
    }

    /** (Try to) obtain original line number in XML file for a node.
     *
     *  @param node Node in document
     *  @return Line number. Empty if not known.
     */
    public static Optional<Integer> getLineNumber(final Node node)
    {
        final Object info = node.getUserData(PositionalXMLReader.LINE_NUMBER);
        if (info instanceof Integer)
            return Optional.of((Integer)info);
        return Optional.empty();
    }

    /** Get line number info for XML-related error messages.
     *
     *  @param node Node in document
     *  @return Line number as string or "unknown"
     */
    public static String getLineInfo(final Node node)
    {
        final Optional<Integer> number = getLineNumber(node);
        if (number.isPresent())
            return Integer.toString(number.get());
        return "unknown";
    }

    /** Iterator over all Elements (not just Nodes) of a parent */
    private static class ElementIterator implements Iterator<Element>
    {
        private Element next_node;

        ElementIterator(final Node parent)
        {
            next_node = findElement(parent.getFirstChild());
        }

        @Override
        public boolean hasNext()
        {
            return next_node != null;
        }

        @Override
        public Element next()
        {
            final Element current = next_node;
            next_node = findElement(current.getNextSibling());
            return current;
        }
    }

    /** Iterator over all Elements (not just Nodes) of a parent
     *  that have specific name.
     */
    private static class NamedElementIterator implements Iterator<Element>
    {
        private Element next_node;

        NamedElementIterator(final Node parent, final String name)
        {
            next_node = findElementByName(parent.getFirstChild(), name);
        }

        @Override
        public boolean hasNext()
        {
            return next_node != null;
        }

        @Override
        public Element next()
        {
            final Element current = next_node;
            next_node = findElementByName(current.getNextSibling(), current.getNodeName());
            return current;
        }
    }

    /** Obtain all child elements.
     *  @param parent Parent node
     *  @return {@link Iterable} for child elements
     */
    public static Iterable<Element> getChildElements(final Node parent)
    {
        return () -> new ElementIterator(parent);
    }

    /** Obtain all child elements with given name.
     *  @param parent Parent node
     *  @param name Name of child elements
     *  @return {@link Iterable} for matching child elements
     */
    public static Iterable<Element> getChildElements(final Node parent, final String name)
    {
        return () -> new NamedElementIterator(parent, name);
    }

    /** Look for child node of given name.
    *
    *  @param parent Node where to start.
    *  @param name Name of the node to look for.
    *  @return Returns Element or <code>null</code>.
    */
    public static final Element getChildElement(final Node parent, final String name)
    {
        return findElementByName(parent.getFirstChild(), name);
    }

    /** Look for Element node.
     *
     *  <p>Checks the node and its siblings.
     *  Does not descent down the 'child' links.
     *
     *  @param node Node where to start.
     *  @return Returns node, next Element sibling or <code>null</code>.
     */
    public static final Element findElement(Node node)
    {
        while (node != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE)
                return (Element) node;
            node = node.getNextSibling();
        }
        return null;
    }

    /** Look for Element node of given name.
     *
     *  <p>Checks the node itself and its siblings for an {@link Element}.
     *  Does not descent down the 'child' links.
     *
     *  @param node Node where to start.
     *  @param name Name of the node to look for.
     *  @return Returns node, the next matching sibling, or <code>null</code>.
     */
    private static final Element findElementByName(Node node, final String name)
    {
        while (node != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE &&
                node.getNodeName().equals(name))
                return (Element) node;
            node = node.getNextSibling();
        }
        return null;
    }

    /** Get string value of an element.
     *  @param element Element
     *  @return String of the node. Empty string if nothing found.
     */
    public static String getString(final Element element)
    {
        final Node text = element.getFirstChild();
        if (text == null) // <empty /> node
            return "";
        if ((text.getNodeType() == Node.TEXT_NODE  ||
             text.getNodeType() == Node.CDATA_SECTION_NODE))
            return text.getNodeValue();
        return "";
    }

    /** Given a parent element, locate string value of a child node.
     *  @param parent Parent element
     *  @param name Name of child element
     *  @return Value of child element, or empty result
     */
    public static Optional<String> getChildString(final Element parent, final String name)
    {
        final Element child = getChildElement(parent, name);
        if (child != null)
            return Optional.of(getString(child));
        else
            return Optional.empty();
    }

    /** @param text Text that should contain true or false
     *  @param default_value Value to use when text is empty
     *  @return Boolean value of text
     */
    public static boolean parseBoolean(final String text, final boolean default_value)
    {
        if (text == null  ||  text.isEmpty())
            return default_value;
        return Boolean.parseBoolean(text);
    }
}
