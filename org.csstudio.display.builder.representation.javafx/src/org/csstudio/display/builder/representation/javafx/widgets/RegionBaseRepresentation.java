/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.VType;

import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

/** Base class for all JavaFX widgets that use a {@link Region}-derived JFX node representations
 *
 *  <p>Implements alarm-sensitive border based on the Region's border.
 *
 *  @author Kay Kasemir
 */
abstract public class RegionBaseRepresentation<JFX extends Region, MW extends Widget> extends JFXBaseRepresentation<JFX, MW>
{
    /** Draw border OUTSIDE, so adding/removing border does not change the layout of the region's content
     *  (Like BorderStrokeStyle.SOLID except for OUTSIDE)
     */
    private static final BorderStrokeStyle border_stroke_style =
            new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10, 0, null);

    private final DirtyFlag dirty_border = new DirtyFlag();
    private volatile WidgetProperty<VType> value_prop = null;
    private volatile WidgetProperty<Boolean> alarm_sensitive_border_prop = null;
    private final AtomicReference<AlarmSeverity> current_alarm = new AtomicReference<>(AlarmSeverity.NONE);
    private volatile Border border;

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        final Optional<WidgetProperty<Boolean>> border = model_widget.checkProperty(displayBorderAlarmSensitive);
        final Optional<WidgetProperty<VType>> value = model_widget.checkProperty(runtimeValue);
        if (border.isPresent()  &&  value.isPresent())
        {
            value_prop = value.get();
            alarm_sensitive_border_prop = border.get();
            // Start 'OK'
            computeAlarmBorder(AlarmSeverity.NONE);
            value_prop.addPropertyListener(this::valueChanged);
        }

        model_widget.runtimeConnected().addPropertyListener(this::connectionChanged);
    }

    private void connectionChanged(final WidgetProperty<Boolean> property, final Boolean was_connected, final Boolean is_connected)
    {
        if (is_connected)
        {   // Reflect severity of primary PV's value
            if (value_prop != null)
                computeValueBorder(value_prop.getValue());
            else // No PV: OK
                computeAlarmBorder(AlarmSeverity.NONE);
        }
        else// Value of primary PV doesn't matter, show disconnected
            computeAlarmBorder(AlarmSeverity.UNDEFINED);
    }

    private void valueChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        computeValueBorder(new_value);
    }

    private void computeValueBorder(final VType value)
    {
        AlarmSeverity severity;
        if (alarm_sensitive_border_prop.getValue())
        {
            if (value instanceof Alarm)
                severity = ((Alarm)value).getAlarmSeverity();
            else
                severity = AlarmSeverity.UNDEFINED;
        }
        else
            severity = AlarmSeverity.NONE;

        computeAlarmBorder(severity);
    }

    private void computeAlarmBorder(final AlarmSeverity severity)
    {
        // Any change?
        if (current_alarm.getAndSet(severity) == severity)
            return;
        final Color color = getAlarmColor(severity);
        if (color == null)
            border = null;
        else
        {
            final int width = 2;
            border = new Border(new BorderStroke(color, border_stroke_style, CornerRadii.EMPTY, new BorderWidths(width)));
        }
        dirty_border.mark();
        toolkit.scheduleUpdate(this);
    }

    /** @param alarm {@link AlarmSeverity}
     *  @return Color for given alarm severity
     */
    private Color getAlarmColor(final AlarmSeverity alarm)
    {
        switch (alarm)
        {
        case MINOR:
            return JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MINOR));
        case MAJOR:
            return JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MAJOR));
        case INVALID:
            return JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_INVALID));
        case UNDEFINED:
            return JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_DISCONNECTED));
        default:
            return null;
        }
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_border.checkAndClear())
            jfx_node.setBorder(border);
    }
}
