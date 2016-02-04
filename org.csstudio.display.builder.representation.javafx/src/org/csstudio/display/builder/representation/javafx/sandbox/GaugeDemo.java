/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/** Draw AWT image of a gauge in background, then display in javafx Canvas.
 *
 *
 *  @author Megan Grodowitz
 */
@SuppressWarnings("nls")
public class GaugeDemo extends Application
{
    //Canvas where we draw the gauge
    //final private Canvas canvas = DemoHelper.createCanvas();
    final private Canvas canvas = new Canvas (600, 400);

    //Counter to count number of times the gauge is redrawn
    private final AtomicLong counter = new AtomicLong();

    //Text Field to output needle value
    private final Text NeedleRead = new Text("0");

    //Some values to toggle: Probably part of gauge model
    private final TextField inp_angle = new TextField();
    private final TextField inp_min = new TextField();
    private final TextField inp_max = new TextField();

    //Gauge attributes:
    //diameter of the gauge
    final int gauge_diam = 300;
    //physical length of the major hash mark lines
    final int gauge_major_len = gauge_diam / 10;
    //physical length of the minor hash mark lines
    final int gauge_minor_len = gauge_diam / 20;
    //total angle of the gauge in degrees (0 < ang < 360)
    Double gauge_total_ang = 220.0;
    //min and max value reading of the gauge
    Double gauge_min = 0.0, gauge_max = 80.0;
    //How frequently to mark major and minor intervals on the gauge
    Double gauge_major_interval = 10.0, gauge_minor_interval = 1.0;
    //Color of the gauge
    double[] needleRGB = new double[]{100.0, 100.0, 255.0};
    //Alarm ranges
    Double gauge_alarms[] = {5.0, 10.0, 70.0, 75.0};
    java.awt.Color gauge_alarm_colors[] = { java.awt.Color.RED, java.awt.Color.YELLOW,
            java.awt.Color.GREEN, java.awt.Color.YELLOW, java.awt.Color.RED };

    //Needle shape
    private final Polygon needle = new Polygon();
    Double[] needle_pts = new Double[]{ -5.0, 0.0, 5.0, 0.0, 0.0, -(gauge_diam / 2.0) };
    //Current rotation of the needle (determined by needle_value)
    Double needle_rotation = 0.0;
    //Direction needle is moving (1 or -1)
    Double needle_direction = 1.0;
    //Current value the needle should read
    Double needle_value = 0.0;
    //Transformations to apply to the needle
    final Rotate rotationTransform = new Rotate(0, 0, 0);
    final Translate xlateTransform = new Translate(0, -(gauge_diam / 4));

    //Animation variables
    private volatile long draw_ms = -1;
    private volatile long update_ms = -1;

    public static void main(final String[] args)
    {
        launch(args);
    }


    @Override
    public void start(final Stage stage)
    {
        final Label label1 = new Label("Canvas:");
        final Label label2 = new Label("Needle Value:");

        Text text = new Text("Hello");
        text.setFill(Color.RED);

        needle.getPoints().addAll(needle_pts);
        needle.getTransforms().addAll(xlateTransform, rotationTransform);
        needle.setFill(Color.RED);
        //needle.set

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        addInputRow(grid, 0, "Total angle size of gauge", inp_angle, gauge_total_ang);
        addInputRow(grid, 1, "Min reading on the gauge", inp_min, gauge_min);
        addInputRow(grid, 2, "Max reading on the gauge", inp_max, gauge_max);


        StackPane pane = makeStackPane();
        //AnchorPane pane = makeAnchorPane();

        final VBox root_jfxnode = new VBox(label1, pane, grid,
                                           label2, NeedleRead);

        final Scene scene = new Scene(root_jfxnode, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Drawing AWT Gauge");

        stage.show();

        final Thread thread = new Thread(this::thread_main);
        thread.setName("Redraw");
        thread.setDaemon(true);
        thread.start();
    }


    private void addInputRow(GridPane grid, int row, String inp_angle_txt, TextField textField, Double default_val)
    {
        final Label inp_label = new Label(inp_angle_txt);
        textField.setPromptText(inp_angle_txt);
        textField.setText(Double.toString(default_val));
        GridPane.setConstraints(textField, 1, row);
        GridPane.setConstraints(inp_label, 0, row);
        grid.getChildren().addAll(inp_label, textField);
    }


    private AnchorPane makeAnchorPane()
    {
        AnchorPane pane = new AnchorPane();
        pane.setMaxWidth(canvas.getWidth());
        pane.setMaxHeight(canvas.getHeight());
        // List should stretch as anchorpane is resized
        AnchorPane.setTopAnchor(canvas, 0.0);
        AnchorPane.setLeftAnchor(canvas, 0.0);
        AnchorPane.setRightAnchor(canvas, 0.0);
        // Button will float on right edge
        AnchorPane.setTopAnchor(needle, canvas.getWidth() / 2);
        AnchorPane.setRightAnchor(needle, 110.0);
        pane.getChildren().addAll(canvas, needle);
        return pane;
    }


    private StackPane makeStackPane()
    {
        StackPane pane = new StackPane();
        pane.setMaxWidth(canvas.getWidth());
        pane.setMaxHeight(canvas.getHeight());
        pane.getChildren().addAll(canvas, needle);
        return pane;
    }




    private void draw_round_gauge(final BufferedImage buf)
    {
        Graphics2D gc = buf.createGraphics();

        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        final int width = buf.getWidth();
        final int height = buf.getHeight();

        gc.clearRect(0, 0, width, height);
        gc.drawRect(0, 0, width, height);

        double gauge_fade = 0.8;
        Double gauge_red = (needleRGB[0] * gauge_fade) / 255.0;
        Double gauge_green = (needleRGB[1] * gauge_fade) / 255.0;
        Double gauge_blue = (needleRGB[2] * gauge_fade) / 255.0;

        gc.setColor(new java.awt.Color( gauge_red.floatValue(),
                                        gauge_green.floatValue(),
                                        gauge_blue.floatValue()));

        final int size = gauge_diam;
        final int x = (width / 2) - (size / 2);
        final int y = (height / 2) - (size / 2);
        final int start_awt_arc_ang = (int) (90 - (gauge_total_ang / 2));
        //gc.fillOval(x, y, size, size);
        gc.fillArc(x, y, size, size, start_awt_arc_ang, gauge_total_ang.intValue());

        final int subsize = gauge_diam / 2;
        final int subx = (width / 2) - (subsize / 2);
        final int suby = (height / 2) - (subsize / 2);
        double gauge_val = gauge_min;
        int prev_angle = start_awt_arc_ang;
        for (int cdx = 0; cdx < gauge_alarm_colors.length; cdx++)
        {
            gc.setColor(gauge_alarm_colors[cdx]);
            gauge_val = (cdx < gauge_alarms.length) ? gauge_alarms[cdx] : gauge_max;
            int next_angle = (int) (start_awt_arc_ang + ((gauge_val - gauge_min) * (gauge_total_ang / (gauge_max - gauge_min))));
            gc.fillArc(subx, suby, subsize, subsize, prev_angle, next_angle - prev_angle);
            prev_angle = next_angle;
        }

        int subsubsize = subsize - 50;
        final int subsubx = (width / 2) - (subsubsize / 2);
        final int subsuby = (height / 2) - (subsubsize / 2);
        gc.setColor(new java.awt.Color( gauge_red.floatValue(),
                gauge_green.floatValue(),
                gauge_blue.floatValue()));
        gc.fillArc(subsubx, subsuby, subsubsize, subsubsize, start_awt_arc_ang, gauge_total_ang.intValue());

        /*
        for (int i = 0; i < (gauge_total_ang.intValue() - 1); i++)
        {
            gc.setColor(java.awt.Color.getHSBColor((float)(i/360.0), 1, 1));
            gc.fillArc(subx, suby, subsize, subsize, i + start_awt_arc_ang, 2);
        }
         */

        //addSomeText(gc, size, x, y);

        gc.setColor(java.awt.Color.PINK);
        java.awt.Font font = new java.awt.Font("Serif", java.awt.Font.PLAIN, 18);
        gc.setFont(font);

        int xc = x + (size / 2);
        int yc = y + (size / 2);
        int r_outer = size / 2;

        int r_inner = r_outer - gauge_major_len;
        //int total_marks = (int) Math.floor((gauge_max - gauge_min) / gauge_major_interval);
        makeGaugeMarks(gc, true, start_awt_arc_ang, xc, yc, r_outer, r_inner, gauge_major_interval);

        r_inner = r_outer - gauge_minor_len;
        //total_marks = (int) Math.floor((gauge_max - gauge_min) / gauge_minor_interval);
        makeGaugeMarks(gc, false, start_awt_arc_ang, xc, yc, r_outer, r_inner, gauge_minor_interval);

        calcNeedleRotation(start_awt_arc_ang);

        /*
        String s = "Here goes a very long test string long long long long long long long test test.";
        gc.setColor(java.awt.Color.CYAN);
        font = new java.awt.Font("Serif", java.awt.Font.PLAIN, 24);
        double start_angle = start_awt_arc_ang + 180;
        double radius = size / 2.0;
        printTextCircle(gc, s, font, xc, yc, (int) radius, Math.toRadians(start_angle));
         */


    }


    private void printTextCircle(Graphics2D gc, String s, java.awt.Font font,
            int x_center, int y_center, int radius,
            double start_angle_rad, Boolean center_txt)
    {
        gc.setFont(font);
        FontRenderContext frc = gc.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, s);
        int length = gv.getNumGlyphs();

        if (center_txt) {
            final double total_ang_rad = gv.getGlyphPosition(length).getX() / radius;
            start_angle_rad -= total_ang_rad / 2.0;
        }
        //double half_width = gv.getGlyphPosition(length-1).getX() / 2.0;
        for (int i = 0; i < length; i++) {
          java.awt.geom.Point2D p = gv.getGlyphPosition(i);
          final double pwidth = p.getX();
          //final double theta_deg = (pwidth * 360.0) / (2.0 * Math.PI * radius);
          final double theta0 = pwidth / radius;
          //final double theta = Math.toRadians(theta_deg + start_angle_deg);
          final double theta = theta0 + start_angle_rad;
          final double xcoord = x_center + (radius * Math.cos(theta));
          final double ycoord = y_center + (radius * Math.sin(theta));
          //gc.drawString(String.valueOf(s.charAt(i)), (int)xcoord, (int)ycoord);
          AffineTransform trans0 = AffineTransform.getTranslateInstance(-p.getX(), -p.getY());
          AffineTransform trans = AffineTransform.getTranslateInstance(xcoord, ycoord);
          AffineTransform rot = AffineTransform.getRotateInstance(theta + (Math.PI / 2.0));
          AffineTransform at = new AffineTransform();
          at.concatenate(trans); // Third translate the letter into place on the circle
          at.concatenate(rot); // Second rotate the letter
          at.concatenate(trans0); // First translate to 0,0
          java.awt.Shape glyph = gv.getGlyphOutline(i);
          java.awt.Shape transformedGlyph = at.createTransformedShape(glyph);
          gc.fill(transformedGlyph);
        }
    }


    private void makeGaugeMarks(Graphics2D gc, Boolean doText, final int start_awt_arc_ang,
            int x_center, int y_center, int r_outer,
            int r_inner, double interval)
    {
        final double gauge_range = gauge_max - gauge_min;
        final int total_marks = (int) Math.floor((gauge_range) / interval);
        final double remove_frac = (gauge_range - (total_marks * interval)) / gauge_range;

        double effective_ang_rad = Math.toRadians(gauge_total_ang - (remove_frac * gauge_total_ang));

        gc.setColor(java.awt.Color.CYAN);
        java.awt.Font font = new java.awt.Font("Serif", java.awt.Font.PLAIN, 18);

        //double alpha_ang_rad = Math.toRadians(gauge_total_ang / total_marks);
        final double alpha_ang_rad = effective_ang_rad / total_marks;
        double start_ang_rad = Math.toRadians(start_awt_arc_ang + 180);
        for (int rdx = 0; rdx <= total_marks; rdx++)
        {
            final double ang = start_ang_rad + (alpha_ang_rad * rdx);
            double cos_ang = Math.cos(ang);
            double sin_ang = Math.sin(ang);
            int xo = (int) (x_center + (r_outer * cos_ang));
            int yo = (int) (y_center + (r_outer * sin_ang));
            int xi = (int) (x_center + (r_inner * cos_ang));
            int yi = (int) (y_center + (r_inner * sin_ang));
            gc.drawLine(xo, yo, xi, yi);
            if (doText)
            {
                final String txt = String.valueOf(gauge_min + rdx * gauge_major_interval);
                //gc.drawString(String.valueOf(gauge_min + rdx * gauge_major_interval), xo, yo);
                printTextCircle(gc, txt, font, x_center, y_center, r_outer + 2, ang, true);
            }
        }
    }

    private void calcNeedleRotation(final int start_awt_arc_ang)
    {
        needle_rotation = start_awt_arc_ang - 90
                + ((needle_value - gauge_min) * (gauge_total_ang / (gauge_max - gauge_min)));
    }

    /*** These values should all come from the gauge model in the regular implementation
     *
     */
    private void getModelValues()
    {
        needle_value = needle_value + needle_direction;

        if (needle_value < gauge_min)
        {
            needle_value = gauge_min;
            needle_direction = 1.0;
        }
        if (needle_value > gauge_max)
        {
            needle_value = gauge_max;
            needle_direction = -1.0;
        }

        try {
            gauge_total_ang = Double.valueOf(inp_angle.getText());
        }
        catch(Exception ex) {
            gauge_total_ang = 180.0;
        }

        try {
            gauge_min = Double.valueOf(inp_min.getText());
        }
        catch(Exception ex) {
            gauge_min = 0.0;
        }

        try {
            gauge_max = Double.valueOf(inp_max.getText());
        }
        catch(Exception ex) {
            gauge_max = 100.0;
        }
    }


    private void addSomeText(Graphics2D gc, final int size, final int x, final int y)
    {
        gc.setColor(java.awt.Color.GRAY);
        java.awt.Font font = new java.awt.Font("Serif", java.awt.Font.PLAIN, 18);
        gc.setFont(font);
        gc.drawString("Text at upper left of gauge", x, y);
        gc.drawLine(x, y, x+size, y);
        gc.drawLine(x+size, y, x+size, y+size);

        gc.setColor(java.awt.Color.PINK);
        gc.drawLine(x+size, y+size, x, y+size);
        gc.drawLine(x, y+size, x, y);
        gc.drawString("Text at lower left of gauge", x+size, y+size);

        String center_str = "Text Centered Under Gauge, Under Text at LL";
        int textw = gc.getFontMetrics().stringWidth(center_str);
        int texth = gc.getFontMetrics().getHeight();
        int textx = x + (size / 2) - (textw / 2);
        int texty = y + size + texth;
        gc.drawString(center_str, textx, texty);
    }

    private void update_all(BufferedImage buf, WritableImage image, Semaphore done)
    {
        SwingFXUtils.toFXImage(buf, image);
        canvas.getGraphicsContext2D().drawImage(image, 0, 0);
        NeedleRead.setText(Double.toString(needle_value));

        //needle.setRotate(needle_rotation);
        rotationTransform.setAngle(needle_rotation);

        done.release();
    }

    private void thread_main()
    {
        final Semaphore done = new Semaphore(0);
        int to_refresh = 1;
        try
        {
            final BufferedImage buf = new BufferedImage((int)canvas.getWidth(), (int)canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
            final WritableImage image = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
            while (true)
            {
                getModelValues();

                // Prepare AWT image
                long start = System.currentTimeMillis();
                draw_round_gauge(buf);
                long ms = System.currentTimeMillis() - start;
                if (draw_ms < 0)
                    draw_ms = ms;
                else
                    draw_ms = (draw_ms * 9 + ms) / 10;

                counter.incrementAndGet();

                // Draw into Canvas on UI thread
                start = System.currentTimeMillis();
                Platform.runLater(() -> update_all(buf, image, done));

                // Wait for UI thread
                done.acquire();
                ms = System.currentTimeMillis() - start;
                if (update_ms < 0)
                    update_ms = ms;
                else
                    update_ms = (update_ms * 9 + ms) / 10;

                to_refresh = 1 - to_refresh;
                Thread.sleep(50);

                if ((counter.get() % 50) == 0)
                {
                    System.out.println("Drawing: " + draw_ms + " ms");
                    System.out.println("Update : " + update_ms + " ms");
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
