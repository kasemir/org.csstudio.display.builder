/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.TableWidget;
import org.csstudio.display.builder.model.widgets.TableWidget.ColumnProperty;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.StringTable;
import org.diirt.util.array.ListDouble;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VTable;

import javafx.scene.input.MouseEvent;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class TableRepresentation extends RegionBaseRepresentation<StringTable, TableWidget>
{
    /** Position, toolbar changed */
    private final DirtyFlag dirty_style = new DirtyFlag();

    /** Columns changed */
    private final DirtyFlag dirty_columns = new DirtyFlag();

    /** Data changed */
    private final DirtyFlag dirty_data = new DirtyFlag();

    /** Most recent column headers */
    private volatile List<String> headers = Collections.emptyList();

    /** Most recent table data, row by row */
    private volatile List<List<String>> data = new ArrayList<>();

    /** Listener for any changes in any column
     *
     *  Triggers update of headers and column configuration
     */
    private final UntypedWidgetPropertyListener column_listener = (WidgetProperty<?> property, Object old_value, Object new_value) ->
    {
        final List<String> new_headers = new ArrayList<>();
        for (ColumnProperty column : model_widget.displayColumns().getValue())
            new_headers.add(column.name().getValue());
        headers = new_headers;
        dirty_columns.mark();
        toolkit.scheduleUpdate(this);
    };

    @Override
    public StringTable createJFXNode() throws Exception
    {
        // In edit mode, table is passive.
        // Change of overall 'editable' at runtime is not supported
        final boolean editable = ! toolkit.isEditMode()  &&  model_widget.behaviorEditable().getValue();
        final StringTable table = new StringTable(editable);
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

        UntypedWidgetPropertyListener listener = this::styleChanged;
        model_widget.positionWidth().addUntypedPropertyListener(listener);
        model_widget.positionHeight().addUntypedPropertyListener(listener);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(listener);
        model_widget.displayForegroundColor().addUntypedPropertyListener(listener);
        model_widget.displayFont().addUntypedPropertyListener(listener);
        model_widget.displayToolbar().addUntypedPropertyListener(listener);

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void valueChanged(final WidgetProperty<Object> property, final Object old_value, final Object new_value)
    {
        if (new_value instanceof List)
        {   // Check for List<List<String>>?
            data = (List)new_value;
        }
        else if (new_value instanceof VTable)
        {
            final VTable table = (VTable) new_value;
            final int rows = table.getRowCount();
            final int cols = table.getColumnCount();
            // Extract 2D string matrix for data
            final List<List<String>> new_data = new ArrayList<>(rows);
            for (int r=0; r<rows; ++r)
            {
                final List<String> row = new ArrayList<>(cols);
                for (int c=0; c<cols; ++c)
                {
                    final Object col_data = table.getColumnData(c);
                    if (col_data instanceof List)
                        row.add( Objects.toString(((List)col_data).get(r)) );
                    else if (col_data instanceof ListDouble)
                        row.add( Double.toString(((ListDouble)col_data).getDouble(r)) );
                    else if (col_data instanceof ListNumber)
                        row.add( Long.toString(((ListNumber)col_data).getLong(r)) );
                    else
                        row.add( Objects.toString(col_data) );
                }
                new_data.add(row);
            }

            // Use table's column headers
            final List<String> new_headers = new ArrayList<>(table.getColumnCount());
            for (int c=0; c<cols; ++c)
                new_headers.add(table.getColumnName(c));
            if (! new_headers.equals(headers))
            {
                headers = new_headers;
                dirty_columns.mark();
            }

            data = new_data;
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

            jfx_node.setBackgroundColor(JFXUtil.convert(model_widget.displayBackgroundColor().getValue()));
            jfx_node.setTextColor(JFXUtil.convert(model_widget.displayForegroundColor().getValue()));
            jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));
            jfx_node.showToolbar(model_widget.displayToolbar().getValue());
        }

        if (dirty_columns.checkAndClear())
        {
            jfx_node.setHeaders(headers);
            final List<ColumnProperty> columns = model_widget.displayColumns().getValue();
            final int num = Math.min(headers.size(), columns.size());
            for (int col=0; col<num; ++col)
                jfx_node.setColumnEditable(col, columns.get(col).editable().getValue());
        }
        if (dirty_data.checkAndClear())
            jfx_node.setData(data);
    }
}
