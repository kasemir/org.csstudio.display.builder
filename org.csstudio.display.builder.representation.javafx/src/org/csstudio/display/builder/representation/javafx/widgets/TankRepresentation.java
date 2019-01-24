/*******************************************************************************
 * Copyright (c) 2015-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.concurrent.TimeUnit;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.TankWidget;
import org.csstudio.display.builder.representation.Preferences;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.rtplot.RTTank;
import org.diirt.vtype.Display;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueUtil;

import javafx.scene.layout.Pane;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class TankRepresentation extends RegionBaseRepresentation<Pane, TankWidget>
{
    private final DirtyFlag dirty_look = new DirtyFlag();
    private final UntypedWidgetPropertyListener lookChangedListener = this::lookChanged;
    private final UntypedWidgetPropertyListener valueChangedListener = this::valueChanged;

    private volatile RTTank tank;

    @Override
    public Pane createJFXNode() throws Exception
    {
        tank = new RTTank();
        tank.setUpdateThrottle(Preferences.getImageUpdateDelayMillisec(), TimeUnit.MILLISECONDS);
        return new Pane(tank);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propWidth().addUntypedPropertyListener(lookChangedListener);
        model_widget.propHeight().addUntypedPropertyListener(lookChangedListener);
        model_widget.propFont().addUntypedPropertyListener(lookChangedListener);
        model_widget.propForeground().addUntypedPropertyListener(lookChangedListener);
        model_widget.propBackground().addUntypedPropertyListener(lookChangedListener);
        model_widget.propFillColor().addUntypedPropertyListener(lookChangedListener);
        model_widget.propEmptyColor().addUntypedPropertyListener(lookChangedListener);
        model_widget.propScaleVisible().addUntypedPropertyListener(lookChangedListener);

        model_widget.propLimitsFromPV().addUntypedPropertyListener(valueChangedListener);
        model_widget.propMinimum().addUntypedPropertyListener(valueChangedListener);
        model_widget.propMaximum().addUntypedPropertyListener(valueChangedListener);
        model_widget.runtimePropValue().addUntypedPropertyListener(valueChangedListener);
        valueChanged(null, null, null);
    }

    @Override
    protected void unregisterListeners()
    {
        model_widget.propWidth().removePropertyListener(lookChangedListener);
        model_widget.propHeight().removePropertyListener(lookChangedListener);
        model_widget.propFont().removePropertyListener(lookChangedListener);
        model_widget.propForeground().removePropertyListener(lookChangedListener);
        model_widget.propBackground().removePropertyListener(lookChangedListener);
        model_widget.propFillColor().removePropertyListener(lookChangedListener);
        model_widget.propEmptyColor().removePropertyListener(lookChangedListener);
        model_widget.propScaleVisible().removePropertyListener(lookChangedListener);
        model_widget.propLimitsFromPV().removePropertyListener(valueChangedListener);
        model_widget.propMinimum().removePropertyListener(valueChangedListener);
        model_widget.propMaximum().removePropertyListener(valueChangedListener);
        model_widget.runtimePropValue().removePropertyListener(valueChangedListener);
        super.unregisterListeners();
    }

    private void lookChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_look.mark();
        toolkit.scheduleUpdate(this);
    }

    private void valueChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        final VType vtype = model_widget.runtimePropValue().getValue();

        final boolean limits_from_pv = model_widget.propLimitsFromPV().getValue();
        double min_val = model_widget.propMinimum().getValue();
        double max_val = model_widget.propMaximum().getValue();
        if (limits_from_pv)
        {
            // Try display range from PV
            final Display display_info = ValueUtil.displayOf(vtype);
            if (display_info != null)
            {
                min_val = display_info.getLowerDisplayLimit();
                max_val = display_info.getUpperDisplayLimit();
            }
        }
        tank.setRange(min_val, max_val);

        double value;
        if (toolkit.isEditMode())
            value = (min_val + max_val) / 2;
        else
            value = VTypeUtil.getValueNumber(vtype).doubleValue();
        tank.setValue(value);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_look.checkAndClear())
        {
            double width = model_widget.propWidth().getValue();
            double height = model_widget.propHeight().getValue();
            jfx_node.setPrefSize(width, height);
            tank.setWidth(width);
            tank.setHeight(height);
            tank.setFont(JFXUtil.convert(model_widget.propFont().getValue()));
            tank.setBackground(JFXUtil.convert(model_widget.propBackground().getValue()));
            tank.setForeground(JFXUtil.convert(model_widget.propForeground().getValue()));
            tank.setFillColor(JFXUtil.convert(model_widget.propFillColor().getValue()));
            tank.setEmptyColor(JFXUtil.convert(model_widget.propEmptyColor().getValue()));
            tank.setScaleVisible(model_widget.propScaleVisible().getValue());
        }
    }

    @Override
    public void dispose()
    {
        tank.dispose();
        super.dispose();
    }
}
