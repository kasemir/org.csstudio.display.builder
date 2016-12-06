/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.preferences;

import static org.csstudio.display.builder.rcp.Plugin.logger;

import java.io.IOException;
import java.util.logging.Level;

import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.display.builder.rcp.Plugin;
import org.csstudio.display.builder.runtime.RuntimePlugin;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/** Preference page that is contributed to the Preferences dialog.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class DisplayPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage
{
    private final IPersistentPreferenceStore model_prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, ModelPlugin.ID);
    private final IPersistentPreferenceStore runtime_prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, RuntimePlugin.ID);
    private final IPreferenceStore rcp_prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, Plugin.ID);

    public DisplayPreferencePage()
    {
        super(GRID);
        // This one page configures preferences in various plugins, i.e. in different preference scopes.
        // Make rcp_prefs the default, and field editors then pick a different one as needed.
        setPreferenceStore(rcp_prefs);
        setDescription("After changes, a restart is required");
    }

    @Override
    public void init(IWorkbench workbench)
    {
        // NOP
    }

    @Override
    public void createFieldEditors()
    {
        addField(new StringFieldEditor(org.csstudio.display.builder.model.Preferences.READ_TIMEOUT,
                                      "File Read Timeout [ms]:", getFieldEditorParent())
        {   // Field uses pref. store that differs from the page's default store
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return model_prefs;
            }
            // Need to force doStore() for pref store that differs from page default
            @Override
            public void store()
            {
                doStore();
            }
        });

        addField(new StringFieldEditor(org.csstudio.display.builder.model.Preferences.CLASSES_FILE,
                                       "Widget Classes File:", getFieldEditorParent())
        {
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return model_prefs;
            }
            @Override
            public void store()
            {
                doStore();
            }
        });

        addField(new StringFieldEditor(org.csstudio.display.builder.model.Preferences.COLOR_FILE,
                                       "Color File:", getFieldEditorParent())
        {
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return model_prefs;
            }
            @Override
            public void store()
            {
                doStore();
            }
        });

        addField(new StringFieldEditor(org.csstudio.display.builder.model.Preferences.FONT_FILE,
                                       "Font File:", getFieldEditorParent())
        {
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return model_prefs;
            }
            @Override
            public void store()
            {
                doStore();
            }
        });

        addField(new StringFieldEditor(org.csstudio.display.builder.runtime.Preferences.PYTHON_PATH,
                                       "Jython Path:", getFieldEditorParent())
        {
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return runtime_prefs;
            }

            @Override
            public void store()
            {
                doStore();
            }
        });

        final String[] implementations = PVFactory.getImplementations();
        // Combo needs list where each entry is [ label, value ].
        // We use [ implementation, implementation ].
        final String[][] pv_factories = new String[implementations.length][];
        for (int i=0; i<implementations.length; ++i)
            pv_factories[i] = new String[] { implementations[i], implementations[i] };

        addField(new ComboFieldEditor(org.csstudio.display.builder.runtime.Preferences.PV_FACTORY,
                "PV Factory:", pv_factories, getFieldEditorParent())
        {
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return runtime_prefs;
            }

            @Override
            public void store()
            {
                doStore();
            }
        });

        // TODO Custom table editor for display Name, path, macros
        addField(new TextAreaFieldEditor(org.csstudio.display.builder.rcp.Preferences.TOP_DISPLAYS,
                                         "Top Displays:", getFieldEditorParent(), 50, 10));

        addField(new MacrosFieldEditor(org.csstudio.display.builder.model.Preferences.MACROS,
                "Macros:", getFieldEditorParent())
        {
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return model_prefs;
            }

            @Override
            public void store()
            {
                doStore();
            }
        });

        addField(new StringFieldEditor(org.csstudio.display.builder.model.Preferences.LEGACY_FONT_CALIBRATION,
                 "Legacy Font Calibration:", getFieldEditorParent())
        {
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return model_prefs;
            }

            private double getFactor()
            {
                try
                {
                    return Double.parseDouble(getStringValue());
                }
                catch (NumberFormatException ex)
                {
                    return Double.NaN;
                }
            }

            @Override
            protected boolean doCheckState()
            {
                final double factor = getFactor();
                if (factor > 0.0)
                    return true;
                setErrorMessage("Invalid font scaling, must be > 0");
                return false;
            }

            @Override
            public void store()
            {
                doStore();
            }
        });

        addField(new BooleanFieldEditor(org.csstudio.display.builder.rcp.Preferences.SHOW_RUNTIME_STACKS,
                "Show Runtime Perspective Placeholders", getFieldEditorParent()));
    }

    @Override
    public boolean performOk()
    {
        if (! super.performOk())
            return false;
        // Page is associated with rcp_prefs and saves them,
        // but need to directly save the other 2 pref stores
        try
        {
            model_prefs.save();
        }
        catch (IOException ex)
        {
            logger.log(Level.WARNING, "Cannot write model preferences", ex);
        }
        try
        {
            runtime_prefs.save();
        }
        catch (IOException ex)
        {
            logger.log(Level.WARNING, "Cannot write runtime preferences", ex);
        }
        return true;
    }
}