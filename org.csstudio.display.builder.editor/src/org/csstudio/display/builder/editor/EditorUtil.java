/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.concurrent.Executor;

import org.csstudio.display.builder.model.util.NamedDaemonPool;

import javafx.scene.Scene;

/** Editor utility
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EditorUtil
{
    private static final Executor executor = NamedDaemonPool.createThreadPool("DisplayEditor");

    /** @return Executor for thread pool meant for editor-related background tasks */
    public static Executor getExecutor()
    {
        return executor;
    }

    public static void setSceneStyle(Scene scene)
    {
        scene.getStylesheets().add(EditorUtil.class.getResource("opieditor.css").toExternalForm());
    }
}
