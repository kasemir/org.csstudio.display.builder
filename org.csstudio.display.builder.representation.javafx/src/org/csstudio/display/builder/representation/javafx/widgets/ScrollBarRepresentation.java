package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.ScrollBarWidget;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class ScrollBarRepresentation extends JFXBaseRepresentation<ScrollBar, ScrollBarWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();

    private volatile double min = 0.0;
    private volatile double max = 10.0;

    @Override
    protected ScrollBar createJFXNode() throws Exception
    {
        ScrollBar scrollbar = new ScrollBar();
        scrollbar.setOrientation(model_widget.displayHorizontal().getValue() ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        scrollbar.setMin(min);
        scrollbar.setMax(max);

        return scrollbar;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizeChanged);
        jfx_node.valueProperty().addListener(this::writeNewValue);
    }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    private void writeNewValue(ObservableValue<? extends Number> property, Number old_value, Number new_value)
    {
        toolkit.fireWrite(model_widget, new_value);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            jfx_node.setPrefHeight(model_widget.positionHeight().getValue());
            jfx_node.setPrefWidth(model_widget.positionWidth().getValue());
        }
    }

}
