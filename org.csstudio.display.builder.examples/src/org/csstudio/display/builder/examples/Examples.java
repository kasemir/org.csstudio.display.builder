/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.examples;

import java.net.URL;

import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.examples.SampleSet;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/** Example support for installing the model's examples
 *  @author Kay Kasemir
 */
public class Examples implements SampleSet
{
    @Override
    public URL getDirectoryURL()
    {
        final Bundle bundle = Platform.getBundle(ModelPlugin.ID);
        return bundle.getEntry("/examples");
    }
}
