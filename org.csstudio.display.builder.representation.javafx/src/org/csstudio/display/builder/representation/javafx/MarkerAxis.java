package org.csstudio.display.builder.representation.javafx;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.control.Slider;

@SuppressWarnings("nls")
/**
 * {@link Axis} implementation for hi, lo, hihi, and lolo markers on a
 * linear scaled widget (i.e. Slider, Thermometer, Tank...).
 * Could potentially be extended for round widgets (e.g. Knob, Gauge).
 */
public class MarkerAxis extends Axis<String>
{
    DoubleBinding length;
    DoubleBinding min;
    DoubleBinding max;

    public MarkerAxis(Slider slider)
    {
        length = new DoubleBinding()
        {
            {
                super.bind(slider.widthProperty(), slider.heightProperty(), slider.orientationProperty());
            }

            @Override
            protected double computeValue()
            {
                return slider.getOrientation()==Orientation.HORIZONTAL ?
                        slider.getWidth() :
                        slider.getHeight();
            }
        };
        min = new DoubleBinding()
        {
            {
                super.bind(slider.minProperty());
            }

            @Override
            protected double computeValue()
            {
                return slider.getMin();
            }
        };
        max = new DoubleBinding()
        {
            {
                super.bind(slider.maxProperty());
            }

            @Override
            protected double computeValue()
            {
                return slider.getMax();
            }
        };

        slider.widthProperty().addListener((property, oldval, newval) ->
        {
            if (slider.getOrientation() == Orientation.HORIZONTAL)
                this.setWidth(newval.doubleValue());
        });
        slider.heightProperty().addListener((property, oldval, newval) ->
        {
            if (slider.getOrientation() == Orientation.VERTICAL)
                this.setHeight(newval.doubleValue());
        });
        slider.orientationProperty().addListener((property, oldval, newval) ->
            this.setSide(newval==Orientation.HORIZONTAL ? Side.TOP : Side.LEFT)
        );
        setSide(slider.getOrientation()==Orientation.HORIZONTAL ? Side.TOP : Side.LEFT);
    }

    private DoubleProperty hi = new SimpleDoubleProperty(this, "hi", 80);
    public void setHi(double value)
    {
        hi.set(value);
    }
    private DoubleProperty hihi = new SimpleDoubleProperty(this, "hihi", 90);
    public void setHiHi(double value)
    {
        hihi.set(value);
    }
    private DoubleProperty lo = new SimpleDoubleProperty(this, "lo", 20);
    public void setLo(double value)
    {
        lo.set(value);
    }
    private DoubleProperty lolo = new SimpleDoubleProperty(this, "lolo", 10);
    public void setLoLo(double value)
    {
        lolo.set(value);
    }

    //--- overridden methods
    @Override
    protected void setRange(Object range, boolean animate)
    {
        this.setSide(getSide() == null || getSide().isHorizontal() ? Side.TOP : Side.LEFT);
    }

    @Override
    protected Object getRange()
    {
        this.setSide(getSide() == null || getSide().isHorizontal() ? Side.TOP : Side.LEFT);
        return null;
    }

    @Override
    protected Object autoRange(double length)
    {
        this.setSide(getSide() == null || getSide().isHorizontal() ? Side.TOP : Side.LEFT);
        return null;
    }

    @Override
    public double getZeroPosition()
    {
        return Double.NaN;
    }

    private double offset = 0;

    @Override
    public double getDisplayPosition(String value)
    {
        final double scale = calculateScale();
        return offset + (toNumericValue(value) - min.get()) * scale;
    }

    @Override
    public String getValueForDisplay(double displayPosition)
    {
        final double scale = calculateScale();
        return toRealValue( (displayPosition-offset) / scale + min.get() );
    }

    @Override
    public boolean isValueOnAxis(String value)
    {
        double number = toNumericValue(value);
        return number >= min.get() && number <= max.get();
    }

    @Override
    public double toNumericValue(String value)
    {
        if ("lolo".equals(value))
            return lolo.get();
        if ("lo".equals(value))
            return lo.get();
        if ("hi".equals(value))
            return hi.get();
        if ("hihi".equals(value))
            return hihi.get();
        return 0;
    }

    @Override
    public String toRealValue(double value)
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
    protected String getTickMarkLabel(String value)
    {
        return value.toUpperCase();
    }

    @Override
    protected List<String> calculateTickValues(double length, Object range)
    {
        String [] markers = new String [] {"lolo", "lo", "hi", "hihi"};
        List<String> list = new ArrayList<String>();
        for (String mark : markers)
            if (isValueOnAxis(mark))
                list.add(mark);
        return list;
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
