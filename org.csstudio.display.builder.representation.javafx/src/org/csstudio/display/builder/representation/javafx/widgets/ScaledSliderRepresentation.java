/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.text.DecimalFormat;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.ScaledSliderWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.Display;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueUtil;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.converter.FormatStringConverter;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScaledSliderRepresentation extends RegionBaseRepresentation<GridPane, ScaledSliderWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();
    private final DirtyFlag dirty_value = new DirtyFlag();
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_look = new DirtyFlag();

    private volatile double min = 0.0;
    private volatile double max = 100.0;
    private volatile double lolo = Double.NaN;
    private volatile double low = Double.NaN;
    private volatile double high = Double.NaN;
    private volatile double hihi = Double.NaN;
    private volatile double value = 50.0;
    private volatile double stepIncrement = 1.0;
    private volatile double tickUnit = 20;

    private volatile boolean active = false;

    private final Slider slider = new Slider();
    private final SliderMarkers markers = new SliderMarkers(slider);

    @Override
    protected GridPane createJFXNode() throws Exception
    {
        slider.setFocusTraversable(true);
        slider.setTooltip(new Tooltip(""));
        slider.setOnKeyPressed((final KeyEvent event) ->
        {
            switch (event.getCode())
            {
            case PAGE_UP:
                slider.adjustValue(value+slider.getBlockIncrement());
                event.consume();
                break;
            case PAGE_DOWN:
                slider.adjustValue(value-slider.getBlockIncrement());
                event.consume();
                break;
            default: break;
            }
        });
        slider.setValue(value);

        final GridPane pane = new GridPane();
        // pane.setGridLinesVisible(true);
        pane.add(markers, 0, 0);
        pane.getChildren().add(slider);
        return pane;
    }

    @Override
    protected boolean isFilteringEditModeClicks()
    {
        return true;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.propLimitsFromPV().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propMinimum().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propMaximum().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propHorizontal().addUntypedPropertyListener(this::lookChanged);
        model_widget.propIncrement().addPropertyListener(this::limitsChanged);
        model_widget.propEnabled().addUntypedPropertyListener(this::lookChanged);
        model_widget.propBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.propShowScale().addUntypedPropertyListener(this::styleChanged);
        model_widget.propShowMinorTicks().addUntypedPropertyListener(this::styleChanged);
        model_widget.propScaleFormat().addUntypedPropertyListener(this::styleChanged);
        model_widget.propLevelHi().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelHiHi().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelLo().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelLoLo().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowMarkers().addUntypedPropertyListener(this::lookChanged);
        model_widget.propShowHi().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowHiHi().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowLo().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowLoLo().addUntypedPropertyListener(this::limitsChanged);

        //Since both the widget's PV value and the JFX node's value property might be
        //written to independently during runtime, both must have listeners.
        slider.valueProperty().addListener(this::nodeValueChanged);
        if (toolkit.isEditMode())
            dirty_value.checkAndClear();
        else
            model_widget.runtimePropValue().addPropertyListener(this::valueChanged);
        limitsChanged(null, null, null);
        styleChanged(null, null, null);
        lookChanged(null, null, null);
    }

    private void lookChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_look.mark();
        toolkit.scheduleUpdate(this);
    }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        tickUnit = calculateTickUnit(max-min);
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void limitsChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        stepIncrement = model_widget.propIncrement().getValue();

        // Start with widget config
        double new_min = model_widget.propMinimum().getValue();
        double new_max = model_widget.propMaximum().getValue();
        double new_lolo = model_widget.propLevelLoLo().getValue();
        double new_low = model_widget.propLevelLo().getValue();
        double new_high = model_widget.propLevelHi().getValue();
        double new_hihi = model_widget.propLevelHiHi().getValue();

        if (model_widget.propLimitsFromPV().getValue())
        {
            // Try to get display range from PV
            final Display display_info = ValueUtil.displayOf(model_widget.runtimePropValue().getValue());
            if (display_info != null)
            {
                new_min = display_info.getLowerCtrlLimit();
                new_max = display_info.getUpperCtrlLimit();
                new_lolo = display_info.getLowerAlarmLimit();
                new_low = display_info.getLowerWarningLimit();
                new_high = display_info.getUpperWarningLimit();
                new_hihi = display_info.getUpperAlarmLimit();
            }
        }
        if (! model_widget.propShowLoLo().getValue())
            new_lolo = Double.NaN;
        if (! model_widget.propShowLo().getValue())
            new_low = Double.NaN;
        if (! model_widget.propShowHi().getValue())
            new_high = Double.NaN;
        if (! model_widget.propShowHiHi().getValue())
            new_hihi = Double.NaN;

        // If invalid limits, fall back to 0..100 range
        if (! (new_min < new_max))
        {
            new_min = 0.0;
            new_max = 100.0;
        }

        boolean changes = false;
        if (Double.compare(min, new_min) != 0)
        {
            min = new_min;
            changes = true;
        }
        if (Double.compare(max, new_max) != 0)
        {
            max = new_max;
            changes = true;
        }
        if (Double.compare(lolo, new_lolo) != 0)
        {
            lolo = new_lolo;
            changes = true;
        }
        if (Double.compare(low, new_low) != 0)
        {
            low = new_low;
            changes = true;
        }
        if (Double.compare(high, new_high) != 0)
        {
            high = new_high;
            changes = true;
        }
        if (Double.compare(hihi, new_hihi) != 0)
        {
            hihi = new_hihi;
            changes = true;
        }

        if (changes)
            sizeChanged(null, null, null);
    }

    /** Nice looking steps for the distance between tick,
     *  In general, the computed steps "fill" the axis.
     *  @see #calculateNumMajUnits(double)
     */
    final private static double[] NICE_STEPS = { 1.0, 2.0, 2.5, 5.0, 10.0 };
    /**
     * Calculate a nice-looking step that displays with at least
     * the size given by the major_tick_step_hint and fits nicely
     * into the given span of data values.
     * @param span Span, in data units, of widget values (i.e. max-min).
     * @return Tick unit, in data units, for slider.
     */
    private double calculateTickUnit(double span)
    {
        final double length = (model_widget.propHorizontal().getValue() ?
                model_widget.propWidth().getValue() :
                model_widget.propHeight().getValue());
        double dataDistance = (model_widget.propMajorTickStepHint().getValue() / length) * span;
            //dataDistance: min. distance, in data units, between major ticks
        final double order_of_magnitude = Math.pow(10, Math.floor(Math.log10(dataDistance)));
        double step = dataDistance / order_of_magnitude;
        for (int i=0; i<NICE_STEPS.length; ++i)
        {
            double quot = span / (NICE_STEPS[i] * order_of_magnitude);
            if (NICE_STEPS[i] >= step && quot - (int)quot == 0)
                 return NICE_STEPS[i] * order_of_magnitude;
        }
        return Math.abs(span); //note: may cause errors if span is 0
    }

    private void nodeValueChanged(ObservableValue<? extends Number> property, Number old_value, Number new_value)
    {
            if (active)
                return;
            // XXX Round value to step increment?
            //            final double save_increment = stepIncrement;
            //            final double save_min = min;
            //            final double numStepsInValue = Math.round(((double)new_value-save_min) / save_increment);
            //            new_value = save_min + numStepsInValue * save_increment;
            toolkit.fireWrite(model_widget, new_value);
    }

    private void valueChanged(final WidgetProperty<? extends VType> property, final VType old_value, final VType new_value)
    {
        if (model_widget.propLimitsFromPV().getValue())
            limitsChanged(null, null, null);
        dirty_value.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            double w = model_widget.propWidth().getValue();
            double h = model_widget.propHeight().getValue();
            //if all prefSize is used, layout is not properly
            //arranged when size changes in editor
            jfx_node.setMaxSize(w, h);
            jfx_node.setMinSize(w, h);
            if (model_widget.propHorizontal().getValue())
                slider.setMaxSize(w, Double.MAX_VALUE);
            else
                slider.setMaxSize(Double.MAX_VALUE, h);
            double save_unit = tickUnit;


            System.out.println("Updating Slider range to " + min + " .. " + max +
                               ", alarms " + lolo + ", " + low + ", " + high + ", " + hihi);
            slider.setMin(min);
            slider.setMax(max);
            markers.setAlarmMarkers(lolo, low, high, hihi);
            slider.setMinorTickCount((int) Math.round(save_unit / stepIncrement) - 1);
            slider.setMajorTickUnit(save_unit);
            slider.setBlockIncrement(model_widget.propIncrement().getValue());
        }
        if (dirty_value.checkAndClear())
        {
            active = true;
            try
            {
                final VType vtype = model_widget.runtimePropValue().getValue();
                double newval = VTypeUtil.getValueNumber(vtype).doubleValue();
                if (newval < min)
                    newval = min;
                else if (newval > max)
                    newval = max;
                if (!slider.isValueChanging())
                {
                    if (Double.isNaN(newval))
                        logger.log(Level.WARNING, model_widget + " PV has with invalid value " + vtype);
                    else
                        slider.setValue(newval);
                }
                value = newval;
            }
            finally
            {
                active = false;
            }
        }
        if (dirty_style.checkAndClear())
        {
            final Background background = new Background(new BackgroundFill(JFXUtil.convert(model_widget.propBackgroundColor().getValue()), CornerRadii.EMPTY, Insets.EMPTY));
            jfx_node.setBackground(background);
            markers.setBackground(background);
            final String format = model_widget.propScaleFormat().getValue();
            slider.setLabelFormatter(new FormatStringConverter<Double>(new DecimalFormat(format)));
            slider.setShowTickLabels(model_widget.propShowScale().getValue());
            slider.setShowTickMarks(model_widget.propShowMinorTicks().getValue());
        }
        if (dirty_look.checkAndClear())
        {
            jfx_node.setDisable(! model_widget.propEnabled().getValue());

            final boolean horizontal = model_widget.propHorizontal().getValue();
            slider.setOrientation(horizontal ? Orientation.HORIZONTAL : Orientation.VERTICAL);
            if (model_widget.propShowMarkers().getValue())
            {
                if (! jfx_node.getChildren().contains(markers))
                    jfx_node.add(markers, 0, 0);
                if (horizontal)
                {
                    GridPane.setConstraints(slider, 0, 1);
                    GridPane.setHgrow(slider, Priority.ALWAYS);
                    GridPane.setVgrow(slider, Priority.NEVER);
                    GridPane.setVgrow(markers, Priority.NEVER);
                }
                else
                {
                    GridPane.setConstraints(slider, 1, 0);
                    GridPane.setHgrow(slider, Priority.NEVER);
                    GridPane.setHgrow(markers, Priority.NEVER);
                    GridPane.setVgrow(slider, Priority.ALWAYS);
                }
                markers.setAlarmMarkers(lolo, low, high, hihi);
            }
            else
            {
                if (jfx_node.getChildren().contains(markers))
                    jfx_node.getChildren().remove(markers);
                GridPane.setConstraints(slider, 0, 0);
                if (horizontal)
                {
                    GridPane.setHgrow(slider, Priority.ALWAYS);
                    GridPane.setVgrow(slider, Priority.NEVER);
                }
                else
                {
                    GridPane.setHgrow(slider, Priority.NEVER);
                    GridPane.setVgrow(slider, Priority.ALWAYS);
                }
            }
        }
    }
}