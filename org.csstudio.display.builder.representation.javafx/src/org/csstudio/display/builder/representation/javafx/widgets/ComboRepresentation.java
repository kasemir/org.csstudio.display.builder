/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.FormatOptionHandler;
import org.csstudio.display.builder.model.widgets.ComboWidget;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class ComboRepresentation extends RegionBaseRepresentation<ComboBox<String>, ComboWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile List<String> items = new CopyOnWriteArrayList<String>();

    @Override
    public ComboBox<String> createJFXNode() throws Exception
    {   // Start out 'disconnected' until first value arrives
        final ComboBox<String> combo = new ComboBox<String>();
        combo.setOnAction((event)->
        {
            String value = combo.getValue();
            if (value != null)
                toolkit.fireWrite(model_widget, value);
                combo.getEditor().setText(value);
        });
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
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    /** @param value Current value of PV
     *  @return Text to show, "<pv name>" if disconnected (no value)
     */
    private List<String> computeItems(final VType value)
    {
        List<String> new_items = new CopyOnWriteArrayList<String>();
        if (model_widget.behaviorItemsFromPV().getValue() && value instanceof VEnum)
        {
            return ((VEnum)value).getLabels(); //TODO: is safe to return List, not CopyOnWriteArrayList?
        }
        else
        {
            List<WidgetProperty<String>> itemProps = model_widget.behaviorItems().getValue();
            for (WidgetProperty<String> itemProp : itemProps)
            {
                new_items.add(itemProp.getValue());
            }
        }
        return new_items;
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        items = computeItems(model_widget.runtimeValue().getValue());
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
            
            //final Font font = JFXUtil.convert(model_widget.displayFont().getValue());
            
            final Color color = JFXUtil.convert(model_widget.displayForegroundColor().getValue());
            jfx_node.setCellFactory((listview_param)->
            {
                return new ListCell<String>()
                {
                    @Override
                    public void updateItem(String item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        setText(item);
                        setTextFill(color);
                        //setFont(font);
                    }
                };
            });
            jfx_node.setButtonCell(jfx_node.getCellFactory().call(null));
            
            final Color background = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
            
            jfx_node.getEditor().setFont(JFXUtil.convert(model_widget.displayFont().getValue()));
        }
        if (dirty_content.checkAndClear())
            jfx_node.setItems(FXCollections.observableArrayList(items));
    }
}
