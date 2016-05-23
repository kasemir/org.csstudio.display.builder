package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;

/** Widget that can read/write numeric PV via scrollbar
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class ScrollBarWidget extends VisibleWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("scrollbar", WidgetCategory.CONTROL,
            "Scrollbar",
            "platform:/plugin/org.csstudio.display.builder.model/icons/scrollbar.png",
            "A scrollbar that can read/write a numeric PV",
            Arrays.asList("org.csstudio.opibuilder.widgets.scrollbar"))
    {
        @Override
        public Widget createWidget()
        {
            return new ScrollBarWidget();
        }
    };

    private volatile WidgetProperty<WidgetColor> background;
    //etc.

    public ScrollBarWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(background = displayBackgroundColor.createProperty(this, new WidgetColor(30, 144, 255)));
        //etc.
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }
    //etc.
}