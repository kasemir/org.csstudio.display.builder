package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.csstudio.display.builder.model.widgets.ArrayWidget;
import org.diirt.util.array.IteratorNumber;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.Array;
import org.diirt.vtype.VByteArray;
import org.diirt.vtype.VDoubleArray;
import org.diirt.vtype.VFloatArray;
import org.diirt.vtype.VIntArray;
import org.diirt.vtype.VLongArray;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VShortArray;
import org.diirt.vtype.VType;

import javafx.scene.layout.Pane;

public class ArrayRepresentation extends JFXBaseRepresentation<Pane, ArrayWidget>
{
    @Override
    protected Pane createJFXNode() throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    //

    @SuppressWarnings("unchecked")
    List<?> readValues()
    {
        VType vtype = model_widget.runtimeValue().getValue();
        if (vtype instanceof Array)
        {
            //require one-dimensional? ((Array)vtype).getSizes().size() == 1?
            //limit to getting only needed number of values?
            //consider also reading only 
            if (vtype instanceof VNumberArray)
            {
                ListNumber dataList = (ListNumber) ((VNumberArray) vtype).getData();
                if (vtype instanceof VByteArray)
                    return readBytes(dataList.iterator());
                else if (vtype instanceof VIntArray)
                    return readInts(dataList.iterator());
                else if (vtype instanceof VLongArray)
                    return readLongs(dataList.iterator());
                else if (vtype instanceof VShortArray)
                    return readShorts(dataList.iterator());
                else if (vtype instanceof VFloatArray)
                    return readFloats(dataList.iterator());
                else if (vtype instanceof VDoubleArray)
                    return readDoubles(dataList.iterator());
                else
                {
                    //throw Exception: Unsupported VNumberArray sub-type?
                }
            }
            else
            { //Boolean, Enum, String; possibly other
                /*if (vtype instanceof VStringArray || vtype instanceof VEnumArray)
                    return (List<String>) ((Array) vtype).getData();
                else if (vtype instanceof VBooleanArray)
                    return (List<Boolean>) ((Array) vtype).getData();
                else*/
                    return (List<?>) ((Array) vtype).getData();
            }
        }
        else //vtype not instanceof Array; could treat as single-element array, but ignoring for now
        {
            return Collections.emptyList();
            //((VNumber) vtype).getValue();
            //((VString) vtype).getValue();
            //((VEnum) vtype).getValue();
            //((VBoolean) vtype).getValue();
        }
        return null;
    }

    //Since there is no generic next() or nextNumber() method for
    //the iterator over the list given by a VNumberArray, there can
    //be no generic read or readNumber
    List<Byte> readBytes(IteratorNumber it)
    {
        List<Byte> list = new ArrayList<Byte>();
        while (it.hasNext())
            list.add(it.nextByte());
        return list;
    }

    List<Integer> readInts(IteratorNumber it)
    {
        List<Integer> list = new ArrayList<Integer>();
        while (it.hasNext())
            list.add(it.nextInt());
        return list;
    }

    List<Long> readLongs(IteratorNumber it)
    {
        List<Long> list = new ArrayList<Long>();
        while (it.hasNext())
            list.add(it.nextLong());
        return list;
    }

    List<Short> readShorts(IteratorNumber it)
    {
        List<Short> list = new ArrayList<Short>();
        while (it.hasNext())
            list.add(it.nextShort());
        return list;
    }

    List<Float> readFloats(IteratorNumber it)
    {
        List<Float> list = new ArrayList<Float>();
        while (it.hasNext())
            list.add(it.nextFloat());
        return list;
    }

    List<Double> readDoubles(IteratorNumber it)
    {
        List<Double> list = new ArrayList<Double>();
        while (it.hasNext())
            list.add(it.nextDouble());
        return list;
    }
}
