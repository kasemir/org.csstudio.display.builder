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
import org.csstudio.display.builder.representation.javafx.MarkerAxis;
import org.diirt.vtype.Display;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueUtil;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.converter.FormatStringConverter;

/** Creates JavaFX item for model widget
 * @author Amanda Carpenter
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
    private volatile double hi;
    private volatile double lo;
    private volatile double hihi;
    private volatile double lolo;
    private volatile double value = 50.0;
    private volatile double stepIncrement = 1.0;
    private volatile double tickUnit = 20;

    private volatile boolean active = false;

    private final Slider slider = createSlider();
    private final MarkerAxis<Slider> axis = new MarkerAxis<Slider>(slider)
    {
        {
            slider.orientationProperty().addListener( (property, oldval, newval) ->
                makeVertical(newval==Orientation.VERTICAL)
            );
        }

        @Override
        protected void initializeBindings(Slider node)
        {
            length = new DoubleBinding()
            {
                {
                    super.bind(node.widthProperty(), node.heightProperty(), node.orientationProperty());
                }

                @Override
                protected double computeValue()
                {
                    return (node.getOrientation() == Orientation.HORIZONTAL ?
                            node.getWidth() : node.getHeight()) -
                            15;
                }
            };
            min = new DoubleBinding()
            {
                {
                    super.bind(node.minProperty());
                }

                @Override
                protected double computeValue()
                {
                    return node.getMin();
                }
            };
            max = new DoubleBinding()
            {
                {
                    super.bind(node.maxProperty());
                }

                @Override
                protected double computeValue()
                {
                    return node.getMax();
                }
            };
        }
    };


    @Override
    protected GridPane createJFXNode() throws Exception
    {
        final GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        GridPane.setConstraints(axis, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER);
        GridPane.setConstraints(slider, 0, 1, 1, 1, HPos.CENTER, VPos.CENTER);
        pane.getChildren().add(slider);
        //do not respond to mouse clicks in edit mode
        if (toolkit.isEditMode())
        {
            slider.setOnMousePressed((event) ->
            {
                event.consume();
                toolkit.fireClick(model_widget, event.isControlDown());
            });
        }
        return pane;
    }

    private Slider createSlider()
    {
        Slider slider = new Slider()
        {
            @Override
            public void increment()
            {
                adjustValue(value+stepIncrement);
            }
            @Override
            public void decrement()
            {
                adjustValue(value-stepIncrement);
            }
        };
        slider.setFocusTraversable(true);
        slider.setTooltip(new Tooltip(""));
        slider.setOnKeyPressed((final KeyEvent event) ->
        {
            switch (event.getCode())
            {
            case PAGE_UP:
                slider.adjustValue(value+slider.getBlockIncrement());
                break;
            case PAGE_DOWN:
                slider.adjustValue(value-slider.getBlockIncrement());
                break;
            default: break;
            }
        });
        slider.setValue(value);
        return slider;
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
        model_widget.propStepIncrement().addPropertyListener(this::limitsChanged);
        model_widget.propPageIncrement().addPropertyListener(this::limitsChanged);
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
        model_widget.propShowHi().addUntypedPropertyListener(this::lookChanged);
        model_widget.propShowHiHi().addUntypedPropertyListener(this::lookChanged);
        model_widget.propShowLo().addUntypedPropertyListener(this::lookChanged);
        model_widget.propShowLoLo().addUntypedPropertyListener(this::lookChanged);

        //Since both the widget's PV value and the JFX node's value property might be
        //written to independently during runtime, both must have listeners.
        model_widget.runtimePropValue().addPropertyListener(this::valueChanged);
        slider.valueProperty().addListener(this::nodeValueChanged);

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
        stepIncrement = model_widget.propStepIncrement().getValue();

        double vals [] = null;
        if (model_widget.propLimitsFromPV().getValue())
        {
            //Try to get display range from PV
            final Display display_info = ValueUtil.displayOf(model_widget.runtimePropValue().getValue());
            if (display_info != null)
            {
                vals = new double []
                {
                    display_info.getLowerCtrlLimit(),
                    display_info.getUpperCtrlLimit(),
                    display_info.getUpperAlarmLimit(),
                    display_info.getUpperWarningLimit(),
                    display_info.getLowerWarningLimit(),
                    display_info.getLowerAlarmLimit(),
                };
                for (double x : vals)
                {
                    if (Double.isNaN(x))
                    {
                        vals = null;
                        break;
                    }
                }
            }
        }
        if (vals == null)
        {
            vals = new double[]
            {
                    model_widget.propMinimum().getValue(),
                    model_widget.propMaximum().getValue(),
                    model_widget.propLevelLo().getValue(),
                    model_widget.propLevelLoLo().getValue(),
                    model_widget.propLevelHiHi().getValue(),
                    model_widget.propLevelHi().getValue()
            };
        }
        //If invalid limits, fall back to 0..100 range
        if (vals[0] >= vals[1])
        {
            vals[0] = 0.0;
            vals[1] = 100.0;
        }

        min = vals[0];
        max = vals[1];

        hi = vals[2];
        hihi = vals[3];
        lo = vals[4];
        lolo = vals[5];

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
            final double save_increment = stepIncrement;
            final double save_min = min;
            final double numStepsInValue = Math.round(((double)new_value-save_min) / save_increment);
            new_value = save_min + numStepsInValue * save_increment;
            slider.getTooltip().setText(""+new_value);
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
            slider.setMin(min);
            slider.setMax(max);
            axis.setHi(hi);
            axis.setHiHi(hihi);
            axis.setLo(lo);
            axis.setLoLo(lolo);
            slider.setMinorTickCount((int) Math.round(save_unit / stepIncrement) - 1);
            slider.setMajorTickUnit(save_unit);
            slider.setBlockIncrement(model_widget.propPageIncrement().getValue());
        }
        if (dirty_value.checkAndClear())
        {
            active = true;
            try
            {
                VType vtype = model_widget.runtimePropValue().getValue();
                double newval = VTypeUtil.getValueNumber(vtype).doubleValue();
                if (newval < min) newval = min;
                else if (newval > max) newval = max;
                if (!slider.isValueChanging())
                {
                    if(Double.isNaN(newval))
                        logger.log(Level.WARNING, "Slider widget '"+model_widget.getName()+
                                "' has PV with invalid value ("+newval+"): "+vtype);
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
            final Color background = JFXUtil.convert(model_widget.propBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
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
                if (horizontal)
                {
                    GridPane.setConstraints(slider, 0, 1);
                    GridPane.setHgrow(slider, Priority.ALWAYS);
                    GridPane.setVgrow(slider, Priority.NEVER);
                }
                else
                {
                    GridPane.setConstraints(slider, 1, 0);
                    GridPane.setHgrow(slider, Priority.NEVER);
                    GridPane.setVgrow(slider, Priority.ALWAYS);
                }
                if (!jfx_node.getChildren().contains(axis))
                    jfx_node.add(axis, 0, 0);
                axis.setShowHi(model_widget.propShowHi().getValue());
                axis.setShowHiHi(model_widget.propShowHiHi().getValue());
                axis.setShowLo(model_widget.propShowLo().getValue());
                axis.setShowLoLo(model_widget.propShowLoLo().getValue());
            }
            else
            {
                jfx_node.getChildren().removeIf((child)->child instanceof MarkerAxis);
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