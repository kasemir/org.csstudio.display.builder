package org.csstudio.display.builder.representation.javafx.sandbox;

import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Helper for drawing random canvas content
 *  @author Kay Kasemir
 */
public class DemoHelper
{
	public static Canvas createCanvas()
	{
		return new Canvas(800, 600);
	}

	public static void refresh(final Canvas canvas)
	{
		final GraphicsContext gc = canvas.getGraphicsContext2D();
		
		final Bounds bounds = canvas.getBoundsInLocal();
		final double width = bounds.getWidth();
		final double height = bounds.getHeight();
		
		gc.clearRect(0, 0, width, height);
		gc.strokeRect(0, 0, width, height);
		
		for (int i=0; i<10000; ++i)
		{
			gc.setFill(Color.hsb(Math.random()*360.0,
					             Math.random(),
					             Math.random()));
			final double size = 5 + Math.random() * 40;
			final double x = Math.random() * (width-size);
			final double y = Math.random() * (height-size);
			gc.fillOval(x, y, size, size);
		}
	}
}
