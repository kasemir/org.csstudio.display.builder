/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.behaviorPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.properties.WritePVActionInfo;
import org.csstudio.display.builder.runtime.script.RuntimeScriptHandler;
import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVListener;
import org.csstudio.vtype.pv.PVPool;
import org.diirt.vtype.VType;

/** Runtime for a Widget.
 *
 *  <p>Connects to scripts and PVs.
 *
 *  @author Kay Kasemir
 *  @param <MW> Model widget
 */
@SuppressWarnings("nls")
public class WidgetRuntime<MW extends Widget>
{
    public static final String EXTENSION_POINT = "org.csstudio.display.builder.runtime.widgets";

    protected static final Logger logger = Logger.getLogger(WidgetRuntime.class.getName());

    /** The widget handled by this runtime */
    protected MW widget;

    // TODO Handle indication of disconnected PVs
    // The primary_pv may already be linked to alarm sensitive border,
    // but the PVs used by actions or scripts could also be disconnected,
    // and _any_ disconnected PV on a widget needs to be detected
    // and somehow indicated in representation.

    private WidgetPropertyListener<String> pv_name_listener = null;

    /** Primary widget PV for behaviorPVName property
     *  <p>SYNC on this
     */
    private Optional<PV> primary_pv = Optional.empty();

    /** Listener for <code>primary_pv</code> */
    private PVListener primary_pv_listener;

    /** PVs used by actions */
    // This is empty for most widgets, or contains very few PVs,
    // so using List with linear lookup by name and not a HashMap
    private final List<PV> pvs = new CopyOnWriteArrayList<>();

    /** Handlers for widget's behaviorScripts property */
    private final List<RuntimeScriptHandler> script_handlers = new CopyOnWriteArrayList<>();

    /** PVListener that updates 'value' property with received VType */
    protected static class PropertyUpdater implements PVListener
    {
        private final WidgetProperty<VType> property;

        /** @param property Widget property to update with values received from PV */
        public PropertyUpdater(final WidgetProperty<VType> property)
        {
            this.property = property;
            // Send initial 'disconnected' update so widget shows
            // disconnected state until the first value arrives
            disconnected(null);
        }

        @Override
        public void valueChanged(final PV pv, final VType value)
        {
            property.setValue(value);
        }

        @Override
        public void permissionsChanged(final PV pv, final boolean readonly)
        {
            // NOP
        }

        @Override
        public void disconnected(final PV pv)
        {
            property.setValue(null);
        }
    };

    /** Listener to "pv_name" property. Connects/re-connects PV */
    private class PVNameListener implements WidgetPropertyListener<String>
    {
        @Override
        public void propertyChanged(final WidgetProperty<String> name_property,
                                    final String old_name, final String pv_name)
        {
            // In case already connected...
            disconnectPV();

            if (pv_name.isEmpty())
                return;

            logger.log(Level.FINER, "Connecting {0} to {1}",  new Object[] { widget, pv_name });
            // Create listener, which marks the value as disconnected
            primary_pv_listener = new PropertyUpdater(widget.getProperty(runtimeValue));
            // Then connect PV, which either gets a value soon,
            // or may throw exception -> widget already shows disconnected
            try
            {
                final PV pv = PVPool.getPV(pv_name);
                synchronized (this)
                {
                    primary_pv = Optional.of(pv);
                }
                pv.addListener(primary_pv_listener);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "Error connecting PV " + pv_name, ex);
            }
        }
    };

    // initialize() could be the constructor, but
    // instantiation from Eclipse registry requires
    // zero-arg constructor

    /** Construct runtime
     *  @param widget Model widget
     */
    public void initialize(final MW widget)
    {
        this.widget = widget;
        widget.setUserData(Widget.USER_DATA_RUNTIME, this);
    }

    /** Start: Connect to PVs, start scripts
     *  @throws Exception on error
     */
    public void start() throws Exception
    {
        // Update "value" property from primary PV, if defined
        final Optional<WidgetProperty<String>> name = widget.checkProperty(behaviorPVName);
        final Optional<WidgetProperty<VType>> value = widget.checkProperty(runtimeValue);

        if (name.isPresent() &&  value.isPresent())
        {
            pv_name_listener = new PVNameListener();
            name.get().addPropertyListener(pv_name_listener);
            // Initial connection
            pv_name_listener.propertyChanged(name.get(), null, name.get().getValue());
        }

        // Prepare action-related PVs
        for (final ActionInfo action : widget.behaviorActions().getValue())
        {
            if (action instanceof WritePVActionInfo)
            {
                final String pv_name = ((WritePVActionInfo) action).getPV();
                final String expanded = MacroHandler.replace(widget.getMacrosOrProperties(), pv_name);
                pvs.add(PVPool.getPV(expanded));
            }
        }

        // Start scripts in pool because Jython setup is expensive
        RuntimeUtil.getExecutor().execute(this::startScripts);
    }

    /** Start Scripts */
    private void startScripts()
    {
        for (final ScriptInfo script_info : widget.behaviorScripts().getValue())
        {
            try
            {
                script_handlers.add(new RuntimeScriptHandler(widget, script_info));
            }
            catch (final Exception ex)
            {
                logger.log(Level.WARNING,
                    "Widget " + widget.getName() + " script " + script_info.getFile() + " failed to initialize", ex);
            }
        }
    }

    /** Write a value to the primary PV
     *  @param value
     */
    public void writePrimaryPV(final Object value)
    {
        try
        {
            final PV pv = primary_pv.orElse(null);
            if (pv == null)
                throw new Exception("No PV");
            primary_pv.get().write(value);
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING,
                "Widget " + widget.getName() + " write error for value " + value, ex);
        }
    }

    /** Write a value to a PV
     *  @param pv_name Name of PV to write, may contain macros
     *  @param value Value to write
     *  @throws Exception on error
     */
    public void writePV(final String pv_name, final Object value) throws Exception
    {
        final String expanded = MacroHandler.replace(widget.getMacrosOrProperties(), pv_name);
        for (final PV pv : pvs)
            if (pv.getName().equals(expanded))
            {
                try
                {
                    pv.write(value);
                }
                catch (final Exception ex)
                {
                    throw new Exception("Failed to write " + value + " to PV " + expanded, ex);
                }
                return;
            }
        throw new Exception("Unknown PV '" + pv_name + "' (expanded: '" + expanded + "')");
    }

    /** Disconnect the primary PV
     *
     *  <p>OK to call when there was no PV
     */
    private void disconnectPV()
    {
        final PV pv;
        synchronized (this)
        {
            pv = primary_pv.orElse(null);
            primary_pv = Optional.empty();
            if (pv == null)
                return;
        }
        pv.removeListener(primary_pv_listener);
        PVPool.releasePV(pv);
    }

    /** Stop: Disconnect PVs, ... */
    public void stop()
    {
        for (final PV pv : pvs)
            PVPool.releasePV(pv);

        disconnectPV();
        if (pv_name_listener != null)
        {
            widget.getProperty(behaviorPVName).removePropertyListener(pv_name_listener);
            pv_name_listener = null;
        }

        for (final RuntimeScriptHandler handler : script_handlers)
            handler.shutdown();
    }
}

