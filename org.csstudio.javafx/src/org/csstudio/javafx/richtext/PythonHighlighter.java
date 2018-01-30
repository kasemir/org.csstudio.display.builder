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
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 29 Jan 2018
 */
public class PythonHighlighter implements LanguageHighlighter {

    private static final String[] KEYWORDS = new String[] {
        "False", "None", "True", "and", "as",
        "assert", "break", "class", "continue", "def",
        "del", "elif", "else", "except", "finally",
        "for", "from", "global", "if", "import",
        "in", "is", "lambda", "nonlocal", "not",
        "or", "pass", "raise", "return", "try",
        "while", "with", "yield"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String COLON_PATTERN = "\\:";
    private static final String STRING_PATTERN = "\"\"\"(.|\\R)*?\"\"\"" + "|" + "'''(.|\\R)*?'''" + "|" + "\"([^\"\\\\]|\\\\.)*\"" + "|" + "\'([^\'\\\\]|\\\\.)*\'";
    private static final String COMMENT_PATTERN = "#[^\n]*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<COLON>" + COLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    @Override
    public StyleSpans<Collection<String>> computeHighlighting( String text ) {

        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while ( matcher.find() ) {

            String styleClass = matcher.group("KEYWORD") != null ? "keyword"
                              : matcher.group("PAREN") != null ? "paren"
                              : matcher.group("BRACE") != null ? "brace"
                              : matcher.group("BRACKET") != null ? "bracket"
                              : matcher.group("COLON") != null ? "colon"
                              : matcher.group("STRING") != null ? "string"
                                      : matcher.group("COMMENT") != null ? "comment"
                              : null;

            //  It should never happen.
            assert styleClass != null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());

            lastKwEnd = matcher.end();

        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);

        return spansBuilder.create();

    }

    @Override
    public void installStylesheets( CodeArea area ) {
        area.getStylesheets().add(PythonHighlighter.class.getResource("python-keywords.css").toExternalForm());
    }

    @Override
    public void uninstallStylesheets( CodeArea area ) {
        area.getStylesheets().remove(PythonHighlighter.class.getResource("python-keywords.css").toExternalForm());
    }

}
