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

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class WebBrowserRepresentation extends RegionBaseRepresentation<Region, WebBrowserWidget>
{
    private final DirtyFlag dirty_size = new DirtyFlag();

    private volatile double width;
    private volatile double height;

    class BrowserWithToolbar extends BrowserWithoutToolbar
    {
        //--toolbar controls
        HBox toolbar;
        final Button backButton = new Button();
        final Button foreButton = new Button();
        final Button stop = new Button();
        final Button refresh  = new Button();
        final ComboBox<String> addressBar = new ComboBox<String>();
        final Button go = new Button();
        Control [] controls = new Control []
                {backButton, foreButton, stop, refresh, addressBar, go};
        String [] iconFiles = new String []
                {"arrow_left.png", "arrow_right.png", "green_chevron.png", "Player_stop.png", "refresh.png"};

        //--toolbar event handlers
        EventHandler<ActionEvent> backButtonEvent = (event) ->
        {
            try { webEngine.getHistory().go(-1); }
            catch (IndexOutOfBoundsException e) {}
        };
        EventHandler<ActionEvent> foreButtonEvent = (event) ->
        {
            try { webEngine.getHistory().go(1); }
            catch (IndexOutOfBoundsException e) {}
        };
        EventHandler<ActionEvent> stopEvent =
                (event) -> webEngine.getLoadWorker().cancel();
        EventHandler<ActionEvent> refreshEvent =
                (event) -> goToURL(webEngine.getLocation());
        EventHandler<ActionEvent> goEvent =
                (event) -> goToURL(addressBar.getValue());

        private void goToURL(String url)
        {
            if (!url.startsWith("http://"))
                if (url.equals(""))
                    url = "about:blank";
                else
                    url = "http://" + url;
            webEngine.load(url);
        }

        //================
        //--constructors
        public BrowserWithToolbar(String url)
        {
            super(url);
            //assemble toolbar controls
            backButton.setOnAction(backButtonEvent);
            foreButton.setOnAction(foreButtonEvent);
            stop.setOnAction(stopEvent);
            refresh.setOnAction(refreshEvent);
            addressBar.setOnAction(goEvent);
            go.setOnAction(goEvent);

            addressBar.setEditable(true);
            webEngine.locationProperty().addListener((observable, oldval, newval)->
            {
                addressBar.getEditor().setText(newval);
                ObservableList<String> items = addressBar.getItems();
                if (!items.contains(newval))
                    items.add(0, newval);
                foreButton.setDisable(items.get(0).equals(newval));
                backButton.setDisable(items.get(items.size()-1).equals(newval));
            });

            final String imageDirectory =
                    "platform:/plugin/org.csstudio.display.builder.model/icons/browser/";
            for (int i = 0; i < controls.length; i++)
            {
                Control control = controls[i];
                if (control instanceof ButtonBase)
                {
                    Image image = new Image(getClass().getResourceAsStream(imageDirectory+iconFiles[i]));
                    ((ButtonBase)control).setGraphic(new ImageView(image));
                    HBox.setHgrow(control, Priority.NEVER);
                }
                else //grow address bar
                    HBox.setHgrow(control, Priority.ALWAYS);
            }

            toolbar = new HBox(controls);

            //apply style
            toolbar.getStyleClass().add("browser-toolbar");
            //add component
            getChildren().add(0, toolbar);
        }

        @Override protected void layoutChildren()
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

    class BrowserWithoutToolbar extends Region
    {
        //================
        //--fields
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        //================
        //--constructors
        public BrowserWithoutToolbar(String url)
        {
            //apply styles
            getStyleClass().add("browser");

            //add components
            getChildren().add(browser);

            // load the web page
            webEngine.load(url);
        }

        private Node createSpacer()
        {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return spacer;
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

    @Override
    public Region createJFXNode() throws Exception
    {
        return model_widget.displayShowToolbar().getValue() ?
                new BrowserWithToolbar(model_widget.widgetURL().getValue()) :
                new BrowserWithoutToolbar(model_widget.widgetURL().getValue());
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
            //jfx_node.requestLayout(); //need this?
        }
    }
}
