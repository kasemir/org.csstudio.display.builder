/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.util.ModelThreadPool;
import org.csstudio.display.builder.model.widgets.PictureWidget;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** Creates JavaFX item for model widget
 *  @author Megan Grodowitz
 */

@SuppressWarnings("nls")
public class PictureRepresentation extends JFXBaseRepresentation<ImageView, PictureWidget>
{
    // Change the image size
    private final DirtyFlag dirty_size = new DirtyFlag();
    // Change the image file
    private final DirtyFlag dirty_content = new DirtyFlag();
    // Change the image rotation or transparency
    private final DirtyFlag dirty_style = new DirtyFlag();
    private Pos pos;
    private volatile Image img_loaded;
    private volatile String img_path;
    private volatile Boolean visible;
    private volatile double rotation;

    @Override
    public ImageView createJFXNode() throws Exception
    {
        return new ImageView();
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::sizeChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::sizeChanged);

        visibleChanged(null, null, model_widget.displayTransparent().getValue());
        model_widget.displayTransparent().addPropertyListener(this::visibleChanged);

        // This will create a race condition with the Application thread, which calls registerListeners and then updateChanges
        // Scheduling a thread to load images may execute contentChanged simultaneous with the updateChanges gets called from main thread
        // which could *clear the dirty_content mark!!* after it is set in the contentChanged function, causing the image update never to happen.
        //final String img_name = model_widget.displayFile().getValue();
        //ModelThreadPool.getExecutor().execute(() -> contentChanged(null, null, img_name));
        ModelThreadPool.getExecutor().execute(() -> contentChanged(null, null, model_widget.displayFile().getValue()));
        //Do not add listener until after calling getValue (or it will trigger)
        model_widget.displayFile().addPropertyListener(this::contentChanged);
    }

    private void visibleChanged(final WidgetProperty<Boolean> property, final Boolean old_value, final Boolean new_value)
    {
        visible = !new_value;
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }


    private void sizeChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_size.mark();
        toolkit.scheduleUpdate(this);
    }

    // Takes place in background thread
    // Race condition exists if called simultaneous with updateChanges
    private void contentChanged(final WidgetProperty<String> property, final String old_value, final String new_value)
    {
        //dirty_content.mark();
        // Imagine if updateChanges executes here. Mark is cleared and image updated before new image loaded.
        // Subsequent Scheduled image update would not happen.

        String base_path = new_value;
        //System.out.println("Picture Representation content changes to " + base_path + " on " + Thread.currentThread().getName());

        try
        {
            // DONT DO THIS! You will trigger the listener
            //String base_path = model_widget.displayFile().getValue();

            // TODO: load this image if the given path fails
            //final String base_path = "platform:/plugin/org.csstudio.display.representation.javafx/icons/add.png"; //$NON-NLS-1$

            // expand macros in the file name
            final String expanded_path = MacroHandler.replace(model_widget.getEffectiveMacros(), base_path);

            // Resolve new image file relative to the source widget model (not 'top'!)
            // Get the display model from the widget tied to this representation
            final DisplayModel widget_model = model_widget.getDisplayModel();
            // Get the parent file path from the display model
            final String parent_file = widget_model.getUserData(DisplayModel.USER_DATA_INPUT_FILE);
            // Resolve the image path using the parent model file path
            img_path = ModelResourceUtil.resolveResource(parent_file, expanded_path);

            // Open the image from the stream created from the resource file
            img_loaded = new Image(ModelResourceUtil.openResourceStream(img_path));
            //img = new Image(ResourceUtil.openPlatformResource(expanded_path));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Don't do this. You will trigger the listener that you are in
        //if (model_widget.displayTransparent().getValue())
        //{   // Image invisible
         //   img = null;
        //}

        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_size.checkAndClear())
        {
            jfx_node.setFitHeight(model_widget.positionHeight().getValue());
            jfx_node.setFitWidth(model_widget.positionWidth().getValue());
        }
        if (dirty_style.checkAndClear())
        {
            jfx_node.setRotate(rotation);
            if (visible)
            {
                jfx_node.setImage(img_loaded);
            }
            else
            {
                jfx_node.setImage(null);
            }
        }
        if (dirty_content.checkAndClear())
        {
            //System.out.println("update change to img path at " + img_path + " on thread " + Thread.currentThread().getName());
            if (visible)
            {
                jfx_node.setImage(img_loaded);
            }
            else
            {
                jfx_node.setImage(null);
            }
            jfx_node.setPreserveRatio(false);
            jfx_node.setCache(true);
        }
    }
}
