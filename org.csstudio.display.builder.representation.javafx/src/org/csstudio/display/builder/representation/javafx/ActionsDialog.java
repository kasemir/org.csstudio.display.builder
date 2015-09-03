/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.ActionInfo.ActionType;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo.Target;
import org.csstudio.display.builder.model.properties.WritePVActionInfo;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Dialog for editing {@link ActionInfo} list
 *  @author Kay Kasemir
 */
public class ActionsDialog extends Dialog<List<ActionInfo>>
{
    // TODO Order actions?
    // TODO Externalize strings
    /** Actions edited by the dialog */
    private final ObservableList<ActionInfo> actions = FXCollections.observableArrayList();

    /** Currently selected item in <code>actions</code> */
    private int selected_action_index = -1;

    /** Table of actions */
    private final ListView<ActionInfo> action_list = new ListView<>(actions);

    // UI elements for OpenDisplayAction
    private TextField open_display_description, open_display_path;
    private ToggleGroup open_display_targets;
    private MacrosTable open_display_macros;

    // UI elements for WritePVAction
    private TextField write_pv_description, write_pv_name, write_pv_value;

    /** Prevent circular updates */
    private boolean updating = false;


    /** ListView cell for ActionInfo, shows title if possible */
    private static class ActionInfoCell extends ListCell<ActionInfo>
    {
        @Override
        protected void updateItem(final ActionInfo action, final boolean empty)
        {
            super.updateItem(action, empty);
            try
            {
                if (action == null)
                {
                    setText("");
                    setGraphic(null);
                }
                else
                {
                    setText(action.toString());
                    setGraphic(new ImageView(new Image(action.getType().getIconStream())));
                }
            }
            catch (Exception ex)
            {
                Logger.getLogger(ActionsDialog.class.getName())
                      .log(Level.WARNING, "Error displaying " + action, ex);
            }
        }
    };

    /** Create dialog
     *  @param initial_actions Initial list of actions
     */
    public ActionsDialog(final List<ActionInfo> initial_actions)
    {
        actions.addAll(initial_actions);

        setTitle("Actions");
        setHeaderText("Configure actions which open displays, write PVs etc.");

        // Actions:           Action Detail:
        // | List |  [Add]    |  Pane       |
        // | List |  [Remove] |  Pane       |
        // | List |           |  Pane       |
        //
        // Inside Action Detail pane, only one the *_details sub-pane
        // suitable for the selected action is visible.
        final GridPane layout = new GridPane();
        // layout.setGridLinesVisible(true); // For debugging
        layout.setHgap(10);
        layout.setVgap(10);
        layout.setPadding(new Insets(10));

        layout.add(new Label("Actions:"), 0, 0);

        action_list.setCellFactory(view -> new ActionInfoCell());
        layout.add(action_list, 0, 1);

        // TODO Change 'add' into drop-down of available action types
        final MenuBar add = new MenuBar();
        final Menu add_items = new Menu(Messages.Add);
        for (ActionType type : ActionType.values())
        {
            final ImageView icon = new ImageView(new Image(type.getIconStream()));
            final MenuItem item = new MenuItem(type.toString(), icon);
            item.setOnAction(event ->
            {
                final ActionInfo new_action = ActionInfo.createAction(type);
                actions.add(new_action);
                action_list.getSelectionModel().select(new_action);
            });
            add_items.getItems().add(item);
        }
        add.getMenus().add(add_items);
        add.setMaxWidth(Double.MAX_VALUE);

        final Button remove = new Button(Messages.Remove);
        remove.setMaxWidth(Double.MAX_VALUE);
        remove.setOnAction(event ->
        {
            if (selected_action_index >= 0  &&  selected_action_index < actions.size())
                actions.remove(selected_action_index);
        });

        final VBox buttons = new VBox(10, add, remove);
        layout.add(buttons, 1, 1);


        layout.add(new Label("Action Detail:"), 2, 0);

        final GridPane open_display_details = createOpenDisplayDetails();
        final GridPane write_pv_details = createWritePVDetails();
        open_display_details.setVisible(false);
        write_pv_details.setVisible(false);

        final StackPane details = new StackPane(open_display_details, write_pv_details);
        layout.add(details, 2, 1);
        GridPane.setHgrow(details, Priority.ALWAYS);
        GridPane.setVgrow(details, Priority.ALWAYS);

        getDialogPane().setContent(layout);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        // Show and initialize *_details sub-pane for selected action
        action_list.getSelectionModel().selectedItemProperty().addListener((l, old, action) ->
        {
            final int selection = action_list.getSelectionModel().getSelectedIndex();
            if (selection < 0)
            {   // Selection was lost because user clicked on some other UI element.
                // If previously selected index is otherwise still valid, keep it
                if (selected_action_index >= 0  &&  selected_action_index < actions.size())
                    return;
            }
            selected_action_index = selection;
            if (action instanceof OpenDisplayActionInfo)
            {
                write_pv_details.setVisible(false);
                open_display_details.setVisible(true);
                showOpenDisplayAction((OpenDisplayActionInfo) action);
            }
            else if (action instanceof WritePVActionInfo)
            {
                open_display_details.setVisible(false);
                write_pv_details.setVisible(true);
                showWritePVAction((WritePVActionInfo) action);
            }
        });

        setResultConverter(button ->
        {
            if (button == ButtonType.OK)
                return actions;
            return null;
        });

        // Select first action, if there is one
        if (actions.size() > 0)
            action_list.getSelectionModel().select(0);
    }

    /** @return Sub-pane for OpenDisplay action */
    private GridPane createOpenDisplayDetails()
    {
        final InvalidationListener update = whatever ->
        {
            if (updating  ||  selected_action_index < 0)
                return;
            actions.set(selected_action_index, getOpenDisplayAction());
        };

        final GridPane open_display_details = new GridPane();
        open_display_details.setHgap(10);
        open_display_details.setVgap(10);

        open_display_details.add(new Label("Description:"), 0, 0);
        open_display_description = new TextField();
        open_display_description.textProperty().addListener(update);
        open_display_details.add(open_display_description, 1, 0);
        GridPane.setHgrow(open_display_description, Priority.ALWAYS);

        open_display_details.add(new Label("Display Path:"), 0, 1);
        open_display_path = new TextField();
        open_display_path.textProperty().addListener(update);
        open_display_details.add(open_display_path, 1, 1);

        final HBox modes_box = new HBox(10);
        open_display_targets = new ToggleGroup();
        final Target[] modes = Target.values();
        for (int i=0; i<modes.length; ++i)
        {
            final RadioButton target = new RadioButton(modes[i].toString());
            target.setToggleGroup(open_display_targets);
            target.selectedProperty().addListener(update);
            modes_box.getChildren().add(target);
        }
        open_display_details.add(modes_box, 0, 2, 2, 1);

        open_display_macros = new MacrosTable(new Macros());
        open_display_macros.addListener(update);
        open_display_details.add(open_display_macros.getNode(), 0, 3, 2, 1);
        GridPane.setHgrow(open_display_macros.getNode(), Priority.ALWAYS);
        GridPane.setVgrow(open_display_macros.getNode(), Priority.ALWAYS);

        return open_display_details;
    }

    /** @param info {@link OpenDisplayActionInfo} to show */
    private void showOpenDisplayAction(final OpenDisplayActionInfo info)
    {
        updating = true;
        try
        {
            open_display_description.setText(info.getDescription());
            open_display_path.setText(info.getFile());
            open_display_targets.getToggles().get(info.getTarget().ordinal()).setSelected(true);
            open_display_macros.setMacros(info.getMacros());
        }
        finally
        {
            updating = false;
        }
    }

    /** @return {@link OpenDisplayActionInfo} from sub pane */
    private OpenDisplayActionInfo getOpenDisplayAction()
    {
        Target target = Target.REPLACE;
        List<Toggle> modes = open_display_targets.getToggles();
        for (int i=0; i<modes.size(); ++i)
            if (modes.get(i).isSelected())
            {
                target = Target.values()[i];
                break;
            }

        return new OpenDisplayActionInfo(open_display_description.getText().trim(),
                                         open_display_path.getText().trim(),
                                         open_display_macros.getMacros(),
                                         target);
    }

    /** @return Sub-pane for WritePV action */
    private GridPane createWritePVDetails()
    {
        final InvalidationListener update = whatever ->
        {
            if (updating  ||  selected_action_index < 0)
                return;
            actions.set(selected_action_index, getWritePVAction());
        };

        final GridPane write_pv_details = new GridPane();
        write_pv_details.setHgap(10);
        write_pv_details.setVgap(10);

        write_pv_details.add(new Label("Description:"), 0, 0);
        write_pv_description = new TextField();
        write_pv_description.textProperty().addListener(update);
        write_pv_details.add(write_pv_description, 1, 0);
        GridPane.setHgrow(write_pv_description, Priority.ALWAYS);

        write_pv_details.add(new Label("PV Name:"), 0, 1);
        write_pv_name = new TextField();
        write_pv_name.textProperty().addListener(update);
        write_pv_details.add(write_pv_name, 1, 1);

        write_pv_details.add(new Label("Value:"), 0, 2);
        write_pv_value = new TextField();
        write_pv_value.textProperty().addListener(update);
        write_pv_details.add(write_pv_value, 1, 2);

        return write_pv_details;
    }

    /** @param action {@link WritePVActionInfo} to show */
    private void showWritePVAction(final WritePVActionInfo action)
    {
        updating = true;
        try
        {
            write_pv_description.setText(action.getDescription());
            write_pv_name.setText(action.getPV());
            write_pv_value.setText(action.getValue());
        }
        finally
        {
            updating = false;
        }
    }

    /** @return {@link WritePVActionInfo} from sub pane */
    private WritePVActionInfo getWritePVAction()
    {
        return new WritePVActionInfo(write_pv_description.getText(),
                                     write_pv_name.getText(),
                                     write_pv_value.getText());
    }
}
