package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetFactory;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.ArrayWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
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

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

@SuppressWarnings({ "nls", "static-access" })
public class ArrayRepresentation extends JFXBaseRepresentation<Pane, ArrayWidget>
{
    private final DirtyFlag dirty_value = new DirtyFlag(); //values of child widgets
    private final DirtyFlag dirty_number = new DirtyFlag(); //number of child widgets
    private final DirtyFlag dirty_look = new DirtyFlag(); //size/color of JavaFX Node

    private String did;

    private static final int inset = 10;

    private CopyOnWriteArrayList<Widget> children = new CopyOnWriteArrayList<>();
    private volatile int numChildren = 0, width = 0, height = 0;
    private volatile boolean isArranging = false, isAddingRemoving = false;
    private volatile Widget master = null;
    private Pane inner_pane;

    @Override
    protected Pane createJFXNode() throws Exception
    {
        model_widget.runtimeInsets().setValue(new int[] { inset, inset });
        inner_pane = new Pane();
        inner_pane.relocate(inset, inset);
        height = model_widget.positionHeight().getValue();
        width = model_widget.positionWidth().getValue();
        return new Pane(inner_pane);
    }

    @Override
    protected Parent getChildParent(final Parent parent)
    {
        return inner_pane;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.runtimeValue().addUntypedPropertyListener(this::valueChanged);
        model_widget.runtimeChildren().addPropertyListener(this::childrenChanged);
        model_widget.positionHeight().addPropertyListener(this::sizeChanged);
        model_widget.positionWidth().addPropertyListener(this::sizeChanged);
        model_widget.displayForegroundColor().addPropertyListener(this::colorChanged);
        model_widget.displayBackgroundColor().addPropertyListener(this::colorChanged);

        childrenChanged(null, null, model_widget.runtimeChildren().getValue());
        adjustNumberByLength();
        did = model_widget.getDID();
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_value.checkAndClear())
        {
            setChildrenValues();
        }
        if (dirty_number.checkAndClear())
        {
            final int diff = children.size() - numChildren;
            if (diff != 0)
            {
                isAddingRemoving = true;
                if (diff > 0)
                    removeChildren(children, diff);
                else
                    addChildren(children, -diff);
                isAddingRemoving = false;
            }
            arrangeChildren();
        }
        if (dirty_look.checkAndClear())
        {
            if (height > 0)
                jfx_node.setPrefHeight(height);
            if (width > 0)
                jfx_node.setPrefWidth(width);
            Color color = JFXUtil.convert(model_widget.displayForegroundColor().getValue());
            jfx_node.setBorder(new Border(
                    new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT,
                            new Insets(inset / 2))));
            color = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(color, null, null)));
        }
    }

    private void colorChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_look.mark();
        toolkit.scheduleUpdate(this);
    }

    private void valueChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_value.mark();
        toolkit.scheduleUpdate(this);
    }

    private void sizeChanged(final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value)
    {
        if (!isArranging && (old_value != new_value || old_value == null))
        {
            width = model_widget.positionWidth().getValue();
            height = model_widget.positionHeight().getValue();
            adjustNumberByLength();

            dirty_look.mark();
            toolkit.scheduleUpdate(this);
        } else
            isArranging = false;
    }

    private void childrenChanged(final WidgetProperty<List<Widget>> property, final List<Widget> removed,
            final List<Widget> added)
    {
        List<Widget> newval = new ArrayList<Widget>(model_widget.runtimeChildren().getValue());
        if (!isAddingRemoving)
        {
            numChildren = newval.size();
            dirty_number.mark();
            toolkit.scheduleUpdate(this);
            if (added != null && !added.isEmpty() && !added.get(0).checkProperty("pv_name").isPresent())
            {
                toolkit.logger
                        .warning("Child " + added.get(0) + " added to " + model_widget
                                + " has no 'pv_name' property.");
            }
        }
        if (added != null)
        {
            for (Widget widget : added)
                addChildListeners(widget);
            //trigger child listeners to copy properties of existing widgets
            if (!added.isEmpty()) //implies !newval.isEmpty()
            {
                Widget widget = added.get(0);
                copyProperties(newval.get(0), widget);
                master = widget;
            }
        }
        else //removed != null
            for (Widget widget : removed)
                removeChildListeners(widget);
        children = new CopyOnWriteArrayList<>(newval);
    }

    private void addChildListeners(Widget widget)
    {
        Iterator<WidgetProperty<?>> it = widget.getProperties().iterator();
        while (it.hasNext())
        {
            WidgetProperty<?> prop = it.next();
            if (!prop.getCategory().equals(model_widget.runtimeChildren().getCategory())
                    && (!prop.getCategory().equals(model_widget.positionHeight().getCategory())
                            || prop.getName().equals(model_widget.positionVisible().getName()))
                    && !prop.getCategory().equals(model_widget.widgetName().getCategory())
                    && !prop.getName().equals(model_widget.behaviorPVName().getName()))
            {
                //toolkit.logger.finest("Array widget adding listener to " + widget + " " + prop);
                prop.addUntypedPropertyListener(listener);
            }
            else if (prop.getCategory().equals(model_widget.positionHeight().getCategory())
                    && !prop.getName().equals(model_widget.positionVisible().getCategory()))
            {
                prop.addUntypedPropertyListener(rearrange);
            }
            if (prop.getName().equals("horizontal"))
            {
                prop.addUntypedPropertyListener(rearrange);
            }
        }
    }

    private void removeChildListeners(Widget widget)
    {
        Iterator<WidgetProperty<?>> it = widget.getProperties().iterator();
        while (it.hasNext())
        {
            WidgetProperty<?> prop = it.next();
            if (!prop.getCategory().equals(model_widget.runtimeChildren().getCategory())
                    && (!prop.getCategory().equals(model_widget.positionHeight().getCategory())
                            || prop.getName().equals(model_widget.positionVisible().getName()))
                    && !prop.getCategory().equals(model_widget.widgetName().getCategory())
                    && !prop.getName().equals(model_widget.behaviorPVName().getName()))
            {
                //toolkit.logger.finest("Array widget removing listener from " + widget + " " + prop);
                prop.removePropertyListener(listener);
            }
            if (prop.getName().equals("horizontal"))
            {
                prop.removePropertyListener(rearrange);
            }
        }
    }

    UntypedWidgetPropertyListener listener = (p, o, n) ->
    {
        //toolkit.logger.finest("Array widget listener called: " + p.getWidget() + " " + p);
        if (!isArranging)
        {
            final String name = p.getName();
            final Object value = p.getValue();
            for (Widget w : children)
            {
                if (w.equals(p.getWidget()))
                    continue;
                try
                {
                    w.setPropertyValue(name, value);
                } catch (Exception ignored)
                {
                }
            }
        }
    };

    UntypedWidgetPropertyListener rearrange = (p, o, n) ->
    {
        if (!isArranging)
        {
            master = p.getWidget();
            dirty_number.mark(); //dirty_number calls arrangeChildren
            toolkit.scheduleUpdate(this);
        }
    };

    private void arrangeChildren()
    {
        List<Widget> children = new ArrayList<Widget>(model_widget.runtimeChildren().getValue());
        Widget master = this.master;
        if (children.isEmpty())
            return;
        if (master == null)
            master = children.get(0);

        isArranging = true;
        numChildren = children.size();

        //Checking horizontal:
        //This isn't a very robust way of doing it, but neither is getting the name from a particular
        //widget's horizontal property descriptor.
        Optional<WidgetProperty<?>> horizontal = master.checkProperty("horizontal");
        final boolean vertical = !horizontal.isPresent() || (boolean) horizontal.get().getValue();

        final int h = vertical ? master.positionHeight().getValue()
                : (height = model_widget.positionHeight().getValue()) - inset * 2;
        final int w = vertical ? (width = model_widget.positionWidth().getValue()) - inset * 2
                : master.positionWidth().getValue();
        int len = 0;
        for (Widget child : children)
        {
            child.positionHeight().setValue(h);
            child.positionWidth().setValue(w);
            child.positionX().setValue(vertical ? 0 : len);
            child.positionY().setValue(vertical ? len : 0);
            len += vertical ? h : w;
        }
        len += inset * 2;
        if (vertical && len != height)
            model_widget.positionHeight().setValue(height = len);
        else if (!vertical && len != width)
            model_widget.positionWidth().setValue(width = len);
        else
            isArranging = false;
        dirty_look.mark();
        toolkit.scheduleUpdate(this);
    }

    private void adjustNumberByLength()
    {
        List<Widget> children = new ArrayList<Widget>(model_widget.runtimeChildren().getValue());
        if (children.isEmpty())
            return;
        Optional<WidgetProperty<?>> horizontal = children.get(0).checkProperty("horizontal");
        final boolean vertical = !horizontal.isPresent() || (boolean) horizontal.get().getValue();
        final int l = vertical ? children.get(0).positionHeight().getValue()
                : children.get(0).positionWidth().getValue();
        numChildren = vertical ? (height - inset * 2) / l : (width - inset * 2) / l;
        dirty_number.mark();
        toolkit.scheduleUpdate(this);
    }

    private void addChildren(List<Widget> children, int number)
    {
        if (number > 0 && !children.isEmpty())
        {
            Widget child = copyWidget(children.get(0));
            child.widgetName().setValue(model_widget.getName() + "-" + child.getType() + "-" + this.children.size());
            if (child != null)
            {
                model_widget.runtimeChildren().addChild(child);
                addChildren(children, number - 1);
            }
        }
    }

    private void removeChildren(List<Widget> children, int number)
    {
        if (number > 0 && children.size() > 1)
        {
            Widget child = children.remove(children.size() - 1);
            model_widget.runtimeChildren().removeChild(child);
            removeChildren(children, number - 1);
        }
    }

    private Widget copyWidget(Widget original)
    {
        try
        {
            Widget copy = WidgetFactory.getInstance().getWidgetDescriptor(original.getType()).createWidget();
            copyProperties(original, copy);
            return copy;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void copyProperties(Widget original, Widget copy)
    {
        if (original.equals(copy))
            return;
        Iterator<WidgetProperty<?>> it = copy.getProperties().iterator();
        while (it.hasNext())
        {
            final WidgetProperty<?> prop = it.next();
            if (prop.getName().equals(original.widgetName().getName()))
                continue;
            try
            {
                final String name = prop.getName();
                prop.setValue(original.getPropertyValue(name));
            } catch (Exception e)
            {
                toolkit.logger.log(Level.WARNING, "Cannot copy " + original + " " + prop, e);
            }
        }
    }

    void setChildrenValues()
    {
        List<?> values = readValues(model_widget.runtimeValue().getValue());
        if (values.size() < 1)
            return;

        //Set values of children widgets using local pvs delivered through the 'pv_name'
        //property. This makes the child widgets essentially read-only.
        final String pvPrefix = "loc://arr" + did + "_el";
        final String pvtype = pvType(values.get(0));
        for (int i = 0; i < values.size() && i < children.size(); i++)
        {
            try
            {
                final String locPV = pvPrefix + i + pvtype + values.get(i) + ')';
                children.get(i).setPropertyValue("pv_name", locPV);
            } catch (IllegalArgumentException e)
            { //thrown by setPropertyValue if "pv_name" is unknown
                break;
            } catch (IndexOutOfBoundsException e)
            {
                break;
            } catch (Exception e)
            { //if new pv_name value is unsuitable; unlikely, since "pv_name" should be a String
                e.printStackTrace();
            }
        }
    }

    private String pvType(Object value)
    {
        if (value instanceof String)
            return "<VString>(";
        else //if (value instanceof Number), etc.
            return "(";
    }

    private List<?> readValues(VType vtype)
    {
        final int n = numChildren;
        if (vtype == null)
            return Collections.emptyList();
        if (vtype instanceof Array)
        {
            //require one-dimensional? ((Array)vtype).getSizes().size() == 1?
            if (vtype instanceof VNumberArray)
            {
                ListNumber dataList = (ListNumber) ((VNumberArray) vtype).getData();
                if (vtype instanceof VByteArray)
                    return readBytes(dataList.iterator(), n);
                else if (vtype instanceof VIntArray)
                    return readInts(dataList.iterator(), n);
                else if (vtype instanceof VLongArray)
                    return readLongs(dataList.iterator(), n);
                else if (vtype instanceof VShortArray)
                    return readShorts(dataList.iterator(), n);
                else if (vtype instanceof VFloatArray)
                    return readFloats(dataList.iterator(), n);
                else if (vtype instanceof VDoubleArray)
                    return readDoubles(dataList.iterator(), n);
                else
                {
                    //throw Exception: Unsupported VNumberArray sub-type?
                }
            }
            else
            {
                List<?> data = (List<?>) ((Array) vtype).getData();
                return (data).subList(0, Math.min(n, data.size()));
            }
        }
        else //vtype not instanceof Array; could treat as single-element array, but ignoring for now
        {
            return Collections.emptyList();
        }
        return null;
    }

    //Since there is no generic next() or nextNumber() method for
    //the iterator over the list given by a VNumberArray, there can
    //be no generic read or readNumber
    private List<Byte> readBytes(IteratorNumber it, int n)
    {
        List<Byte> list = new ArrayList<Byte>(n);
        while (it.hasNext() && n-- > -1)
            list.add(it.nextByte());
        return list;
    }

    private List<Integer> readInts(IteratorNumber it, int n)
    {
        List<Integer> list = new ArrayList<Integer>(n);
        while (it.hasNext() && n-- > 0)
            list.add(it.nextInt());
        return list;
    }

    private List<Long> readLongs(IteratorNumber it, int n)
    {
        List<Long> list = new ArrayList<Long>(n);
        while (it.hasNext() && n-- > 0)
            list.add(it.nextLong());
        return list;
    }

    private List<Short> readShorts(IteratorNumber it, int n)
    {
        List<Short> list = new ArrayList<Short>(n);
        while (it.hasNext() && n-- > 0)
            list.add(it.nextShort());
        return list;
    }

    private List<Float> readFloats(IteratorNumber it, int n)
    {
        List<Float> list = new ArrayList<Float>(n);
        while (it.hasNext() && n-- > 0)
            list.add(it.nextFloat());
        return list;
    }

    private List<Double> readDoubles(IteratorNumber it, int n)
    {
        List<Double> list = new ArrayList<Double>(n);
        while (it.hasNext() && n-- > 0)
            list.add(it.nextDouble());
        return list;
    }
}
