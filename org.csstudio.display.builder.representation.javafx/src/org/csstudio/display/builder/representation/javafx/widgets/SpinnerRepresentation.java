package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.SpinnerWidget;

import javafx.scene.control.Spinner;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class SpinnerRepresentation extends JFXBaseRepresentation<Spinner, SpinnerWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();

    protected volatile Integer value = 0;

    @Override
    protected final Spinner createJFXNode() throws Exception
    {
        //SpinnerValueFactory()
        final Spinner spinner = new Spinner();
        //adjust spinner
        return spinner;
    }


    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::sizeChanged);
   }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }


    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            jfx_node.setPrefWidth(model_widget.positionWidth().getValue());
            jfx_node.setPrefHeight(model_widget.positionHeight().getValue());
        }
    }
}
