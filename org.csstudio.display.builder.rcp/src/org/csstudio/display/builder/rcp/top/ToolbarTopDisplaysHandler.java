package org.csstudio.display.builder.rcp.top;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;

/** Handler for "Top Displays" button in tool bar
 *
 *  <p>Invoked when user clicks on the button itself,
 *  without selecting a specific entry of the drop down.
 *  Triggers opening the drop-down list.
 *
 *  (Legacy opibuilder invoked the first display
 *   from the list, which is likely unexpected by the user.
 *   Better offer list for user to choose a display)
 *
 *  @author Kay Kasemir
 */
public class ToolbarTopDisplaysHandler extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        // See https://www.eclipse.org/forums/index.php/t/488692/
        if (event == null  ||
            ! (event.getTrigger() instanceof Event))
            return null;
        final Event trigger = (Event) event.getTrigger();
        if (! (trigger.widget instanceof ToolItem))
            return null;

        // Simulate user pressing the "down" arrow
        final ToolItem item = (ToolItem) trigger.widget;
        final Rectangle bounds = item.getBounds();
        final Event sim = new Event();
        sim.button = 1;
        sim.widget = item;
        sim.detail = SWT.ARROW;
        sim.x = bounds.x;
        sim.y = bounds.y;
        item.notifyListeners(SWT.Selection, sim);
        return null;
    }
}
