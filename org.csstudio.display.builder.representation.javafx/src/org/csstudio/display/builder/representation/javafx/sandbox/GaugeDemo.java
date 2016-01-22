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
    final private Canvas canvas = new Canvas (600, 400);
    private final AtomicLong counter = new AtomicLong();
    private final Text updates = new Text("0");
    private final Polygon needle = new Polygon();

    Double[] needle_pts = new Double[]{ -5.0, 0.0, 5.0, 0.0, 0.0, -50.0 };
    Double needle_rotation = 45.0;
    final Rotate rotationTransform = new Rotate(0, 0, 0);
    final Translate xlateTransform = new Translate(0, -25);

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
        final Label label2 = new Label("Updates:");

        Text text = new Text("Hello");
        text.setFill(Color.RED);

        needle.getPoints().addAll(needle_pts);
        needle.getTransforms().addAll(xlateTransform, rotationTransform);
        needle.setFill(Color.RED);
        //needle.set

        StackPane pane = makeStackPane();
        //AnchorPane pane = makeAnchorPane();

        final VBox root_jfxnode = new VBox(label1, pane, label2, updates);

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

        needle_rotation = (needle_rotation > 359) ? 0 : needle_rotation + 1.0;
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

        final int size = 100;
        final int x = (width / 2) - (size / 2);
        final int y = (height / 2) - (size / 2);
        //gc.fillOval(x, y, size, size);
        gc.fillArc(x, y, size, size, 0, 180);
    }

    private void update_all(BufferedImage buf, WritableImage image, Semaphore done)
    {
        SwingFXUtils.toFXImage(buf, image);
        canvas.getGraphicsContext2D().drawImage(image, 0, 0);
        updates.setText(Long.toString(counter.get()));

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
