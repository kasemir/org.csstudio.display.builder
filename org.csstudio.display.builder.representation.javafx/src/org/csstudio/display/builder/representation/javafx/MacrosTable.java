/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.csstudio.display.builder.model.macros.Macros;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

/** JFX Table for editing {@link Macros}
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MacrosTable
{
    /** Java FX type observable property for a macro (name, value) pair */
    public static class MacroItem
    {
        private StringProperty name, value;

        public MacroItem(final String name, final String value)
        {
            this.name = new SimpleStringProperty(name);
            this.value = new SimpleStringProperty(value);
        }

        public StringProperty nameProperty()     { return name;                  }
        public void setName(final String name)   { nameProperty().set(name);     }
        public String getName()                  { return nameProperty().get();  }

        public StringProperty valueProperty()    { return value;                 }
        public void setValue(final String value) { valueProperty().set(value);   }
        public String getValue()                 { return valueProperty().get(); }
    };

    /** Top-level UI node */
    private final GridPane content = new GridPane();

    /** Data that is linked to the table */
    private final ObservableList<MacroItem> data = FXCollections.observableArrayList();

    private List<InvalidationListener> listeners = new CopyOnWriteArrayList<>();


    /** Create dialog
     *  @param initial_macros Initial {@link Macros}
     */
    public MacrosTable(final Macros initial_macros)
    {
        // Layout:
        //
        // | table |  [Add]
        // | table |  [Remove]
        // | table |
        // | table |
        content.setHgap(10);
        content.setVgap(10);
//        content.setBackground(new Background(new BackgroundFill(Color.PALEGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
//        content.setGridLinesVisible(true); // For debugging

        // Create table with editable columns
        final TableColumn<MacroItem, String> name_col = new TableColumn<>(Messages.MacrosDialog_NameCol);
        name_col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MacroItem,String>, ObservableValue<String>>()
        {
            @Override
            public ObservableValue<String> call(final CellDataFeatures<MacroItem, String> param)
            {
                final String name = param.getValue().getName();
                if (name.isEmpty())
                    return new ReadOnlyStringWrapper(Messages.MacrosTable_NameHint);
                return new ReadOnlyStringWrapper(name);
            }
        });
        name_col.setCellFactory(TextFieldTableCell.<MacroItem>forTableColumn());
        name_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            data.get(row).setName(event.getNewValue());
            fixup(row);
        });

        final TableColumn<MacroItem, String> value_col = new TableColumn<>(Messages.MacrosDialog_ValueCol);
        value_col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MacroItem,String>, ObservableValue<String>>()
        {
            @Override
            public ObservableValue<String> call(final CellDataFeatures<MacroItem, String> param)
            {
                final String name = param.getValue().getValue();
                if (name.isEmpty())
                    return new ReadOnlyStringWrapper(Messages.MacrosTable_ValueHint);
                return new ReadOnlyStringWrapper(name);
            }
        });
        value_col.setCellFactory(TextFieldTableCell.<MacroItem>forTableColumn());
        value_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            data.get(row).setValue(event.getNewValue());
            fixup(row);
        });

        final TableView<MacroItem> table = new TableView<>(data);
        table.getColumns().add(name_col);
        table.getColumns().add(value_col);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setTooltip(new Tooltip(Messages.MacrosTable_ToolTip));

        content.add(table, 0, 0, 1, 3);
        GridPane.setHgrow(table, Priority.ALWAYS);
        GridPane.setVgrow(table, Priority.ALWAYS);

        // Buttons
        final Button add = new Button(Messages.MacrosDialog_Add);
        add.setMaxWidth(Double.MAX_VALUE);
        content.add(add, 1, 0);
        add.setOnAction(event ->
        {
            data.add(new MacroItem("", ""));
        });

        final Button remove = new Button(Messages.MacrosDialog_Remove);
        remove.setMaxWidth(Double.MAX_VALUE);
        content.add(remove, 1, 1);
        remove.setOnAction(event ->
        {
            final int sel = table.getSelectionModel().getSelectedIndex();
            if (sel >= 0)
            {
                data.remove(sel);
                fixup(sel);
            }
        });

        // Without this filler in the bottom right corner,
        // the table's Vgrow setting stops taking effect after a certain hight
        // of the content pane?!
        final Label filler = new Label();
        content.add(filler, 1, 2);
        GridPane.setVgrow(filler, Priority.ALWAYS);

        setMacros(initial_macros);
    }

    /** Fix table: Delete empty rows in middle, but keep one empty final row
     *  @param changed_row Row to check, and remove if it's empty
     */
    private void fixup(final int changed_row)
    {
        // Check if edited row is now empty and should be deleted
        if (changed_row < data.size())
        {
            final MacroItem item = data.get(changed_row);
            final String name = item.getName().trim();
            final String value = item.getValue().trim();

            if (name.isEmpty()  &&  value.isEmpty())
                data.remove(changed_row);
        }
        // Assert one empty row at bottom
        final int len  = data.size();
        if (len <= 0  ||
            (data.get(len-1).getName().trim().length() > 0  &&
             data.get(len-1).getValue().trim().length() > 0) )
            data.add(new MacroItem("", ""));

        for (InvalidationListener listener : listeners)
            listener.invalidated(data);
    }

    /** @return Top-level UI node of this Java FX composite */
    public Node getNode()
    {
        return content;
    }

    /** @param listener Listener that will be invoked whenever anything
     *                  in the macros is edited.
     */
    public void addListener(final InvalidationListener listener)
    {
        listeners .add(listener);
    }

    /** @param macros {@link Macros} to show in table */
    public void setMacros(final Macros macros)
    {
        data.clear();
        for (String name : macros.getNames())
            data.add(new MacroItem(name, macros.getValue(name)));
        // Add empty final row
        data.add(new MacroItem("", ""));
    }

    /** @return {@link Macros} for data in table */
    public Macros getMacros()
    {
        final Macros macros = new Macros();
        for (MacroItem item : data)
        {
            final String name = item.getName().trim();
            final String value = item.getValue().trim();
            // Skip empty rows
            if (!name.isEmpty()  &&  !value.isEmpty())
                macros.add(name, value);
        }
        return macros;
    }
}
