/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Group;
import javafx.scene.Scene;

/** Part that hosts display builder runtime
 *
 *  <p>Can be used as E4 POJO or E3 'e4view'.
 *  Usage with 'e4view' requires "Import-Package: javax.annotation"
 *  in MANIFEST.MF
 *
 *  <p>Hosts FXCanvas in SWT
 *
 *  @author Kay Kasemir
 */
public class RuntimeViewPart
{
    public final static String ID = "org.csstudio.display.builder.rcp.run.RuntimeViewPart";

    @PostConstruct
    public void createPartControl(final Composite parent)
    {
        final FXCanvas fx_canvas = new FXCanvas(parent, SWT.NONE);
        final Group root = new Group();

        new JFXDisplayRuntime().representModel(root, null);

        final Scene scene = new Scene(root);
        fx_canvas.setScene(scene);
    }

    @Focus
    void setFocus()
    {
    }
}
