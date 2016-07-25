/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.runtime.pv.ArrayPVDispatcher;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.display.builder.runtime.pv.RuntimePVListener;
import org.csstudio.display.builder.runtime.pv.ArrayPVDispatcher.Listener;
import org.csstudio.vtype.pv.PVPool;
import org.csstudio.vtype.pv.local.LocalPVFactory;
import org.diirt.vtype.VType;
import org.junit.BeforeClass;
import org.junit.Test;

/** JUnit demo of the {@link ArrayPVDispatcher}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ArrayPVDispatcherTest
{
    @BeforeClass
    public static void setup()
    {
        PVPool.addPVFactory(new LocalPVFactory());
    }

    @Test
    public void testArrayPVDispatcher() throws Exception
    {
        // Array PV. It's elements are to be dispatched into seprate PVs
        final RuntimePV array_pv = PVFactory.getPV("loc://an_array(1.0, 2.2, 3, 4)");

        // The per-element PVs that will be bound to the array
        final AtomicReference<List<RuntimePV>> element_pvs = new AtomicReference<>();

        // Listener for updates of individual element_pvs
        final RuntimePVListener element_listener = new RuntimePVListener()
        {
            @Override
            public void valueChanged(RuntimePV pv, VType value)
            {
                System.out.println(pv.getName() + " changed to " + value);
            }
        };

        final CountDownLatch got_element_pvs = new CountDownLatch(1);

        // Listener to the ArrayPVDispatcher
        final Listener dispatch_listener = new Listener()
        {
            @Override
            public void arrayChanged(final List<RuntimePV> pvs)
            {
                System.out.println("Per-element PVs:");
                for (RuntimePV el : pvs)
                    el.addListener(element_listener);
                element_pvs.set(pvs);
                got_element_pvs.countDown();
            }
        };

        final ArrayPVDispatcher dispatcher = new ArrayPVDispatcher(array_pv, "elementA247FE_", dispatch_listener);

        // Await initial set of per-element PVs
        got_element_pvs.await();
        assertThat(VTypeUtil.getValueNumber(element_pvs.get().get(0).read()).doubleValue(), equalTo(1.0));
        assertThat(VTypeUtil.getValueNumber(element_pvs.get().get(1).read()).doubleValue(), equalTo(2.2));
        assertThat(VTypeUtil.getValueNumber(element_pvs.get().get(2).read()).doubleValue(), equalTo(3.0));
        assertThat(VTypeUtil.getValueNumber(element_pvs.get().get(3).read()).doubleValue(), equalTo(4.0));

        // Change array
        System.out.println("Updating array");
        array_pv.write(new double[] { 1.0, 22.5, 3, 4 } );
        // On one hand, this changed only one array element.
        // On the other hand, it's a new array value with a new time stamp.
        // Unclear if the array dispatcher should detect this and only update
        // the per-element PVs that really have a new value, or all.
        // Currently it updates all, but the test is satisfied with just one update:
        assertThat(VTypeUtil.getValueNumber(element_pvs.get().get(1).read()).doubleValue(), equalTo(22.5));


        // Stop listening to per-element PVs
        for (RuntimePV el : element_pvs.get())
            el.removeListener(element_listener);

        // Close dispatcher
        dispatcher.close();

        // Close the array PV
        PVFactory.releasePV(array_pv);
    }
}
