package org.csstudio.display.builder.editor.rcp;

import java.util.LinkedList;

import org.csstudio.autocomplete.AutoCompleteResult;
import org.csstudio.autocomplete.AutoCompleteService;
import org.csstudio.autocomplete.AutoCompleteType;
import org.csstudio.autocomplete.ui.AutoCompleteUIPlugin;
import org.csstudio.display.builder.editor.AutocompleteMenu;
import org.csstudio.display.builder.editor.AutocompleteMenuUpdater;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import javafx.application.Platform;

/**
 * Implements a Function to get autocomplete suggestions from text input
 * 
 * @author Amanda Carpenter
 *
 */
public class AutoCompleteUpdater implements AutocompleteMenuUpdater
{
    private final AutocompleteMenu menu;

    public AutoCompleteUpdater(AutocompleteMenu menu)
    {
        this.menu = menu;
        //menu given in order to be used in IAutoCompleteResultListener
    }

    @Override
    public void requestEntries(String content)
    {
        AutoCompleteService acs = AutoCompleteService.getInstance();
        acs.get(System.currentTimeMillis(), AutoCompleteType.PV, content,
                (Long uniqueId, Integer index, AutoCompleteResult result) ->
        {
            Platform.runLater(()->
            {
                menu.setResults(result.getProposalsAsString());
            });
        });
    }

    @Override
    public void updateHistory(String entry)
    {
        LinkedList<String> fifo = AutoCompleteUIPlugin.getDefault().getHistory(AutoCompleteType.PV.value());
        if (fifo == null)
            return;
        IPreferencesService service = null;
        try
        {
            service = org.eclipse.core.runtime.Platform.getPreferencesService();
        } finally
        {
        }
        final int size = service != null ? service.getInt(AutoCompleteUIPlugin.PLUGIN_ID, "history_size", 100, null) //$NON-NLS-1$
                : 100;
        if (size == 0)
        {
            fifo.clear();
            return;
        }
        // Remove if present, so that is re-added on top
        int index = -1;
        while ((index = fifo.indexOf(entry)) >= 0)
            fifo.remove(index);

        // Maybe remove oldest, i.e. bottom-most, entry
        while (fifo.size() >= size)
            fifo.removeLast();

        // Add at the top
        fifo.addFirst(entry);
    }
}
