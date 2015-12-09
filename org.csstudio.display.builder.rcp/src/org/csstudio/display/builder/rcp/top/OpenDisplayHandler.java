package org.csstudio.display.builder.rcp.top;

import org.eclipse.e4.core.di.annotations.Execute;

// E4 Handler to open a display
public class OpenDisplayHandler
{
	@Execute
	public void execute(/* @Named("display") String display */)
	{
	    System.out.println("Should open some display");
	}
}