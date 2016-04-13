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

import java.util.concurrent.CountDownLatch;

import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.display.builder.runtime.pv.RuntimePVListener;
import org.csstudio.vtype.pv.PVPool;
import org.csstudio.vtype.pv.local.LocalPVFactory;
import org.diirt.vtype.VType;
import org.junit.BeforeClass;
import org.junit.Test;

/** JUnit demo of the {@link PVFactory}
 *  @author Kay Kasemir
 */
public class PVFactoryTest
{
    @BeforeClass
    public static void setup()
    {
        PVPool.addPVFactory(new LocalPVFactory());
    }

    @Test
    public void testPVFactory() throws Exception
    {
        final RuntimePV pv = PVFactory.getPV("loc://test(3.14)");
        try
        {
            assertThat(pv.getName(), equalTo("loc://test(3.14)"));

            final CountDownLatch updates = new CountDownLatch(1);
            RuntimePVListener listener = new RuntimePVListener()
            {
                @Override
                public void valueChanged(RuntimePV pv, VType value)
                {
                    System.out.println(pv.getName() + " = " + value);
                    updates.countDown();
                }
            };
            pv.addListener(listener);
            updates.await();
        }
        finally
        {
            PVFactory.releasePV(pv);
        }
    }
}
