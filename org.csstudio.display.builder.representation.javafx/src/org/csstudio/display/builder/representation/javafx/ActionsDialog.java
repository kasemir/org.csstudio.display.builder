/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.ActionInfo.ActionType;
import org.csstudio.display.builder.model.properties.ExecuteScriptActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo.Target;
import org.csstudio.display.builder.model.properties.ScriptInfo;
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
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/** Dialog for editing {@link ActionInfo} list
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionsDialog extends Dialog<List<ActionInfo>>
{
    // XXX: Smoother handling of script type changes
    // Prompt if embedded text should be deleted when changing to external file
    // Read existing file into embedded text when switching from file to embedded

    private final AutocompleteMenu menu;

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

    // UI elements for ExecuteScriptAction
    private TextField execute_script_description, execute_script_file;
    private TextArea execute_script_text;

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
                logger.log(Level.WARNING, "Error displaying " + action, ex);
            }
        }
    };

    /** Create dialog
     *  @param initial_actions Initial list of actions
     */
    public ActionsDialog(final List<ActionInfo> initial_actions)
    {
        this(initial_actions, new AutocompleteMenu());
    }

    /**
     * Create dialog
     * 
     * @param initial_actions Initial list of actions
     * @param menu {@link AutocompleteMenu} to use for PV names (must not be
     *            null)
     */
    public ActionsDialog(final List<ActionInfo> initial_actions, final AutocompleteMenu menu)
    {
        this.menu = menu;

        actions.addAll(initial_actions);

        setTitle(Messages.ActionsDialog_Title);
        setHeaderText(Messages.ActionsDialog_Info);

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

        layout.add(new Label(Messages.ActionsDialog_Actions), 0, 0);

        action_list.setCellFactory(view -> new ActionInfoCell());
        layout.add(action_list, 0, 1);

        final MenuButton add = new MenuButton(Messages.Add, JFXUtil.getIcon("add.png"));
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
            add.getItems().add(item);
        }
        add.setMaxWidth(Double.MAX_VALUE);

        final Button remove = new Button(Messages.Remove, JFXUtil.getIcon("delete.png"));
        remove.setMaxWidth(Double.MAX_VALUE);
        remove.setOnAction(event ->
        {
            if (selected_action_index >= 0  &&  selected_action_index < actions.size())
                actions.remove(selected_action_index);
        });

        final Button up = new Button(Messages.MoveUp, JFXUtil.getIcon("up.png"));
        up.setMaxWidth(Double.MAX_VALUE);
        up.setOnAction(event ->
        {
            if (selected_action_index > 0  &&  selected_action_index < actions.size())
            {
                updating = true;
                try
                {
                    final ActionInfo item = actions.remove(selected_action_index);
                    -- selected_action_index;
                    actions.add(selected_action_index, item);
                    action_list.getSelectionModel().select(item);
                }
                finally
                {
                    updating = false;
                }
            }
        });

        final Button down = new Button(Messages.MoveDown, JFXUtil.getIcon("down.png"));
        down.setMaxWidth(Double.MAX_VALUE);
        down.setOnAction(event ->
        {
            if (selected_action_index >= 0  &&  selected_action_index < actions.size() - 1)
            {
                updating = true;
                try
                {
                    final ActionInfo item = actions.remove(selected_action_index);
                    ++ selected_action_index;
                    actions.add(selected_action_index, item);
                    action_list.getSelectionModel().select(item);
                }
                finally
                {
                    updating = false;
                }
            }
        });


        final VBox buttons = new VBox(10, add, remove, up, down);
        layout.add(buttons, 1, 1);


        layout.add(new Label(Messages.ActionsDialog_Detail), 2, 0);

        final GridPane open_display_details = createOpenDisplayDetails();
        final GridPane write_pv_details = createWritePVDetails();
        final GridPane execute_script_details = createExecuteScriptDetails();
        open_display_details.setVisible(false);
        write_pv_details.setVisible(false);
        execute_script_details.setVisible(false);

        final StackPane details = new StackPane(open_display_details, write_pv_details, execute_script_details);
        layout.add(details, 2, 1);
        GridPane.setHgrow(details, Priority.ALWAYS);
        GridPane.setVgrow(details, Priority.ALWAYS);

        getDialogPane().setContent(layout);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        // Show and initialize *_details sub-pane for selected action
        action_list.getSelectionModel().selectedItemProperty().addListener((l, old, action) ->
        {
            if (updating)
                return;
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
                execute_script_details.setVisible(false);
                showOpenDisplayAction((OpenDisplayActionInfo) action);
            }
            else if (action instanceof WritePVActionInfo)
            {
                open_display_details.setVisible(false);
                write_pv_details.setVisible(true);
                execute_script_details.setVisible(false);
                showWritePVAction((WritePVActionInfo) action);
            }
            else if (action instanceof ExecuteScriptActionInfo)
            {
                open_display_details.setVisible(false);
                write_pv_details.setVisible(false);
                execute_script_details.setVisible(true);
                showExecuteScriptAction((ExecuteScriptActionInfo)action);
            }
            else
            {
                write_pv_details.setVisible(false);
                open_display_details.setVisible(false);
                logger.log(Level.WARNING, "Unknown action type " + action);
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

        open_display_details.add(new Label(Messages.ActionsDialog_Description), 0, 0);
        open_display_description = new TextField();
        open_display_description.textProperty().addListener(update);
        open_display_details.add(open_display_description, 1, 0);
        GridPane.setHgrow(open_display_description, Priority.ALWAYS);

        open_display_details.add(new Label(Messages.ActionsDialog_DisplayPath), 0, 1);
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

        return new OpenDisplayActionInfo(open_display_description.getText(),
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

        write_pv_details.add(new Label(Messages.ActionsDialog_Description), 0, 0);
        write_pv_description = new TextField();
        write_pv_description.textProperty().addListener(update);
        write_pv_details.add(write_pv_description, 1, 0);
        GridPane.setHgrow(write_pv_description, Priority.ALWAYS);

        write_pv_details.add(new Label(Messages.ActionsDialog_PVName), 0, 1);
        write_pv_name = new TextField();
        menu.attachField(write_pv_name);
        setOnHidden((event) -> menu.removeField(write_pv_name));
        write_pv_name.textProperty().addListener(update);
        write_pv_details.add(write_pv_name, 1, 1);

        write_pv_details.add(new Label(Messages.ActionsDialog_Value), 0, 2);
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

    /** @return Sub-pane for ExecuteScript action */
    private GridPane createExecuteScriptDetails()
    {
        final InvalidationListener update = whatever ->
        {
            if (updating  ||  selected_action_index < 0)
                return;
            actions.set(selected_action_index, getExecuteScriptAction());
        };

        final GridPane execute_script_details = new GridPane();
        execute_script_details.setHgap(10);
        execute_script_details.setVgap(10);

        execute_script_details.add(new Label(Messages.ActionsDialog_Description), 0, 0);
        execute_script_description = new TextField();
        execute_script_description.textProperty().addListener(update);
        execute_script_details.add(execute_script_description, 1, 0);
        GridPane.setHgrow(execute_script_description, Priority.ALWAYS);

        execute_script_details.add(new Label(Messages.ActionsDialog_ScriptPath), 0, 1);
        execute_script_file = new TextField();
        execute_script_file.textProperty().addListener(update);
        execute_script_details.add(execute_script_file, 1, 1);

        final Button btn_file = new Button(Messages.ScriptsDialog_BtnFile, JFXUtil.getIcon("open_file.png"));
        btn_file.setOnAction(event ->
        {
            final FileChooser dlg = new FileChooser();
            dlg.setTitle(Messages.ScriptsDialog_FileBrowser_Title);
            if (execute_script_file.getText().length() > 0)
            {
                File file = new File(execute_script_file.getText());
                dlg.setInitialDirectory(file.getParentFile());
                dlg.setInitialFileName(file.getName());
            }
            dlg.getExtensionFilters().addAll(
                    new ExtensionFilter(Messages.ScriptsDialog_FileType_All, "*.*"),
                    new ExtensionFilter(Messages.ScriptsDialog_FileType_Py, "*.py"),
                    new ExtensionFilter(Messages.ScriptsDialog_FileType_JS, "*.js"));
            final Window window = btn_file.getScene().getWindow();
            final File result = dlg.showOpenDialog(window);
            if (result != null)
            {
                execute_script_file.setText(result.getPath());
                execute_script_text.setText(null);
            }
        });

        final Button btn_embed_py = new Button(Messages.ScriptsDialog_BtnEmbedPy, JFXUtil.getIcon("embedded_script.png"));
        btn_embed_py.setOnAction(event ->
        {
            execute_script_file.setText(ScriptInfo.EMBEDDED_PYTHON);
            final String text = execute_script_text.getText();
            if (text == null  ||  text.trim().isEmpty())
                execute_script_text.setText(Messages.ScriptsDialog_DefaultEmbeddedPython);
        });

        final Button btn_embed_js = new Button(Messages.ScriptsDialog_BtnEmbedJS, JFXUtil.getIcon("embedded_script.png"));
        btn_embed_js.setOnAction(event ->
        {
            execute_script_file.setText(ScriptInfo.EMBEDDED_JAVASCRIPT);
            final String text = execute_script_text.getText();
            if (text == null  ||  text.trim().isEmpty())
                execute_script_text.setText(Messages.ScriptsDialog_DefaultEmbeddedJavaScript);
        });

        execute_script_details.add(new HBox(10, btn_file, btn_embed_py, btn_embed_js), 1, 2);

        execute_script_details.add(new Label(Messages.ActionsDialog_ScriptText), 0, 3);
        execute_script_text = new TextArea();
        execute_script_text.setText(null);
        execute_script_text.textProperty().addListener(update);
        execute_script_details.add(execute_script_text, 0, 4, 2, 1);
        GridPane.setVgrow(execute_script_text, Priority.ALWAYS);

        return execute_script_details;
    }

    /** @param action {@link ExecuteScriptActionInfo} to show */
    private void showExecuteScriptAction(final ExecuteScriptActionInfo action)
    {
        updating = true;
        try
        {
            execute_script_description.setText(action.getDescription());
            execute_script_file.setText(action.getInfo().getPath());
            execute_script_text.setText(action.getInfo().getText());
        }
        finally
        {
            updating = false;
        }
    }

    /** @return {@link ExecuteScriptActionInfo} from sub pane */
    private ExecuteScriptActionInfo getExecuteScriptAction()
    {
        final String file = execute_script_file.getText();
        final String text = (file.equals(ScriptInfo.EMBEDDED_PYTHON) ||
                             file.equals(ScriptInfo.EMBEDDED_JAVASCRIPT))
                            ? execute_script_text.getText()
                            : null;
        return new ExecuteScriptActionInfo(execute_script_description.getText(),
                                           new ScriptInfo(file, text, Collections.emptyList()));
    }
}
