package org.csstudio.display.builder.editor.rcp;

import org.csstudio.display.builder.editor.WidgetSelectionHandler;
import org.csstudio.display.builder.editor.properties.PropertyPanel;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.properties.IPropertySheetPage;

public class DisplayPropertyPage extends Page implements IPropertySheetPage
{
    private Composite top;
    private final PropertyPanel property_panel;

    public DisplayPropertyPage(final WidgetSelectionHandler widget_selection,
                               final UndoableActionManager undo)
    {
        property_panel = new PropertyPanel(widget_selection, undo);
    }

    @Override
    public void createControl(final Composite parent)
    {
        top = new Composite(parent, SWT.NONE);
        top.setLayout(new FillLayout());
        final Label label = new Label(top, SWT.NONE);
        label.setText("TODO: Property panel");

        // TODO FXCanvas
    }

    @Override
    public Control getControl()
    {
        return top;
    }

    @Override
    public void setFocus()
    {
        top.setFocus();
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection)
    {
        // NOP

    }

}
