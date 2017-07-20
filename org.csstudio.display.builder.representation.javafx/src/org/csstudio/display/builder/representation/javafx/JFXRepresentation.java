/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.model.widgets.ArcWidget;
import org.csstudio.display.builder.model.widgets.ArrayWidget;
import org.csstudio.display.builder.model.widgets.BoolButtonWidget;
import org.csstudio.display.builder.model.widgets.ByteMonitorWidget;
import org.csstudio.display.builder.model.widgets.CheckBoxWidget;
import org.csstudio.display.builder.model.widgets.ClockWidget;
import org.csstudio.display.builder.model.widgets.ComboWidget;
import org.csstudio.display.builder.model.widgets.DigitalClockWidget;
import org.csstudio.display.builder.model.widgets.EllipseWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.GaugeWidget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.LEDWidget;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.model.widgets.LinearMeterWidget;
import org.csstudio.display.builder.model.widgets.MeterWidget;
import org.csstudio.display.builder.model.widgets.MultiStateLEDWidget;
import org.csstudio.display.builder.model.widgets.NavigationTabsWidget;
import org.csstudio.display.builder.model.widgets.PictureWidget;
import org.csstudio.display.builder.model.widgets.PolygonWidget;
import org.csstudio.display.builder.model.widgets.PolylineWidget;
import org.csstudio.display.builder.model.widgets.ProgressBarWidget;
import org.csstudio.display.builder.model.widgets.RadioWidget;
import org.csstudio.display.builder.model.widgets.RectangleWidget;
import org.csstudio.display.builder.model.widgets.ScaledSliderWidget;
import org.csstudio.display.builder.model.widgets.ScrollBarWidget;
import org.csstudio.display.builder.model.widgets.SpinnerWidget;
import org.csstudio.display.builder.model.widgets.SymbolWidget;
import org.csstudio.display.builder.model.widgets.TableWidget;
import org.csstudio.display.builder.model.widgets.TabsWidget;
import org.csstudio.display.builder.model.widgets.TankWidget;
import org.csstudio.display.builder.model.widgets.TextEntryWidget;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.model.widgets.ThermometerWidget;
import org.csstudio.display.builder.model.widgets.WebBrowserWidget;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.csstudio.display.builder.model.widgets.plots.XYPlotWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentationFactory;
import org.csstudio.display.builder.representation.javafx.widgets.ActionButtonRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ArcRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ArrayRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.BoolButtonRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ByteMonitorRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.CheckBoxRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ClockRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ComboRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.DigitalClockRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.EllipseRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.EmbeddedDisplayRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.GaugeRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.GroupRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.LEDRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.LabelRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.LinearMeterRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.MeterRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.MultiStateLEDRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.NavigationTabsRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.PictureRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.PolygonRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.PolylineRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ProgressBarRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.RadioRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.RectangleRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ScaledSliderRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ScrollBarRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.SpinnerRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.SymbolRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TableRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TabsRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TankRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TextEntryRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TextUpdateRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ThermometerRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.WebBrowserRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.plots.ImageRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.plots.XYPlotRepresentation;
import org.csstudio.javafx.DialogHelper;
import org.csstudio.javafx.Styles;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/** Represent model items in JavaFX toolkit
 *
 *  <p>The parent of each widget is either a {@link Group} or
 *  a {@link Pane}.
 *  Common ancestor of both is {@link Parent}, but note that other
 *  parent types (Region, ..) are not permitted.
 *
 *  <p>General scene layout:
 *  <pre>
 *  model_root
 *   |
 *  scroll_body
 *   |
 *  widget_parent
 *  </pre>
 *
 *  <p>widget_parent:
 *  This is where the widgets of the model get represented.
 *  Its scaling factors are used to zoom.
 *  Also used to set the overall background color.
 *
 *  <p>scroll_body:
 *  Needed for scroll pane to use visual bounds, i.e. be aware of zoom.
 *  Otherwise scroll bars would enable/disable based on layout bounds,
 *  regardless of zoom.
 *  ( https://pixelduke.wordpress.com/2012/09/16/zooming-inside-a-scrollpane )
 *
 *  <p>model_root:
 *  Scroll pane for viewing a subsection of larger display.
 *  This is also the 'root' of the model-related scene.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFXRepresentation extends ToolkitRepresentation<Parent, Node>
{
    /** Adjustment for scroll body size to prevent scroll bars from being displayed */
    // XXX Would be good to understand this value instead of 2-by-trial-and-error
    private static final int SCROLLBAR_ADJUST = 2;

    public static final String ACTIVE_MODEL = "_active_model";

    /** Zoom to fit display */
    public static final double ZOOM_ALL = -1.0;

    /** Zoom to fit display's width */
    public static final double ZOOM_WIDTH = -3.0;

    /** Zoom to fit display's height */
    public static final double ZOOM_HEIGHT = -2.0;

    /** Width of the grid lines. */
    private static final float GRID_LINE_WIDTH = 0.222F;

    /** Update model size indicators (in edit mode) */
    private WidgetPropertyListener<Integer> model_size_listener = ( p, o, n ) -> execute(this::updateModelSizeIndicators);

    /** Update background color, grid */
    private UntypedWidgetPropertyListener background_listener = ( p, o, n ) -> execute(this::updateBackground);

    private Line horiz_bound, vert_bound;
    private Pane widget_parent;
    private Group scroll_body;
    private ScrollPane model_root;

    /** Constructor
     *  @param edit_mode Edit mode?
     */
    public JFXRepresentation(final boolean edit_mode)
    {
        super(edit_mode);
    }

    @Override
    protected void initialize()
    {
        final Map<String, WidgetRepresentationFactory<Parent, Node>> factories = new HashMap<>();
        registerKnownRepresentations(factories);
        final IExtensionRegistry registry = RegistryFactory.getRegistry();
        if (registry != null)
        {   // Load available representations from registry,
            // which allows other plugins to contribute new widgets.
            for (IConfigurationElement config : registry.getConfigurationElementsFor(WidgetRepresentation.EXTENSION_POINT))
            {
                final String type = config.getAttribute("type");
                final String clazz = config.getAttribute("class");
                logger.log(Level.CONFIG, "{0} contributes {1}", new Object[] { config.getContributor().getName(), clazz });
                factories.put(type, createFactory(config));
            }
        }
        for (Map.Entry<String, WidgetRepresentationFactory<Parent, Node>> entry : factories.entrySet())
            register(entry.getKey(), entry.getValue());
    }

    /**
     * Add known representations as fallback in absence of registry information
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private static void registerKnownRepresentations(final Map<String, WidgetRepresentationFactory<Parent, Node>> factories)
    {
        factories.put(ActionButtonWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ActionButtonRepresentation());
        factories.put(ArcWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ArcRepresentation());
        factories.put(ArrayWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ArrayRepresentation());
        factories.put(BoolButtonWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new BoolButtonRepresentation());
        factories.put(ByteMonitorWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ByteMonitorRepresentation());
        factories.put(CheckBoxWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new CheckBoxRepresentation());
        factories.put(ClockWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ClockRepresentation());
        factories.put(ComboWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ComboRepresentation());
        factories.put(DigitalClockWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new DigitalClockRepresentation());
        factories.put(EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new EmbeddedDisplayRepresentation());
        factories.put(EllipseWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new EllipseRepresentation());
        factories.put(GaugeWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new GaugeRepresentation());
        factories.put(GroupWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new GroupRepresentation());
        factories.put(ImageWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ImageRepresentation());
        factories.put(LabelWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new LabelRepresentation());
        factories.put(LEDWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new LEDRepresentation());
        factories.put(LinearMeterWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new LinearMeterRepresentation());
        factories.put(MeterWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new MeterRepresentation());
        factories.put(MultiStateLEDWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new MultiStateLEDRepresentation());
        factories.put(NavigationTabsWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new NavigationTabsRepresentation());
        factories.put(PictureWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new PictureRepresentation());
        factories.put(PolygonWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new PolygonRepresentation());
        factories.put(PolylineWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new PolylineRepresentation());
        factories.put(ProgressBarWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ProgressBarRepresentation());
        factories.put(RadioWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new RadioRepresentation());
        factories.put(RectangleWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new RectangleRepresentation());
        factories.put(ScaledSliderWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ScaledSliderRepresentation());
        factories.put(ScrollBarWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ScrollBarRepresentation());
        factories.put(SpinnerWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new SpinnerRepresentation());
        factories.put(SymbolWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new SymbolRepresentation());
        factories.put(TableWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new TableRepresentation());
        factories.put(TabsWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new TabsRepresentation());
        factories.put(TankWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new TankRepresentation());
        factories.put(TextEntryWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new TextEntryRepresentation());
        factories.put(TextUpdateWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new TextUpdateRepresentation());
        factories.put(ThermometerWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new ThermometerRepresentation());
        factories.put(WebBrowserWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new WebBrowserRepresentation());
        factories.put(XYPlotWidget.WIDGET_DESCRIPTOR.getType(), ( ) -> (WidgetRepresentation) new XYPlotRepresentation());
    }

    @SuppressWarnings("unchecked")
    private WidgetRepresentationFactory<Parent, Node> createFactory(final IConfigurationElement config)
    {
        return () -> (WidgetRepresentation<Parent, Node, Widget>) config.createExecutableExtension("class");
    }

    /** Create scrollpane etc. for hosting the model
     *
     *  @return ScrollPane
     *  @throws IllegalStateException if had already been called
     */
    final public ScrollPane createModelRoot()
    {
        if (model_root != null)
            throw new IllegalStateException("Already created model root");

        widget_parent = new Pane();
        scroll_body = new Group(widget_parent);

        if (isEditMode())
        {
            horiz_bound = new Line();
            horiz_bound.getStyleClass().add("display_model_bounds");
            horiz_bound.setStartX(0);

            vert_bound = new Line();
            vert_bound.getStyleClass().add("display_model_bounds");
            vert_bound.setStartY(0);

            scroll_body.getChildren().addAll(vert_bound, horiz_bound);
        }

        model_root = new ScrollPane(scroll_body);

        final InvalidationListener resized = prop -> handleViewportChanges();
        model_root.widthProperty().addListener(resized);
        model_root.heightProperty().addListener(resized);

        return model_root;
    }

    /** @return Parent node of model widgets */
    final public Parent getModelParent()
    {
        return widget_parent;
    }

    /** @param scene Scene where style sheet for display builder is added */
    public static void setSceneStyle(final Scene scene)
    {
        // Fetch css relative to JFXRepresentation, not derived class
        final String css = JFXRepresentation.class.getResource("opibuilder.css").toExternalForm();
        scene.getStylesheets().add(css);

        Styles.setSceneStyle(scene);
    }

    /** Set zoom level
     *  @param zoom Zoom level: 1.0 for 100%, 0.5 for 50%, ZOOM_ALL, ZOOM_WIDTH, ZOOM_HEIGHT
     *  @return Zoom level actually used
     */
    final public double setZoom(double zoom)
    {
        if (zoom <= 0.0)
        {   // Determine zoom to fit outline of display into available space
            final Bounds available = model_root.getLayoutBounds();
            final Bounds outline = widget_parent.getLayoutBounds();
            final double zoom_x = outline.getWidth()  > 0 ? available.getWidth()  / outline.getWidth() : 1.0;
            final double zoom_y = outline.getHeight() > 0 ? available.getHeight() / outline.getHeight() : 1.0;

            if (zoom == ZOOM_WIDTH)
                zoom = zoom_x;
            else if (zoom == ZOOM_HEIGHT)
                zoom = zoom_y;
            else // Assume ZOOM_ALL
                zoom = Math.min(zoom_x, zoom_y);
        }

        widget_parent.getTransforms().setAll(new Scale(zoom, zoom));
        // Appears similar to using this API:
        //     widget_parent.setScaleX(zoom);
        //     widget_parent.setScaleY(zoom);
        // but when resizing the window,
        // using setScaleX/Y results in sluggish updates,
        // sometimes shifting the content around
        // (top left origin of content no longer in top left corner of window).
        // Setting a Scale() transform does not exhibit that quirk,
        // maybe because both X and Y scaling are set 'at once'?

        return zoom;
    }

    /** Obtain the 'children' of a Toolkit widget parent
     *  @param parent Parent that's either Group or Pane
     *  @return Children
     */
    public static ObservableList<Node> getChildren(final Parent parent)
    {
        if (parent instanceof Group)
            return ((Group)parent).getChildren();
        else if (parent instanceof Pane)
            return ((Pane)parent).getChildren();
        throw new IllegalArgumentException("Expecting Group or Pane, got " + parent);
    }

    @Override
    public ToolkitRepresentation<Parent, Node> openNewWindow(final DisplayModel model, Consumer<DisplayModel> close_handler) throws Exception
    {   // Use JFXStageRepresentation or RCP-based implementation
        throw new IllegalStateException("Not implemented");
    }

    /** Handle changes in on-screen size of this representation */
    private void handleViewportChanges()
    {
        final int view_width = (int) model_root.getWidth();
        final int view_height = (int) model_root.getHeight();

        final int model_width, model_height;
        final DisplayModel copy = model;
        if (copy == null)
            model_width = model_height = 0;
        else
        {
            model_width = copy.propWidth().getValue();
            model_height = copy.propHeight().getValue();
        }

        // If on-screen viewport is larger than model,
        // grow the scroll_body so that the complete area is
        // filled with the background color
        // and - in edit mode - the grid.
        // If the viewport is smaller, use the model's size
        // to get appropriate scrollbars.
        final int show_x = view_width >= model_width
                         ? view_width-SCROLLBAR_ADJUST
                         : model_width;
        final int show_y = view_height >= model_height
                         ? view_height-SCROLLBAR_ADJUST
                         : model_height;

        // Does not consider zooming.
        // If the widget_parent is zoomed 'out', e.g. 50%,
        // the widget_parent will only be half as large
        // as we specify here in pixels
        // -> Ignore. If user zooms out a lot, there'll be an
        //    area a gray area at the right and bottom of the display.
        //    But user tends to zoom out to see the complete set of widgets,
        //    so there is very little gray area.
        //        widget_parent.setMinWidth(show_x / zoom);
        //        widget_parent.setMinHeight(show_y / zoom);
        widget_parent.setMinSize(show_x, show_y);
    }

    /** Update lines that indicate model's size in edit mode */
    private void updateModelSizeIndicators()
    {
        final int width = model.propWidth().getValue();
        final int height = model.propHeight().getValue();

        horiz_bound.setStartY(height);
        horiz_bound.setEndX(width);
        horiz_bound.setEndY(height);

        vert_bound.setStartX(width);
        vert_bound.setEndY(height);
        vert_bound.setEndX(width);
    }

    @Override
    public void representModel(final Parent root, final DisplayModel model) throws Exception
    {
        root.getProperties().put(ACTIVE_MODEL, model);
        super.representModel(root, model);

        // In edit mode, indicate overall bounds of the top-level model
        if (model.isTopDisplayModel())
        {
            // Listen to model background
            model.propBackgroundColor().addUntypedPropertyListener(background_listener);

            if (isEditMode())
            {
                // Track display size w/ initial update
                model.propWidth().addPropertyListener(model_size_listener);
                model.propHeight().addPropertyListener(model_size_listener);
                model_size_listener.propertyChanged(null, null, null);

                // Track grid changes w/ initial update
                model.propGridVisible().addUntypedPropertyListener(background_listener);
                model.propGridColor().addUntypedPropertyListener(background_listener);
                model.propGridStepX().addUntypedPropertyListener(background_listener);
                model.propGridStepY().addUntypedPropertyListener(background_listener);
            }
            background_listener.propertyChanged(null, null, null);
        }
    }

    @Override
    public Parent disposeRepresentation(final DisplayModel model)
    {
        if (model.isTopDisplayModel())
        {
            model.propBackgroundColor().removePropertyListener(background_listener);
            if (isEditMode())
            {
                model.propGridStepY().removePropertyListener(background_listener);
                model.propGridStepX().removePropertyListener(background_listener);
                model.propGridColor().removePropertyListener(background_listener);
                model.propGridVisible().removePropertyListener(background_listener);
                model.propHeight().removePropertyListener(model_size_listener);
                model.propWidth().removePropertyListener(model_size_listener);
            }
        }

        final Parent root = super.disposeRepresentation(model);
        root.getProperties().remove(ACTIVE_MODEL);
        return root;
    }

    @Override
    public void execute(final Runnable command)
    {   // If already on app thread, execute right away
        if (Platform.isFxApplicationThread())
            command.run();
        else
            Platform.runLater(command);
    }

    @Override
    public void showMessageDialog(final Widget widget, final String message)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CountDownLatch done = new CountDownLatch(1);
        execute( ()->
        {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION);
            DialogHelper.positionDialog(alert, node, -100, -50);
            alert.setResizable(true);
            alert.setTitle("Message");
            // "header text" allows for larger content than the "content text"
            alert.setContentText(null);
            alert.setHeaderText(message);
            alert.showAndWait();
            done.countDown();
        });
        try
        {
            done.await();
        }
        catch (InterruptedException ex)
        {
            // Ignore
        }
    }

    @Override
    public void showErrorDialog(final Widget widget, final String error)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CountDownLatch done = new CountDownLatch(1);
        execute( ()->
        {
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            DialogHelper.positionDialog(alert, node, -100, -50);
            alert.setResizable(true);
            alert.setTitle("Error");
            alert.setHeaderText(error);
            alert.showAndWait();
            done.countDown();
        });
        try
        {
            done.await();
        }
        catch (InterruptedException ex)
        {
            // Ignore
        }
    }

    @Override
    public boolean showConfirmationDialog(final Widget widget, final String question)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CompletableFuture<Boolean> done = new CompletableFuture<>();
        execute( ()->
        {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            DialogHelper.positionDialog(alert, node, -100, -50);
            alert.setResizable(true);
            alert.setTitle("Please Confirm");
            alert.setHeaderText(question);
            // Setting "Yes", "No" buttons
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.YES);
            alert.getButtonTypes().add(ButtonType.NO);
            final Optional<ButtonType> result = alert.showAndWait();
            // NOTE that button type OK/YES/APPLY checked in here must match!
            done.complete(result.isPresent()  &&  result.get() == ButtonType.YES);
        });
        try
        {
            return done.get();
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Confirmation dialog ('" + question + "') failed", ex);
        }
        return false;
    }

    @Override
    public String showSelectionDialog(final Widget widget, final String title, final List<String> options)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CompletableFuture<String> done = new CompletableFuture<>();
        execute( ()->
        {
            final ChoiceDialog<String> dialog = new ChoiceDialog<>(null, options);
            DialogHelper.positionDialog(dialog, node, -100, -50);

            dialog.setHeaderText(title);
            final Optional<String> result = dialog.showAndWait();
            done.complete(result.orElse(null));
        });
        try
        {
            return done.get();
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Selection dialog ('" + title + ", ..') failed", ex);
        }
        return null;
    }

    @Override
    public String showPasswordDialog(final Widget widget, final String title, final String correct_password)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CompletableFuture<String> done = new CompletableFuture<>();
        execute( ()->
        {
            final PasswordDialog dialog = new PasswordDialog(title, correct_password);
            DialogHelper.positionDialog(dialog, node, -100, -50);
            final Optional<String> result = dialog.showAndWait();
            done.complete(result.orElse(null));
        });
        try
        {
            return done.get();
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Password dialog ('" + title + ", ..') failed", ex);
        }
        return null;
    }

    @Override
    public String showSaveAsDialog(final Widget widget, final String initial_value)
    {
        final FileChooser dialog = new FileChooser();
        if (initial_value != null)
        {
            final File file = new File(initial_value);
            dialog.setInitialDirectory(file.getParentFile());
            dialog.setInitialFileName(file.getName());
        }
        dialog.getExtensionFilters().addAll(FilenameSupport.file_extensions);
        final Window window = null;
        final File file = dialog.showSaveDialog(window);
        return file == null ? null : file.toString();
    }

    /** Update background, using background color and grid information from model */
    private void updateBackground()
    {
        final WidgetColor background = model.propBackgroundColor().getValue();

        // Setting the "-fx-background:" of the root node propagates
        // to all child nodes in the scene graph.
        //
        //        if (isEditMode())
        //            model_root.setStyle("-fx-background: linear-gradient(from 0px 0px to 10px 10px, reflect, #D2A2A2 48%, #D2A2A2 2%, #D2D2A2 48% #D2D2A2 2%)");
        //        else
        //            model_root.setStyle("-fx-background: " + JFXUtil.webRGB(background));
        //
        // In edit mode, this results in error messages because the linear-gradient doesn't "work" for all nodes:
        //
        // javafx.scene.CssStyleHelper (calculateValue)
        // Caught java.lang.ClassCastException: javafx.scene.paint.LinearGradient cannot be cast to javafx.scene.paint.Color
        // while converting value for
        // '-fx-background-color' from rule '*.text-input' in stylesheet ..jfxrt.jar!/com/sun/javafx/scene/control/skin/modena/modena.bss
        // '-fx-effect' from rule '*.scroll-bar:vertical>*.increment-button>*.increment-arrow' in StyleSheet ...  jfxrt.jar!/com/sun/javafx/scene/control/skin/modena/modena.bss
        // '-fx-effect' from rule '*.scroll-bar:vertical>*.decrement-button>*.decrement-arrow' in stylesheet ... modena.bss
        // '-fx-effect' from rule '*.scroll-bar:horizontal>*.increment-button>*.increment-arrow' in stylesheet ... modena.bss
        //
        // In the runtime, the background color style is applied to for example the TextEntryRepresentation,
        // overriding its jfx_node.setBackground(..) setting.

        // Setting just the scroll body background to a plain color or grid image provides basic color control.
        // In edit mode, the horiz_bound, vert_bound lines and grid provide sufficient
        // visual indication of the display size.

        final Color backgroundColor = new Color(background.getRed(), background.getGreen(), background.getBlue());

        final boolean gridVisible = isEditMode() ? model.propGridVisible().getValue() : false;
        final int gridStepX = model.propGridStepX().getValue(),
                  gridStepY = model.propGridStepY().getValue();
        final WidgetColor grid_rgb = model.propGridColor().getValue();
        final Color gridColor = new Color(grid_rgb.getRed(), grid_rgb.getGreen(), grid_rgb.getBlue());

        final BufferedImage image = new BufferedImage(gridStepX, gridStepY, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setBackground(backgroundColor);
        g2d.clearRect(0, 0, gridStepX, gridStepY);

        if (gridVisible)
        {
            g2d.setColor(gridColor);
            g2d.setStroke(new BasicStroke(GRID_LINE_WIDTH));
            g2d.drawLine(0, 0, gridStepX, 0);
            g2d.drawLine(0, 0, 0, gridStepY);
        }

        final WritableImage wimage = new WritableImage(gridStepX, gridStepY);
        SwingFXUtils.toFXImage(image, wimage);
        final ImagePattern pattern = new ImagePattern(wimage, 0, 0, gridStepX, gridStepY, false);
        widget_parent.setBackground(new Background(new BackgroundFill(pattern, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    // Future for controlling the audio player
    private class AudioFuture implements Future<Boolean>
    {
        private volatile MediaPlayer player;

        AudioFuture(final MediaPlayer player)
        {
            this.player = player;
            // Player by default just stays in "PLAYING" state
            player.setOnEndOfMedia(() -> player.stop());
            player.play();
            logger.log(Level.INFO, "Playing " + this);
        }

        @Override
        public boolean isDone()
        {
            switch (player.getStatus())
            {
            case UNKNOWN:
            case READY:
            case PLAYING:
            case PAUSED:
            case STALLED:
                return false;
            case HALTED:
            case STOPPED:
            case DISPOSED:
            default:
                return true;
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            logger.log(Level.INFO, "Stopping " + this);
            final boolean stopped = !isDone();

            // TODO On Linux, playback of WAV doesn't work. Just stays in PLAYING state.
            // Worse: player.stop() as well as player.dispose() hang
            execute(() ->
            {
                player.stop();
            });

            return stopped;
        }

        @Override
        public boolean isCancelled()
        {
            return player.getStatus() == Status.STOPPED;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException
        {
            while (! isDone())
            {
                logger.log(Level.FINE, "Awaiting end " + this);
                Thread.sleep(100);
            }
            return !isCancelled();
        }

        @Override
        public Boolean get(final long timeout, final TimeUnit unit)
                throws InterruptedException, ExecutionException,
                TimeoutException
        {
            final long end = System.currentTimeMillis() + unit.toMillis(timeout);
            while (! isDone())
            {
                logger.log(Level.FINE, "Awaiting end " + this);
                Thread.sleep(100);
                if (System.currentTimeMillis() >= end)
                    throw new TimeoutException("Timeout for " + this);
            }
            return !isCancelled();
        }

        @Override
        protected void finalize() throws Throwable
        {
            logger.log(Level.INFO, "Disposing " + this);
            player.dispose();
            player = null;
        }

        @Override
        public String toString()
        {
            final MediaPlayer copy = player;
            if (copy == null)
                return "Disposed audio player";
            return "Audio player for " + player.getMedia().getSource() + " (" + player.getStatus() + ")";
        }
    }

    @Override
    public Future<Boolean> playAudio(final String url)
    {
        final CompletableFuture<AudioFuture> result = new CompletableFuture<>();
        // Create on UI thread
        execute(() ->
        {
            try
            {
                final Media sound = new Media(url);
                final MediaPlayer player = new MediaPlayer(sound);
                result.complete(new AudioFuture(player));
            }
            catch (Exception ex)
            {
                result.completeExceptionally(ex);
            }
        });

        try
        {
            return result.get();
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Audio playback error for " + url, ex);
        }
        return CompletableFuture.completedFuture(false);
    }
}
