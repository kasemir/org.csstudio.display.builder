/*******************************************************************************
 * Copyright (c) 2015-2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp;

import java.util.function.Supplier;

import org.eclipse.fx.ui.workbench3.FXViewPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import javafx.scene.Scene;

/** Helper for showing JavaFX Scene in SWT
 *
 *  <p>Basic idea is to use the SWT FXCanvas to host a JFX Scene:
 *  <pre>
 *  FXCanvas canvas = new FXCanvas(parent, SWT.NONE);
 *  canvas.setScene(scene);
 *  </pre>
 *
 *  <p>FXCanvas is in jre/lib/jfxswt.jar (earlier jre/lib/ext).
 *  Maven will find it, but Eclipse IDE requires adding the jar file
 *  to the classpath.
 *  That would be easy if classpath could use {JRE_LIB} variable,
 *  but need to provide full path which differs for each development setup.
 *
 *  <p>FXViewPart, however, can be reached via plain plugin dependency
 *  on org.eclupse.fx.ui.workbench3, so using that to create the FXCanvas.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFX_SWT_Wrapper
{
    private Scene scene;

    // Implementation detail:
    //
    // In an SWT application, creating the FXCanvas initializes JFX
    // and arranges for both SWT and JFX events to be handled
    // by the same UI thread.
    // This requires that FXCanvas is created _before_ JFX is accessed,
    // so constructor receives a Scene supplied (which then later creates the Scene)
    // instead of the actual Scene right away.

    /** @param scene JavaFX Scene where cursor is monitored
     *  @param display SWT Display where the scene's cursor is set
     */
    public JFX_SWT_Wrapper(final Composite parent, final Supplier<Scene> scene_creator)
    {
        final FXViewPart part = new FXViewPart()
        {
            @Override
            protected Scene createFxScene()
            {
                scene = scene_creator.get();
                return scene;
            }

            @Override
            protected void setFxFocus()
            {
                JFX_SWT_Wrapper.this.setFxFocus(scene);
            }
        };
        // Not using the part as such,
        // only invoking its createPartControl()
        // which creates an FXCanvas under the 'parent',
        // sets its scene and focus.
        part.createPartControl(parent);
    }

    /** @param parent Parent to which an FXCanvas has been added
     *  @return FXCanvas
     *  @throws IllegalStateException if not found
     */
    public static Control findFXCanvas(final Composite parent)
    {
        for (Control child : parent.getChildren())
            if (child.getClass().getName().contains("FXCanvas"))
                return child;
        throw new IllegalStateException("Cannot identify FXCanvas");
    }

    /** @return Scene that was created and attached to FXCanvas */
    public Scene getScene()
    {
        return scene;
    }

    protected void setFxFocus(final Scene scene)
    {
        // NOP
    }
}
