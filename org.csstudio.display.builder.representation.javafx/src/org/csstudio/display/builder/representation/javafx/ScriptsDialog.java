/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.properties.ScriptPV;
import org.csstudio.javafx.MultiLineInputDialog;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import javafx.util.Callback;

/** Dialog for editing {@link ScriptInfo}s
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScriptsDialog extends Dialog<List<ScriptInfo>>
{
    /** ScriptPV info as property-based item for table */
    public static class PVItem
    {
        private final StringProperty name = new SimpleStringProperty();
        private final BooleanProperty trigger = new SimpleBooleanProperty(true);

        public PVItem(final String name, final boolean trigger)
        {
            this.name.set(name);
            this.trigger.set(trigger);
        }

        public static PVItem forPV(final ScriptPV info)
        {
            return new PVItem(info.getName(), info.isTrigger());
        }

        public ScriptPV toScriptPV()
        {
            return new ScriptPV(name.get(), trigger.get());
        }

        public StringProperty nameProperty()
        {
            return name;
        }

        public BooleanProperty triggerProperty()
        {
            return trigger;
        }
    };

    /** Modifiable ScriptInfo */
    public static class ScriptItem
    {
        public StringProperty file = new SimpleStringProperty();
        public String text;
        public List<PVItem> pvs;

        public ScriptItem()
        {
            this("", null, new ArrayList<>());
        }

        public ScriptItem(final String file, final String text, final List<PVItem> pvs)
        {
            this.file.set(file);
            this.text = text;
            this.pvs = pvs;
        }

        public static ScriptItem forInfo(final ScriptInfo info)
        {
            final List<PVItem> pvs = new ArrayList<>();
            info.getPVs().forEach(pv -> pvs.add(PVItem.forPV(pv)));
            return new ScriptItem(info.getPath(), info.getText(), pvs);
        }

        public ScriptInfo getScriptInfo()
        {
            final List<ScriptPV> spvs = new ArrayList<>();
            pvs.forEach(pv -> spvs.add(pv.toScriptPV()));
            return new ScriptInfo(file.get(), text, spvs);
        }

        public StringProperty fileProperty()
        {
            return file;
        }
    };

    /** Table cell with buttons to select file or edit the "embedded" script */
    private static class ScriptButtonCell extends TableCell<ScriptItem, Boolean>
    {
        private final Button btn_file = new Button(Messages.ScriptsDialog_BtnFile, JFXUtil.getIcon("open_file.png"));
        private final Button btn_embed = new Button(Messages.ScriptsDialog_BtnEmbed, JFXUtil.getIcon("embedded_script.png"));

        private final HBox buttons = new HBox(10, btn_file, btn_embed);

        public static Callback<TableColumn<ScriptItem, Boolean>, TableCell<ScriptItem, Boolean>> forTableColumn()
        {
            return col -> new ScriptButtonCell(col);
        };

        public ScriptButtonCell(TableColumn<ScriptItem, Boolean> col)
        {
            btn_file.setMinWidth(USE_PREF_SIZE);
            btn_embed.setMinWidth(USE_PREF_SIZE);

            btn_file.setOnAction(event ->
            {
                final ScriptItem item = getScriptItem();

                final FileChooser dlg = new FileChooser();
                dlg.setTitle(Messages.ScriptsDialog_FileBrowser_Title);
                if (item.file.get().length() > 0)
                {
                    File file = new File(item.file.get());
                    dlg.setInitialDirectory(file.getParentFile());
                    dlg.setInitialFileName(file.getName());
                }
                dlg.getExtensionFilters().addAll(new ExtensionFilter(Messages.ScriptsDialog_FileType_Script, "*.py"),
                                                 new ExtensionFilter(Messages.ScriptsDialog_FileType_All, "*.*"));
                final Window window = col.getTableView().getScene().getWindow();
                final File result = dlg.showOpenDialog(window);
                if (result != null)
                {
                    item.file.set(result.getPath());
                    item.text = null;
                }
            });
            btn_embed.setOnAction(event ->
            {
                final ScriptItem item = getScriptItem();
                if (item.text == null  ||  item.text.trim().isEmpty())
                    item.text = Messages.ScriptsDialog_DefaultEmbeddedScript;

                final MultiLineInputDialog dlg = new MultiLineInputDialog(getTableView(), item.text);
                final Optional<String> result = dlg.showAndWait();
                if (result.isPresent())
                {
                    item.file.set(ScriptInfo.EMBEDDED_PYTHON);
                    item.text = result.get();
                }
            });
        }

        private ScriptItem getScriptItem()
        {
        	final TableView<ScriptItem> table = getTableView();
        	return table.getItems().get(getTableRow().getIndex());
        }

        @Override
        protected void updateItem(final Boolean item, final boolean empty)
        {
            setGraphic(empty ? null : buttons);
        }
    };



    /** Data that is linked to the scripts_table */
    private final ObservableList<ScriptItem> script_items = FXCollections.observableArrayList();

    /** Table for all scripts */
    private TableView<ScriptItem> scripts_table;

    /** Data that is linked to the pvs_table */
    private final ObservableList<PVItem> pv_items = FXCollections.observableArrayList();

    /** Table for PVs of currently selected script */
    private TableView<PVItem> pvs_table;


    /** @param scripts Scripts to show/edit in the dialog */
    public ScriptsDialog(final List<ScriptInfo> scripts)
    {
        setTitle(Messages.ScriptsDialog_Title);
        setHeaderText(Messages.ScriptsDialog_Info);

        scripts.forEach(script -> script_items.add(ScriptItem.forInfo(script)));
        fixupScripts(0);

        getDialogPane().setContent(createContent());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        setResultConverter(button ->
        {
            if (button != ButtonType.OK)
                return null;
            return script_items.stream()
					           .filter(item -> ! item.file.get().isEmpty())
					           .map(ScriptItem::getScriptInfo)
					           .collect(Collectors.toList());
        });
    }

    private Node createContent()
    {
        final Node scripts = createScriptsTable();
        final Node pvs = createPVsTable();

        // Display PVs of currently selected script
        scripts_table.getSelectionModel().selectedItemProperty().addListener((prop, old, selected) ->
        {
            if (selected == null)
                pv_items.clear();
            else
            {
                pv_items.setAll(selected.pvs);
                fixupPVs(0);
            }
        });
		// Update PVs of selected script from PVs table
        final ListChangeListener<PVItem> ll = change ->
        {
            final ScriptItem selected = scripts_table.getSelectionModel().getSelectedItem();
        	if (selected != null)
        		selected.pvs = new ArrayList<>(change.getList());
        };
        pv_items.addListener(ll);

        final HBox box = new HBox(10, scripts, pvs);
        HBox.setHgrow(scripts, Priority.ALWAYS);
        HBox.setHgrow(pvs, Priority.ALWAYS);

        // box.setStyle("-fx-background-color: rgb(255, 100, 0, 0.2);"); // For debugging
        return box;
    }

    /** @return Node for UI elements that edit the scripts */
    private Node createScriptsTable()
    {
        // Create table with editable script 'file' column
        final TableColumn<ScriptItem, String> name_col = new TableColumn<>(Messages.ScriptsDialog_ColScript);
        name_col.setCellValueFactory(new PropertyValueFactory<ScriptItem, String>("file"));
        name_col.setCellFactory(TextFieldTableCell.<ScriptItem>forTableColumn());
        name_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            script_items.get(row).file.set(event.getNewValue());
            fixupScripts(row);
        });

        // Table column w/ buttons to browse for file or set to ScriptInfo.EMBEDDED_PYTHON
        final TableColumn<ScriptItem, Boolean> buttons_col = new TableColumn<>();
        buttons_col.setStyle("-fx-background-color: -fx-color");
        buttons_col.setCellFactory(ScriptButtonCell.forTableColumn());

        scripts_table = new TableView<>(script_items);
        scripts_table.getColumns().add(name_col);
        scripts_table.getColumns().add(buttons_col);
        scripts_table.setEditable(true);
        scripts_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scripts_table.setTooltip(new Tooltip(Messages.ScriptsDialog_ScriptsTT));
        scripts_table.setMinWidth(400);

        // Buttons
        final Button add = new Button(Messages.Add, JFXUtil.getIcon("add.png"));
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(event ->
        {
            script_items.add(new ScriptItem());
        });

        final Button remove = new Button(Messages.Remove, JFXUtil.getIcon("delete.png"));
        remove.setMaxWidth(Double.MAX_VALUE);
        remove.setOnAction(event ->
        {
            final int sel = scripts_table.getSelectionModel().getSelectedIndex();
            if (sel >= 0)
            {
                script_items.remove(sel);
                fixupScripts(sel);
            }
        });

        final VBox buttons = new VBox(10, add, remove);
        final HBox content = new HBox(10, scripts_table, buttons);
        HBox.setHgrow(scripts_table, Priority.ALWAYS);
        return content;
    }

    /** Fix scripts data: Delete empty rows in middle
     *  @param changed_row Row to check, and remove if it's empty
     */
    private void fixupScripts(final int changed_row)
    {
        // Check if edited row is now empty and should be deleted
        if (changed_row < script_items.size())
        {
            final ScriptItem item = script_items.get(changed_row);
            if (item.fileProperty().get().trim().isEmpty())
                script_items.remove(changed_row);
        }
    }

    /** @return Node for UI elements that edit the PVs of a script */
    private Node createPVsTable()
    {
        // Create table with editable 'name' column
        final TableColumn<PVItem, String> name_col = new TableColumn<>(Messages.ScriptsDialog_ColPV);
        name_col.setCellValueFactory(new PropertyValueFactory<PVItem, String>("name"));
        name_col.setCellFactory(TextFieldTableCell.forTableColumn());
        name_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            pv_items.get(row).nameProperty().set(event.getNewValue());
            fixupPVs(row);
        });

        // Table column for 'trigger' uses CheckBoxTableCell that directly modifies the Observable Property
        final TableColumn<PVItem, Boolean> trigger_col = new TableColumn<>(Messages.ScriptsDialog_ColTrigger);
        trigger_col.setCellValueFactory(new PropertyValueFactory<PVItem, Boolean>("trigger"));
        trigger_col.setCellFactory(CheckBoxTableCell.<PVItem>forTableColumn(trigger_col));

        pvs_table = new TableView<>(pv_items);
        pvs_table.getColumns().add(name_col);
        pvs_table.getColumns().add(trigger_col);
        pvs_table.setEditable(true);
        pvs_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pvs_table.setTooltip(new Tooltip(Messages.ScriptsDialog_PVsTT));
        pvs_table.setPlaceholder(new Label(Messages.ScriptsDialog_SelectScript));

        // Buttons
        final Button add = new Button(Messages.Add, JFXUtil.getIcon("add.png"));
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(event ->
        {
            pv_items.add(new PVItem("", true));
        });

        final Button remove = new Button(Messages.Remove, JFXUtil.getIcon("delete.png"));
        remove.setMaxWidth(Double.MAX_VALUE);
        remove.setOnAction(event ->
        {
            final int sel = pvs_table.getSelectionModel().getSelectedIndex();
            if (sel >= 0)
            {
                pv_items.remove(sel);
                fixupPVs(sel);
            }
        });

        final VBox buttons = new VBox(10, add, remove);
        final HBox content = new HBox(10, pvs_table, buttons);
        HBox.setHgrow(pvs_table, Priority.ALWAYS);
        return content;
    }

    /** Fix PVs data: Delete empty rows in middle
     *  @param changed_row Row to check, and remove if it's empty
     */
    private void fixupPVs(final int changed_row)
    {
        // Check if edited row is now empty and should be deleted
        if (changed_row < pv_items.size())
        {
            final PVItem item = pv_items.get(changed_row);
            if (item.nameProperty().get().trim().isEmpty())
                pv_items.remove(changed_row);
        }
    }
}
