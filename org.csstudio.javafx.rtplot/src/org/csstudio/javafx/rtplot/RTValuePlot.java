/*******************************************************************************
 * Copyright (c) 2014-2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot;

/** Real-time plot using numbers on the 'X' axis
 *  @author Kay Kasemir
 */
public class RTValuePlot extends RTPlot<Double>
{
    /** @param parent Parent widget */
    public RTValuePlot()
    {
        super(Double.class);
    }
}
