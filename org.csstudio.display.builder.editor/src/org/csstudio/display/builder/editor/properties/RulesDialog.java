/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.RuleInfo;
import org.csstudio.display.builder.model.properties.RuleInfo.ExpressionInfo;
import org.csstudio.display.builder.model.properties.RulesWidgetProperty;
import org.csstudio.display.builder.model.properties.ScriptPV;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.Messages;
import org.csstudio.display.builder.util.undo.UndoableActionManager;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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
import javafx.util.Callback;

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

    /** Expression info as property-based item for table */
    public static class ExprItem<T>
    {
        final private StringProperty boolExp = new SimpleStringProperty();
        //TODO: this valExp/val_exp naming is terrible. Change to propVal
        final private T valExpObj;
        final private StringProperty valExpStr = new SimpleStringProperty();
        final private SimpleObjectProperty<Node> field = new SimpleObjectProperty<Node>();
        private final List<WidgetPropertyBinding<?,?>> bindings = new ArrayList<>();

        public ExprItem(final String boolE, final T valE, final UndoableActionManager undo)
        {
            this.boolExp.set(boolE);
            this.valExpObj = valE;
            if (valE instanceof String)
            {
                valExpStr.set((String)valE);
                field.setValue(null);
            }
            else if (valE instanceof WidgetProperty<?>)
            {
                WidgetProperty<?> prop = (WidgetProperty<?>)valE;
                valExpStr.set( prop.getValue().toString() );
                field.setValue(PropertyPanelSection.
                        bindSimplePropertyField(undo, bindings, prop, new ArrayList<Widget>()));
            }
            else
            {
                valExpStr.set("ValExp class error" + valExpStr.getClass().getName());
            }
        }

        public ExpressionInfo<T> toExp()
        {
            return new ExpressionInfo<T>(boolExp.get(), valExpObj);
        }

        public SimpleObjectProperty<Node> fieldProperty()
        {
            return field;
        }

        public StringProperty boolExpProperty()
        {
            return boolExp;
        }

        public StringProperty valExpStrProperty()
        {
            return valExpStr;
        }
    };

    public static <T> ExprItem<T> forExp(final ExpressionInfo<T> exp, final UndoableActionManager undo)
    {
        return new ExprItem<T>(exp.getBoolExp(), exp.getValExp(), undo);
    }

    public static <T> ExprItem<T> changePropType(final T prop, final ExprItem<?> old_exp, final UndoableActionManager undo)
    {
        return new ExprItem<T>(old_exp.boolExpProperty().get(), prop, undo);
    }

    public static <T> ExprItem<T> makeExprItem(final T prop,  final UndoableActionManager undo)
    {
        return new ExprItem<T>("new expr", prop, undo);
    }

    public class ValueFormatCell extends TableCell<ExprItem<?>, Node> {

        public ValueFormatCell() {    }

        @Override protected void updateItem(Node item, boolean empty) {
            // calling super here is very important - don't skip this!
            super.updateItem(item, empty);
            setGraphic(item);
        }

    }

    /** Modifiable RuleInfo */
    public static class RuleItem
    {
        private final Logger logger = Logger.getLogger(getClass().getName());

        public List<ExprItem<?>> expressions;
        public List<PVItem> pvs;
        protected StringProperty name = new SimpleStringProperty();
        protected StringProperty prop_id = new SimpleStringProperty();
        public BooleanProperty out_exp = new SimpleBooleanProperty(false);
        protected Widget attached_widget = null;


        public RuleItem(final Widget attached_widget, final String prop_id)
        {
            this(attached_widget, new ArrayList<>(), new ArrayList<>(),
                    Messages.RulesDialog_DefaultRuleName, prop_id, false);
        }

        public RuleItem(final Widget attached_widget,
                final List<ExprItem<?>> exprs,
                final List<PVItem> pvs,
                final String name,
                final String prop_id,
                final boolean out_exp)
        {
            this.attached_widget = attached_widget;
            this.expressions = exprs;
            this.pvs = pvs;
            this.name.set(name);
            this.prop_id.set(prop_id);
            this.out_exp.set(out_exp);
        }

        public static RuleItem forInfo(final Widget attached_widget, final RuleInfo info, final UndoableActionManager undo)
        {
            final List<PVItem> pvs = new ArrayList<>();
            info.getPVs().forEach(pv -> pvs.add(PVItem.forPV(pv)));
            final List<ExprItem<?>> exprs = new ArrayList<>();
            info.getExpressions().forEach(expr -> exprs.add(RulesDialog.forExp(expr, undo)));
            return new RuleItem(attached_widget, exprs, pvs, info.getName(), info.getPropID(), info.getOutputExprFlag());
        }

        public RuleInfo getRuleInfo()
        {
            final List<ScriptPV> spvs = new ArrayList<>();
            pvs.forEach(pv -> spvs.add(pv.toScriptPV()));
            final List<ExpressionInfo<?>> exps = new ArrayList<>();
            expressions.forEach(exp -> exps.add(exp.toExp()));
            return new RuleInfo(name.get(), prop_id.get(), out_exp.get(), exps, spvs);
        }

        public StringProperty nameProperty()
        {
            return name;
        }

        public StringProperty propIDProperty()
        {
            return prop_id;
        }

        public ExprItem<?> addNewExpr(final UndoableActionManager undo)
        {
            WidgetProperty<?> new_prop =
                    RulesWidgetProperty.propIDToNewProp(attached_widget, prop_id.get(), "");

            ExprItem<?> new_exp = makeExprItem(new_prop, undo);
            expressions.add(new_exp);
            return new_exp;
        }

        public boolean tryUpdatePropID( final UndoableActionManager undo, String new_prop_id )
        {
            if (new_prop_id == null)
                return false;

            if (new_prop_id.equals(prop_id.get()))
                return false;

            if (attached_widget == null)
                return false;

            Optional<WidgetProperty<?>> prop = attached_widget.checkProperty(new_prop_id);
            if (!prop.isPresent())
            {
                logger.log(Level.WARNING, "Widget " + attached_widget.getClass().getName()
                        + " cannot make make rule for unknown property id " + new_prop_id);
                return false;
            }

            prop_id.set(new_prop_id);;

            // If just an output expression string. No need to change objects
            if (out_exp.get()) {
                return true;
            }

            List<ExprItem<?>> new_exps = new ArrayList<>();

            for (final ExprItem<?> exp : expressions)
            {
                WidgetProperty<?> new_prop =
                        RulesWidgetProperty.propIDToNewProp(attached_widget, prop_id.get(), "");
                new_exps.add(changePropType(new_prop, exp, undo));

            }
            expressions = new_exps;

            return true;
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

    /** Data that is linked to the expressions_table */
    private final ObservableList<ExprItem<?>> expression_items = FXCollections.observableArrayList();

    /** Table for PVs of currently selected rule */
    private TableView<ExprItem<?>> expressions_table;

    /** Buttons for removing or reordering rules **/
    private Button btn_remove_rule, btn_move_rule_up, btn_move_rule_down;
    /** Buttons for adding/removing PVs and expressions from the selected rule **/
    private Button btn_add_pv, btn_rm_pv, btn_add_exp, btn_rm_exp;

    /** Currently selected rule **/
    private RuleItem selected_rule_item = null;

    /** Widget name and type for the header bar **/
    private final Widget attached_widget;
    /** Undo actions for choosing property values in expressions **/
    final UndoableActionManager undo;

    /** Property options for target of expression **/
    ObservableList<String> prop_id_opts = FXCollections.observableArrayList();
    final List<WidgetProperty<?>> propls;
    private ComboBox<String> propComboBox;

    /** Is the property value an expressions (i.e. user input string) **/
    private CheckBox valExpBox;

    /** turn a property into the long string form used in the combo box **/
    public String getPropLongString(WidgetProperty<?> prop)
    {
        String ret = prop.getDescription() + ",  [" + prop.getName() + "=" + prop.getValue() + "]";
        if ( !propls.contains(prop) )
        {
            return "INVALID: " + ret;
        }
        return ret;
    }

    /** turn this rule's property into the long string form used in the combo box **/
    public String getPropLongString(RuleItem rule)
    {
        Optional<WidgetProperty<?>> prop = rule.attached_widget.checkProperty(rule.prop_id.get());

        if (prop.isPresent())
        {
            return getPropLongString(prop.get());
        }

        return "INVALID: " + rule.prop_id.get();
    }

    /** get the short property id from the long string from the combo box **/
    public String getPropID( String longString )
    {
        int idx = prop_id_opts.indexOf(longString);
        if ((idx < 0) || (idx > propls.size())) {
            Logger.getLogger(this.getClass().getName()).
            log(Level.FINE, "Could not match long string " + longString + " to property." +
                    " Got index " + String.valueOf(idx) + " out of bounds " + String.valueOf(propls.size()));
            return null;
        }
        return propls.get(idx).getName();
    }

    /** @param rules Rules to show/edit in the dialog */
    public RulesDialog(final UndoableActionManager undo, final List<RuleInfo> rules, final Widget attached_widget)
    {
        setTitle(Messages.RulesDialog_Title);
        this.undo = undo;
        this.attached_widget = attached_widget;

        propls = RuleInfo.getTargettableProperties(attached_widget);
        propls.forEach(prop ->
        {
            prop_id_opts.add( getPropLongString(prop) );
        });

        setHeaderText(Messages.RulesDialog_Info + ": " +
                (String) attached_widget.getProperty("type").getValue() + " " +
                (String) attached_widget.getProperty("name").getValue());


        rules.forEach(rule -> rule_items.add(RuleItem.forInfo(attached_widget, rule, undo)));
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
        final Node exprs = createExpressionsTable();

        // Display PVs of currently selected rule
        rules_table.getSelectionModel().selectedItemProperty().addListener((prop, old, selected) ->
        {
            selected_rule_item = selected;
            if (selected == null)
            {
                btn_remove_rule.setDisable(true);
                btn_move_rule_up.setDisable(true);
                btn_move_rule_down.setDisable(true);
                btn_add_pv.setDisable(true);
                btn_rm_pv.setDisable(true);
                btn_add_exp.setDisable(true);
                btn_rm_exp.setDisable(true);
                propComboBox.setDisable(true);
                propComboBox.getSelectionModel().select(null);
                valExpBox.setDisable(true);
                pv_items.clear();
                expression_items.clear();
            }
            else
            {
                btn_remove_rule.setDisable(false);
                btn_move_rule_up.setDisable(false);
                btn_move_rule_down.setDisable(false);
                btn_add_pv.setDisable(false);
                btn_rm_pv.setDisable(false);
                btn_add_exp.setDisable(false);
                btn_rm_exp.setDisable(false);
                propComboBox.setDisable(false);
                propComboBox.getSelectionModel().select(getPropLongString(selected));
                valExpBox.setDisable(false);
                valExpBox.selectedProperty().set(selected.out_exp.get());
                pv_items.setAll(selected.pvs);
                expression_items.setAll(selected.expressions);
                fixupPVs(0);
            }
        });
        // Update PVs of selected rule from PVs table
        final ListChangeListener<PVItem> pll = change ->
        {
            final RuleItem selected = rules_table.getSelectionModel().getSelectedItem();
            if (selected != null)
                selected.pvs = new ArrayList<>(change.getList());
        };
        pv_items.addListener(pll);

        // Update Expressions of selected rule from Expressions table
        final ListChangeListener<ExprItem<?>> ell = change ->
        {
            final RuleItem selected = rules_table.getSelectionModel().getSelectedItem();
            if (selected != null)
                selected.expressions = new ArrayList<>(change.getList());
        };
        expression_items.addListener(ell);

        //What is the property id option we are using?
        final Label propLabel = new Label("Property ID:");
        propComboBox = new ComboBox<String>(prop_id_opts);
        propComboBox.setDisable(true);

        propComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                if (!selected_rule_item.tryUpdatePropID(undo, getPropID(t1)))
                {
                    Logger.getLogger(this.getClass().getName()).
                    log(Level.FINE, "Did not update rule property ID to " + t1);
                }
                else
                {
                    expression_items.setAll(selected_rule_item.expressions);
                }
            }
        });

        //TODO: change this to actually manipulate expression objects in the rule
        valExpBox = new CheckBox("Value as Expression");
        valExpBox.setDisable(true);
        valExpBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                selected_rule_item.out_exp.set(new_val);
            }
        });

        final HBox props = new HBox(10, valExpBox, new Separator(Orientation.VERTICAL), propLabel, propComboBox);
        final HBox subtabs = new HBox(10, pvs, exprs);
        final VBox subitems = new VBox(10, props, new Separator(Orientation.HORIZONTAL), subtabs);
        final VBox rulebox = new VBox(10, rules);
        final HBox box = new HBox(10, rulebox, new Separator(Orientation.VERTICAL), subitems);
        VBox.setVgrow(rules, Priority.ALWAYS);
        HBox.setHgrow(pvs, Priority.ALWAYS);
        HBox.setHgrow(exprs, Priority.ALWAYS);
        HBox.setHgrow(subitems, Priority.ALWAYS);
        VBox.setVgrow(subtabs, Priority.ALWAYS);


        //final Label mainLabel = new Label("Editting rules for widget type " + widgetType);
        //final VBox box = new VBox(10, mainLabel, hbox);

        // box.setStyle("-fx-background-color: rgb(255, 100, 0, 0.2);"); // For debugging
        return box;
    }

    /** @param attached_widget
     * @return Node for UI elements that edit the rules */
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
            rule_items.add( new RuleItem(
                    attached_widget,
                    ( (selected_rule_item == null) ?
                            ( (propls.size() == 0) ? "" : propls.get(0).getName() )
                            :
                                selected_rule_item.prop_id.get() )
                    ));
        });

        btn_remove_rule = new Button(Messages.Remove, JFXUtil.getIcon("delete.png"));
        btn_remove_rule.setMaxWidth(Double.MAX_VALUE);
        btn_remove_rule.setDisable(true);
        btn_remove_rule.setOnAction(event ->
        {
            final int sel = rules_table.getSelectionModel().getSelectedIndex();
            if (sel >= 0)
            {
                rule_items.remove(sel);
                fixupRules(sel);
            }
        });

        //TODO: Add Messages
        btn_move_rule_up = new Button("Move up", JFXUtil.getIcon("up.png"));
        btn_move_rule_up.setMaxWidth(Double.MAX_VALUE);
        btn_move_rule_up.setDisable(true);

        btn_move_rule_down = new Button("Move down", JFXUtil.getIcon("down.png"));
        btn_move_rule_down.setMaxWidth(Double.MAX_VALUE);
        btn_move_rule_down.setDisable(true);


        final VBox buttons = new VBox(10, add,
                new Separator(Orientation.HORIZONTAL),
                btn_remove_rule,
                btn_move_rule_up,
                btn_move_rule_down);
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

    /** @return Node for UI elements that edit the expressions */
    private Node createExpressionsTable()
    {
        // Create table with editable rule 'bool expression' column
        final TableColumn<ExprItem<?>, String> bool_exp_col = new TableColumn<>(Messages.RulesDialog_ColBoolExp);
        bool_exp_col.setCellValueFactory(new PropertyValueFactory<ExprItem<?>, String>("boolExp"));
        bool_exp_col.setCellFactory(TextFieldTableCell.forTableColumn());
        bool_exp_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            expression_items.get(row).boolExpProperty().set(event.getNewValue());
            fixupExpressions(row);
        });

        // Create table with editable rule 'value expression' column
        /*
		final TableColumn<ExprItem<?>, String> val_exp_col = new TableColumn<>(Messages.RulesDialog_ColValExp);
		val_exp_col.setCellValueFactory(new PropertyValueFactory<ExprItem<?>, String>("valExpStr"));
		val_exp_col.setCellFactory(TextFieldTableCell.forTableColumn());
		val_exp_col.setOnEditCommit(event ->
		{
			final int row = event.getTablePosition().getRow();
			expression_items.get(row).valExpStrProperty().set(event.getNewValue());
			fixupExpressions(row);
		});
         */


        final TableColumn<ExprItem<?>, Node> val_exp_col = new TableColumn<>(Messages.RulesDialog_ColValExp);
        val_exp_col.setCellValueFactory(new PropertyValueFactory<ExprItem<?>, Node>("field"));
        val_exp_col.setCellFactory(new Callback<TableColumn<ExprItem<?>, Node>, TableCell<ExprItem<?>, Node>>() {
            @Override public TableCell<ExprItem<?>, Node> call(TableColumn<ExprItem<?>, Node> t) {
                return new ValueFormatCell();
            }
        });
        val_exp_col.setOnEditCommit(event ->
        {
            final int row = event.getTablePosition().getRow();
            expression_items.get(row).fieldProperty().set(event.getNewValue());
            fixupExpressions(row);
        });


        expressions_table = new TableView<>(expression_items);
        expressions_table.getColumns().add(bool_exp_col);
        expressions_table.getColumns().add(val_exp_col);
        expressions_table.setEditable(true);
        expressions_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        expressions_table.setTooltip(new Tooltip(Messages.RulesDialog_ExpressionsTT));
        expressions_table.setPlaceholder(new Label(Messages.RulesDialog_SelectRule));


        btn_add_exp = new Button(Messages.Add, JFXUtil.getIcon("add.png"));
        btn_add_exp.setMaxWidth(Double.MAX_VALUE);
        btn_add_exp.setDisable(true);
        btn_add_exp.setOnAction(event ->
        {
            selected_rule_item.addNewExpr(undo);
            expression_items.setAll(selected_rule_item.expressions);
        });


        btn_rm_exp = new Button(Messages.Remove, JFXUtil.getIcon("delete.png"));
        btn_rm_exp.setMaxWidth(Double.MAX_VALUE);
        btn_rm_exp.setDisable(true);
        btn_rm_exp.setOnAction(event ->
        {
            final int sel = expressions_table.getSelectionModel().getSelectedIndex();
            if (sel >= 0)
            {
                expression_items.remove(sel);
                fixupExpressions(sel);
            }
        });

        final VBox buttons = new VBox(10, btn_add_exp, btn_rm_exp);
        final HBox content = new HBox(10, expressions_table, buttons);
        HBox.setHgrow(expressions_table, Priority.ALWAYS);
        return content;
    }

    /** Fix expressions data: Delete empty rows in middle
     *  @param changed_row Row to check, and remove if it's empty
     */
    private void fixupExpressions(final int changed_row)
    {
        // Check if edited row is now empty and should be deleted
        if (changed_row < expression_items.size())
        {
            //final ExpressionItem item = expression_items.get(changed_row);
            //if (item.boolExpProperty().get().trim().isEmpty())
            //  expression_items.remove(changed_row);
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

        //name_col.prefWidthProperty().bind(pvs_table.widthProperty().multiply(0.8));
        //trigger_col.prefWidthProperty().bind(pvs_table.widthProperty().multiply(0.2));


        // Buttons
        btn_add_pv = new Button(Messages.Add, JFXUtil.getIcon("add.png"));
        btn_add_pv.setMaxWidth(Double.MAX_VALUE);
        btn_add_pv.setDisable(true);
        btn_add_pv.setOnAction(event ->
        {
            pv_items.add(new PVItem("newpv", true));
        });

        btn_rm_pv = new Button(Messages.Remove, JFXUtil.getIcon("delete.png"));
        btn_rm_pv.setMaxWidth(Double.MAX_VALUE);
        btn_rm_pv.setDisable(true);
        btn_rm_pv.setOnAction(event ->
        {
            final int sel = pvs_table.getSelectionModel().getSelectedIndex();
            if (sel >= 0)
            {
                pv_items.remove(sel);
                fixupPVs(sel);
            }
        });

        final VBox buttons = new VBox(10, btn_add_pv, btn_rm_pv);
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
