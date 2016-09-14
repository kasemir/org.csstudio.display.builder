/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propMacros;

import java.util.List;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;

/** Display Model.
 *
 *  <p>Describes overall size of display, global settings,
 *  and holds widgets.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayModel extends Widget
{
    /** File extension used for display files */
    public static final String FILE_EXTENSION = "bob";

    public static final String WIDGET_TYPE = "display";

    /** Reserved DisplayModel user data key for name of input file */
    public static final String USER_DATA_INPUT_FILE = "_input_file";

    /** Reserved DisplayModel user data key for storing toolkit used as representation.
     *
     *  <p>Holds ToolkitRepresentation.
     */
    public static final String USER_DATA_TOOLKIT = "_toolkit";

    /** Widget user data key for storing the embedding widget.
     *
     *  <p>For a {@link DisplayModel} that is held by an {@link EmbeddedDisplayWidget},
     *  this user data element of the model points to the embedding widget.
     */
    public static final String USER_DATA_EMBEDDING_WIDGET = "_embedding_widget";

    /** Macros set in preferences
     * 
     *  <p>Fetched once on display creation to
     *  use latest preference settings on newly opened display,
     *  while not fetching preferences for each macro evaluation
     *  within a running display to improve performance
     */
    private final Macros preference_macros = Preferences.getMacros();

    private volatile WidgetProperty<Macros> macros;
    private volatile WidgetProperty<WidgetColor> background;
    private volatile ChildrenProperty children;

    /** Create display model */
    public DisplayModel()
    {
        super(WIDGET_TYPE, 800, 600);
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(macros = propMacros.createProperty(this, new Macros()));
        properties.add(background = propBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
        properties.add(children = new ChildrenProperty(this));
    }

    /** @return 'macros' property */
    public WidgetProperty<Macros> propMacros()
    {
        return macros;
    }

    /** @return Runtime 'children' */
    public ChildrenProperty runtimeChildren()
    {
        return children;
    }

    /** Get read-only list of children
     *
     *  <p>Convenience method.
     *  Use <code>runtimeChildren()</code>
     *  for full access.
     *
     *  @return Child widgets
     */
    public List<Widget> getChildren()
    {
        return children.getValue();
    }

    @Override
    protected void setParent(final Widget parent)
    {
        throw new IllegalStateException("Display cannot have parent widget " + parent);
    }

    /** Display model provides macros for all its widgets.
     *  @return {@link Macros}
     */
    @Override
    public Macros getEffectiveMacros()
    {
        // 1) Lowest priority are either
        // 1.a) .. global macros from preferences
        // 1.b) .. macros from embedding widget,
        //      which may in turn be embedded elsewhere,
        //      ultimately fetching the macros from preferences.
        final Widget embedder = getUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET);
        Macros result = (embedder == null)
            ? preference_macros
            : embedder.getEffectiveMacros();

        // 2) This display may provide added macros or replacement values
        result = Macros.merge(result, propMacros().getValue());
        return result;
    }

    /** @return 'background_color' property */
    public WidgetProperty<WidgetColor> propBackgroundColor()
    {
        return background;
    }
}
