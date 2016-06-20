package org.csstudio.display.builder.runtime.scriptUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.RuntimePV;

/**
 * Script Utilities
 * 
 * Contains static utility methods to be used in scripts. This class contains
 * utilities for the following tasks: logging, obtaining PVs from widgets, and
 * showing modal dialogs.
 * 
 * @author Amanda Carpenter
 *
 */
@SuppressWarnings("nls")
public class ScriptUtil
{
    // ================
    // logging utils
    /**
     * Log a message at the given {@link Level}.
     * 
     * @param level
     * @param message
     */
    public static void log(Level level, String message)
    {
        Logger.getLogger("ScriptUtil").log(level, message);
    }

    /**
     * Log a message at Information ({@link Level.INFO}) level.
     * 
     * @param message
     */
    public static void logInfo(String message)
    {
        Logger.getLogger("ScriptUtil").info(message);
    }

    /**
     * Log a message at Warning ({@link Level.WARNING}) level.
     * 
     * @param message
     */
    public static void logWarning(String msg)
    {
        Logger.getLogger("ScriptUtil").warning(msg);
    }

    /**
     * Log a message at Severe ({@link Level.SEVERE}) level.
     * 
     * @param message
     */
    public static void logSevere(String msg)
    {
        Logger.getLogger("ScriptUtil").severe(msg);
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
        Logger.getLogger("ScriptUtil").warning("Script Util: Could not find pv by name '" + name + "' for widget '"
                + widget.getName() + "' (" + widget.getType() + ").");
        return null;
    }

    // ====================
    // public alert dialog utils
    /**
     * Show an information-style dialog with a message.
     * 
     * @param message
     *            Message for information dialog
     * @param widget
     *            Widget passed to obtain toolkit for representing dialog
     */
    public static void infoDialog(String message, Widget widget)
    {
        try
        {
            ToolkitRepresentation.getToolkit(widget.getDisplayModel()).showDialog(false, message);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Show a warning-style dialog with a message.
     * 
     * @param message
     *            Message for warning dialog
     * @param widget
     *            Widget passed to obtain toolkit for representing dialog
     */
    public static void warningDialog(String message, Widget widget)
    {
        try
        {
            ToolkitRepresentation.getToolkit(widget.getDisplayModel()).showDialog(true, message);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}