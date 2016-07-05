/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.javafx;

import static org.csstudio.javafx.Activator.logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

/** Table of strings
 *
 *  <p>Table that shows String data based on a list
 *  of headers and a String matrix (List of Lists).
 *
 *  <p>Data can be changed at runtime, columns will
 *  then be re-created.

 *  <p>User can edit the cells.
 *  While inefficient, the table creates a deep copy
 *  of the data submitted to it for display, so changes
 *  in the table will not affect the original data.
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
    private static final List<String> MAGIC_LAST_ROW = Arrays.asList(Messages.MagicLastRow);

    /** Cell factory for displaying the text
     *
     *  <p>special coloring of MAGIC_LAST_ROW which only has one column
     */
    private static final Callback<CellDataFeatures<List<String>, String>, ObservableValue<String>> CELL_FACTORY = param ->
    {
        final TableView<List<String>> table = param.getTableView();
        final int col_index = table.getColumns().indexOf(param.getTableColumn());
        List<String> value = param.getValue();
        final String text;
        if (value == MAGIC_LAST_ROW)
            text = col_index == 0 ? MAGIC_LAST_ROW.get(0): "";
        else
            text = value.get(col_index);
        return new SimpleStringProperty(text);
    };

    private final boolean editable;

    private Color background_color = Color.WHITE;

    private Color text_color = Color.BLACK;

    private Color last_row_color = text_color.deriveColor(0, 0, 0, 0.5);

    private Font font = Font.font(12);

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

    /** Cell with checkbox, sets data to "true"/"false" */
    private class BooleanCell extends TableCell<List<String>, String>
    {
        private final CheckBox checkBox = new CheckBox();

        public BooleanCell()
        {
            getStyleClass().add("check-box-table-cell");
        }

        @Override
        protected void updateItem(final String item, final boolean empty)
        {
            super.updateItem(item, empty);
            if (empty)
                setGraphic(null);
            else
            {
                setGraphic(checkBox);
                checkBox.setSelected(item.equalsIgnoreCase("true"));
                checkBox.setOnAction(event ->
                {
                    // TODO Auto-generated method stub
                    System.out.println("TODO: Check box was clicked in row " + getIndex() + " of " + getTableColumn().getText());
                });
            }
        }
    };

    /** Cell that allows selecting options from a combo */
    private class ComboCell extends ComboBoxTableCell<List<String>, String>
    {
        public ComboCell(final List<String> options)
        {
            super(FXCollections.observableArrayList(options));
            setComboBoxEditable(true);
        }
    };


    private final ToolBar toolbar = new ToolBar();

    /** Data shown in the table, includes MAGIC_LAST_ROW */
    private final ObservableList<List<String>> data = FXCollections.observableArrayList();

    private final TableView<List<String>> table = new TableView<>(data);

    /** Currently editing a cell? */
    private boolean editing = false;

    private volatile StringTableListener listener = null;

    /** Constructor
     *  @param editable Allow user interaction (toolbar, edit), or just display data?
     */
    public StringTable(final boolean editable)
    {
        this.editable = editable;
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().getSelectedIndices().addListener(this::selectionChanged);
        table.setPlaceholder(new Label());

        if (editable)
        {
            table.setEditable(true);
            // Check for keys in both toolbar and table
            setOnKeyPressed(this::handleKey);
        }
        updateStyle();
        fillToolbar();
        setTop(toolbar);
        setCenter(table);

        setData(Arrays.asList(Arrays.asList()));
    }

    /** @param listener Listener to notify of changes */
    public void setListener(final StringTableListener listener)
    {
        this.listener = listener;
    }

    private void fillToolbar()
    {
        toolbar.getItems().add(createToolbarButton("add_row", Messages.AddRow, event -> addRow()));
        toolbar.getItems().add(createToolbarButton("remove_row", Messages.RemoveRow, event -> deleteRow()));
        toolbar.getItems().add(createToolbarButton("rename_col", Messages.RenameColumn, event -> renameColumn()));
        toolbar.getItems().add(createToolbarButton("add_col", Messages.AddColumn, event -> addColumn()));
        toolbar.getItems().add(createToolbarButton("remove_col", Messages.RemoveColumn, event -> deleteColumn()));
    }

    private Button createToolbarButton(final String id, final String tool_tip, final EventHandler<ActionEvent> handler)
    {
        final Button button = new Button();
        try
        {
            button.setGraphic(new ImageView(Activator.getIcon(id)));
            button.setTooltip(new Tooltip(tool_tip));
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot load icon for " + id, ex);
            button.setText(tool_tip);
        }
        button.setOnAction(handler);
        return button;
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

    /** @param color Background color */
    public void setBackgroundColor(final Color color)
    {
        background_color = color;
        updateStyle();
    }

    /** @param color Text color */
    public void setTextColor(final Color color)
    {
        text_color = color;
        last_row_color = color.deriveColor(0, 0, 0, 0.5);
        updateStyle();
    }

    /** @param font Font */
    public void setFont(final Font font)
    {
        this.font = font;
        updateStyle();
    }

    /** Update style for colors and font */
    private void updateStyle()
    {
        table.setStyle("-fx-base: " + JFXUtil.webRGB(background_color) + "; " +
                       "-fx-text-background-color: " + JFXUtil.webRGB(text_color) + "; " +
                       "-fx-font-family: \"" + font.getFamily() + "\"; " +
                       "-fx-font-size: " + font.getSize()/12 + "em");
    }

    /** Set or update headers, i.e. define the columns
     *  @param headers Header labels
     */
    public void setHeaders(final List<String> headers)
    {
        table.getColumns().clear();
        data.clear();
        if (editable)
            data.add(MAGIC_LAST_ROW);

        for (String header : headers)
        {
            final TableColumn<List<String>, String> table_column =
                createTableColumn(header);
            table.getColumns().add(table_column);
        }
    }

    /** Set (minimum) column width
     *
     *  @param column Column index, 0 .. <code>getHeaders().size()-1</code>
     *  @param width Width
     */
    public void setColumnWidth(final int column, final int width)
    {
        table.getColumns().get(column).setMinWidth(width);
    }

    /** Allow editing a column
     *
     *  <p>By default, all columns of an 'active' table
     *  are editable, but this method can change it.
     *
     *  @param column Column index, 0 .. <code>getHeaders().size()-1</code>
     *  @param editable
     */
    public void setColumnEditable(final int column, final boolean editable)
    {
        table.getColumns().get(column).setEditable(editable);
    }

    /** Configure column options.
     *
     *  <p>If the list of options is empty,
     *  the cells in the column will offer a generic text field
     *  for entering values.
     *
     *  <p>If there are options, the column will use a drop-down
     *  list (combo box) for selecting one of the options.
     *
     *  @param column Column index, 0 .. <code>getHeaders().size()-1</code>
     *  @param options
     */
    public void setColumnOptions(final int column, final List<String> options)
    {
        @SuppressWarnings("unchecked")
        final TableColumn<List<String>, String> table_column = (TableColumn<List<String>, String>) table.getColumns().get(column);
        final Callback<TableColumn<List<String>, String>, TableCell<List<String>, String>> factory;

        if (options == null || options.isEmpty())
            factory = list -> new StringTextCell();
        else if (options.equals(Arrays.asList("false", "true")))
            // XXX Use checkbox if there are only two options True/False or Yes/No?
            factory = list -> new BooleanCell();
        else
            factory = list -> new ComboCell(options);
        table_column.setCellFactory(factory);
    }

    private TableColumn<List<String>, String> createTableColumn(final String header)
    {
        final TableColumn<List<String>, String> table_column = new TableColumn<>(header);
        table_column.setCellValueFactory(CELL_FACTORY);

        // By default, use text field editor. setColumnOptions() can replace
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
                row = createEmptyRow();
                final List<List<String>> data = table.getItems();
                data.add(data.size()-1, row);
            }
            row.set(col, event.getNewValue());
            fireDataChanged();
        });
        table_column.setOnEditCancel(event -> editing = false);
        table_column.setSortable(false);

        return table_column;
    }

    /** @return Header labels */
    public List<String> getHeaders()
    {
        return table.getColumns().stream().map(col -> col.getText()).collect(Collectors.toList());
    }

    private List<String> createEmptyRow()
    {
        final int size = getColumnCount();
        final List<String> row = new ArrayList<>(size);
        for (int i=0; i<size; ++i)
            row.add("");
        return row;
    }

    private int getColumnCount()
    {
        return table.getColumns().size();
    }

    /** Set or update data
     *
     *  @param new_data Rows of data,
     *                  where each row must contain the same number
     *                  of elements as the column headers
     */
    public void setData(final List<List<String>> new_data)
    {
        final int columns = getColumnCount();
        data.clear();
        for (List<String> new_row : new_data)
        {
            final ArrayList<String> row;
            if (new_row instanceof ArrayList)
                row = (ArrayList<String>)new_row;
            else
                row = new ArrayList<>(new_row);
            if (row.size() < columns)
            {
                logger.log(Level.WARNING, "Table needs " + columns +
                           " columns but got row with just " + row.size());
                for (int i=row.size(); i<columns; ++i)
                    row.add("");
            }
            data.add(row);
        }

        if (editable)
            data.add(MAGIC_LAST_ROW);
        // Don't fire, since external source changed data, not user
        // fireDataChanged();
    }

    /** Get complete table content
     *  @return List of rows, where each row contains the list of cell strings
     */
    public List<List<String>> getData()
    {
        final List<List<String>> data = new ArrayList<>(table.getItems());
        while (data.size() > 0  &&  data.get(data.size()-1) == MAGIC_LAST_ROW)
            data.remove(data.size()-1);
        return data;
    }

    /** Get data of one table cell
     *  @param row Table row
     *  @param col Table column
     *  @return Value of that cell or "" for invalid row, column
     */
    public String getCell(final int row, final int col)
    {
        try
        {
            final List<String> row_data = table.getItems().get(row);
            if (row_data == MAGIC_LAST_ROW)
                return "";
            return row_data.get(col);
        }
        catch (IndexOutOfBoundsException ex)
        {
            return "";
        }
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

    /** Add a row above the selected column,
     *  or on the very bottom if nothing selected
     */
    private void addRow()
    {
        int row = table.getSelectionModel().getSelectedIndex();
        final List<List<String>> data = table.getItems();
        final int len = data.size();
        if (row < 0  ||  row > len-1)
            row = len-1;
        data.add(row, createEmptyRow());
        fireDataChanged();
    }

    /** Delete currently selected row */
    private void deleteRow()
    {
        int row = table.getSelectionModel().getSelectedIndex();
        final List<List<String>> data = table.getItems();
        final int len = data.size();
        if (row < 0  ||  row >= len-1)
            return;
        data.remove(row);
        fireDataChanged();
    }

    /** Listener to table selection */
    private void selectionChanged(final Observable what)
    {
        final StringTableListener copy = listener;
        if (copy == null)
            return;

        @SuppressWarnings("rawtypes")
        final ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
        int num = cells.size();
        // Don't select the magic last row
        if (num > 0  &&  data.get(cells.get(num-1).getRow()) == MAGIC_LAST_ROW)
            --num;
        final int[] rows = new int[num], cols = new int[num];
        for (int i=0; i<num; ++i)
        {
            rows[i] = cells.get(i).getRow();
            cols[i] = cells.get(i).getColumn();
        }
        copy.selectionChanged(this, rows, cols);
    }

    /** @return Currently selected table column or -1 */
    private int getSelectedColumn()
    {
        @SuppressWarnings("rawtypes")
        final ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
        if (cells.isEmpty())
            return -1;
        return cells.get(0).getColumn();
    }

    /** Prompt for column name
     *  @param name Suggested name
     *  @return Name entered by user or <code>null</code>
     */
    private String getColumnName(final String name)
    {
        final TextInputDialog dialog = new TextInputDialog(name);
        // Position dialog near table
        final Bounds absolute = localToScreen(getBoundsInLocal());
        dialog.setX(absolute.getMinX() + 10);
        dialog.setY(absolute.getMinY() + 10);
        dialog.setTitle(Messages.RenameColumnTitle);
        dialog.setHeaderText(Messages.RenameColumnInfo);
        return dialog.showAndWait().orElse(null);
    }

    /** Renames the currently selected column */
    private void renameColumn()
    {
        final int column = getSelectedColumn();
        if (column < 0)
            return;
        final TableColumn<List<String>, ?> table_col = table.getColumns().get(column);
        final String name = getColumnName(table_col.getText());
        if (name == null)
            return;
        table_col.setText(name);
        fireDataChanged();
    }

    /** Add a column to the left of the selected column,
     *  or on the very right if nothing selected
     */
    private void addColumn()
    {
        int column = getSelectedColumn();
        final String name = getColumnName(Messages.DefaultNewColumnName);
        if (name == null)
            return;
        if (column < 0)
            column = table.getColumns().size();
        table.getColumns().add(column, createTableColumn(name));
        for (List<String> row : data)
            if (row != MAGIC_LAST_ROW)
                row.add(column, "");
        fireDataChanged();
    }

    /** Delete currently selected column */
    private void deleteColumn()
    {
        final int column = getSelectedColumn();
        if (column < 0)
            return;
        table.getColumns().remove(column);
        for (List<String> row : data)
            if (row != MAGIC_LAST_ROW)
                row.remove(column);
        fireDataChanged();
    }

    private void fireDataChanged()
    {
        final StringTableListener copy = listener;
        if (copy != null)
            copy.dataChanged(this);
    }
}

