/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.util.List;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.NavigationTabsWidget;

/** Creates JavaFX item for model widget
 *
 *  <p>Different from widget representations in general,
 *  this one implements the loading of the embedded model,
 *  an operation that could be considered a runtime aspect.
 *  This was done to allow viewing the embedded content
 *  in the editor.
 *  The embedded model will be started by the EmbeddedDisplayRuntime.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NavigationTabsRepresentation extends RegionBaseRepresentation<NavigationTabs, NavigationTabsWidget>
{
    private final DirtyFlag dirty_sizes = new DirtyFlag();
    private final DirtyFlag dirty_tabs = new DirtyFlag();
    private final DirtyFlag dirty_tabsize = new DirtyFlag();

    @Override
    protected boolean isFilteringEditModeClicks()
    {
        return true;
    }

    @Override
    public NavigationTabs createJFXNode() throws Exception
    {
        final NavigationTabs tabs = new NavigationTabs();

        // model_widget.setUserData(EmbeddedDisplayWidget.USER_DATA_EMBEDDED_DISPLAY_CONTAINER, inner);

        return tabs;
    }

//    @Override
//    protected Parent getChildParent(final Parent parent)
//    {
//        return inner;
//    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propWidth().addUntypedPropertyListener(this::sizesChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::sizesChanged);

        model_widget.propTabWidth().addPropertyListener(this::tabSizeChanged);
        model_widget.propTabHeight().addPropertyListener(this::tabSizeChanged);
        model_widget.propTabSpacing().addPropertyListener(this::tabSizeChanged);

        model_widget.propTabs().addPropertyListener(this::tabsChanged);
    }

    private void sizesChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_sizes.mark();
        toolkit.scheduleUpdate(this);
    }

    private void tabSizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_tabsize.mark();
        toolkit.scheduleUpdate(this);
    }

    private void tabsChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_tabs.mark();
        toolkit.scheduleUpdate(this);
    }


    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_sizes.checkAndClear())
        {
            final Integer width = model_widget.propWidth().getValue();
            final Integer height = model_widget.propHeight().getValue();
            jfx_node.setPrefSize(width, height);
        }
        if (dirty_tabsize.checkAndClear())
        {
            jfx_node.setTabSize(model_widget.propTabWidth().getValue(),
                                model_widget.propTabHeight().getValue());
            jfx_node.setTabSpacing(model_widget.propTabSpacing().getValue());
        }
        if (dirty_tabs.checkAndClear())
        {
            final List<String> tabs = model_widget.propTabs().getValue()
                                                  .stream()
                                                  .map(tab -> tab.name().getValue())
                                                  .collect(Collectors.toList());
            jfx_node.setTabs(tabs);
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
//        inner = null;
    }
}
