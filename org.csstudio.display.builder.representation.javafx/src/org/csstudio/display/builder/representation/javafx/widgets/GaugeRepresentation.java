/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.GaugeWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VType;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/** Creates JavaFX item for model widget
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class GaugeRepresentation extends RegionBaseRepresentation<Pane, GaugeWidget>
{
    protected final Logger logger = Logger.getLogger(getClass().getName());

    /** Mark when the static part of the gauge (background image) changes due to
     *  resize or change in colors or style
     */
    private final DirtyFlag dirty_background = new DirtyFlag();
    /** Mark when the dynamic part of the gauge (needle that moves) changes due to
     * resize, change in reading or change in colors or style
     */
    private final DirtyFlag dirty_foreground = new DirtyFlag();

    // Following values come from Gauge Widget Model
    private volatile Color needle_color;
    private volatile java.awt.Color bg_color;
    /** Angle size of gauge (30-360) */
    private volatile int gauge_ang_deg = 270;
    /** Are we using text labels? */
    private volatile boolean use_labels;

    // Following values will be set by examining PV Vtype
    /** Min and max displayed values on gauge */
    private volatile double display_min = 0.0, display_max = 100.0;
    /** Upper and lower warning (Yellow) limit */
    private volatile double lower_warn = 10.0, upper_warn = 90.0;
    /** Upper and lower alarm (Red) limit */
    private volatile double lower_alarm = 5.0, upper_alarm = 95.0;

    /** Current reading of the gauge (PV value) */
    private volatile double current_value;
    /** Is the PV connected */
    private volatile boolean connected = false;
    /** Current rotation of the needle based on value and gauge range */
    private volatile double needle_rot_ang;

    //Alarm colors
    private final java.awt.Color color_ok = java.awt.Color.GREEN;
    private final java.awt.Color color_warn = java.awt.Color.YELLOW;
    private final java.awt.Color color_alarm = java.awt.Color.RED;

    //Following values change on widget resize
    /** diameter of gauge */
    private volatile int gauge_diam = 100;

    //Javafx objects we need to draw the gauge
    /** canvas where we draw the gauge background */
    private volatile Canvas canvas;
    /** needle that shows gauge reading */
    private volatile Polygon needle;
    /** javafx arc outline to make the gauge pretty */
    private volatile Arc outline;
    /** rotation for the needle object */
    private volatile Rotate rotationTransform = new Rotate(0, 0, 0);
    /** translation for the needle object */
    private volatile Translate xlateNeedle = new Translate(0, -(gauge_diam / 4));

    /** Buffered image that gets drawn with Java.awt */
    private volatile BufferedImage buf;
    private volatile WritableImage image;



    @Override
    public Pane createJFXNode() throws Exception
    {
        createColors();
        //value_color = colors[0];

        canvas = new Canvas (100,100);

        needle = new Polygon();
        needle.getTransforms().addAll(xlateNeedle, rotationTransform);

        outline = new Arc();
        outline.setFill(Color.TRANSPARENT);
        outline.setStroke(Color.BLACK);
        outline.setStrokeWidth(2);
        outline.setStrokeType(StrokeType.CENTERED);
        outline.setType(ArcType.ROUND);

        final Pane pane = new Pane();
        pane.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        pane.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        pane.getChildren().addAll(canvas, outline, needle);

        return pane;
    }

    private void createColors()
    {
        needle_color = JFXUtil.convert(model_widget.needleColor().getValue());
        final WidgetColor bgc = model_widget.bgColor().getValue();
        bg_color = new java.awt.Color(bgc.getRed(), bgc.getGreen(), bgc.getBlue());
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        sizeChanged(null,null,null);
        model_widget.positionWidth().addPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addPropertyListener(this::sizeChanged);

        model_widget.bgColor().addUntypedPropertyListener(this::backgroundChanged);
        model_widget.needleColor().addUntypedPropertyListener(this::foregroundChanged);

        model_widget.runtimeValue().addPropertyListener(this::contentChanged);

        //When the PV connects, need to collect its alarm limit data
        model_widget.runtimeConnected().addPropertyListener(this::connectionChanged);
    }

    /** The PV connection state has changed. The gauge ranges and alarm values must be recollected.
     * This also means that the background must be redrawn, the needle reconfigured, and the needle
     * rotation recalculated, since these are all dependent on the gauge ranges. */
    private void connectionChanged(final WidgetProperty<Boolean> property, final Boolean was_connected, final Boolean is_connected)
    {
        connected = is_connected;
        if (is_connected) {
            collectPVMetaData(model_widget.runtimeValue().getValue());
        }
        else {
            //set dummy meta data for dico'd PV
        }
        redraw_background();
        reconfig_needle();
        calc_needle_rotation();
        dirty_foreground.mark();
        dirty_background.mark();
    }

    private void checkLimits()
    {
        // TODO check and correct any problems with limits
        // log any problems corrected
    }

    private void collectPVMetaData(VType value)
    {
        if (value instanceof VNumber) {
            VNumber vnum = (VNumber)value;
            lower_alarm = vnum.getLowerAlarmLimit();
            upper_alarm = vnum.getUpperAlarmLimit();

            lower_warn = vnum.getLowerWarningLimit();
            upper_warn = vnum.getUpperWarningLimit();

            display_min = vnum.getLowerCtrlLimit();
            display_max = vnum.getUpperCtrlLimit();
        }
        checkLimits();
    }


    /** Widget has been resized. Both the needle and the background need to be redrawn */
    private void sizeChanged(final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value)
    {
        final int w = model_widget.positionWidth().getValue();
        final int h = model_widget.positionHeight().getValue();
        gauge_diam = ( w > h ) ? h : w;
        canvas.setWidth(w);
        canvas.setHeight(h);

        redraw_background();
        reconfig_needle();
        dirty_background.mark();
        dirty_foreground.mark();
        toolkit.scheduleUpdate(this);
    }

    /** Something has changed in the background style. Redraw the background */
    private void backgroundChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        createColors();
        redraw_background();
        dirty_background.mark();
        toolkit.scheduleUpdate(this);
    }

    /** Something has changed in the foreground style. Reconfigure the needle */
    private void foregroundChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        createColors();
        reconfig_needle();
        dirty_foreground.mark();
        toolkit.scheduleUpdate(this);
    }

    /** The value that the needle is reading has changed. Calculate the new needle rotation. */
    private void contentChanged(final WidgetProperty<VType> property, final VType old_value, final VType new_value)
    {
        if (!connected && (new_value != null))
        {
            //This can happen when contentChanged and connectionChanged run in parallel
            //logger.log(Level.FINE, "Gauge got value change when PV not connected");
            connected = true;

        }

        if (new_value instanceof VNumber) {
            VNumber vnum = (VNumber)new_value;
            current_value = vnum.getValue().doubleValue();
        }
        calc_needle_rotation();
        dirty_foreground.mark();
        toolkit.scheduleUpdate(this);
    }

    /** Set points, color, and transform for needle to handle resize or recolor */
    private void reconfig_needle()
    {
        Double[] needle_pts = new Double[]{ -(gauge_diam / 50.0), 0.0,
                                            (gauge_diam / 50.0), 0.0,
                                             0.0, -(gauge_diam / 2.0) };

        needle.setFill(needle_color);
        needle.getPoints().clear();
        needle.getPoints().addAll(needle_pts);

        final int w = model_widget.positionWidth().getValue();
        final int h = model_widget.positionHeight().getValue();

        xlateNeedle.setX(w/2);
        xlateNeedle.setY(h/2);
    }

    /** Start angle as used to draw the gauge arc in awt.
     * used as baseline point for many other angle calculations */
    private int start_awt_arc_deg()
    {
        return 90 - (gauge_ang_deg / 2);
    }

    private void calc_needle_rotation()
    {
        needle_rot_ang = start_awt_arc_deg() - 90;

        if (connected)
        {
            needle_rot_ang += ((current_value - display_min) * (gauge_ang_deg / (display_max - display_min)));
        }
    }

    private void redraw_background()
    {
        /** Major and minor gauge mark intervals (based on display min/max) */
        double gauge_major_interval, gauge_minor_interval;

        buf = new BufferedImage((int)canvas.getWidth(), (int)canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        image = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());

        draw_round_gauge(buf);
    }

    private void draw_round_gauge(final BufferedImage buf)
    {
        Graphics2D gc = buf.createGraphics();

        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        final int width = buf.getWidth();
        final int height = buf.getHeight();
        final int start_awt_deg = start_awt_arc_deg();

        //gc.setColor(new java.awt.Color(0.0f, 0.0f, 1.0f, 0.0f));
        //gc.clearRect(0, 0, width, height);
        //gc.drawRect(0, 0, width, height);

        gc.setColor(bg_color);
        basicBGArc(gc, width, height, start_awt_deg, gauge_diam);

        if (connected)
        {
            makeGaugeMarks(gc, use_labels, start_awt_arc_deg(), (width / 2), (height / 2), gauge_diam / 2 - 1, 10);
            basicBGArc(gc, width, height, start_awt_deg, gauge_diam - 30);

            //Alarm ranges
            Double gauge_alarms[] = {lower_alarm, lower_warn, upper_warn, upper_alarm};
            java.awt.Color gauge_alarm_colors[] = { color_alarm, color_warn, color_ok, color_warn, color_alarm };


            final int subdiam = gauge_diam / 2;
            final int subarcx = (width / 2) - (subdiam / 2);
            final int subarcy = (height / 2) - (subdiam / 2);
            double gauge_val = display_min;
            int prev_angle = start_awt_deg;
            for (int cdx = 0; cdx < gauge_alarm_colors.length; cdx++)
            {
                gc.setColor(gauge_alarm_colors[cdx]);
                gauge_val = (cdx < gauge_alarms.length) ? gauge_alarms[cdx] : display_max;
                int next_angle = (int) (start_awt_deg + ((gauge_val - display_min) * (gauge_ang_deg / (display_max - display_min))));
                int final_angle = (next_angle - prev_angle) + ((next_angle < gauge_ang_deg) ? 1 : 0);
                gc.fillArc(subarcx, subarcy, subdiam, subdiam, prev_angle, final_angle);
                prev_angle = next_angle;
            }

            basicBGArc(gc, width, height, start_awt_deg, (int) (subdiam * 0.7));
        }

    }

    /** Draw an arc for the round gauge, using the background color, at the given diameter */
    private void basicBGArc(Graphics2D gc, final int width, final int height, final int start_awt_deg,
            final int major_diam)
    {
        final int major_arcx = (width / 2) - (major_diam / 2);
        final int major_arcy = (height / 2) - (major_diam / 2);
        gc.setColor(bg_color);
        gc.fillArc(major_arcx, major_arcy, major_diam, major_diam, start_awt_deg, gauge_ang_deg);
    }

    private void makeGaugeMarks(Graphics2D gc, Boolean doText, final int start_awt_arc_ang,
            int x_center, int y_center, int r_outer, double interval)
    {
        final BasicStroke mark_stroke =
                new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        gc.setStroke(mark_stroke);

        final double gauge_range = display_max - display_min;
        final int total_marks = (int) Math.floor((gauge_range) / interval);
        final double remove_frac = (gauge_range - (total_marks * interval)) / gauge_range;

        double effective_ang_rad = Math.toRadians(gauge_ang_deg - (remove_frac * gauge_ang_deg));

        gc.setColor(java.awt.Color.BLACK);
        java.awt.Font font = new java.awt.Font("Serif", java.awt.Font.BOLD, gauge_diam / 10);

        //double alpha_ang_rad = Math.toRadians(gauge_total_ang / total_marks);
        final double alpha_ang_rad = effective_ang_rad / total_marks;
        double start_ang_rad = Math.toRadians(start_awt_arc_ang + 180);
        for (int rdx = 0; rdx <= total_marks; rdx++)
        {
            final double ang = start_ang_rad + (alpha_ang_rad * rdx);
            double cos_ang = Math.cos(ang);
            double sin_ang = Math.sin(ang);
            int xo = (int) Math.round(x_center + (r_outer * cos_ang));
            int yo = (int) Math.round(y_center + (r_outer * sin_ang));
            gc.drawLine(xo, yo, x_center, y_center);
            if (doText)
            {
                String s = String.valueOf(display_min + rdx * interval);
                s = s.indexOf(".") < 0 ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
                gc.drawString(s, xo, yo);
                //printTextCircle(gc, s, font, x_center, y_center, r_outer + 2, ang, true);
            }
        }
    }

    private void update_outline(final int w, final int h)
    {
        outline.setCenterX(w/2);
        outline.setCenterY(h/2);
        outline.setRadiusX(gauge_diam/2);
        outline.setRadiusY(gauge_diam/2);

        outline.setStartAngle(start_awt_arc_deg());
        outline.setLength(gauge_ang_deg);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_background.checkAndClear())
        {
            final int w = model_widget.positionWidth().getValue();
            final int h = model_widget.positionHeight().getValue();

            jfx_node.setPrefSize(w, h);
            SwingFXUtils.toFXImage(buf, image);
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.getGraphicsContext2D().drawImage(image, 0, 0);

            update_outline(w, h);
        }
        if (dirty_foreground.checkAndClear())
        {
            rotationTransform.setAngle(needle_rot_ang);
        }
    }
}
