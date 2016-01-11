/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VType;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextUpdateRepresentation extends RegionBaseRepresentation<Label, TextUpdateWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile String value_text = "<?>";

    @Override
    public Label createJFXNode() throws Exception
    {   // Start out 'disconnected' until first value arrives
        value_text = computeText(null);
        return new Label();
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
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
        model_widget.behaviorPVName().addPropertyListener(this::pvnameChanged);
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    /** @param value Current value of PV
     *  @return Text to show, "<pv name>" if disconnected (no value)
     */
    private String computeText(final VType value)
    {
        if (value == null)
            return "<" + model_widget.behaviorPVName().getValue() + ">";
        return VTypeUtil.getValueString(value, true);
    }

    private void pvnameChanged(final WidgetProperty<String> property, final String old_value, final String new_value)
    {   // PV name typically changes in edit mode.
        // -> Show new PV name.
        // Runtime could deal with disconnect/reconnect for new PV name
        // -> Also OK to show disconnected state until runtime
        //    subscribes to new PV, so we eventually get values from new PV.
        value_text = computeText(null);
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        value_text = computeText(new_value);
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

            Color color = JFXUtil.convert(model_widget.displayForegroundColor().getValue());
            jfx_node.setTextFill(color);
            color = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
            jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));
        }
        if (dirty_content.checkAndClear())
            jfx_node.setText(value_text);
    }
}
