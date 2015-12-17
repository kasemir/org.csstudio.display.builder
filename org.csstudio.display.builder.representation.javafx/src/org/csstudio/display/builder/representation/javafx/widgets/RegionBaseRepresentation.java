/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Optional;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.DirtyFlag;
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
abstract public class RegionBaseRepresentation<JFX extends Region, MW extends BaseWidget> extends JFXBaseRepresentation<JFX, MW>
{
    private WidgetProperty<Boolean> alarm_sensitive_border;
    private volatile AlarmSeverity current_alarm = AlarmSeverity.NONE;
    private final DirtyFlag dirty_border = new DirtyFlag();
    private volatile Border border = null;

    /** Draw border OUTSIDE, so adding/removing border does not change the layout of the region's content
     *  (Like BorderStrokeStyle.SOLID except for OUTSIDE)
     */
    private static final BorderStrokeStyle border_stroke_style =
        new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 10, 0, null);

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        final Optional<WidgetProperty<Boolean>> border = model_widget.checkProperty(displayBorderAlarmSensitive);
        final Optional<WidgetProperty<VType>> value = model_widget.checkProperty(runtimeValue);
        if (border.isPresent()  &&  value.isPresent())
        {
            alarm_sensitive_border = border.get();
            value.get().addPropertyListener(this::valueChanged);
        }
    }

    private void valueChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        AlarmSeverity alarm;
        if (alarm_sensitive_border.getValue())
        {
            if (new_value instanceof Alarm)
                alarm = ((Alarm)new_value).getAlarmSeverity();
            else
                alarm = AlarmSeverity.UNDEFINED;
        }
        else
            alarm = AlarmSeverity.NONE;

        // Any change?
        if (current_alarm == alarm)
            return;

        // Compute border
        current_alarm = alarm;
        final Color color = getAlarmColor(alarm);
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
