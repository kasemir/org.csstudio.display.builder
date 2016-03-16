/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/** Helper for widget drag/drop
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetTransfer
{
    private static final Logger logger = Logger.getLogger(WidgetTransfer.class.getName());

    // Could create custom data format, or use "application/xml".
    // Transferring as DataFormat("text/plain"), however, allows exchange
    // with basic text editor, which can be very convenient.

    /** Add support for 'dragging' a widget out of a node
     *  @param source Source {@link Node}
     *  @param selection
     *  @param desc Description of widget type to drag
     *  @param image Image to represent the widget, or <code>null</code>
     */
    public static void addDragSupport(final Node source, final WidgetSelectionHandler selection, final WidgetDescriptor descriptor, final Image image)
    {
        source.setOnDragDetected((MouseEvent event) ->
        {
            logger.log(Level.FINE, "Starting drag for {0}", descriptor);
            selection.clear();
            final String xml;
            try
            {
                xml = ModelWriter.getXML(Arrays.asList(descriptor.createWidget()));
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot drag-serialize", ex);
                return;
            }
            final Dragboard db = source.startDragAndDrop(TransferMode.COPY);
            final ClipboardContent content = new ClipboardContent();
            content.putString(xml);
            db.setContent(content);
            if (image != null)
                db.setDragView(image);
            event.consume();
        });
        // TODO Mouse needs to be clicked once after drop completes.
        // Unclear why. Tried source.setOnDragDone() to consume that event, no change.
        // Somehow the drag is still 'active' until one more mouse click.
    }

    /** Add support for dropping widgets
     *  @param node Node that will receive the widgets
     *  @param group_handler Group handler
     *  @param handleDroppedModel Callback for handling the dropped widgets
     */
    public static void addDropSupport(final Node node,
                                      final ParentHandler group_handler,
                                      final Consumer<DisplayModel> handleDroppedModel)
    {
        node.setOnDragOver((DragEvent event) ->
        {
            if (event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

            group_handler.locateParent(event.getX(), event.getY(), 10, 10);
            event.consume();
        });

        node.setOnDragDropped((DragEvent event) ->
        {
            final Dragboard db = event.getDragboard();
            if (db.hasString())
            {
                final String xml = db.getString();
                try
                {
                    final DisplayModel model = ModelReader.parseXML(xml);
                    final List<Widget> widgets = model.getChildren();
                    logger.log(Level.FINE, "Dropped {0} widgets", widgets.size());

                    // Find upper left corner of dropped widgets
                    int min_x = Integer.MAX_VALUE, min_y = Integer.MAX_VALUE;
                    for (Widget widget : widgets)
                    {
                        min_x = Math.min(widget.positionX().getValue(), min_x);
                        min_y = Math.min(widget.positionY().getValue(), min_y);
                    }
                    // Move upper left corner to mouse location
                    final int dx = (int)event.getX() - Math.max(0, min_x);
                    final int dy = (int)event.getY() - Math.max(0, min_y);
                    for (Widget widget : widgets)
                    {
                        widget.positionX().setValue(widget.positionX().getValue() + dx);
                        widget.positionY().setValue(widget.positionY().getValue() + dy);
                    }
                    handleDroppedModel.accept(model);
                }
                catch (Exception ex)
                {
                    logger.log(Level.WARNING, "Cannot parse dropped model", ex);
                }
                event.setDropCompleted(true);
            }
            else
                event.setDropCompleted(false);
            event.consume();
        });
    }
}
