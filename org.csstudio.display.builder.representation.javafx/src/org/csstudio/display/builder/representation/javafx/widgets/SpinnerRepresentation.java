/*******************************************************************************
 * Copyright (c) 2015-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.FormatOptionHandler;
import org.csstudio.display.builder.model.widgets.SpinnerWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.Styles;
import org.diirt.vtype.Display;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueUtil;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    private final UntypedWidgetPropertyListener behaviorChangedListener = this::behaviorChanged;
    private final UntypedWidgetPropertyListener contentChangedListener = this::contentChanged;
    private final UntypedWidgetPropertyListener styleChangedListener = this::styleChanged;

    private Node pressedButton = null;

    protected volatile String value_text = "<?>";
    protected volatile VType  value      = null;
    private volatile double   value_max  = 100.0;
    private volatile double   value_min  = 0.0;

    @Override
    protected final Spinner<String> createJFXNode() throws Exception
    {
        final Spinner<String> spinner = new Spinner<String>();
        spinner.setValueFactory(createSVF());
        styleChanged(null, null, null);
        spinner.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        spinner.focusedProperty().addListener((property, oldval, newval)->
        {
            if (!spinner.isFocused())
                restore();
            active = false;
        });
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
            case PAGE_UP:
                if (!active)
                    jfx_node.getValueFactory().increment(1);
                break;
            case DOWN:
            case PAGE_DOWN:
                if (!active)
                    jfx_node.getValueFactory().decrement(1);
                break;
            default:
                // Any other key results in active state
                active = true;
            }
        });

        // While context menu is handled by SWT, there is a problem
        // when the primary button is held down to increment/decrement the spinner,
        // and _then_ the secondary button is used to open the context menu.
        // Releasing the primary button will in that case NOT stop the value changes
        // because SWT has the focus.
        // Fix is to trace the pressedButton,
        // and suppress context menu while button is held down,
        // and stopping incrementing/decrementing when exiting the spinner.
        spinner.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if ( e.getButton() == MouseButton.PRIMARY ) {

                Node node = e.getPickResult().getIntersectedNode();

                if ( node.getStyleClass().contains("increment-arrow-button")
                  || node.getStyleClass().contains("decrement-arrow-button") ) {
                    pressedButton = node;
                }

            } else if ( e.getButton() == MouseButton.SECONDARY ) {
                // Disable the contemporary triggering of a value change and of the
                // opening of contextual menu when right-clicking on the spinner's
                // buttons.
                e.consume();
            }
        });

        spinner.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if ( e.getButton() == MouseButton.PRIMARY ) {
                pressedButton = null;
            }
        });

        spinner.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {

            Node node = e.getPickResult().getIntersectedNode();

            // Contextual menu is forbidden inside the spinner's buttons, otherwise
            // if a button has focus (i.e. previously clicked), right-clicking on it
            // and, after the context menu id shown, double-clicking outside the
            // the menu and the spinner's boundary will close the menu and press
            // the button (probably because SWT remember the JFX cursor's position
            // when the menu was triggered).
            // This implementation will limit the context menu only inside the text area.
            if ( !node.getStyleClass().contains("increment-arrow-button")
              && !node.getStyleClass().contains("decrement-arrow-button") ) {
                spinner.getParent().fireEvent((Event) e.clone());
            }

            e.consume();

        });

        spinner.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            if ( pressedButton != null ) {
                pressedButton.fireEvent(e.copyFor(e.getSource(), e.getTarget(), MouseEvent.MOUSE_RELEASED));
                pressedButton = null;
            }
        });

        // This code manages layout,
        // because otherwise for example border changes would trigger
        // expensive Node.notifyParentOfBoundsChange() recursing up the scene graph
        spinner.setManaged(false);

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
                FormatOptionHandler.parse(model_widget.runtimePropValue().getValue(), text, model_widget.propFormat().getValue());
        if (value instanceof Number)
        {
            if (((Number)value).doubleValue() < value_min)
                value = value_min;
            else if (((Number)value).doubleValue() > value_max)
                value = value_max;
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
            this(model_widget.propMinimum().getDefaultValue(),
                 model_widget.propMaximum().getDefaultValue(),
                 model_widget.propIncrement().getDefaultValue());
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
            if (!toolkit.isEditMode() && model_widget.propEnabled().getValue())
                writeResultingValue(-steps*getStepIncrement());
        }

        @Override
        public void increment(int steps)
        {
            if (!toolkit.isEditMode() && model_widget.propEnabled().getValue())
                writeResultingValue(steps*getStepIncrement());
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
            return "<" + model_widget.propPVName().getValue() + ">";
        return FormatOptionHandler.format(value,
                                          model_widget.propFormat().getValue(),
                                          model_widget.propPrecision().getValue(),
                                          model_widget.propShowUnits().getValue());
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propWidth().addUntypedPropertyListener(styleChangedListener);
        model_widget.propHeight().addUntypedPropertyListener(styleChangedListener);
        model_widget.propButtonsOnLeft().addUntypedPropertyListener(styleChangedListener);

        model_widget.propForegroundColor().addUntypedPropertyListener(styleChangedListener);
        model_widget.propBackgroundColor().addUntypedPropertyListener(styleChangedListener);
        model_widget.propEnabled().addUntypedPropertyListener(styleChangedListener);

        model_widget.propIncrement().addUntypedPropertyListener(behaviorChangedListener);
        model_widget.propMinimum().addUntypedPropertyListener(behaviorChangedListener);
        model_widget.propMaximum().addUntypedPropertyListener(behaviorChangedListener);
        model_widget.propLimitsFromPV().addUntypedPropertyListener(behaviorChangedListener);

        model_widget.propFormat().addUntypedPropertyListener(contentChangedListener);
        model_widget.propPrecision().addUntypedPropertyListener(contentChangedListener);
        model_widget.propShowUnits().addUntypedPropertyListener(contentChangedListener);
        model_widget.runtimePropValue().addUntypedPropertyListener(contentChangedListener);

        behaviorChanged(null, null, null);
        contentChanged(null, null, null);
    }

    @Override
    protected void unregisterListeners()
    {
        model_widget.propWidth().removePropertyListener(styleChangedListener);
        model_widget.propHeight().removePropertyListener(styleChangedListener);
        model_widget.propButtonsOnLeft().removePropertyListener(styleChangedListener);
        model_widget.propForegroundColor().removePropertyListener(styleChangedListener);
        model_widget.propBackgroundColor().removePropertyListener(styleChangedListener);
        model_widget.propEnabled().removePropertyListener(styleChangedListener);
        model_widget.propIncrement().removePropertyListener(behaviorChangedListener);
        model_widget.propMinimum().removePropertyListener(behaviorChangedListener);
        model_widget.propMaximum().removePropertyListener(behaviorChangedListener);
        model_widget.propLimitsFromPV().removePropertyListener(behaviorChangedListener);
        model_widget.propFormat().removePropertyListener(contentChangedListener);
        model_widget.propPrecision().removePropertyListener(contentChangedListener);
        model_widget.propShowUnits().removePropertyListener(contentChangedListener);
        model_widget.runtimePropValue().removePropertyListener(contentChangedListener);
        super.unregisterListeners();
    }

    @Override
    protected boolean isFilteringEditModeClicks()
    {
        return true;
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void behaviorChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {

        updateLimits();

        final TextSpinnerValueFactory factory = (TextSpinnerValueFactory) jfx_node.getValueFactory();

        factory.setStepIncrement(model_widget.propIncrement().getValue());
        factory.setMin(value_min);
        factory.setMax(value_max);

    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {

        if ( model_widget.propLimitsFromPV().getValue() ) {
            behaviorChanged(null, null, null);
        }

        value = model_widget.runtimePropValue().getValue();
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
            final String color = JFXUtil.webRGB(model_widget.propForegroundColor().getValue());
            jfx_node.editorProperty().getValue().setStyle("-fx-text-fill:" + color);
            final Color background = JFXUtil.convert(model_widget.propBackgroundColor().getValue());
            jfx_node.editorProperty().getValue().setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
            jfx_node.resize(model_widget.propWidth().getValue(), model_widget.propHeight().getValue());

            final boolean enabled = model_widget.propEnabled().getValue();
            Styles.update(jfx_node, Styles.NOT_ENABLED, !enabled);
            jfx_node.setEditable(!toolkit.isEditMode() && enabled);

            int x = jfx_node.getStyleClass().indexOf(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL);
            if (model_widget.propButtonsOnLeft().getValue())
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

    /**
     * Updates, if required, the limits and zones.
     *
     * @return {@code true} is something changed and and UI update is required.
     */
    private boolean updateLimits ( ) {

        boolean somethingChanged = false;

        //  Model's values.
        double newMin = model_widget.propMinimum().getValue();
        double newMax = model_widget.propMaximum().getValue();

        //  If invalid limits, fall back to 0..100 range.
        if ( Double.isNaN(newMin) || Double.isNaN(newMax) || newMin > newMax ) {
            newMin = 0.0;
            newMax = 100.0;
        }

        if ( model_widget.propLimitsFromPV().getValue() ) {

            //  Try to get display range from PV.
            final Display display_info = ValueUtil.displayOf(model_widget.runtimePropValue().getValue());

            if ( display_info != null ) {

                double infoMin = display_info.getLowerCtrlLimit();
                double infoMax = display_info.getUpperCtrlLimit();

                if ( !Double.isNaN(infoMin) && !Double.isNaN(infoMax) && infoMin < infoMax ) {
                    newMin = infoMin;
                    newMax = infoMax;
                }

            }

        }

        if ( Double.compare(value_min, newMin) != 0 ) {
            value_min = newMin;
            somethingChanged = true;
        }
        if ( Double.compare(value_max, newMax) != 0 ) {
            value_max = newMax;
            somethingChanged = true;
        }

        return somethingChanged;

    }

}
