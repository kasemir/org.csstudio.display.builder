/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
public class EmbeddedDisplayRepresentation extends SWTBaseRepresentation<Composite, EmbeddedDisplayWidget>
{
    // No border, just a container for the embedded display

    /** Inner composite that holds child widgets */
    private Composite inner;

    @Override
    protected Composite createSWTControl(final Composite parent) throws Exception
    {
        inner = new Composite(parent, SWT.NO_FOCUS);
        model_widget.setUserData(EmbeddedDisplayWidget.USER_DATA_EMBEDDED_DISPLAY_CONTAINER, inner);

        return inner;
    }

    @Override
    protected Composite getChildParent(final Composite parent)
    {
        return inner;
    }
}
