package org.csstudio.display.builder.representation.javafx.widgets;

import java.text.DecimalFormat;

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
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.util.converter.FormatStringConverter;

@SuppressWarnings("nls")
//big TODO: layout is very very strange
public class ScaledSliderRepresentation extends RegionBaseRepresentation<Pane, ScaledSliderWidget>
//TODO: consider placing under a ScaledWidgetBase superclass (with ProgressBar) or an IncrementedControl (with scrollbar, spinner)
    //consider also interfacing; perhaps make IncrementedControlWidget the interface
{
    private final DirtyFlag dirty_size = new DirtyFlag();
    private final DirtyFlag dirty_value = new DirtyFlag();
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_orientation = new DirtyFlag();

    private volatile double min = 0.0;
    private volatile double max = 100.0;
    private volatile double value = 50.0;
    private volatile double stepIncrement = 1.0;
    private volatile int tickCount = 20;

    //border for debugging layout //TODO: after debug layout, remove all setBorder() calls
    final Border blackborder = new Border(new BorderStroke(new Color(0, 0, 0, 1),
                                                            new BorderStrokeStyle(StrokeType.OUTSIDE,
                                                                                  StrokeLineJoin.MITER,
                                                                                  StrokeLineCap.BUTT,
                                                                                  10, 0, null),
                                                            CornerRadii.EMPTY,
                                                            new BorderWidths(1)));
    final Border blueborder = new Border(new BorderStroke(new Color(0, 0, 1, 1),
                                                          new BorderStrokeStyle(StrokeType.OUTSIDE,
                                                                                StrokeLineJoin.MITER,
                                                                                StrokeLineCap.BUTT,
                                                                                10, 0, null),
                                                          CornerRadii.EMPTY,
                                                          new BorderWidths(1)));

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
                    return node.getOrientation() == Orientation.HORIZONTAL ?
                            node.getWidth() :
                            node.getHeight();
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


    /** The jfx_node pane is a wrapper for a subpane, which is recreated
     * as a new VBox or HBox as orientation changes. */
    @Override
    protected Pane createJFXNode() throws Exception
    {
        final Pane pane = new Pane(createSubPane());
        //axis.setBorder(blackborder);
        return pane;
    }

    private Pane createSubPane()
    {
        Pane newpane = slider.getOrientation()==Orientation.HORIZONTAL ? new VBox(axis, slider) : new HBox(axis, slider);
        if (slider.getOrientation()==Orientation.HORIZONTAL)
            ((VBox)newpane).setFillWidth(true);
        else
            ((HBox)newpane).setFillHeight(true);
        newpane.setBorder(blackborder);
        return newpane;
    }

    private Slider createSlider()
    {
        Slider slider = new Slider();
        slider.setFocusTraversable(true);
        slider.setTooltip(new Tooltip(""));
        slider.setOnKeyPressed((final KeyEvent event) ->
        {
            switch (event.getCode())
            {
            case DOWN: case LEFT:
                slider.adjustValue(value-stepIncrement);
                break;
            case UP: case RIGHT:
                slider.adjustValue(value+stepIncrement);
                break;
            case PAGE_UP:
                slider.decrement();
                break;
            case PAGE_DOWN:
                slider.increment();
                break;
            default: break;
            }
        });
        slider.setValue(value);
        slider.setSnapToTicks(true);
        slider.setBorder(blueborder);
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
        model_widget.displayHorizontal().addPropertyListener(this::orientChanged);
        model_widget.behaviorStepIncrement().addPropertyListener(this::sizeChanged);
        model_widget.behaviorPageIncrement().addPropertyListener(this::sizeChanged);
        //model_widget.displayForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        //model_widget.displayFillColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayShowScale().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayShowMinorTicks().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayScaleFormat().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayLevelHi().addUntypedPropertyListener(this::sizeChanged);
        model_widget.displayLevelHiHi().addUntypedPropertyListener(this::sizeChanged);
        model_widget.displayLevelLo().addUntypedPropertyListener(this::sizeChanged);
        model_widget.displayLevelLoLo().addUntypedPropertyListener(this::sizeChanged);

        //Since both the widget's PV value and the JFX node's value property might be
        //written to independently during runtime, both must have listeners.
        model_widget.runtimeValue().addPropertyListener(this::valueChanged);
        slider.valueProperty().addListener(this::nodeValueChanged);

        limitsChanged(null, null, null);
        styleChanged(null, null, null);
        orientChanged(null, null, null);
    }

    private void orientChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_orientation.mark();
        toolkit.scheduleUpdate(this);
    }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        stepIncrement = model_widget.behaviorStepIncrement().getValue();
        //The node's majorTickUnit value should always be calculated from its
        //minorTickCount in order to avoid errors caused by casting to int.
        tickCount = (int) (calculateTickUnit() / stepIncrement) - 1;
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

    /*  Method for calculating the slider's majorTickUnit property value. The actual value should
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

    private void nodeValueChanged(ObservableValue<? extends Number> property, Number old_value, Number new_value)
    {
        slider.getTooltip().setText(""+new_value);
        toolkit.fireWrite(model_widget, new_value);
    }

    private void valueChanged(final WidgetProperty<? extends VType> property, final VType old_value, final VType new_value)
    {
        dirty_value.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            double w = model_widget.positionWidth().getValue();
            double h = model_widget.positionHeight().getValue();
            jfx_node.setPrefSize(w, h);
            Pane subpane = (Pane) jfx_node.getChildren().get(0);
            subpane.setMinSize(w, h);
            subpane.setMaxSize(w, h);
            if (model_widget.displayHorizontal().getValue())
                slider.setPrefWidth(w);
            else
                slider.setPrefHeight(h);
            subpane.requestLayout();
            int save_count = tickCount;
            slider.setMin(min);
            slider.setMax(max);
            slider.setMinorTickCount(save_count);
            slider.setMajorTickUnit((save_count + 1) * model_widget.behaviorStepIncrement().getValue());
            slider.setBlockIncrement(model_widget.behaviorPageIncrement().getValue());
            axis.setHi(model_widget.displayLevelHi().getValue());
            axis.setHiHi(model_widget.displayLevelHiHi().getValue());
            axis.setLo(model_widget.displayLevelLo().getValue());
            axis.setLoLo(model_widget.displayLevelLoLo().getValue());
        }
        if (dirty_value.checkAndClear())
        {
            double newval = VTypeUtil.getValueNumber( model_widget.runtimeValue().getValue() ).doubleValue();
            if (newval < min) newval = min;
            else if (newval > max) newval = max;
            slider.setValue(newval);
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
            slider.setLabelFormatter(new FormatStringConverter<Double>(new DecimalFormat(format)));
            slider.setShowTickLabels(model_widget.displayShowScale().getValue());
            slider.setShowTickMarks(model_widget.displayShowMinorTicks().getValue());
        }
        if (dirty_orientation.checkAndClear())
        {
            final boolean horizontal = model_widget.displayHorizontal().getValue();
            slider.setOrientation(horizontal ? Orientation.HORIZONTAL : Orientation.VERTICAL);
            Pane subpane = createSubPane();
            if (horizontal)
                slider.setPrefWidth(jfx_node.getPrefWidth());
            else
                slider.setPrefHeight(jfx_node.getPrefHeight());
            subpane.setMinSize(jfx_node.getPrefWidth(), jfx_node.getPrefHeight());
            subpane.setMaxSize(jfx_node.getPrefWidth(), jfx_node.getPrefHeight());
            jfx_node.getChildren().clear();
            jfx_node.getChildren().add(subpane);
        }
    }
}