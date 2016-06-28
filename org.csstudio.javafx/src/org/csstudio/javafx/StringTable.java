/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.javafx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.converter.DefaultStringConverter;

/** Table of strings
 *
 *  <p>Table that shows String data based on a list
 *  of headers and a String matrix (List of Lists).
 *
 *  <p>Data can be changed at runtime, columns will
 *  then be re-created.
 *
 *  <p>User can edit the cells.
 *
 *  <p>Toolbar and key shortcuts can be used to add/remove
 *  rows or columns:
 *  <ul>
 *  <li>t - Show/hide toolbar
 *  </ul>
 *
 *  <p>Class is implemented as {@link BorderPane}, but should
 *  be treated as a {@link Region}.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class StringTable extends BorderPane
{
    /** Value used for the last row
     *
     *  <p>This exact value is placed in the last row.
     *  It's not considered part of the data,
     *  but a marker that allows user to start a new row
     *  by entering values.
     *
     *  <p>Table data is compared as exact identity (== MAGIC_LAST_ROW).
     */
    private static final List<String> MAGIC_LAST_ROW = Arrays.asList("Click to add row");

    private Paint text_color = Color.BLACK;

    private Paint last_row_color = Color.GRAY;

    /** Table cell that displays a String,
     *  with special coloring of the MAGIC_LAST_ROW
     */
    private class StringTextCell extends TextFieldTableCell<List<String>, String>
    {
        public StringTextCell()
        {
            super(new DefaultStringConverter());
        }

        @Override
        public void updateItem(final String item, final boolean empty)
        {
            super.updateItem(item, empty);
            if (empty)
                return;
            final List<List<String>> data = getTableView().getItems();
            final int row = getIndex();
            setTextFill(data.get(row) == MAGIC_LAST_ROW ? last_row_color : text_color);
        }
    }

    // TODO Toolbar to add/remove rows and columns
    private final Node toolbar = new Label("Toolbar");

    private final TableView<List<String>> table = new TableView<>();

    /** Currently editing a cell? */
    private boolean editing = false;

    /** Constructor */
    public StringTable()
    {
        super();
        setTop(toolbar);
        setCenter(table);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Empty"));
        table.setEditable(true);
        table.setOnKeyPressed(this::handleKey);
    }

    /** @return <code>true</code> if toolbar is visible */
    public boolean isToolbarVisible()
    {
        return getTop() != null;
    }

    /** @param show <code>true</code> if toolbar should be displayed */
    public void showToolbar(final boolean show)
    {
        if (isToolbarVisible() == show)
            return;
        if (show)
            setTop(toolbar);
        else
            setTop(null);

        // Force layout to reclaim space used by hidden toolbar,
        // or make room for the visible toolbar
        layoutChildren();
        // XX Hack: Toolbar is garbled, all icons in pile at left end,
        // when shown the first time, i.e. it was hidden when the plot
        // was first shown.
        // Manual fix is to hide and show again.
        // Workaround is to force another layout a little later
        if (show)
            ForkJoinPool.commonPool().submit(() ->
            {
                Thread.sleep(1000);
                Platform.runLater(() -> layoutChildren() );
                return null;
            });
    }

    /** Set or update headers, i.e. define the columns
     *  @param headers Header labels
     */
    public void setHeaders(final List<String> headers)
    {
        table.getColumns().clear();

        int column = 0;
        for (String header : headers)
        {
            final int col_index = column++;
            final TableColumn<List<String>, String> table_column = new TableColumn<>(header);
            table_column.setCellValueFactory(param ->
            {
                List<String> value = param.getValue();
                final String text;
                if (value == MAGIC_LAST_ROW)
                    text = col_index == 0 ? MAGIC_LAST_ROW.get(0): "";
                else
                    text = value.get(col_index);
                return new SimpleStringProperty(text);
            });
            table_column.setCellFactory(list -> new StringTextCell());
            table_column.setOnEditStart(event -> editing = true);
            table_column.setOnEditCommit(event ->
            {
                editing = false;
                final int col = event.getTablePosition().getColumn();
                List<String> row = event.getRowValue();
                if (row == MAGIC_LAST_ROW)
                {
                    // Entered in last row? Create new row
                    final int size = table.getColumns().size();
                    row = new ArrayList<>(size);
                    for (int i=0; i<size; ++i)
                        row.add("");
                    final List<List<String>> data = table.getItems();
                    data.add(data.size()-1, row);
                }
                row.set(col, event.getNewValue());
            });
            table_column.setOnEditCancel(event -> editing = false);
            table.getColumns().add(table_column);
        }
    }

    /** @return Header labels */
    public List<String> getHeaders()
    {
        return table.getColumns().stream().map(col -> col.getText()).collect(Collectors.toList());
    }

    /** Set or update data
     *
     *  @param data Rows of data,
     *              where each row must contain the same number
     *              of elements as the column headers
     */
    public void setData(final List<List<String>> data)
    {
        final ObservableList<List<String>> list = FXCollections.observableArrayList(data);
        list.add(MAGIC_LAST_ROW);
        table.setItems(list);
    }

    /** @return List of rows, where each row contains the list of cell strings */
    public List<List<String>> getData()
    {
        final List<List<String>> data = new ArrayList<>(table.getItems());
        while (data.size() > 0  &&  data.get(data.size()-1) == MAGIC_LAST_ROW)
            data.remove(data.size()-1);
        return data;
    }

    /** Handle key pressed on the table
     *
     *  <p>Ignores keystrokes while editing a cell.
     *
     *  @param event Key pressed
     */
    private void handleKey(final KeyEvent event)
    {
        if (editing)
            return;
        switch (event.getCode())
        {
        case T:
            showToolbar(! isToolbarVisible());
            break;
        default:
        }
    }
}

