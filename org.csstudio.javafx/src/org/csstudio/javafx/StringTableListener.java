/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.javafx;

/** Listener to a {@link StringTable}
 *
 *  @author Kay Kasemir
 */
public interface StringTableListener
{
    /** Invoked when the data of the table changes
     *
     *  <p>May be the result of user editing a cell,
     *  adding a column etc.
     *
     *  @param table Table that changed
     */
    public void dataChanged(StringTable table);
}
