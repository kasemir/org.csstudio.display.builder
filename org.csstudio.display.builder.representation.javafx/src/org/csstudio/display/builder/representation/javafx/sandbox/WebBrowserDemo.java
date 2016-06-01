package org.csstudio.display.builder.representation.javafx.sandbox;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class WebBrowserDemo extends Application
{
    public static void main(String [] args)
    {
        launch(args);
    }

    @Override
    public void start(final Stage stage)
    {
        Browser browser = new Browser();

        final Scene scene = new Scene(browser, 800, 700);
        stage.setScene(scene);
        stage.setTitle("WebBrowser");

        stage.show();

    }

    class Browser extends Region
    {
        //================
        //--fields
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        //--toolbar controls
        HBox toolbar;
        final Button backButton = new Button("Back");
        final Button foreButton = new Button("Fwd");
        final Button stop = new Button("Stp");
        final Button refresh  = new Button("Ref");
        final ComboBox<String> addressBar = new ComboBox<String>();
        final Button go = new Button("Go");
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
        public Browser()
        {
            this("http://www.google.com"); //www.oracle.com/products/index.html
        }
        public Browser(String url)
        {
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
                    //((ButtonBase)control).setGraphic(new ImageView(image));
                    HBox.setHgrow(control, Priority.NEVER);
                }
                else //grow address bar
                    HBox.setHgrow(control, Priority.ALWAYS);
            }

            toolbar = new HBox(controls);

            //apply styles
            getStyleClass().add("browser");
            toolbar.getStyleClass().add("browser-toolbar");

            //add components
            getChildren().add(toolbar);
            getChildren().add(browser);

            // load the web page
            goToURL(url);
        }

        private Node createSpacer()
        {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return spacer;
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

        @Override
        protected double computePrefWidth(double height)
        {
            return 750;
        }

        @Override
        protected double computePrefHeight(double width)
        {
            return 500;
        }
    }
}
