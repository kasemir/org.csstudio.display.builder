package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayForegroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFormat;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayPrecision;
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
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.FormatOption;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.diirt.vtype.VType;

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

    //TODO? configurator?

    public static final WidgetPropertyDescriptor<Integer> behaviorStepIncrement =
            CommonWidgetProperties.newIntegerPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "step_increment", Messages.Spinner_StepIncrement);

    public static final WidgetPropertyDescriptor<Integer> behaviorPageIncrement =
            CommonWidgetProperties.newIntegerPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "page_increment", Messages.Spinner_PageIncrement);

    public static final WidgetPropertyDescriptor<Boolean> displayButtonsOnLeft =
            CommonWidgetProperties.newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "buttons_on_left", Messages.Spinner_ButtonsOnLeft);

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile WidgetProperty<WidgetColor> foreground;
    private volatile WidgetProperty<WidgetFont> font;
    private volatile WidgetProperty<FormatOption> format;
    private volatile WidgetProperty<Integer> precision;
    private volatile WidgetProperty<VType> value;
    //Minimum (minimum) //Lower limit of the widget.
    //Maximum (maximum)
    //Limits From PV(limits_from_pv)
    //increments: configurable at Runtime from its context menu Configure Runtime Properties....
        //does this make runtime category?
    private volatile WidgetProperty<Integer> step_increment;
    private volatile WidgetProperty<Integer> page_increment;
    private volatile WidgetProperty<Boolean> buttons_on_left;

    public SpinnerWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(pv_name = behaviorPVName.createProperty(this, ""));
        properties.add(background = displayBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.WRITE_BACKGROUND)));
        properties.add(foreground = displayForegroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
        properties.add(font = displayFont.createProperty(this, NamedWidgetFonts.DEFAULT));
        properties.add(format = displayFormat.createProperty(this, FormatOption.DEFAULT));
        properties.add(precision = displayPrecision.createProperty(this, 2));
        properties.add(value = runtimeValue.createProperty(this, null));
        //TODO: properties.add: minimum, maximum, limits_from_pv
        properties.add(step_increment = behaviorStepIncrement.createProperty(this, 1));
        properties.add(page_increment = behaviorPageIncrement.createProperty(this, 1));
        properties.add(buttons_on_left = displayButtonsOnLeft.createProperty(this, false));
    }

    /** @return Behavior 'pv_name' */
    public WidgetProperty<String> behaviorPVName()
    {
        return pv_name;
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** @return Display 'foreground_color' */
    public WidgetProperty<WidgetColor> displayForegroundColor()
    {
        return foreground;
    }

    /** @return Display 'font' */
    public WidgetProperty<WidgetFont> displayFont()
    {
        return font;
    }

    /** @return Display 'format' */
    public WidgetProperty<FormatOption> displayFormat()
    {
        return format;
    }

    /** @return Display 'precision' */
    public WidgetProperty<Integer> displayPrecision()
    {
        return precision;
    }

    /** @return Runtime 'value' */
    public WidgetProperty<VType> runtimeValue()
    {
        return value;
    }

    //TODO: return minimum, maximum, limits_from_pv

    /** @return Behavior 'step_increment' */
    public WidgetProperty<Integer> behaviorStepIncrement()
    {
        return step_increment;
    }

    /** @return Behavior 'step_increment' */
    public WidgetProperty<Integer> behaviorPageIncrement()
    {
        return page_increment;
    }

    /** @return Display 'buttons_on_left' */
    public WidgetProperty<Boolean> displayButtonsOnLeft()
    {
        return buttons_on_left;
    }
}
