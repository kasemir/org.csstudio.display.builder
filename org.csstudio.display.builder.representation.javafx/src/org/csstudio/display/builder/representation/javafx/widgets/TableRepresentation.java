/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.TableWidget;
import org.csstudio.display.builder.model.widgets.TableWidget.ColumnProperty;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.StringTable;
import org.csstudio.javafx.StringTableListener;
import org.diirt.vtype.VTable;

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

    /** Is user is changing the table, which in turn updates the widget value
     *  --> Ignore widget value change
     */
    private volatile boolean updating_table = false;

    /** Listener for any changes in any column
     *
     *  Triggers update of headers and column configuration
     */
    private final UntypedWidgetPropertyListener column_listener = (WidgetProperty<?> property, Object old_value, Object new_value) ->
    {
        if (updating_table)
            return;
        final List<String> new_headers = new ArrayList<>();
        for (ColumnProperty column : model_widget.propColumns().getValue())
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
        final boolean editable = ! toolkit.isEditMode()  &&  model_widget.propEditable().getValue();
        return new StringTable(editable);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        if (! toolkit.isEditMode())
        {
            jfx_node.setListener(new StringTableListener()
            {
                @Override
                public void tableChanged(final StringTable table)
                {
                    updating_table = true;
                    try
                    {
                        final List<String> new_headers = headers = table.getHeaders();
                        model_widget.setHeaders(new_headers);
                        // Updating the headers clears their options as well as the data.
                        // Restore column options.
                        for (int column=0; column<new_headers.size(); ++column)
                            model_widget.setColumnOptions(column, table.getColumnOptions(column));
                        model_widget.setValue(table.getData());
                    }
                    finally
                    {
                        updating_table = false;
                    }
                }

                @Override
                public void dataChanged(final StringTable table)
                {
                    updating_table = true;
                    try
                    {
                        model_widget.setValue(table.getData());
                    }
                    finally
                    {
                        updating_table = false;
                    }
                }

                @Override
                public void selectionChanged(final StringTable table, final int[] rows, final int[] cols)
                {
                    updateSelection(rows, cols);
                }
            });
        }

        UntypedWidgetPropertyListener listener = this::styleChanged;
        model_widget.propWidth().addUntypedPropertyListener(listener);
        model_widget.propHeight().addUntypedPropertyListener(listener);
        model_widget.propBackgroundColor().addUntypedPropertyListener(listener);
        model_widget.propForegroundColor().addUntypedPropertyListener(listener);
        model_widget.propFont().addUntypedPropertyListener(listener);
        model_widget.propToolbar().addUntypedPropertyListener(listener);

        columnsChanged(model_widget.propColumns(), null, model_widget.propColumns().getValue());
        model_widget.propColumns().addPropertyListener(this::columnsChanged);

        model_widget.runtimeValue().addPropertyListener(this::valueChanged);
    }

    private void updateSelection(final int[] rows, final int[] cols)
    {
        // Create VTable that holds the selection
        final List<String> headers = jfx_node.getHeaders();
        final int num_cols = headers.size();
        final List<List<String>> columns = new ArrayList<>(num_cols);
        for (int c=0; c<num_cols; ++c)
        {
            final List<String> column = new ArrayList<>(rows.length);
            for (int r : rows)
                column.add(jfx_node.getCell(r, c));
            columns.add(column);
        }

        final VTable selection = new SelectionVTable(headers, rows, cols, columns);
        model_widget.runtimePropSelection().setValue(selection);
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
        column.options().addUntypedPropertyListener(column_listener);
    }

    /** @param column Column where changes should be ignored */
    private void ignoreColumnChanges(final ColumnProperty column)
    {
        column.name().removePropertyListener(column_listener);
        column.width().removePropertyListener(column_listener);
        column.editable().removePropertyListener(column_listener);
        column.options().removePropertyListener(column_listener);
    }

    private void valueChanged(final WidgetProperty<Object> property, final Object old_value, final Object new_value)
    {
        if (updating_table)
            return;
        data = model_widget.getValue();
        if (new_value instanceof VTable)
        {   // Use table's column headers
            final VTable table = (VTable) new_value;
            final int cols = table.getColumnCount();
            final List<String> new_headers = new ArrayList<>(cols);
            for (int c=0; c<cols; ++c)
                new_headers.add(table.getColumnName(c));
            if (! new_headers.equals(headers))
            {
                headers = new_headers;
                dirty_columns.mark();
            }
        }
        dirty_data.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_style.checkAndClear())
        {
            jfx_node.setPrefSize(model_widget.propWidth().getValue(),
                                 model_widget.propHeight().getValue());

            jfx_node.setBackgroundColor(JFXUtil.convert(model_widget.propBackgroundColor().getValue()));
            jfx_node.setTextColor(JFXUtil.convert(model_widget.propForegroundColor().getValue()));
            jfx_node.setFont(JFXUtil.convert(model_widget.propFont().getValue()));
            jfx_node.showToolbar(model_widget.propToolbar().getValue());
        }

        if (dirty_columns.checkAndClear())
        {
            jfx_node.setHeaders(headers);
            final List<ColumnProperty> columns = model_widget.propColumns().getValue();
            final int num = Math.min(headers.size(), columns.size());
            for (int col=0; col<num; ++col)
            {
                final ColumnProperty column = columns.get(col);
                jfx_node.setColumnWidth(col, column.width().getValue());
                jfx_node.setColumnEditable(col, column.editable().getValue());

                final List<WidgetProperty<String>> options_value = column.options().getValue();
                if (options_value.isEmpty())
                    jfx_node.setColumnOptions(col, Collections.emptyList());
                else
                {
                    final List<String> options = new ArrayList<>();
                    for (WidgetProperty<String> option : options_value)
                        options.add(option.getValue());
                    jfx_node.setColumnOptions(col, options);
                }
            }
        }
        if (dirty_data.checkAndClear())
            jfx_node.setData(data);
    }
}
