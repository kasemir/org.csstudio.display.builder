/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorActions;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayText;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentationListener;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionButtonRepresentation extends JFXBaseRepresentation<ButtonBase, ActionButtonWidget>
{
    // Uses a Button if there is only one action,
    // otherwise a MenuButton so that user can select the specific action.
    //
    // These two types were chosen because they share the same ButtonBase base class.
    // ChoiceBox is not derived from onButtonBase, plus it has currently selected 'value',
    // and with action buttons it wouldn't make sense to select one of the actions.
    //
    // Current implementation does not allow changing the actions at runtime.
    // Specifically, if the action count changed between 1 and >1,
    // it won't update between Button and MenuButton.

    private final DirtyFlag dirty_representation = new DirtyFlag();

    /** Optional modifier of the open display 'target */
    private Optional<OpenDisplayActionInfo.Target> target_modifier = Optional.empty();

    public ActionButtonRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                                      final ActionButtonWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    public ButtonBase createJFXNode() throws Exception
    {
        final List<ActionInfo> actions = model_widget.getPropertyValue(behaviorActions);
        final ButtonBase base;
        // Create either single-action button or menu for selecting one out of N actions
        if (actions.size() == 1)
        {
            final Button button = new Button();
            final ActionInfo the_action = actions.get(0);
            button.setOnAction(event -> handleAction(the_action));
            base = button;
        }
        else
        {
            final MenuButton button = new MenuButton();
            for (ActionInfo action : actions)
            {
                final MenuItem item = new MenuItem(action.getDescription());
                final ActionInfo the_action = action;
                item.setOnAction(event -> handleAction(the_action));
                button.getItems().add(item);
            }
            base = button;
        }

        // Monitor keys that modify the OpenDisplayActionInfo.Target
        base.setOnMousePressed((MouseEvent event) ->
        {
            if (event.isControlDown())
                target_modifier = Optional.of(OpenDisplayActionInfo.Target.TAB);
            else if (event.isShiftDown())
                target_modifier = Optional.of(OpenDisplayActionInfo.Target.WINDOW);
            else
                target_modifier = Optional.empty();

            // At least on Linux, a Control-click or Shift-click
            // will not 'arm' the button, so the click is basically ignored.
            // Force the 'arm', so user can Control-click or Shift-click to
            // invoke the button
            if (target_modifier.isPresent())
                base.arm();
        });
        return base;
    }

    /** @param action Action that the user invoked */
    private void handleAction(ActionInfo action)
    {
        if (action instanceof OpenDisplayActionInfo  &&  target_modifier.isPresent())
        {
            final OpenDisplayActionInfo orig = (OpenDisplayActionInfo) action;
            action = new OpenDisplayActionInfo(orig.getDescription(), orig.getFile(), orig.getMacros(), target_modifier.get());
        }

        try
        {
            for (WidgetRepresentationListener listener : getListeners())
                listener.handleAction(model_widget, action);
        }
        catch (Throwable ex)
        {
            Logger.getLogger(getClass().getName())
                .log(Level.WARNING, "Action failure when invoking " + action + " for " + model_widget, ex);
        }
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.addPropertyListener(positionWidth, this::representationChanged);
        model_widget.addPropertyListener(positionHeight, this::representationChanged);
        model_widget.addPropertyListener(displayText, this::representationChanged);
    }

    private void representationChanged(final PropertyChangeEvent event)
    {
        dirty_representation.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_representation.checkAndClear())
        {
            jfx_node.setText(model_widget.getPropertyValue(displayText));
            jfx_node.setPrefSize(model_widget.getPropertyValue(positionWidth).doubleValue(),
                                 model_widget.getPropertyValue(positionHeight).doubleValue());
        }
    }
}
