package org.csstudio.display.builder.runtime.script;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.ChildrenProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
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
    // Model gymnastics

    /** Locate a widget by name
     *
     *  @param widget Widget used to locate the widget model
     *  @param name Name of widget to find
     *  @return Widget or <code>null</code>
     *  @throws Exception
     */
    public static Widget findWidgetByName(final Widget widget, final String name) throws Exception
    {
        final ChildrenProperty siblings = widget.getDisplayModel().runtimeChildren();
        return siblings.getChildByName(name);
    }

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

    /** Show file "Save As" dialog for selecting/entering a new file name
     *
     *  <p>Call blocks until the user closes the dialog
     *  by either either entering/selecting a file name, or pressing "Cancel".
     *
     *  @param widget Widget, used to obtain toolkit for representing dialog
     *  @param initial_value Initial path and file name
     *  @return Path and file name or <code>null</code>
     */
    public static String showSaveAsDialog(final Widget widget, final String initial_value)
    {
        try
        {
            return ToolkitRepresentation.getToolkit(widget.getDisplayModel()).showSaveAsDialog(initial_value);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Error in save-as dialog for " + initial_value, ex);
        }
        return null;
    }

    // ==================
    // get PV utils

    /** Get primary PV of given widget.
     *
     *  <p>This is the PV that a Text Update widget displays,
     *  or the one that a Text Entry widget writes.
     *
     *  <p>Some widgets have no primary PV (Label, Rectangle),
     *  or more then one PV (XY Plot).
     *
     *  @param widget Widget to get PV of.
     *  @return Primary PV of widget; otherwise, if not found, null.
     */
    public static RuntimePV getPrimaryPV(final Widget widget)
    {
        Optional<RuntimePV> pv = WidgetRuntime.ofWidget(widget).getPrimaryPV();
        return pv.orElse(null);
    }

    /** Get all PVs of a widget.
     *
     *  <p>This includes the primary PV of a widget as well as
     *  PVs from scripts and rules assigned to the widget.
     *
     *  @param widget Widget of which to get PVs
     *  @return List of PVs.
     */
    public static Collection<RuntimePV> getPVs(final Widget widget)
    {
        return WidgetRuntime.ofWidget(widget).getPVs();
    }

    /** Get widget's PV by name
     *
     *  <p>Locates widget's PV by name, including the primary PV
     *  as well as PVs from scripts and rules.
     *
     *  @param widget Widget to get PV from
     *  @param name  Name of PV to get
     *  @return PV of given widget with given name; otherwise, if not found,
     *          <code>null</code>
     */
    public static RuntimePV getPVByName(final Widget widget, final String name)
    {
        final Collection<RuntimePV> pvs = getPVs(widget);
        for (RuntimePV pv : pvs)
            if (name.equals(pv.getName()))
                return pv;
        logger.warning("Could not find PV with name '" + name + "' for " + widget);
        return null;
    }


    // ==================
    // Workspace helpers

    /** @param workspace_path Path within workspace
     *  @return Location in local file system or <code>null</code>
     */
    public static String workspacePathToSysPath(final String workspace_path)
    {
        return ModelResourceUtil.getLocalPath(workspace_path);
    }
}