/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
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
    //final private Canvas canvas = DemoHelper.createCanvas();
    final private Canvas canvas = new Canvas (800, 600);
    private final AtomicLong counter = new AtomicLong();
    private final Text NeedleRead = new Text("0");
    private final Polygon needle = new Polygon();

    final int gauge_diam = 300;
    final int gauge_major_len = gauge_diam / 10;
    final int guage_minor_len = gauge_diam / 20;

    Double gauge_total_ang = 220.0;
    Double gauge_min = 0.0, gauge_max = 80.0;
    Double gauge_major_interval = 10.0, gauge_minor_interval = 1.0;

    Double[] needle_pts = new Double[]{ -5.0, 0.0, 5.0, 0.0, 0.0, -(gauge_diam / 2.0) };
    Double needle_rotation = 0.0;
    Double needle_direction = 1.0;
    Double needle_value = 0.0;
    final Rotate rotationTransform = new Rotate(0, 0, 0);
    final Translate xlateTransform = new Translate(0, -(gauge_diam / 4));

    Double gauge_fade = 0.0;

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

        StackPane pane = makeStackPane();
        //AnchorPane pane = makeAnchorPane();

        final VBox root_jfxnode = new VBox(label1, pane, label2, NeedleRead);

        final Scene scene = new Scene(root_jfxnode, 800, 700);
        stage.setScene(scene);
        stage.setTitle("Drawing AWT Gauge");

        stage.show();

        final Thread thread = new Thread(this::thread_main);
        thread.setName("Redraw");
        thread.setDaemon(true);
        thread.start();
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
        final int width = buf.getWidth();
        final int height = buf.getHeight();

        gc.clearRect(0, 0, width, height);
        gc.drawRect(0, 0, width, height);

        gauge_fade = (gauge_fade > 0.5) ? gauge_fade - 0.01 : 1.0;
        Double gauge_red = (100.0 * gauge_fade) / 255.0;
        Double gauge_green = (100.0 * gauge_fade) / 255.0;
        Double gauge_blue = (255.0 * gauge_fade) / 255.0;

        //gc.setColor(java.awt.Color.getHSBColor( (float)Math.random(),
          //                                      (float)Math.random(),
            //                                    (float)Math.random()));

        gc.setColor(new java.awt.Color( gauge_red.floatValue(),
                                        gauge_green.floatValue(),
                                        gauge_blue.floatValue()));

        //final int size = (int)(5 + Math.random() * 40);
        //final int x = (int)(Math.random() * (width-size));
        //final int y = (int)(Math.random() * (height-size));

        final int size = gauge_diam;
        final int x = (width / 2) - (size / 2);
        final int y = (height / 2) - (size / 2);
        //gc.fillOval(x, y, size, size);
        final int start_arc_ang = (int) (90 - (gauge_total_ang / 2));
        gc.fillArc(x, y, size, size, start_arc_ang, gauge_total_ang.intValue());

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


        int xc = x + (size / 2);
        int yc = y + (size / 2);
        int r_outer = size / 2;
        int r_inner = r_outer - gauge_major_len;
        int total_major_marks = (int) Math.floor((gauge_max - gauge_min) / gauge_major_interval);
        double alpha_ang = Math.toRadians(gauge_total_ang / total_major_marks);
        double start_ang = Math.toRadians(start_arc_ang + 180);
        for (int rdx = 0; rdx <= total_major_marks; rdx++)
        {
            double cos_ang = Math.cos(start_ang + alpha_ang * rdx);
            double sin_ang = Math.sin(start_ang + alpha_ang * rdx);
            int xo = (int) (xc + (r_outer * cos_ang));
            int yo = (int) (yc + (r_outer * sin_ang));
            int xi = (int) (xc + (r_inner * cos_ang));
            int yi = (int) (yc + (r_inner * sin_ang));
            gc.drawLine(xo, yo, xi, yi);
            gc.drawString(String.valueOf(rdx), xo, yo);
        }


        needle_value = needle_value + needle_direction;

        if (needle_value < 0)
        {
            needle_value = 0.0;
            needle_direction = 1.0;
        }
        if (needle_value > gauge_max)
        {
            needle_value = gauge_max;
            needle_direction = -1.0;
        }

        needle_rotation = start_arc_ang - 90 + ((needle_value) * (gauge_total_ang / (gauge_max - gauge_min)));
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
