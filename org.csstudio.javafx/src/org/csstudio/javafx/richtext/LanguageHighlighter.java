/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.javafx.richtext;


import java.util.Collection;
import java.util.concurrent.Executor;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;


/**
 * Defines the behavior of a text highlighter used in conjunction with a
 * {@link CodeArea} editor.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 29 Jan 2018
 */
public interface LanguageHighlighter {

    /**
     * Computes the highlighting. This method is called from a java {@link Executor}.
     *
     * @param text The text to be highlighted.
     * @return The highlighting {@link StyleSpans}.
     */
    StyleSpans<Collection<String>> computeHighlighting( String text );

    /**
     * Install the appropriate style sheets to the given text area.
     *
     * @param area
     */
    void installStylesheets ( CodeArea area );

    /**
     * Remove the previously installed style sheets from the given text area.
     *
     * @param area
     */
    void uninstallStylesheets ( CodeArea area );

}
