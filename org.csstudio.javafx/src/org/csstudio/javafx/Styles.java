/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx;

import javafx.scene.Scene;

/** Helper for dealing with styles
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Styles
{
    /** @param scene Scene where style sheet for csstudio is added */
    public static void setSceneStyle(final Scene scene)
    {
        final String css = Styles.class.getResource("csstudio.css").toExternalForm();
        scene.getStylesheets().add(css);
    }
}
