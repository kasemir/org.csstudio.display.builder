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

    /** Is user setting data, which in turn updates the widget value
     *  --> Ignore widget value change
     */
    private boolean setting_data = false;

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
        return new StringTable(editable);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        if (toolkit.isEditMode())
        {   // Capture clicks and use to select widget in editor,
            // instead of interacting with the table
            jfx_node.addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
            {
                event.consume();
                toolkit.fireClick(model_widget, event.isControlDown());
            });
        }
        else
        {
            jfx_node.setListener(new StringTableListener()
            {
                @Override
                public void dataChanged(final StringTable table)
                {
                    setting_data = true;
                    try
                    {
                        // TODO Update headers, BUT:
                        // This clears the table value
                        // AND looses column options
                        // model_widget.setHeaders(table.getHeaders());
                        model_widget.setValue(table.getData());
                    }
                    finally
                    {
                        setting_data = false;
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
        model_widget.runtimeSelection().setValue(selection);
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
        if (setting_data)
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
