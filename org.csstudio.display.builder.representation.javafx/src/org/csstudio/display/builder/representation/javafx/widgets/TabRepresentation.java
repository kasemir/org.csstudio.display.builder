/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.util.ModelThreadPool;
import org.csstudio.display.builder.model.widgets.TabWidget;
import org.csstudio.display.builder.model.widgets.TabWidget.TabItemProperty;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import com.sun.javafx.tk.Toolkit;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TabRepresentation extends JFXBaseRepresentation<TabPane, TabWidget>
{
    private final DirtyFlag dirty_layout = new DirtyFlag();

    private final AtomicBoolean changing_active_tab = new AtomicBoolean();

    private static final int inset = 10;

    private volatile Font tab_font;

    private final WidgetPropertyListener<String> tab_title_listener = (property, old_value, new_value) ->
    {
        final List<TabItemProperty> model_tabs = model_widget.displayTabs().getValue();
        for (int i=0; i<model_tabs.size(); ++i)
            if (model_tabs.get(i).name() == property)
            {
                final Tab tab = jfx_node.getTabs().get(i);
                final Label label = (Label) tab.getGraphic();
                label.setText(property.getValue());
                break;
            }
    };

    private final WidgetPropertyListener<List<Widget>> tab_children_listener = (property, removed, added) ->
    {
        final List<TabItemProperty> model_tabs = model_widget.displayTabs().getValue();
        int index;
        for (index = model_tabs.size() - 1;  index >= 0; --index)
            if (model_tabs.get(index).children() == property)
                break;
        if (index < 0)
            throw new IllegalStateException("Cannot locate tab children " + property + " in " + model_widget);

        if (removed != null)
            for (Widget removed_widget : removed)
            {
                toolkit.execute(() -> toolkit.disposeWidget(removed_widget));
            }

        if (added != null)
            addChildren(index, added);
    };

    @Override
    public TabPane createJFXNode() throws Exception
    {
        final TabPane tabs = new TabPane();
//      tabs.setStyle("-fx-background-color: mediumaquamarine;");
        tabs.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);

        tabs.setMinSize(TabPane.USE_PREF_SIZE, TabPane.USE_PREF_SIZE);

        // TODO Allow configuration of tab height
        //tabs.setTabMinHeight(50);
        //tabs.setTabMaxHeight(50);

        return tabs;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();

        // Create initial tabs and their children
        addTabs(model_widget.displayTabs().getValue());

        final UntypedWidgetPropertyListener listener = this::layoutChanged;
//        model_widget.displayBackgroundColor().addUntypedPropertyListener(listener);
        model_widget.displayFont().addUntypedPropertyListener(listener);
        model_widget.positionWidth().addUntypedPropertyListener(listener);
        model_widget.positionHeight().addUntypedPropertyListener(listener);

        model_widget.displayTabs().addPropertyListener(this::tabsChanged);

        // Update UI when model selects a tab
        final WidgetPropertyListener<Integer> track_active_model_tab = (p, old, value) ->
        {
            if (! changing_active_tab.compareAndSet(false, true))
                return;
            if (value == null)
                value = p.getValue();
            if (value >= jfx_node.getTabs().size())
                value = jfx_node.getTabs().size() - 1;
            jfx_node.getSelectionModel().select(value);
            changing_active_tab.set(false);
        };
        // Select initial tab
        track_active_model_tab.propertyChanged(model_widget.displayActiveTab(), null, null);
        model_widget.displayActiveTab().addPropertyListener(track_active_model_tab);

        // Update model when UI selects a tab
        jfx_node.getSelectionModel().selectedIndexProperty().addListener((t, o, selected) ->
        {
            if (! changing_active_tab.compareAndSet(false, true))
                return;
            model_widget.displayActiveTab().setValue(selected.intValue());
            changing_active_tab.set(false);
        });

        // Initial update of font, size
        layoutChanged(null, null, null);
    }

    private void tabsChanged(final WidgetProperty<List<TabItemProperty>> property,
                             final List<TabItemProperty> removed,
                             final List<TabItemProperty> added)
    {
        Toolkit.getToolkit().checkFxUserThread();
        if (removed != null)
            removeTabs(removed);
        if (added != null)
            addTabs(added);
    }

    private void addTabs(final List<TabItemProperty> added)
    {
        for (TabItemProperty item : added)
        {
            final String name = item.name().getValue();
            final Pane content = new Pane();

            // 'Tab's are added with a Label as 'graphic'
            // because that label allows setting the font.

            // XXX Quirk: Tabs will not show the label unless there's also a non-empty text
            final Tab tab = new Tab(" ", content);
            final Label label = new Label(name);
            tab.setGraphic(label);
            tab.setClosable(false); // !!
            tab.setUserData(item);

            final int index = jfx_node.getTabs().size();
            jfx_node.getTabs().add(tab);

            addChildren(index, item.children().getValue());

            // XXX How to best set the background color?
            // No API other than style sheet?
            // tab.setStyle("-fx-background-color: green;");
            // content.setStyle("-fx-background-color: green;");

            item.name().addPropertyListener(tab_title_listener);
            item.children().addPropertyListener(tab_children_listener);
        }
    }

    private void removeTabs(final List<TabItemProperty> removed)
    {
        for (TabItemProperty item : removed)
        {
            item.children().removePropertyListener(tab_children_listener);
            item.name().removePropertyListener(tab_title_listener);
            for (Tab tab : jfx_node.getTabs())
                if (tab.getUserData() == item)
                {
                    jfx_node.getTabs().remove(tab);
                    break;
                }
        }
    }

    private void addChildren(final int index, final List<Widget> added)
    {
        final Pane parent_item = (Pane) jfx_node.getTabs().get(index).getContent();
        for (Widget added_widget : added)
        {
            final Optional<Widget> parent = added_widget.getParent();
            if (! parent.isPresent())
                throw new IllegalStateException("Cannot locate parent widget for " + added_widget);
            toolkit.execute(() -> toolkit.representWidget(parent_item, added_widget));
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

            // Compute insets
            // TODO Result is wrong until the tab is once resized?!
            final Pane pane = (Pane)jfx_node.getTabs().get(0).getContent();
            final Point2D tab_bounds = jfx_node.localToScene(0.0, 0.0);
            final Point2D pane_bounds = pane.localToScene(0.0, 0.0);
            System.out.println("Tab bounds : " + tab_bounds);
            System.out.println("pane bounds : " + pane_bounds);
            final int[] insets = new int[] { (int)(pane_bounds.getX() - tab_bounds.getX()),
                                             (int)(pane_bounds.getY() - tab_bounds.getY()) };
            System.out.println("Insets: " + Arrays.toString(insets));
            model_widget.runtimeInsets().setValue(insets);

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
