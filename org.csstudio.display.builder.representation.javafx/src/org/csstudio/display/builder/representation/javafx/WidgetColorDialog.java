/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.csstudio.display.builder.model.properties.WidgetColor;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

/** Dialog for selecting a {@link WidgetColor}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetColorDialog extends Dialog<WidgetColor>
{
    private WidgetColor color;

    // TODO: Change into ListView<WidgetColor> once there are named colors
    private final ListView<String> color_names = new ListView<>();

    private final ColorPicker picker = new ColorPicker();

    private final Slider red_slider = new Slider(0, 255, 50);
    private final Slider green_slider = new Slider(0, 255, 50);
    private final Slider blue_slider = new Slider(0, 255, 50);

    private final TextField red_text = new TextField();
    private final TextField green_text = new TextField();
    private final TextField blue_text = new TextField();

    /** Prevent circular updates */
    private boolean updating = false;

    /** Create dialog
     *  @param initial_color Initial {@link WidgetColor}
     */
    public WidgetColorDialog(final WidgetColor initial_color)
    {
        setTitle(Messages.ColorDialog_Title);
        setHeaderText(Messages.ColorDialog_Info);

        /* Predefined Colors   Custom Color
         * [               ]   Picker
         * [               ]   Red   ---*----  [100]
         * [     List      ]   Green ------*-  [250]
         * [               ]   Blue  *-------  [  0]
         * [               ]   /..    dummy      ../
         */

        final GridPane content = new GridPane();
        // content.setGridLinesVisible(true); // For debugging
        content.setHgap(10);
        content.setVgap(10);
        content.setPadding(new Insets(10));

        content.add(new Label(Messages.ColorDialog_Predefined), 0, 0);

        // TODO: Show named colors
        color_names.getItems().addAll("Title #255,0,0", "Background #250,255,250", "OK #0,255,0");
        content.add(color_names, 0, 1, 1, 5);

        content.add(new Label(Messages.ColorDialog_Custom), 1, 0, 3, 1);
        content.add(picker, 1, 1, 3, 1);

        content.add(new Label(Messages.Red), 1, 2);
        content.add(new Label(Messages.Green), 1, 3);
        final Label label = new Label(Messages.Blue);
        content.add(label, 1, 4);

        content.add(red_slider, 2, 2);
        content.add(green_slider, 2, 3);
        content.add(blue_slider, 2, 4);
        red_slider.setBlockIncrement(1);

        red_text.setPrefColumnCount(3);
        green_text.setPrefColumnCount(3);
        blue_text.setPrefColumnCount(3);
        content.add(red_text, 3, 2);
        content.add(green_text, 3, 3);
        content.add(blue_text, 3, 4);

        // Placeholder that fills the lower right corner
        final Label dummy = new Label();
        content.add(dummy, 1, 5, 3, 1);

        getDialogPane().setContent(content);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // User selects named color -> Update picker, sliders, texts
        color_names.getSelectionModel().selectedItemProperty().addListener((l, old, value) ->
        {
            // TODO Handle names from color file
            final Pattern pattern = Pattern.compile(".*#(\\d+),(\\d+),(\\d+)");
            final Matcher matcher = pattern.matcher(value);
            if (matcher.matches())
            {
                setColor(new WidgetColor(Integer.parseInt(matcher.group(1)),
                                         Integer.parseInt(matcher.group(2)),
                                         Integer.parseInt(matcher.group(3))));
            }
        });

        // User changes slider or text -> Update picker
        bind(red_slider,   red_text);
        bind(green_slider, green_text);
        bind(blue_slider,  blue_text);

        // User configures color in picker -> Update sliders, texts
        picker.setOnAction(event ->
        {
            if (updating)
                return;
            updating = true;

            final Color value = picker.getValue();
            final int r = (int) (value.getRed() * 255);
            final int g = (int) (value.getGreen() * 255);
            final int b = (int) (value.getBlue() * 255);
            red_slider.setValue(r);
            green_slider.setValue(g);
            blue_slider.setValue(b);
            red_text.setText(Integer.toString(r));
            green_text.setText(Integer.toString(g));
            blue_text.setText(Integer.toString(b));

            color = new WidgetColor(r, g, b);
            updating = false;
        });

        // From http://code.makery.ch/blog/javafx-dialogs-official/,
        // attempts to focus on a field.
        // Will only work if the dialog is opened "soon".
        Platform.runLater(() -> color_names.requestFocus());

        setResultConverter(button ->
        {
            if (button == ButtonType.OK)
                return color;
            return null;
        });

        setColor(initial_color);
    }

    /** Bidirectionally bind slider and text, also update picker and color
     *  @param slider {@link Slider}
     *  @param text {@link TextField}
     */
    private void bind(final Slider slider, final TextField text)
    {
        slider.valueProperty().addListener((s, old, value) ->
        {
            if (updating)
                return;
            updating = true;
            text.setText(Integer.toString(value.intValue()));
            final Color jfx_col = getSliderColor();
            picker.setValue(jfx_col);
            color = JFXUtil.convert(jfx_col);
            updating = false;
        });

        text.textProperty().addListener((t, old, value) ->
        {
            if (updating)
                return;
            updating = true;
            try
            {
                int num = Integer.parseInt(value);
                if (num > 255)
                {
                    num = 255;
                    text.setText("255");
                }
                if (num < 0)
                {
                    num = 0;
                    text.setText("0");
                }
                slider.setValue(num);
                final Color jfx_col = getSliderColor();
                picker.setValue(jfx_col);
                color = JFXUtil.convert(jfx_col);
            }
            catch (Throwable ex)
            {
                text.setText(Integer.toString((int)slider.getValue()));
            }
            finally
            {
                updating = false;
            }
        });
    }

    /** @return Color currently configured in sliders */
    private Color getSliderColor()
    {
        return Color.rgb((int) red_slider.getValue(),
                         (int) green_slider.getValue(),
                         (int) blue_slider.getValue());
    }

    /** Set all display elements to color
     *  @param color WidgetColor
     */
    private void setColor(final WidgetColor color)
    {
        picker.setValue(JFXUtil.convert(color));
        red_slider.setValue(color.getRed());
        green_slider.setValue(color.getGreen());
        blue_slider.setValue(color.getBlue());
        red_text.setText(Integer.toString(color.getRed()));
        green_text.setText(Integer.toString(color.getGreen()));
        blue_text.setText(Integer.toString(color.getBlue()));
        this.color = color;
    }
}
