/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.bobwidget;

import org.csstudio.display.builder.model.widgets.VisibleWidget;

/** Model for persisting data browser widget configuration.
 *
 *  For the OPI, it holds the Data Browser config file name.
 *  For the Data Browser, it holds the {@link DataBrowserModel}.
 *
 *  @author Jaka Bobnar - Original selection value PV support
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DataBrowserWidgetModel extends VisibleWidget
{

    public DataBrowserWidgetModel(String type)
    {
        super(type);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String toString()
    {
        return "DataBrowserWidgetModel";
    }
}
