/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.javafx;

import java.util.prefs.Preferences;

/**
 * Helper class for {@link Preferences.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 14 Jun 2018
 */
public class PreferencesHelper {

    private PreferencesHelper ( ) {
    }

    public static Preferences userNodeForClass ( Class<?> clazz ) {
        return Preferences.userNodeForPackage(clazz).node(clazz.getSimpleName());
    }

}
