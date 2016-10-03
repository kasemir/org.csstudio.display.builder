/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.persist.ModelLoader;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget.Resize;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.LabelWidget;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Scale;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EmbeddedDisplayRepresentation extends RegionBaseRepresentation<ScrollPane, EmbeddedDisplayWidget>
{
    /** Timeout used to await UI thread operations to prevent deadlock */
    private static final long TIMEOUT_MS = 5000;

    private final DirtyFlag dirty_sizes = new DirtyFlag();

    /** Inner group that holds child widgets */
    private Group inner;
    private Scale zoom;
    private ScrollPane scroll;

    /** Track active model in a thread-safe way
     *  to assert that each one is repesented and removed
     */
    private final AtomicReference<DisplayModel> active_content_model = new AtomicReference<>();


    @Override
    public ScrollPane createJFXNode() throws Exception
    {
        // inner.setScaleX() and setScaleY() zoom from the center
        // and not the top-left edge, requiring adjustments to
        // inner.setTranslateX() and ..Y() to compensate.
        // Using a separate Scale transformation does not have that problem.
        // See http://stackoverflow.com/questions/10707880/javafx-scale-and-translate-operation-results-in-anomaly
        inner = new Group();
        inner.getTransforms().add(zoom = new Scale());

        scroll = new ScrollPane(inner);
        // Panning tends to 'jerk' the content when clicked
        // scroll.setPannable(true);

        if (toolkit.isEditMode())
        {   // Capture mouse clicks, use them to select the model_widget,
            // instead of passing them through to the embedded model
            // where they would select widgets from the body of this
            // embedded widget
            scroll.addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
            {
                event.consume();
                if (event.isPrimaryButtonDown())
                    toolkit.fireClick(model_widget, event.isControlDown());
            });
        }
        else
        {
            // Hide border around the ScrollPane
            // Details changed w/ JFX versions, see
            // http://stackoverflow.com/questions/17540137/javafx-scrollpane-border-and-background/17540428#17540428
            scroll.setStyle("-fx-background-color:transparent;");
        }

        model_widget.setUserData(EmbeddedDisplayWidget.USER_DATA_EMBEDDED_DISPLAY_CONTAINER, inner);

        return scroll;
    }

    @Override
    protected Parent getChildParent(final Parent parent)
    {
        return inner;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propWidth().addUntypedPropertyListener(this::sizesChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::sizesChanged);
        model_widget.propResize().addUntypedPropertyListener(this::sizesChanged);
        model_widget.runtimePropScale().addUntypedPropertyListener(this::sizesChanged);

        model_widget.propFile().addUntypedPropertyListener(this::fileChanged);
        model_widget.propGroupName().addUntypedPropertyListener(this::fileChanged);
        fileChanged(null, null, null);
    }

    private void sizesChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_sizes.mark();
        toolkit.scheduleUpdate(this);
    }

    private void fileChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        final String file = model_widget.propFile().getValue();
        final String group = model_widget.propGroupName().getValue();
        // Load embedded display in background thread
        toolkit.execute(() -> updateEmbeddedDisplay(file, group));
    }

    /** Load and represent embedded display
     *  @param display_file
     *  @param group_name
     */
    private void updateEmbeddedDisplay(final String display_file, final String group_name)
    {
        try
        {   // Load new model (potentially slow)
            final DisplayModel new_model = loadDisplayModel(display_file, group_name);

            // Atomically update the 'active' model
            final DisplayModel old_model = active_content_model.getAndSet(new_model);

            if (old_model != null)
            {   // Dispose old model
                final Future<Object> completion = toolkit.submit(() ->
                {
                    toolkit.disposeRepresentation(old_model);
                    return null;
                });
                checkCompletion(completion, "timeout disposing old representation");
            }
            // Represent new model on UI thread
            final Future<Object> completion = toolkit.submit(() ->
            {
                representContent(new_model);
                return null;
            });
            checkCompletion(completion, "timeout representing new content");
            model_widget.runtimePropEmbeddedModel().setValue(new_model);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Failed to handle embedded display " + display_file, ex);
        }
    }

    /** Load display model, optionally trimmed to group
     *  @param display_file
     *  @param group_name
     *  @return {@link DisplayModel}
     */
    private DisplayModel loadDisplayModel(final String display_file, final String group_name)
    {
        DisplayModel embedded_model;
        if (display_file.isEmpty())
        {   // Empty model for empty file name
            embedded_model = new DisplayModel();
            model_widget.runtimePropConnected().setValue(true);
        }
        else
        {
            try
            {   // Load model for displayFile, allowing lookup relative to this widget's model
                final DisplayModel display = model_widget.getDisplayModel();
                final String parent_display = display.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
                embedded_model = ModelLoader.loadModel(parent_display, display_file);
                if (!group_name.isEmpty())
                    reduceDisplayModelToGroup(display_file, embedded_model, group_name);
                // Adjust model name to reflect source file
                embedded_model.propName().setValue("EmbeddedDisplay " + display_file);
                model_widget.runtimePropConnected().setValue(true);
            }
            catch (final Throwable ex)
            {   // Log error and show message in pseudo model
                final String message = "Failed to load embedded display '" + display_file + "'";
                logger.log(Level.WARNING, message, ex);
                embedded_model = createErrorModel(message);
                model_widget.runtimePropConnected().setValue(false);
            }

        }
        // Tell embedded model that it is held by this widget
        embedded_model.setUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET, model_widget);
        return embedded_model;
    }

    /** Reduce display model to content of one named group
     *  @param display_file Name of the display file
     *  @param model Model loaded from that file
     *  @param group_name Name of group to use
     */
    private void reduceDisplayModelToGroup(final String display_file, final DisplayModel model, final String group_name)
    {
        final List<Widget> children = model.runtimeChildren().getValue();

        // Remove all but groups with matching name
        int index = 0;
        while (!children.isEmpty() && index < children.size())
        {
            final Widget child = children.get(index);
            if (child.getType().equals(GroupWidget.WIDGET_DESCRIPTOR.getType()) &&
                child.getName().equals(group_name))
                index++;
            else
                model.runtimeChildren().removeChild(child);
        }

        // Expect exactly one
        final int groups = model.runtimeChildren().getValue().size();
        if (children.size() != 1)
        {
            model_widget.propResize().setValue(Resize.None);
            logger.log(Level.WARNING, "Expected one group named '" + group_name + "' in '" + display_file + "', found " + groups);
            return;
        }

        // Replace display with just that one group
        final GroupWidget group = (GroupWidget) children.get(0);
        model.runtimeChildren().removeChild(group);
        int xmin = Integer.MAX_VALUE, ymin = Integer.MAX_VALUE,
            xmax = 0,                 ymax = 0;
        for (Widget child : group.runtimePropChildren().getValue())
        {
            // Not removing child from 'group', since group
            // will be GC'ed anyway.
            model.runtimeChildren().addChild(child);
            xmin = Math.min(xmin, child.propX().getValue());
            ymin = Math.min(ymin, child.propY().getValue());
            xmax = Math.min(xmax, child.propX().getValue() + child.propWidth().getValue());
            ymax = Math.min(ymax, child.propY().getValue() + child.propHeight().getValue());
        }
        // Move all widgets to top-left corner
        for (Widget child : children)
        {
            child.propX().setValue(child.propX().getValue() - xmin);
            child.propY().setValue(child.propY().getValue() - ymin);
        }
        // Shrink display to size of widgets
        model.propWidth().setValue(xmax - xmin);
        model.propHeight().setValue(ymax - ymin);
    }

    /** @param content_model Model to represent */
    private void representContent(final DisplayModel content_model)
    {
        try
        {
            final Resize resize = model_widget.propResize().getValue();
            final int content_width = content_model.propWidth().getValue();
            final int content_height = content_model.propHeight().getValue();
            if (resize == Resize.ResizeContent)
            {
                final double zoom_x = content_width  > 0 ? (double)model_widget.propWidth().getValue()  / content_width : 1.0;
                final double zoom_y = content_height > 0 ? (double)model_widget.propHeight().getValue() / content_height : 1.0;
                final double zoom = Math.min(zoom_x, zoom_y);
                model_widget.runtimePropScale().setValue(zoom);
            }
            else if (resize == Resize.SizeToContent)
            {
                if (content_width > 0)
                    model_widget.propWidth().setValue(content_width);
                if (content_height > 0)
                    model_widget.propHeight().setValue(content_height);
            }
            toolkit.representModel(inner, content_model);
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Failed to represent embedded display", ex);
        }
    }

    /** Wait for future to complete
     *
     *  .. with timeout in case the UI thread cannot execute the submitted task right now.
     *
     *  <p>Intermediate versions of the embedded widget code
     *  experienced a deadlock when the UI was shut down, i.e. UI tried to dispose content,
     *  while at the same time a script was updating the content, also using the UI thread
     *  to create the new representation.
     *  The deadlock resulted from each waiting on each other.
     *  Using a timeout, then moving on without waiting for the submitted UI thread,
     *  would resolve that deadlock.
     *
     *  @param completion
     *  @param message
     *  @throws Exception
     */
    private void checkCompletion(final Future<Object> completion, final String message) throws Exception
    {
        try
        {
            completion.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException timeout)
        {
            logger.log(Level.WARNING, message + " for " + model_widget);
        }
    }

    /** @param message Error message
     *  @return DisplayModel that shows the message
     */
    private DisplayModel createErrorModel(final String message)
    {
        final LabelWidget info = new LabelWidget();
        info.propText().setValue(message);
        info.propForegroundColor().setValue(WidgetColorService.getColor(NamedWidgetColors.ALARM_DISCONNECTED));
        // Size a little smaller than the widget to fill but not require scrollbars
        final int wid = model_widget.propWidth().getValue()-2;
        final int hei = model_widget.propHeight().getValue()-2;
        info.propWidth().setValue(wid);
        info.propHeight().setValue(hei);
        final DisplayModel error_model = new DisplayModel();
        error_model.propWidth().setValue(wid);
        error_model.propHeight().setValue(hei);
        error_model.runtimeChildren().addChild(info);
        return error_model;
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_sizes.checkAndClear())
        {
            final Integer width = model_widget.propWidth().getValue();
            final Integer height = model_widget.propHeight().getValue();
            scroll.setPrefSize(width, height);

            final Resize resize = model_widget.propResize().getValue();
            if (resize == Resize.None)
            {
                zoom.setX(1.0);
                zoom.setY(1.0);
                scroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
                scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
            }
            else if (resize == Resize.ResizeContent)
            {
                final double factor = model_widget.runtimePropScale().getValue();
                zoom.setX(factor);
                zoom.setY(factor);
                scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
            }
            else // SizeToContent
            {
                zoom.setX(1.0);
                zoom.setY(1.0);
                scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
            }
        }
    }
}
