package org.csstudio.display.builder.editor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;

public class AutocompleteMenu
{
    private final ContextMenu menu = new ContextMenu();
    private Function<String, List<String>> supplier;
    private TextInputControl field = null;

    private final ChangeListener<Boolean> focused_listener = (obs, oldval, newval) ->
    {
        menu.hide();
    };
    private final ChangeListener<String> text_listener = (obs, oldval, newval) ->
    {
        //TODO conditionally, hide menu
        repopulate();
        if (!menu.isShowing())
        {
            //TODO fix width
            menu.setMinWidth(field.getWidth());
            menu.show(field, Side.BOTTOM, 0, 0);
        }
    };

    public AutocompleteMenu()
    {
        supplier = (input) -> Collections.emptyList();
    }

    public void setField(final TextInputControl field)
    {
        removeField();
        this.field = field;
        if (this.field != null)
        {
            field.textProperty().addListener(text_listener);
            field.focusedProperty().addListener(focused_listener);
        }
    }

    public void removeField()
    {
        menu.hide();
        if (field != null)
        {
            field.textProperty().removeListener(text_listener);
            field.focusedProperty().removeListener(focused_listener);
        }
    }

    public void setSupplier(Function<String, List<String>> results_supplier)
    {
        supplier = results_supplier;
    }

    public void repopulate()
    {
        //TODO make use of cursor position
        populate(supplier.apply(field.getText()));
    }

    private void populate(List<String> topresults)
    {
        List<MenuItem> items = new LinkedList<MenuItem>();
        for (String result : topresults)
        {
            final MenuItem item = new MenuItem(result);
            item.addEventHandler(ActionEvent.ACTION, (event) ->
            {
                //TODO use UndoableActionManager
                field.setText(item.getText());
            });
            items.add(item);
        }
        menu.getItems().setAll(items);
    }
}
