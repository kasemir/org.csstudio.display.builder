/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.pv;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VEnumArray;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VStringArray;
import org.diirt.vtype.VType;

/** Dispatches elements of an array PV to per-element local PVs
 *
 *  <p>Intended use is for the array widget:
 *  Elements of the original array value are sent to separate PVs,
 *  one per array element.
 *  Changing one of the per-element PVs will update the original
 *  array PV.
 *
 *  <p>Treats scalar input PVs as one-element array.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ArrayPVDispatcher implements AutoCloseable
{
    /** Listener interface of the {@link ArrayPVDispatcher} */
    @FunctionalInterface
    public static interface Listener
    {
        /** Notification of new/updated per-element PVs.
         *
         *  <p>Sent on initial connection to the array PV,
         *  and also when the array PV changes its size,
         *  i.e. adds or removes elements.
         *
         *  @param element_pvs One scalar PV for each element of the array
         */
        public void arrayChanged(List<RuntimePV> element_pvs);
    }

    private final RuntimePV array_pv;

    private final String basename;

    private final Listener listener;

    private final RuntimePVListener array_listener = new RuntimePVListener()
    {
        @Override
        public void valueChanged(final RuntimePV pv, final VType value)
        {
            try
            {
                dispatchArrayUpdate(value);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot handle array update from " + pv.getName(), ex);
            }
        }

        @Override
        public void disconnected(final RuntimePV pv)
        {
            notifyOfDisconnect();
        }
    };

    private final AtomicReference<List<RuntimePV>> element_pvs = new AtomicReference<>(Collections.emptyList());

    /** Construct dispatcher
     *
     *  @param array_pv PV that will be dispatched into per-element PVs
     *  @param basename Base name used to create per-element PVs.
     *  @see #close()
     */
    public ArrayPVDispatcher(final RuntimePV array_pv, final String basename,
                             final Listener listener)
    {
        this.array_pv = array_pv;
        this.basename = basename;
        this.listener = listener;

        array_pv.addListener(array_listener);
    }

    /** @param value Value update from array */
    private void dispatchArrayUpdate(final VType value) throws Exception
    {
        System.out.println("ArrayPVDispatcher received " + value);

        if (value == null)
            notifyOfDisconnect();
        else
        {
            if (value instanceof VNumberArray)
                dispatchArrayUpdate(((VNumberArray)value).getData());
            else if (value instanceof VEnumArray)
                dispatchArrayUpdate(((VEnumArray)value).getIndexes());
            else if (value instanceof VStringArray)
                dispatchArrayUpdate(((VStringArray)value).getData());
            // TODO Dispatch scalar PVs as one-element arrays
            else
                throw new Exception("Cannot handle " + value);
        }
    }

    private void notifyOfDisconnect()
    {
        for (RuntimePV pv : element_pvs.get())
        {
            try
            {
                pv.write(null);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot notify element PV " + pv.getName() + " of disconnect", ex);
            }
        }
    }

    /** @param value Value update from array of numbers or enum indices */
    private void dispatchArrayUpdate(final ListNumber value) throws Exception
    {
        List<RuntimePV> pvs = element_pvs.get();
        final int N = value.size();
        if (pvs.size() != N)
        {   // Create new element PVs
            pvs = new ArrayList<>(N);
            for (int i=0; i<N; ++i)
            {
                final double val = value.getDouble(i);
                final String name = "loc://" + basename + i;
                final RuntimePV pv = PVFactory.getPV(name);
                pv.write(val);
                pvs.add(pv);
            }
            updateElementPVs(pvs);
        }
        else
        {   // Update existing element PVs
            for (int i=0; i<N; ++i)
                pvs.get(i).write(value.getDouble(i));
        }
    }

    /** @param value Value update from array of strings */
    private void dispatchArrayUpdate(final List<String> value) throws Exception
    {
        throw new Exception("Later"); // TODO
    }

    /** Update per-element PVs.
     *
     *  <p>Disposes old PVs.
     *
     *  <p>Notifies listeners except for special <code>null</code>
     *  parameter used on close
     *
     *  @param new_pvs New per-element PVs
     */
    private void updateElementPVs(final List<RuntimePV> new_pvs)
    {
        final List<RuntimePV> old = element_pvs.getAndSet(new_pvs);
        for (RuntimePV pv : old)
            PVFactory.releasePV(pv);
        if (new_pvs != null)
            listener.arrayChanged(new_pvs);
    }

    /** Must be called when dispatcher is no longer needed.
     *
     *  <p>Releases the per-element PVs
     */
    @Override
    public void close()
    {
        array_pv.removeListener(array_listener);
        updateElementPVs(null);
    }
}
