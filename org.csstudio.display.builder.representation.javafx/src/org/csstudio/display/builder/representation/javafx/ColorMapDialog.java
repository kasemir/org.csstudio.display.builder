/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import org.csstudio.display.builder.model.properties.ColorMap;
import org.csstudio.display.builder.model.properties.WidgetColor;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Dialog for editing {@link ColorMap}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ColorMapDialog extends Dialog<ColorMap>
{
    private static class ColorSection
    {
        int value;
        Color color;

        ColorSection(final int value, final Color color)
        {
            this.value = value;
            this.color = color;
        }
    }

    private TableView<ColorMap.Predefined> predefined_table;

    private TableView<ColorSection> sections_table;

    private final ObservableList<ColorSection> color_sections = FXCollections.observableArrayList();


    private ColorMap map;

    private Region color_bar;

    /** @param scripts Scripts to show/edit in the dialog */
    public ColorMapDialog(final ColorMap map)
    {
        setTitle("Color Map");
        setHeaderText("Select predefined color map. Optionally, customize it.");

        getDialogPane().setContent(createContent());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        updateUIfromMap(map);

        hookListeners();

        setResultConverter(button ->
        {
            if (button != ButtonType.OK)
                return null;
            return this.map;
        });
    }

    private static class ColorTableCell extends TableCell<ColorSection, ColorPicker>
    {
        @Override
        protected void updateItem(final ColorPicker picker, final boolean empty)
        {
            super.updateItem(picker, empty);
            setGraphic(empty ? null : picker);
        }
    }

    private Node createContent()
    {
        // Table for selecting a predefined color map
        predefined_table = new TableView<>();
        final TableColumn<ColorMap.Predefined, String> name_column = new TableColumn<>("Predefined Color Map");
        name_column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDescription()));
        predefined_table.getColumns().add(name_column);
        predefined_table.setItems(FXCollections.observableArrayList(ColorMap.PREDEFINED));
        predefined_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Table for viewing/editing color sections
        sections_table = new TableView<>();
        // Value of color section
        final TableColumn<ColorSection, String> value_column = new TableColumn<>("Value (0-255)");
        value_column.setCellValueFactory(param -> new SimpleStringProperty(Integer.toString(param.getValue().value)));
        // TODO Edit value column

        // Color of color section
        final TableColumn<ColorSection, ColorPicker> color_column = new TableColumn<>("Color");
        color_column.setCellValueFactory(param ->
        {
            final Color color = param.getValue().color;
            final ColorPicker picker = new ColorPicker(color);
            return new SimpleObjectProperty<ColorPicker>(picker);
        });
        color_column.setCellFactory(column -> new ColorTableCell());
        // TODO React to picker changing the color

        sections_table.getColumns().add(value_column);
        sections_table.getColumns().add(color_column);
        sections_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        sections_table.setPlaceholder(new Label("Add a color"));
        sections_table.setItems(color_sections);

        // Buttons to add/remove color sections
        final Button add = new Button("Add Color", JFXUtil.getIcon("add.png"));
        add.setMaxWidth(Double.MAX_VALUE);
        final Button remove = new Button("Remove Color", JFXUtil.getIcon("delete.png"));
        remove.setMaxWidth(Double.MAX_VALUE);
        final VBox buttons = new VBox(10, add, remove);

        // Upper section with tables
        final HBox tables_and_buttons = new HBox(10, predefined_table, sections_table, buttons);
        HBox.setHgrow(predefined_table, Priority.ALWAYS);
        HBox.setHgrow(sections_table, Priority.ALWAYS);

        // Lower section with resulting color map
        final Region fill1 = new Region(), fill2 = new Region(), fill3 = new Region();
        HBox.setHgrow(fill1, Priority.ALWAYS);
        HBox.setHgrow(fill2, Priority.ALWAYS);
        HBox.setHgrow(fill3, Priority.ALWAYS);
        final HBox color_title = new HBox(fill1, new Label("Result"), fill2);

        color_bar = new Region();
        color_bar.setMinHeight(50.0);

        final HBox color_legend = new HBox(new Label("0"), fill3, new Label("255"));

        final VBox box = new VBox(10, tables_and_buttons, new Separator(), color_title, color_bar, color_legend);
        VBox.setVgrow(tables_and_buttons, Priority.ALWAYS);

        return box;
    }

    private void hookListeners()
    {
        predefined_table.getSelectionModel().selectedItemProperty().addListener(
            (p, old, selected_map) ->
            {
                if (selected_map != null)
                    updateUIfromMap(selected_map);
            });
    }

    private void updateUIfromMap(final ColorMap map)
    {
        this.map = map;

        if (map instanceof ColorMap.Predefined)
            predefined_table.getSelectionModel().select((ColorMap.Predefined) map);
        else
            predefined_table.getSelectionModel().clearSelection();

        color_sections.clear();
        final int[][] sections = map.getSections();
        for (int[] section : sections)
            color_sections.add(new ColorSection(section[0], Color.rgb(section[1], section[2], section[3])));

        final WritableImage colors = new WritableImage(256, 1);
        final PixelWriter writer = colors.getPixelWriter();
        for (int x=0; x<256; ++x)
        {
            final WidgetColor color = map.getColor(x);
            final int arfb = (255 << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
            writer.setArgb(x, 0, arfb);
        }

        color_bar.setBackground(new Background(
                new BackgroundImage(colors, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.REPEAT,
                                    BackgroundPosition.DEFAULT,
                                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, false))));
    }
}
