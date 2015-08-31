/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import java.beans.PropertyChangeEvent;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.epics.vtype.VType;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextUpdateRepresentation extends JFXBaseRepresentation<Label, TextUpdateWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile String value_text = "<?>";

    public TextUpdateRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                                    final TextUpdateWidget model_widget)
    {
        super(toolkit, model_widget);
        value_text = "<" + model_widget.behaviorPVName().getValue() + ">";
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
        model_widget.positionWidth().addPropertyListener(this::styleChanged);
        model_widget.positionHeight().addPropertyListener(this::styleChanged);
        model_widget.displayForegroundColor().addPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addPropertyListener(this::styleChanged);
        model_widget.displayFont().addPropertyListener(this::styleChanged);
        model_widget.runtimeValue().addPropertyListener(this::contentChanged);
    }

    private void styleChanged(final PropertyChangeEvent event)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
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
        if (dirty_style.checkAndClear())
        {
            jfx_node.setPrefSize(model_widget.positionWidth().getValue(),
                                 model_widget.positionHeight().getValue());

            Color color = JFXUtil.convert(model_widget.displayForegroundColor().getValue());
            jfx_node.setTextFill(color);
            color = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
            jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));
        }
        if (dirty_content.checkAndClear())
            jfx_node.setText(value_text);
    }
}
