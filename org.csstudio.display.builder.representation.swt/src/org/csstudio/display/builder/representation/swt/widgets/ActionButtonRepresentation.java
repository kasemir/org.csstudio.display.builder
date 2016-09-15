/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.swt.widgets;

import java.util.List;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/** Creates SWT item for model widget
 *  @author Kay Kasemir
 */
public class ActionButtonRepresentation extends SWTBaseRepresentation<Button, ActionButtonWidget>
{
    private final DirtyFlag dirty_representation = new DirtyFlag();

    @Override
    protected Button createSWTControl(final Composite parent) throws Exception
    {
        final List<ActionInfo> actions = model_widget.propActions().getValue();
        final Button button = new Button(parent, SWT.PUSH);

        // Use basic button for single action
        if (actions.size() == 1)
        {
            final ActionInfo the_action = actions.get(0);
            button.addSelectionListener(new SelectionAdapter()
            {
                @Override
                public void widgetSelected(final SelectionEvent event)
                {
                    handleAction(the_action, event.stateMask);
                }
            });
        }
        else
        {   // Add context menu if there are multiple actions
            final Menu menu = new Menu(button);
            for (final ActionInfo action : actions)
            {
                final MenuItem item = new MenuItem(menu, SWT.PUSH);
                item.setText(action.getDescription());
                final ActionInfo the_action = action;
                item.addSelectionListener(new SelectionAdapter()
                {
                    @Override
                    public void widgetSelected(final SelectionEvent event)
                    {
                        handleAction(the_action, event.stateMask);
                    }
                });
            }
            button.setMenu(menu);
        }
        return button;
    }

    /** @param action Action that the user invoked
     *  @param state_mask Keyboard modifiers
     */
    private void handleAction(ActionInfo action, final int state_mask)
    {
        if (action instanceof OpenDisplayActionInfo)
        {
            final OpenDisplayActionInfo orig = (OpenDisplayActionInfo) action;
            if ((state_mask & SWT.CONTROL) != 0)
                action = new OpenDisplayActionInfo(orig.getDescription(), orig.getFile(),
                                                   orig.getMacros(), OpenDisplayActionInfo.Target.TAB);
            if ((state_mask & SWT.SHIFT) != 0)
                action = new OpenDisplayActionInfo(orig.getDescription(), orig.getFile(),
                                                   orig.getMacros(), OpenDisplayActionInfo.Target.WINDOW);
        }
        toolkit.fireAction(model_widget, action);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propText().addUntypedPropertyListener(this::representationChanged);
    }

    private void representationChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_representation.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_representation.checkAndClear())
            control.setText(model_widget.propText().getValue());
    }
}
