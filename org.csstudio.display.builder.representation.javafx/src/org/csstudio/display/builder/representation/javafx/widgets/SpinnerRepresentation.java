package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.SpinnerWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import javafx.geometry.Insets;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Amanda Carpenter
 */
public class SpinnerRepresentation extends JFXBaseRepresentation<Spinner<Double>, SpinnerWidget>
{
    /** Is user actively editing the content, so updates should be suppressed? */
    private volatile boolean active = false;

    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_colors = new DirtyFlag();
    //since foreground&background color are commonly changed during runtime by
    //rules/scripts, their changes are handled separately from sizing, buttons, etc.,
    //which are unlikely to change outside of edit mode
    private final DirtyFlag dirty_content = new DirtyFlag();

    protected volatile Integer value = 0;
    protected volatile Integer max = 10;
    protected volatile Integer min = 0;

    //TODO: problem? when writing type-in values, SVF writes out-of-limits value before correcting
    @Override
    protected final Spinner<Double> createJFXNode() throws Exception
    {
        SpinnerValueFactory<Double> svf = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max);
        final Spinner<Double> spinner = new Spinner();
        spinner.setValueFactory(svf);
        final String color = JFXUtil.webRGB(model_widget.displayForegroundColor().getValue());
        spinner.setStyle("-fx-text-fill:" + color);
        final Color background = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
        spinner.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
        if (model_widget.displayButtonsOnLeft().getValue())
            spinner.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL);
        spinner.setEditable(true);
        return spinner;
    }


    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayButtonsOnLeft().addPropertyListener(this::styleChanged);

        model_widget.displayForegroundColor().addUntypedPropertyListener(this::colorsChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::colorsChanged);

        model_widget.displayFormat().addUntypedPropertyListener(this::contentChanged);
        model_widget.displayPrecision().addUntypedPropertyListener(this::contentChanged);
        model_widget.runtimeValue().addUntypedPropertyListener(this::contentChanged);
   }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    private void colorsChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_colors.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        //TODO: stub
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_style.checkAndClear())
        {
            jfx_node.setPrefWidth(model_widget.positionWidth().getValue());
            jfx_node.setPrefHeight(model_widget.positionHeight().getValue());
            int x = jfx_node.getStyleClass().indexOf(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL);
            if (model_widget.displayButtonsOnLeft().getValue())
            {
                if (x < 0)
                    jfx_node.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL);
            }
            else if (x > 0)
                jfx_node.getStyleClass().remove(x);
        }
        if (dirty_colors.checkAndClear())
        {
            final String color = JFXUtil.webRGB(model_widget.displayForegroundColor().getValue());
            jfx_node.editorProperty().getValue().setStyle("-fx-text-fill:" + color);
            final Color background = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.editorProperty().getValue().setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        if (!active && dirty_content.checkAndClear())
        {
        }
    }
}
