/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.ComboWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
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
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile List<String> items = new CopyOnWriteArrayList<String>();
    private volatile int index = -1;
    private volatile Callback<ListView<String>,ListCell<String>> cellFactory = null;

    @Override
    public ComboBox<String> createJFXNode() throws Exception
    {   // Start out 'disconnected' until first value arrives
        final ComboBox<String> combo = new ComboBox<String>();
        combo.setOnAction((event)->
        {
            String value = combo.getValue();
            if (value != null)
            {
                // Restore current value
                contentChanged(null, null, null);
                // ... which should soon be replaced by updated value, if accepted
                toolkit.fireWrite(model_widget, value);
            }
        });
        cellFactory = combo.getCellFactory();
        return combo;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayFont().addUntypedPropertyListener(this::styleChanged);

        model_widget.runtimeValue().addUntypedPropertyListener(this::contentChanged);
        model_widget.behaviorItemsFromPV().addUntypedPropertyListener(this::contentChanged);
        model_widget.behaviorItems().addUntypedPropertyListener(this::contentChanged);
        
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
        cellFactory = createCellFactory(JFXUtil.convert(model_widget.displayForegroundColor().getValue()),
                                        JFXUtil.convert(model_widget.displayBackgroundColor().getValue()),
                                        JFXUtil.convert(model_widget.displayFont().getValue()) );

        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    /** @param value Current value of PV
     *  @return Text to show, "<pv name>" if disconnected (no value)
     */
    private List<String> computeItems(final VType value, final boolean fromPV)
    {
        if (fromPV)
        {
            index = ((VEnum)value).getIndex();
            return ((VEnum)value).getLabels(); //TODO: is safe to return List, not CopyOnWriteArrayList?
        }
        else
        {
            List<String> new_items = new CopyOnWriteArrayList<String>();
            List<WidgetProperty<String>> itemProps = model_widget.behaviorItems().getValue();
            int new_index = -1;
            String currValue = VTypeUtil.getValueString(value, false);
            for (WidgetProperty<String> itemProp : itemProps)
            {
                new_items.add(itemProp.getValue());
                if (itemProp.getValue().equals(currValue))
                    new_index = new_items.size()-1;
            }
            if (new_index < 0)
            {
                new_items.add(currValue);
                new_index = items.size()-1;
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
        if (dirty_style.checkAndClear())
        {
            jfx_node.setPrefSize(model_widget.positionWidth().getValue(),
                                 model_widget.positionHeight().getValue());
            if (cellFactory != null)
            {
                //jfx_node.setCellFactory(cellFactory);
                jfx_node.setButtonCell(cellFactory.call(null));
            }
        }
        if (dirty_content.checkAndClear())
        {
            jfx_node.setItems(FXCollections.observableArrayList(items));
            jfx_node.getSelectionModel().clearAndSelect(index);
        }
    }
}
