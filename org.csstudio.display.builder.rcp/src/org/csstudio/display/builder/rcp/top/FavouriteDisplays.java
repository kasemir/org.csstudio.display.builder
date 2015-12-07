
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
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

// E4 DdynamicMenuContribution
public class FavouriteDisplays
{
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
        final MDirectMenuItem item = MMenuFactory.INSTANCE.createDirectMenuItem();
        item.setLabel(display.getName());
        item.setIconURI("platform:/plugin/org.csstudio.display.builder.rcp/icons/display.png");

        menu.getChildren().add(item);

        // TODO: Unclear how to tie these menu items to a command that
        //       then receives the DisplayInfo

//      MHandledMenuItem item2 = MMenuFactory.INSTANCE.createHandledMenuItem();
//      item2.setLabel("Display 2");
//      item2.setIconURI("platform:/plugin/org.csstudio.display.builder.rcp/icons/display.png");

//
//      Map<String, Object> parameters = new HashMap<>();
//      parameters.put("command.parameter.display", "/some/path");
//      ParameterizedCommand command = command_service.createCommand("org.csstudio.display.builder.rcp.command.open_display", parameters);
//      item2.setCommand(command);
//      menu.getChildren().add(item2);
    }
}