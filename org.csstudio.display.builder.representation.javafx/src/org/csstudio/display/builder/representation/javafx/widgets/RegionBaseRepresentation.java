/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimePropValue;

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
 *  <p>For widgets with "border_alarm_sensitive" and "value" properties,
 *  the border is based on the alarm severity of that value,
 *  which in turn tends to be read from a primary PV.
 *
 *  <p>In addition, the "connected" runtime property is checked,
 *  which is updated by the runtime to indicate if _all_ PVs
 *  associated with the widget are connected.
 *  Whenever any PV is disconnected, the border reflects that.
 *
 *  @author Kay Kasemir
 */
abstract public class RegionBaseRepresentation<JFX extends Region, MW extends VisibleWidget> extends JFXBaseRepresentation<JFX, MW>
{
    // Like BorderStrokeStyle.SOLID except for OUTSIDE
    private static final BorderStrokeStyle solid =
        new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10, 0, null);

    private static final BorderStrokeStyle dashed =
        new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10, 0,
                              Collections.unmodifiableList(Arrays.asList(10.0, 8.0)));

    private static final BorderStrokeStyle dash_dotted =
            new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10, 0,
                                  Collections.unmodifiableList(Arrays.asList(8.0, 2.0, 2.0, 2.0)));

    private static final BorderWidths thin = new BorderWidths(1);
    private static final BorderWidths normal = new BorderWidths(2);

    /** Colors used for the various alarm severities */
    private static final Color[] alarm_colors = new Color[AlarmSeverity.values().length];

    /** Common border for each {@link AlarmSeverity} when not using custom radii */
    private static final Border[] alarm_borders = new Border[AlarmSeverity.values().length];

    /** Prepare alarm_borders
     *
     *  <p>Alarm borders are distinguished by color as well as style in case of color vision deficiency.
     *
     *  <p>They are drawn OUTSIDE the widget, so adding/removing border does not change the layout
     *  of the region's content.
     */
    static
    {
        alarm_colors[AlarmSeverity.NONE.ordinal()] = null;
        alarm_colors[AlarmSeverity.MINOR.ordinal()] = JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MINOR));
        alarm_colors[AlarmSeverity.MAJOR.ordinal()] =  JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_MAJOR));
        alarm_colors[AlarmSeverity.INVALID.ordinal()] =  JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_INVALID));
        alarm_colors[AlarmSeverity.UNDEFINED.ordinal()] =  JFXUtil.convert(WidgetColorService.getColor(NamedWidgetColors.ALARM_DISCONNECTED));

        for (AlarmSeverity severity : AlarmSeverity.values())
            alarm_borders[severity.ordinal()] = createAlarmBorder(severity, CornerRadii.EMPTY);
    }

    private final DirtyFlag dirty_border = new DirtyFlag();
    private volatile WidgetProperty<VType> value_prop = null;
    private volatile WidgetProperty<Boolean> alarm_sensitive_border_prop = null;
    private final AtomicReference<AlarmSeverity> current_alarm = new AtomicReference<>(AlarmSeverity.NONE);
    private volatile Border border;

    /** Create alarm-based border
     *  @param severity AlarmSeverity
     *  @param corners CornerRadii
     *  @return Border
     */
    private static Border createAlarmBorder(final AlarmSeverity severity, final CornerRadii corners)
    {
        switch (severity)
        {
        case NONE:
            // No alarm -> no border
            return null;
        case MINOR:
            // Minor -> Simple border
            return new Border(new BorderStroke(alarm_colors[severity.ordinal()], solid, corners, normal));
        case MAJOR:
            // Major -> Double border
            return new Border(new BorderStroke(alarm_colors[severity.ordinal()], solid, corners, thin),
                              new BorderStroke(alarm_colors[severity.ordinal()], solid, corners, thin, new Insets(-2*thin.getTop())));
        case INVALID:
            // Invalid -> Border is cleverly interrupted just like the communication to the control system
            return new Border(new BorderStroke(alarm_colors[severity.ordinal()], dash_dotted, corners, normal));
        case UNDEFINED:
        default:
            // Disconnected -> Gaps in dashed style are even wider than dash_dotted
            return new Border(new BorderStroke(alarm_colors[severity.ordinal()], dashed, corners, normal));
        }
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        final Optional<WidgetProperty<Boolean>> border = model_widget.checkProperty(propBorderAlarmSensitive);
        final Optional<WidgetProperty<VType>> value = model_widget.checkProperty(runtimePropValue);
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

        model_widget.runtimePropConnected().addPropertyListener(this::connectionChanged);
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

    /** Get radii for the border of the widget.
     *
     *  <p>Default implementation returns <code>null</code>,
     *  i.e. no border radii, to use the shared default alarm
     *  border.
     *
     *  <p>Derived class can override to request alarm border
     *  which follows the border of the widget.
     *
     *  @return Array with horizontal and vertical border radius
     */
    public int[] getBorderRadii()
    {
        return null;
    }

    private void computeAlarmBorder(final AlarmSeverity severity)
    {
        // Any change?
        if (current_alarm.getAndSet(severity) == severity)
            return;

        final int[] radii = getBorderRadii();
        if (radii == null)
            // Use common alarm border
            border = alarm_borders[severity.ordinal()];
        else
        {   // Create a custom alarm border
            final int horiz = radii[0], vert = radii[1];
            // There's a bug in CornerRadii:
            // Even though horiz != vert, it considers them all 'uniform'
            // because it _separately_ compares all the horizontal and vertical radii,
            // never checking if horiz == vert.
            // Workaround: Make one of the horiz or vert radii a little different (+0.1).
            // Bug was in at least Java 1.8.0_101.
            final CornerRadii corners = new CornerRadii(horiz, vert, vert, horiz, horiz, vert, vert, horiz+0.1,
                                                        false, false, false, false, false, false, false, false);
            border = createAlarmBorder(severity, corners);
        }
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
