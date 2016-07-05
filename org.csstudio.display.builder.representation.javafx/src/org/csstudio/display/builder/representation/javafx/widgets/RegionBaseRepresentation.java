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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.widgets.VisibleWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.VType;

import javafx.geometry.Insets;
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
abstract public class RegionBaseRepresentation<JFX extends Region, MW extends VisibleWidget> extends JFXBaseRepresentation<JFX, MW>
{
    /** Border for each {@link AlarmSeverity} */
    private static Border[] alarm_borders = new Border[AlarmSeverity.values().length];

    /** Prepare alarm_borders
     *
     *  <p>Alarm borders are distinguished by color as well as style in case of color vision deficiency.
     *
     *  <p>They are drawn OUTSIDE the widget, so adding/removing border does not change the layout
     *  of the region's content.
     */
    static
    {
        // Like BorderStrokeStyle.SOLID except for OUTSIDE
        final BorderStrokeStyle solid =
            new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10, 0, null);

        final BorderStrokeStyle dashed =
            new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10, 0,
                                  Collections.unmodifiableList(Arrays.asList(10.0, 8.0)));

        final BorderStrokeStyle dash_dotted =
                new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10, 0,
                                      Collections.unmodifiableList(Arrays.asList(8.0, 2.0, 2.0, 2.0)));

        final BorderWidths thin = new BorderWidths(1);
        final BorderWidths normal = new BorderWidths(2);

        // No alarm -> no border
        alarm_borders[AlarmSeverity.NONE.ordinal()] = null;

        // Minor -> Simple border
        Color color = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MINOR));
        alarm_borders[AlarmSeverity.MINOR.ordinal()] =
            new Border(new BorderStroke(color, solid, CornerRadii.EMPTY, normal));

        // Major -> Double border
        color =  JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MAJOR));
        alarm_borders[AlarmSeverity.MAJOR.ordinal()] =
            new Border(new BorderStroke(color, solid, CornerRadii.EMPTY, thin),
                       new BorderStroke(color, solid, CornerRadii.EMPTY, thin, new Insets(-2*thin.getTop())));

        // Invalid -> Border is cleverly interrupted just like the communication to the control system
        color =  JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_INVALID));
        alarm_borders[AlarmSeverity.INVALID.ordinal()] =
            new Border(new BorderStroke(color, dash_dotted, CornerRadii.EMPTY, normal));

        // Disconnected -> Gaps in dashed style are even wider than dash_dotted
        color =  JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_DISCONNECTED));
        alarm_borders[AlarmSeverity.UNDEFINED.ordinal()] =
            new Border(new BorderStroke(color, dashed, CornerRadii.EMPTY, normal));
    }

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
            // runtimeValue should be a VType,
            // but some widgets may allow other data types (Table),
            // so use Object and then check for VType
            value_prop.addUntypedPropertyListener(this::valueChanged);
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

    private void valueChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        computeValueBorder(new_value);
    }

    private void computeValueBorder(final Object value)
    {
        AlarmSeverity severity;
        if (alarm_sensitive_border_prop.getValue())
        {
            if (value instanceof Alarm)
                // Have alarm info
                severity = ((Alarm)value).getAlarmSeverity();
            else if (value instanceof VType)
                // VType that doesn't provide alarm, always OK
                severity = AlarmSeverity.NONE;
            else if (value != null)
                // Not a vtype, but non-null, assume OK
                severity = AlarmSeverity.NONE;
            else// null
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
        border = alarm_borders[severity.ordinal()];
        dirty_border.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_border.checkAndClear())
            jfx_node.setBorder(border);
    }
}
