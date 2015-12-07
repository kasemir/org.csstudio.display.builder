package org.csstudio.display.builder.rcp.top;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;

// E4 Handler to open a display
public class OpenDisplayHandler
{
	@Execute
	public void execute(@Named("command.parameter.display") String display)
	{
	    System.out.println("Should open " + display);
	}
}