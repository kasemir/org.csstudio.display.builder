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
    private Long currentId;
    private int ignored_results;

    public AutoCompleteUpdater(AutocompleteMenu menu)
    {
        this.menu = menu;
    }

    @Override
    public void requestEntries(String content)
    {
        AutoCompleteService acs = AutoCompleteService.getInstance();
        currentId = System.currentTimeMillis();
        acs.get(currentId, AutoCompleteType.PV, content,
                (Long uniqueId, Integer index, AutoCompleteResult result) ->
                {
                    if (uniqueId != null && uniqueId.equals(currentId))
                        Platform.runLater(() ->
                        {
                            menu.setResults(result.getProvider(), result.getProposalsAsString(),
                                            index != null ? index : 0);
                        });
                    else if (/* uniqueId < currentId && */ ++ignored_results > 15)
                    {
                        ignored_results = 0;
                        Platform.runLater(() ->
                        {
                            menu.setResults(result.getProvider(), result.getProposalsAsString(),
                                    index != null ? index : 0);
                        });
                    }
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
