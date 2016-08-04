package org.csstudio.display.builder.editor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;

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
        //Hide menu if not in focus in case field value changes without typing (i.e., if
        //change in widget's inline editor causes property panel field to change)
        if (field == null | !field.isFocused())
        {
            menu.hide();
            return;
        }
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

    private final List<Result> result_list = new ArrayList<Result>();

    private class Result
    {
        private CustomMenuItem header;
        private List<String> results;
        protected int expected; //expected index

        protected Result(String header, List<String> results, int expected)
        {
            this.header = createHeaderItem(header);
            this.expected = expected;
            setResults(results);
        }

        protected void addItemsTo(List<MenuItem> menu)
        {
            menu.add(header);
            for (String result : results)
                menu.add(createMenuItem(result));
        }

        protected void setResults(List<String> results)
        {
            this.results = new ArrayList<String>(results);
        }

        protected boolean eq_str(String str)
        {
            return ((Text) header.getContent()).getText().equals(str);
        }

        @Override
        public String toString()
        {
            return ((Text) header.getContent()).getText() + "@" + expected + " (" + results.size() + "): "
                    + results.toString();
        }
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

    public void setUpdater(AutocompleteMenuUpdater results_updater)
    {
        updater = results_updater;
    }

    public void setResults(final String label, final List<String> results)
    {
        setResults(label, results, 0);
    }

    public void setResults(final String label, final List<String> results, int index)
    {
        if (label == null)
            return;
        System.out.println("results for " + label + " @" + index + " (" + results.size() + ")");

        final List<MenuItem> items = new LinkedList<MenuItem>();

        //TODO: see if this can be improved
        synchronized (result_list)
        {
            final ListIterator<Result> it = result_list.listIterator();
            Result result;
            if (it.hasNext())
            {
                result = it.next();
                while (result.expected < index)
                {
                    if (result.eq_str(label))
                    {
                        it.remove();
                    }
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
                if (result.expected > index)
                    index++;
                if (result.expected == index)
                    result.expected++;
                if (result.eq_str(label))
                    it.remove();
                else
                    result.addItemsTo(items);
            }
        }

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

    private final CustomMenuItem createHeaderItem(String header)
    {
        //TODO style with class using stylesheet (where is sheet?)
        final CustomMenuItem item = new CustomMenuItem(new Text(header), false);
        item.getStyleClass().add("ac-menu-label");
        item.setHideOnClick(false);
        return item;
    }

    private final MenuItem createMenuItem(String text)
    {
        final MenuItem item = new MenuItem(text);
        item.addEventHandler(ActionEvent.ACTION, (event) ->
        {
            field.setText(item.getText());
        });
        item.getStyleClass().add("ac-menu-item");
        return item;
    }
}
