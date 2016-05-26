package org.csstudio.display.builder.representation.javafx.widgets;

import java.text.DecimalFormat;

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
import javafx.scene.paint.Color;
import javafx.util.converter.FormatStringConverter;

public class ScaledSliderRepresentation extends RegionBaseRepresentation<Slider, ScaledSliderWidget>
//TODO: consider placing under a ScaledWidgetBase superclass (with ProgressBar) or an IncrementedControl (with scrollbar, spinner)
    //consider also interfacing; perhaps make IncrementedControlWidget the interface
{
    private final DirtyFlag dirty_size = new DirtyFlag();
    private final DirtyFlag dirty_value = new DirtyFlag();
    private final DirtyFlag dirty_style = new DirtyFlag();

    private volatile double min = 0.0;
    private volatile double max = 100.0;
    private volatile double value = 50.0;
    private volatile double stepIncrement = 1.0;
    private volatile int tickCount = 20;

    @SuppressWarnings("nls")
    @Override
    protected Slider createJFXNode() throws Exception
    {
        Slider slider = new Slider();
        slider.setOrientation(model_widget.displayHorizontal().getValue() ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        slider.setFocusTraversable(true);
        slider.setTooltip(new Tooltip(""));
        slider.setOnKeyPressed((final KeyEvent event) ->
        {
            switch (event.getCode())
            {
            case DOWN: case LEFT: jfx_node.adjustValue(value-stepIncrement);
                break;
            case UP: case RIGHT: jfx_node.adjustValue(value+stepIncrement);
                break;
            case PAGE_UP:
                jfx_node.decrement();
                break;
            case PAGE_DOWN:
                jfx_node.increment();
                break;
            default: break;
            }
        });
        limitsChanged(null, null, null);
        styleChanged(null, null, null);
        slider.setValue(value);
        slider.setSnapToTicks(true);

        return slider;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.behaviorLimitsFromPV().addUntypedPropertyListener(this::limitsChanged);
        model_widget.behaviorMinimum().addUntypedPropertyListener(this::limitsChanged);
        model_widget.behaviorMaximum().addUntypedPropertyListener(this::limitsChanged);
        model_widget.displayHorizontal().addPropertyListener(this::sizeChanged);
        model_widget.behaviorStepIncrement().addPropertyListener(this::sizeChanged);
        model_widget.behaviorPageIncrement().addPropertyListener(this::sizeChanged);
        model_widget.displayForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayFillColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayShowScale().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayShowMinorTicks().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayScaleFormat().addUntypedPropertyListener(this::styleChanged);

        //Since both the widget's PV value and the JFX node's value property might be
        //written to independently during runtime, both must be listened to. Since ChangeListeners
        //only fire with an actual change, the listeners will not endlessly trigger each other.
        model_widget.runtimeValue().addPropertyListener(this::valueChanged);
        jfx_node.valueProperty().addListener(this::nodeValueChanged);

    }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        stepIncrement = model_widget.behaviorStepIncrement().getValue();
        double increment = model_widget.behaviorStepIncrement().getValue();
        //The node's majorTickUnit value should always be calculated from its
        //minorTickCount in order to avoid errors caused by casting to int.
        tickCount = (int) (calculateTickUnit() / increment) - 1;
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
        double min_val = model_widget.behaviorMinimum().getValue();
        double max_val = model_widget.behaviorMaximum().getValue();
        if (model_widget.behaviorLimitsFromPV().getValue())
        {
            //Try to get display range from PV
            final Display display_info = ValueUtil.displayOf(model_widget.runtimeValue().getValue());
            if (display_info != null)
            {
                min_val = display_info.getLowerDisplayLimit();
                max_val = display_info.getUpperDisplayLimit();
            }
        }
        //If invalid limits, fall back to 0..100 range
        if (min_val >= max_val)
        {
            min_val = 0.0;
            max_val = 100.0;
        }

        min = min_val;
        max = max_val;

        sizeChanged(null, null, null);
    }

    /*  Method for calculating the node's majorTickUnit property value. The actual value should
     *  be calculated from the integer minorTickCount (tick count per major unit) to maintain
     *  correct increments for snapping/stepping between minor ticks.
     */
    private double calculateTickUnit()
    {
        final int mtsh = model_widget.positionMajorTickStepHint().getValue();
        final int length = (model_widget.displayHorizontal().getValue() ?
                        model_widget.positionWidth().getValue() :
                        model_widget.positionHeight().getValue());
        final double range = max - min;
        return (range > 0 ? range : 100) / (length / mtsh);
    }

    @SuppressWarnings("nls")
    private void nodeValueChanged(ObservableValue<? extends Number> property, Number old_value, Number new_value)
    {
        jfx_node.getTooltip().setText(""+new_value);
        toolkit.fireWrite(model_widget, new_value);
    }

    private void valueChanged(final WidgetProperty<? extends VType> property, final VType old_value, final VType new_value)
    {
        dirty_value.mark();
        toolkit.scheduleUpdate(this);
    }

    @SuppressWarnings("nls")
    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            int save_count = tickCount;
            jfx_node.setPrefSize(model_widget.positionWidth().getValue(), model_widget.positionHeight().getValue());
            jfx_node.setMin(min);
            jfx_node.setMax(max);
            jfx_node.setOrientation(model_widget.displayHorizontal().getValue() ? Orientation.HORIZONTAL : Orientation.VERTICAL);
            jfx_node.setMinorTickCount(save_count);
            jfx_node.setMajorTickUnit((save_count + 1) * model_widget.behaviorStepIncrement().getValue());
            jfx_node.setBlockIncrement(model_widget.behaviorPageIncrement().getValue());
        }
        if (dirty_value.checkAndClear())
        {
            double newval = VTypeUtil.getValueNumber( model_widget.runtimeValue().getValue() ).doubleValue();
            if (newval < min) newval = min;
            else if (newval > max) newval = max;
            jfx_node.setValue(newval);
            value = newval;
        }
        if (dirty_style.checkAndClear())
        {
            //TODO: properly represent fg color, font
            //final String color = JFXUtil.webRGB(model_widget.displayForegroundColor().getValue());
            //jfx_node.setStyle("-fx-text-fill:" + color + ";-fx-stroke:" + color); //this doesn't do anything
            final Color background = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
            //jfx_node.setFont(JFXUtil.convert(model_widget.displayScaleFont().getValue()));
            final String format = model_widget.displayScaleFormat().getValue();
            jfx_node.setLabelFormatter(new FormatStringConverter<Double>(new DecimalFormat(format)));
            jfx_node.setShowTickLabels(model_widget.displayShowScale().getValue());
            jfx_node.setShowTickMarks(model_widget.displayShowMinorTicks().getValue());
        }
    }
}