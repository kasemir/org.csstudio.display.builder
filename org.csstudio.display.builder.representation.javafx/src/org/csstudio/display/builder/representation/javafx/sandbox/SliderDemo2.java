/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import org.csstudio.display.builder.representation.javafx.MarkerAxis;

import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * @author Amanda Carpenter
 */
public class SliderDemo2 extends Application
{
    public static void main(String [] args)
    {
        launch(args);
    }

    final Border blackborder = new Border(new BorderStroke(Color.BLACK,
            new BorderStrokeStyle(StrokeType.OUTSIDE,
                                  StrokeLineJoin.MITER,
                                  StrokeLineCap.BUTT,
                                  10, 0, null),
            CornerRadii.EMPTY,
            new BorderWidths(1)));
    final Border blueborder = new Border(new BorderStroke(Color.BLUE,
            new BorderStrokeStyle(StrokeType.OUTSIDE,
                                  StrokeLineJoin.MITER,
                                  StrokeLineCap.BUTT,
                                  10, 0, null),
            CornerRadii.EMPTY,
            new BorderWidths(1)));
    final Border greenborder = new Border(new BorderStroke(Color.GREEN,
            new BorderStrokeStyle(StrokeType.OUTSIDE,
                                  StrokeLineJoin.MITER,
                                  StrokeLineCap.BUTT,
                                  10, 0, null),
            CornerRadii.EMPTY,
            new BorderWidths(1)));

    @SuppressWarnings("nls")
    @Override
    public void start(final Stage stage)
    {
        Slider slider = new Slider();
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setMinorTickCount(5);
        //slider.setBorder(blueborder);

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
                        return (node.getOrientation() == Orientation.HORIZONTAL ?
                                node.getWidth() : node.getHeight()) -
                                15;
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
        GridPane.setConstraints(axis, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
        //axis.setBorder(greenborder);

        final GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        GridPane.setConstraints(slider, 0, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
        pane.getChildren().add(slider);
        pane.setPrefWidth(100);
        pane.setMaxWidth(100);
        //pane.setBorder(blackborder);

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
                        slider.getPrefWidth()/5-100 : slider.getPrefHeight()/5-100);
            }
            else
            {   //adjusting lo
                button3.setText("Adjust length");
                slider.setValue(axis.getLo());
            }
        });
        Button button4 = new Button("Relocate.");
        button4.setOnAction((event)->
        {
            pane.relocate(127, 138);
        });


        HBox buttons = new HBox(10.0, new Text("[Slide to adjust.]"), button3, button, button2, button4);

        final VBox root = new VBox(pane, buttons);
        root.setPadding(new Insets(5));

        reorient(slider.getOrientation(), pane, axis);
        slider.orientationProperty().addListener((property, oldval, newval)->
        {
            reorient(newval, pane, axis);
        });
        slider.valueProperty().addListener((observable, oldval, newval)->
        {
            if ("Adjust lo".equals(button3.getText()))
            {
                if (slider.getOrientation()==Orientation.HORIZONTAL)
                {
                    pane.setPrefWidth(100+5*newval.doubleValue());
                    pane.setMaxWidth(100+5*newval.doubleValue());
                }
                else
                {
                    pane.setMaxHeight(100+5*newval.doubleValue());
                    pane.setMinHeight(100+5*newval.doubleValue());
                }
            }
            else
                axis.setLo(newval.doubleValue());
        });

        final Scene scene = new Scene(root, 800, 700);
        stage.setScene(scene);
        stage.setTitle("Slider Demo");

        stage.show();
    }

    private void reorient(Orientation newValue, GridPane pane, MarkerAxis<Slider> axis)
    {
        boolean showMarkers = true;
        Node slider = pane.getChildren().get(0);
        if (showMarkers)
        {
            if (newValue == Orientation.HORIZONTAL)
            {
                GridPane.setConstraints(slider, 0, 1);
                GridPane.setHgrow(slider, Priority.ALWAYS);
                GridPane.setVgrow(slider, Priority.NEVER);
                pane.setPrefHeight(Region.USE_COMPUTED_SIZE);
            }
            else
            {
                GridPane.setConstraints(slider, 1, 0);
                GridPane.setHgrow(slider, Priority.NEVER);
                GridPane.setVgrow(slider, Priority.ALWAYS);
                pane.setPrefWidth(Region.USE_COMPUTED_SIZE);
            }
            if (!pane.getChildren().contains(axis))
                pane.add(axis, 0, 0);
        }
        else
        {
            pane.getChildren().removeIf((child)->child instanceof MarkerAxis);
            GridPane.setConstraints(slider, 0, 0);
            if (newValue == Orientation.HORIZONTAL)
            {
                GridPane.setHgrow(slider, Priority.ALWAYS);
                GridPane.setVgrow(slider, Priority.NEVER);
            }
            else
            {
                GridPane.setHgrow(slider, Priority.NEVER);
                GridPane.setVgrow(slider, Priority.ALWAYS);
            }
        }
    }
}