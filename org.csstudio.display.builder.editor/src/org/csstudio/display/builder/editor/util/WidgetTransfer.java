/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.util;

import static org.csstudio.display.builder.editor.DisplayEditor.logger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.tracker.SelectedWidgetUITracker;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
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

    private static Color TRANSPARENT = new Color(0, 0, 0, 24);
    private static Stroke OUTLINE_STROKE = new BasicStroke(2.2F);

    // Could create custom data format, or use "application/xml".
    // Transferring as DataFormat("text/plain"), however, allows exchange
    // with basic text editor, which can be very convenient.

    /**
     * Add support for 'dragging' a widget out of a node
     *
     * @param source Source {@link Node}
     * @param selection
     * @param desc Description of widget type to drag
     * @param image Image to represent the widget, or <code>null</code>
     */
    public static void addDragSupport ( final Node source, final WidgetSelectionHandler selection, final WidgetDescriptor descriptor, final Image image ) {

        source.setOnDragDetected( ( MouseEvent event ) -> {

            logger.log(Level.FINE, "Starting drag for {0}", descriptor);

            selection.clear();

            Widget widget = descriptor.createWidget();
            final String xml;

            try {
                xml = ModelWriter.getXML(Arrays.asList(widget));
            } catch ( Exception ex ) {
                logger.log(Level.WARNING, "Cannot drag-serialize", ex);
                return;
            }

            final Dragboard db = source.startDragAndDrop(TransferMode.COPY);
            final ClipboardContent content = new ClipboardContent();

            content.putString(xml);
            db.setContent(content);

            final int width = widget.propWidth().getValue();
            final int height = widget.propHeight().getValue();

            db.setDragView(createDragImage(widget, image, width, height), width / 2, -height / 2);

            event.consume();

        });

        // TODO Mouse needs to be clicked once after drop completes.
        // Unclear why. Tried source.setOnDragDone() to consume that event, no
        // change.
        // Somehow the drag is still 'active' until one more mouse click.

    }

    /**
     * Add support for dropping widgets
     *
     * @param node Node that will receive the widgets
     * @param group_handler Group handler
     * @param selection_tracker The selection tracker.
     * @param handleDroppedModel Callback for handling the dropped widgets
     */
//    public static void addDropSupport ( final Node node, final ParentHandler group_handler, final Consumer<List<Widget>> handleDroppedModel ) {
    public static void addDropSupport (
            final Node node,
            final ParentHandler group_handler,
            final SelectedWidgetUITracker selection_tracker,
            final Consumer<List<Widget>> handleDroppedModel
    ) {

        node.setOnDragOver( ( DragEvent event ) -> {

            if ( event.getDragboard().hasString() ) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            group_handler.locateParent(event.getX(), event.getY(), 10, 10);
            event.consume();

        });

        node.setOnDragDropped( ( DragEvent event ) -> {

            final Dragboard db = event.getDragboard();

            if ( db.hasString() ) {

                final String xml = db.getString();

                try {

                    final DisplayModel model = ModelReader.parseXML(xml);
                    final List<Widget> widgets = model.getChildren();
                    final Point2D location = selection_tracker.gridConstrain(event.getX(), event.getY());

                    logger.log(Level.FINE, "Dropped {0} widgets", widgets.size());
                    GeometryTools.moveWidgets((int) location.getX(), (int) location.getY(), widgets);
                    handleDroppedModel.accept(widgets);

                } catch ( Exception ex ) {
                    logger.log(Level.WARNING, "Cannot parse dropped model", ex);
                }

                event.setDropCompleted(true);

            } else {
                event.setDropCompleted(false);
            }

            event.consume();

        });

    }

    /**
     * Create a image representing the dragged widget.
     *
     * @param widget The {@link Widget} being dragged.
     * @param image  The widget's type image. Can be {@code null}.
     * @return An {@link Image} instance.
     */
    private static Image createDragImage ( final Widget widget, final Image image, final int width, final int height ) {

        final BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = bImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setBackground(TRANSPARENT);
        g2d.clearRect(0, 0, width, height);
        g2d.setColor(Color.ORANGE);
        g2d.setStroke(OUTLINE_STROKE);
        g2d.drawRect(0, 0, width, height);

        if ( image != null ) {

            int w = (int) image.getWidth();
            int h = (int) image.getHeight();
            BufferedImage bbImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            SwingFXUtils.fromFXImage(image, bbImage);
            g2d.drawImage(bbImage, (int) ( ( width - w ) / 2.0 ), (int) ( ( height - h ) / 2.0 ), null);

        }

        WritableImage dImage = new WritableImage(width, height);

        SwingFXUtils.toFXImage(bImage, dImage);

        return dImage;

    }

}
