/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.util;

import java.io.InputStream;

/** Helper for handling workspace files
 *
 *  <p>Allow model code to handle workspace files when running
 *  in RCP.
 *
 *  @author Kay Kasemir
 */
public interface WorkspaceResourceHelper
{
    /** Fragment for model plugin provides this class as implementation. */
    final static String IMPL = "org.csstudio.display.builder.model.util.WorkspaceResourceHelperImpl";

    /** @param resource_name Name of resource in workspace
     *  @return <code>true</code> if found in workspace
     */
    public boolean isWorkspaceResource(String resource_name);

    /** @param resource_name Name of resource in workspace
     *  @return Path in local file system or <code>null</code>
     */
    public String getLocalPath(String resource_name);

    /** @param resource_name Name of resource in workspace
     *  @return Stream for the resource, or <code>null</code> if there is no such resource
     *  @throws Exception on error opening a resource that was found in the workspace
     */
    public InputStream openWorkspaceResource(String resource_name) throws Exception;

}
