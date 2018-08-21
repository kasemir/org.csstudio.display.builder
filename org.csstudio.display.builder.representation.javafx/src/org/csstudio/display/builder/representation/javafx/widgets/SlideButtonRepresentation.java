/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.List;
import java.util.logging.Level;

import org.controlsfx.control.ToggleSwitch;
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.SlideButtonWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.Styles;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VType;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;


/**
 * JavaFX representation of the SlideButton model.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 21 Aug 2018
 */
public class SlideButtonRepresentation extends RegionBaseRepresentation<HBox, SlideButtonWidget> {

    /*
     * This code come as close as possible from BoolButtonRepresentation.
     */

    private final DirtyFlag dirty_representation = new DirtyFlag();
    private final DirtyFlag dirty_enablement     = new DirtyFlag();
    private final DirtyFlag dirty_value          = new DirtyFlag();

    /**
     * State: 0 or 1
     */
    private volatile int     on_state = 1;
    private volatile int     use_bit  = 0;
    private volatile Integer rt_value = 0;

    private volatile ToggleSwitch button;
    private volatile Label        label;

    private volatile Color    foreground;
    private volatile String   state_colors;
    private volatile String[] state_labels;
    private volatile String   value_label;

    @Override
    public HBox createJFXNode ( ) throws Exception {

        button = new ToggleSwitch();

        button.setMinSize(37, 20);
        button.setPrefSize(37, 20);
        button.setGraphicTextGap(0);
        button.setMnemonicParsing(false);

        if ( !toolkit.isEditMode() ) {
            button.selectedProperty().addListener( ( p, o, n ) -> handleSlide());
        }

        label = new Label();

        label.setMaxWidth(Double.MAX_VALUE);
        label.setMnemonicParsing(false);
        HBox.setHgrow(label, Priority.ALWAYS);

        HBox hbox = new HBox(6, button, label);

        hbox.setAlignment(Pos.CENTER_RIGHT);

        return hbox;

    }

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        boolean update_value = dirty_value.checkAndClear();

        if ( dirty_representation.checkAndClear() ) {

            final int w = model_widget.propWidth().getValue();
            final int h = model_widget.propHeight().getValue();

            jfx_node.setPrefSize(w, h);

            button.setStyle(state_colors);

            label.setFont(JFXUtil.convert(model_widget.propFont().getValue()));
            label.setTextFill(foreground);

            update_value = true;

        }

        if ( dirty_enablement.checkAndClear() ) {

            final boolean enabled = model_widget.propEnabled().getValue() && model_widget.runtimePropPVWritable().getValue();

            button.setDisable(!enabled);
            Styles.update(label, Styles.NOT_ENABLED, !enabled);

        }

        if ( update_value ) {
            label.setText(value_label);
            button.setSelected(on_state == 1);
        }

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        representationChanged(null, null, null);

        model_widget.propWidth().addUntypedPropertyListener(this::representationChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::representationChanged);
        model_widget.propOffLabel().addUntypedPropertyListener(this::representationChanged);
        model_widget.propOffColor().addUntypedPropertyListener(this::representationChanged);
        model_widget.propOnLabel().addUntypedPropertyListener(this::representationChanged);
        model_widget.propOnColor().addUntypedPropertyListener(this::representationChanged);
        model_widget.propFont().addUntypedPropertyListener(this::representationChanged);
        model_widget.propForegroundColor().addUntypedPropertyListener(this::representationChanged);
        model_widget.propEnabled().addPropertyListener(this::enablementChanged);
        model_widget.runtimePropPVWritable().addPropertyListener(this::enablementChanged);
        model_widget.propBit().addPropertyListener(this::bitChanged);
        model_widget.runtimePropValue().addPropertyListener(this::valueChanged);

        bitChanged(model_widget.propBit(), null, model_widget.propBit().getValue());
        enablementChanged(null, null, null);
        valueChanged(null, null, model_widget.runtimePropValue().getValue());

    }

    private void bitChanged ( final WidgetProperty<Integer> property, final Integer old_value, final Integer new_value ) {

        use_bit = new_value;

        stateChanged();

    }

    private void confirm ( ) {

        final boolean prompt;

        switch ( model_widget.propConfirmDialog().getValue() ) {
            case BOTH:
                prompt = true;
                break;
            case PUSH:
                prompt = on_state == 0;
                break;
            case RELEASE:
                prompt = on_state == 1;
                break;
            case NONE:
            default:
                prompt = false;
        }

        if ( prompt ) {

            final String message = model_widget.propConfirmMessage().getValue();
            final String password = model_widget.propPassword().getValue();

            if ( password.length() > 0 ) {
                if ( toolkit.showPasswordDialog(model_widget, message, password) == null ) {
                    return;
                }
            } else if ( !toolkit.showConfirmationDialog(model_widget, message) ) {
                return;
            }

        }

        final int new_val = ( rt_value ^ ( ( use_bit < 0 ) ? 1 : ( 1 << use_bit ) ) );

        toolkit.fireWrite(model_widget, new_val);

    }

    private void enablementChanged ( final WidgetProperty<Boolean> property, final Boolean old_value, final Boolean new_value ) {
        dirty_enablement.mark();
        toolkit.scheduleUpdate(this);
    }

    private void handleSlide ( ) {
        logger.log(Level.FINE, "{0} slided", model_widget);
        Platform.runLater(this::confirm);
    }

    private void representationChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {

        foreground = JFXUtil.convert(model_widget.propForegroundColor().getValue());
        state_colors = "-db-toggle-switch-off: " + JFXUtil.webRGB(model_widget.propOffColor().getValue()) + ";"
                     + "-db-toggle-switch-on: " + JFXUtil.webRGB(model_widget.propOnColor().getValue()) + ";";
        state_labels = new String[] { model_widget.propOffLabel().getValue(), model_widget.propOnLabel().getValue() };
        value_label = state_labels[on_state];

        dirty_representation.mark();
        toolkit.scheduleUpdate(this);

    }

    private void stateChanged ( ) {

        on_state = ( ( use_bit < 0 ) ? ( rt_value != 0 ) : ( ( ( rt_value >> use_bit ) & 1 ) == 1 ) ) ? 1 : 0;
        value_label = state_labels[on_state];

        dirty_value.mark();
        toolkit.scheduleUpdate(this);

    }

    private void valueChanged ( final WidgetProperty<VType> property, final VType old_value, final VType new_value ) {

        if ( ( new_value instanceof VEnum ) && model_widget.propLabelsFromPV().getValue() ) {

            final List<String> labels = ( (VEnum) new_value ).getLabels();

            if ( labels.size() == 2 ) {
                model_widget.propOffLabel().setValue(labels.get(0));
                model_widget.propOnLabel().setValue(labels.get(1));
            }

        }

        rt_value = VTypeUtil.getValueNumber(new_value).intValue();

        stateChanged();

    }

}
