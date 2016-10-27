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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;

import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.tracker.SelectedWidgetUITracker;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.model.widgets.PictureWidget;
import org.csstudio.display.builder.model.widgets.WebBrowserWidget;

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
    public static void addDragSupport ( final Node source, final DisplayEditor editor, final WidgetDescriptor descriptor, final Image image ) {

        source.setOnDragDetected( ( MouseEvent event ) -> {

            logger.log(Level.FINE, "Starting drag for {0}", descriptor);

            editor.getWidgetSelectionHandler().clear();

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
        source.setOnDragDone(event -> editor.getAutoScrollHandler().canceTimeline());

    }

    /**
     * Add support for dropping widgets
     *
     * @param node Node that will receive the widgets
     * @param group_handler Group handler
     * @param selection_tracker The selection tracker.
     * @param handleDroppedModel Callback for handling the dropped widgets
     */
    public static void addDropSupport (
            final Node node,
            final ParentHandler group_handler,
            final SelectedWidgetUITracker selection_tracker,
            final Consumer<List<Widget>> handleDroppedModel
    ) {

        node.setOnDragOver( ( DragEvent event ) -> {

            final Dragboard db = event.getDragboard();

            if ( ( db.hasString() && db.getString() != null )
              || ( db.hasUrl()    && db.getUrl() != null    )
              || ( db.hasRtf()    && db.getRtf() != null    )
              || ( db.hasHtml()   && db.getHtml() != null   )
              || ( db.hasImage()  && db.getImage() != null  ) ) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            group_handler.locateParent(event.getX(), event.getY(), 10, 10);
            event.consume();

        });

        node.setOnDragDropped( ( DragEvent event ) -> {

            final Dragboard db = event.getDragboard();
            final Point2D location = selection_tracker.gridConstrain(event.getX(), event.getY());
            List<Widget> widgets = new ArrayList<>();

            if ( db.hasImage() && db.getImage() != null ) {
                installWidgetsFromImage(db, widgets);
            } else if ( db.hasUrl() && db.getUrl() != null ) {
                installWidgetsFromURL(db, widgets);
            } else if ( db.hasHtml()  && db.getHtml() != null ) {
                installWidgetsFromHTML(db, widgets);
            } else if ( db.hasRtf() && db.getRtf() != null ) {
                installWidgetsFromRTF(db, widgets);
            } else if ( db.hasString() && db.getString() != null ) {
                installWidgetsFromString(db, widgets);
            }

            if ( widgets != null && !widgets.isEmpty() ) {
                acceptWidgets(widgets, location, handleDroppedModel);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }

            event.consume();

        });

    }

    /**
     * Accept and install the given {@code widgets} into the model.
     *
     * @param widgets            The widgets to be installed.
     * @param location           The drop location.
     * @param handleDroppedModel Callback for handling the dropped widgets.
     */
    private static void acceptWidgets ( final List<Widget> widgets, final Point2D location, final Consumer<List<Widget>> handleDroppedModel ) {

        logger.log(Level.FINE, "Dropped {0} widgets.", widgets.size());

        GeometryTools.moveWidgets((int) location.getX(), (int) location.getY(), widgets);
        handleDroppedModel.accept(widgets);

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

    private static void installWidgetsFromHTML ( final Dragboard db, final List<Widget> widgets ) {

        String html = db.getHtml();
        HTMLEditorKit htmlParser = new HTMLEditorKit();
        Document document = htmlParser.createDefaultDocument();

        try {

            htmlParser.read(new ByteArrayInputStream(html.getBytes()), document, 0);

            String text = document.getText(0, document.getLength());
            LabelWidget widget = (LabelWidget) LabelWidget.WIDGET_DESCRIPTOR.createWidget();

            widget.propText().setValue(text);
            widgets.add(widget);

            logger.log(Level.FINE, "Dropped HTML: creating LabelWidget [{0}].", html);

        } catch ( Exception ex ) {
            logger.log(Level.WARNING, "Invalid HTML string [{0}].", ex.getMessage());
        }

    }

    private static void installWidgetsFromImage ( final Dragboard db, final List<Widget> widgets ) {

        try {

            Image image = db.getImage();
            File tmpFile = File.createTempFile("picture", ".png");
            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

            try {

                ImageIO.write(bImage, "png", tmpFile);
                PictureWidget widget = (PictureWidget) PictureWidget.WIDGET_DESCRIPTOR.createWidget();

                widget.propFile().setValue(tmpFile.toString());
                widget.propWidth().setValue((int) image.getWidth());
                widget.propHeight().setValue((int) image.getHeight());
                widgets.add(widget);

                logger.log(Level.FINE, "Dropped image: creating PictureWidget");

            } catch ( IOException ioex ) {
                logger.log(Level.WARNING, "Unable to save image into a temporary location [{0}].", ioex.getMessage());
            }

        } catch ( Exception ex ) {
            logger.log(Level.WARNING, "Invalid image [{0}].", ex.getMessage());
        }

    }

    private static void installWidgetsFromRTF ( final Dragboard db, final List<Widget> widgets ) {

        String rtf = db.getRtf();
        RTFEditorKit rtfParser = new RTFEditorKit();
        Document document = rtfParser.createDefaultDocument();

        try {

            rtfParser.read(new ByteArrayInputStream(rtf.getBytes()), document, 0);

            String text = document.getText(0, document.getLength());
            LabelWidget widget = (LabelWidget) LabelWidget.WIDGET_DESCRIPTOR.createWidget();

            widget.propText().setValue(text);
            widgets.add(widget);

            logger.log(Level.FINE, "Dropped RTF: creating LabelWidget [{0}].", rtf);

        } catch ( Exception ex ) {
            logger.log(Level.WARNING, "Invalid RTF string [{0}].", ex.getMessage());
        }

    }

    private static void installWidgetsFromString ( final Dragboard db, final List<Widget> widgets ) {

        final String xmlOrText = db.getString();

        try {

            final DisplayModel model = ModelReader.parseXML(xmlOrText);

            widgets.addAll(model.getChildren());

        } catch ( Exception ex ) {

            // Not a valid XML. Instantiate a Label object.
            LabelWidget widget = (LabelWidget) LabelWidget.WIDGET_DESCRIPTOR.createWidget();

            widget.propText().setValue(xmlOrText);
            widgets.add(widget);

            logger.log(Level.FINE, "Dropped text: created LabelWidget [{0}].", xmlOrText);

        }

    }

    private static void installWidgetsFromURL ( final Dragboard db, final List<Widget> widgets ) {

        String url = db.getUrl();
        WebBrowserWidget widget = (WebBrowserWidget) WebBrowserWidget.WIDGET_DESCRIPTOR.createWidget();

        widget.propWidgetURL().setValue(url);
        widgets.add(widget);

        logger.log(Level.FINE, "Dropped URL: created WebBrowserWidget [{0}].", url);

    }

}
