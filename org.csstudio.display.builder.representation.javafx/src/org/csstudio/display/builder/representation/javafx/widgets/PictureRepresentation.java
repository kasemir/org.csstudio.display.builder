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

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/** Creates JavaFX item for model widget
 *  @author Megan Grodowitz
 */

@SuppressWarnings("nls")
public class PictureRepresentation extends JFXBaseRepresentation<Group, PictureWidget>
{
    /** Change the image border properties */
    private final DirtyFlag dirty_border = new DirtyFlag();
    /** Change the image file */
    private final DirtyFlag dirty_content = new DirtyFlag();
    /** Change the image size, rotation or preserve_ratio */
    private final DirtyFlag dirty_style = new DirtyFlag();

    private volatile Image img_loaded;
    private volatile ImageView iv;
    private volatile String img_path;
    private volatile double native_ratio = 1.0;

    private static final Color border_color = Color.GRAY;
    private static final int inset = 0;
    private static final int border_width = 1;
    private volatile Rectangle border = new Rectangle();

    private volatile Rotate rotation = new Rotate(0);
    private volatile Translate translate = new Translate(0,0);

    @Override
    public Group createJFXNode() throws Exception
    {
        iv = new ImageView();

        Group gr = new Group(border, iv);
        gr.getTransforms().addAll(translate, rotation);
        return gr;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.propWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::styleChanged);

        model_widget.propStretch().addPropertyListener(this::styleChanged);
        model_widget.propRotation().addUntypedPropertyListener(this::styleChanged);
        styleChanged(null, null, null);

        //TODO: add way to disable border or remove permanently
        borderChanged(null, null, null);

        // This is one of those weird cases where getValue calls setValue and fires the listener.
        // So register listener after getValue called
        final String img_name = model_widget.propFile().getValue();
        model_widget.propFile().addPropertyListener(this::contentChanged);
        ModelThreadPool.getExecutor().execute(() -> contentChanged(null, null, img_name));
        //ModelThreadPool.getExecutor().execute(() -> contentChanged(null, null, model_widget.displayFile().getValue()));

    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void borderChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
       dirty_border.mark();
       toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final WidgetProperty<String> property, final String old_value, final String new_value)
    //private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        //dirty_content.mark();
        // Imagine if updateChanges executes here. Mark is cleared and image updated before new image loaded.
        // Subsequent Scheduled image update would not happen.

        String base_path = new_value;
        //String base_path = model_widget.displayFile().getValue();
        //System.out.println("Picture Representation content changes to " + base_path + " on " + Thread.currentThread().getName());
        boolean load_failed = false;

        try
        {
            // TODO: load this image if the given path fails
            //final String base_path = "platform:/plugin/org.csstudio.display.representation.javafx/icons/add.png"; //$NON-NLS-1$

            // expand macros in the file name
            final String expanded_path = MacroHandler.replace(model_widget.getMacrosOrProperties(), base_path);

            // Resolve new image file relative to the source widget model (not 'top'!)
            // Get the display model from the widget tied to this representation
            final DisplayModel widget_model = model_widget.getDisplayModel();
            // Resolve the image path using the parent model file path
            img_path = ModelResourceUtil.resolveResource(widget_model, expanded_path);
        }
        catch (Exception e)
        {
            System.out.println("Failure resolving image path from base path: " + base_path);
            e.printStackTrace();
            load_failed = true;
        }

        if (!load_failed)
        {
            try
            {
                // Open the image from the stream created from the resource file
                img_loaded = new Image(ModelResourceUtil.openResourceStream(img_path));
                native_ratio = img_loaded.getWidth() / img_loaded.getHeight();
            }
            catch (Exception e)
            {
                System.out.println("Failure loading image file:" + img_path);
                e.printStackTrace();
                load_failed = true;
            }
        }

        if (load_failed)
        {
            final String dflt_img = PictureWidget.default_pic;
            try
            {
                // Open the image from the stream created from the resource file
                img_loaded = new Image(ModelResourceUtil.openResourceStream(dflt_img));
                native_ratio = img_loaded.getWidth() / img_loaded.getHeight();
            }
            catch (Exception e)
            {
                System.out.println("Failure loading default image file:" + dflt_img);
                e.printStackTrace();
                load_failed = true;
            }

        }

        // Resize/reorient in case we are preserving aspect ratio and changed native_ratio
        dirty_style.mark();
        // Switch to the new image
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }


    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_border.checkAndClear())
        {
            border.relocate(border_width, inset);
            border.setFill(Color.TRANSPARENT);
//            border.setStroke(border_color);
            border.setStroke(null);
            border.setStrokeWidth(border_width);
            border.setStrokeType(StrokeType.INSIDE);
        }
        if (dirty_content.checkAndClear())
        {
            //System.out.println("update change to img path at " + img_path + " on thread " + Thread.currentThread().getName());}
            iv.setImage(img_loaded);
            // We handle ratio internally, do not let ImageView do that
            iv.setPreserveRatio(false);
            jfx_node.setCache(true);
        }
        if (dirty_style.checkAndClear())
        {
            Integer widg_w = model_widget.propWidth().getValue();
            Integer widg_h = model_widget.propHeight().getValue();
            Integer pic_w = widg_w;
            Integer pic_h = widg_h;

            // preserve aspect ratio
            if (!model_widget.propStretch().getValue())
            {

                double w_prime = pic_h * native_ratio;
                double h_prime = pic_w / native_ratio;
                if (w_prime < pic_w)
                {
                    pic_h = (int) Math.round(h_prime);
                }
                else if (h_prime < pic_h)
                {
                    pic_w = (int) Math.round(w_prime);
                }
            }

            Integer final_pic_w, final_pic_h;

            double cos_a = Math.cos(Math.toRadians(model_widget.propRotation().getValue()));
            double sin_a = Math.sin(Math.toRadians(model_widget.propRotation().getValue()));
            double pic_bb_w = pic_w * Math.abs(cos_a) + pic_h * Math.abs(sin_a);
            double pic_bb_h = pic_w * Math.abs(sin_a) + pic_h * Math.abs(cos_a);

            double scale_fac = Math.min(widg_w / pic_bb_w, widg_h / pic_bb_h);
            if (scale_fac < 1.0)
            {
                final_pic_w = (int) Math.floor(scale_fac * pic_w);
                final_pic_h = (int) Math.floor(scale_fac * pic_h);
            }
            else {
                final_pic_w = pic_w;
                final_pic_h = pic_h;
            }

            border.setWidth(final_pic_w - 2*inset);
            border.setHeight(final_pic_h - 2*inset);

            //iv.setImage(visible ? img_loaded : null);
            iv.setFitHeight(final_pic_h);
            iv.setFitWidth(final_pic_w);

            //jfx_node.maxWidth(final_pic_w);
            //jfx_node.maxHeight(final_pic_h);

            // Rotate around the center of the resized image
            rotation.setAngle(model_widget.propRotation().getValue());
            rotation.setPivotX(final_pic_w / 2.0);
            rotation.setPivotY(final_pic_h / 2.0);

            // translate to the center of the widget
            translate.setX((widg_w - final_pic_w) / 2.0);
            translate.setY((widg_h - final_pic_h) / 2.0);
        }

    }
}
