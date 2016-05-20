package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.CheckBoxWidget;
import org.diirt.vtype.VType;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class CheckBoxRepresentation extends JFXBaseRepresentation<CheckBox, CheckBoxWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();

    //TODO: why Integer and int on BoolButton? thread-safety implications?
    protected volatile int bit = 0;
    protected volatile Integer value = 0;
    protected volatile boolean state = false;

    @Override
    protected final CheckBox createJFXNode() throws Exception
    {
        final CheckBox checkbox = new CheckBox(model_widget.displayLabel().getValue());
        checkbox.setMinSize(ButtonBase.USE_PREF_SIZE, ButtonBase.USE_PREF_SIZE);
        checkbox.setOnAction(event -> handlePress());
        return checkbox;
    }

    /** @param respond to button press */
    @SuppressWarnings("nls")
    private void handlePress()
    {
        logger.log(Level.FINE, "{0} pressed", model_widget);
        int new_val = (value ^ ((bit < 0) ? 1 : (1 << bit)) );
        toolkit.fireWrite(model_widget, new_val);
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::sizeChanged);
        model_widget.displayAutoSize().addUntypedPropertyListener(this::sizeChanged);

        bitChanged(model_widget.behaviorBit(), null, model_widget.behaviorBit().getValue());
        model_widget.behaviorBit().addPropertyListener(this::bitChanged);
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
   }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    private void bitChanged(final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value)
    {
        bit = new_value;
        stateChanged(new_value, value);
    }

    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        value = VTypeUtil.getValueNumber(new_value).intValue();
        stateChanged(bit, value);
    }

    private void stateChanged(final Integer new_bit, final int new_value)
    {
        state  = (new_bit < 0) ? (new_value != 0) : (((new_value >> new_bit) & 1) == 1);
        dirty_content.mark();
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
            if (model_widget.displayAutoSize().getValue())
                jfx_node.autosize();
        }
        if (dirty_content.checkAndClear())
        {
            jfx_node.setSelected(state);
        }
    }
}
