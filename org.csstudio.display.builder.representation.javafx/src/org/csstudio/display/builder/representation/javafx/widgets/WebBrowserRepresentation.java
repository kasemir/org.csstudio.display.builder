/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.WebBrowserWidget;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class WebBrowserRepresentation extends RegionBaseRepresentation<Region, WebBrowserWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();

    private volatile double width;
    private volatile double height;

    class Browser extends Region
    {
        //================
        //--fields
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        //================
        //--constructors
        public Browser(String url)
        {
            getStyleClass().add("browser");
            getChildren().add(browser);
            goToURL(url);
        }

        //================
        //--private methods
        private Node createSpacer()
        {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return spacer;
        }

        //================
        //--protected methods
        protected void goToURL(String url)
        {
            if (!url.startsWith("http://"))
                if (url.equals(""))
                    url = "about:blank";
                else
                    url = "http://" + url;
            webEngine.load(url);
        }

        @Override
        protected void layoutChildren()
        {
            double w = getWidth();
            double h = getHeight();
            layoutInArea(browser, 0,0, w,h, 0, HPos.CENTER, VPos.CENTER);
        }

        @Override
        protected double computePrefWidth(double height)
        {
            return width;
        }

        @Override
        protected double computePrefHeight(double width)
        {
            return height;
        }
    }

    class BrowserWithToolbar extends Browser
    {
        //================
        //--fields
        //--toolbar controls
        //TODO: remove button text when icons work
        HBox toolbar;
        final Button backButton = new Button("<");
        final Button foreButton = new Button(">");
        final Button stop = new Button("X");
        final Button refresh  = new Button("R");
        final ComboBox<String> addressBar = new ComboBox<String>();
        final Button go = new Button("->");
        Control [] controls = new Control []
                {backButton, foreButton, stop, refresh, addressBar, go};
        String [] iconFiles = new String []
                {"arrow_left.png", "arrow_right.png", "green_chevron.png", "Player_stop.png", "refresh.png"};

        //--toolbar handlers and listeners
        void handleBackButton(ActionEvent event)
        {
            try { webEngine.getHistory().go(-1); }
            catch (IndexOutOfBoundsException e) {}
        }
        void handleForeButton(ActionEvent event)
        {
            try { webEngine.getHistory().go(1); }
            catch (IndexOutOfBoundsException e) {}
        }
        void handleStop(ActionEvent event)
        {
            webEngine.getLoadWorker().cancel();
        }
        void handleRefresh(ActionEvent event)
        {
            goToURL(webEngine.getLocation());
        }
        void handleGo(ActionEvent event)
        {
            goToURL(addressBar.getValue());
        }
        void locationChanged(ObservableValue<? extends String> observable, String oldval, String newval)
        {
            addressBar.getEditor().setText(newval);
            int index = addressBar.getItems().indexOf(newval);
            foreButton.setDisable(index <= 0);
            backButton.setDisable(index == addressBar.getItems().size()-1);
        }
        void handleShowing(Event event)
        {
            WebHistory history = webEngine.getHistory();
            int size = history.getEntries().size();
            if (history.getCurrentIndex() == size-1)
            {
                addressBar.getItems().clear();
                for (int i = 0; i < size; i++)
                {
                    addressBar.getItems().add(0, history.getEntries().get(i).getUrl());
                }
            }
        }

        //================
        //--constructor
        public BrowserWithToolbar(String url)
        {
            super(url);
            locationChanged(null, null, webEngine.getLocation());
            //assemble toolbar controls
            backButton.setOnAction(this::handleBackButton);
            foreButton.setOnAction(this::handleForeButton);
            stop.setOnAction(this::handleStop);
            refresh.setOnAction(this::handleRefresh);
            addressBar.setOnAction(this::handleGo);
            go.setOnAction(this::handleGo);

            addressBar.setEditable(true);
            addressBar.setOnShowing(this::handleShowing);
            webEngine.locationProperty().addListener(this::locationChanged);

            final String imageDirectory =
                    "platform:/plugin/org.csstudio.display.builder.model/icons/browser/";
            for (int i = 0; i < controls.length; i++)
            {
                Control control = controls[i];
                if (control instanceof ButtonBase)
                {
                    //TODO: image files aren't gotten; wrong directory?
                    //Image image = new Image(getClass().getResourceAsStream(imageDirectory+iconFiles[i]));
                    //((ButtonBase)control).setGraphic(new ImageView(image));
                    HBox.setHgrow(control, Priority.NEVER);
                }
                else //grow address bar
                    HBox.setHgrow(control, Priority.ALWAYS);
            }

            //add toolbar component
            toolbar = new HBox(controls);
            toolbar.getStyleClass().add("browser-toolbar");
            getChildren().add(toolbar);
        }

        //================
        //--protected methods
        @Override
        protected void layoutChildren()
        {
            double w = getWidth();
            double h = getHeight();
            double tbHeight = toolbar.prefHeight(w);
            addressBar.setPrefWidth( addressBar.prefWidth(tbHeight) +
                                    (w - toolbar.prefWidth(h)) );
            layoutInArea(browser, 0,tbHeight, w,h-tbHeight, 0, HPos.CENTER, VPos.CENTER);
            layoutInArea(toolbar, 0,0, w,tbHeight, 0, HPos.CENTER,VPos.CENTER);
        }
    }

    @Override
    public Region createJFXNode() throws Exception
    {
        return model_widget.displayShowToolbar().getValue() ?
                new BrowserWithToolbar(model_widget.widgetURL().getValue()) :
                new Browser(model_widget.widgetURL().getValue());
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::sizeChanged);
   }

    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            width = model_widget.positionWidth().getValue();
            height = model_widget.positionHeight().getValue();
            jfx_node.requestLayout(); //need this?
        }
    }
}
