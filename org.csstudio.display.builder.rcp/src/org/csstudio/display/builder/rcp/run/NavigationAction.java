/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.run;

import java.util.List;

import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.display.builder.rcp.DisplayInfo;
import org.csstudio.display.builder.rcp.Messages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Action to open 'previous' or 'next' display in back/forward navigation
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
abstract public class NavigationAction extends Action implements DisplayNavigation.Listener, IMenuCreator
{
    private final static ImageDescriptor icon =
            AbstractUIPlugin.imageDescriptorFromPlugin(ModelPlugin.ID, "icons/open_display.png");
	private final RuntimeViewPart part;
	private final DisplayNavigation navigation;
	private Menu menu = null;

	/** @param part Part for which to navigate
	 *  @param navigation Navigation helper
	 *  @return Action to go 'back'
	 */
	public static NavigationAction createBackAction(final RuntimeViewPart part, final DisplayNavigation navigation)
	{
	    return new NavigationAction(part, navigation,
                         	        Messages.NavigateBack, Messages.NavigateBack_TT,
                         	        ActionFactory.BACKWARD_HISTORY,
                        	        ISharedImages.IMG_TOOL_BACK, ISharedImages.IMG_TOOL_BACK_DISABLED)
        {
	        @Override
            protected List<DisplayInfo> getDisplays()
	        {
	            return navigation.getBackwardDisplays();
	        }

            @Override
            protected DisplayInfo navigate(final int steps)
            {
                return navigation.goBackward(steps);
            }
        };
	}

    /** @param part Part for which to navigate
     *  @param navigation Navigation helper
     *  @return Action to go 'forward'
     */
	public static NavigationAction createForwardAction(final RuntimeViewPart part, final DisplayNavigation navigation)
    {
        return new NavigationAction(part, navigation,
                                    Messages.NavigateForward, Messages.NavigateForward_TT,
                                    ActionFactory.FORWARD_HISTORY,
                                    ISharedImages.IMG_TOOL_FORWARD, ISharedImages.IMG_TOOL_FORWARD_DISABLED)
        {
            @Override
            protected List<DisplayInfo> getDisplays()
            {
                return navigation.getForwardDisplays();
            }

            @Override
            protected DisplayInfo navigate(final int steps)
            {
                return navigation.goForward(steps);
            }
        };
    }

	// This many-arg private constructor
	// and the two abstract methods allow sharing most of the
	// code for both 'back' and 'forward'
	private NavigationAction(final RuntimeViewPart part,
	        final DisplayNavigation navigation,
	        final String text, final String tooltip,
	        final ActionFactory action_factory,
	        final String shared_icon, final String shared_icon_disabled)
    {
        this.part = part;
        this.navigation = navigation;

        final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setText(text);
        setToolTipText(tooltip);
        setId(action_factory.getId());
        setImageDescriptor(sharedImages.getImageDescriptor(shared_icon));
        setDisabledImageDescriptor(sharedImages.getImageDescriptor(shared_icon_disabled));
        setActionDefinitionId(action_factory.getCommandId());
        navigation.addListener(this);

        // Trigger initial enable/disable
        displayHistoryChanged(navigation);

        setMenuCreator(this);
    }

	/** @return List of back resp. forward displays */
	abstract protected List<DisplayInfo> getDisplays();

	/** @param steps Number of steps to navigate back resp. forward
	 *  @return Display to which we navigated
	 */
	abstract protected DisplayInfo navigate(int steps);

	// DisplayNavigation.Listener
	@Override
	public void displayHistoryChanged(final DisplayNavigation navigation)
	{
		setEnabled(getDisplays().size() > 0);
	}

	// IMenuCreator
	@Override
	public Menu getMenu(final Control parent)
	{   // Entries can change, so re-create on each call
		if (menu != null)
			menu.dispose();

		menu = new Menu(parent);
		final List<DisplayInfo> displays = getDisplays();
		final int N = displays.size();
		for (int i=0; i<N; ++i)
			addAction(displays.get(N-i-1), i+1);

		return menu;
	}

	// IMenuCreator
	@Override
	public Menu getMenu(final Menu parent)
	{
	    // Not called since only used for toolbar button, i.e. 'Control'
	    return null;
	}

	/** Add action to navigate to a specific display to the 'menu'.
	 *
	 *  <p><code>menu</code> must be valid
	 *
	 *  @param display Target display
	 *  @param steps Position of that display in navigation stack
	 */
    private void addAction(final DisplayInfo display, final int steps)
	{
		final Action action = new Action(display.getName())
		{
			@Override
			public void run()
			{
				final DisplayInfo display = navigate(steps);
				part.loadDisplayFile(display);
			}
		};
		action.setImageDescriptor(icon);
		final ActionContributionItem item = new ActionContributionItem(action);
		item.fill(menu, -1);
	}

	/** Navigate one step
	 *  <p>.. as opposed to selecting a display further down the stack
	 *  from the menu
	 */
    @Override
    public void run()
    {
        final DisplayInfo display = navigate(1);
        part.loadDisplayFile(display);
    }

	// IMenuCreator
	@Override
	public void dispose()
	{
		if (menu != null)
			menu.dispose();
		menu = null;
		navigation.removeListener(this);
	}
}
