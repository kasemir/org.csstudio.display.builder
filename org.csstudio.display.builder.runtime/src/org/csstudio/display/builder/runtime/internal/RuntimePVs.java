/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVListenerAdapter;
import org.diirt.vtype.VType;

/** Tracker for all PVs used by a widget.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RuntimePVs
{
    // Similar to org.csstudio.opibuilder.editparts.ConnectionHandler by Xihui Chen
    private final Widget widget;

    /** PVs and their connection info
     *
     *  <p>PVs are tracked by PV instance.
     *  A widget may use the same PV name in the primary widget PV
     *  and then again as a script PV.
     *  The PV pool will provide the same PV instance,
     *  and the PVInfo keeps a reference count to only
     *  track the PV once.
     */
    private final ConcurrentMap<PV, PVInfo> pvs = new ConcurrentHashMap<>();

    /** Listener for tracking connection state of individual PV.
     *  Reference counted because widget may use the same PV
     *  multiple times, but connection state is only tracked once.
     */
    private class PVInfo extends PVListenerAdapter
    {
        private final AtomicInteger refs = new AtomicInteger();
        private volatile boolean connected = false;

        public int addReference()
        {
            return refs.incrementAndGet();
        }

        public int removeReference()
        {
            return refs.decrementAndGet();
        }

        public boolean isConnected()
        {
            return connected;
        }

        @Override
        public void valueChanged(final PV pv, final VType value)
        {
            if (connected)
                return;
            connected = true;
            updateConnections(true);
        }

        @Override
        public void disconnected(final PV pv)
        {
            connected = false;
            updateConnections(false);
        }
    }

    /** @param widget PVs of this widget will be tracked */
    public RuntimePVs(final Widget widget)
    {
        this.widget = widget;
    }

    /** @param pv PV to track */
    public void addPV(final PV pv)
    {
        final PVInfo info = pvs.computeIfAbsent(pv, (p) -> new PVInfo());
        if (info.addReference() == 1)
        {
            // Awaiting connections for at least one PV, so widget is for now disconnected
            widget.runtimeConnected().setValue(false);
            pv.addListener(info);
        }
    }

    /** @param pv PV to no longer track */
    public void removePV(final PV pv)
    {
        final PVInfo info = pvs.get(pv);
        if (info == null)
            throw new IllegalStateException("Unknown PV " + pv);
        if (info.removeReference() == 0)
        {
            pvs.remove(pv, info);
            pv.removeListener(info);
        }
    }

    /** Update overall connection state of the widget
     *  @param pv_connected <code>true</code> if one PV just connected, <code>false</code> if one PV disconnected
     */
    private void updateConnections(final boolean pv_connected)
    {
        boolean all_connected = pv_connected;
        if (all_connected)
        {   // Are _all_ PVs connected?
            // If this code turns into a bottleneck, could
            // optimize by simply counting connections +1/-1,
            // but full search of all PVs is less error-prone.
            for (PVInfo info : pvs.values())
                if (! info.isConnected())
                {
                    all_connected = false;
                    break;
                }
        }
        // else: For sure not connected

        widget.runtimeConnected().setValue(all_connected);
    }

    /** @return All PVs of this widget */
    public Collection<PV> getPVs()
    {   // Create safe copy
        return new ArrayList<>(pvs.keySet());
    }

    /** @return Disconnected PVs of this widget */
    public Collection<PV> getDisconnectedPVs()
    {
        final List<PV> disconnected = new ArrayList<>();
        for (Map.Entry<PV, PVInfo> entry : pvs.entrySet())
            if (! entry.getValue().isConnected())
                disconnected.add(entry.getKey());
        return disconnected;
    }
}
