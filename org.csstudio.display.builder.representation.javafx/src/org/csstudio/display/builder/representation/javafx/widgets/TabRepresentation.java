/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.TabWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class TabRepresentation extends JFXBaseRepresentation<TabPane, TabWidget>
{
    private final DirtyFlag dirty_layout = new DirtyFlag();

    private static final int inset = 10;

    /** Inner group that holds child widgets */
    private Group inner;

    private volatile Font tab_font;

    @Override
    public TabPane createJFXNode() throws Exception
    {
        final TabPane tabs = new TabPane();

        // TODO Remove dummy content
        // Debug: Show tab area
        tabs.setStyle("-fx-background-color: mediumaquamarine;");

        final int N = 3;
        final Pane[] content = new Pane[N];
        for (int i=0; i<N; ++i)
        {
            content[i] = new Pane();
            // Use Label as 'graphic' to allow setting its font
            final Tab tab = new Tab("", content[i]);
            final Label label = new Label("Tab " + (i+1));
            tab.setGraphic(label);
            tab.setClosable(false); // !!
            tabs.getTabs().add(tab);
        }

        for (int i=0; i<N; ++i)
        {
            final Rectangle rect = new Rectangle(i*100, 100, 10+i*100, 20+i*80);
            rect.setFill(Color.BLUE);
            content[i].getChildren().add(rect);
        }

        tabs.getSelectionModel().selectedIndexProperty().addListener((t, o, selected) ->
        {
            System.out.println("Active Tab: " + selected);
//            System.out.println("Active Tab: " + tabs.getSelectionModel().getSelectedIndex());
        });

//      model_widget.runtimeInsets().setValue(new int[] { inset, 2*inset });

        // Initial update of font, size
        layoutChanged(null, null, null);

        return tabs;
    }

//    @Override
//    protected Group getChildParent(final Group parent)
//    {
//        return inner;
//    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        final UntypedWidgetPropertyListener listener = this::layoutChanged;
//        model_widget.displayBackgroundColor().addUntypedPropertyListener(listener);
        model_widget.displayFont().addUntypedPropertyListener(listener);
        model_widget.positionWidth().addUntypedPropertyListener(listener);
        model_widget.positionHeight().addUntypedPropertyListener(listener);
    }

    private void layoutChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        tab_font = JFXUtil.convert(model_widget.displayFont().getValue());
        dirty_layout.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_layout.checkAndClear())
        {
//            final Color background = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
//            label.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
            for (Tab tab : jfx_node.getTabs())
            {   // Set the font of the 'graphic' that's used to represent the tab
                final Label label = (Label) tab.getGraphic();
                label.setFont(tab_font);
            }

            jfx_node.setPrefWidth(model_widget.positionWidth().getValue());
            jfx_node.setPrefHeight(model_widget.positionHeight().getValue());
        }
    }
}
