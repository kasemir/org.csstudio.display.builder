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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.widgets.SymbolWidget;
import org.csstudio.javafx.Styles;
import org.diirt.util.array.ListInt;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VBoolean;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VEnumArray;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VString;
import org.diirt.vtype.VType;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 19 Jun 2017
 */
public class SymbolRepresentation extends RegionBaseRepresentation<BorderPane, SymbolWidget> {

    private static Image defaultSymbol = null;

    private int                                  arrayIndex            = 0;
    private final DirtyFlag                      dirtyContent          = new DirtyFlag();
    private final DirtyFlag                      dirtyGeometry         = new DirtyFlag();
    private final DirtyFlag                      dirtyStyle            = new DirtyFlag();
    private final DirtyFlag                      dirtyValue            = new DirtyFlag();
    private volatile boolean                     enabled               = true;
    private int                                  imageIndex            = -1;
    private final List<Image>                    imagesList            = new ArrayList<>(4);
    private final Map<String, Image>             imagesMap             = new TreeMap<>();
    private final WidgetPropertyListener<String> imagePropertyListener = this::imageChanged;
    private final AtomicBoolean                  updatingValue         = new AtomicBoolean(false);

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyGeometry.checkAndClear() ) {

            value = model_widget.propVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isVisible()) ) {
                jfx_node.setVisible((boolean) value);
            }

            jfx_node.setLayoutX(model_widget.propX().getValue());
            jfx_node.setLayoutY(model_widget.propY().getValue());
            jfx_node.setPrefWidth(model_widget.propWidth().getValue());
            jfx_node.setPrefHeight(model_widget.propHeight().getValue());

        }

        if ( dirtyContent.checkAndClear() ) {

            value = model_widget.propArrayIndex().getValue();

            if ( !Objects.equals(value, arrayIndex) ) {
                arrayIndex = Math.max(0, (int) value);
            }

            imageIndex = Math.min(Math.max(imageIndex, 0), imagesList.size() - 1);

            ((ImageView) jfx_node.getCenter()).setImage(( imageIndex >= 0 ) ? imagesList.get(imageIndex) : getDefaultSymbol());

        }

        if ( dirtyStyle.checkAndClear() ) {

            value = model_widget.propPreserveRatio().getValue();

            if ( !Objects.equals(value, ((ImageView) jfx_node.getCenter()).isPreserveRatio()) ) {
                ((ImageView) jfx_node.getCenter()).setPreserveRatio((boolean) value);
            }

            value = model_widget.propEnabled().getValue();

            if ( !Objects.equals(value, enabled) ) {

                enabled = (boolean) value;

                Styles.update(jfx_node, Styles.NOT_ENABLED, !enabled);

            }

        }

        if ( dirtyValue.checkAndClear() && updatingValue.compareAndSet(false, true) ) {

            try {

                value = model_widget.runtimePropValue().getValue();

                if ( value != null ) {
                    if ( value instanceof VBoolean ) {
                        imageIndex = ((VBoolean) value).getValue() ? 1 : 0;
                    } else if ( value instanceof VString ) {
                        try {
                            imageIndex = Integer.parseInt(((VString) value).getValue());
                        } catch ( NumberFormatException nfex ) {
                            logger.log(Level.FINE, "Failure parsing the string value: {0} [{1}].", new Object[] { ((VString) value).getValue(), nfex.getMessage() });
                        }
                    } else if ( value instanceof VNumber ) {
                        imageIndex = ((VNumber) value).getValue().intValue();
                    } else if ( value instanceof VEnum ) {
                        imageIndex = ((VEnum) value).getIndex();
                    } else if ( value instanceof VNumberArray ) {

                        ListNumber array = ((VNumberArray) value).getData();

                        if ( array.size() > 0 ) {
                            imageIndex = array.getInt(Math.min(arrayIndex, array.size() - 1));
                        }

                    } else if ( value instanceof VEnumArray ) {

                        ListInt array = ((VEnumArray) value).getIndexes();

                        if ( array.size() > 0 ) {
                            imageIndex = array.getInt(Math.min(arrayIndex, array.size() - 1));
                        }

                    }
                }

            } finally {
                updatingValue.set(false);
            }

            imageIndex = Math.min(Math.max(imageIndex, 0), imagesList.size() - 1);

            ((ImageView) jfx_node.getCenter()).setImage(( imageIndex >= 0 ) ? imagesList.get(imageIndex) : getDefaultSymbol());

        }

    }

    @Override
    protected BorderPane createJFXNode ( ) throws Exception {

        updateSymbols(model_widget.propSymbols().getValue());

        BorderPane symbol = new BorderPane();
        ImageView imageView = new ImageView();

        imageView.setImage(imagesList.isEmpty() ? getDefaultSymbol() : imagesList.get(0));
        imageView.setPreserveRatio(model_widget.propPreserveRatio().getValue());
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.fitHeightProperty().bind(symbol.prefHeightProperty());
        imageView.fitWidthProperty().bind(symbol.prefWidthProperty());

        symbol.setCenter(imageView);

        enabled = model_widget.propEnabled().getValue();

        Styles.update(symbol, Styles.NOT_ENABLED, !enabled);

        return symbol;

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propPVName().addPropertyListener(this::contentChanged);
        model_widget.propSymbols().addPropertyListener(this::imagesChanged);

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propArrayIndex().addUntypedPropertyListener(this::contentChanged);
        model_widget.propEnabled().addUntypedPropertyListener(this::styleChanged);
        model_widget.propPreserveRatio().addUntypedPropertyListener(this::styleChanged);

        if ( toolkit.isEditMode() ) {
            dirtyValue.checkAndClear();
        } else {
            model_widget.runtimePropValue().addPropertyListener(this::valueChanged);
        }

    }

    private void contentChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyContent.mark();
        toolkit.scheduleUpdate(this);
    }

    private void geometryChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyGeometry.mark();
        toolkit.scheduleUpdate(this);
    }

    private Image getDefaultSymbol() {

        if ( defaultSymbol == null ) {
            defaultSymbol = loadSymbol(SymbolWidget.DEFAULT_SYMBOL);
        }

        return defaultSymbol;

    }

    private void imageChanged ( final WidgetProperty<String> property, final String oldValue, final String newValue ) {

        updateSymbols(model_widget.propSymbols().getValue());

        dirtyContent.mark();
        toolkit.scheduleUpdate(this);

    }

    private void imagesChanged ( final WidgetProperty<List<WidgetProperty<String>>> property, final List<WidgetProperty<String>> oldValue, final List<WidgetProperty<String>> newValue ) {

        updateSymbols(model_widget.propSymbols().getValue());

        if ( oldValue != null ) {
            oldValue.stream().forEach(p -> p.removePropertyListener(imagePropertyListener));
        }

        if ( newValue != null ) {
            newValue.stream().forEach(p -> p.addPropertyListener(imagePropertyListener));
        }

        dirtyContent.mark();
        toolkit.scheduleUpdate(this);

    }

    /**
     * Load the image for the given file name.
     *
     * @param fileName The file name of the image to be loaded.
     * @return The loaded {@link Image}, or {@code null} if no image was loaded.
     */
    private Image loadSymbol ( String fileName ) {

        String imageFileName;

        try {

            String expandedFileName = MacroHandler.replace(model_widget.getMacrosOrProperties(), fileName);

            //  Resolve new image file relative to the source widget model (not 'top'!).
            //  Get the display model from the widget tied to this representation.
            final DisplayModel widgetModel = model_widget.getDisplayModel();

            // Resolve the image path using the parent model file path.
            imageFileName = ModelResourceUtil.resolveResource(widgetModel, expandedFileName);

        } catch ( Exception ex ) {

            logger.log(Level.WARNING, "Failure resolving image path: {0} [{1}].", new Object[] { fileName, ex.getMessage() });

            return null;

        }

        try {
            //  Open the image from the stream created from the resource file.
            return new Image(ModelResourceUtil.openResourceStream(imageFileName));
        } catch ( Exception ex ) {

            logger.log(Level.WARNING, "Failure loading image: ({0}) {1} [{2}].", new Object[] { fileName, imageFileName, ex.getMessage() });

            return null;

        }

    }

    private void styleChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyStyle.mark();
        toolkit.scheduleUpdate(this);
    }

    private void updateSymbols ( List<WidgetProperty<String>> fileNames ) {

        if ( fileNames == null ) {
            logger.log(Level.WARNING, "Empty list of file names.");
        } else {

            imagesList.clear();

            fileNames.stream().forEach(f -> {

                String fileName = f.getValue();
                Image image = imagesMap.get(fileName);

                if ( image == null ) {

                    image = loadSymbol(fileName);

                    if ( image != null ) {
                        imagesMap.put(fileName, image);
                    }

                }

                if ( image != null ) {
                    imagesList.add(image);
                }

            });

            Set<String> toBeRemoved = imagesMap.keySet().stream().filter(f -> !imagesList.contains(imagesMap.get(f))).collect(Collectors.toSet());

            toBeRemoved.stream().forEach(f -> imagesMap.remove(f));

            imageIndex = Math.min(Math.max(imageIndex, 0), imagesList.size() - 1);

        }

    }

    private void valueChanged ( final WidgetProperty<? extends VType> property, final VType oldValue, final VType newValue ) {
        dirtyValue.mark();
        toolkit.scheduleUpdate(this);
    }

}
