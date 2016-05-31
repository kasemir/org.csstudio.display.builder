package org.csstudio.display.builder.representation.javafx.sandbox;

import org.csstudio.display.builder.representation.javafx.MarkerAxis;

import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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

        MarkerAxis<Slider> axis = new MarkerAxis<Slider>(slider)
        {
            {
                slider.orientationProperty().addListener( (property, oldval, newval) ->
                    makeVertical(newval==Orientation.VERTICAL)
                );
            }

            @Override
            protected void initializeBindings(Slider node)
            {
                length = new DoubleBinding()
                {
                    {
                        super.bind(node.widthProperty(), node.heightProperty(), node.orientationProperty());
                    }

                    @Override
                    protected double computeValue()
                    {
                        return node.getOrientation() == Orientation.HORIZONTAL ?
                                node.getWidth() :
                                node.getHeight();
                    }
                };
                min = new DoubleBinding()
                {
                    {
                        super.bind(node.minProperty());
                    }

                    @Override
                    protected double computeValue()
                    {
                        return node.getMin();
                    }
                };
                max = new DoubleBinding()
                {
                    {
                        super.bind(node.maxProperty());
                    }

                    @Override
                    protected double computeValue()
                    {
                        return node.getMax();
                    }
                };
            }
        };

        Button button = new Button("Rotate me.");
        button.setOnAction((event)->
            {
                slider.setOrientation(slider.getOrientation()==Orientation.HORIZONTAL ?
                        Orientation.VERTICAL : Orientation.HORIZONTAL);
            }
        );
        Button button2 = new Button("Toggle hihi");
        button2.setOnAction((event)->
        {
            axis.setShowHiHi(!axis.getShowHiHi());
        }
        );
        Button button3 = new Button("Adjust lo");
        button3.setOnAction((event)->
        {
            if ("Adjust length".equals(button3.getText()))
            {   //adjusting length
                button3.setText("Adjust lo");
                slider.setValue(slider.getOrientation()==Orientation.HORIZONTAL ?
                        slider.getPrefWidth()-400 : slider.getPrefHeight()-400);
            }
            else
            {   //adjusting lo
                button3.setText("Adjust length");
                slider.setValue(axis.getLo());
            }
        });

        HBox buttons = new HBox(10.0, new Text("[Slide to adjust.]"), button3, button, button2);

        VBox root = new VBox();

        reorient(slider.getOrientation(), root, buttons, axis, slider);
        slider.orientationProperty().addListener((property, oldval, newval)->
        {
            reorient(newval, root, buttons, axis, slider);
        });
        slider.setPrefWidth(400);
        slider.valueProperty().addListener((observable, oldval, newval)->
        {
            if ("Adjust lo".equals(button3.getText()))
            {
                if (slider.getOrientation()==Orientation.HORIZONTAL)
                    slider.setPrefWidth(400+newval.doubleValue());
                else
                    slider.setPrefHeight(400+newval.doubleValue());
            }
            else
                axis.setLo(newval.doubleValue());
        });

        final Scene scene = new Scene(root, 800, 700);
        stage.setScene(scene);
        stage.setTitle("Slider Demo");

        stage.show();
    }

    private void reorient(Orientation newValue, Pane root, Pane buttons, MarkerAxis axis, Slider slider)
    {
        Pane newpane = newValue==Orientation.HORIZONTAL ? new VBox(axis, slider) : new HBox(axis, slider);
        if (newValue==Orientation.HORIZONTAL)
            ((VBox)newpane).setFillWidth(false);
        else
            ((HBox)newpane).setFillHeight(false);
        final double oldwidth = slider.getPrefWidth();
        slider.setPrefWidth(slider.getPrefHeight());
        slider.setPrefHeight(oldwidth);
        root.getChildren().clear();
        root.getChildren().addAll(newpane, buttons);
    }
}