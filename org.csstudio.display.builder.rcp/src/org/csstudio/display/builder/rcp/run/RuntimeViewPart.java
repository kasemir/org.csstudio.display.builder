/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Group;
import javafx.scene.Scene;

/** Part that hosts display builder runtime
 *
 *  <p>Hosts FXCanvas in SWT
 *
 *  @author Kay Kasemir
 */
public class RuntimeViewPart extends ViewPart
{
	// FXViewPart could save a tiny bit code, but this may allow more control.
	// e4view would allow E4-like POJO, but unclear how representation
	// would then best find the newly created RuntimeViewPart to set its input etc.
	// --> Using E3 ViewPart
    public final static String ID = "org.csstudio.display.builder.rcp.run.RuntimeViewPart";

    @Override
    public void createPartControl(final Composite parent)
    {
        final FXCanvas fx_canvas = new FXCanvas(parent, SWT.NONE);
        final Group root = new Group();

        new JFXDisplayRuntime().representModel(root, null);

        final Scene scene = new Scene(root);
        fx_canvas.setScene(scene);
    }

    @Override
    public void setFocus()
    {
    }
}
