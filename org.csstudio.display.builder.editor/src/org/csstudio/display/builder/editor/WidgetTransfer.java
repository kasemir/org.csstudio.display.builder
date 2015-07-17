/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javafx.scene.layout.Pane;

/** Helper for widget drag/drop
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetTransfer
{
    // TODO Turn every tracker move into a 'drag'.
    //      Groups highlight on drag-over,
    //      TransferMode.COPY is handled just like a drop from palette.

    // TODO Create custom data types
    //      for actual widget and for widget description (widget type)
    //      Maybe use new DataFormat("application/xml");

    /** Add support for 'dragging' a widget out of a node
     *  @param source Source {@link Node}
     *  @param desc Description of widget type to drag
     *  @param image Image to represent the widget, or <code>null</code>
     */
    public static void addDragSupport(final Node source,final WidgetDescriptor descriptor, final Image image)
    {
        source.setOnDragDetected((MouseEvent event) ->
        {
            final DisplayModel model = new DisplayModel();
            model.addChild(descriptor.createWidget());
            final String xml;
            try
            {
                xml = ModelWriter.getXML(model);
            }
            catch (Exception ex)
            {
                Logger.getLogger(WidgetTransfer.class.getName())
                      .log(Level.WARNING, "Cannot drag-serialize", ex);
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

    public static void addDropSupport(final Pane pane, final Consumer<DisplayModel> handleDroppedModel)
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
                final String xml = db.getString();
                try
                {
                    final DisplayModel model = ModelReader.parseXML(xml);
                    for (Widget widget : model.getChildren())
                    {
                        widget.positionX().setValue((int)event.getX());
                        widget.positionY().setValue((int)event.getY());
                    }
                    handleDroppedModel.accept(model);
                }
                catch (Exception ex)
                {
                    Logger.getLogger(WidgetTransfer.class.getName())
                    .log(Level.WARNING, "Cannot parse dropped model", ex);
                }
                event.setDropCompleted(true);
            }
            else
                event.setDropCompleted(false);
            event.consume();
        });
    }
}
