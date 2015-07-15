/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetFactory;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

/** Helper for widget drag/drop
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetTransfer
{
    // TODO Create custom data types
    //      for actual widget and for widget description (widget type)
    //      Maybe use new DataFormat("application/xml");

    /** Add support for 'dragging' a widget out of a node
     *  @param source Source {@link Node}
     *  @param desc Description of widget type to drag
     *  @param image Image to represent the widget, or <code>null</code>
     */
    public static void addDragSupport(final Node source,final WidgetDescriptor desc, final Image image)
    {
        source.setOnDragDetected((MouseEvent event) ->
        {
            final Dragboard db = source.startDragAndDrop(TransferMode.COPY);
            final ClipboardContent content = new ClipboardContent();
            content.putString(desc.getType());
            db.setContent(content);
            if (image != null)
                db.setDragView(image);
            event.consume();
        });
        // TODO Mouse needs to be clicked once after drop completes.
        // Unclear why. Tried source.setOnDragDone() to consume that event, no change.
        // Somehow the drag is still 'active' until one more mouse click.
    }

    public static void addDropSupport(final Pane pane, final Consumer<Widget> handleDroppedWidget)
    {
        pane.setOnDragOver((DragEvent event) ->
        {
            if (event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        });

        pane.setOnDragDropped((DragEvent event) ->
        {
            final Dragboard db = event.getDragboard();
            if (db.hasString())
            {
                final String type = db.getString();
                final Optional<WidgetDescriptor> descriptor = WidgetFactory.getInstance().getWidgetDescriptior(type);
                if (descriptor.isPresent())
                {
                    final Widget widget = descriptor.get().createWidget();
                    widget.positionX().setValue((int)event.getX());
                    widget.positionY().setValue((int)event.getY());
                    handleDroppedWidget.accept(widget);
                }
                else
                    Logger.getLogger(WidgetTransfer.class.getName())
                          .log(Level.WARNING, "'Dropped' unknown widget type ", type);
                event.setDropCompleted(true);
            }
            else
                event.setDropCompleted(false);
            event.consume();
        });
    }
}
