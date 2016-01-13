/**
 * 
 */
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayLineWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayTransparent;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.BaseWidget;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;

/**
 * Widget that displays an arc
 * @author Megan Grodowitz
 *
 */
@SuppressWarnings("nls")
public class ArcWidget extends BaseWidget {

    /** Widget descriptor */
	//Does the opibuilder.widgets.Arc work? Guessed at the name...
    public static final WidgetDescriptor WIDGET_DESCRIPTOR =
        new WidgetDescriptor("arc", WidgetCategory.GRAPHIC,
            "Arc",
            "platform:/plugin/org.csstudio.display.builder.model/icons/arc.png",
            "An arc",
            Arrays.asList("org.csstudio.opibuilder.widgets.Arc"))
    {
        @Override
        public Widget createWidget()
        {
            return new ArcWidget();
        }
    };
    
    // fill color
    private WidgetProperty<WidgetColor> background;
    // Do we need transparency for arc? It appears that existing displays have clear arcs, so I think yes
    private WidgetProperty<Boolean> transparent;
    // line color and width
    private WidgetProperty<WidgetColor> line_color;
    private WidgetProperty<Integer> line_width;  
    // start/end degree of arc (0-365)
    private WidgetProperty<Double> arc_start;
    private WidgetProperty<Double> arc_end;
	

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
        
        //Looks like we need some new properties for displaying angles... 
        //properties.add(arc_start) = 
    }


}
