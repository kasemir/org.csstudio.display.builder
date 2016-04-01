/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.preferences;

import org.csstudio.display.builder.model.ModelPlugin;
import org.csstudio.display.builder.rcp.Plugin;
import org.csstudio.display.builder.runtime.RuntimePlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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
    private final IPreferenceStore model_prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, ModelPlugin.ID);
    private final IPreferenceStore runtime_prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, RuntimePlugin.ID);
    private final IPreferenceStore rcp_prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, Plugin.ID);

    public DisplayPreferencePage()
    {
        super(GRID);
        // This one page configures preferences in various plugins, i.e. in different preference scopes.
        // Make rcp_prefs the default, and field editors then pick a different one as needed.
        setPreferenceStore(rcp_prefs);
        setDescription("After changes, a restart is required");
    }

    public void init(IWorkbench workbench)
    {
        // NOP
    }

    public void createFieldEditors()
    {
        addField(new StringFieldEditor(org.csstudio.display.builder.model.Preferences.READ_TIMEOUT,
                                      "File Read Timeout [ms]:", getFieldEditorParent())
        {
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return model_prefs;
            }
        });

        addField(new StringFieldEditor(org.csstudio.display.builder.rcp.Preferences.COLOR_FILE,
                                       "Color File:", getFieldEditorParent()));

        addField(new StringFieldEditor(org.csstudio.display.builder.rcp.Preferences.FONT_FILE,
                                       "Font File:", getFieldEditorParent()));

        addField(new StringFieldEditor(org.csstudio.display.builder.runtime.Preferences.PYTHON_PATH,
                                       "Jython Path:", getFieldEditorParent())
        {
            @Override
            public IPreferenceStore getPreferenceStore()
            {
                return runtime_prefs;
            }
        });

        // TODO Custom table editor for display Name, path, macros
        addField(new TextAreaFieldEditor(org.csstudio.display.builder.rcp.Preferences.TOP_DISPLAYS,
                                         "Top Displays:", getFieldEditorParent(), 50, 10));
    }
}