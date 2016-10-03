/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.representation.EmbeddedDisplayRepresentationUtil.checkCompletion;
import static org.csstudio.display.builder.representation.EmbeddedDisplayRepresentationUtil.loadDisplayModel;
import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.ModelThreadPool;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget.Resize;

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
    private final DirtyFlag dirty_sizes = new DirtyFlag();

    private volatile double zoom_factor = 1.0;

    /** Inner group that holds child widgets */
    private Group inner;
    private Scale zoom;
    private ScrollPane scroll;

    /** Track active model in a thread-safe way
     *  to assert that each one is represented and removed
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

        model_widget.propFile().addUntypedPropertyListener(this::fileChanged);
        model_widget.propGroupName().addUntypedPropertyListener(this::fileChanged);
        fileChanged(null, null, null);
    }

    private volatile boolean resizing = false;

    private void sizesChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        if (resizing)
            return;

        final Resize resize = model_widget.propResize().getValue();
        final DisplayModel content_model = active_content_model.get();
        if (content_model != null)
        {
            final int content_width = content_model.propWidth().getValue();
            final int content_height = content_model.propHeight().getValue();
            if (resize == Resize.ResizeContent)
            {
                final double zoom_x = content_width  > 0 ? (double)model_widget.propWidth().getValue()  / content_width : 1.0;
                final double zoom_y = content_height > 0 ? (double)model_widget.propHeight().getValue() / content_height : 1.0;
                zoom_factor = Math.min(zoom_x, zoom_y);
            }
            else if (resize == Resize.SizeToContent)
            {
                zoom_factor = 1.0;
                resizing = true;
                if (content_width > 0)
                    model_widget.propWidth().setValue(content_width);
                if (content_height > 0)
                    model_widget.propHeight().setValue(content_height);
                resizing = false;
            }
        }

        dirty_sizes.mark();
        toolkit.scheduleUpdate(this);
    }

    private void fileChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        final String file = model_widget.propFile().getValue();
        final String group = model_widget.propGroupName().getValue();
        // Load embedded display in background thread
        ModelThreadPool.getExecutor().execute(() -> updateEmbeddedDisplay(file, group));
    }

    /** Load and represent embedded display
     *  @param display_file
     *  @param group_name
     */
    private void updateEmbeddedDisplay(final String display_file, final String group_name)
    {
        try
        {   // Load new model (potentially slow)
            final DisplayModel new_model = loadDisplayModel(model_widget, display_file, group_name);

            // Atomically update the 'active' model
            final DisplayModel old_model = active_content_model.getAndSet(new_model);

            if (old_model != null)
            {   // Dispose old model
                final Future<Object> completion = toolkit.submit(() ->
                {
                    toolkit.disposeRepresentation(old_model);
                    return null;
                });
                checkCompletion(model_widget, completion, "timeout disposing old representation");
            }
            // Represent new model on UI thread
            final Future<Object> completion = toolkit.submit(() ->
            {
                representContent(new_model);
                return null;
            });
            checkCompletion(model_widget, completion, "timeout representing new content");
            model_widget.runtimePropEmbeddedModel().setValue(new_model);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Failed to handle embedded display " + display_file, ex);
        }
    }

    /** @param content_model Model to represent */
    private void representContent(final DisplayModel content_model)
    {
        try
        {
            sizesChanged(null, null, null);
            toolkit.representModel(inner, content_model);
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Failed to represent embedded display", ex);
        }
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
                zoom.setX(zoom_factor);
                zoom.setY(zoom_factor);
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
