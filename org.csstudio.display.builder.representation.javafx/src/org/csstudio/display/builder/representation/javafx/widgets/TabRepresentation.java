/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.List;
import java.util.concurrent.Callable;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.ModelThreadPool;
import org.csstudio.display.builder.model.widgets.TabWidget;
import org.csstudio.display.builder.model.widgets.TabWidget.TabItemProperty;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import com.sun.javafx.tk.Toolkit;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
public class TabRepresentation extends JFXBaseRepresentation<TabPane, TabWidget>
{
    private final DirtyFlag dirty_layout = new DirtyFlag();

    private static final int inset = 10;

    private volatile Font tab_font;

    private final UntypedWidgetPropertyListener tab_title_listener = (property, old_value, new_value) ->
    {
        final List<TabItemProperty> desired = model_widget.displayTabs().getValue();
        final ObservableList<Tab> actual = jfx_node.getTabs();
        final int N = Math.min(desired.size(), actual.size());
        for (int i=0; i<N; ++i)
        {
            final Tab tab = actual.get(i);
            final Label label = (Label) tab.getGraphic();
            label.setText(desired.get(i).name().getValue());
        }
    };

    @Override
    public TabPane createJFXNode() throws Exception
    {
        final TabPane tabs = new TabPane();
//      tabs.setStyle("-fx-background-color: mediumaquamarine;");
        tabs.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);

        tabs.setMinSize(TabPane.USE_PREF_SIZE, TabPane.USE_PREF_SIZE);

//      model_widget.runtimeInsets().setValue(new int[] { inset, 2*inset });

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

        tabsChanged(null, null, model_widget.displayTabs().getValue());
        model_widget.displayTabs().addPropertyListener(this::tabsChanged);

        jfx_node.getSelectionModel().selectedIndexProperty().addListener((t, o, selected) ->
        {
            System.out.println("Active Tab: " + selected);
//            System.out.println("Active Tab: " + tabs.getSelectionModel().getSelectedIndex());
        });

        // Initial update of font, size
        layoutChanged(null, null, null);
    }

    private void tabsChanged(final WidgetProperty<List<TabItemProperty>> property,
                             final List<TabItemProperty> removed,
                             final List<TabItemProperty> added)
    {
        Toolkit.getToolkit().checkFxUserThread();
        // System.out.println("Tabs added: " + added + ", removed: " + removed);
        if (removed != null)
            for (TabItemProperty item : removed)
                item.name().removePropertyListener(tab_title_listener);
        if (added != null)
            for (TabItemProperty item : added)
                item.name().addUntypedPropertyListener(tab_title_listener);

        final List<TabItemProperty> desired = model_widget.displayTabs().getValue();
        final ObservableList<Tab> actual = jfx_node.getTabs();

        final int N = desired.size();
        for (int i=actual.size()-1; i >= N; --i)
            actual.remove(i);

        for (int i=actual.size(); i<N; ++i)
        {
            final String name = desired.get(i).name().getValue();
            final Pane content = new Pane();

            // 'Tab's are added with a Label as 'graphic'
            // because that label allows setting the font.

            // XXX Quirk: Tabs will not show the label unless there's also a non-empty text
            final Tab tab = new Tab(" ", content);
            final Label label = new Label(name);
            tab.setGraphic(label);
            tab.setClosable(false); // !!
            actual.add(tab);

            // XXX How to best set the background color?
            // No API other than style sheet?
            // tab.setStyle("-fx-background-color: green;");
            // content.setStyle("-fx-background-color: green;");
        }
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
            for (Tab tab : jfx_node.getTabs())
            {   // Set the font of the 'graphic' that's used to represent the tab
                final Label label = (Label) tab.getGraphic();
                label.setFont(tab_font);
            }

            final Integer width = model_widget.positionWidth().getValue();
            final Integer height = model_widget.positionHeight().getValue();
            jfx_node.setPrefSize(width, height);

            // XXX Force TabPane refresh
            // See org.csstudio.display.builder.representation.javafx.sandbox.TabDemo
            final Callable<Object> twiddle = () ->
            {
                Thread.sleep(500);
                Platform.runLater(() ->
                {
                    jfx_node.setSide(Side.BOTTOM);
                    jfx_node.setSide(Side.TOP);
                });
                return null;
            };
            ModelThreadPool.getExecutor().submit(twiddle);
        }
    }
}
