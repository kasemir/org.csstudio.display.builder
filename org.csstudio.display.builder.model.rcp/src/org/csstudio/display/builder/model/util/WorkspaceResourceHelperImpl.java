/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/** Helper for handling workspace files
*
*  <p>Installed in ModelResourceUtil
*
*  @author Kay Kasemir
*/
public class WorkspaceResourceHelperImpl implements WorkspaceResourceHelper
{
    @Override
    public boolean isWorkspaceResource(final String resource_name)
    {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        try
        {
            final IFile file = root.getFile(new Path(resource_name));
            return file.exists();
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    @Override
    public String getLocalPath(String resource_name)
    {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        try
        {
            final IFile file = root.getFile(new Path(resource_name));
            if (file.exists())
                return file.getLocation().toOSString();
        }
        catch (Exception ex)
        {
            // NOP, file does not exist
        }
        return null;
    }

    @Override
    public InputStream openWorkspaceResource(final String resource_name) throws Exception
    {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IFile file = root.getFile(new Path(resource_name));
        if (file.exists())
            return file.getContents(true);
        return null;
    }
}
