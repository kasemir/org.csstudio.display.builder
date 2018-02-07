/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.javafx;


import static org.csstudio.javafx.Activator.logger;

import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.csstudio.javafx.richtext.JavaHighlighter;
import org.csstudio.javafx.richtext.JavaScriptHighlighter;
import org.csstudio.javafx.richtext.LanguageHighlighter;
import org.csstudio.javafx.richtext.PythonHighlighter;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;


/**
 * Dialog for entering multi-line syntax highlighted text.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 30 Jan 2018
 * @see MultiLineInputDialog
 */
public class SyntaxHighlightedMultiLineInputDialog extends Dialog<String> {

    public enum Language {
        Java,
        JavaScript,
        Python
    }

    private final LanguageHighlighter highlighter;
    private final CodeArea codeArea;

    /**
     * @param initialText Initial text.
     * @param language The language this dialog will use to highlight syntax.
     */
    public SyntaxHighlightedMultiLineInputDialog ( final String initialText, final Language language ) {

        codeArea = new CodeArea();

        switch ( language ) {
            case JavaScript:
                highlighter = new JavaScriptHighlighter();
                break;
            case Python:
                highlighter = new PythonHighlighter();
                break;
            case Java:
            default:
                highlighter = new JavaHighlighter();
                break;
        }

        highlighter.installStylesheets(codeArea);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> codeArea.setStyleSpans(0, highlighter.computeHighlighting(codeArea.getText())));
        codeArea.replaceText(0, 0, initialText);

        getDialogPane().setContent(new BorderPane(new VirtualizedScrollPane<>(codeArea)));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Preferences readPref = Preferences.userNodeForPackage(SyntaxHighlightedMultiLineInputDialog.class);
        double prefWidth = readPref.getDouble("dialog.width", 600);
        double prefHeight = readPref.getDouble("dialog.height", 300);

        getDialogPane().setPrefSize(prefWidth, prefHeight);

        setResizable(true);
        setResultConverter(button -> ( button == ButtonType.OK ) ? codeArea.getText() : null);
        setOnHidden(event -> {

            final Preferences writePref = Preferences.userNodeForPackage(SyntaxHighlightedMultiLineInputDialog.class);

            writePref.putDouble("dialog.width", getDialogPane().getWidth());
            writePref.putDouble("dialog.height", getDialogPane().getHeight());

            try {
                writePref.flush();
            } catch ( BackingStoreException ex ) {
                logger.log(Level.WARNING, "Unable to flush preferences", ex);
            }

        });

    }

    /**
     * @param parent Parent node, dialog will be positioned relative to it.
     * @param initialText Initial text.
     * @param language The language this dialog will use to highlight syntax.
     */
    public SyntaxHighlightedMultiLineInputDialog ( final Node parent, final String initialText, final Language language ) {

        this(initialText, language);
        initOwner(parent.getScene().getWindow());

        final Bounds bounds = parent.localToScreen(parent.getBoundsInLocal());

        setX(bounds.getMinX());
        setY(bounds.getMinY());

    }

    /**
     * @param parent Parent node, dialog will be positioned relative to it.
     * @param initialText Initial text.
     * @param language The language this dialog will use to highlight syntax.
     * @param editable {@code true} if the dialog allows editing, {@code false otherwise}.
     */
    public SyntaxHighlightedMultiLineInputDialog ( final Node parent, final String initialText, final Language language, boolean editable ) {

        this(parent, initialText, language);

        codeArea.setEditable(editable);

    }

    /**
     * @param pixels Suggested height of text in pixels .
     */
    public void setTextHeight ( final double pixels ) {
        codeArea.setPrefHeight(pixels);
    }

    // TODO Catch/consume 'escape'
    // If ESC key is pressed while editing the text,
    // the dialog closes, not returning a value.
    // Fine.
    // But the ESC passes on to whoever called the dialog..
}
