/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.util.ModelThreadPool;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/** Dialog for editing {@link Macros}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class MacrosDialog extends Dialog<Macros>
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

    /** Data that is linked to the table */
    private final ObservableList<MacroItem> data = FXCollections.observableArrayList();

    /** Table UI */
    private final TableView<MacroItem> table = new TableView<>();

    /** Create dialog
     *  @param initial_macros Initial {@link Macros}
     */
    public MacrosDialog(final Macros initial_macros)
    {
        setTitle(Messages.MacrosDialog_Title);
        setHeaderText(Messages.MacrosDialog_Info);

        // Layout:
        //
        // | table |  [Add]
        // | table |  [Remove]
        // | table |
        // | table |

        final GridPane content = new GridPane();
        // content.setGridLinesVisible(true); // For debugging
        content.setHgap(10);
        content.setVgap(10);
        content.setPadding(new Insets(10));

        // Create table with editable columns
        final TableColumn<MacroItem, String> name_col = new TableColumn<>(Messages.MacrosDialog_NameCol);
        name_col.setCellValueFactory(new PropertyValueFactory<MacroItem, String>("Name"));
        name_col.setCellFactory(TextFieldTableCell.<MacroItem>forTableColumn());
        name_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            data.get(row).setName(event.getNewValue());
        });
        table.getColumns().add(name_col);

        final TableColumn<MacroItem, String> value_col = new TableColumn<>(Messages.MacrosDialog_ValueCol);
        value_col.setCellValueFactory(new PropertyValueFactory<MacroItem, String>("Value"));
        value_col.setCellFactory(TextFieldTableCell.<MacroItem>forTableColumn());
        value_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            data.get(row).setValue(event.getNewValue());
        });
        table.getColumns().add(value_col);

        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label(Messages.MacrosDialog_Empty));

        content.add(table, 0, 0, 1, 2);
        GridPane.setHgrow(table, Priority.ALWAYS);
        GridPane.setVgrow(table, Priority.ALWAYS);

        // Buttons
        // TODO nicer would be an "empty last row" to add new macros
        final Button add = new Button(Messages.MacrosDialog_Add);
        add.setMaxWidth(Double.MAX_VALUE);
        content.add(add, 1, 0);
        add.setOnAction(event ->
        {
            data.add(new MacroItem("NAME", "VALUE"));
        });

        // TODO nicer would be simply setting a row to "empty" to delete
        final Button remove = new Button(Messages.MacrosDialog_Remove);
        remove.setMaxWidth(Double.MAX_VALUE);
        content.add(remove, 1, 1);
        GridPane.setValignment(remove, VPos.TOP);
        remove.setOnAction(event ->
        {
            final int sel = table.getSelectionModel().getSelectedIndex();
            if (sel >= 0)
                data.remove(sel);
        });

        // Populate table data off UI thread
        ModelThreadPool.getExecutor().execute(() ->
        {
            for (String name : initial_macros.getNames())
                data.add(new MacroItem(name, initial_macros.getValue(name)));
            table.setItems(data);
        });

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        setResultConverter(button ->
        {
            if (button == ButtonType.OK)
                return getCurrentMacros();
            return null;
        });
    }

    /** @return {@link Macros} for data in table */
    private Macros getCurrentMacros()
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
