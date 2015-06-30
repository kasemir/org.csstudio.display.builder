/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimeValue;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.epics.vtype.VType;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextUpdateRepresentation extends JFXBaseRepresentation<Label, TextUpdateWidget>
{
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile String value_text = "<?>";

    public TextUpdateRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                                    final TextUpdateWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    public Label createJFXNode() throws Exception
    {
        return new Label();
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.addPropertyListener(runtimeValue, this::contentChanged);
    }

    private void contentChanged(final PropertyChangeEvent event)
    {
        value_text = VTypeUtil.getValueString((VType)event.getNewValue(), true);
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_content.checkAndClear())
            jfx_node.setText(value_text);
    }
}
