/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.rcp.preferences;

import org.csstudio.display.builder.model.macros.MacroXMLUtil;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.representation.javafx.MacrosTable;
import org.csstudio.javafx.swt.JFX_SWT_Wrapper;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import javafx.scene.Scene;

/** Preference field editor for macros
 *
 *  <p>SWT field editor that wraps the JFX MacrosTable
 *
 *  @author Kay Kasemir
 */
public class MacrosFieldEditor extends FieldEditor
{
    private Control fxcanvas;
    private MacrosTable table;

    public MacrosFieldEditor(final String name, final String labelText,
            final Composite parent)
    {
        super(name, labelText, parent);
    }

    @Override
    protected void doFillIntoGrid(final Composite parent, final int numColumns)
    {
        getLabelControl(parent);
        getFXCanvas(parent);

        final GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessVerticalSpace = true;
        fxcanvas.setLayoutData(gd);
    }

    private void getFXCanvas(final Composite parent)
    {
        if (fxcanvas == null)
        {
            final JFX_SWT_Wrapper wrapper = new JFX_SWT_Wrapper(parent, () ->
            {
                table = new MacrosTable(new Macros());
                return new Scene(table.getNode());
            });
            fxcanvas = wrapper.getFXCanvas();
        }
    }

    @Override
    protected void adjustForNumColumns(final int numColumns)
    {
        if (fxcanvas == null)
            return;
        GridData gd = (GridData) fxcanvas.getLayoutData();
        gd.horizontalSpan = numColumns - 1;
        // We only grab excess space if we have to
        // If another field editor has more columns then
        // we assume it is setting the width.
        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
    }

    @Override
    protected void doLoad()
    {
        loadMacros(getPreferenceStore().getString(getPreferenceName()));
    }

    @Override
    protected void doLoadDefault()
    {
        loadMacros(getPreferenceStore().getDefaultString(getPreferenceName()));
    }

    private void loadMacros(final String text)
    {
        Macros macros;
        try
        {

            macros = MacroXMLUtil.readMacros(text);
        }
        catch (Exception ex)
        {
            // Ignore
            macros = new Macros();
        }
        table.setMacros(macros);
    }

    @Override
    protected void doStore()
    {
        final String text = MacroXMLUtil.toString(table.getMacros());
        getPreferenceStore().setValue(getPreferenceName(), text);
    }

    @Override
    public int getNumberOfControls()
    {
        return 2;
    }
}
