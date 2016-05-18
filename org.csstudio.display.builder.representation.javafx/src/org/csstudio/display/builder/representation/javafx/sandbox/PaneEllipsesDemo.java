package org.csstudio.display.builder.representation.javafx.sandbox;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;

public class PaneEllipsesDemo extends Application
{

    public static void main(final String[] args)
    {
        launch(args);
    }

	final int number = 8;
	final int size = 50;
    @Override
    public void start(final Stage stage)
    {
        Color [] value_colors = getColors(170); //10101010

        Pane pane = new Pane();
        Ellipse ellipses [] = new Ellipse [number];
        for (int i = 0; i < number; i++) {
            ellipses[i] = new Ellipse();
            pane.getChildren().add(ellipses[i]);
        }
//        Pane pane = new Pane(ellipses);
        //pane.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        //pane.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        pane.setPrefSize(size*number, size);
        for (int i = 0; i < number; i++) {
        	ellipses[i].setCenterX(size/2 + i*size);
        	ellipses[i].setCenterY(size/2);
        	ellipses[i].setRadiusX(size/2);
        	ellipses[i].setRadiusY(size/2);
        }

    	for (int i = 0; i < number; i++)
            ellipses[i].setFill(
                // Put highlight in top-left corner, about 0.2 wide,
                // relative to actual size of LED
                new RadialGradient(0, 0, 0.3, 0.3, 0.4, true, CycleMethod.NO_CYCLE,
                                   new Stop(0, value_colors[i].interpolate(Color.WHITESMOKE, 0.8)),
                                   new Stop(1, value_colors[i])));
        
        //VBox.setVgrow(pane, Priority.NEVER);
        VBox vbox = new VBox(pane);

        final Scene scene = new Scene(vbox, 800, 700);
        stage.setScene(scene);
        stage.setTitle("Pane with Ellipses");

        stage.show();
    }
    
	private Color[] getColors(int bits) {
		Color [] result = new Color [size];
		Color bright = Color.web("0x00FF00",1.0);
		Color dark = Color.web("0x007700",1.0);
		for (int i = 0; i < size; i++) {
			result[i] = (bits & 1) == 1 ? bright : dark;
			bits = bits >> 1;
		}
		return result;
	}

}
