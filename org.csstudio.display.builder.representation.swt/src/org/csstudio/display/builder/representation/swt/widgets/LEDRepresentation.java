/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.LEDWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.swt.SWTRepresentation;
import org.diirt.vtype.VType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
public class LEDRepresentation extends SWTBaseRepresentation<Canvas, LEDWidget>
{
    private final DirtyFlag dirty_content = new DirtyFlag();

    private volatile Color[] colors = new Color[0];
    private volatile Color value_color;

    public LEDRepresentation(final ToolkitRepresentation<Composite, Control> toolkit,
                             final LEDWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    protected Canvas createSWTControl(final Composite parent) throws Exception
    {
        createColors();

        // Unfortunately, the canvas is not transparent..
        final Canvas canvas = new Canvas(parent, SWT.NO_FOCUS | SWT.NO_BACKGROUND | SWT.TRANSPARENT);
        canvas.addPaintListener(this::doPaint);

        canvas.addDisposeListener((e) ->
        {
            for (final Color color : colors)
                color.dispose();
        });
        return canvas;
    }

    private void createColors()
    {
        colors = new Color[]
        {
            ((SWTRepresentation)toolkit).convert(model_widget.offColor().getValue()),
            ((SWTRepresentation)toolkit).convert(model_widget.onColor().getValue())
        };
        value_color = colors[0];
    }

    private void doPaint(final PaintEvent event)
    {
        final GC gc = event.gc;
        final Point size = control.getSize();
        gc.setBackground(value_color);
        gc.fillRoundRectangle(0, 0, size.x-1, size.y-1, size.x, size.y);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
    }

    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        int value_index = VTypeUtil.getValueNumber(new_value).intValue();
        if (value_index < 0)
            value_index = 0;
        if (value_index >= colors.length)
            value_index = colors.length-1;
        value_color = colors[value_index];

        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_content.checkAndClear())
            control.redraw();
    }
}
