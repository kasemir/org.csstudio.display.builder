/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/** Property UI
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PropertyPanel extends ScrollPane
{
    private final DisplayEditor editor;
    private final PropertyPanelSection section = new PropertyPanelSection();

    /** @param selection Selection handler
     *  @param undo 'Undo' manager
     */
    public PropertyPanel(final DisplayEditor editor)
    {
        this.editor = editor;

        final Label header = new Label("Properties");
        header.setMaxWidth(Double.MAX_VALUE);
        header.getStyleClass().add("header");

        final VBox box = new VBox(header, section);
        setContent(box);

        // Track currently selected widgets
        editor.getWidgetSelectionHandler().addListener(this::setSelectedWidgets);
    }

    /** Populate UI with properties of widgets
     *  @param widgets Widgets to configure
     */
    private void setSelectedWidgets(final List<Widget> widgets)
    {
    	section.clear();

        if (widgets.size() < 1)
        {   // Use the DisplayModel
            final DisplayModel model = editor.getModel();
            if (model != null)
                section.fill(editor.getUndoableActionManager(), model.getProperties(), Collections.emptyList(), true);
        }
        else
        {   // Determine common properties
            final List<Widget> other = new ArrayList<>(widgets);
            final Widget primary = other.remove(0);
            final Set<WidgetProperty<?>> properties = commonProperties(primary, other);
            section.fill(editor.getUndoableActionManager(), properties, other, true);
        }
    }

    /** Determine common properties
     *  @param primary Primary widget, the one selected first
     *  @param other Zero or more 'other' widgets
     *  @return Common properties
     */
    private Set<WidgetProperty<?>> commonProperties(final Widget primary, final List<Widget> other)
    {
        if (other.contains(primary))
            throw new IllegalArgumentException("Primary widget " + primary + " included in 'other'");

        // Start with properties of primary widget
        final Set<WidgetProperty<?>> common = new LinkedHashSet<>(primary.getProperties());
        // Keep properties shared by other widgets, i.e. remove those _not_ in other
        for (Widget o : other)
            common.removeIf(prop  ->  ! o.checkProperty(prop.getName()).isPresent());
        return common;
    }
}
