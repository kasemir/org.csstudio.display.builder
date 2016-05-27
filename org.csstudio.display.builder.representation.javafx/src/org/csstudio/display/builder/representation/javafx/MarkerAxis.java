package org.csstudio.display.builder.representation.javafx;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.layout.Region;

@SuppressWarnings("nls")
/**
 * {@link Axis} implementation for representing hi, lo, hihi, and lolo markers
 * on a linear scaled widget (i.e. Slider, Thermometer, Tank...).
 * Could potentially be extended for round widgets (e.g. Knob, Gauge).
 */
public abstract class MarkerAxis<T extends Region> extends Axis<String>
{
    /**
     * Instantiates a marker axis, initializing bindings and registering
     * property listeners associated with the node.
     * @param node Region-based node associated with the axis.
     */
    public MarkerAxis(T node)
    {
        initializeBindings(node);
        setSide(getSide() == null || getSide().isHorizontal() ? Side.TOP : Side.LEFT);
        node.widthProperty().addListener((property, oldval, newval) ->
        {
            if (getSide().isHorizontal())
            {
                setWidths(newval.doubleValue());
            }
        });
        node.heightProperty().addListener((property, oldval, newval) ->
        {
            if (getSide().isVertical())
            {
                setHeights(newval.doubleValue());
            }
        });
    }

    void setHeights(double value)
    {
        setMinHeight(value);
        setMaxHeight(value);
        setHeight(value);
    }

    void setWidths(double value)
    {
        setMinWidth(value);
        setMaxWidth(value);
        setWidth(value);
    }

    protected DoubleBinding length;
    protected DoubleBinding min;
    protected DoubleBinding max;

    /**
     * Initialize the DoubleBinding fields length, min, and max. The
     * node is provided to allow binding its properties.
     * @param node The node associated with the MarkerAxis.
     */
    protected abstract void initializeBindings(T node);

    //--- properties
    private final DoubleProperty hi = new SimpleDoubleProperty(this, "hi", 80);
    public final void setHi(double value)
    {
        hi.set(value);
        invalidateRange();
        layoutChildren();
    }
    public final double getHi()
    {
        return hi.get();
    }

    private final DoubleProperty hihi = new SimpleDoubleProperty(this, "hihi", 90);
    public final void setHiHi(double value)
    {
        hihi.set(value);
        invalidateRange();
        layoutChildren();
    }
    public final double getHiHi()
    {
        return hihi.get();
    }

    private final DoubleProperty lo = new SimpleDoubleProperty(this, "lo", 20);
    public final void setLo(double value)
    {
        lo.set(value);
        invalidateRange();
        layoutChildren();
    }
    public final double getLo()
    {
        return lo.get();
    }

    private final DoubleProperty lolo = new SimpleDoubleProperty(this, "lolo", 10);
    public final void setLoLo(double value)
    {
        lolo.set(value);
        invalidateRange();
        layoutChildren();
    }
    public final double getLoLo()
    {
        return lolo.get();
    }

    private final BooleanProperty showHi = new SimpleBooleanProperty(this, "showHi", true);
    public final void setShowHi(boolean value)
    {
        showHi.set(value);
        invalidateRange();
        layoutChildren();
    }
    public final boolean getShowHi()
    {
        return showHi.get();
    }

    private final BooleanProperty showHiHi = new SimpleBooleanProperty(this, "showHiHi", true);
    public final void setShowHiHi(boolean value)
    {
        showHiHi.set(value);
        invalidateRange();
        layoutChildren();
    }
    public final boolean getShowHiHi()
    {
        return showHiHi.get();
    }

    private final BooleanProperty showLo = new SimpleBooleanProperty(this, "showLo", true);
    public final void setShowLo(boolean value)
    {
        showLo.set(value);
        invalidateRange();
        layoutChildren();
    }
    public final boolean getShowLo()
    {
        return showLo.get();
    }

    private final BooleanProperty showLoLo = new SimpleBooleanProperty(this, "showLoLo", true);
    public final void setShowLoLo(boolean value)
    {
        showLoLo.set(value);
        invalidateRange();
        layoutChildren();
    }
    public final boolean getShowLoLo()
    {
        return showLoLo.get();
    }

    //--- overridden methods
    @Override
    protected void setRange(Object range, boolean animate)
    {
    }

    @Override
    protected Object getRange()
    {
        return null;
    }

    @Override
    protected Object autoRange(double length)
    {
        return null;
    }

    @Override
    public final double getZeroPosition()
    {
        return Double.NaN;
    }

    private double offset = 0;

    @Override
    public final double getDisplayPosition(String value)
    {
        final double scale = calculateScale();
        return offset + (toNumericValue(value) - min.get()) * scale;
    }

    @Override
    public final String getValueForDisplay(double displayPosition)
    {
        final double scale = calculateScale();
        return toRealValue( (displayPosition-offset) / scale + min.get() );
    }

    @Override
    public final boolean isValueOnAxis(String value)
    {
        double number = toNumericValue(value);
        if (number==lolo.get())
            return showLoLo.get();
        if (number==lo.get())
            return showLo.get();
        if (number==hi.get())
            return showHi.get();
        if (number==hihi.get())
            return showHiHi.get();
        return false;
    }

    @Override
    public final double toNumericValue(String value)
    {
        if ("lolo".equals(value))
            return lolo.get();
        if ("lo".equals(value))
            return lo.get();
        if ("hi".equals(value))
            return hi.get();
        if ("hihi".equals(value))
            return hihi.get();
        return Double.NaN;
    }

    @Override
    public final String toRealValue(double value)
    {
        if (value <= lolo.get())
            return "lolo";
        if (value <= lo.get())
            return "lo";
        if (value >= hi.get())
            return "hi";
        if (value >= hihi.get())
            return "hihi";
        return null;
    }

    @Override
    protected final String getTickMarkLabel(String value)
    {
        return value.toUpperCase();
    }

    @Override
    protected final List<String> calculateTickValues(double length, Object range)
    {
        String [] markers = new String [] {"lolo", "lo", "hi", "hihi"};
        List<String> list = new ArrayList<String>();
        for (String mark : markers)
            if (isValueOnAxis(mark))
                list.add(mark);
        return list;
    }

    //--- other methods
    /**
     * Adjusts side property of axis according to orientation. To be
     * called when orientation changes.
     * @param vertical Whether to make the axis vertical or, if false, horizontal.
     */
    protected void makeVertical(boolean vertical)
    {
        setSide(vertical ? Side.LEFT : Side.TOP);
        invalidateRange();
    }

    private double calculateScale()
    {
        final Side side = getSide();
        final double dataRange = max.get()-min.get();
        if (side!=null && side.isVertical())
        {
            offset = length.get();
            return (dataRange == 0) ? -length.get() : -(length.get() / dataRange);
        }
        offset = 0;
        return (dataRange == 0) ? length.get() : length.get() / dataRange;
    }
}
