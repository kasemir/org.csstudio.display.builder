/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.properties.ScriptPV;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

/** Dialog for editing {@link ScriptInfo}s
 *  @author Kay Kasemir
 */
public class ScriptsDialog extends Dialog<List<ScriptInfo>>
{
    /** Modifiable ScriptInfo */
    private static class ScriptItem
    {
        public String file, text;
        public List<ScriptPV> pvs;

        public ScriptItem()
        {
            file = "";
            text = "";
            pvs = new ArrayList<>();
        }

        public ScriptItem(final ScriptInfo info)
        {
            file = info.getFile();
            text = info.getText();
            pvs = new ArrayList<>(info.getPVs());
        }

        public ScriptInfo getScriptInfo()
        {
            return new ScriptInfo(file, text, pvs);
        }
    };

    /** Data that is linked to the table */
    private final ObservableList<ScriptItem> data = FXCollections.observableArrayList();

    public ScriptsDialog(final List<ScriptInfo> scripts)
    {
        setTitle("Scripts");
        setHeaderText("Edit scripts and their PVs");

        getDialogPane().setContent(createContent(scripts));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        setResultConverter(button ->
        {
            if (button != ButtonType.OK)
                return null;

            final List<ScriptInfo> result = new ArrayList<>();
            for (ScriptItem item : data)
            {
                if (!item.file.isEmpty())
                    result.add(item.getScriptInfo());
            }
            return result;
        });
    }

    private GridPane createContent(final List<ScriptInfo> scripts)
    {
        scripts.forEach(script -> data.add(new ScriptItem(script)));
        fixup(0);

        final GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);
        content.setGridLinesVisible(true); // TODO For debugging

        // Create table with editable column
        final TableColumn<ScriptItem, String> name_col = new TableColumn<>("Script");
        name_col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ScriptItem,String>, ObservableValue<String>>()
        {
            @Override
            public ObservableValue<String> call(final CellDataFeatures<ScriptItem, String> param)
            {
                final String name = param.getValue().file;
                if (name.isEmpty())
                    return new ReadOnlyStringWrapper("<enter name>");
                return new ReadOnlyStringWrapper(name);
            }
        });
        name_col.setCellFactory(TextFieldTableCell.<ScriptItem>forTableColumn());
        name_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            data.get(row).file = event.getNewValue();
            fixup(row);
        });

        final TableView<ScriptItem> table = new TableView<>(data);
        table.getColumns().add(name_col);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setTooltip(new Tooltip("Edit scripts. Add new script in last row"));

        content.add(table, 0, 0, 1, 3);
        GridPane.setHgrow(table, Priority.ALWAYS);
        GridPane.setVgrow(table, Priority.ALWAYS);

        // Buttons
        final Button add = new Button(Messages.Add, JFXUtil.getIcon("add.png"));
        add.setMaxWidth(Double.MAX_VALUE);
        content.add(add, 1, 0);
        add.setOnAction(event ->
        {
            data.add(new ScriptItem());
        });

        final Button remove = new Button(Messages.Remove, JFXUtil.getIcon("delete.png"));
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


        return content;
    }

    /** Fix table: Delete empty rows in middle, but keep one empty final row
     *  @param changed_row Row to check, and remove if it's empty
     */
    private void fixup(final int changed_row)
    {
        // Check if edited row is now empty and should be deleted
        if (changed_row < data.size())
        {
            final ScriptItem item = data.get(changed_row);
            if (item.file.trim().isEmpty())
                data.remove(changed_row);
        }
        // Assert one empty row at bottom
        final int len  = data.size();
        if (len <= 0  ||
            data.get(len-1).file.trim().length() > 0)
            data.add(new ScriptItem());
    }
}
