package org.csstudio.display.builder.representation.javafx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

/**
 * Creates a menu for use with auto-completion. Fields which edit
 * auto-completeable properties should use attachField on the related
 * TextInputControl to attach the menu, and removeField to unregister listeners.
 * For meaningful auto-completion, an {@link AutocompleteMenuUpdater} should be
 * given which implements methods to request result entries for a given value,
 * calling setResults as results arrive, and to update the history of the menu.
 * 
 * @author Amanda Carpenter
 *
 */
public class AutocompleteMenu
{
    private final ContextMenu menu = new ContextMenu();
    private AutocompleteMenuUpdater updater = null;
    private List<ControlWrapper> fields = new ArrayList<ControlWrapper>();
    private TextInputControl current_field = null;

    private class ControlWrapper
    {
        private TextInputControl field = null;

        private final ChangeListener<Boolean> focused_listener = (obs, oldval, newval) ->
        {
            menu.hide();
            if (newval)
                current_field = field;
        };
        private final ChangeListener<String> text_listener = (obs, oldval, newval) ->
        {
            if (field.isFocused())
            {
                //TODO: could make use of cursor position for more intelligent suggestions
                if (updater != null)
                    updater.requestEntries(field.getText());
                if (!menu.isShowing())
                    menu.show(field, Side.BOTTOM, 0, 0);
            }
        };
        private final EventHandler<KeyEvent> submit_handler = (event) ->
        {
            if (event.getCode().equals(KeyCode.ENTER))
            {
                updateHistory(field.getText());
            }
        };

        ControlWrapper(TextInputControl field)
        {
            this.field = field;
            field.focusedProperty().addListener(focused_listener);
            field.addEventHandler(KeyEvent.KEY_RELEASED, submit_handler);
            field.textProperty().addListener(text_listener);
        }

        protected void unbind()
        {
            field.textProperty().removeListener(text_listener);
            field.focusedProperty().removeListener(focused_listener);
            field.removeEventHandler(KeyEvent.KEY_RELEASED, submit_handler);
        }
    }

    private final List<Result> result_list = new LinkedList<Result>();

    private class Result
    {
        private CustomMenuItem label;
        private List<String> results;
        protected int expected; //expected index of results (in result_list)

        protected Result(String provider_label, List<String> results, int expected)
        {
            label = createHeaderItem(provider_label);
            this.expected = expected;
            setResults(results);
        }

        protected void addItemsTo(List<MenuItem> items)
        {
            items.add(label);
            for (String result : results)
                items.add(createMenuItem(result));
        }

        protected void setResults(List<String> results)
        {
            this.results = new ArrayList<String>(results);
        }

        protected boolean textIs(String str)
        {
            return ((Text) label.getContent()).getText().equals(str);
        }

        @SuppressWarnings("nls")
        @Override
        public String toString()
        {
            return ((Text) label.getContent()).getText() + " at " + expected + " (" + results.size() + "): "
                    + results.toString();
        }
    }

    /**
     * Attach a field to the menu (add a field to the menu's list of monitored
     * controls)
     * 
     * @param field Control to add
     */
    public void attachField(TextInputControl field)
    {
        fields.add(new ControlWrapper(field));
    }

    /**
     * Remove the auto-completed field from the menu's list of monitored
     * controls
     */
    public void removeField(TextInputControl control)
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

    public void setResults(final String label, final List<String> results)
    {
        setResults(label, results, 0);
    }

    /**
     * Set the results for the provider with the given label at the given index.
     * 
     * @param label Label for results provider or category
     * @param results List of results to be shown
     * @param index Expected index (with respect to labels) of results
     */
    public void setResults(final String label, final List<String> results, int index)
    {
        if (label == null)
            return;

        //System.out.println("results for " + label + " at " + index);

        final List<MenuItem> items = new LinkedList<MenuItem>();

        synchronized (result_list)
        {
            final ListIterator<Result> it = result_list.listIterator();
            Result result;
            if (it.hasNext())
            {
                result = it.next();
                while (result.expected < index)
                {
                    if (result.textIs(label))
                        it.remove();
                    else
                        result.addItemsTo(items);
                    if (it.hasNext())
                        result = it.next();
                    else
                        break;
                }
                if (result.expected >= index && it.hasPrevious())
                    it.previous();
                else
                    result.addItemsTo(items);
            }
            result = new Result(label, results, index);
            it.add(result);
            result.addItemsTo(items);
            while (it.hasNext())
            {
                result = it.next();
                if (result.expected <= index)
                    result.expected++;
                if (result.expected >= index)
                    index++;
                if (result.textIs(label))
                    it.remove();
                else
                    result.addItemsTo(items);
            }
        }

        //for (Result result : result_list)
        //System.out.println(result);

        //Must make changes to JavaFX ContextMenu object from JavaFX thread
        Platform.runLater(() -> menu.getItems().setAll(items));
    }

    /**
     * Add the given history to the entry (uses AutocompleteMenuUpdater if
     * non-null).
     * 
     * @param entry
     */
    public void updateHistory(String entry)
    {
        if (updater != null)
            updater.updateHistory(entry);
        else
        { //add entry to top of menu items (for the particular autocomplete menu instance)
          //(Currently, there are two instances of this class in the editor: one for the inline editor, one for the palette)
            List<MenuItem> items = menu.getItems();
            //remove entry if present, to avoid duplication
            items.removeIf((item) -> item.getText().equals(entry));
            items.add(0, createMenuItem(entry));
        }
    }

    private final CustomMenuItem createHeaderItem(String header)
    {
        final CustomMenuItem item = new CustomMenuItem(new Text(header), false);
        item.getStyleClass().add("ac-menu-label"); //$NON-NLS-1$
        item.setHideOnClick(false);
        item.setMnemonicParsing(false);
        return item;
    }

    private final MenuItem createMenuItem(String text)
    {
        final MenuItem item = new MenuItem(text);
        item.setOnAction((event) ->
        {
            if (current_field != null)
                current_field.setText(text);
        });
        item.getStyleClass().add("ac-menu-item"); //$NON-NLS-1$
        item.setMnemonicParsing(false);
        return item;
    }
}
