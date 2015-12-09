
package org.csstudio.display.builder.rcp.top;

import java.util.List;

import javax.inject.Inject;

import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.DisplayInfoXMLUtil;
import org.csstudio.display.builder.rcp.Plugin;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

// E4 DynamicMenuContribution
public class TopDisplaysMenuContribution
{
    @Inject
    MApplication application;

    @Inject
    EModelService model_service;

    @Inject
    ECommandService command_service;

    @Inject
    EHandlerService handler_service;


    @AboutToShow
	public void aboutToShow(final List<MMenuElement> items,
	                        @Preference(nodePath=Plugin.ID, value="top_displays")
	                        String setting)
	{
        final MMenu menu = MMenuFactory.INSTANCE.createMenu();
        menu.setLabel("Fav. Displays (E4)");
        menu.setIconURI("platform:/plugin/org.csstudio.display.builder.rcp/icons/display.png");
        items.add(menu);

        try
        {
            final List<DisplayInfo> displays = DisplayInfoXMLUtil.fromDisplaysXML(setting);
            for (DisplayInfo display : displays)
                addMenuItem(menu, display);
        }
        catch (Exception ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
	}


    private void addMenuItem(MMenu menu, DisplayInfo display)
    {
        // TODO: Unclear how to pass the DisplayInfo to a command parameter
        final MCommand command = model_service.createModelElement(MCommand.class);
        command.setElementId("org.csstudio.display.builder.rcp.command.open_display");

//        final MParameter parameter = model_service.createModelElement(MParameter.class);
//        parameter.setElementId("org.csstudio.display.builder.rcp.commandparameter.display");
//        parameter.setName("display");
//        parameter.setValue(display.getPath());

        final MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem();
        item.setLabel(display.getName());
        item.setIconURI("platform:/plugin/org.csstudio.display.builder.rcp/icons/display.png");
        item.setCommand(command);
//        item.getParameters().add(parameter);

        menu.getChildren().add(item);
    }
}