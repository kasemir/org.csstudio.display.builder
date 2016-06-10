/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.logging.Level;

import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.diirt.vtype.VType;

/** Runtime for the ImageWidget
 *
 *  <p>Updates 'Cursor Info PV' with location and value at cursor.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ImageWidgetRuntime  extends WidgetRuntime<ImageWidget>
{
    private volatile RuntimePV cursor_pv = null;

    private final WidgetPropertyListener<VType> cursor_listener = (prop, old, value) ->
    {
        final RuntimePV pv = cursor_pv;
        if (pv == null)
            return;
        try
        {
            pv.write(value);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Error writing " + value + " to " + pv, ex);
        }
    };

    @Override
    public void start() throws Exception
    {
        super.start();

        // Connect cursor info PV
        final String cursor_pv_name = widget.behaviorCursorInfoPV().getValue();
        if (! cursor_pv_name.isEmpty())
        {
            logger.log(Level.FINER, "Connecting {0} to {1}",  new Object[] { widget, cursor_pv_name });
            try
            {
                final RuntimePV pv = PVFactory.getPV(cursor_pv_name);
                addPV(pv);
                widget.runtimeCursorInfo().addPropertyListener(cursor_listener);
                cursor_pv = pv;
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Error connecting PV " + cursor_pv_name, ex);
            }
        }
    }

    @Override
    public void stop()
    {
        // Disconnect cursor info PV
        final RuntimePV pv = cursor_pv;
        cursor_pv = null;
        if (pv != null)
        {
            widget.runtimeCursorInfo().removePropertyListener(cursor_listener);
            removePV(pv);
            PVFactory.releasePV(pv);
        }
        super.stop();
    }
}
