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
import org.csstudio.display.builder.model.widgets.ComboWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.Styles;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VType;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class ComboRepresentation extends RegionBaseRepresentation<ComboBox<String>, ComboWidget>
{
    private volatile boolean active = false;

    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private final DirtyFlag dirty_enable = new DirtyFlag();
    private volatile List<String> items = Collections.emptyList();
    private volatile int index = -1;
    private volatile Callback<ListView<String>,ListCell<String>> cellFactory = null;

    @Override
    public ComboBox<String> createJFXNode() throws Exception
    {
        final ComboBox<String> combo = new ComboBox<String>();
        if (! toolkit.isEditMode())
        {
            // 'editable' cannot be changed at runtime
            combo.setEditable(model_widget.propEditable().getValue());

            // Handle user's selection
            combo.setOnAction((event)->
            {   // We are updating the UI, ignore
                if (active)
                    return;
                String value = combo.getValue();
                if (value != null)
                {
                    // Restore current value
                    contentChanged(null, null, null);
                    // ... which should soon be replaced by updated value, if accepted
                    toolkit.fireWrite(model_widget, value);
                }
            });
        }
        cellFactory = combo.getCellFactory();
        return combo;
    }

    @Override
    protected boolean isFilteringEditModeClicks()
    {   // Allow selecting the Combo in editor
        return true;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.propForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.propBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.propFont().addUntypedPropertyListener(this::styleChanged);

        model_widget.runtimePropValue().addUntypedPropertyListener(this::contentChanged);
        model_widget.propItemsFromPV().addUntypedPropertyListener(this::contentChanged);
        model_widget.propItems().addUntypedPropertyListener(this::contentChanged);
        model_widget.propEnabled().addUntypedPropertyListener(this::enableChanged);
        model_widget.runtimePropPVWritable().addUntypedPropertyListener(this::enableChanged);

        styleChanged(null, null, null);
    }

    private Callback<ListView<String>,ListCell<String>> createCellFactory(Color fg, Color bg, Font font)
    {
        return (param)->
        {
            ListCell<String> cell = new ListCell<String>()
            {
                @Override
                public void updateItem(String item, boolean empty)
                {
                    super.updateItem(item, empty);
                    setText(item);
                }
            };
            cell.setTextFill(fg);
            cell.setBackground(new Background(new BackgroundFill(bg, CornerRadii.EMPTY, Insets.EMPTY)));
            cell.setFont(font);

            return cell;
        };
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        cellFactory = createCellFactory(JFXUtil.convert(model_widget.propForegroundColor().getValue()),
                                        JFXUtil.convert(model_widget.propBackgroundColor().getValue()),
                                        JFXUtil.convert(model_widget.propFont().getValue()) );

        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void enableChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_enable.mark();
        toolkit.scheduleUpdate(this);
    }


    /** @param value Current value of PV
     *  @param fromPV Use items from enum?
     *  @return List of items, potentially adding the current value to the items originally in the combo
     */
    private List<String> computeItems(final VType value, final boolean fromPV)
    {
        // System.out.println("computeItems(" + value + ", " + fromPV + "): ");
        if (fromPV)
        {
            index = ((VEnum)value).getIndex();
            return ((VEnum)value).getLabels();
        }
        else
        {
            final List<String> new_items = new ArrayList<String>();
            for (WidgetProperty<String> itemProp : model_widget.propItems().getValue())
                new_items.add(itemProp.getValue());

            final String currValue = VTypeUtil.getValueString(value, false);
            int new_index = new_items.indexOf(currValue);
            if (new_index < 0)
            {   // User entered a custom value ('editable' combo).
                // Add to top of list and select it
                new_items.add(0, currValue);
                new_index = 0;
            }

            // System.out.println(new_items);
            // System.out.println(new_index);

            index = new_index;
            return new_items;
        }
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        VType value = model_widget.runtimePropValue().getValue();
        boolean fromPV = model_widget.propItemsFromPV().getValue() && value instanceof VEnum;
        items = computeItems(value, fromPV); //also sets index
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_style.checkAndClear())
        {
            jfx_node.setPrefSize(model_widget.propWidth().getValue(),
                                 model_widget.propHeight().getValue());
            if (cellFactory != null)
            {
                //jfx_node.setCellFactory(cellFactory);
                jfx_node.setButtonCell(cellFactory.call(null));
            }
        }
        if (dirty_content.checkAndClear())
        {
            active = true;
            try
            {
                jfx_node.setItems(FXCollections.observableArrayList(items));
                jfx_node.getSelectionModel().clearAndSelect(index);
            }
            finally
            {
                active = false;
            }
        }
        if (dirty_enable.checkAndClear())
        {
            final boolean enabled = model_widget.propEnabled().getValue()  &&
                                    model_widget.runtimePropPVWritable().getValue();
            jfx_node.setDisable(! enabled);
            Styles.update(jfx_node, Styles.NOT_ENABLED, !enabled);
        }
    }
}
