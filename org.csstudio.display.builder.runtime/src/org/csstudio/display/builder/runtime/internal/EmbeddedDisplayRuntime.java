/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.internal;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayFile;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetName;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.runtime.RuntimeUtil;
import org.csstudio.display.builder.runtime.WidgetRuntime;

/** Runtime for the EmbeddedDisplayWidget
 *
 *  <p>Initializes display-wide facilities
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EmbeddedDisplayRuntime extends WidgetRuntime<EmbeddedDisplayWidget>
{
    /** Model for the embedded content */
    private volatile DisplayModel content_model;

    public EmbeddedDisplayRuntime(final EmbeddedDisplayWidget widget)
    {
        super(widget);
    }

    /** Start: Connect to PVs, ...
     *  @throws Exception on error
     */
    @Override
    public void start() throws Exception
    {
        super.start();

        // Register for changes, and load initial content
        widget.addPropertyListener(displayFile, (final PropertyChangeEvent event) -> loadContent());
        loadContent();
    }

    private void loadContent()
    {
        try
        {
            // Load model for displayFile, allowing lookup relative to this widget's model
            final DisplayModel model = RuntimeUtil.getDisplayModel(widget);
            final String parent_display = model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
            final String display_file = widget.getPropertyValue(displayFile);
            content_model = RuntimeUtil.loadModel(parent_display, display_file);
            // Adjust model name to reflect source file
            content_model.setPropertyValue(widgetName, "EmbeddedDisplay " + display_file);

            // Attach toolkit to embedded model
            final ToolkitRepresentation<Object, ?> toolkit = RuntimeUtil.getToolkit(RuntimeUtil.getDisplayModel(widget));

            // Place hint in embedded model that it is embedded by this widget
            content_model.setUserData(DisplayModel.USER_DATA_EMBEDDING_WIDGET, widget);

            // Represent on UI thread
            toolkit.execute(() -> representContent(toolkit));
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Failed to load embedded display", ex);
        }
    }

    private void representContent(final ToolkitRepresentation<Object, ?> toolkit)
    {
        try
        {
            final Object parent = widget.getUserData(EmbeddedDisplayWidget.USER_DATA_EMBEDDED_DISPLAY_CONTAINER);
            toolkit.representModel(parent, content_model);

            // Start runtimes of child widgets off the UI thread
            RuntimeUtil.getExecutor().execute(() -> RuntimeUtil.startRuntime(content_model));
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Failed to represent embedded display", ex);
        }
    }

    @Override
    public void stop()
    {
        RuntimeUtil.stopRuntime(content_model);
        super.stop();
    }
}
