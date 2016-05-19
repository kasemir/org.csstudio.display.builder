package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorBit;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newStringPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.diirt.vtype.VType;

@SuppressWarnings("nls")
public class CheckboxWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("checkbox", WidgetCategory.CONTROL,
            "Checkbox",
            "platform:/plugin/org.csstudio.display.builder.model/icons/checkbox.png",
            "Read/write a bit in a PV",
            Arrays.asList("org.csstudio.opibuilder.widgets.checkbox") )
    {
        @Override
        public Widget createWidget()
        {
            return new RectangleWidget();
        }
    };
    /** Display 'label': Text for label */
    public static final WidgetPropertyDescriptor<String> displayLabel =
        newStringPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "label", Messages.Checkbox_Label);

    //TODO? displayAutoSize ("auto_size")

    private volatile WidgetProperty<Integer> bit;
    private volatile WidgetProperty<VType> value;

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(behaviorPVName.createProperty(this, ""));
        properties.add(bit = behaviorBit.createProperty(this, 0));
        properties.add(value = runtimeValue.createProperty(this, null));
    }

    public CheckboxWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    /** @return Behavior 'bit' */
    public WidgetProperty<Integer> behaviorBit()
    {
        return bit;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }

}
