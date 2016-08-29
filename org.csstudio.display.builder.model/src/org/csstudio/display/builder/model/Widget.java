/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.ModelPlugin.logger;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorActions;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorRules;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorScripts;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionHeight;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionWidth;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.macros.MacroOrPropertyProvider;
import org.csstudio.display.builder.model.macros.MacroValueProvider;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.RuleInfo;
import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.osgi.framework.Version;

/** Base class for all widgets.
 *
 *  <p>A Widget has properties, supporting read access, subscription
 *  and for most properties also write access.
 *
 *  <p>Properties can be accessed in a most generic way based on the
 *  property name:
 *  <pre>
 *  getPropertyValue("text")
 *  setPropertyValue("text", "Hello")
 *  getPropertyValue("x")
 *  setPropertyValue("x", 60)
 *  </pre>
 *
 *  <p>While this is ideal for access from scripts,
 *  Java code that deals with a specific widget can access
 *  properties in the type-safe way:
 *  <pre>
 *  LabelWidget label;
 *  label.positionX().getValue();
 *  label.positionX().setValue(60);
 *  </pre>
 *
 *  <p>Widgets are part of a hierarchy.
 *  Their parent is either the {@link DisplayModel} or another
 *  widget with a {@link ChildrenProperty}
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

    /** Reserved user data key for Widget that has 'children', i.e. is a parent,
     *  to store the toolkit parent item
     */
    public static final String USER_DATA_TOOLKIT_PARENT = "_toolkit_parent";

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
    private volatile Widget parent = null;

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
    private WidgetProperty<List<ActionInfo>> actions;
    private WidgetProperty<List<ScriptInfo>> scripts;
    private WidgetProperty<List<RuleInfo>> rules;

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
        prelim_properties.add(actions = behaviorActions.createProperty(this, Collections.emptyList()));
        prelim_properties.add(scripts = behaviorScripts.createProperty(this, Collections.emptyList()));
        prelim_properties.add(rules = behaviorRules.createProperty(this, Collections.emptyList()));

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
    public final String getType()
    {
        return type.getValue();
    }

    /** @return Widget Name */
    public final String getName()
    {
        return name.getValue();
    }

    /** Unique runtime identifier of a widget
     *
     *  <p>At runtime, this ID can be used to construct
     *  PVs that are unique and specific to this instance
     *  of a widget.
     *  Even if the same display is opened multiple times
     *  within the same JVM, the widget is very likely
     *  to receive a new, unique identifier.
     *
     *  @return Unique Runtime Identifier for widget
     */
    public final String getID()
    {   // Base on ID hash code
        final int id = System.identityHashCode(this);
        return "WD" + Integer.toHexString(id);
    }

    /** @return Parent widget in Widget tree */
    public final Optional<Widget> getParent()
    {
        return Optional.ofNullable(parent);
    }

    /** Invoked by the parent widget
     *  @param parent Parent widget
     */
    protected void setParent(final Widget parent)
    {
        this.parent = parent;
    }

    /** Locate display model, i.e. root of widget tree
     *
     *  <p>Note that for embedded displays, this would
     *  return the embedded model, not the top-level
     *  model of the window.
     *  Compare <code>RuntimeUtil.getTopDisplayModel(widget)</code>
     *
     *  @return {@link DisplayModel} for widget
     *  @throws Exception if widget is not part of a model
     */
    public final DisplayModel getDisplayModel() throws Exception
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
    // but are useful in IDE when dealing with
    // known widget type

    /** @return Widget 'name' */
    public final WidgetProperty<String> widgetName()
    {
        return name;
    }

    /** @return Position 'x' */
    public final WidgetProperty<Integer> positionX()
    {
        return x;
    }

    /** @return Position 'y' */
    public final WidgetProperty<Integer> positionY()
    {
        return y;
    }

    /** @return Position 'width' */
    public final WidgetProperty<Integer> positionWidth()
    {
        return width;
    }

    /** @return Position 'height' */
    public final WidgetProperty<Integer> positionHeight()
    {
        return height;
    }

    /** @return Behavior 'actions' */
    public final WidgetProperty<List<ActionInfo>> behaviorActions()
    {
        return actions;
    }

    /** @return Behavior 'scripts' */
    public final WidgetProperty<List<ScriptInfo>> behaviorScripts()
    {
        return scripts;
    }

    /** @return Behavior 'rules' */
    public final WidgetProperty<List<RuleInfo>> behaviorRules()
    {
        return rules;
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
    public final Set<WidgetProperty<?>> getProperties()
    {
        return properties;
    }

    /** Get names of all properties of the widget
     *
     *  <p>Provides the complete list of properties,
     *  including all current array items and structure elements
     *  via their path name.
     *
     *  @return Property names
     */
    public final Collection<String> getCurrentPropertyNames()
    {
        final List<String> names = new ArrayList<>();
        for (WidgetProperty<?> property : properties)
            addPropertyNames(names, property.getName(), property);
        return names;
    }

    /** Helper for adding the complete property name 'paths'
     *
     *  <p>For a scalar property, this method simply adds that property
     *  name to the list of names.
     *
     *  <p>For arrays or structures, it adds names for each array resp. structure element.
     *
     * @param names
     * @param path
     * @param property
     */
    public static final void addPropertyNames(final List<String> names, String path, final WidgetProperty<?> property)
    {
        if (property instanceof ArrayWidgetProperty)
        {
            final ArrayWidgetProperty<?> array = (ArrayWidgetProperty<?>) property;
            for (int i=0; i<array.size(); ++i)
                addPropertyNames(names, path + "[" + i + "]", array.getElement(i));
        }
        else if (property instanceof StructuredWidgetProperty)
        {
            final StructuredWidgetProperty struct = (StructuredWidgetProperty) property;
            for (int i=0; i<struct.size(); ++i)
            {
                final WidgetProperty<?> item = struct.getElement(i);
                addPropertyNames(names, path + "." + item.getName(), item);
            }
        }
        else
            names.add(path);
    }

    /** Check if widget has a given property.
     *
     *  <p>This is called by rules or scripts which
     *  retrieve a property by name, since they do not
     *  know the exact widget class and thus cannot
     *  use the type-safe property accessors.
     *
     *  @param name Property name
     *  @return Optional {@link WidgetProperty}
     */
    public final Optional<WidgetProperty<?>> checkProperty(final String name)
    {
        WidgetProperty<?> property;
        try
        {
            property = getProperty(name);
        }
        catch (Exception e)
        {
            property = null;
        }

        return Optional.ofNullable(property);
    }

    /** Check if widget has a given property.
     *  @param property Property descriptor
     *  @return Optional {@link WidgetProperty}
     */
    public final <PT> Optional<WidgetProperty<PT>> checkProperty(final WidgetPropertyDescriptor<PT> property_description)
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
    public final <PT> WidgetProperty<PT> getProperty(final WidgetPropertyDescriptor<PT> property_description)
    {
        final WidgetProperty<?> property = getProperty(property_description.getName());
        return (WidgetProperty<PT>)property;
    }

    /** Get widget property.
     *
     *  <p>This method ends up being called from rules and scripts
     *  which do now know the exact widget type
     *  and thus fetch properties by name.
     *
     *  <p>Property access based on property name returns generic
     *  WidgetProperty without known type.
     *
     *  <p>To allow use of legacy scripts and rules,
     *  the widget implementation may override to
     *  handle deprecated property names.
     *
     *  @param name Property name
     *  @return {@link WidgetProperty}
     *  @throws IllegalArgumentException if property is unknown
     */
    public WidgetProperty<?> getProperty(final String name)
    {   // Is name a path "struct_prop.array_prop[2].element" ?
        if (name.indexOf('.') >=0  ||  name.indexOf('[') >= 0)
            return getPropertyByPath(name);
        // Plain property name
        final WidgetProperty<?> property = property_map.get(name);
        if (property == null)
            throw new IllegalArgumentException(toString() + " has no '" + name + "' property");
        return property;
    }

    /** Get property via path
     *  @param path_name "struct_prop.array_prop[2].element"
     *  @return Property for "element"
     *  @throws IllegalArgumentException if path includes invalid elements
     */
    @SuppressWarnings("rawtypes")
    private WidgetProperty<?> getPropertyByPath(final String path_name) throws IllegalArgumentException
    {
        final String[] path = path_name.split("\\.");
        WidgetProperty<?> property = null;
        for (String item : path)
        {   // Does item refer to array element?
            final String name;
            final int index;
            final int braces = item.indexOf('[');
            if (braces >= 0)
            {
                if (! item.endsWith("]"))
                    throw new IllegalArgumentException("Missing ']' for end of array element");
                name = item.substring(0, braces);
                index = Integer.parseInt(item.substring(braces+1, item.length() - 1));
            }
            else
            {
                name = item;
                index = -1;
            }
            // Get property for the 'name'.
            // For first item, from widget. Later descent into structure.
            if (property == null)
                property = property_map.get(name);
            else if (property instanceof StructuredWidgetProperty)
                property = ((StructuredWidgetProperty)property).getElement(name);
            else
                throw new IllegalArgumentException("Cannot locate '" + name + "' for '" + path_name + "'");
            // Fetch individual array element?
            if (index >= 0)
                if (property instanceof ArrayWidgetProperty)
                    property = ((ArrayWidgetProperty)property).getElement(index);
                else
                    throw new IllegalArgumentException("'" + name + "' of '" + path_name + "' it not an array");
        }
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
    public final <PT> PT getPropertyValue(final WidgetPropertyDescriptor<PT> property_description)
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
     *  @param name Property name, may also be path like "struct_prop.array_prop[2].element"
     *  @param <TYPE> Data is cast to the receiver's type
     *  @return Value of the property
     *  @throws IllegalArgumentException if property is unknown
     *  @throws IndexOutOfBoundsException for array access beyond last element
     *
     */
    @SuppressWarnings("unchecked")
    public final <TYPE> TYPE getPropertyValue(final String name)
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
    public final <PT> void setPropertyValue(final WidgetPropertyDescriptor<PT> property_description,
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
    public final void setPropertyValue(final String name,
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
        final Optional<Widget> the_parent = getParent();
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
    public final void setUserData(final String key, final Object data)
    {
        user_data.put(key, data);
    }

    /** @param key Key
     *  @param <TYPE> Data is cast to the receiver's type
     *  @return User data associated with key, or <code>null</code>
     *  @see #setUserData(String, Object)
     */
    @SuppressWarnings("unchecked")
    public final <TYPE> TYPE getUserData(final String key)
    {
        if (key == null)
        {   // Debug gimmick:
            // null is not supported as valid key,
            // but triggers dump of all user properties
            logger.info(this + " user data: " + user_data.entrySet());
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
    public final <TYPE> TYPE clearUserData(final String key)
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
