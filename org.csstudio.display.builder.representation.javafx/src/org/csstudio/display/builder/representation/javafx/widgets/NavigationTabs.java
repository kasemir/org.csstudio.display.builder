/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.csstudio.display.builder.model.properties.Direction;
import org.csstudio.javafx.JFXUtil;

import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Navigation Tabs
 *
 *  <p>Similar to a tab pane in look,
 *  but only has one 'body' for content.
 *
 *  <p>Selecting one of the tabs invokes listener,
 *  whi can then update the content.
 *  In comparison, a tab pane results in a scene graph
 *  where the content of all tabs is always present.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NavigationTabs extends BorderPane
{
    @FunctionalInterface
    public static interface Listener
    {
        void tabSelected(int tab_index);
    }

    private final static PseudoClass HORIZONTAL = PseudoClass.getPseudoClass("horizontal");

    /** HBox or VBox for tab buttons */
    private Pane buttons = new VBox();

    /** 'content', body */
    private final Pane body = new Pane();

    /** Labels for the tabs */
    private final List<String> tabs = new CopyOnWriteArrayList<>();

    private int tab_width = 100, tab_height = 50, tab_spacing = 2;

    /** Direction of tabs */
    private Direction direction = Direction.VERTICAL;

    private Color selected = Color.rgb(236, 236, 236),
                  deselected = Color.rgb(200, 200, 200);

    private volatile Listener listener;

    public NavigationTabs()
    {
        setCenter(body);
        body.getStyleClass().add("navtab_body");
    }

    /** @param listener Listener to notify when tab is selected */
    public void addListener(final Listener listener)
    {
        if (this.listener != null)
            throw new IllegalStateException("Only one listener supported");
        this.listener = listener;
    }

    /** @param listener Listener to remove */
    public void removeListener(final Listener listener)
    {
        if (this.listener != listener)
            throw new IllegalStateException("Unknown listener");
        this.listener = null;
    }

    /** @param tabs Tab labels */
    public void setTabs(final List<String> tabs)
    {
        this.tabs.clear();
        this.tabs.addAll(tabs);
        updateTabs();
    }

    /** Select a tab
     *
     *  <p>Does not invoke listener.
     *
     *  @param index Index of tab to select */
    public void selectTab(final int index)
    {
        int i = 0;
        for (Node button : buttons.getChildren())
        {
            if (i == index)
                button.setStyle("-fx-color: " + JFXUtil.webRGB(selected));
            else
                button.setStyle("-fx-color: " + JFXUtil.webRGB(deselected));
            ++i;
        }
    }

    /** @param content Content for the 'body' */
    public void setContent(final Node content)
    {
        body.getChildren().setAll(content);
    }

    /** @param direction Direction of tabs, horizontal (on top) or vertical (on left) */
    public void setDirection(final Direction direction)
    {
        if (this.direction == direction)
            return;
        this.direction = direction;
        updateTabs();
    }

    /** @param width Width and ..
     *  @param height height of tabs
     */
    public void setTabSize(final int width, final int height)
    {
        if (tab_width == width  &&  tab_height == height)
            return;
        tab_width = width;
        tab_height = height;
        updateTabs();
    }

    /** @param spacing Spacing between tabs */
    public void setTabSpacing(final int spacing)
    {
        if (tab_spacing == spacing)
            return;
        tab_spacing = spacing;
        updateTabs();
    }

    /** @param color Color for selected tab */
    public void setSelectedColor(final Color color)
    {
        if (selected.equals(color))
            return;
        selected = color;
        updateTabs();
    }

    /** @param color Color for de-selected tabs */
    public void setDeselectedColor(final Color color)
    {
        if (deselected.equals(color))
            return;
        deselected = color;
        updateTabs();
    }

    private void updateTabs()
    {
        if (direction == Direction.VERTICAL)
        {
            setTop(null);
            final VBox box = new VBox(tab_spacing);
            setLeft(box);
            buttons = box;
        }
        else
        {
            setLeft(null);
            final HBox box = new HBox(tab_spacing);
            setTop(box);
            buttons = box;
        }

        buttons.getStyleClass().add("navtab_tabregion");

        // Create button for each tab
        for (int i=0; i<tabs.size(); ++i)
        {
            final ToggleButton button = new ToggleButton(tabs.get(i));
            if (direction == Direction.HORIZONTAL)
                button.pseudoClassStateChanged(HORIZONTAL, true);

            // base color, '-fx-color', is either selected or deselected
            button.setStyle("-fx-color: " + JFXUtil.webRGB(deselected));
            button.getStyleClass().add("navtab_button");
            button.setMinSize(ButtonBase.USE_PREF_SIZE, ButtonBase.USE_PREF_SIZE);
            button.setPrefSize(tab_width, tab_height);
            buttons.getChildren().add(button);
            button.setOnAction(e -> handleTabSelection(button));
        }
    }

    /** Indicate the active tab, notify listeners
     *  @param button Button that was pressed
     */
    private void handleTabSelection(final ToggleButton button)
    {
        // Highlight active tab by setting it to the 'selected' color
        final ObservableList<Node> siblings = buttons.getChildren();
        int i = 0, selected_tab = -1;
        for (Node sibling : siblings)
        {
            if (sibling == button)
            {
                button.setStyle("-fx-color: " + JFXUtil.webRGB(selected));
                selected_tab = i;
            }
            else
                sibling.setStyle("-fx-color: " + JFXUtil.webRGB(deselected));
            ++i;
        }
        // Notify listener
        final Listener safe_copy = listener;
        if (safe_copy != null)
            safe_copy.tabSelected(selected_tab);
    }
}