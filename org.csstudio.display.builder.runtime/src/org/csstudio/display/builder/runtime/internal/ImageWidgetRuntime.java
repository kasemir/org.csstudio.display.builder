/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget.ROIWidgetProperty;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.display.builder.runtime.pv.RuntimePVListener;
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
    private final List<RuntimePV> roi_pvs = new CopyOnWriteArrayList<>();

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
    private final Map<WidgetProperty<?>, WidgetPropertyListener<?>> roi_prop_listeners = new ConcurrentHashMap<>();
    private final Map<RuntimePV, RuntimePVListener> roi_pv_listeners = new ConcurrentHashMap<>();


    @Override
    public void start() throws Exception
    {
        super.start();

        // Connect cursor info PV
        final String cursor_pv_name = widget.miscCursorInfoPV().getValue();
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

        // Connect ROI PVs
        for (ROIWidgetProperty roi : widget.miscROIs().getValue())
        {
            bindROI(roi.x_pv(), roi.x_value());
            bindROI(roi.y_pv(), roi.y_value());
            bindROI(roi.width_pv(), roi.width_value());
            bindROI(roi.height_pv(), roi.height_value());
        }
    }

    /** Bind an ROI PV to an ROI value
     *  @param name_prop Property for the PV name
     *  @param value_prop Property for the value
     */
    private void bindROI(final WidgetProperty<String> name_prop, final WidgetProperty<VType> value_prop)
    {
        final String pv_name = name_prop.getValue();
        if (pv_name.isEmpty())
            return;

        logger.log(Level.FINER, "Connecting {0} to ROI PV {1}",  new Object[] { widget, pv_name });
        try
        {
            final RuntimePV pv = PVFactory.getPV(pv_name);
            addPV(pv);
            roi_pvs.add(pv);

            // Write value changes to the PV
            final WidgetPropertyListener<VType> prop_listener = (prop, old, value) ->
            {
                try
                {
                    System.out.println("Writing " + value_prop + " to PV " + pv_name);
                    pv.write(value);
                }
                catch (Exception ex)
                {
                    logger.log(Level.WARNING, "Error writing ROI value to PV " + pv_name, ex);
                }
            };
            value_prop.addPropertyListener(prop_listener);
            roi_prop_listeners .put(value_prop, prop_listener);

            // Write PV updates to the value
            final RuntimePVListener pv_listener = new RuntimePVListener()
            {
                @Override
                public void valueChanged(final RuntimePV pv, final VType value)
                {
                    System.out.println("Writing from PV " + pv_name + " to " + value_prop);
                    value_prop.setValue(value);
                }
            };
            pv.addListener(pv_listener);
            roi_pv_listeners.put(pv, pv_listener);

            // TODO Avoid loop
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Error connecting ROI PV " + pv_name, ex);
        }
    }

    @Override
    public void stop()
    {
        // Disconnect ROI PVs and listeners
        for (Map.Entry<WidgetProperty<?>, WidgetPropertyListener<?>> entry : roi_prop_listeners.entrySet())
            entry.getKey().removePropertyListener(entry.getValue());
        roi_prop_listeners.clear();

        for (Map.Entry<RuntimePV, RuntimePVListener> entry : roi_pv_listeners.entrySet())
            entry.getKey().removeListener(entry.getValue());
        roi_pv_listeners.clear();

        for (RuntimePV pv : roi_pvs)
        {
            removePV(pv);
            PVFactory.releasePV(pv);
        }
        roi_pvs.clear();

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
