/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.csstudio.display.builder.runtime.WidgetRuntime;

/** Runtime for the ImageWidget
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ImageWidgetRuntime  extends WidgetRuntime<ImageWidget>
{
    @Override
    public void start() throws Exception
    {
        super.start();

        // TODO: Get the cursor info [x, y, value] from widget and write to PV
        System.out.println("Starting " + widget);
    }


    @Override
    public void stop()
    {
        super.stop();
    }
}
