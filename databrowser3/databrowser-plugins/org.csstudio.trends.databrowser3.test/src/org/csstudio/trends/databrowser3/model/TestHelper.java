/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.model;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.archive.vtype.TimestampHelper;
import org.csstudio.vtype.pv.PVPool;
import org.csstudio.vtype.pv.jca.JCA_PVFactory;
import org.csstudio.vtype.pv.local.LocalPVFactory;
import org.csstudio.vtype.pv.sim.SimPVFactory;
//import org.epics.pvmanager.sim.SimulationDataSource;
import org.diirt.util.array.ArrayDouble;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueFactory;

/** Unit-test helper for creating samples
 *  @author Kay Kasemir
 *  @author Takashi Nakamoto added makeWaveform() method
 */
@SuppressWarnings("nls")
public class TestHelper
{
    public static void setup()
    {
        PVPool.addPVFactory(new JCA_PVFactory());
        PVPool.addPVFactory(new LocalPVFactory());
        PVPool.addPVFactory(new SimPVFactory());
        PVPool.setDefaultType(LocalPVFactory.TYPE);

        // Logging
        final Level level = Level.FINE;
        Logger logger = Logger.getLogger("");
        logger.setLevel(level);
        for (Handler handler : logger.getHandlers())
            handler.setLevel(level);
    }

    /** @param i Numeric value as well as pseudo-timestamp
     *  @return Sample that has value and time based on input parameter
     */
    public static VType makeValue(final int i)
    {
        return ValueFactory.newVDouble(Double.valueOf(i), ValueFactory.newTime(TimestampHelper.fromMillisecs(i)));
    }

    /**@param ts timestamp
     * @param vals array
     * @return Sample that has waveform and time based on input parameter
     */
    public static VType makeWaveform(final int ts, final double array[])
    {
        return ValueFactory.newVDoubleArray(new ArrayDouble(array),
                ValueFactory.alarmNone(),
                ValueFactory.timeNow(),
                ValueFactory.displayNone());
    }

    /** @param i Pseudo-timestamp
     *  @return Sample that has error text with time based on input parameter
     */
    public static VType makeError(final int i, final String error)
    {
        return ValueFactory.newVDouble(Double.NaN,
                ValueFactory.newAlarm(AlarmSeverity.UNDEFINED, error),
                ValueFactory.newTime(TimestampHelper.fromMillisecs(i)),
                ValueFactory.displayNone());
    }


    /** @param i Numeric value as well as pseudo-timestamp
     *  @return IValue sample that has value and time based on input parameter
     */
    public static PlotSample makePlotSample(int i)
    {
        return new PlotSample("Test", makeValue(i));
    }

    /** Create array of samples
     *  @param start First value/time stamp
     *  @param end   Last value/time stamp (exclusive)
     */
    public static PlotSample[] makePlotSamples(final int start, final int end)
    {
        int N = end - start;
        final PlotSample result[] = new PlotSample[N];
        for (int i=0; i<N; ++i)
            result[i] = makePlotSample(start + i);
        return result;
    }
}


