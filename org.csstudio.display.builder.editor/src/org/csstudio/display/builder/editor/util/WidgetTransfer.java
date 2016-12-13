/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.util;

import static org.csstudio.display.builder.editor.Plugin.logger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.csstudio.display.builder.editor.EditorUtil;
import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.tracker.SelectedWidgetUITracker;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetFactory;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.persist.WidgetClassesService;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.model.widgets.PVWidget;
import org.csstudio.display.builder.model.widgets.PictureWidget;
import org.csstudio.display.builder.model.widgets.WebBrowserWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
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

/** Helper for widget drag/drop
 *
 *  <h3>Handling New File/URL Extensions</h3>
 *  To add the support for a new set of file/URL extensions do the following:
 *  <ul>
 *   <li>Create a new static list of extensions (see {@link #IMAGE_FILE_EXTENSIONS});</li>
 *   <li>Update {@link #SUPPORTED_EXTENSIONS} to include the new list;</li>
 *   <li>Update {@link #installWidgetsFromFiles(List,SelectedWidgetUITracker,List)
 *       to handle files having the new extensions;</li>
 *   <li>Update {@link #installWidgetsFromURL(DragEvent,List)
 *       to handle URLs having the new extensions.</li>
 *  </ul>
 *
 *  @author Kay Kasemir
 *  @author Claudio Rosati
 */
@SuppressWarnings("nls")
public class WidgetTransfer
{
    private static Color TRANSPARENT = new Color(0, 0, 0, 24);
    private static Stroke OUTLINE_STROKE = new BasicStroke(2.2F);

    //  The extensions listed here MUST BE ALL UPPERCASE.
    private static List<String> IMAGE_FILE_EXTENSIONS = Arrays.asList("BMP", "GIF", "JPEG", "JPG", "PNG");
    private static List<String> EMBEDDED_FILE_EXTENSIONS = Arrays.asList("BOB", "OPI");
    private static List<String> SUPPORTED_EXTENSIONS = Stream.concat(IMAGE_FILE_EXTENSIONS.stream(), EMBEDDED_FILE_EXTENSIONS.stream()).collect(Collectors.toList());

    // Lazily initialized list of widgets that have a PV
    private static List<WidgetDescriptor> pvWidgetDescriptors = null;

    // Could create custom data format, or use "application/xml".
    // Transferring as DataFormat("text/plain"), however, allows exchange
    // with basic text editor, which can be very convenient.

    /** Add support for 'dragging' a widget out of a node
     *
     *  @param source Source {@link Node}
     *  @param selection
     *  @param desc Description of widget type to drag
     *  @param image Image to represent the widget, or <code>null</code>
     */
    public static void addDragSupport(final Node source, final DisplayEditor editor, final WidgetDescriptor descriptor, final Image image)
    {
        source.setOnDragDetected( ( MouseEvent event ) ->
        {
            logger.log(Level.FINE, "Starting drag for {0}", descriptor);

            editor.getWidgetSelectionHandler().clear();

            final Widget widget = descriptor.createWidget();
            WidgetClassesService.getWidgetClasses().apply(widget);
            final String xml;
            try
            {
                xml = ModelWriter.getXML(Arrays.asList(widget));
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

            final int width = widget.propWidth().getValue();
            final int height = widget.propHeight().getValue();
            db.setDragView(createDragImage(widget, image, width, height), width / 2, -height / 2);

            event.consume();
        });
        source.setOnDragDone(event -> editor.getAutoScrollHandler().canceTimeline());
    }

    /** Add support for dropping widgets
     *
     *  @param node Node that will receive the widgets
     *  @param group_handler Group handler
     *  @param selection_tracker The selection tracker.
     *  @param handleDroppedModel Callback for handling the dropped widgets
     */
    public static void addDropSupport(
            final Node node,
            final ParentHandler group_handler,
            final SelectedWidgetUITracker selection_tracker,
            final Consumer<List<Widget>> handleDroppedModel)
    {
        node.setOnDragOver( ( DragEvent event ) ->
        {
            final Dragboard db = event.getDragboard();

            if (db.hasString() || db.hasUrl() || db.hasRtf() || db.hasHtml() || db.hasImage() || db.hasFiles())
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

            group_handler.locateParent(event.getX(), event.getY(), 10, 10);
            event.consume();
        });

        node.setOnDragDropped( (DragEvent event) ->
        {
            final Dragboard db = event.getDragboard();
            final Point2D location = selection_tracker.gridConstrain(event.getX(), event.getY());
            final List<Widget> widgets = new ArrayList<>();
            final List<Runnable> updates = new ArrayList<>();

            if (db.hasFiles()  &&  canAcceptFiles(db.getFiles()))
                installWidgetsFromFiles(db, selection_tracker, widgets, updates);
            else if (db.hasImage() && db.getImage() != null)
                installWidgetsFromImage(db, selection_tracker, widgets);
            else if (db.hasUrl() && db.getUrl() != null)
                installWidgetsFromURL(event, widgets, updates);
            else if (db.hasHtml()  && db.getHtml() != null)
               installWidgetsFromHTML(event, selection_tracker, widgets);
            else if (db.hasRtf() && db.getRtf() != null)
                installWidgetsFromRTF(event, selection_tracker, widgets);
            else if (db.hasString() && db.getString() != null)
                installWidgetsFromString(event, selection_tracker, widgets);

            if (widgets.isEmpty())
                event.setDropCompleted(false);
            else
            {
                logger.log(Level.FINE, "Dropped {0} widgets.", widgets.size());
                GeometryTools.moveWidgets((int) location.getX(), (int) location.getY(), widgets);
                handleDroppedModel.accept(widgets);
                // Now that model holds the widgets, perform updates that for example check an image size
                for (Runnable update : updates)
                    EditorUtil.getExecutor().execute(update);
                event.setDropCompleted(true);
            }
            event.consume();
        });
    }

    /** Return {@code true} if there is a {@link File} in {@code files}
     *  whose extension is one of the {@link #SUPPORTED_EXTENSIONS}.
     *
     *  <P>
     *  <B>Note:<B> only one file will be accepted: the first one
     *  matching the above condition.
     *
     *  @param files The {@link List} of {@link File}s to be checked.
     *               Can be {@code null} or empty.
     *  @return {@code true} if a file existing whose extension is
     *          contained in {@link #SUPPORTED_EXTENSIONS}.
     */
    private static boolean canAcceptFiles(final List<File> files)
    {
        if (files != null && !files.isEmpty())
            return files.stream().anyMatch(f -> SUPPORTED_EXTENSIONS.contains(FilenameUtils.getExtension(f.toString()).toUpperCase()));
        return false;
    }

    /** Create a image representing the dragged widget.
     *
     *  @param widget The {@link Widget} being dragged.
     *  @param image  The widget's type image. Can be {@code null}.
     *  @return An {@link Image} instance.
     */
    private static Image createDragImage (final Widget widget, final Image image, final int width, final int height)
    {
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

        if ( image != null )
        {
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

    /** @param image_file        The image file used to create and preset a {@link PictureWidget}.
     *  @param selection_tracker Used to get the grid steps from its model to be used
     *                           in offsetting multiple widgets.
     *  @param widgets           The container of the created widgets.
     *  @param updates           Updates to perform on widgets
     */
    private static void imageFileAcceptor(final String image_file, final SelectedWidgetUITracker selection_tracker,
                                          final List<Widget> widgets, final List<Runnable> updates)
    {
        logger.log(Level.FINE, "Creating PictureWidget for dropped image {0}", image_file);
        final DisplayModel model = selection_tracker.getModel();
        final PictureWidget widget = (PictureWidget) PictureWidget.WIDGET_DESCRIPTOR.createWidget();
        widget.propFile().setValue(image_file);
        final int index = widgets.size();
        widget.propX().setValue(model.propGridStepX().getValue() * index);
        widget.propY().setValue(model.propGridStepY().getValue() * index);
        widgets.add(widget);
        updates.add(() -> updatePictureWidget(widget));
    }

    /** Update a picture widget's size from image file
     *  @param widget {@link PictureWidget}
     */
    private static void updatePictureWidget(final PictureWidget widget)
    {
        final String image_file = widget.propFile().getValue();
        try
        {
            final String filename = ModelResourceUtil.resolveResource(widget.getTopDisplayModel(), image_file);
            final BufferedImage image = ImageIO.read(ModelResourceUtil.openResourceStream(filename));
            widget.propWidth().setValue(image.getWidth());
            widget.propHeight().setValue(image.getHeight());
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot obtain image size for " + image_file, ex);
        }
    }

    /** @param db                The {@link Dragboard} containing the dragged data.
     *  @param selection_tracker Used to get the grid steps from its model to be used
     *                           in offsetting multiple widgets.
     *  @param widgets           The container of the created widgets.
     *  @param updates           Updates to perform on widgets
     */
    private static void installWidgetsFromFiles(final Dragboard db, final SelectedWidgetUITracker selection_tracker,
                                                final List<Widget> widgets, final List<Runnable> updates)
    {
        final List<File> files = db.getFiles();
        for (int i = 0; i < files.size(); i++)
        {
            String filename = files.get(i).toString();
            // If running under RCP, try to convert dropped files which are always
            // absolute file system locations into workspace resource
            final String workspace_file = ModelResourceUtil.getWorkspacePath(filename);
            if (workspace_file != null)
                filename = workspace_file;
            // Attempt to turn into relative path
            final DisplayModel model = selection_tracker.getModel();
            filename = ModelResourceUtil.getRelativePath(model.getUserData(DisplayModel.USER_DATA_INPUT_FILE), filename);

            final String extension = FilenameUtils.getExtension(filename).toUpperCase();
            if (IMAGE_FILE_EXTENSIONS.contains(extension))
                imageFileAcceptor(filename, selection_tracker, widgets, updates);
           else if (EMBEDDED_FILE_EXTENSIONS.contains(extension) )
                displayFileAcceptor(filename, selection_tracker, widgets, updates);
        }
    }

    /** @param event             The {@link DragEvent} containing the dragged data.
     *  @param selection_tracker Used to get the grid steps from its model to be used
     *                           in offsetting multiple widgets.
     *  @param widgets           The container of the created widgets.
     */
    private static void installWidgetsFromHTML(final DragEvent event, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets)
    {
        final Dragboard db = event.getDragboard();
        String html = db.getHtml();
        HTMLEditorKit htmlParser = new HTMLEditorKit();
        Document document = htmlParser.createDefaultDocument();
        try
        {
            htmlParser.read(new ByteArrayInputStream(html.getBytes()), document, 0);
            installWidgetsFromString(event, document.getText(0, document.getLength()), selection_tracker, widgets);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Invalid HTML string", ex);
        }
    }

    /** @param db                The {@link Dragboard} containing the dragged data.
     *  @param selection_tracker Used to get the display model.
     *  @param widgets           The container of the created widgets.
     */
    private static void installWidgetsFromImage(final Dragboard db, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets)
    {
        logger.log(Level.FINE, "Dropped image: creating PictureWidget");
        final DisplayModel model = selection_tracker.getModel();
        final ToolkitRepresentation<?, ?> toolkit = ToolkitRepresentation.getToolkit(selection_tracker.getModel());
        final String filename = toolkit.showSaveAsDialog(model, null);
        if (filename == null)
            return;

        final Image image = db.getImage();
        if (image == null)
            return;

        final PictureWidget widget = (PictureWidget) PictureWidget.WIDGET_DESCRIPTOR.createWidget();
        widget.propWidth().setValue((int) image.getWidth());
        widget.propHeight().setValue((int) image.getHeight());
        widgets.add(widget);

        // File access should not be on UI thread,
        // but we need to return the widget right away.
        // -> Return the widget now,
        //    create the image file later,
        //    and then update the widget's file property
        EditorUtil.getExecutor().execute(() ->
        {
            try
            {
                BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bImage, "png", new File(filename));
                widget.propFile().setValue(ModelResourceUtil.getRelativePath(model.getUserData(DisplayModel.USER_DATA_INPUT_FILE), filename));
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot save image as " + filename, ex);
            }
        });
    }

    /** @param db                The {@link Dragboard} containing the dragged data.
     *  @param selection_tracker Used to get the grid steps from its model to be used
     *                           in offsetting multiple widgets.
     *  @param widgets           The container of the created widgets.
     */
    private static void installWidgetsFromRTF(final DragEvent event, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets)
    {
        final Dragboard db = event.getDragboard();
        final String rtf = db.getRtf();
        final RTFEditorKit rtfParser = new RTFEditorKit();
        final Document document = rtfParser.createDefaultDocument();

        try
        {
            rtfParser.read(new ByteArrayInputStream(rtf.getBytes()), document, 0);
            installWidgetsFromString(event, document.getText(0, document.getLength()), selection_tracker, widgets);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Invalid RTF string", ex);
        }
    }

    /** @param event             The {@link DragEvent} containing the dragged data.
     *  @param selection_tracker Used to get the grid steps from its model to be used
     *                           in offsetting multiple widgets.
     *  @param widgets           The container of the created widgets.
     */
    private static void installWidgetsFromString(final DragEvent event, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets)
    {
        final Dragboard db = event.getDragboard();
        final String xmlOrText = db.getString();
        try
        {
            final DisplayModel model = ModelReader.parseXML(xmlOrText);
            widgets.addAll(model.getChildren());
        }
        catch (Exception ex)
        {
            installWidgetsFromString(event, xmlOrText, selection_tracker, widgets);
        }
    }

    private static void installWidgetsFromString(final DragEvent event, final String text, final SelectedWidgetUITracker selection_tracker, final List<Widget> widgets)
    {
        // Consider each word a separate PV
        final String[] words = text.split("[ \n]+");
        final boolean multiple = words.length > 1;
        final List<WidgetDescriptor> descriptors = getPVWidgetDescriptors();
        final List<String> choices = new ArrayList<>(descriptors.size() + ( multiple ? 1 : 0 ));
        final String format = multiple ? Messages.WT_FromString_multipleFMT : Messages.WT_FromString_singleFMT;

        // Always offer a single LabelWidget for the complete text
        final String defaultChoice = NLS.bind(Messages.WT_FromString_singleFMT, LabelWidget.WIDGET_DESCRIPTOR.getName());
        choices.add(defaultChoice);

        // When multiple words were dropped, offer multiple label widgets
        if (multiple)
            choices.add(NLS.bind(Messages.WT_FromString_multipleFMT, LabelWidget.WIDGET_DESCRIPTOR.getName()));

        choices.addAll(descriptors.stream().map(d -> NLS.bind(format, d.getName())).collect(Collectors.toList()));
        Collections.sort(choices);

        final ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setX(event.getScreenX()-100);
        dialog.setY(event.getScreenY()-200);
        dialog.setTitle(Messages.WT_FromString_dialog_title);
        dialog.setHeaderText(NLS.bind(Messages.WT_FromString_dialog_headerFMT, reduceStrings(words)));
        dialog.setContentText(Messages.WT_FromString_dialog_content);

        final Optional<String> result = dialog.showAndWait();
        if (! result.isPresent())
            return;
        final String choice = result.get();
        if (defaultChoice.equals(choice))
        {
            logger.log(Level.FINE, "Dropped text: created LabelWidget [{0}].", text);
            // Not a valid XML. Instantiate a Label object.
            final LabelWidget widget = (LabelWidget) LabelWidget.WIDGET_DESCRIPTOR.createWidget();
            widget.propText().setValue(text);
            widgets.add(widget);
        }
        else
        {   // Parse choice back into widget descriptor
            final MessageFormat msgf = new MessageFormat(format);
            final String descriptorName;
            try
            {
                descriptorName = msgf.parse(choice)[0].toString();
            }
            catch (Exception ex)
            {
                logger.log(Level.SEVERE, "Cannot parse selected widget type " + choice, ex);
                return;
            }

            WidgetDescriptor descriptor = null;
            if (LabelWidget.WIDGET_DESCRIPTOR.getName().equals(descriptorName))
                descriptor = LabelWidget.WIDGET_DESCRIPTOR;
            else
                descriptor = descriptors.stream().filter(d -> descriptorName.equals(d.getName())).findFirst().orElse(null);

            if (descriptor == null)
            {
                logger.log(Level.SEVERE, "Cannot obtain widget for " + descriptorName);
                return;
            }

            for (String word : words)
            {
                final Widget widget = descriptor.createWidget();
                logger.log(Level.FINE, "Dropped text: created {0} [{1}].", new Object[] { widget.getClass().getSimpleName(), word });
                if (widget instanceof PVWidget)
                    ((PVWidget) widget).propPVName().setValue(word);
                else if (widget instanceof LabelWidget)
                    ((LabelWidget) widget).propText().setValue(word);
                else
                    logger.log(Level.WARNING, "Unexpected widget type [{0}].", widget.getClass().getSimpleName());

                final int index = widgets.size();
                if (index > 0)
                {   // Place widgets below each other
                    final Widget previous = widgets.get(index - 1);
                    int x = previous.propX().getValue();
                    int y = previous.propY().getValue() + previous.propHeight().getValue();
                    // Align (if enabled)
                    final Point2D pos = selection_tracker.gridConstrain(x, y);
                    widget.propX().setValue((int)pos.getX());
                    widget.propY().setValue((int)pos.getY());
                }

                widgets.add(widget);
            }
        }
    }

    /** @return Widgets that have a PV */
    private static List<WidgetDescriptor> getPVWidgetDescriptors()
    {
        if (pvWidgetDescriptors == null)
            pvWidgetDescriptors = WidgetFactory.getInstance().getWidgetDescriptions()
                                               .stream()
                                               .filter(d -> d.createWidget() instanceof PVWidget)
                                               .collect(Collectors.toList());
        return pvWidgetDescriptors;
    }

    private static void installWidgetsFromURL(final DragEvent event, final List<Widget> widgets, final List<Runnable> updates)
    {
        final String choice;
        final Dragboard db = event.getDragboard();
        String url = db.getUrl();

        // Fix URL, which on linux can contain the file name twice:
        // "http://some/path/to/file.xyz\nfile.xyz"
        int sep = url.indexOf('\n');
        if (sep > 0)
            url = url.substring(0, sep);

        // Check URL's extension
        sep = url.lastIndexOf('.');
        final String ext = sep > 0  ?  url.substring(1 + sep).toUpperCase() : null;
        if (EMBEDDED_FILE_EXTENSIONS.contains(ext))
            choice = EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.getName();
        else if (IMAGE_FILE_EXTENSIONS.contains(ext))
            choice = PictureWidget.WIDGET_DESCRIPTOR.getName();
        else
        {   // Prompt user
            final List<String> choices = Arrays.asList(LabelWidget.WIDGET_DESCRIPTOR.getName(),
                                                       EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.getName(),
                                                       PictureWidget.WIDGET_DESCRIPTOR.getName(),
                                                       WebBrowserWidget.WIDGET_DESCRIPTOR.getName());
            final ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(3), choices);
            // Position dialog
            dialog.setX(event.getScreenX());
            dialog.setY(event.getScreenY());

            dialog.setTitle(Messages.WT_FromURL_dialog_title);
            dialog.setHeaderText(NLS.bind(Messages.WT_FromURL_dialog_headerFMT, reduceURL(url)));
            dialog.setContentText(Messages.WT_FromURL_dialog_content);
            final Optional<String> result = dialog.showAndWait();
            if (result.isPresent())
                choice = result.get();
            else
                return;
        }

        if (LabelWidget.WIDGET_DESCRIPTOR.getName().equals(choice))
        {
            logger.log(Level.FINE, "Creating LabelWidget for {0}", url);
            final LabelWidget widget = (LabelWidget) LabelWidget.WIDGET_DESCRIPTOR.createWidget();
            widget.propText().setValue(url);
            widgets.add(widget);
        }
        else if (WebBrowserWidget.WIDGET_DESCRIPTOR.getName().equals(choice))
        {
            logger.log(Level.FINE, "Creating WebBrowserWidget for {0}", url);
            final WebBrowserWidget widget = (WebBrowserWidget) WebBrowserWidget.WIDGET_DESCRIPTOR.createWidget();
            widget.propWidgetURL().setValue(url);
            widgets.add(widget);
        }
        else if (PictureWidget.WIDGET_DESCRIPTOR.getName().equals(choice))
        {
            logger.log(Level.FINE, "Creating PictureWidget for {0}", url);
            final PictureWidget widget = (PictureWidget) PictureWidget.WIDGET_DESCRIPTOR.createWidget();
            widget.propFile().setValue(url);
            widgets.add(widget);
            updates.add(() -> updatePictureWidget(widget));
        }
        else if (EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.getName().equals(choice))
        {
            logger.log(Level.FINE, "Creating EmbeddedDisplayWidget for {0}", url);
            EmbeddedDisplayWidget widget = (EmbeddedDisplayWidget) EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.createWidget();
            widget.propFile().setValue(url);
            widgets.add(widget);
            updates.add(() -> updateEmbeddedWidget(widget));
        }
    }

    /** @param display_file      Display file for which to create an {@link EmbeddedDisplayWidget}.
     *  @param selection_tracker Used to get the grid steps from its model to be used
     *                           in offsetting multiple widgets.
     *  @param widgets           The container of the created widgets.
     *  @param updates           Updates to perform on widgets
     */
    private static void displayFileAcceptor(final String display_file,
                                            final SelectedWidgetUITracker selection_tracker,
                                            final List<Widget> widgets, final List<Runnable> updates)
    {
        logger.log(Level.FINE, "Creating EmbeddedDisplayWidget for {0}", display_file);
        final DisplayModel model = selection_tracker.getModel();
        final EmbeddedDisplayWidget widget = (EmbeddedDisplayWidget) EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.createWidget();
        widget.propFile().setValue(display_file);
        // Offset multiple widgets by grid size
        final int index = widgets.size();
        widget.propX().setValue(model.propGridStepX().getValue() * index);
        widget.propY().setValue(model.propGridStepY().getValue() * index);
        widgets.add(widget);
        updates.add(() -> updateEmbeddedWidget(widget));
    }

    /** Update an embedded widget's name and size from its input
     *  @param widget {@link EmbeddedDisplayWidget}
     */
    private static void updateEmbeddedWidget(final EmbeddedDisplayWidget widget)
    {
        final String display_file = widget.propFile().getValue();
        final String resolved;
        try
        {
            resolved = ModelResourceUtil.resolveResource(widget.getTopDisplayModel(), display_file);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot resolve resource " + display_file, ex);
            return;
        }
        try
        (
            final InputStream bis = ModelResourceUtil.openResourceStream(resolved);
        )
        {
            final ModelReader reader = new ModelReader(bis);
            final DisplayModel embedded_model = reader.readModel();
            final String name = embedded_model.getName();
            if (! name.isEmpty())
              widget.propName().setValue(name);
            widget.propWidth().setValue(embedded_model.propWidth().getValue());
            widget.propHeight().setValue(embedded_model.propHeight().getValue());
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, "Error updating embedded widget", ex);
        }
    }

    private static String reduceString(String text)
    {
        if (text.length() <= 64)
            return text;
        else
            return StringUtils.join(StringUtils.left(text, 32), "...", StringUtils.right(text, 32));
    }

    private static String reduceStrings(String[] lines)
    {
        final int ALLOWED_LINES = 16;   //  Should be a even number.
        List<String> validLines = new ArrayList<>(1 + ALLOWED_LINES);

        if ( lines.length <= ALLOWED_LINES )
            Arrays.asList(lines).stream().forEach(l -> validLines.add(reduceString(l)));
        else
        {
            for ( int i = 0; i < ALLOWED_LINES / 2; i++ )
                validLines.add(reduceString(lines[i]));

            validLines.add("...");

            for ( int i = lines.length - ALLOWED_LINES / 2; i < lines.length; i++ )
                validLines.add(reduceString(lines[i]));
        }

        StringBuilder builder = new StringBuilder();

        validLines.stream().forEach(l -> builder.append(l).append("\n"));
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    /** Return a reduced version of the given {@code url}.
     *
     *  @param url An URL string that, if long, must be reduced
     *             to a shorter version to be displayed.
     *  @return A reduced version of the given {@code url}.
     */
    private static String reduceURL(String url)
    {
        if (url.length() > 64)
        {
            String shortURL = url;
            int leftSlash = 2;
            int rightSlash = 2;

            if (url.contains("://"))
                leftSlash += 2;
            else if (url.startsWith("/"))
                leftSlash += 1;

            if (StringUtils.countMatches(url, '/')  >  (leftSlash + rightSlash))
            {
                shortURL = reduceURL(url, leftSlash, rightSlash);
                if (shortURL.length() <= 80)
                    return shortURL;
            }

            if (shortURL.length() > 64)
            {
                leftSlash--;
                rightSlash--;

                if (StringUtils.countMatches(url, '/')  >  (leftSlash + rightSlash))
                {
                    shortURL = reduceURL(url, leftSlash, rightSlash);

                    if (shortURL.length() <= 80)
                        return shortURL;
                }
            }

            if (shortURL.length() > 64)
                return StringUtils.join(StringUtils.left(url, 32), "...", StringUtils.right(url, 32));
        }

        return url;

    }

    private static String reduceURL(String url, int leftSlash, int rightSlash)
    {
        int leftSlashIndex = StringUtils.ordinalIndexOf(url, "/", leftSlash);
        int rightSlashIndex = StringUtils.ordinalIndexOf(StringUtils.reverse(url), "/", rightSlash);

        return StringUtils.join(StringUtils.left(url, leftSlashIndex + 1), "...", StringUtils.right(url, rightSlashIndex + 1));
    }
}
