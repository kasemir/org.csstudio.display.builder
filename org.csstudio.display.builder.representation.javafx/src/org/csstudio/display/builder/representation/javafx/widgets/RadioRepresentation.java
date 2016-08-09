/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.RadioWidget;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VType;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.TilePane;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class RadioRepresentation extends JFXBaseRepresentation<TilePane, RadioWidget>
{
    private volatile boolean active = false;
    private final ToggleGroup toggle = new ToggleGroup();

    private final DirtyFlag dirty_size = new DirtyFlag();
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile List<String> items = Collections.emptyList();
    private volatile int index = -1;

    @Override
    public TilePane createJFXNode() throws Exception
    {
        final TilePane pane = new TilePane();
        return pane;
    }

    private RadioButton createRadioButton()
    {
        final RadioButton rb = new RadioButton();
        rb.setToggleGroup(toggle);
        //TODO: if toolkit.isEditMode, prevent selection
        rb.setOnAction((event) ->
        {   // We are updating the UI, ignore
            if (active)
                return;
            final RadioButton selected = (RadioButton) toggle.getSelectedToggle();
            final String value = selected != null ? selected.getText() : null;
            if (value != null)
            {
                // Restore current value
                contentChanged(null, null, null);
                // ... which should soon be replaced by updated value, if accepted
                toolkit.fireWrite(model_widget, value);
            }
        });
        return rb;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::sizeChanged);
        model_widget.displayHorizontal().addUntypedPropertyListener(this::sizeChanged);

        model_widget.displayForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displaySelectedColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayFont().addUntypedPropertyListener(this::styleChanged);

        model_widget.runtimeValue().addUntypedPropertyListener(this::contentChanged);
        model_widget.behaviorItemsFromPV().addUntypedPropertyListener(this::contentChanged);
        model_widget.behaviorItems().addUntypedPropertyListener(this::contentChanged);

        styleChanged(null, null, null);
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    /**
     * @param value Current value of PV
     * @return list of items for each radio button
     */
    private List<String> computeItems(final VType value, final boolean fromPV)
    {
        if (fromPV && value instanceof VEnum)
        {
            index = ((VEnum)value).getIndex();
            return ((VEnum)value).getLabels();
        }
        else
        {
            List<WidgetProperty<String>> itemProps = model_widget.behaviorItems().getValue();
            List<String> new_items = new ArrayList<String>(itemProps.size());
            int new_index = -1;
            String currValue = VTypeUtil.getValueString(value, false);
            for (WidgetProperty<String> itemProp : itemProps)
            {
                new_items.add(itemProp.getValue());
                if (itemProp.getValue().equals(currValue))
                    new_index = new_items.size()-1;
            }
            index = new_index;
            return new_items;
        }
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        VType value = model_widget.runtimeValue().getValue();
        boolean fromPV = model_widget.behaviorItemsFromPV().getValue() && value instanceof VEnum;
        items = computeItems(value, fromPV); //also sets index
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            //size
            jfx_node.setPrefSize(model_widget.positionWidth().getValue(), model_widget.positionHeight().getValue());
            //horizontal
            jfx_node.setOrientation(
                    model_widget.displayHorizontal().getValue() ? Orientation.HORIZONTAL : Orientation.VERTICAL);
        }
        if (dirty_content.checkAndClear())
        {
            active = true;
            try
            {
                //copy volatile lists before iteration
                final List<String> save_items = new ArrayList<String>(items);
                final List<Node> save_buttons = new ArrayList<Node>(jfx_node.getChildren());
                //final List<Toggle> new_toggles = new ArrayList<Toggle>(save_items.size());

                //set text of buttons and create new list of Toggles for the ToggleGroup
                int i, save_index = index;
                for (i = 0; i < save_items.size(); i++)
                {
                    RadioButton rb;
                    if (i < save_buttons.size())
                        rb = (RadioButton) save_buttons.get(i);
                    else
                    {
                        rb = createRadioButton();
                        save_buttons.add(rb);
                    }
                    rb.setText(save_items.get(i));
                    //new_toggles.add(rb);
                }
                while (i < save_buttons.size())
                    save_buttons.remove(save_buttons.size() - 1);

                //set values for JavaFX items
                //toggle.getToggles().setAll(new_toggles);
                toggle.selectToggle(save_index < 0 ? null : (Toggle) save_buttons.get(save_index));
                jfx_node.getChildren().setAll(save_buttons);
            }
            finally
            {
                active = false;
            }
        }
        if (dirty_style.checkAndClear())
        {
            //colors
            //font
        }
    }
}
