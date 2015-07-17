/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt;

import java.util.function.Predicate;
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
import org.csstudio.display.builder.representation.swt.widgets.ActionButtonRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.EmbeddedDisplayRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.GroupRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.LEDRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.LabelRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.RectangleRepresentation;
import org.csstudio.display.builder.representation.swt.widgets.TextUpdateRepresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/** Represent model items in SWT toolkit
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SWTRepresentation extends ToolkitRepresentation<Composite, Control>
{
    private final Display display;

    public SWTRepresentation(final Display display)
    {
        this.display = display;

        // TODO Load available widget representation from registry
        register(ActionButtonWidget.class, ActionButtonRepresentation.class);
        register(EmbeddedDisplayWidget.class, EmbeddedDisplayRepresentation.class);
        register(GroupWidget.class, GroupRepresentation.class);
        register(LabelWidget.class, LabelRepresentation.class);
        register(LEDWidget.class, LEDRepresentation.class);
        register(RectangleWidget.class, RectangleRepresentation.class);
        register(TextUpdateWidget.class, TextUpdateRepresentation.class);
    }

    @Override
    public Composite openNewWindow(final DisplayModel model, final Predicate<DisplayModel> close_request_handler)
    {
        final Shell shell = new Shell(display);
        shell.setText(model.widgetName().getValue());
        shell.setSize(model.positionWidth().getValue(),
                      model.positionHeight().getValue());
        shell.open();

        shell.addListener(SWT.CLOSE, new Listener()
        {
            @Override
            public void handleEvent(final Event event)
            {
                try
                {
                    event.doit = close_request_handler.test(model);
                }
                catch (final Exception ex)
                {
                    logger.log(Level.WARNING, "Close request handler failed", ex);
                }
            }
        });
        return shell;
    }

    @Override
    public void execute(final Runnable command)
    {
        display.asyncExec(command);
    }

    @Override
    public Composite disposeRepresentation(final DisplayModel model)
    {
        final Composite parent = model.getUserData(DisplayModel.USER_DATA_TOOLKIT_PARENT);
        for (final Control child : parent.getChildren())
            child.dispose();
        return parent;
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
