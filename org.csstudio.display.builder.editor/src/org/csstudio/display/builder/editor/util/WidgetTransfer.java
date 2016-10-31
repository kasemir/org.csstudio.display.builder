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
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.csstudio.display.builder.editor.DisplayEditor;
import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.tracker.SelectedWidgetUITracker;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget.Resize;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.model.widgets.PictureWidget;
import org.csstudio.display.builder.model.widgets.WebBrowserWidget;
import org.eclipse.osgi.util.NLS;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;


/**
 * Helper for widget drag/drop
 *
 * @author Kay Kasemir
 * @author Claudio Rosati
 */
@SuppressWarnings("nls")
public class WidgetTransfer
{

    private static Color TRANSPARENT = new Color(0, 0, 0, 24);
    private static Stroke OUTLINE_STROKE = new BasicStroke(2.2F);

    //  The extensions listed here MUST BE ALL UPPERCASE.
    private static List<String> IMAGE_FILE_EXTENSIONS = Arrays.asList("BMP", "GIF", "JPEG", "JPG", "PNG");
    private static List<String> EMBEDDED_FILE_EXTENSIONS = Arrays.asList("BOB", "OPI");
    private static List<String> SUPPORTED_EXTENSIONS = Stream.of(IMAGE_FILE_EXTENSIONS, EMBEDDED_FILE_EXTENSIONS).flatMap(Collection::stream).collect(Collectors.toList());

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

            if ( db.hasString() || db.hasUrl() || db.hasRtf() || db.hasHtml() || db.hasImage() || db.hasFiles() ) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            group_handler.locateParent(event.getX(), event.getY(), 10, 10);
            event.consume();

        });

        node.setOnDragDropped( ( DragEvent event ) -> {

            final Dragboard db = event.getDragboard();
            final Point2D location = selection_tracker.gridConstrain(event.getX(), event.getY());
            List<Widget> widgets = new ArrayList<>();

            if ( db.hasFiles()  && canAcceptFiles(db.getFiles()) ) {
                installWidgetsFromFiles(db, selection_tracker, widgets);
            } else if ( db.hasImage() && db.getImage() != null ) {
                installWidgetsFromImage(db, selection_tracker, widgets);
            } else if ( db.hasUrl() && db.getUrl() != null ) {
                installWidgetsFromURL(event, selection_tracker, widgets);
            } else if ( db.hasHtml()  && db.getHtml() != null ) {
                installWidgetsFromHTML(db, widgets);
            } else if ( db.hasRtf() && db.getRtf() != null ) {
                installWidgetsFromRTF(db, widgets);
            } else if ( db.hasString() && db.getString() != null ) {
                installWidgetsFromString(db, selection_tracker, widgets);
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
     * Return {@code true} if there is a {@link File} in {@code files}
     * whose extension is one of the {@link #SUPPORTED_EXTENSIONS}.
     * <P>
     * <B>Note:<B> only one file will be accepted: the first one
     * matching the above condition.
     *
     * @param files The {@link List} of {@link File}s to be checked.
     *              Can be {@code null} or empty.
     * @return {@code true} if a file existing whose extension is
     *         contained in {@link #SUPPORTED_EXTENSIONS}.
     */
    private static boolean canAcceptFiles ( List<File> files ) {

        if ( files != null && !files.isEmpty() ) {
            return files.stream().anyMatch(f -> SUPPORTED_EXTENSIONS.contains(FilenameUtils.getExtension(f.toString()).toUpperCase()));
        }

        return false;

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

    /**
     * @param file              The image file used to create and preset a {@link PictureWidget}.
     * @param index             The index of the {@code file} inside the dropped set.
     * @param selection_tracker Used to get the grid steps from its model to be used
     *                          in offsetting multiple widgets.
     * @param widgets           The container of the created widgets.
     */
    private static void imageFileAcceptor ( File file, int index, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets ) {

        if ( file!= null && file.exists() ) {
            try {

                DisplayModel model = selection_tracker.getModel();
                BufferedImage image = ImageIO.read(file);
                PictureWidget widget = (PictureWidget) PictureWidget.WIDGET_DESCRIPTOR.createWidget();

                widget.propFile().setValue(ModelResourceUtil.getRelativePath(model.getUserData(DisplayModel.USER_DATA_INPUT_FILE), file.toString()));
                widget.propX().setValue(model.propGridStepX().getValue() * index);
                widget.propY().setValue(model.propGridStepY().getValue() * index);
                widget.propWidth().setValue(image.getWidth());
                widget.propHeight().setValue(image.getHeight());
                widgets.add(widget);

                logger.log(Level.FINE, "Dropped image file: creating PictureWidget");

            } catch ( IOException ex ) {
                logger.log(Level.WARNING, "Unable to read image [{0}].", ex.getMessage());
            }
        }

    }

    /**
     * @param db                The {@link Dragboard} containing the dragged data.
     * @param selection_tracker Used to get the grid steps from its model to be used
     *                          in offsetting multiple widgets.
     * @param widgets           The container of the created widgets.
     */
    private static void installWidgetsFromFiles ( final Dragboard db, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets ) {

        List<File> files = db.getFiles();

        for ( int i = 0; i < files.size(); i++ ) {

            File file = files.get(i);
            String extension = FilenameUtils.getExtension(file.toString()).toUpperCase();

            if ( IMAGE_FILE_EXTENSIONS.contains(extension) ) {
                imageFileAcceptor(file, i, selection_tracker, widgets);
            } else if ( EMBEDDED_FILE_EXTENSIONS.contains(extension) ) {
                opiFileAcceptor(file, i, selection_tracker, widgets);
            }

        }

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

    /**
     * @param db                The {@link Dragboard} containing the dragged data.
     * @param selection_tracker Used to get the display model.
     * @param widgets           The container of the created widgets.
     */
    private static void installWidgetsFromImage ( final Dragboard db, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets ) {

        try {

            Image image = db.getImage();
            File file = File.createTempFile("picture", ".png");
            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

            try {

                ImageIO.write(bImage, "png", file);

                DisplayModel model = selection_tracker.getModel();
                PictureWidget widget = (PictureWidget) PictureWidget.WIDGET_DESCRIPTOR.createWidget();

                widget.propFile().setValue(ModelResourceUtil.getRelativePath(model.getUserData(DisplayModel.USER_DATA_INPUT_FILE), file.toString()));
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

    /**
     * @param db                The {@link Dragboard} containing the dragged data.
     * @param selection_tracker Used to get the grid steps from its model to be used
     *                          in offsetting multiple widgets.
     * @param widgets           The container of the created widgets.
     */
    private static void installWidgetsFromString ( final Dragboard db, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets ) {

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

    /**
     * @param event             The {@link DragEvent} object.
     * @param selection_tracker Used to get display model.
     * @param widgets           The container of the created widgets.
     */
    private static void installWidgetsFromURL ( final DragEvent event, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets ) {

        final Dragboard db = event.getDragboard();
        final String url = db.getUrl();
//  TODO: CR: provare ad usare Labels con icone.
        List<String> choices = new ArrayList<>(3);

        choices.add(WebBrowserWidget.WIDGET_DESCRIPTOR.getName());
        choices.add(PictureWidget.WIDGET_DESCRIPTOR.getName());
        choices.add(EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.getName());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);

        dialog.setTitle(Messages.WT_FromURL_dialog_title);
        dialog.setHeaderText(NLS.bind(Messages.WT_FromURL_dialog_headerFMT, reduceURL(url)));
        dialog.setContentText(Messages.WT_FromURL_dialog_content);

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(choice -> {
            if ( WebBrowserWidget.WIDGET_DESCRIPTOR.getName().equals(choice) ) {

              WebBrowserWidget widget = (WebBrowserWidget) WebBrowserWidget.WIDGET_DESCRIPTOR.createWidget();

              widget.propWidgetURL().setValue(url);
              widgets.add(widget);

              logger.log(Level.FINE, "Dropped URL: created WebBrowserWidget [{0}].", url);

            } else if ( PictureWidget.WIDGET_DESCRIPTOR.getName().equals(choice) ) {
                try {

                    Image image = new Image(url, false);
                    PictureWidget widget = (PictureWidget) PictureWidget.WIDGET_DESCRIPTOR.createWidget();

                    widget.propFile().setValue(url);
                    widget.propWidth().setValue((int) image.getWidth());
                    widget.propHeight().setValue((int) image.getHeight());
                    widgets.add(widget);

                    logger.log(Level.FINE, "Dropped image URL: creating PictureWidget");

                } catch ( Exception ex ) {
                    logger.log(Level.WARNING, "Invalid image [{0}].", ex.getMessage());
                }
            } else if ( EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.getName().equals(choice) ) {
                try {

                    EmbeddedDisplayWidget widget = (EmbeddedDisplayWidget) EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.createWidget();

                    widget.propFile().setValue(url);
                    widget.propResize().setValue(Resize.SizeToContent);

                    try ( InputStream istream = new URL(url).openStream() ) {

                        ModelReader reader = new ModelReader(istream);
                        Optional<String> name = reader.getName();

                        if ( name.isPresent() ) {
                            widget.propName().setValue(name.get());
                        }

                    } catch ( Exception iex ) {
                        logger.log(Level.WARNING, "Unable to read OPI/BOB file [{0}].", iex.getMessage());
                    }

                    widgets.add(widget);

                    logger.log(Level.FINE, "Dropped image file: creating PictureWidget");

                } catch ( Exception ex ) {
                    logger.log(Level.WARNING, "Unable to read OPI/BOB file [{0}].", ex.getMessage());
                }
            }
        });

    }

    /**
     * @param file              The image file used to create and preset a {@link EmbeddedDisplayWidget}.
     * @param index             The index of the {@code file} inside the dropped set.
     * @param selection_tracker Used to get the grid steps from its model to be used
     *                          in offsetting multiple widgets.
     * @param widgets           The container of the created widgets.
     */
    private static void opiFileAcceptor ( File file, int index, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets ) {

        if ( file!= null && file.exists() ) {
            try {

                DisplayModel model = selection_tracker.getModel();
                EmbeddedDisplayWidget widget = (EmbeddedDisplayWidget) EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.createWidget();

                widget.propFile().setValue(ModelResourceUtil.getRelativePath(model.getUserData(DisplayModel.USER_DATA_INPUT_FILE), file.toString()));
                widget.propX().setValue(model.propGridStepX().getValue() * index);
                widget.propY().setValue(model.propGridStepY().getValue() * index);
                widget.propResize().setValue(Resize.SizeToContent);

                try ( BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file)) ) {

                    ModelReader reader = new ModelReader(bis);
                    Optional<String> name = reader.getName();

                    if ( name.isPresent() ) {
                        widget.propName().setValue(name.get());
                    }

                } catch ( Exception iex ) {
                    logger.log(Level.WARNING, "Unable to read OPI/BOB file [{0}].", iex.getMessage());
                }

                widgets.add(widget);

                logger.log(Level.FINE, "Dropped image file: creating PictureWidget");

            } catch ( Exception ex ) {
                logger.log(Level.WARNING, "Unable to read OPI/BOB file [{0}].", ex.getMessage());
            }
        }

    }

    /**
     * Return a reduced version of the given {@code url}.
     *
     * @param url An URL string tha, if long, must be reduced
     *            to a shorter version to be displayed.
     * @return A reduced version of the given {@code url}.
     */
    private static String reduceURL ( String url ) {

        if ( url.length() > 64 ) {

            String shortURL = url;
            int leftSlash = 2;
            int rightSlash = 2;

            if ( url.contains("://") ) {
                leftSlash += 2;
            } else if ( url.startsWith("/") ) {
                leftSlash += 1;
            }

            if ( StringUtils.countMatches(url, '/') > ( leftSlash + rightSlash ) ) {

                int leftSlashIndex = StringUtils.ordinalIndexOf(url, "/", leftSlash);
                int rightSlashIndex = StringUtils.ordinalIndexOf(StringUtils.reverse(url), "/", rightSlash);

                shortURL = StringUtils.join(StringUtils.left(url, leftSlashIndex + 1), "...", StringUtils.right(url, rightSlashIndex + 1));

                if ( shortURL.length() <= 80 ) {
                    return shortURL;
                }

            }

            if ( shortURL.length() > 64 ) {

                leftSlash--;
                rightSlash--;

                if ( StringUtils.countMatches(url, '/') > ( leftSlash + rightSlash ) ) {

                    int leftSlashIndex = StringUtils.ordinalIndexOf(url, "/", leftSlash);
                    int rightSlashIndex = StringUtils.ordinalIndexOf(StringUtils.reverse(url), "/", rightSlash);

                    shortURL = StringUtils.join(StringUtils.left(url, leftSlashIndex + 1), "...", StringUtils.right(url, rightSlashIndex + 1));

                    if ( shortURL.length() <= 80 ) {
                        return shortURL;
                    }

                }

            }

            if ( shortURL.length() > 64 ) {
                return StringUtils.join(StringUtils.left(url, 32), "...", StringUtils.right(url, 32));
            }

        }

        return url;

    }

}
