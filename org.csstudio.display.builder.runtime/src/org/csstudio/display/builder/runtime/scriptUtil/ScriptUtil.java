package org.csstudio.display.builder.runtime.scriptUtil;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.Alert;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.RuntimePV;

@SuppressWarnings("nls")
public class ScriptUtil
{
    //================
    //logging utils
    public static void log(String message)
    {
        Logger.getLogger("ScriptUtil").info(message);
    }
    public static void log(Level level, String msg)
    {
        Logger.getLogger("ScriptUtil").log(level, msg);
    }

    //==================
    //get PV utils
    //these better if methods of widget
    public static RuntimePV getPVByName(Widget widget, String name)
    {
        Collection<RuntimePV> pvs = WidgetRuntime.ofWidget(widget).getPVs();
        for (RuntimePV pv : pvs)
            if (name.equals(pv.getName()))
                return pv;
        Logger.getLogger("ScriptUtil").warning("Script Util: Could not find pv by name '"+name+"' for widget '"+widget.getName()+"' ("+widget.getType()+").");
        return null;
    }
    public static RuntimePV getPV(Widget widget)
    {
        //better if WidgetRuntime could getPrimaryPV()
        try
        {
            String name = widget.getPropertyValue("pv_name");
            return getPVByName(widget, name);
        }
        catch (IllegalArgumentException e)
        {
            Logger.getLogger("ScriptUtil").warning("Script Util: Could not get PV for widget '"+
                    widget.getName()+"' ("+widget.getType()+"): No 'pv_name' property");
            return null;
        }
    }
    
    //====================
    //public alert dialog utils
    public static void informationDialog(String msg)
    {
        informationDialog(msg, null, null);
    }
    public static void informationDialog(String msg, String header)
    {
        informationDialog(msg, header, header);
    }
    public static void informationDialog(String msg, String header, String title)
    {
        submitRunnable(new InfoRunnable(msg, header, title));
    }

    public static void warningDialog(String msg)
    {
        warningDialog(msg, null, null);
    }
    public static void warningDialog(String msg, String header)
    {
        warningDialog(msg, header, header);
    }
    public static void warningDialog(String msg, String header, String title)
    {
        submitRunnable(new WarnRunnable(msg, header, title));
    }
    
    //========================
    //private utilities for alert dialogs
    private static class InfoRunnable implements Runnable
    {
        Alert alert;
        InfoRunnable(String content, String title, String header)
        {
            alert = new Alert(Alert.AlertType.INFORMATION, content);
            alert.setHeaderText(header);
            alert.setTitle(title);
        }

        @Override
        public void run()
        {
            alert.showAndWait().ifPresent( (result)->alert.close() );
        }
    }

    private static class WarnRunnable implements Runnable
    {
        Alert alert;
        WarnRunnable(String content, String title, String header)
        {
            alert = new Alert(Alert.AlertType.WARNING, content);
            alert.setHeaderText(header);
            alert.setTitle(title);
        }

        @Override
        public void run()
        {
            alert.showAndWait().ifPresent( (result)->alert.close() );
        }
    }

    private static void submitRunnable(Runnable object)
    {
        // TODO needs to "submit runnable to JFX UI thread"
    }
}
