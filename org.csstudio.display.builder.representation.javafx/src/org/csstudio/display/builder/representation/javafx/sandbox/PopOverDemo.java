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
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectPropertyBase;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.VLineTo;
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
        	private PopOver popover;
        private final StackPane root = new StackPane();

        // popover.content uses (0, 0) - (w, h)
        // background extends around content by 'ROUND'
        private final Path background = new Path();

        /** Distance between edge of popover.actove_owner and popover.content */
        static final double DISTANCE = 20.0;
        
        private static final double ROUND = 10.0;
        
        private final InvalidationListener update_background = prop ->
        {
            System.out.println("Updating size");
            final double w = root.getWidth(),
                         h = root.getHeight();
            
            switch (popover.side.get())
            {
            case TOP:
                {   // popup is on top of owner, reference point is on bottom
                    final double ref_x = popover.getOwner().getWidth() / 2;
                    background.getElements().setAll(
                            new MoveTo(0, -ROUND),
                            new HLineTo(w),
                            new ArcTo(ROUND, ROUND, 0, w+ROUND, 0, false, true),
                            new VLineTo(h),
                            new ArcTo(ROUND, ROUND, 0, w, h+ROUND, false, true),
                            new HLineTo(ref_x + ROUND),
                            new LineTo(ref_x, h+DISTANCE),
                            new LineTo(ref_x-ROUND, h+ROUND),
                            new HLineTo(0),
                            new ArcTo(ROUND, ROUND, 0, -ROUND, h, false, true),
                            new VLineTo(0),
                            new ArcTo(ROUND, ROUND, 0, 0, -ROUND, false, true)); 
                }
                break;
            case BOTTOM:
                {   // popup is on buttom of owner, reference point is on top
                    final double ref_x = popover.getOwner().getWidth() / 2;
                    background.getElements().setAll(
                            new MoveTo(0, -ROUND),
                            new HLineTo(ref_x - ROUND),
                            new LineTo(ref_x, -DISTANCE),
                            new LineTo(ref_x+ROUND, -ROUND),
                            new HLineTo(w),
                            new ArcTo(ROUND, ROUND, 0, w+ROUND, 0, false, true),
                            new VLineTo(h),
                            new ArcTo(ROUND, ROUND, 0, w, h+ROUND, false, true),
                            new HLineTo(0),
                            new ArcTo(ROUND, ROUND, 0, -ROUND, h, false, true),
                            new VLineTo(0),
                            new ArcTo(ROUND, ROUND, 0, 0, -ROUND, false, true)); 
                }
                break;
            }
        };

        PopOverSkin(final PopOver popover)
        {
            this.popover = popover;
            background.setManaged(false);
            background.setStyle("-fx-stroke: linear-gradient(to bottom, rgba(0,0,0, .3), rgba(0, 0, 0, .7));" + 
                                "-fx-stroke-width: 0.5;" + 
                                "-fx-fill: rgba(255.0,255.0,255.0, .95);" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,.2), 10.0, 0.5, 2.0, 2.0);");
            root.widthProperty().addListener(update_background);
            root.heightProperty().addListener(update_background);
            popover.side.addListener(update_background);
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
        private Region active_owner;
        
        /** Side of owner where popover is shown */
        private ObjectPropertyBase<Side> side = new SimpleObjectProperty(Side.BOTTOM);

        private final InvalidationListener update_position = p ->
        {
            final Bounds bounds = active_owner.localToScreen(active_owner.getBoundsInLocal());

            switch (side.get())
            {
            case BOTTOM:
                setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
                setAnchorX(bounds.getMinX());
                setAnchorY(bounds.getMaxY());
                break;
            case TOP:
                setAnchorLocation(AnchorLocation.WINDOW_BOTTOM_LEFT);
                setAnchorX(bounds.getMinX());
                setAnchorY(bounds.getMinY());
                break;
            default:
                break;
            }
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

        Region getOwner()
        {
            return active_owner;
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
         *  
         *  @param owner Owner node relative to which the PopOver will be located
         *  @see {@link PopupControl#hide()}
         */
        public void show(final Region owner)
        {
            // Unhook from previous owner
            if (active_owner != null)
            {
                final Window window = active_owner.getScene().getWindow();
                window.xProperty().removeListener(weak_update_position);
                window.yProperty().removeListener(weak_update_position);
                active_owner.layoutXProperty().removeListener(weak_update_position);
                active_owner.layoutYProperty().removeListener(weak_update_position);
            }

            // Track movement of owner resp. its window
            active_owner = owner;
            final Window window = owner.getScene().getWindow();
            window.xProperty().addListener(weak_update_position);
            window.yProperty().addListener(weak_update_position);
            owner.layoutXProperty().addListener(weak_update_position);
            owner.layoutYProperty().addListener(weak_update_position);

            // TODO Determine 'side',
            // auto-position above/below/left/right of owner,
            if (side.get() == Side.BOTTOM)
                side.set(Side.TOP);
            else
                side.set(Side.BOTTOM);
            
            // Show relative to owner
            update_position.invalidated(null);
            show(window);
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
