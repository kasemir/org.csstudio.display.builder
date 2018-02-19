/*******************************************************************************
 * Copyright (c) 2015-2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import org.csstudio.display.builder.rcp.RuntimeViewPart;
import org.csstudio.display.builder.representation.javafx.JFXRepresentation;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/** Toolbar contribution for zooming the display
 *  @author Kay Kasemir
 */
public class ZoomAction extends ContributionItem
{
    private final RuntimeViewPart part;

    /** @param part RuntimeViewPart that will be zoomed */
    public ZoomAction(final RuntimeViewPart part)
    {
        this.part = part;
    }

    @Override
    public void fill(final ToolBar parent, final int index)
    {
        // Create tool item w/ combo box
        final ToolItem item = new ToolItem(parent, SWT.SEPARATOR, index);
        final Combo combo = createCombo(parent, item);

        part.getRepresentation().setZoomListener(txt -> combo.setText(txt));
        // Auto-size combo, then use that for tool item
        combo.pack();
        item.setWidth(combo.getSize().x);
        item.setControl(combo);
    }

    private Combo createCombo(final ToolBar parent, final ToolItem item)
    {
        final Combo combo = new Combo(parent, SWT.DROP_DOWN);
        for (String level : JFXRepresentation.ZOOM_LEVELS)
            combo.add(level);
        combo.setText(JFXRepresentation.DEFAULT_ZOOM_LEVEL);

        combo.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {   // User selected a predefined zoom level
                widgetDefaultSelected(e);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {   // User entered a zoom value
                combo.setText(part.getRepresentation().requestZoom(combo.getText()));
            }
        });

        return combo;
    }
}
