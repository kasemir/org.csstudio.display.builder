package org.csstudio.display.builder.editor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AutocompleteMenu
{
    private final ContextMenu menu = new ContextMenu();
    private AutocompleteMenuUpdater updater = null;
    private TextInputControl field = null;

    private final ChangeListener<Boolean> focused_listener = (obs, oldval, newval) ->
    {
        menu.hide();
    };
    private final ChangeListener<String> text_listener = (obs, oldval, newval) ->
    {
        //TODO conditionally, hide menu

        //TODO make use of cursor position
        if (updater != null)
            updater.requestEntries(field.getText());
        if (!menu.isShowing())
        {
            //TODO fix width
            menu.setMinWidth(field.getWidth());
            menu.show(field, Side.BOTTOM, 0, 0);
        }
    };
    //better to handle submission where field submits
    private final EventHandler<KeyEvent> submit_handler = (event) ->
    {
        try
        {
            if (event.getCode().equals(KeyCode.ENTER))
                updateHistory(field.getText());
        } finally
        {
        }
    };


    public void setField(final TextInputControl field)
    {
        removeField();
        this.field = field;
        if (this.field != null)
        {
            field.textProperty().addListener(text_listener);
            field.focusedProperty().addListener(focused_listener);
            field.addEventHandler(KeyEvent.KEY_PRESSED, submit_handler);
        }
    }

    public void removeField()
    {
        menu.hide();
        if (field != null)
        {
            field.textProperty().removeListener(text_listener);
            field.focusedProperty().removeListener(focused_listener);
            field.removeEventHandler(KeyEvent.KEY_PRESSED, submit_handler);
        }
    }

    public void setUpdater(AutocompleteMenuUpdater results_updater)
    {
        updater = results_updater;
    }

    public void setResults(Collection<String> results)
    {
        //TODO allow "headers"
        List<MenuItem> items = new LinkedList<MenuItem>();
        for (String result : results)
            items.add(createMenuItem(result));
        menu.getItems().setAll(items);
    }

    public void updateHistory(String entry)
    {
        if (updater != null)
        {
            updater.updateHistory(entry);
        }
        else
        { //add entry to top of menu items
            List<MenuItem> items = menu.getItems();
            //remove entry if present, to avoid duplication
            items.removeIf((item) -> item.getText().equals(entry));
            items.add(0, createMenuItem(entry));
        }
    }

    private final MenuItem createMenuItem(String text)
    {
        final MenuItem item = new MenuItem(text);
        item.addEventHandler(ActionEvent.ACTION, (event) ->
        {
            field.setText(item.getText());
        });
        return item;
    }

}
