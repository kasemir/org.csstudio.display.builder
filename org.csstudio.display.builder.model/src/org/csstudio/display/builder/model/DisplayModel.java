/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;

import java.util.List;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;

/** Display Model.
 *
 *  <p>Describes overall size of display, global settings,
 *  and holds widgets.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayModel extends ContainerWidget
{
    /** Reserved DisplayModel user data key for name of input file */
    public static final String USER_DATA_INPUT_FILE = "_input_file";

    /** Reserved DisplayModel user data key for storing toolkit used as representation.
     *
     *  <p>Holds ToolkitRepresentation.
     */
    public static final String USER_DATA_TOOLKIT = "_toolkit";

    /** Reserved DisplayModel user data key for storing toolkit parent item */
    public static final String USER_DATA_TOOLKIT_PARENT = "_toolkit_parent";

    /** Widget user data key for storing the embedding widget.
     *
     *  <p>For a {@link DisplayModel} that is held by an {@link EmbeddedDisplayWidget},
     *  this user data element of the model points to the embedding widget.
     */
    public static final String USER_DATA_EMBEDDING_WIDGET = "_embedding_widget";

    private WidgetProperty<Macros> macros;

    /** Create display model */
    public DisplayModel()
    {
        super("display");
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(macros = widgetMacros.createProperty(this, new Macros()));
    }

    /** @return Widget 'macros' */
    public WidgetProperty<Macros> widgetMacros()
    {
        return macros;
    }

    @Override
    protected void setParent(final ContainerWidget parent)
    {
        throw new IllegalStateException("Display widget cannot have parent widget " + parent);
    }

    /** Locate a child widget by name
     *
     *  <p>Recurses through all widgets of the model,
     *  including groups and sub-groups.
     *
     *  @param name Name of widget
     *  @return First widget with given name or <code>null</code>
     */
    public Widget getChildByName(final String name)
    {
        return searchChildByName(this, name);
    }

    private static Widget searchChildByName(final ContainerWidget container, final String name)
    {
        for (final Widget child : container.getChildren())
        {
            if (child.getName().equals(name))
                return child;
            if (child instanceof ContainerWidget)
            {
                final Widget maybe = searchChildByName((ContainerWidget) child, name);
                if (maybe != null)
                    return maybe;
            }
        }
        return null;
    }

    /** Replace content, i.e. children and user data.
     *
     *  <p>A model is typically associated with a representation.
     *  As the model changes, the representation is updated,
     *  but it holds on to the same original model instance.
     *  When replacing the model, the child elements of the
     *  existing model instance are updated.
     *  Note that the 'other_model' is afterwards empty,
     *  because its children have been assigned to a different model.
     *
     *  @param other_model Other model
     */
    public void replaceWith(final DisplayModel other_model)
    {
        // removeChild also updates the child's parent. children.clear() wouldn't
        for (final Widget child : getChildren())
            removeChild(child);
        for (final Widget child : other_model.getChildren())
        {
            other_model.removeChild(child);
            addChild(child);
        }
        widgetMacros().setValue(other_model.widgetMacros().getValue());
        user_data.clear();
        user_data.putAll(other_model.user_data);
        firePropertyChange(null, null, null);
    }

    /** Display model provides macros for all its widgets.
     *  @return {@link Macros}
     */
    @Override
    public Macros getEffectiveMacros()
    {
        final Macros my_macros = widgetMacros().getValue();
        final Widget embedder = getUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET);
        if (embedder != null)
            return Macros.merge(embedder.getEffectiveMacros(), my_macros);
        else
        {
            // TODO Merge macros from preferences with my_macros
            // return Macros.merge(Preferences.getMacros(), my_macros);
            return my_macros;
        }
    }
}
