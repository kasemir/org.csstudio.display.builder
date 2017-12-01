/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.controlsfx.control.PopOver;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.properties.NamedWidgetColor;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.util.ModelThreadPool;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 29 Nov 2017
 */
public class WidgetColorPopOver {

    @FXML
    private ListView<NamedWidgetColor> colorNames;

    @FXML
    private ColorPicker picker;

    @FXML
    private Slider redSlider;
    @FXML
    private Slider greenSlider;
    @FXML
    private Slider blueSlider;
    @FXML
    private Slider alphaSlider;

    @FXML
    private Spinner<Integer> redSpinner;
    @FXML
    private Spinner<Integer> greenSpinner;
    @FXML
    private Spinner<Integer> blueSpinner;
    @FXML
    private Spinner<Integer> alphaSpinner;

    @FXML
    private Circle currentColorCircle;
    @FXML
    private Circle restoreColorCircle;

    @FXML
    private Button restoreButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button okButton;

    private PopOver popOver;
    private final AtomicBoolean namesLoaded = new AtomicBoolean(false);
    private final Map<Color, NamedWidgetColor> namedColors = Collections.synchronizedMap(new HashMap<>());
    private Color restoreColor = null;
    private boolean updating = false;

    /*
     * ---- color --------------------------------------------------------------
     */
    private final ObjectProperty<Color> color = new SimpleObjectProperty<Color>(this, "color", Color.GOLDENROD) {
        @Override
        protected void invalidated() {

            Color col = get();

            if ( col == null ) {
                set(Color.GOLDENROD);
            } else {
                currentColorCircle.setFill(col);
            }

        }
    };

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public Color getColor() {
        return color.get();
    }

    public void setColor( Color color ) {
        this.color.set(color);
    }

    /*
     * -------------------------------------------------------------------------
     */
    public void initialize() {

        picker.valueProperty().bindBidirectional(colorProperty());

        redSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255));
        redSpinner.valueProperty().addListener(this::updateFromSpinner);
        greenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255));
        greenSpinner.valueProperty().addListener(this::updateFromSpinner);
        blueSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255));
        blueSpinner.valueProperty().addListener(this::updateFromSpinner);
        alphaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255));
        alphaSpinner.valueProperty().addListener(this::updateFromSpinner);

        colorProperty().addListener(( observable, oldValue, newValue ) -> {

            updating = true;

            if ( namedColors.containsKey(newValue) ) {
                colorNames.getSelectionModel().select(namedColors.get(newValue));
            }

            redSlider.setValue(getRed());
            redSpinner.getValueFactory().setValue(getRed());
            greenSlider.setValue(getGreen());
            greenSpinner.getValueFactory().setValue(getGreen());
            blueSlider.setValue(getBlue());
            blueSpinner.getValueFactory().setValue(getBlue());
            alphaSlider.setValue(getAlpha());
            alphaSpinner.getValueFactory().setValue(getAlpha());

            updating = false;

        });

        colorNames.setCellFactory(view -> new NamedWidgetColorCell());
        colorNames.getSelectionModel().selectedItemProperty().addListener(( observable, oldValue, newValue ) -> {
            if ( newValue != null ) {
                setColor(JFXUtil.convert(newValue));
            }
        });

        // Get colors on background thread
        ModelThreadPool.getExecutor().execute( ( ) -> {

            final NamedWidgetColors colors = WidgetColorService.getColors();
            final Collection<NamedWidgetColor> values = colors.getColors();

            values.parallelStream().forEach(nc -> namedColors.put(JFXUtil.convert(nc), nc));

            Platform.runLater(() -> {
                values.stream().forEach(nc -> {
                    colorNames.getItems().addAll(nc);
                    picker.getCustomColors().add(JFXUtil.convert(nc));
                });
                namesLoaded.set(true);
            });

        });

    }

    public void setInitialConditions ( PopOver popOver, WidgetColor widgetColor ) {

        this.popOver = popOver;
        this.restoreColor = JFXUtil.convert(widgetColor);

        restoreColorCircle.setFill(restoreColor);
        setColor(restoreColor);

        ModelThreadPool.getExecutor().execute( ( ) -> {

            while ( !namesLoaded.get() ) {
                Thread.yield();
            }

            Platform.runLater(() -> {
                if ( widgetColor instanceof NamedWidgetColor ) {
                    colorNames.getSelectionModel().select((NamedWidgetColor) widgetColor);
                    colorNames.scrollTo(colorNames.getSelectionModel().getSelectedIndex());
                }
            });

        });

    }

    @FXML
    void cancelPressed ( ActionEvent event ) {
        if ( popOver != null ) {
            popOver.hide();
        }
    }

    @FXML
    void okPressed ( ActionEvent event ) {
        if ( popOver != null ) {
            popOver.hide();
        }
    }

    @FXML
    void restorePressed ( ActionEvent event ) {
        setColor(restoreColor);
    }

    private int getAlpha() {

        Color c = getColor();

        return c.isOpaque() ? 255 : (int) ( 255 * c.getOpacity() );

    }

    private int getBlue() {
        return (int) ( 255 * getColor().getBlue() );
    }

    private int getGreen() {
        return (int) ( 255 * getColor().getGreen() );
    }

    private int getRed() {
        return (int) ( 255 * getColor().getRed() );
    }

    private Color getSliderColor ( ) {
        return Color.rgb(
            (int) redSlider.getValue(),
            (int) greenSlider.getValue(),
            (int) blueSlider.getValue(),
            (int) alphaSlider.getValue() / 255.0
        );
    }

    private Color getSpinnerColor ( ) {
        return Color.rgb(
            Math.max(0, Math.min(255, redSpinner.getValue())),
            Math.max(0, Math.min(255, greenSpinner.getValue())),
            Math.max(0, Math.min(255, blueSpinner.getValue())),
            Math.max(0, Math.min(255, alphaSpinner.getValue() / 255.0))
        );
    }

    private void updateFromSpinner ( ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue ) {
        if ( !updating ) {
            setColor(getSpinnerColor());
        }
    }

    /**
     * List cell for a NamedWidgetColor: Color 'blob' and color's name.
     */
    private static class NamedWidgetColorCell extends ListCell<NamedWidgetColor> {

        private final static int SIZE = 16;
        private final Canvas     blob = new Canvas(SIZE, SIZE);

        NamedWidgetColorCell ( ) {
            setGraphic(blob);
        }

        @Override
        protected void updateItem ( final NamedWidgetColor color, final boolean empty ) {

            super.updateItem(color, empty);

            if ( color == null ) {
                // Content won't change from non-null to null, so no need to clear.
                return;
            }

            setText(color.getName());

            final GraphicsContext gc = blob.getGraphicsContext2D();

            gc.setFill(JFXUtil.convert(color));
            gc.fillRect(0, 0, SIZE, SIZE);

        }
    };

}
