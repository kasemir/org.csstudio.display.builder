/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt;

import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.LEDWidget;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.model.widgets.RectangleWidget;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.ActionButtonRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.EmbeddedDisplayRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.GroupRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.LEDRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.LabelRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.RectangleRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.TextUpdateRepresentation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/** Represent model items in SWT toolkit
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SWTRepresentation extends ToolkitRepresentation<Composite, Control>
{
    private static final String ACTIVE_MODEL = "_active_model";
    private final Display display;

    public SWTRepresentation(final Display display)
    {
        this.display = display;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void initialize()
    {
        register(ActionButtonWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation) new ActionButtonRepresentation());
        register(EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation) new EmbeddedDisplayRepresentation());
        register(GroupWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation) new GroupRepresentation());
        register(LabelWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation) new LabelRepresentation());
        register(LEDWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation) new LEDRepresentation());
        register(RectangleWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation) new RectangleRepresentation());
        register(TextUpdateWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation) new TextUpdateRepresentation());
    }

    @Override
    public Composite openNewWindow(final DisplayModel model, final Consumer<DisplayModel> close_request_handler)
    {
        final Shell shell = new Shell(display);
        shell.setText(model.widgetName().getValue());
        shell.setSize(model.positionWidth().getValue(),
                      model.positionHeight().getValue());
        shell.open();
        shell.addDisposeListener((e) -> handleClose(shell, close_request_handler));
        return shell;
    }

    @Override
    public void setBackground(final WidgetColor color)
    {
        // Not implemented
    }

    @Override
    public void representModel(final Composite shell, final DisplayModel model)
            throws Exception
    {
        super.representModel(shell, model);
        if (! (shell instanceof Shell))
            throw new IllegalStateException("Expected Shell, got " + shell);
        shell.setData(ACTIVE_MODEL, model);
    }

    @Override
    public Composite disposeRepresentation(DisplayModel model)
    {
        final Composite shell = super.disposeRepresentation(model);
        if (! (shell instanceof Shell))
            throw new IllegalStateException("Expected Shell, got " + shell);
        shell.setData(ACTIVE_MODEL, null);
        return shell;
    }

    private void handleClose(final Shell shell, final Consumer<DisplayModel> close_request_handler)
    {
        final DisplayModel model = (DisplayModel) shell.getData(ACTIVE_MODEL);
        try
        {
            if (model != null)
                close_request_handler.accept(model);
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Close request handler failed", ex);
        }
    }

    @Override
    public void execute(final Runnable command)
    {
        display.asyncExec(command);
    }

    /** Convert model color into JFX color
     *  @param color {@link WidgetColor}
     *  @return {@link Color}
     */
    public Color convert(final WidgetColor color)
    {
        return new Color(display, color.getRed(), color.getGreen(), color.getBlue());
    }
}
