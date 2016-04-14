/*******************************************************************************
 * Copyright (c) 2014-2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.csstudio.javafx.rtplot.data.PlotDataItem;
import org.csstudio.javafx.rtplot.data.PlotDataProvider;
import org.csstudio.javafx.rtplot.internal.AnnotationImpl;
import org.csstudio.javafx.rtplot.internal.MouseMode;
import org.csstudio.javafx.rtplot.internal.Plot;
import org.csstudio.javafx.rtplot.internal.ToolbarHandler;
import org.csstudio.javafx.rtplot.internal.TraceImpl;
import org.csstudio.javafx.rtplot.internal.util.GraphicsUtils;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** Real-time plot
 *
 *  @param <XTYPE> Data type used for the {@link PlotDataItem}
 *  @author Kay Kasemir
 */
@SuppressWarnings({ "nls", "restriction" })
public class RTPlot<XTYPE extends Comparable<XTYPE>> extends BorderPane
{
    final protected Plot<XTYPE> plot;
    final protected ToolbarHandler<XTYPE> toolbar;
//    final private ToggleToolbarAction<XTYPE> toggle_toolbar;
//    final private ToggleLegendAction<XTYPE> toggle_legend;
//    final private Action snapshot;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected RTPlot(final Class<XTYPE> type)
    {
    	// To avoid unchecked casts, factory methods for..() would need
        // pass already constructed Plot<T> and Toolbar<T>, where T is set,
        // into constructor.
        if (type == Double.class)
        {
            plot = (Plot) new Plot<Double>(Double.class);
            toolbar = (ToolbarHandler) new ToolbarHandler<Double>((RTPlot)this);
//            toggle_toolbar = (ToggleToolbarAction) new ToggleToolbarAction<Double>((RTPlot)this, true);
//            toggle_legend = (ToggleLegendAction) new ToggleLegendAction<Double>((RTPlot)this, true);
//            snapshot = new SnapshotAction(this);
        }
        else if (type == Instant.class)
        {
            plot = (Plot) new Plot<Instant>(Instant.class);
            toolbar = (ToolbarHandler) new ToolbarHandler<Instant>((RTPlot)this);
//            toggle_toolbar = (ToggleToolbarAction) new ToggleToolbarAction<Double>((RTPlot)this, true);
//            toggle_legend = (ToggleLegendAction) new ToggleLegendAction<Double>((RTPlot)this, true);
//            snapshot = new SnapshotAction(this);
        }
        else
            throw new IllegalArgumentException("Cannot handle " + type.getName());

        // Canvas, i.e. plot, is not directly size-manageable by a layout.
        // --> Let BorderPane resize 'center', then plot binds to is size.
        final Pane center = new Pane(plot);
        plot.widthProperty().bind(center.widthProperty());
        plot.heightProperty().bind(center.heightProperty());
        setCenter(center);
        showToolbar(true);

        addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);

//        toolbar.addContextMenu(toggle_toolbar);
    }

    /** onKeyPressed */
    private void keyPressed(final KeyEvent event)
    {
        // TODO Only receives key presses when toolbar is visible?!
        System.out.println("RTPlot.keyPressed: " + event);
        if (event.isControlDown())
            toolbar.selectMouseMode(MouseMode.ZOOM_IN);
        else if (event.isAltDown())
            toolbar.selectMouseMode(MouseMode.ZOOM_OUT);
        else if (event.isShiftDown())
            toolbar.selectMouseMode(MouseMode.PAN);
        else
            toolbar.selectMouseMode(MouseMode.NONE);
    }

    /** @param listener Listener to add */
    public void addListener(final RTPlotListener<XTYPE> listener)
    {
        plot.addListener(listener);
    }

    /** @param listener Listener to remove */
    public void removeListener(final RTPlotListener<XTYPE> listener)
    {
        plot.removeListener(listener);
    }

//    /** @return Control for the plot, to attach context menu */
//    public Control getPlotControl()
//    {
//        return plot;
//    }
//
//    /** @return {@link Action} that can show/hide the legend */
//    public Action getLegendAction()
//    {
//        return toggle_legend;
//    }
//
//    /** @return {@link Action} that can show/hide the toolbar */
//    public Action getToolbarAction()
//    {
//        return toggle_toolbar;
//    }
//
//    /** @return {@link Action} that saves a snapshot of the plot */
//    public Action getSnapshotAction()
//    {
//        return snapshot;
//    }

    /** @param color Background color */
    public void setBackground(final Color color)
    {
        plot.setBackground(GraphicsUtils.convert(Objects.requireNonNull(color)));
    }

    /** @param title Title text */
    public void setTitle(final Optional<String> title)
    {
        plot.setTitle(title);
    }

    /** @param font Font to use for title */
    public void setTitleFont(final Font font)
    {
        plot.setTitleFont(GraphicsUtils.convert(Objects.requireNonNull(font)));
    }

    /** @param font Font to use for legend */
    public void setLegendFont(final Font font)
    {
        plot.setLegendFont(GraphicsUtils.convert(Objects.requireNonNull(font)));
    }

//    /** @return {@link Image} of current plot. Caller must dispose */
//    public Image getImage()
//    {
//        return plot.getImage();
//    }

    /** @return <code>true</code> if legend is visible */
    public boolean isLegendVisible()
    {
        return plot.isLegendVisible();
    }

    /** @param show <code>true</code> if legend should be displayed */
    public void showLegend(final boolean show)
    {
        if (isLegendVisible() == show)
            return;
        plot.showLegend(show);
//        toggle_legend.updateText();
    }

    /** @return <code>true</code> if toolbar is visible */
    public boolean isToolbarVisible()
    {
        return getTop() != null;
    }

    /** @param show <code>true</code> if toolbar should be displayed */
    public void showToolbar(final boolean show)
    {
        if (isToolbarVisible() == show)
            return;
        if (show)
            setTop(toolbar.getToolBar());
        else
            setTop(null);
        plot.fireToolbarChange(show);
    }

    /** Add a custom tool bar button
     *  @param icon Icon {@link Image}
     *  @param tool_tip Tool tip text
     *  @return {@link Button}
     */
    public Button addToolItem(final Image icon, final String tool_tip)
    {
        return toolbar.addItem(icon, tool_tip);
    }

    /** @param show Show the cross-hair cursor? */
    public void showCrosshair(final boolean show)
    {
        plot.showCrosshair(show);
    }

    /** Stagger the range of axes */
    public void stagger()
    {
        plot.stagger();
    }

    /** @param mode New {@link MouseMode}
     *  @throws IllegalArgumentException if mode is internal
     */
    public void setMouseMode(final MouseMode mode)
    {
        plot.setMouseMode(mode);
    }

    /** @return {@link UndoableActionManager} for this plot */
    public UndoableActionManager getUndoableActionManager()
    {
        return plot.getUndoableActionManager();
    }

    /** @return X/Time axis */
    public Axis<XTYPE> getXAxis()
    {
        return plot.getXAxis();
    }

    /** Add another Y axis
     *  @param name
     *  @return Y Axis that was added
     */
    public YAxis<XTYPE> addYAxis(final String name)
    {
        return plot.addYAxis(name);
    }

    /** @return Y axes */
    public List<YAxis<XTYPE>> getYAxes()
    {
        final List<YAxis<XTYPE>> result = new ArrayList<>();
        result.addAll(plot.getYAxes());
        return Collections.unmodifiableList(result);
    }

    /** @param index Index of Y axis to remove */
    public void removeYAxis(final int index)
    {
        plot.removeYAxis(index);
    }

    /** @param name Name, must not be <code>null</code>
     *  @param units Units, may be <code>null</code>
     *  @param data
     *  @param color
     *  @param type
     *  @param width
     *  @param y_axis
     *  @return {@link Trace} that was added
     */
    public Trace<XTYPE> addTrace(final String name, final String units,
            final PlotDataProvider<XTYPE> data,
            final Color color,
            final TraceType type, final int width,
            final PointType point_type, final int size,
            final int y_axis)
    {
        final TraceImpl<XTYPE> trace = new TraceImpl<XTYPE>(name, units, data, color, type, width, point_type, size, y_axis);
        plot.addTrace(trace);
        return trace;
    }

    /** @return Thread-safe, read-only traces of the plot */
    public Iterable<Trace<XTYPE>> getTraces()
    {
        return plot.getTraces();
    }

    /** @return Count the number of traces */
    public int getTraceCount(){
    return plot.getTraceCount();
    }

    /** @param trace Trace to move from its current Y axis
     *  @param new_y_axis Index of new Y Axis
     */
    public void moveTrace(final Trace<XTYPE> trace, final int new_y_axis)
    {
        plot.moveTrace((TraceImpl<XTYPE>)trace, new_y_axis);
    }

    /** @param trace Trace to remove */
    public void removeTrace(final Trace<XTYPE> trace)
    {
        plot.removeTrace(trace);
    }

    /** Update the dormant time between updates
     *  @param dormant_time How long throttle remains dormant after a trigger
     *  @param unit Units for the dormant period
     */
    public void setUpdateThrottle(final long dormant_time, final TimeUnit unit)
    {
        plot.setUpdateThrottle(dormant_time, unit);
    }

    /** Request a complete redraw of the plot */
    public void requestUpdate()
    {
        plot.requestUpdate();
    }

    /** @param trace Trace to which an annotation should be added
     *  @param text Text for the annotation
     */
    public void addAnnotation(final Trace<XTYPE> trace, final String text)
    {
        plot.addAnnotation(trace, text);
    }

    /** @param annotation Annotation to add */
    public void addAnnotation(final Annotation<XTYPE> annotation)
    {
        plot.addAnnotation(annotation);
    }

    /** @return Current {@link AnnotationImpl}s */
    public List<Annotation<XTYPE>> getAnnotations()
    {
        return Collections.unmodifiableList(plot.getAnnotations());
    }

    /** Update text of annotation
     *  @param annotation {@link Annotation} to update.
     *         Must be an existing annotation obtained from <code>getAnnotations()</code>
     *  @param text New text
     *  @throws IllegalArgumentException if annotation is unknown
     */
    public void updateAnnotation(final Annotation<XTYPE> annotation, final String text)
    {
        plot.updateAnnotation(annotation, text);
    }

    /** @param annotation Annotation to remove */
    public void removeAnnotation(final Annotation<XTYPE> annotation)
    {
        plot.removeAnnotation(annotation);
    }

    /** Should be invoked when plot no longer used to release resources */
    public void dispose()
    {
    	plot.dispose();
    }
}
