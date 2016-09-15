/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import org.csstudio.display.builder.rcp.Messages;
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
@SuppressWarnings("nls")
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
        // Auto-size combo, then use that for tool item
        combo.pack();
        item.setWidth(combo.getSize().x);
        item.setControl(combo);
    }

    private Combo createCombo(final ToolBar parent, final ToolItem item)
    {
        final Combo combo = new Combo(parent, SWT.DROP_DOWN);
        combo.setText("100 %");
        // Values and order of options similar to 'Word' on Mac
        combo.add("200 %");
        combo.add("150 %");
        combo.add("125 %");
        combo.add("100 %");
        combo.add("75 %");
        combo.add("50 %");
        combo.add("25 %");
        combo.add(Messages.Zoom_Width);
        combo.add(Messages.Zoom_Height);
        combo.add(Messages.Zoom_All);

        combo.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {   // User selected a predefined zoom level
                handleZoomRequest(combo);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {   // User entered a zoom value
                handleZoomRequest(combo);
            }
        });

        return combo;
    }

    /** @param combo Combo with requested zoom level */
    private void handleZoomRequest(final Combo combo)
    {
        final String level_spec = combo.getText();
        final double zoom = performZoom(level_spec);
        combo.setText(Math.round(zoom*100) + " %");
    }

    /** @param level_spec "123 %" or Messages.Zoom_*
     *  @return Zoom level actually used
     */
    private double performZoom(final String level_spec)
    {
        double zoom;
        if (level_spec.equalsIgnoreCase(Messages.Zoom_All))
            return part.setZoom(JFXRepresentation.ZOOM_ALL);
        else if (level_spec.equalsIgnoreCase(Messages.Zoom_Width))
            return part.setZoom(JFXRepresentation.ZOOM_WIDTH);
        else if (level_spec.equalsIgnoreCase(Messages.Zoom_Height))
            return part.setZoom(JFXRepresentation.ZOOM_HEIGHT);
        // else: Parse "123 %"
        String number = level_spec.trim();
        if (number.endsWith("%"))
            number = number.substring(0, number.length()-1).trim();
        try
        {
            zoom = Double.parseDouble(number) / 100.0;
        }
        catch (NumberFormatException ex)
        {
            zoom = 1.0;
        }
        return part.setZoom(zoom);
    }
}
