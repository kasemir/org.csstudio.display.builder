package org.csstudio.display.builder.editor.tracker;

import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.util.GeometryTools;
import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.widgets.GroupWidget;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;

/** Helper for handling GroupWidget membership of widgets
 *
 *  <p>Used to locate group in model,
 *  to highlight group on move-over,
 *  move widgets in and out of groups
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class GroupHandler
{
    public static final int PARALLEL_THRESHOLD = 10;

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final WidgetSelectionHandler selection;

    private final Rectangle group_highlight = new Rectangle();

    /** Group that was found to contain a region */
    private static class GroupSearchResult
    {
        /** Group or null */
        GroupWidget group = null;
        /** Depth in display model hierarchy where group resides */
        int depth = 0;

        void update(final GroupWidget group, final int depth)
        {
            if (group != null  &&  depth >= this.depth)
                this.group = group;
        }

        void update(final GroupSearchResult other)
        {
            update(other.group, other.depth);
        }
    }

    /** Brute-force, parallel search for a GroupWidget that surrounds a given region of the screen. */
    private class SurroundingGroupSearch extends RecursiveTask<GroupSearchResult>
    {
        private static final long serialVersionUID = -5074784016726334794L;
        private final Rectangle2D bounds;
        private final List<Widget> widgets;
        private final int depth;

        /** Create search for a group
         *  @param bounds Region of screen
         *  @param model Display model
         */
        SurroundingGroupSearch(final Rectangle2D bounds, final DisplayModel model)
        {
            this(bounds, model.getChildren(), 1);
        }

        private SurroundingGroupSearch(final Rectangle2D bounds, final List<Widget> widgets, final int depth)
        {
            this.bounds = bounds;
            this.widgets = widgets;
            this.depth = depth;
        }

        @Override
        protected GroupSearchResult compute()
        {
            final int N = widgets.size();
            if (N > PARALLEL_THRESHOLD)
            {
                // System.out.println("Splitting the search");
                final int split = N / 2;
                final SurroundingGroupSearch sub = new SurroundingGroupSearch(bounds, widgets.subList(0, split), depth);
                sub.fork();
                final GroupSearchResult result = findGroup(widgets.subList(split,  N));
                result.update(sub.join());
                return result;
            }
            else
                return findGroup(widgets);
        }

        private GroupSearchResult findGroup(final List<Widget> children)
        {
            // System.out.println("Searching for surrounding group in " + children.size() + " widgets on " + Thread.currentThread().getName());
            final GroupSearchResult result = new GroupSearchResult();
            for (Widget widget : children)
            {
                if (widget instanceof GroupWidget)
                {
                    final GroupWidget found = checkGroup((GroupWidget) widget);
                    if (found != null)
                        result.update(found, depth);
                }
                if (widget instanceof ContainerWidget)
                    result.update(new SurroundingGroupSearch(bounds, ((ContainerWidget)widget).getChildren(), depth + 1).compute());
            }
            return result;
        }

        private GroupWidget checkGroup(final GroupWidget group)
        {
            final Rectangle2D group_bounds = GeometryTools.getDisplayBounds(group);
            if (group_bounds.contains(bounds))
                return group;
            return null;
        }
    };

    public GroupHandler(final Group parent, final WidgetSelectionHandler selection)
    {
        this.selection = selection;
        group_highlight.getStyleClass().add("group_highlight");
        parent.getChildren().add(0, group_highlight);
    }

    /** Locate group for region of display.
     *
     *  <p>If there is a group that contains the region, it is highlighted.
     *  @param x
     *  @param y
     *  @param width
     *  @param height
     */
    public void locateGroup(final double x, final double y, final double width, final double height)
    {
        final Rectangle2D bounds = new Rectangle2D(x, y, width, height);

        GroupWidget group = null;
        try
        {
            final List<Widget> widgets = selection.getSelection();
            if (widgets.size() > 0)
            {
                final DisplayModel model = widgets.get(0).getDisplayModel();
                group = new SurroundingGroupSearch(bounds, model).compute().group;
            }
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Error locating surrounding group", ex);
        }

        if (group == null)
            group_highlight.setVisible(false);
        else
        {
            final Rectangle2D group_bounds = GeometryTools.getDisplayBounds(group);
            group_highlight.setX(group_bounds.getMinX());
            group_highlight.setY(group_bounds.getMinY());
            group_highlight.setWidth(group_bounds.getWidth());
            group_highlight.setHeight(group_bounds.getHeight());
            group_highlight.setVisible(true);
        }
    }

    /** Hide the group highlight (in case it's visible) */
    public void hide()
    {
        group_highlight.setVisible(false);
    }
}
