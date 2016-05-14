/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import org.csstudio.display.builder.model.properties.ColorMap;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

    /** @param scripts Scripts to show/edit in the dialog */
    public ColorMapDialog(final ColorMap map)
    {
        this.map = map;
        setTitle("Color Map");
        setHeaderText("Select predefined color map. Optionally, customize it.");

        getDialogPane().setContent(createContent());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        hookListeners();

        setResultConverter(button ->
        {
            if (button != ButtonType.OK)
                return null;
            return this.map;
        });
    }

    private static class ColorTableCell extends TableCell<ColorSection, Node>
    {
        @Override
        protected void updateItem(Node item, boolean empty)
        {
            super.updateItem(item, empty);
            if (empty)
                setGraphic(null);
            else
                setGraphic(item);
        }
    }

    private Node createContent()
    {
        predefined_table = new TableView<>();
        final TableColumn<ColorMap.Predefined, String> name_column = new TableColumn<>("Predefined Color Map");
        name_column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDescription()));
        predefined_table.getColumns().add(name_column);
        predefined_table.setItems(FXCollections.observableArrayList(ColorMap.PREDEFINED));
        predefined_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        sections_table = new TableView<>();
        final TableColumn<ColorSection, String> value_column = new TableColumn<>("Value (0-255)");
        value_column.setCellValueFactory(param -> new SimpleStringProperty(Integer.toString(param.getValue().value)));

        final TableColumn<ColorSection, Node> color_column = new TableColumn<>("Color");
        color_column.setCellValueFactory(param ->
        {
            // TODO Change Label into color picker
            final Color color = param.getValue().color;
            final Label label = new Label(color.toString());
            label.setBackground(new Background(new BackgroundFill(color, null, null)));
            return new SimpleObjectProperty<Node>(label);
        });
        color_column.setCellFactory(column -> new ColorTableCell());
        sections_table.getColumns().add(value_column);
        sections_table.getColumns().add(color_column);
        sections_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        sections_table.setPlaceholder(new Label("Add a color"));
        sections_table.setItems(color_sections);

        final HBox box = new HBox(10, predefined_table, sections_table);
        HBox.setHgrow(predefined_table, Priority.ALWAYS);
        HBox.setHgrow(sections_table, Priority.ALWAYS);

        // box.setStyle("-fx-background-color: rgb(255, 100, 0, 0.2);"); // For debugging
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

        color_sections.clear();
        final int[][] sections = map.getSections();
        for (int[] section : sections)
            color_sections.add(new ColorSection(section[0], Color.rgb(section[1], section[2], section[3])));

    }
}
