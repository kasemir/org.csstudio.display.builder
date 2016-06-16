/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx.rtplot.internal;

/** Listener to {@link Tracker}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public interface TrackerListener
{
    /** Tracker has changed
     *  @param dx Change in X position
     *  @param dy Change in Y position
     *  @param dw Change in width
     *  @param dh Change in height
     */
    default public void trackerChanged(double dx, double dy, double dw, double dh)
    {
        System.out.println("Moved by " + dx + ", " + dy + ", resized by " + dw + ", " + dh);
    }
}
