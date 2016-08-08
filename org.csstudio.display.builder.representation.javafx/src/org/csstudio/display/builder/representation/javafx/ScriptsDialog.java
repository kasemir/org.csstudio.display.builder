/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

/** Dialog for editing {@link ScriptInfo}s
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScriptsDialog extends Dialog<List<ScriptInfo>>
{
    // XXX Smoother script type change:
    // If already "EmbeddedPy" and file is selected, prompt if embedded script should be deleted.
    // If already "EmbeddedPy" and Embedded JS is selected, prompt if type should be changed from python to JS.
    // If already "EmbeddedJS", ..

    private final AutocompleteMenu menu;

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
            this(Messages.ScriptsDialog_DefaultScriptFile, null, new ArrayList<>());
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

    /** Data that is linked to the scripts_table */
    private final ObservableList<ScriptItem> script_items = FXCollections.observableArrayList();

    /** Table for all scripts */
    private TableView<ScriptItem> scripts_table;

    /** Data that is linked to the pvs_table */
    private final ObservableList<PVItem> pv_items = FXCollections.observableArrayList();

    /** Table for PVs of currently selected script */
    private TableView<PVItem> pvs_table;

    private Button btn_file, btn_embed_py, btn_embed_js;

    private ScriptItem selected_script_item = null;

    public ScriptsDialog(final List<ScriptInfo> scripts)
    {
        this(scripts, new AutocompleteMenu());
    }

    /** @param scripts Scripts to show/edit in the dialog */
    public ScriptsDialog(final List<ScriptInfo> scripts, final AutocompleteMenu menu)
    {
        this.menu = menu;

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
            selected_script_item = selected;
            if (selected == null)
            {
                btn_file.setDisable(true);
                btn_embed_py.setDisable(true);
                btn_embed_js.setDisable(true);
                pv_items.clear();
            }
            else
            {
                btn_file.setDisable(false);
                btn_embed_py.setDisable(false);
                btn_embed_js.setDisable(false);
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

        scripts_table = new TableView<>(script_items);
        scripts_table.getColumns().add(name_col);
        scripts_table.setEditable(true);
        scripts_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scripts_table.setTooltip(new Tooltip(Messages.ScriptsDialog_ScriptsTT));

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

        btn_file = new Button(Messages.ScriptsDialog_BtnFile, JFXUtil.getIcon("open_file.png"));
        btn_file.setMaxWidth(Double.MAX_VALUE);
        btn_file.setDisable(true);
        btn_file.setOnAction(event ->
        {
            final FileChooser dlg = new FileChooser();
            dlg.setTitle(Messages.ScriptsDialog_FileBrowser_Title);
            if (selected_script_item.file.get().length() > 0)
            {
                File file = new File(selected_script_item.file.get());
                dlg.setInitialDirectory(file.getParentFile());
                dlg.setInitialFileName(file.getName());
            }
            dlg.getExtensionFilters().addAll(new ExtensionFilter(Messages.ScriptsDialog_FileType_Py, "*.py"),
                    new ExtensionFilter(Messages.ScriptsDialog_FileType_JS, "*.js"),
                    new ExtensionFilter(Messages.ScriptsDialog_FileType_All, "*.*"));
            final Window window = btn_file.getScene().getWindow();
            final File result = dlg.showOpenDialog(window);
            if (result != null)
            {
                selected_script_item.file.set(result.getPath());
                selected_script_item.text = null;
            }
        });

        btn_embed_py = new Button(Messages.ScriptsDialog_BtnEmbedPy, JFXUtil.getIcon("embedded_script.png"));
        btn_embed_py.setMaxWidth(Double.MAX_VALUE);
        btn_embed_py.setDisable(true);
        btn_embed_py.setOnAction(event ->
        {
            if (selected_script_item.text == null  ||  selected_script_item.text.trim().isEmpty())
                selected_script_item.text = Messages.ScriptsDialog_DefaultEmbeddedPython;

            final MultiLineInputDialog dlg = new MultiLineInputDialog(scripts_table, selected_script_item.text);
            final Optional<String> result = dlg.showAndWait();
            if (result.isPresent())
            {
                selected_script_item.file.set(ScriptInfo.EMBEDDED_PYTHON);
                selected_script_item.text = result.get();
            }
        });

        btn_embed_js = new Button(Messages.ScriptsDialog_BtnEmbedJS, JFXUtil.getIcon("embedded_script.png"));
        btn_embed_js.setMaxWidth(Double.MAX_VALUE);
        btn_embed_js.setDisable(true);
        btn_embed_js.setOnAction(event ->
        {
            if (selected_script_item.text == null  ||  selected_script_item.text.trim().isEmpty())
                selected_script_item.text = Messages.ScriptsDialog_DefaultEmbeddedJavaScript;

            final MultiLineInputDialog dlg = new MultiLineInputDialog(scripts_table, selected_script_item.text);
            final Optional<String> result = dlg.showAndWait();
            if (result.isPresent())
            {
                selected_script_item.file.set(ScriptInfo.EMBEDDED_JAVASCRIPT);
                selected_script_item.text = result.get();
            }
        });

        final VBox buttons = new VBox(10, add, remove,
                                          new Separator(Orientation.HORIZONTAL),
                                          btn_file, btn_embed_py, btn_embed_js);
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

    /**
     * {@link PVItem} {@link TableCell} with {@link AutocompleteMenu}
     * 
     * @author Amanda Carpenter
     */
    private class AutoCompletedTableCell extends TableCell<PVItem, String>
    {
        private TextField textField;
        private final AutocompleteMenu menu;

        public AutoCompletedTableCell(final AutocompleteMenu menu)
        {
            this.menu = menu;
        }

        @Override
        public void startEdit()
        {
            if (!isEmpty())
            {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                menu.attachField(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit()
        {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty)
        {
            super.updateItem(item, empty);
            if (empty)
            {
                setText(null);
                setGraphic(null);
            } else if (isEditing())
            {
                if (textField != null)
                    textField.setText(getItem() == null ? "" : getItem());
                setText(null);
                setGraphic(textField);
            } else
            {
                setText(getItem() == null ? "" : getItem());
                setGraphic(null);
                if (textField != null)
                    menu.removeField(textField);
            }
        }

        private void createTextField()
        {
            if (textField == null)
            {
                textField = new TextField(getItem() == null ? "" : getItem());
                textField.setOnAction((event) ->
                {
                    commitEdit(textField.getText());
                });
            } else
                textField.setText(getItem() == null ? "" : getItem());
            textField.setMinWidth(getWidth() - getGraphicTextGap() * 2);
        }
    }

    /** @return Node for UI elements that edit the PVs of a script */
    private Node createPVsTable()
    {
        // Create table with editable 'name' column
        final TableColumn<PVItem, String> name_col = new TableColumn<>(Messages.ScriptsDialog_ColPV);
        name_col.setCellValueFactory(new PropertyValueFactory<PVItem, String>("name"));
        name_col.setCellFactory((col) -> new AutoCompletedTableCell(menu));
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

        final Button up = new Button(Messages.MoveUp, JFXUtil.getIcon("up.png"));
        up.setMaxWidth(Double.MAX_VALUE);
        up.setOnAction(event ->
        {
            final int sel = pvs_table.getSelectionModel().getSelectedIndex();
            if (sel < 0  ||  sel >= pv_items.size())
                return;
            final PVItem pv = pv_items.remove(sel);
            pv_items.add(sel-1, pv);
            pvs_table.getSelectionModel().select(pv);
        });

        final Button down = new Button(Messages.MoveDown, JFXUtil.getIcon("down.png"));
        down.setMaxWidth(Double.MAX_VALUE);
        down.setOnAction(event ->
        {
            final int sel = pvs_table.getSelectionModel().getSelectedIndex();
            if (sel < 0  ||  sel >= pv_items.size())
                return;
            final PVItem pv = pv_items.remove(sel);
            pv_items.add(sel+1, pv);
            pvs_table.getSelectionModel().select(pv);
        });

        final VBox buttons = new VBox(10, add, remove, up, down);
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
