/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.properties.RuleInfo;
import org.csstudio.display.builder.model.properties.ScriptPV;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Dialog for editing {@link RuleInfo}s
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class RulesDialog extends Dialog<List<RuleInfo>>
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

    /** Modifiable RuleInfo */
    public static class RuleItem
    {
        public StringProperty name = new SimpleStringProperty();
        public String text;
        public List<PVItem> pvs;

        public RuleItem()
        {
            this(Messages.RulesDialog_DefaultRuleName, null, new ArrayList<>());
        }

        public RuleItem(final String name, final String text, final List<PVItem> pvs)
        {
            this.name.set(name);
            this.text = text;
            this.pvs = pvs;
        }

        public static RuleItem forInfo(final RuleInfo info)
        {
            final List<PVItem> pvs = new ArrayList<>();
            info.getPVs().forEach(pv -> pvs.add(PVItem.forPV(pv)));
            return new RuleItem(info.getName(), "text", pvs);
        }

        public RuleInfo getRuleInfo()
        {
            final List<ScriptPV> spvs = new ArrayList<>();
            pvs.forEach(pv -> spvs.add(pv.toScriptPV()));
            return new RuleInfo(name.get(), text, false, null, spvs);
        }

        public StringProperty nameProperty()
        {
            return name;
        }
    };

    /** Data that is linked to the rules_table */
    private final ObservableList<RuleItem> rule_items = FXCollections.observableArrayList();

    /** Table for all rules */
    private TableView<RuleItem> rules_table;

    /** Data that is linked to the pvs_table */
    private final ObservableList<PVItem> pv_items = FXCollections.observableArrayList();

    /** Table for PVs of currently selected rule */
    private TableView<PVItem> pvs_table;

    private RuleItem selected_rule_item = null;


    /** @param rules Rules to show/edit in the dialog */
    public RulesDialog(final List<RuleInfo> rules)
    {
        setTitle(Messages.RulesDialog_Title);
        setHeaderText(Messages.RulesDialog_Info);

        rules.forEach(rule -> rule_items.add(RuleItem.forInfo(rule)));
        fixupRules(0);

        getDialogPane().setContent(createContent());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        setResultConverter(button ->
        {
            if (button != ButtonType.OK)
                return null;
            return rule_items.stream()
					           .filter(item -> ! item.name.get().isEmpty())
					           .map(RuleItem::getRuleInfo)
					           .collect(Collectors.toList());
        });
    }

    private Node createContent()
    {
        final Node rules = createRulesTable();
        final Node pvs = createPVsTable();

        // Display PVs of currently selected rule
        rules_table.getSelectionModel().selectedItemProperty().addListener((prop, old, selected) ->
        {
            selected_rule_item = selected;
            if (selected == null)
            {
                pv_items.clear();
            }
            else
            {
                pv_items.setAll(selected.pvs);
                fixupPVs(0);
            }
        });
		// Update PVs of selected rule from PVs table
        final ListChangeListener<PVItem> ll = change ->
        {
            final RuleItem selected = rules_table.getSelectionModel().getSelectedItem();
        	if (selected != null)
        		selected.pvs = new ArrayList<>(change.getList());
        };
        pv_items.addListener(ll);

        final HBox box = new HBox(10, rules, pvs);
        HBox.setHgrow(rules, Priority.ALWAYS);
        HBox.setHgrow(pvs, Priority.ALWAYS);

        // box.setStyle("-fx-background-color: rgb(255, 100, 0, 0.2);"); // For debugging
        return box;
    }

    /** @return Node for UI elements that edit the rules */
    private Node createRulesTable()
    {
        // Create table with editable rule 'name' column
        final TableColumn<RuleItem, String> name_col = new TableColumn<>(Messages.RulesDialog_ColName);
        name_col.setCellValueFactory(new PropertyValueFactory<RuleItem, String>("name"));
        name_col.setCellFactory(TextFieldTableCell.<RuleItem>forTableColumn());
        name_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            rule_items.get(row).name.set(event.getNewValue());
            fixupRules(row);
        });

        rules_table = new TableView<>(rule_items);
        rules_table.getColumns().add(name_col);
        rules_table.setEditable(true);
        rules_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rules_table.setTooltip(new Tooltip(Messages.RulesDialog_RulesTT));

        // Buttons
        final Button add = new Button(Messages.Add, JFXUtil.getIcon("add.png"));
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(event ->
        {
            rule_items.add(new RuleItem());
        });

        final Button remove = new Button(Messages.Remove, JFXUtil.getIcon("delete.png"));
        remove.setMaxWidth(Double.MAX_VALUE);
        remove.setOnAction(event ->
        {
            final int sel = rules_table.getSelectionModel().getSelectedIndex();
            if (sel >= 0)
            {
                rule_items.remove(sel);
                fixupRules(sel);
            }
        });

        final VBox buttons = new VBox(10, add, remove,
                                          new Separator(Orientation.HORIZONTAL));
        final HBox content = new HBox(10, rules_table, buttons);
        HBox.setHgrow(rules_table, Priority.ALWAYS);
        return content;
    }

    /** Fix rules data: Delete empty rows in middle
     *  @param changed_row Row to check, and remove if it's empty
     */
    private void fixupRules(final int changed_row)
    {
        // Check if edited row is now empty and should be deleted
        if (changed_row < rule_items.size())
        {
            final RuleItem item = rule_items.get(changed_row);
            if (item.nameProperty().get().trim().isEmpty())
                rule_items.remove(changed_row);
        }
    }

    /** @return Node for UI elements that edit the PVs of a rule */
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
        pvs_table.setTooltip(new Tooltip(Messages.RulesDialog_PVsTT));
        pvs_table.setPlaceholder(new Label(Messages.RulesDialog_SelectRule));

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
