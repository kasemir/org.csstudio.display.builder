package org.csstudio.display.builder.model.widgets;

import java.util.Arrays;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;

/** Widget that represents a spinner
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class SpinnerWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("spinner", WidgetCategory.CONTROL,
            "Spinner",
            "platform:/plugin/org.csstudio.display.builder.model/icons/Spinner.png",
            "A spinner, with up/down arrows",
            Arrays.asList("org.csstudio.opibuilder.widgets.Spinner"))//TODO: check on this
        {
            @Override
            public Widget createWidget()
            {
                return new SpinnerWidget();
            }
        };

    //private volatile WidgetProperty<??> ??;

    public SpinnerWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }


}
