/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import static org.csstudio.display.builder.model.ModelPlugin.logger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.AccessDeniedException;
import java.util.logging.Level;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;

/** Helper for handling workspace files
*
*  <p>Installed in ModelResourceUtil
*
*  @author Kay Kasemir
*/
@SuppressWarnings("nls")
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
    public String getLocalPath(final String resource_name)
    {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        try
        {   // The file itself must not exist, because caller
            // may be about to create it.
            // But the parent container should exist,
            // otherwise this is unlikely to be a valid resource name
            // within the workspace.
            final IFile file = root.getFile(new Path(resource_name));
            if (file.getParent().exists())
                return file.getLocation().toOSString();
        }
        catch (Exception ex)
        {
            // NOP, file does not exist
        }
        return null;
    }

    @Override
    public String getWorkspacePath(final String local_name)
    {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IFile file[] = root.findFilesForLocationURI(URIUtil.toURI(local_name));
        if (file != null  &&  file.length > 0  &&  file[0].exists())
            return file[0].getFullPath().toOSString();
        return null;

    }

    @Override
    public InputStream openWorkspaceResource(final String resource_name) throws Exception
    {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IFile file = root.getFile(new Path(resource_name));

        if ( !file.exists() ) {
            throw new FileNotFoundException(file.getRawLocationURI().toString());
        } else if ( !file.isAccessible() ) {
            throw new AccessDeniedException(file.getRawLocationURI().toString());
        } else {

            if ( file.isReadOnly() ) {
                logger.log(Level.WARNING, "Current user lacks write permission to file " + file.getRawLocationURI().toString());
            }

            return file.getContents(true);

        }
    }

    @Override
    public OutputStream writeWorkspaceResource(final String resource_name) throws Exception
    {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IFile file = root.getFile(new Path(resource_name));

        // IFile API requires an InputStream for the content.
        // That content, however, doesn't exist at this time, because
        // it's about to be written to an OutputStream by the caller
        // of this function.
        // -> Provide pipe, with background job to read from pipe and write the file
        final PipedOutputStream buf = new PipedOutputStream();
        final PipedInputStream input = new PipedInputStream(buf);
        final IJobFunction writer = monitor ->
        {
            try
            {
                if (file.exists())
                    file.setContents(input, true, false, monitor);
                else
                    file.create(input, true, monitor);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Cannot write to " + resource_name, ex);
            }
            return Status.OK_STATUS;
        };

        Job.create("Workspace Writer", writer).schedule();

        // Provide caller with output end of pipe to fill
        return buf;
    }
}
