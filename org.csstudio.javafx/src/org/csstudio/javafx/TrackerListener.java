/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx;

import javafx.geometry.Rectangle2D;

/** Listener to {@link Tracker}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public interface TrackerListener
{
    /** Tracker has changed
     *  @param original Original position
     *  @param current Current position
     */
    default public void trackerChanged(final Rectangle2D original, final Rectangle2D current)
    {
        System.out.println("Moved from " + original + " to " + current);
    }
}
