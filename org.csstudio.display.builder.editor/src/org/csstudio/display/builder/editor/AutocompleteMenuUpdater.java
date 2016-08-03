package org.csstudio.display.builder.editor;

/**
 * Generic updater interface for (JavaFX-based) {@link AutocompleteMenu}.
 * 
 * @author Amanda Carpenter
 *
 */
public interface AutocompleteMenuUpdater
{
    /**
     * Request autocomplete entries for the given content
     * 
     * @param content Content for which to request autocomplete entries
     */
    public void requestEntries(String content);

    public void updateHistory(String entry);
}
