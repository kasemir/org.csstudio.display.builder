/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorActions;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorScripts;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionVisible;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeConnected;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.macros.MacroOrPropertyProvider;
import org.csstudio.display.builder.model.macros.MacroValueProvider;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.osgi.framework.Version;

/** Base class for all widgets.
 *
 *  <p>A Widget has properties, supporting read access, subscription
 *  and for most properties also write access.
 *
 *  <p>Widgets are part of a hierarchy.
 *  Their parent is either the {@link DisplayModel} or another
 *  {@link ContainerWidget}.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Widget
{
    // These user data keys are reserved for internal use
    // of the framework.
    //
    // On the API level, the Model (DisplayModel, Widgets)
    // is independent from the Representation (ToolkitRepresentation)
    // and Runtime (WidgetRuntime).
    //
    // The representation and runtime implementations, however,
    // need to associate certain pieces of data with model elements,
    // which is done via the following reserved user data keys.
    //
    // They all start with an underscore to indicate that they
    // are meant to be private, not to be used as API.

    /** Reserved widget user data key for storing the representation.
     *
     *  <p>The WidgetRepresentation for each {@link Widget}
     *  is stored under this key.
     */
    public static final String USER_DATA_REPRESENTATION = "_representation";

    /** Reserved widget user data key for storing the runtime.
     *
     *  <p>The WidgetRuntime for each {@link Widget}
     *  is stored under this key.
     */
    public static final String USER_DATA_RUNTIME = "_runtime";

    /** Reserved widget user data key for storing script support.
     *
     *  <p>ScriptSupport is attached to the top-level root
     *  of the widget tree, i.e. the {@link DisplayModel}
     *  that is obtained by traversing up via {@link EmbeddedDisplayWidget}s
     *  to the top-level model.
     */
    public static final String USER_DATA_SCRIPT_SUPPORT = "_script_support";

    /** Parent widget */
    private volatile ContainerWidget parent = null;

    /** All properties, ordered by category, then sequence of definition */
    protected final Set<WidgetProperty<?>> properties;

    // Design decision:
    //
    // Widget holds a map of properties: "name" -> CommonWidgetProperties.widgetName
    //
    // Could also depend on each widget defining getters/setters getName()/setName()
    // and then use beam-type introspection, but this would not allow determining
    // if a property has a default value, is a runtime-only property etc.
    //
    // A property accessor nameProperty() as used for JavaFX bindable properties
    // would work as long as it preserves the order of properties.
    //
    // The implementation might later change from a property_map to
    // introspection-based lookup of xxxProperty() accessors.
    // The API for widget users would remain the same:
    // getProperties(), getProperty(), getPropertyValue(), setPropertyValue()

    /** Map of property names to properties */
    // Map is final, all properties are collected in widget constructor.
    // Values of properties can change, but the list of properties itself
    // is thread safe
    protected final Map<String, WidgetProperty<?>> property_map;

    // Actual properties
    private WidgetProperty<String> type;
    private WidgetProperty<String> name;
    private WidgetProperty<Integer> x;
    private WidgetProperty<Integer> y;
    private WidgetProperty<Integer> width;
    private WidgetProperty<Integer> height;
    private WidgetProperty<Boolean> visible;
    private WidgetProperty<List<ActionInfo>> actions;
    private WidgetProperty<List<ScriptInfo>> scripts;
    private WidgetProperty<Boolean> connected;

    /** Map of user data */
    protected final Map<String, Object> user_data = new ConcurrentHashMap<>(4); // Reserve room for "representation", "runtime"

    /** Widget constructor.
     *  @param type Widget type
     */
    public Widget(final String type)
    {
        this(type, 100, 20);
    }

    /** Widget constructor.
     *  @param type Widget type
     *  @param default_width Default width
     *  @param default_height .. and height
     */
    public Widget(final String type, final int default_width, final int default_height)
    {
        // Collect properties
        final List<WidgetProperty<?>> prelim_properties = new ArrayList<>();

        // -- Mandatory properties --
        prelim_properties.add(this.type = widgetType.createProperty(this, type));
        prelim_properties.add(name = widgetName.createProperty(this, ""));
        prelim_properties.add(x = positionX.createProperty(this, 0));
        prelim_properties.add(y = positionY.createProperty(this, 0));
        prelim_properties.add(width = positionWidth.createProperty(this, default_width));
        prelim_properties.add(height = positionHeight.createProperty(this, default_height));
        prelim_properties.add(visible = positionVisible.createProperty(this, true));
        prelim_properties.add(actions = behaviorActions.createProperty(this, Collections.emptyList()));
        prelim_properties.add(scripts = behaviorScripts.createProperty(this, Collections.emptyList()));
        // Start 'connected', assuming there are no PVs
        prelim_properties.add(connected = runtimeConnected.createProperty(this, true));

        // -- Widget-specific properties --
        defineProperties(prelim_properties);
        if (prelim_properties.contains(null))
            throw new IllegalStateException("Null properties");

        // Sort by category, then order of definition.
        // Prelim_properties has the original order of definition,
        // which we want to preserve as a secondary sorting criteria
        // after property category.
        final List<WidgetProperty<?>> sorted = new ArrayList<>(prelim_properties.size());
        sorted.addAll(prelim_properties);

        final Comparator<WidgetProperty<?>> byCategory =
                Comparator.comparing(WidgetProperty::getCategory);
        final Comparator<WidgetProperty<?>> byOrder =
                Comparator.comparingInt(p -> prelim_properties.indexOf(p));
        Collections.sort(sorted, byCategory.thenComparing(byOrder));
        // Capture as constant sorted set
        properties = Collections.unmodifiableSet(new LinkedHashSet<>(sorted));

        // Map for faster lookup by property name
        property_map = properties.stream().collect(
                Collectors.toMap(WidgetProperty::getName, Function.identity()));
    }

    /** @return Widget version number */
    public Version getVersion()
    {
        // Legacy used 1.0.0 for most widgets,
        // so 2.0.0 indicates an update.
        // Selected legacy widgets had incremented to a higher version,
        // which needs to be handled for each such widget.
        return new Version(2, 0, 0);
    }

    /** @return Widget Type */
    public String getType()
    {
        return type.getValue();
    }

    /** @return Widget Name */
    public String getName()
    {
        return name.getValue();
    }

    /** @return Parent widget in Widget tree */
    public Optional<ContainerWidget> getParent()
    {
        return Optional.ofNullable(parent);
    }

    /** Invoked by the parent widget
     *  @param parent Parent widget
     */
    protected void setParent(final ContainerWidget parent)
    {
        this.parent = parent;
    }

    /** Locate display model, i.e. root of widget tree
     *  @return {@link DisplayModel} for widget
     *  @throws Exception if widget is not part of a model
     */
    public DisplayModel getDisplayModel() throws Exception
    {
        Widget candidate = this;
        while (candidate.getParent().isPresent())
            candidate = candidate.getParent().get();
        if (candidate instanceof DisplayModel)
            return (DisplayModel) candidate;
        throw new Exception("Missing DisplayModel for " + this);
    }

    /** Called on construction to define widget's properties.
     *
     *  <p>Mandatory properties have already been defined.
     *  Derived class overrides to add its own properties.
     *
     *  @param properties List to which properties must be added
     */
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        // Derived class should invoke
        //    super.defineProperties(properties)
        // and may then add its own properties.
    }

    // Accessors to properties are not strictly needed
    // because of generic getProperty(..),
    // but can sometimes be useful in IDE when dealing with
    // known widget type

    /** @return Widget 'name' */
    public WidgetProperty<String> widgetName()
    {
        return name;
    }

    /** @return Position 'x' */
    public WidgetProperty<Integer> positionX()
    {
        return x;
    }

    /** @return Position 'y' */
    public WidgetProperty<Integer> positionY()
    {
        return y;
    }

    /** @return Position 'width' */
    public WidgetProperty<Integer> positionWidth()
    {
        return width;
    }

    /** @return Position 'height' */
    public WidgetProperty<Integer> positionHeight()
    {
        return height;
    }

    /** @return Position 'visible' */
    public WidgetProperty<Boolean> positionVisible()
    {
        return visible;
    }

    /** @return Behavior 'actions' */
    public WidgetProperty<List<ActionInfo>> behaviorActions()
    {
        return actions;
    }

    /** @return Behavior 'scripts' */
    public WidgetProperty<List<ScriptInfo>> behaviorScripts()
    {
        return scripts;
    }

    /** @return Runtime 'connected' */
    public WidgetProperty<Boolean> runtimeConnected()
    {
        return connected;
    }

    /** Obtain configurator.
     *
     *  <p>While typically using the default {@link WidgetConfigurator},
     *  widget may provide a different configurator for reading older
     *  persisted date.
     *  @param persisted_version Version of the persisted data.
     *  @return Widget configurator for that version
     *  @throws Exception if persisted version cannot be handled
     */
    public WidgetConfigurator getConfigurator(final Version persisted_version)
        throws Exception
    {
        // if (persisted_version.getMajor() < 1)
        //    throw new Exception("Can only handle version 1.0.0 and higher");
        return new WidgetConfigurator(persisted_version);
    }

    /** Get all properties of the widget.
     *
     *  <p>Properties are ordered by category and sequence of definition.
     *  @return Unmodifiable set
     */
    public Set<WidgetProperty<?>> getProperties()
    {
        return properties;
    }

    /** Check if widget has a given property.
     *  @param name Property name
     *  @return Optional {@link WidgetProperty}
     */
    public Optional<WidgetProperty<?>> checkProperty(final String name)
    {
        final WidgetProperty<?> property = property_map.get(name);
        return Optional.ofNullable(property);
    }

    /** Check if widget has a given property.
     *  @param property Property descriptor
     *  @return Optional {@link WidgetProperty}
     */
    public <PT> Optional<WidgetProperty<PT>> checkProperty(final WidgetPropertyDescriptor<PT> property_description)
    {
        @SuppressWarnings("unchecked")
        final WidgetProperty<PT> property = (WidgetProperty<PT>) property_map.get(property_description.getName());
        return Optional.ofNullable(property);
    }

    /** Get widget property.
     *
     *  <p>Property access based on property description allows
     *  type-safe access.
     *
     *  @param property_description Property description
     *  @return {@link WidgetProperty}
     *  @throws IllegalArgumentException if property is unknown
     */
    @SuppressWarnings("unchecked")
    public <PT> WidgetProperty<PT> getProperty(final WidgetPropertyDescriptor<PT> property_description)
    {
        final WidgetProperty<?> property = getProperty(property_description.getName());
        return (WidgetProperty<PT>)property;
    }

    /** Get widget property.
     *
     *  <p>Property access based on property name returns generic
     *  WidgetProperty without known type.
     *
     *  @param name Property name
     *  @return {@link WidgetProperty}
     *  @throws IllegalArgumentException if property is unknown
     */
    public WidgetProperty<?> getProperty(final String name)
    {
        final WidgetProperty<?> property = property_map.get(name);
        if (property == null)
            throw new IllegalArgumentException(toString() + " has no '" + name + "' property");
        return property;
    }

    /** Get widget property value.
     *
     *  <p>Property access based on property description allows
     *  type-safe access.
     *
     *  @param property_description Property description
     *  @return Value of the property
     *  @throws IllegalArgumentException if property is unknown
     */
    public <PT> PT getPropertyValue(final WidgetPropertyDescriptor<PT> property_description)
    {
        return getProperty(property_description).getValue();
    }

    /** Get widget property value.
     *
     *  <p>Property access based on property name returns generic
     *  WidgetProperty without known type.
     *  Data is cast to the receiver type, but that cast may fail
     *  if actual data type differs.
     *
     *  @param name Property name
     *  @param <TYPE> Data is cast to the receiver's type
     *  @return Value of the property
     *  @throws IllegalArgumentException if property is unknown
     */
    @SuppressWarnings("unchecked")
    public <TYPE> TYPE getPropertyValue(final String name)
    {
        return (TYPE) getProperty(name).getValue();
    }

    /** Set widget property value.
     *
     *  <p>Property access based on property description allows
     *  type-safe access.
     *
     *  @param property_description Property description
     *  @param value New value of the property
     *  @throws IllegalArgumentException if property is unknown
     */
    public <PT> void setPropertyValue(final WidgetPropertyDescriptor<PT> property_description,
                                      final PT value)
    {
        getProperty(property_description).setValue(value);
    }

    /** Set widget property value.
     *
     *  <p>Property access based on property name returns generic
     *  WidgetProperty without known type.
     *
     *  @param name Property name
     *  @param value New value of the property
     *  @throws IllegalArgumentException if property is unknown
     *  @throws Exception if value is unsuitable for this property
     */
    public void setPropertyValue(final String name,
                                 final Object value) throws Exception
    {
        getProperty(name).setValueFromObject(value);
    }

    /** Determine effective macros.
     *
     *  <p>Default implementation requests macros
     *  from parent.
     *
     *  <p>Macros will be <code>null</code> while
     *  the widget is loaded until it is included in a model.
     *
     *  @return {@link Macros}
     *  @throws IllegalStateException
     */
    public Macros getEffectiveMacros()
    {
        final Optional<ContainerWidget> the_parent = getParent();
        if (! the_parent.isPresent())
            return null;
        return the_parent.get().getEffectiveMacros();
    }

    /** @return Macro provider for effective macros, falling back to properties */
    public MacroValueProvider getMacrosOrProperties()
    {
        return new MacroOrPropertyProvider(getEffectiveMacros(), property_map);
    }

    /** Set user data
     *
     *  <p>User code can attach arbitrary data to a widget.
     *  This data is _not_ persisted with the model,
     *  and there is no change notification.
     *
     *  <p>User code should avoid using reserved keys
     *  which start with an underscore "_...".
     *
     *  @param key Key
     *  @param data Data
     */
    public void setUserData(final String key, final Object data)
    {
        user_data.put(key, data);
    }

    /** @param key Key
     *  @param <TYPE> Data is cast to the receiver's type
     *  @return User data associated with key, or <code>null</code>
     *  @see #setUserData(String, Object)
     */
    @SuppressWarnings("unchecked")
    public <TYPE> TYPE getUserData(final String key)
    {
        if (key == null)
        {   // Debug gimmick:
            // null is not supported as valid key,
            // but triggers dump of all user properties
            Logger.getLogger(getClass().getName())
                .info(this + " user data: " + user_data.entrySet());
            return null;
        }
        final Object data = user_data.get(key);
        return (TYPE)data;
    }

    /** Remove a user data entry
     *  @param key Key for which to remove user data
     *  @return User data associated with key that has been removed, or <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public <TYPE> TYPE clearUserData(final String key)
    {
        return (TYPE)user_data.remove(key);
    }

    @Override
    public String toString()
    {
        // Show name's specification, not value, because otherwise
        // a plain debug printout can trigger macro resolution for the name
        return "Widget '" + ((MacroizedWidgetProperty<?>)name).getSpecification() + "' (" + getType() + ")";
    }
}
