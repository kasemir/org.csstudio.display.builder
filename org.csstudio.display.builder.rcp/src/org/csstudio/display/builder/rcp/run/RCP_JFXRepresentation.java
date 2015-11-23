/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

/** Represent display builder in JFX inside RCP Views
 *
 *  @author Kay Kasemir
 */
public class RCP_JFXRepresentation // TODO extends JFXRepresentation? extends ToolkitRepresentation<Group, Node>
{
    // TODO Similar to JFXRepresentation, but using RuntimeViewPart as 'Window'
	
	// TODO Since each top-level RCP part has one RCP_JFXRepresentation,
	//      perform the toolkit init. in a static init, not each constructor run.

	// TODO Update ToolkitRepresentation to have
	//     openInitialWindow()
	// as well as
	//     openNewWindow().
	// For standalone SWT, openInitialWindow calls openNewWindow
	// For JFX as well as RCP-hosted toolkit, the initial window is already 'there'.
	
    public void openNewWindow() throws Exception
    {
    	RuntimeViewPart.open();
    }
}
