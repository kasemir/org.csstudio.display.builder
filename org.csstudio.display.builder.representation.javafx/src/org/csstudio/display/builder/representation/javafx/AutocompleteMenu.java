/*******************************************************************************
 * Copyright (c) 2015-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

/** Creates a menu for use with auto-completion. Fields which edit
 *  auto-completeable properties should use attachField on the related
 *  TextInputControl to attach the menu, and removeField to unregister listeners.
 *  For meaningful auto-completion, an {@link AutocompleteMenuUpdater} should be
 *  given which implements methods to request result entries for a given value,
 *  calling setResults as results arrive, and to update the history of the menu.
 *
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class AutocompleteMenu
{
    private final ContextMenu menu = new ContextMenu();
    private AutocompleteMenuUpdater updater = null;
    private List<ControlWrapper> fields = new ArrayList<ControlWrapper>();
    private TextInputControl current_field = null;

    private class ControlWrapper
    {
        private final TextInputControl field;
        private final ChangeListener<Boolean> focused_listener;
        private final ChangeListener<String> text_listener;
        private final EventHandler<KeyEvent> submit_handler;

        ControlWrapper(final TextInputControl field)
        {
            this.field = field;

            focused_listener = (obs, oldval, newval) ->
            {
                menu.hide();
                if (newval)
                    current_field = field;
            };

            text_listener = (obs, oldval, newval) ->
            {
                if (field.isFocused())
                {
                    if (updater != null)
                        updater.requestEntries(field.getText());
                    if (!menu.isShowing())
                        menu.show(field, Side.BOTTOM, 0, 0);
                }
            };

            submit_handler = (event) ->
            {
                if (event.getCode() == KeyCode.ENTER)
                    updateHistory(field.getText());
            };

            field.focusedProperty().addListener(focused_listener);
            field.addEventHandler(KeyEvent.KEY_RELEASED, submit_handler);
            field.textProperty().addListener(text_listener);
        }

        protected void unbind()
        {
            field.textProperty().removeListener(text_listener);
            field.removeEventHandler(KeyEvent.KEY_RELEASED, submit_handler);
            field.focusedProperty().removeListener(focused_listener);
        }
    }

    /** One result received from completion service */
    private class Result
    {
        // Category, for example "History"
        private final String category;
        // Entries for that category
        private final List<String> results;
        // Priority of that category within the results
        final int priority;

        protected Result(final String category, final List<String> results, final int priority)
        {
            this.category = category;
            this.results = new ArrayList<>(results);
            this.priority = priority;
        }

        @Override
        public String toString()
        {
            return category;
        }
    }

    // The AutoCompleteService provides results with a somewhat
    // strange 'index' or priority.
    // It is supposed to order the results, but it can change
    // over time, i.e. might receive results for "History"
    // and index 0 but then one result with "History" and index 1.
    //
    // -> Identify results by label ("History"),
    //    and present them ordered by priority.
    //
    // Might contemplate a 'Set' with label as key,
    // then sort entries by priority.
    // But since there are only very few entries (History, sim:// PVs, local lookup, ..),
    // a plain list works fine
    private final ArrayList<Result> all_results = new ArrayList<>();

    public AutocompleteMenu()
    {
        // The drop-down menu which lists suggestions happens
        // to capture ENTER keys.
        //
        // When user types PV name into current_field and presses ENTER,
        // the menu captures that ENTER key and uses it to hide the menu.
        // --> Need to send ENTER down to current_field.
        //
        // If user selects one of the suggested menu items
        // and presses enter, menu item will update the current_field
        // --> Need to send ENTER down to current_field _after_
        //     the menu item updated current_field.
        menu.addEventFilter(KeyEvent.KEY_PRESSED, event ->
        {
            if (event.getCode() == KeyCode.ENTER && current_field != null)
                Platform.runLater(() ->
                {
                    if (current_field != null)
                        Event.fireEvent(current_field, event);
                });
        });
    }

    /**
     * Attach a field to the menu (add a field to the menu's list of monitored
     * controls)
     *
     * @param field Control to add
     */
    public void attachField(final TextInputControl field)
    {
        fields.add(new ControlWrapper(field));
    }

    /**
     * Remove the auto-completed field from the menu's list of monitored
     * controls
     */
    public void removeField(final TextInputControl control)
    {
        if (current_field != null && current_field.equals(control))
        {
            menu.hide();
            current_field = null;
        }
        synchronized (fields)
        {
            for (ControlWrapper cw : fields)
            {
                if (cw.field.equals(control))
                {
                    cw.unbind();
                    fields.remove(cw);
                    break;
                }
            }
        }
    }

    public void removeFields()
    {
        menu.hide();
        current_field = null;
        synchronized (fields)
        {
            for (ControlWrapper cw : fields)
            {
                cw.unbind();
                fields.remove(cw);
            }
        }
    }

    /**
     * Set updater interface which is used to update the menu as text changes or
     * values are submitted.
     *
     * @param results_updater
     */
    public void setUpdater(AutocompleteMenuUpdater results_updater)
    {
        updater = results_updater;
    }

    /**
     * Set the results for the provider with the given label at the given index.
     *
     * @param category Label for results provider or category
     * @param results List of results to be shown
     * @param index Expected index (with respect to labels) of results
     */
    public void setResults(final String category, final List<String> results, final int priority)
    {
        if (category == null)
            return;

        final List<MenuItem> items = new ArrayList<>();
        synchronized (all_results)
        {
            // Merge new results: Remove existing info for that category, add new, sort by priority
            all_results.removeIf(result -> result.category.equals(category));
            all_results.add(new Result(category, results, priority));
            all_results.sort((a, b) -> a.priority - b.priority);
            // Create menu with all the known results
            for (Result result : all_results)
            {
                if (result.results.isEmpty())
                    continue;
                items.add(createHeaderItem(result.category));
                for (String item : result.results)
                    items.add(createMenuItem(item));
            }
        }

        // Must make changes to JavaFX ContextMenu object from JavaFX thread
        Platform.runLater(() ->  menu.getItems().setAll(items));
    }

    /**
     * Add the given history to the entry (uses AutocompleteMenuUpdater if
     * non-null).
     *
     * @param entry
     */
    public void updateHistory(final String entry)
    {
        if (updater != null)
            updater.updateHistory(entry);
        else
        {
            Platform.runLater(() ->
            {
                // add entry to top of menu items (for the particular autocomplete menu instance)
                // (Currently, there are two instances of this class in the editor: one for the inline editor, one for the palette)
                final List<MenuItem> items = menu.getItems();
                // remove entry if present, to avoid duplication
                items.removeIf((item) -> item.getText().equals(entry));
                items.add(0, createMenuItem(entry));
            });
        }
    }

    private final CustomMenuItem createHeaderItem(final String header)
    {
        final CustomMenuItem item = new CustomMenuItem(new Text(header), false);
        item.getStyleClass().add("ac-menu-label");
        item.setHideOnClick(false);
        item.setMnemonicParsing(false);
        return item;
    }

    private final MenuItem createMenuItem(final String text)
    {
        final MenuItem item = new MenuItem(text);
        item.setOnAction((event) ->
        {
            if (current_field == null)
                return;
            current_field.setText(text);
            // Menu's key_pressed handler will send ENTER on to current_field
        });
        item.getStyleClass().add("ac-menu-item");
        item.setMnemonicParsing(false);
        return item;
    }
}
