package org.csstudio.display.builder.model.widgets;

import java.util.Arrays;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;

public class CheckboxWidget
{
    /** Widget descriptor */
    @SuppressWarnings("nls")
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

}
