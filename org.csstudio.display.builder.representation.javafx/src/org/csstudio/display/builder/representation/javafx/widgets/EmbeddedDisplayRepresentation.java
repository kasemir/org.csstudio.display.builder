/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget.Resize;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.transform.Scale;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EmbeddedDisplayRepresentation extends RegionBaseRepresentation<ScrollPane, EmbeddedDisplayWidget>
{
    private final DirtyFlag dirty_sizes = new DirtyFlag();
    private final DirtyFlag dirty_info = new DirtyFlag();

    /** Inner group that holds child widgets */
    private Group inner;
    private Scale zoom;
    private ScrollPane scroll;
    private Label info = null;
    private volatile String info_text = "";

    @Override
    public ScrollPane createJFXNode() throws Exception
    {
        // inner.setScaleX() and setScaleY() zoom from the center
        // and not the top-left edge, requiring adjustments to
        // inner.setTranslateX() and ..Y() to compensate.
        // Using a separate Scale transformation does not have that problem.
        // See http://stackoverflow.com/questions/10707880/javafx-scale-and-translate-operation-results-in-anomaly
        inner = new Group();
        inner.getTransforms().add(zoom = new Scale());

        scroll = new ScrollPane(inner);
        // Panning tends to 'jerk' the content when clicked
        // scroll.setPannable(true);


        if (toolkit.isEditMode())
        {
            info = new Label();
            inner.getChildren().add(info);
        }
        else
        {
            // Hide border around the ScrollPane
            // Details changed w/ JFX versions, see
            // http://stackoverflow.com/questions/17540137/javafx-scrollpane-border-and-background/17540428#17540428
            scroll.setStyle("-fx-background-color:transparent;");
        }

        model_widget.setUserData(EmbeddedDisplayWidget.USER_DATA_EMBEDDED_DISPLAY_CONTAINER, inner);

        return scroll;
    }

    @Override
    protected Parent getChildParent(final Parent parent)
    {
        return inner;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizesChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::sizesChanged);
        model_widget.displayResize().addUntypedPropertyListener(this::sizesChanged);
        model_widget.runtimeScale().addUntypedPropertyListener(this::sizesChanged);
        if (info != null)
        {
            model_widget.displayFile().addUntypedPropertyListener(this::infoChanged);
            model_widget.displayGroupName().addUntypedPropertyListener(this::infoChanged);
            // Initial info
            infoChanged(null, null, null);
        }
    }

    private void sizesChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_sizes.mark();
        toolkit.scheduleUpdate(this);
    }

    private void infoChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        String file = model_widget.displayFile().getValue();
        String group = model_widget.displayGroupName().getValue();
        if (group.isEmpty())
            info_text = file;
        else
            info_text = file + " (" + group + ")";
        dirty_info.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_sizes.checkAndClear())
        {
            final Integer width = model_widget.positionWidth().getValue();
            final Integer height = model_widget.positionHeight().getValue();
            scroll.setPrefSize(width, height);

            final Resize resize = model_widget.displayResize().getValue();
            if (resize == Resize.None)
            {
                zoom.setX(1.0);
                zoom.setY(1.0);
                scroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
                scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
            }
            else if (resize == Resize.ResizeContent)
            {
                final double factor = model_widget.runtimeScale().getValue();
                zoom.setX(factor);
                zoom.setY(factor);
                scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
            }
            else // SizeToContent
            {
                zoom.setX(1.0);
                zoom.setY(1.0);
                scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
            }
        }
        if (dirty_info.checkAndClear())
            info.setText(info_text);
    }
}
