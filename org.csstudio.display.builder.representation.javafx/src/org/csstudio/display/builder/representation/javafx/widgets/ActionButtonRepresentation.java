/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.macros.MacroValueProvider;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.model.properties.StringWidgetProperty;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.Messages;
import org.eclipse.osgi.util.NLS;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Megan Grodowitz
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ActionButtonRepresentation extends RegionBaseRepresentation<Pane, ActionButtonWidget>
{
    // Uses a Button if there is only one action,
    // otherwise a MenuButton so that user can select the specific action.
    //
    // These two types were chosen because they share the same ButtonBase base class.
    // ChoiceBox is not derived from ButtonBase, plus it has currently selected 'value',
    // and with action buttons it wouldn't make sense to select one of the actions.
    //
    // The 'base' button is wrapped in a 'pane'
    // to allow replacing the button as actions change from single actions (or zero)
    // to multiple actions.

    private final DirtyFlag dirty_representation = new DirtyFlag();
    private final DirtyFlag dirty_actionls = new DirtyFlag();

    private volatile ButtonBase base;
    private volatile String background;
    private volatile Color foreground;
    private volatile String button_text;

    /** Optional modifier of the open display 'target */
    private Optional<OpenDisplayActionInfo.Target> target_modifier = Optional.empty();

    private Pane pane;


    @Override
    public Pane createJFXNode() throws Exception
    {
        updateColors();
        base = makeBaseButton();

        pane = new Pane();
        pane.getChildren().add(base);

        return pane;
    }

    /** @param event Mouse event to check for target modifier keys */
    private void checkModifiers(final MouseEvent event)
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
        {
            logger.log(Level.FINE, "{0} modifier: {1}", new Object[] { model_widget, target_modifier.get() });
            base.arm();
        }
    }

    /** Create <code>base</code>, either single-action button
     *  or menu for selecting one out of N actions
     */
    private ButtonBase makeBaseButton()
    {
        final List<ActionInfo> actions = model_widget.propActions().getValue();
        final ButtonBase result;
        if (actions.size() < 2)
        {
            final Button button = new Button();
            if (actions.size() > 0)
            {
                final ActionInfo the_action = actions.get(0);
                button.setOnAction(event -> handleAction(the_action));
            }
            result = button;
        }
        else
        {
            final MenuButton button = new MenuButton();
            for (final ActionInfo action : actions)
            {
                final MenuItem item = new MenuItem(makeActionText(action));
                item.setOnAction(event -> handleAction(action));
                button.getItems().add(item);
            }
            result = button;
        }
        result.setStyle(background);
        result.getStyleClass().add("action_button");
        result.setMnemonicParsing(false);

        // Model has width/height, but JFX widget has min, pref, max size.
        // updateChanges() will set the 'pref' size, so make min use that as well.
        result.setMinSize(ButtonBase.USE_PREF_SIZE, ButtonBase.USE_PREF_SIZE);

        // Monitor keys that modify the OpenDisplayActionInfo.Target.
        // Use filter to capture event that's otherwise already handled.
        result.addEventFilter(MouseEvent.MOUSE_PRESSED, this::checkModifiers);

        return result;
    }

    private String makeButtonText()
    {
        // If text is "$(actions)", evaluate the actions ourself because
        // a) That way we can format it beyond just "[ action1, action2, ..]"
        // b) Macro won't be re-evaluated as actions change,
        //    while this code will always use current actions
        final StringWidgetProperty text_prop = (StringWidgetProperty)model_widget.propText();
        if ("$(actions)".equals(text_prop.getSpecification()))
        {
            final List<ActionInfo> actions = model_widget.propActions().getValue();
            if (actions.size() < 1)
                return Messages.ActionButton_NoActions;
            if (actions.size() > 1)
                return NLS.bind(Messages.ActionButton_N_ActionsFmt, actions.size());
            return makeActionText(actions.get(0));
        }
        else
            return text_prop.getValue();
    }

    private String makeActionText(final ActionInfo action)
    {
        String action_str = action.getDescription();
        if (action_str.isEmpty())
            action_str = action.toString();
        String expanded;
        try
        {
            final MacroValueProvider macros = model_widget.getMacrosOrProperties();
            expanded = MacroHandler.replace(macros, action_str);
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, model_widget + " action " + action + " cannot expand macros for " + action_str, ex);
            expanded = action_str;
        }
        return expanded;
    }

    /** @param action Action that the user invoked */
    private void handleAction(ActionInfo action)
    {
        logger.log(Level.FINE, "{0} pressed", model_widget);
        if (action instanceof OpenDisplayActionInfo  &&  target_modifier.isPresent())
        {
            final OpenDisplayActionInfo orig = (OpenDisplayActionInfo) action;
            action = new OpenDisplayActionInfo(orig.getDescription(), orig.getFile(), orig.getMacros(), target_modifier.get());
        }
        toolkit.fireAction(model_widget, action);
    }

    @Override
    protected void registerListeners()
    {
        updateColors();
        super.registerListeners();

        model_widget.propWidth().addUntypedPropertyListener(this::representationChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::representationChanged);
        model_widget.propText().addUntypedPropertyListener(this::representationChanged);
        model_widget.propFont().addUntypedPropertyListener(this::representationChanged);
        model_widget.propEnabled().addUntypedPropertyListener(this::representationChanged);

        model_widget.propBackgroundColor().addUntypedPropertyListener(this::buttonChanged);
        model_widget.propForegroundColor().addUntypedPropertyListener(this::buttonChanged);
        model_widget.propActions().addUntypedPropertyListener(this::buttonChanged);
    }

    /** Complete button needs to be updated */
    private void buttonChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_actionls.mark();
        representationChanged(property, old_value, new_value);
    }

    /** Only details of the existing button need to be updated */
    private void representationChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        updateColors();
        dirty_representation.mark();
        toolkit.scheduleUpdate(this);
    }

    private void updateColors()
    {
        foreground = JFXUtil.convert(model_widget.propForegroundColor().getValue());
        background = JFXUtil.shadedStyle(model_widget.propBackgroundColor().getValue());
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_actionls.checkAndClear())
        {
            base = makeBaseButton();
            jfx_node.getChildren().setAll(base);
        }
        if (dirty_representation.checkAndClear())
        {
            button_text = makeButtonText();
            base.setText(button_text);
            base.setTextFill(foreground);
            base.setPrefSize(model_widget.propWidth().getValue(),
                             model_widget.propHeight().getValue());
            base.setFont(JFXUtil.convert(model_widget.propFont().getValue()));
            base.setDisable(! model_widget.propEnabled().getValue());
        }
    }
}
