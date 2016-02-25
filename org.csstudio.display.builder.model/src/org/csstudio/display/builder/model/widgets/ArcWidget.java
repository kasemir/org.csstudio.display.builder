/**
 *
 */
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayTransparent;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newDoublePropertyDescriptor;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.WidgetColor;

/**
 * Widget that displays an arc
 * @author Megan Grodowitz
 *
 */
@SuppressWarnings("nls")
public class ArcWidget extends VisibleWidget {

    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("arc", WidgetCategory.GRAPHIC,
            "Arc",
            "platform:/plugin/org.csstudio.display.builder.model/icons/arc.png",
            "An arc",
            Arrays.asList("org.csstudio.opibuilder.widgets.arc"))
    {
        @Override
        public Widget createWidget()
        {
            return new ArcWidget();
        }
    };

    //TODO: change start_anlge and total_angle to new terms. Setup input configurator to handle old terms
    private static final WidgetPropertyDescriptor<Double> displayAngleStart =
            newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "start_angle", Messages.WidgetProperties_AngleStart);

    private static final WidgetPropertyDescriptor<Double> displayAngleSize =
            newDoublePropertyDescriptor(WidgetPropertyCategory.DISPLAY, "total_angle", Messages.WidgetProperties_AngleSize);

    // fill color
    private WidgetProperty<WidgetColor> background;
    // Do we need transparency for arc? It appears that existing displays have clear arcs, so I think yes
    private WidgetProperty<Boolean> transparent;
    // line color and width
    private WidgetProperty<WidgetColor> line_color;
    private WidgetProperty<Integer> line_width;
    // start/size degree of arc (0-365)
    private WidgetProperty<Double> arc_start;
    private WidgetProperty<Double> arc_size;


	public ArcWidget() {
		super(WIDGET_DESCRIPTOR.getType());
	}

	// By default create an arc with dark blue line, light blue interior, no transparency, 90 degree angle from 0-90
    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(background = displayBackgroundColor.createProperty(this, new WidgetColor(30, 144, 255)));
        properties.add(transparent = displayTransparent.createProperty(this, false));
        properties.add(line_color = displayLineColor.createProperty(this, new WidgetColor(0, 0, 255)));
        properties.add(line_width = displayLineWidth.createProperty(this, 3));
        properties.add(arc_start = displayAngleStart.createProperty(this, 0.0));
        properties.add(arc_size = displayAngleSize.createProperty(this, 90.0));
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** @return Display 'transparent' */
    public WidgetProperty<Boolean> displayTransparent()
    {
        return transparent;
    }

    /** @return Display 'line_color' */
    public WidgetProperty<WidgetColor> displayLineColor()
    {
        return line_color;
    }

    /** @return Display 'line_width' */
    public WidgetProperty<Integer> displayLineWidth()
    {
        return line_width;
    }

    /** @return Display 'arc_start' */
    public WidgetProperty<Double> displayArcStart()
    {
        return arc_start;
    }

    /** @return Display 'arc_size' */
    public WidgetProperty<Double> displayArcSize()
    {
        return arc_size;
    }
}
