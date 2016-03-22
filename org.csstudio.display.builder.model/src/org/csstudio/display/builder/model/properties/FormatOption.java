/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

/** Options for formatting a value
 *  @author Kay Kasemir
 */
public enum FormatOption
{
    /** Use default settings from PV */
    DEFAULT("Default"),

    /** Use decimal representation, precision determines number of decimals */
    DECIMAL("Decimal"),

    /** Use exponential representation, precision determines number of decimals */
    EXPONENTIAL("Exponential"),

    /** Use exponential representation where exponent is multiple of 3, precision determines number of decimals */
    ENGINEERING("Engineering"),

    /** Use hexadecimal representation, precision determines number of hex digits. 8 for 32 bits */
    HEX("Hexadecimal"),

    /** Decimal for values in 0.0001 <= |value| <= 10000, else exponential, precision determines number of of decimals */
    COMPACT("Compact"),

    /** Force string, most important for array-of-bytes */
    STRING("String");

    private final String label;

    private FormatOption(final String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
