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
import java.util.Objects;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.TableWidget;
import org.csstudio.display.builder.model.widgets.TableWidget.ColumnProperty;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.StringTable;

import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TableRepresentation extends RegionBaseRepresentation<StringTable, TableWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_columns = new DirtyFlag();
    private final DirtyFlag dirty_data = new DirtyFlag();

    private volatile List<List<String>> data;

    private final UntypedWidgetPropertyListener column_listener = (WidgetProperty<?> property, Object old_value, Object new_value) ->
    {
        dirty_columns.mark();
        toolkit.scheduleUpdate(this);
    };

    @Override
    public StringTable createJFXNode() throws Exception
    {
        // In edit mode, table is passive.
        final StringTable table = new StringTable(! toolkit.isEditMode());
        if (toolkit.isEditMode())
        {   // Capture clicks and use to select widget in editor,
            // instead of interacting with the table
            table.addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
            {
                event.consume();
                toolkit.fireClick(model_widget, event.isControlDown());
            });
        }
        return table;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayToolbar().addUntypedPropertyListener(this::styleChanged);

        columnsChanged(model_widget.displayColumns(), null, model_widget.displayColumns().getValue());
        model_widget.displayColumns().addPropertyListener(this::columnsChanged);

        model_widget.runtimeValue().addPropertyListener(this::valueChanged);
    }

    /** Location, toolbar changed */
    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    /** Columns were added or removed */
    private void columnsChanged(final WidgetProperty<List<ColumnProperty>> property,
                                final List<ColumnProperty> removed, final List<ColumnProperty> added)
    {
        // Remove columns
        if (removed != null)
            for (ColumnProperty column : removed)
                ignoreColumnChanges(column);

        // Add columns
        if (added != null)
            for (ColumnProperty column : added)
                trackColumnChanges(column);
        column_listener.propertyChanged(null, null, null);
    }

    /** @param column Column where changes need to be monitored */
    private void trackColumnChanges(final ColumnProperty column)
    {
        column.name().addUntypedPropertyListener(column_listener);
        column.width().addUntypedPropertyListener(column_listener);
        column.editable().addUntypedPropertyListener(column_listener);
    }

    /** @param column Column where changes should be ignored */
    private void ignoreColumnChanges(final ColumnProperty column)
    {
        column.name().removePropertyListener(column_listener);
        column.width().removePropertyListener(column_listener);
        column.editable().removePropertyListener(column_listener);
    }

    private void valueChanged(final WidgetProperty<Object> property, final Object old_value, final Object new_value)
    {
        System.out.println("Table rep. reveived value " + new_value);

        if (new_value instanceof List)
        {
            // TODO Check for List<List<String>>?
            data = (List)new_value;
        }
        else
            data = Arrays.asList(Arrays.asList(Objects.toString(new_value)));
        dirty_data.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_style.checkAndClear())
        {
            jfx_node.setPrefSize(model_widget.positionWidth().getValue(),
                                 model_widget.positionHeight().getValue());

//            Color color = JFXUtil.convert(model_widget.displayForegroundColor().getValue());
//            jfx_node.setTextFill(color);
            Color color = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
//            jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));

            jfx_node.showToolbar(model_widget.displayToolbar().getValue());
        }

        if (dirty_columns.checkAndClear())
        {
            final List<ColumnProperty> columns = model_widget.displayColumns().getValue();
            final List<String> headers = columns.stream().map(c -> c.name().getValue())
                                                         .collect(Collectors.toList());
            jfx_node.setHeaders(headers);
            for (int col=0; col<columns.size(); ++col)
                jfx_node.setColumnEditable(col, columns.get(col).editable().getValue());
        }
        if (dirty_data.checkAndClear())
            jfx_node.setData(data);
    }
}
