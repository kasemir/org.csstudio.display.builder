/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.net.URI;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.OpenDisplayAction;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;

/** 'Editor' Launcher for display runtime
 *
 *  <p>Registered as 'editor', allows opening a display runtime
 *  from the Navigator.
 *
 *  @author Kay Kasemir
 */
public class RuntimeLauncher implements IEditorLauncher
{
    @Override
    public void open(final IPath location)
    {
        String path = location.toOSString();

        // If possible, convert to workspace resource
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        URI uri = URIUtil.toURI(location);
        final IFile[] files = root.findFilesForLocationURI(uri);
        if (files != null  &&  files.length > 0)
            path = files[0].getFullPath().toOSString();

        final DisplayInfo info = new DisplayInfo(path, "Navigator File", new Macros());
        new OpenDisplayAction(info).run();
    }
}
