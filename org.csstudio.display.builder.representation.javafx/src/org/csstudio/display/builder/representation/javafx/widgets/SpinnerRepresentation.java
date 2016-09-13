/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
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
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class SpinnerRepresentation extends RegionBaseRepresentation<Spinner<String>, SpinnerWidget>
{
    /** Is user actively editing the content, so updates should be suppressed? */
    private volatile boolean active = false;

    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();

    protected volatile String value_text = "<?>";
    protected volatile VType value = null;

    @Override
    protected final Spinner<String> createJFXNode() throws Exception
    {
        final Spinner<String> spinner = new Spinner<String>();
        spinner.setValueFactory(createSVF());
        styleChanged(null, null, null);
        spinner.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        if (!toolkit.isEditMode())
            spinner.setEditable(true);
        spinner.focusedProperty().addListener((property, oldval, newval)->
        {
            if (!spinner.isFocused())
                restore();
            active = false;
        });
        //don't respond to click in edit mode; instead, select widget
        if (toolkit.isEditMode())
        {
            spinner.getEditor().setOnMousePressed((event) ->
            {
                event.consume();
                toolkit.fireClick(model_widget, event.isControlDown());
            });
        }
        spinner.getEditor().setOnKeyPressed((final KeyEvent event) ->
        {
            switch (event.getCode())
            {
            case ESCAPE: //TODO: fix: escape key event not sensed
                // Revert original value, leave active state
                restore();
                active = false;
                break;
            case ENTER:
                // Submit value, leave active state
                submit();
                active = false;
                break;
            //incrementing by keyboard
            case UP:
                if (!active)
                    jfx_node.getValueFactory().increment(1);
                break;
            case DOWN:
                if (!active)
                    jfx_node.getValueFactory().decrement(1);
                break;
            case PAGE_UP:
                if (!active)
                    ((TextSpinnerValueFactory)jfx_node.getValueFactory()).pageIncrement(1);
                break;
            case PAGE_DOWN:
                if (!active)
                    ((TextSpinnerValueFactory)jfx_node.getValueFactory()).pageDecrement(1);
                break;
            default:
                // Any other key results in active state
                active = true;
            }
        });
        return spinner;
    }

    /** Restore representation to last known value,
     *  replacing what user might have entered
     */
    private void restore()
    {
        //The old value is restored.
        jfx_node.getEditor().setText(jfx_node.getValueFactory().getValue());
    }

    /** Submit value entered by user */
    private void submit()
    {
        //The value factory retains the old values, and will be updated as scheduled below.
        final String text = jfx_node.getEditor().getText();
        Object value =
                FormatOptionHandler.parse(model_widget.runtimeValue().getValue(), text);
        double min = model_widget.behaviorMinimum().getValue();
        double max = model_widget.behaviorMaximum().getValue();
        if (value instanceof Number)
        {
            if (((Number)value).doubleValue() < min)
                value = min;
            else if (((Number)value).doubleValue() > max)
                value = max;
        }
        logger.log(Level.FINE, "Writing '" + text + "' as " + value + " (" + value.getClass().getName() + ")");
        toolkit.fireWrite(model_widget, value);

        // Wrote value. Expected is either
        // a) PV receives that value, PV updates to
        //    submitted value or maybe a 'clamped' value
        // --> We'll receive contentChanged() and update the value factory.
        // b) PV doesn't receive the value and never sends
        //    an update. The value factory retains the old value,
        // --> Schedule an update to the new value.
        //
        // This could result in a little flicker:
        // User enters "new_value".
        // We send that, but retain "old_value" to handle case b)
        // PV finally sends "new_value", and we show that.
        //
        // In practice, this rarely happens because we only schedule an update.
        // By the time it executes, we already have case a.
        // If it does turn into a problem, could introduce toolkit.scheduleDelayedUpdate()
        // so that case b) only restores the old 'value_text' after some delay,
        // increasing the chance of a) to happen.
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    private SpinnerValueFactory<String> createSVF()
    {
        SpinnerValueFactory<String> svf = new TextSpinnerValueFactory();
        svf.setValue(value_text);
        return svf;
    }

    private class TextSpinnerValueFactory extends SpinnerValueFactory<String>
    {
        // Constructors
        TextSpinnerValueFactory()
        {
            this(model_widget.behaviorMinimum().getDefaultValue(),
                 model_widget.behaviorMaximum().getDefaultValue(),
                 model_widget.behaviorStepIncrement().getDefaultValue());
        }

        TextSpinnerValueFactory(double min, double max, double stepIncrement)
        {
            setStepIncrement(stepIncrement);
            setMin(min);
            setMax(max);
            setConverter(new StringConverter<String>()
            {
                @Override
                public String toString(String object)
                {
                    return object;
                }

                @Override
                public String fromString(String text)
                {
                    return text;
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

        private DoubleProperty pageIncrement = new SimpleDoubleProperty(this, "pageIncrement");
        public final void setPageIncrement(double value)
        {
            pageIncrement.set(value);
        }
        public final double getPageIncrement()
        {
            return pageIncrement.get();
        }
        /**
         * Sets the amount to increment or decrement by when page up/down buttons pressed.
         */
        public final DoubleProperty pageIncrementProperty()
        {
            return pageIncrement;
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

        //TODO: Is really better to have this separate?
        private ObjectPropertyBase<VType> vtypeValue = new ObjectPropertyBase<VType>()
        {
            @Override
            public Object getBean()
            {
                return this; //return the TextSpinnerValueFactory
            }
            @Override
            public String getName()
            {
                return "vtypeValue";
            }

        };
        public final void setVTypeValue(VType value)
        {
            vtypeValue.set(value);
        }
        public final VType getVTypeValue()
        {
            return vtypeValue.get();
        }
        /**
         * Sets the associated VType value.
         */
        //implement if needed: public final ObjectPropertyBase<VType> vtypeValueProperty()

        // Increment and decrement
        @Override
        public void decrement(int steps)
        {
            writeResultingValue(-steps*getStepIncrement());
        }

        @Override
        public void increment(int steps)
        {
            writeResultingValue(steps*getStepIncrement());
        }

        public void pageDecrement(int steps)
        {
            writeResultingValue(-steps*getPageIncrement());
        }

        public void pageIncrement(int steps)
        {
            writeResultingValue(steps*getPageIncrement());
        }

        private void writeResultingValue(double change)
        {
            double value;
            if (!(getVTypeValue() instanceof VNumber))
            {
                scheduleContentUpdate();
                return;
            }
            value = ((VNumber)getVTypeValue()).getValue().doubleValue();
            if (Double.isNaN(value) || Double.isInfinite(value)) return;
            value += change;
            if (value < getMin()) value = getMin();
            else if (value > getMax()) value = getMax();
            toolkit.fireWrite(model_widget, value);
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
        model_widget.propWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayButtonsOnLeft().addPropertyListener(this::styleChanged);

        model_widget.displayForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);

        model_widget.behaviorStepIncrement().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.behaviorPageIncrement().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.behaviorMinimum().addUntypedPropertyListener(this::behaviorChanged);
        model_widget.behaviorMaximum().addUntypedPropertyListener(this::behaviorChanged);

        model_widget.displayFormat().addUntypedPropertyListener(this::contentChanged);
        model_widget.displayPrecision().addUntypedPropertyListener(this::contentChanged);
        model_widget.runtimeValue().addUntypedPropertyListener(this::contentChanged);

        contentChanged(null, null, null);
        behaviorChanged(null, null, null);
   }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void behaviorChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        TextSpinnerValueFactory factory = (TextSpinnerValueFactory)jfx_node.getValueFactory();
        factory.setStepIncrement(model_widget.behaviorStepIncrement().getValue());
        factory.setPageIncrement(model_widget.behaviorPageIncrement().getValue());
        factory.setMin(model_widget.behaviorMinimum().getValue());
        factory.setMax(model_widget.behaviorMaximum().getValue());
    }


    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        value = model_widget.runtimeValue().getValue();
        value_text = computeText(value);
        scheduleContentUpdate();
    }

    private void scheduleContentUpdate()
    {
        dirty_content.mark();
        if (!active)
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
            jfx_node.setPrefWidth(model_widget.propWidth().getValue());
            jfx_node.setPrefHeight(model_widget.propHeight().getValue());
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
            ( (TextSpinnerValueFactory)jfx_node.getValueFactory() ).setVTypeValue(value);
            jfx_node.getValueFactory().setValue(value_text);

        }
    }
}
