package org.csstudio.display.builder.representation.javafx.sandbox;

import org.csstudio.display.builder.representation.javafx.MarkerAxis;

import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Amanda Carpenter
 */
public class SliderDemo1 extends Application
{
    public static void main(String [] args)
    {
        launch(args);
    }

    @SuppressWarnings("nls")
    @Override
    public void start(final Stage stage)
    {
        Slider slider = new Slider();
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setMinorTickCount(5);

        Button button = new Button("Try me.");
        button.setOnAction((event)->
            {
                slider.setMaxWidth(400);
            }
        );

        MarkerAxis axis = new MarkerAxis(slider);
        axis.setSide(slider.getOrientation()==Orientation.HORIZONTAL ? Side.TOP : Side.LEFT);


        VBox root = new VBox(axis, slider, button);

        final Scene scene = new Scene(root, 800, 700);
        stage.setScene(scene);
        stage.setTitle("Slider Demo");

        stage.show();
    }


    DoubleBinding newDoubleBinding(ReadOnlyDoubleProperty readOnlyDoubleProperty)
    {
        return new DoubleBinding()
        {
            {
                super.bind(readOnlyDoubleProperty);
            }

            @Override
            protected double computeValue() {
                return readOnlyDoubleProperty.getValue();
            }
        };
    }

}
