/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.sandbox;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Basic {@link PopOver} demo
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PopOverDemo extends Application
{
    /** Skin for PopOver
     *
     *  <p>Shows the popover's 'content'
     *  with a background.
     */
    static class PopOverSkin implements Skin<PopOver>
    {
        private final PopOver popover;
        private final StackPane root = new StackPane();
        // TODO Turn into 'path' with pointer to 'reference'
        private final Rectangle background = new Rectangle(200, 200,  Color.LIGHTGRAY);

        private final InvalidationListener update_background = prop ->
        {
            background.setWidth(root.getWidth());
            background.setHeight(root.getHeight());
        };

        PopOverSkin(final PopOver popover)
        {
            this.popover = popover;
            background.setManaged(false);
            background.setArcWidth(10.0);
            background.setArcHeight(10.0);
            background.setStyle("-fx-fill: rgba(255.0,255.0,255.0, .95);" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,.2), 10.0, 0.5, 2.0, 2.0);");
            root.widthProperty().addListener(update_background);
            root.heightProperty().addListener(update_background);
            root.getChildren().setAll(background, popover.getContentNode());
        }

        @Override
        public PopOver getSkinnable()
        {
            return popover;
        }

        @Override
        public Node getNode()
        {
            return root;
        }

        @Override
        public void dispose()
        {
            // NOP
        }
    }

    /** PopupControl with fixed 'content'.
     *
     *  <p>Displays that content as a popup,
     *  adding a basic background.
     *
     *  <p>Popup is positioned relative to an 'owner'
     *  node and moves with it when the window is relocated.
     */
    static class PopOver extends PopupControl
    {
        /** Root of content scene graph */
        private final Node content;

        /** Node to which the popover is currently attached */
        private Node active_owner;

        private final InvalidationListener update_position = p ->
        {
            final Bounds bounds = active_owner.localToScreen(active_owner.getBoundsInLocal());
            setAnchorX(bounds.getMinX());
            setAnchorY(bounds.getMaxY());
        };
        private final WeakInvalidationListener weak_update_position = new WeakInvalidationListener(update_position);

        /** Create popover
         *
         *  @param content Root of content scene graph
         */
        public PopOver(final Node content)
        {
            this.content = content;
        }

        Node getContentNode()
        {
            return content;
        }

        @Override
        protected Skin<PopOver> createDefaultSkin()
        {
            return new PopOverSkin(this);
        }

        /** Show PopOver positioned relative to other node
         *
         *  <p>Moving the node or window will result
         *  in move of the PopOver.
         *  @param owner Owner node relative to which the PopOver will be located
         *  @see {@link PopupControl#hide()}
         */
        public void show(final Node owner)
        {
            // Unhook from previous owner
            if (active_owner != null)
            {
                final Window window = active_owner.getScene().getWindow();
                window.xProperty().removeListener(weak_update_position);
                window.yProperty().removeListener(weak_update_position);
            }

            // Track movement of owner resp. its window
            active_owner = owner;
            final Window window = owner.getScene().getWindow();
            window.xProperty().addListener(weak_update_position);
            window.yProperty().addListener(weak_update_position);

            // TODO Determine 'reference',
            // auto-position above/below/left/right of owner

            // Show relative to owner
            final Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());
            show(owner, bounds.getMinX(), bounds.getMaxY());
        }
    }

    @Override
    public void start(final Stage stage)
    {
        final BorderPane content = new BorderPane(new TextField("Center"), new Label("Top"), new Label("Right"), new Label("Bottom"), new Label("Left"));
        final PopOver popover = new PopOver(content);

        final Button toggle_popup = new Button("Popup");
        toggle_popup.setOnAction(event ->
        {
            if (popover.isShowing())
                popover.hide();
            else
                popover.show(toggle_popup);
        });

        final BorderPane layout = new BorderPane(toggle_popup);
        stage.setScene(new Scene(layout));
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
