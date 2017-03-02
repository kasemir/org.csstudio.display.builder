package org.csstudio.display.builder.representation;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.persist.ModelLoader;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget.Resize;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.LabelWidget;

/** Helper for common (SWT, JFX) representation code of {@link EmbeddedDisplayWidget}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EmbeddedDisplayRepresentationUtil
{
    /** Timeout used to await UI thread operations to prevent deadlock */
    private static final long TIMEOUT_MS = 5000;

    /** Display file name and optional group within that display */
    public static class DisplayAndGroup
    {
        private final String display_file, group_name;

        public DisplayAndGroup(final EmbeddedDisplayWidget widget)
        {
            display_file = widget.propFile().getValue();
            group_name = widget.propGroupName().getValue();
        }

        public String getDisplayFile()
        {
            return display_file;
        }

        public String getGroupName()
        {
            return group_name;
        }

        @Override
        public String toString()
        {
            if (group_name.isEmpty())
                return display_file;
            return display_file + " (Group " + group_name + ")";
        }
    }

    /** Load display model, optionally trimmed to group
     *  @param display_file
     *  @param group_name
     *  @return {@link DisplayModel}
     */
    public static  DisplayModel loadDisplayModel(final EmbeddedDisplayWidget model_widget, final String display_file, final String group_name)
    {
        DisplayModel embedded_model;
        if (display_file.isEmpty())
        {   // Empty model for empty file name
            embedded_model = new DisplayModel();
            embedded_model.setUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET, model_widget);
            model_widget.runtimePropConnected().setValue(true);
        }
        else
        {
            try
            {   // Load model for displayFile, allowing lookup relative to this widget's model
                final DisplayModel display = model_widget.getDisplayModel();
                final String parent_display = display.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
                embedded_model = ModelLoader.resolveAndLoadModel(parent_display, display_file);
                // Tell embedded model that it is held by this widget,
                // which provides access to macros of model_widget.
                embedded_model.setUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET, model_widget);
                if (!group_name.isEmpty())
                    reduceDisplayModelToGroup(model_widget, display_file, embedded_model, group_name);
                // Adjust model name to reflect source file
                embedded_model.propName().setValue("EmbeddedDisplay " + display_file);
                model_widget.runtimePropConnected().setValue(true);
            }
            catch (final Throwable ex)
            {   // Log error and show message in pseudo model
                final String message = "Failed to load embedded display '" + display_file + "'";
                logger.log(Level.WARNING, message, ex);
                embedded_model = createErrorModel(model_widget, message);
                model_widget.runtimePropConnected().setValue(false);
            }
        }
        return embedded_model;
    }


    /** Reduce display model to content of one named group
     *  @param display_file Name of the display file
     *  @param model Model loaded from that file
     *  @param group_name Name of group to use
     */
    private static void reduceDisplayModelToGroup(final EmbeddedDisplayWidget model_widget, final String display_file, final DisplayModel model, final String group_name)
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
        for (Widget child : group.runtimeChildren().getValue())
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

    /** @param message Error message
     *  @return DisplayModel that shows the message
     */
    private static DisplayModel createErrorModel(final EmbeddedDisplayWidget model_widget, final String message)
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
        error_model.setUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET, model_widget);
        return error_model;
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
    public static void checkCompletion(final EmbeddedDisplayWidget model_widget, final Future<Object> completion, final String message) throws Exception
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


}
