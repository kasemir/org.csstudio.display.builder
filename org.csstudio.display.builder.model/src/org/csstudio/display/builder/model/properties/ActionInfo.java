/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo.Target;
import org.csstudio.display.builder.util.UtilPlugin;

/** Information about an action
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public abstract class ActionInfo
{
    /** Description of a type of action: Name, icon */
    public enum ActionType
    {
        OPEN_DISPLAY("Open Display", "platform:/plugin/org.csstudio.display.builder.model/icons/open_display.png"),
        WRITE_PV("Write PV", "platform:/plugin/org.csstudio.display.builder.model/icons/write_pv.png");

        private final String name, icon_path;

        private ActionType(final String name, final String icon_path)
        {
            this.name = name;
            this.icon_path = icon_path;
        }

        /** @return Stream for icon's content or <code>null</code> */
        public InputStream getIconStream()
        {
            try
            {
                return UtilPlugin.getStream(icon_path);
            }
            catch (Exception ex)
            {
                Logger.getLogger(ActionInfo.class.getName()).log(Level.WARNING, "Cannot obtain icon", ex);
                return null;
            }
        }

        @Override
        public String toString()
        {
            return name;
        }
    };

    /** Create action with generic values
     *  @param type Action type
     *  @return Action of that type
     */
    public static ActionInfo createAction(final ActionType type)
    {
        switch (type)
        {
        case OPEN_DISPLAY:
            return new OpenDisplayActionInfo(type.toString(), "", new Macros(), Target.REPLACE);
        case WRITE_PV:
            return new WritePVActionInfo(type.toString(), "$(pv_name)", "0");
        default:
            throw new IllegalStateException("Unknown type " + type);
        }
    }

    private final String description;

    /** @param description Action description */
    public ActionInfo(final String description)
    {
        this.description = Objects.requireNonNull(description);
    }

    /** @return Type info */
    abstract public ActionType getType();

    /** @return Action description */
    public String getDescription()
    {
        return description;
    }
}
