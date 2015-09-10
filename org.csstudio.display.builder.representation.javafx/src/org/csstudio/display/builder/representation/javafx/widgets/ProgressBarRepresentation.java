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
import org.csstudio.display.builder.model.widgets.ProgressBarWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.epics.vtype.Display;
import org.epics.vtype.VType;
import org.epics.vtype.ValueUtil;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ProgressBarRepresentation extends JFXBaseRepresentation<ProgressBar, ProgressBarWidget>
{
    private final DirtyFlag dirty_look = new DirtyFlag();
    private final DirtyFlag dirty_value = new DirtyFlag();
    private volatile double percentage = 0.0;

    public ProgressBarRepresentation(final ToolkitRepresentation<Group, Node> toolkit,
                                     final ProgressBarWidget model_widget)
    {
        super(toolkit, model_widget);
    }

    @Override
    public ProgressBar createJFXNode() throws Exception
    {
        final ProgressBar bar = new ProgressBar();
        return bar;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.displayFillColor().addPropertyListener(this::lookChanged);
        model_widget.positionWidth().addPropertyListener(this::lookChanged);
        model_widget.positionHeight().addPropertyListener(this::lookChanged);
        model_widget.behaviorLimitsFromPV().addPropertyListener(this::valueChanged);
        model_widget.behaviorMinimum().addPropertyListener(this::valueChanged);
        model_widget.behaviorMaximum().addPropertyListener(this::valueChanged);
        model_widget.runtimeValue().addPropertyListener(this::valueChanged);
    }

    private void lookChanged(final PropertyChangeEvent event)
    {
        dirty_look.mark();
        toolkit.scheduleUpdate(this);
    }

    private void valueChanged(final PropertyChangeEvent event)
    {
        final VType vtype = model_widget.runtimeValue().getValue();

        final boolean limits_from_pv = model_widget.behaviorLimitsFromPV().getValue();
        double min_val = model_widget.behaviorMinimum().getValue();
        double max_val = model_widget.behaviorMaximum().getValue();
        if (limits_from_pv)
        {
            // Try display range from PV
            final Display display_info = ValueUtil.displayOf(vtype);
            if (display_info != null)
            {
                min_val = display_info.getLowerDisplayLimit();
                max_val = display_info.getUpperDisplayLimit();
            }
        }
        // Fall back to 0..100 range
        if (min_val >= max_val)
        {
            min_val = 0.0;
            max_val = 100.0;
        }

        // Determine percentage of value within the min..max range
        final double value = VTypeUtil.getValueNumber(vtype).doubleValue();
        final double percentage = (value - min_val) / (max_val - min_val);
        // Limit to 0.0 .. 1.0
        if (percentage < 0.0)
            this.percentage = 0.0;
        else if (percentage > 1.0)
            this.percentage = 1.0;
        else
            this.percentage = percentage;
        dirty_value.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_look.checkAndClear())
        {
            jfx_node.setPrefSize(model_widget.positionWidth().getValue(),
                                 model_widget.positionHeight().getValue());
            // Could clear style and use setBackground(),
            // but result is very plain.
            // Tweaking the color used by CSS keeps overall style.
            // See also http://stackoverflow.com/questions/13467259/javafx-how-to-change-progressbar-color-dynamically
            jfx_node.setStyle("-fx-accent: " + JFXUtil.webRGB(model_widget.displayFillColor().getValue()));
        }
        if (dirty_value.checkAndClear())
            jfx_node.setProgress(percentage);
    }
}
