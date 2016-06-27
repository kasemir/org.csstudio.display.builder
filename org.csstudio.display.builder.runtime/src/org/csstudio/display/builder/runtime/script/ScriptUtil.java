package org.csstudio.display.builder.runtime.script;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.RuntimePV;

/** Script Utilities
 *
 *  <p>Contains static utility methods to be used in scripts:
 *  <ul>
 *  <li>Logging
 *  <li>Dialogs
 *  <li>Obtaining PVs from widgets
 *  </ul>
 *
 * @author Amanda Carpenter
 *
 */
@SuppressWarnings("nls")
public class ScriptUtil
{
    // ================
    // logging utils

    private static final Logger logger = Logger.getLogger("script");

    /** Get logger for scripts.
     *
     *  <p>The logger is a basic java.util.logging.Logger:
     *  <pre>
     *  getLogger().warning("Script has a problem")
     *  getLogger().info("Script is at step 3")
     *  </pre>
     *  @return Logger for scripts
     */
    public static Logger getLogger()
    {
        return logger;
    }

    // ====================
    // public alert dialog utils

    /** Show a message dialog.
     *
     *  <p>Call blocks until the user presses "OK"
     *  in the dialog.
     *
     *  @param widget Widget, used to obtain toolkit for representing dialog
     *  @param is_warning Whether to style dialog as warning or information
     *  @param message Message to display on dialog
     */
    public static void showMessageDialog(final Widget widget, final boolean is_warning, final String message)
    {
        try
        {
            ToolkitRepresentation.getToolkit(widget.getDisplayModel()).showMessageDialog(is_warning, message);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Error showing dialog for " + message, ex);
        }
    }

    /** Show confirmation dialog.
     *
     *  <p>Call blocks until the user closes the dialog
     *  by selecting either "Yes" or "No"
     *  ("Confirm", "Cancel", depending on implementation).
     *
     *  @param widget Widget, used to obtain toolkit for representing dialog
     *  @param mesquestionsage Message to display on dialog
     *  @return <code>true</code> if user selected "Yes" ("Confirm")
     */
    public static boolean showConfirmationDialog(final Widget widget, final String question)
    {
        try
        {
            return ToolkitRepresentation.getToolkit(widget.getDisplayModel()).showConfirmationDialog(question);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Error asking in dialog for " + question, ex);
        }
        return false;
    }


    // ==================
    // get PV utils

    /**
     * Get primary PV of given widget.
     *
     * @param widget
     *            Widget to get PV of.
     * @return Primary PV of widget; otherwise, if not found, null.
     */
    public static RuntimePV getPV(Widget widget)
    {
        Optional<RuntimePV> pv = WidgetRuntime.ofWidget(widget).getPrimaryPV();
        Object value = pv.isPresent() ? pv.get().read() : null;
        return pv.isPresent() ? pv.get() : null;
    }

    /**
     * Get PV by name from widget's collection, including PVs from scripts and
     * rules.
     *
     * @param widget
     *            Widget to get PV from
     * @param name
     *            Name of PV to get
     * @return PV of given widget with given name; otherwise, if not found,
     *         null.
     */
    public static RuntimePV getPVByName(Widget widget, String name)
    {
        Collection<RuntimePV> pvs = WidgetRuntime.ofWidget(widget).getPVs();
        for (RuntimePV pv : pvs)
            if (name.equals(pv.getName()))
                return pv;
        Logger.getLogger("ScriptUtil").warning("Could not find pv by name '" + name + "' for " + widget);
        return null;
    }
}