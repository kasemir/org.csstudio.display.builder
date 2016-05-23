package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.ScrollBarWidget;
import org.diirt.vtype.Display;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueUtil;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class ScrollBarRepresentation extends JFXBaseRepresentation<ScrollBar, ScrollBarWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_value = new DirtyFlag();

    private volatile double min = 0.0;
    private volatile double max = 100.0;

    @Override
    protected ScrollBar createJFXNode() throws Exception
    {
        ScrollBar scrollbar = new ScrollBar();
        scrollbar.setOrientation(model_widget.displayHorizontal().getValue() ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        limitsChanged(null, null, null);

        return scrollbar;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.behaviorLimitsFromPV().addUntypedPropertyListener(this::limitsChanged);
        model_widget.behaviorMinimum().addUntypedPropertyListener(this::limitsChanged);
        model_widget.behaviorMaximum().addUntypedPropertyListener(this::limitsChanged);
        model_widget.displayHorizontal().addPropertyListener(this::styleChanged);

        //Since both the widget's PV value and the ScrollBar node's value property might be
        //written to independently during runtime, both must be listened to. Since ChangeListeners
        //only fire with an actual change, the listeners will not endlessly trigger each other.
        model_widget.runtimeValue().addPropertyListener(this::valueChanged);
        jfx_node.valueProperty().addListener(this::nodeValueChanged);

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

        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void nodeValueChanged(ObservableValue<? extends Number> property, Number old_value, Number new_value)
    {
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
        if (dirty_style.checkAndClear())
        {
            jfx_node.setPrefHeight(model_widget.positionHeight().getValue());
            jfx_node.setPrefWidth(model_widget.positionWidth().getValue());
            jfx_node.setMin(min);
            jfx_node.setMax(max);
            jfx_node.setOrientation(model_widget.displayHorizontal().getValue() ? Orientation.HORIZONTAL : Orientation.VERTICAL);
        }
        if (dirty_value.checkAndClear())
        {
            double newval = VTypeUtil.getValueNumber( model_widget.runtimeValue().getValue() ).doubleValue();
            if (newval < min) newval = min;
            else if (newval > max) newval = max;
            jfx_node.setValue(newval);
        }
    }

}
