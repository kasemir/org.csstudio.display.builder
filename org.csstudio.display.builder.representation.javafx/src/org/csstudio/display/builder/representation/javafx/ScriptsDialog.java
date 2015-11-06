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
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.properties.ScriptPV;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

/** Dialog for editing {@link ScriptInfo}s
 *  @author Kay Kasemir
 */
public class ScriptsDialog extends Dialog<List<ScriptInfo>>
{
    /** Modifiable ScriptPV */
    private static class PVItem
    {
        public String name;
        public boolean trigger;

        public PVItem(final String name, final boolean trigger)
        {
            this.name = name;
            this.trigger = trigger;
        }

        public static PVItem forPV(final ScriptPV info)
        {
            return new PVItem(info.getName(), info.isTrigger());
        }

        public ScriptPV toScriptPV()
        {
            return new ScriptPV(name, trigger);
        }
    };

    /** Modifiable ScriptInfo */
    private static class ScriptItem
    {
        public String file, text;
        public List<PVItem> pvs;

        public ScriptItem()
        {
            this("", "", new ArrayList<>());
        }

        public ScriptItem(final String file, final String text, final List<PVItem> pvs)
        {
            this.file = file;
            this.text = text;
            this.pvs = pvs;
        }

        public static ScriptItem forInfo(final ScriptInfo info)
        {
            final List<PVItem> pvs = new ArrayList<>();
            info.getPVs().forEach(pv -> pvs.add(PVItem.forPV(pv)));
            return new ScriptItem(info.getFile(), info.getText(), pvs);
        }

        public ScriptInfo getScriptInfo()
        {
            final List<ScriptPV> spvs = new ArrayList<>();
            pvs.forEach(pv -> spvs.add(pv.toScriptPV()));
            return new ScriptInfo(file, text, spvs);
        }
    };

    /** Data that is linked to the table */
    private final ObservableList<ScriptItem> script_items = FXCollections.observableArrayList();
    private TableView<ScriptItem> scripts_table;

    /** @param scripts Scripts to show/edit in the dialog */
    public ScriptsDialog(final List<ScriptInfo> scripts)
    {
        setTitle("Scripts");
        setHeaderText("Edit scripts and their PVs");

        scripts.forEach(script -> script_items.add(ScriptItem.forInfo(script)));
        fixupScripts(0);

        getDialogPane().setContent(createContent());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        setResultConverter(button ->
        {
            if (button != ButtonType.OK)
                return null;

            final List<ScriptInfo> result = new ArrayList<>();
            for (ScriptItem item : script_items)
            {
                if (!item.file.isEmpty())
                    result.add(item.getScriptInfo());
            }
            return result;
        });
    }

    private Node createContent()
    {
        final Node scripts = createScriptsTable();

        // TODO Table/add/remove for PVs of current script

        scripts_table.getSelectionModel().selectedItemProperty().addListener((prop, old, selected) ->
        {
            // TODO Show in "pvs" table
            if (selected == null)
                System.out.println("Nothing selected");
            else
                System.out.println("PVs: " + selected.pvs.stream().map(pv -> pv.name).collect(Collectors.joining(", ")));
        });


        return new HBox(scripts);
    }

    /** @return Node for UI elements that edit the scripts */
    private Node createScriptsTable()
    {
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
            script_items.get(row).file = event.getNewValue();
            fixupScripts(row);
        });

        // TODO Table column to select file or set to ScriptInfo.EMBEDDED_PYTHON

        scripts_table = new TableView<>(script_items);
        scripts_table.getColumns().add(name_col);
        scripts_table.setEditable(true);
        scripts_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scripts_table.setTooltip(new Tooltip("Edit scripts. Add new script in last row"));

        content.add(scripts_table, 0, 0, 1, 3);
        GridPane.setHgrow(scripts_table, Priority.ALWAYS);
        GridPane.setVgrow(scripts_table, Priority.ALWAYS);

        // Buttons
        final Button add = new Button(Messages.Add, JFXUtil.getIcon("add.png"));
        add.setMaxWidth(Double.MAX_VALUE);
        content.add(add, 1, 0);
        add.setOnAction(event ->
        {
            script_items.add(new ScriptItem());
        });

        final Button remove = new Button(Messages.Remove, JFXUtil.getIcon("delete.png"));
        remove.setMaxWidth(Double.MAX_VALUE);
        content.add(remove, 1, 1);
        remove.setOnAction(event ->
        {
            final int sel = scripts_table.getSelectionModel().getSelectedIndex();
            if (sel >= 0)
            {
                script_items.remove(sel);
                fixupScripts(sel);
            }
        });

        return content;
    }

    /** Fix scripts data: Delete empty rows in middle, but keep one empty final row
     *  @param changed_row Row to check, and remove if it's empty
     */
    private void fixupScripts(final int changed_row)
    {
        // Check if edited row is now empty and should be deleted
        if (changed_row < script_items.size())
        {
            final ScriptItem item = script_items.get(changed_row);
            if (item.file.trim().isEmpty())
                script_items.remove(changed_row);
        }
        // Assert one empty row at bottom
        final int len  = script_items.size();
        if (len <= 0  ||
            script_items.get(len-1).file.trim().length() > 0)
            script_items.add(new ScriptItem());
    }

}
