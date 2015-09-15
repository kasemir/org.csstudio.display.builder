/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.javafx.rtplot;

import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.javafx.rtplot.data.ArrayPlotDataProvider;
import org.csstudio.javafx.rtplot.data.SimpleDataItem;
import org.csstudio.javafx.rtplot.internal.MouseMode;
import org.csstudio.javafx.rtplot.internal.Plot;
import org.csstudio.javafx.rtplot.internal.TraceImpl;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/** @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class BasicPlotDemo extends Application
{
    @Override
    public void start(final Stage stage) throws Exception
    {
        Logger.getLogger("").setLevel(Level.FINE);
        for (Handler handler : Logger.getLogger("").getHandlers())
            handler.setLevel(Level.FINE);

        final Plot<Double> plot = new Plot<Double>(Double.class);
        plot.setTitle(Optional.of("Plot Demo"));
        plot.getXAxis().setName("The horizontal quantities on 'X'");
        plot.addYAxis("Another Axis");
        plot.getYAxes().get(1).setOnRight(true);

        final ArrayPlotDataProvider<Double> data1 = new ArrayPlotDataProvider<>();
        final ArrayPlotDataProvider<Double> data2 = new ArrayPlotDataProvider<>();
        for (double x = -10.0; x <= 10.0; x += 1.0)
        {
        	data1.add(new SimpleDataItem<Double>(x, x*x));
        	data2.add(new SimpleDataItem<Double>(x, 2*x));
        }
        final TraceImpl<Double> trace1 = new TraceImpl<Double>("Demo Data", "socks", data1, Color.BLUE, TraceType.AREA, 3, PointType.DIAMONDS, 15, 0);
		plot.addTrace(trace1);
		final TraceImpl<Double> trace2 = new TraceImpl<Double>("More Data", "pants", data2, Color.RED, TraceType.AREA, 3, PointType.SQUARES, 15, 1);
		plot.addTrace(trace2);
        plot.getXAxis().setValueRange(-12.0, 12.0);

        // a) Fixed range
//		plot.getYAxes().get(0).setValueRange(-10.0, 120.0);
//		plot.getYAxes().get(1).setValueRange(-25.0, 25.0);

        // b) Autoscale
//		plot.getYAxes().get(0).setAutoscale(true);
//		plot.getYAxes().get(1).setAutoscale(true);

        // c) Stagger
        plot.stagger();

        plot.showCrosshair(true);

        plot.setMouseMode(MouseMode.PAN);

		final Pane root = new Pane(plot);
		plot.widthProperty().bind(root.widthProperty());
		plot.heightProperty().bind(root.heightProperty());

        final Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Basic Plot Demo");
        stage.show();

        stage.setOnCloseRequest((event) -> plot.dispose());
    }

    public static void main(final String[] args)
    {
        launch(args);
    }
}