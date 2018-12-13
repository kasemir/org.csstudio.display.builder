/*******************************************************************************
 * Copyright (c) 2015-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimePropPVValue;
import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.csstudio.display.builder.model.MacroizedWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.macros.MacroValueProvider;
import org.csstudio.display.builder.model.properties.StringWidgetProperty;
import org.csstudio.java.time.TimestampFormats;
import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.Time;
import org.diirt.vtype.ValueUtil;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/** Support for tooltips
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TooltipSupport
{
    /** Legacy tool tip: "$(pv_name)\n$(pv_value)" where number of '\n' can vary */
    private final static Pattern legacy_tooltip = Pattern.compile("\\$\\(pv_name\\)\\s*\\$\\(pv_value\\)");

    private static boolean initialized_behavior = false;

    /** System property to disable tool tips (for debugging problems seen on Linux) */
    private static boolean disable_tooltips = Boolean.parseBoolean(System.getProperty("org.csstudio.display.builder.disable_tooltips"));

    /** Attach tool tip
     *  @param node Node that should have the tool tip
     *  @param tooltip_property Tool tip to show
     */
    public static void attach(final Node node, final WidgetProperty<String> tooltip_property)
    {
        attach(node, tooltip_property, null);
    }

    /** Attach tool tip
     *  @param node Node that should have the tool tip
     *  @param tooltip_property Tool tip to show
     *  @param pv_value Supplier of formatted "$(pv_value)". <code>null</code> to use toString of the property.
     */
    public static void attach(final Node node, final WidgetProperty<String> tooltip_property, final Supplier<String> pv_value)
    {
        if (disable_tooltips)
            return;

        // Patch legacy tool tips that defaulted to pv name & value,
        // even for static widgets
        final StringWidgetProperty ttp = (StringWidgetProperty)tooltip_property;
        if (legacy_tooltip.matcher(ttp.getSpecification()).matches()  &&
            ! tooltip_property.getWidget().checkProperty("pv_name").isPresent())
            ttp.setSpecification("");

        // Suppress tool tip if _initial_ text is empty.
        // In case a script changes the tool tip at runtime,
        // tool tip must have some initial non-empty value.
        // This was done for optimization:
        // Avoid listener and code to remove/add tooltip at runtime.
        if (tooltip_property.getValue().isEmpty())
            return;

        final Tooltip tooltip = new Tooltip();
        tooltip.setWrapText(true);
        // Evaluate the macros in tool tip specification each time
        // the tool tip is about to show
        tooltip.setOnShowing(event ->
        {
            String spec = ((MacroizedWidgetProperty<?>)tooltip_property).getSpecification();

            // Use custom supplier for $(pv_value)?
            // Otherwise replace like other macros, i.e. use toString of the property
            if (pv_value != null)
            {
                final StringBuilder buf = new StringBuilder();
                buf.append(pv_value.get());
                final Object vtype = tooltip_property.getWidget().getPropertyValue(runtimePropPVValue);
                final Alarm alarm = ValueUtil.alarmOf(vtype);
                if (alarm != null  &&  alarm.getAlarmSeverity() != AlarmSeverity.NONE)
                    buf.append(", ").append(alarm.getAlarmSeverity()).append(" - ").append(alarm.getAlarmName());
                final Time time = ValueUtil.timeOf(vtype);
                if (time != null)
                    buf.append(", ").append(TimestampFormats.FULL_FORMAT.format(time.getTimestamp()));
                spec = spec.replace("$(pv_value)", buf.toString());
            }
            final Widget widget = tooltip_property.getWidget();
            final MacroValueProvider macros = widget.getMacrosOrProperties();
            String expanded;
            try
            {
                expanded = MacroHandler.replace(macros, spec);
                tooltip.setText(expanded);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot evaluate tooltip of " + widget, ex);
                tooltip.setText(spec);
            }
        });

        Tooltip.install(node, tooltip);
        node.getProperties().put("_tooltip", tooltip);

        if (! initialized_behavior)
        {
            // Unfortunately, no API to control when tooltop shows, and for how long.
            // http://stackoverflow.com/questions/26854301/control-javafx-tooltip-delay
            // has the hack used in here, which only needs to be applied once
            // because it changes a static BEHAVIOR inside the Tooltip.
            // Java 9 will offer API, https://bugs.openjdk.java.net/browse/JDK-8090477
            hack_behavior(tooltip);
            initialized_behavior = true;
        }
    }


    /**
     * Detach tool tip.
     *
     * @param node Node that should have the tool tip removed.
     */
    public static void detach ( final Node node )
    {
        if ( node != null )
            Optional.ofNullable(node.getProperties().get("_tooltip"))
                    .ifPresent(t -> Tooltip.uninstall(node, (Tooltip) t));
    }

    private static void hack_behavior(final Tooltip tooltip)
    {
        try
        {
            final Field behavior_field = tooltip.getClass().getDeclaredField("BEHAVIOR");
            behavior_field.setAccessible(true);
            final Object behavior = behavior_field.get(tooltip);

            // Show after 250 instead of 1000 ms
            hack_behavior(behavior, "activationTimer", 250);
            // Show after 30 instead of 5 secs
            hack_behavior(behavior, "hideTimer", 30000);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot set Tooltip behaviour", ex);
        }
    }

    private static void hack_behavior(final Object behavior, final String aspect, final int ms) throws Exception
    {
        final Field field = behavior.getClass().getDeclaredField(aspect);
        field.setAccessible(true);
        final Timeline timer = (Timeline) field.get(behavior);
        timer.getKeyFrames().clear();
        timer.getKeyFrames().add(new KeyFrame(new Duration(ms)));
        logger.log(Level.FINE, "Set Tooltip " + aspect + " to " + ms + " ms");
    }
}
