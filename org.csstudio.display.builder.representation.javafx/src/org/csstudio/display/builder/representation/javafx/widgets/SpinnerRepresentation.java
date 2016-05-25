package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.FormatOptionHandler;
import org.csstudio.display.builder.model.widgets.SpinnerWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VType;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
//This needs more work. Lots more work.
//TODO: handle changes to min/max
@SuppressWarnings("nls")
public class SpinnerRepresentation extends RegionBaseRepresentation<Spinner<VNumber>, SpinnerWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();

    protected volatile String value_text = "<?>";
    protected volatile Double max = 10.0;
    protected volatile Double min = 0.0;

    @Override
    protected final Spinner<VNumber> createJFXNode() throws Exception
    {
        final Spinner<VNumber> spinner = new Spinner<VNumber>();
        spinner.setValueFactory(createSVF());
        styleChanged(null, null, null);
        spinner.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        spinner.setEditable(true);
        return spinner;
    }

    private SpinnerValueFactory<VNumber> createSVF()
    {
        SpinnerValueFactory<VNumber> svf = new VNumberSpinnerValueFactory();
        svf.setValue((VNumber) model_widget.runtimeValue().getValue());
        return svf;
    }

    private class VNumberSpinnerValueFactory extends SpinnerValueFactory<VNumber>
    {
        // Constructors
        VNumberSpinnerValueFactory()
        {
            this(model_widget.behaviorMinimum().getDefaultValue(),
                 model_widget.behaviorMaximum().getDefaultValue(),
                 model_widget.behaviorStepIncrement().getDefaultValue());
        }

        VNumberSpinnerValueFactory(double min, double max, double stepIncrement)
        {
            setStepIncrement(stepIncrement);
            setMin(min);
            setMax(max);
            setConverter(new StringConverter<VNumber>()
            {
                @Override
                public String toString(VNumber object)
                {
                    return computeText(object);
                }

                @Override
                public VNumber fromString(String text)
                {
                    value_text = text;
                    final Object parsed = FormatOptionHandler.parse(model_widget.runtimeValue().getValue(), text);
                    logger.log(Level.FINE, "Writing '" + text + "' as " + parsed + " (" + parsed.getClass().getName() + ")");
                    toolkit.fireWrite(model_widget, parsed);
                    //The VNumber returned below may not reflect the text. However, the request to
                    //write has two outcomes:
                    //(a) The write is successful, causing contentChanged() to be called
                    //(b) The write fails. Thus, we schedule an update to restore the old text.
                    scheduleContentUpdate();
                    return (VNumber) model_widget.runtimeValue().getValue();
                }
            });
        }

        // Properties
        private DoubleProperty stepIncrement = new SimpleDoubleProperty(this, "stepIncrement");
        public final void setStepIncrement(double value)
        {
            stepIncrement.set(value);
        }
        public final double getStepIncrement()
        {
            return stepIncrement.get();
        }
        /**
         * Sets the amount to increment or decrement by, per step.
         */
        public final DoubleProperty stepIncrementProperty()
        {
            return stepIncrement;
        }

        private DoubleProperty min = new SimpleDoubleProperty(this, "min");
        public final void setMin(double value)
        {
            min.set(value);
        }
        public final double getMin()
        {
            return min.get();
        }
        /**
         * Sets the minimum possible value.
         */
        public final DoubleProperty minProperty()
        {
            return min;
        }

        private DoubleProperty max = new SimpleDoubleProperty(this, "max");
        public final void setMax(double value)
        {
            max.set(value);
        }
        public final double getMax()
        {
            return max.get();
        }
        /**
         * Sets the maximum possible value.
         */
        public final DoubleProperty maxProperty()
        {
            return max;
        }

        // Increment and decrement
        @Override
        public void decrement(int steps)
        {
            double value = this.getValue().getValue().doubleValue();
            if (Double.isNaN(value) || Double.isInfinite(value))
                return;
            double result = value - steps*getStepIncrement();
            if (result <= getMax() && result >= getMin())
                toolkit.fireWrite(model_widget, result);
        }

        @Override
        public void increment(int steps)
        {
            double value = this.getValue().getValue().doubleValue();
            if (Double.isNaN(value) || Double.isInfinite(value))
                return;
            double result = value + steps*getStepIncrement();
            toolkit.fireWrite(model_widget, result);
        }
    };

    /** @param value Current value of PV
     *  @return Text to show, "<pv name>" if disconnected (no value)
     */
    private String computeText(final VType value)
    {
        if (value == null)
            return "<" + model_widget.behaviorPVName().getValue() + ">";
        return FormatOptionHandler.format(value,
                                          model_widget.displayFormat().getValue(),
                                          model_widget.displayPrecision().getValue(),
                                          false);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayButtonsOnLeft().addPropertyListener(this::styleChanged);

        model_widget.displayForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);

        model_widget.behaviorStepIncrement().addUntypedPropertyListener(this::incrementChanged);

        model_widget.displayFormat().addUntypedPropertyListener(this::contentChanged);
        model_widget.displayPrecision().addUntypedPropertyListener(this::contentChanged);
        model_widget.runtimeValue().addUntypedPropertyListener(this::contentChanged);

        contentChanged(null, null, null);
        incrementChanged(null, null, null);
   }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void incrementChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        ( (VNumberSpinnerValueFactory)jfx_node.getValueFactory() )
            .setStepIncrement(model_widget.behaviorStepIncrement().getValue());
    }


    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        value_text = computeText(model_widget.runtimeValue().getValue());
        scheduleContentUpdate();
    }

    protected void scheduleContentUpdate()
    {
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_style.checkAndClear())
        {
            final String color = JFXUtil.webRGB(model_widget.displayForegroundColor().getValue());
            jfx_node.editorProperty().getValue().setStyle("-fx-text-fill:" + color);
            final Color background = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.editorProperty().getValue().setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
            jfx_node.setPrefWidth(model_widget.positionWidth().getValue());
            jfx_node.setPrefHeight(model_widget.positionHeight().getValue());
            int x = jfx_node.getStyleClass().indexOf(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL);
            if (model_widget.displayButtonsOnLeft().getValue())
            {
                if (x < 0)
                    jfx_node.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL);
            }
            else if (x > 0)
                jfx_node.getStyleClass().remove(x);
        }
        if (dirty_content.checkAndClear())
        {
            VType vtype = model_widget.runtimeValue().getValue();
            if (vtype instanceof VNumber)
                jfx_node.getValueFactory().setValue((VNumber) vtype);
        }
    }
}
