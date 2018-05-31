/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.model.properties.FontWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.javafx.PopOver;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;


/**
 * PopOver editor for selecting a {@link WidgetFont}s.
 *
 * @author Kay Kasemir, SNS
 * @author claudiorosati, European Spallation Source ERIC
 */
@SuppressWarnings("nls")
public class WidgetFontPopOver extends PopOver {

    public WidgetFontPopOver ( final FontWidgetProperty font_prop, final Consumer<WidgetFont> fontChangeConsumer ) {

        try {

            URL fxml = WidgetFontPopOver.class.getResource("WidgetFontPopOver.fxml");
            InputStream iStream = WidgetFontPopOver.class.getResourceAsStream("messages.properties");
            ResourceBundle bundle = new PropertyResourceBundle(iStream);
            FXMLLoader fxmlLoader = new FXMLLoader(fxml, bundle);
            Node content = (Node) fxmlLoader.load();

            setContent(content);

            WidgetFontPopOverController controller = fxmlLoader.<WidgetFontPopOverController> getController();

            controller.setInitialConditions(
                this,
                font_prop.getValue(),
                font_prop.getDefaultValue(),
                font_prop.getDescription(),
                fontChangeConsumer
            );

        } catch ( IOException ex ) {
            logger.log(Level.WARNING, "Unable to edit font.", ex);
            setContent(new Label("Unable to edit font."));
        }

    }






//    // Sizes are of type double, but comparison of double to set selected element
//    // can suffer rounding errors, so use strings
//    private final static Collection<String> default_sizes =
//        Arrays.asList("8.0", "10.0", "12.0", "14.0", "16.0", "18.0", "22.0", "24.0", "32.0");
//
//    private WidgetFont font;
//
//    private final ListView<NamedWidgetFont> font_names = new ListView<>();
//    private final ListView<String> families = new ListView<>();
//    private final ListView<WidgetFontStyle> styles = new ListView<>();
//    private final TextField size = new TextField();
//    private final ListView<String> sizes = new ListView<>();
//    private final TextField example = new TextField();
//
//    /** Prevent circular updates */
//    private boolean updating = false;
//
//    /** Create dialog
//     *  @param initial_font Initial {@link WidgetFont}
//     *  @param handle_selected_color Will be called when user selected a color
//     */
//    public WidgetFontPopOver(final WidgetFont initial_font, final Consumer<WidgetFont> handle_selected_color)
//    {
//        super(new BorderPane());
//
//        final BorderPane layout = getContentNode();
//        layout.setTop(new Label(Messages.FontDialog_Info));
//
//        /* Predefined Fonts    Custom Family   Style  Size
//         * [               ]   [            ]  [    ]  ___
//         * [               ]   [            ]  [    ] [   ]
//         */
//
//        final GridPane content = new GridPane();
//        // content.setGridLinesVisible(true); // For debugging
//        content.setHgap(10);
//        content.setVgap(10);
//        content.setPadding(new Insets(10));
//
//        // Get font families and named fonts on background thread
//        ModelThreadPool.getExecutor().execute(() ->
//        {
//            final List<String> fams = Font.getFamilies();
//            final NamedWidgetFonts fonts = WidgetFontService.getFonts();
//            final Collection<NamedWidgetFont> values = fonts.getFonts();
//            Platform.runLater(() ->
//            {
//                families.getItems().setAll(fams);
//                font_names.getItems().setAll(values);
//                setFont(initial_font);
//            });
//        });
//
//        styles.getItems().addAll(WidgetFontStyle.values());
//        sizes.getItems().addAll(default_sizes);
//
//        example.setText(Messages.FontDialog_ExampleText);
//        example.setPrefHeight(50);
//
//        content.add(new Label(Messages.FontDialog_Predefined), 0, 0);
//        content.add(font_names, 0, 1, 1, 2);
//
//        content.add(new Label(Messages.FontDialog_Family), 1, 0, 3, 1);
//
//        content.add(families, 1, 1, 1, 2);
//
//        content.add(new Label(Messages.FontDialog_Style), 2, 0);
//        content.add(styles, 2, 1, 1, 2);
//
//        content.add(new Label(Messages.FontDialog_Size), 3, 0);
//        size.setPrefColumnCount(3);
//        content.add(size, 3, 1);
//        content.add(sizes, 3, 2);
//
//        content.add(new Label(Messages.FontDialog_Preview), 0, 3, 4, 1);
//        content.add(example, 0, 4, 4, 1);
//
//        GridPane.setVgrow(font_names, Priority.ALWAYS);
//        GridPane.setVgrow(families, Priority.ALWAYS);
//        GridPane.setVgrow(styles, Priority.ALWAYS);
//        GridPane.setVgrow(sizes, Priority.ALWAYS);
//
//        layout.setCenter(content);
//
//        final Button ok = createButton(ButtonType.OK);
//        final Button cancel = createButton(ButtonType.CANCEL);
//        final ButtonBar buttons = new ButtonBar();
//        buttons.getButtons().setAll(ok, cancel);
//        layout.setBottom(buttons);
//
//        // User selects named color -> Update picker, sliders, texts
//        font_names.getSelectionModel().selectedItemProperty().addListener((l, old, value) ->  setFont(value));
//
//
//        // Double-click exiting named font confirms/closes
//        font_names.setOnMouseClicked((event) ->
//        {
//            if (event.getButton() == MouseButton.PRIMARY  &&
//                event.getClickCount() >= 2)
//            {
//                handle_selected_color.accept(font);
//                hide();
//            }
//        });
//
//        // Update font as family, style, size is adjusted
//        final InvalidationListener update_font = (observable) ->
//        {
//            if (updating)
//                return;
//            updating = true;
//            try
//            {
//                final String family = families.getSelectionModel().getSelectedItem();
//                if (family == null)
//                    return;
//                final WidgetFontStyle font_style = styles.getSelectionModel().getSelectedItem();
//                double font_size;
//                try
//                {
//                    font_size = Double.parseDouble(size.getText());
//                }
//                catch (NumberFormatException ex)
//                {
//                    font_size = 1;
//                }
//                if (font_size < 1)
//                    font_size = 1;
//                setFont(new WidgetFont(family, font_style, font_size));
//            }
//            finally
//            {
//                updating = false;
//            }
//        };
//        families.getSelectionModel().selectedItemProperty().addListener(update_font);
//        styles.getSelectionModel().selectedItemProperty().addListener(update_font);
//        size.setOnAction(event -> update_font.invalidated(null));
//        sizes.getSelectionModel().selectedItemProperty().addListener((l, old, value) ->
//        {
//            if (value != null)
//            {
//                size.setText(value);
//                update_font.invalidated(null);
//            }
//        });
//
//        showingProperty().addListener(prop ->
//        {
//            if (isShowing())
//                font_names.requestFocus();
//        });
//
//        setFont(initial_font);
//
//        ok.setOnAction(event ->
//        {
//            handle_selected_color.accept(font);
//            hide();
//        });
//        cancel.setOnAction(event -> hide());
//
//        // OK button is the 'default' button
//        layout.addEventFilter(KeyEvent.KEY_PRESSED, event ->
//        {
//            if (event.getCode() == KeyCode.ENTER)
//                ok.getOnAction().handle(null);
//        });
//    }
//
//    private static Button createButton(final ButtonType type)
//    {
//        final Button button = new Button(type.getText());
//        ButtonBar.setButtonData(button, type.getButtonData());
//        button.setDefaultButton(type.getButtonData().isDefaultButton());
//        button.setCancelButton(type.getButtonData().isCancelButton());
//        return button;
//    }
//
//    /** Set all display elements to font
//     *  @param color WidgetFont
//     */
//    private void setFont(final WidgetFont font)
//    {
//        updating = true;
//        try
//        {
//            if (font instanceof NamedWidgetFont)
//            {
//                font_names.getSelectionModel().select((NamedWidgetFont) font);
//                font_names.scrollTo(font_names.getSelectionModel().getSelectedIndex());
//            }
//
//            families.getSelectionModel().select(font.getFamily());
//            families.scrollTo(families.getSelectionModel().getSelectedIndex());
//
//            styles.getSelectionModel().select(font.getStyle());
//            size.setText(Double.toString(font.getSize()));
//
//            final String current_size = String.format("%.1f", font.getSize());
//            sizes.getSelectionModel().select(current_size);
//
//            example.setFont(JFXUtil.convert(font));
//        }
//        finally
//        {
//            updating = false;
//        }
//        this.font = font;
//    }
}
