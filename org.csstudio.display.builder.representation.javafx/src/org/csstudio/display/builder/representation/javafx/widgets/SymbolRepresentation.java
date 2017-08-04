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

import org.apache.commons.lang3.tuple.MutablePair;
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.widgets.SymbolWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
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

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 19 Jun 2017
 */
public class SymbolRepresentation extends RegionBaseRepresentation<AnchorPane, SymbolWidget> {

    private static Image defaultSymbol = null;

    private int                                  arrayIndex            = 0;
    private volatile boolean                     autoSize              = false;
    private final DirtyFlag                      dirtyContent          = new DirtyFlag();
    private final DirtyFlag                      dirtyGeometry         = new DirtyFlag();
    private final DirtyFlag                      dirtyIndex            = new DirtyFlag();
    private final DirtyFlag                      dirtyStyle            = new DirtyFlag();
    private final DirtyFlag                      dirtyValue            = new DirtyFlag();
    private volatile boolean                     enabled               = true;
    private final List<Image>                    imagesList            = new ArrayList<>(4);
    private final Map<String, Image>             imagesMap             = new TreeMap<>();
    private final WidgetPropertyListener<String> imagePropertyListener = this::imageChanged;
    private ImageView                            imageView;
    private Label                                indexLabel;
    private Circle                               indexLabelBackground;
    private Dimension2D                          maxSize               = new Dimension2D(0, 0);
    private final AtomicBoolean                  updatingValue         = new AtomicBoolean(false);

    //  ---- imageIndex property
    private IntegerProperty imageIndex =  new SimpleIntegerProperty(-1);

    private int getImageIndex ( ) {
        return imageIndex.get();
    }

    private IntegerProperty imageIndexProperty ( ) {
        return imageIndex;
    }

    private void setImageIndex ( int imageIndex ) {
        this.imageIndex.set(imageIndex);
    }


    /**
     * Compute the maximum width and height of the given {@code widget} based on
     * the its set of symbol images.
     *
     * @param widget The {@link SymbolWidget} whose size must be computed.
     * @return A not {@code null} maximum dimension of the given {@code widget}.
     */
    public static Dimension2D computeMaximumSize ( final SymbolWidget widget ) {

        MutablePair<Double, Double> maxSize = MutablePair.of(0.0, 0.0);

        widget.propSymbols().getValue().stream().forEach(s -> {

            final String imageFile = s.getValue();

            try {

                final String filename = ModelResourceUtil.resolveResource(widget.getTopDisplayModel(), imageFile);
                final Image image = new Image(ModelResourceUtil.openResourceStream(filename));

                if ( maxSize.getLeft() < image.getWidth() ) {
                    maxSize.setLeft(image.getWidth());
                }
                if ( maxSize.getRight() < image.getHeight() ) {
                    maxSize.setRight(image.getHeight());
                }

            } catch ( Exception ex ) {
                logger.log(Level.WARNING, "Cannot obtain image size for {0} [{1}].", new Object[] { imageFile, ex.getMessage() });
            }

        });

        return new Dimension2D(maxSize.getLeft(), maxSize.getRight());

    }

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyGeometry.checkAndClear() ) {

            value = model_widget.propVisible().getValue();

            if ( !Objects.equals(value, jfx_node.isVisible()) ) {
                jfx_node.setVisible((boolean) value);
            }

            value = model_widget.propAutoSize().getValue();

            if ( !Objects.equals(value, autoSize) ) {
                autoSize = (boolean) value;
            }

            if ( autoSize ) {
                model_widget.propWidth().setValue((int) Math.round(maxSize.getWidth()));
                model_widget.propHeight().setValue((int) Math.round(maxSize.getHeight()));
            }

            jfx_node.setLayoutX(model_widget.propX().getValue());
            jfx_node.setLayoutY(model_widget.propY().getValue());
            jfx_node.setPrefWidth(model_widget.propWidth().getValue());
            jfx_node.setPrefHeight(model_widget.propHeight().getValue());

        }

        if ( dirtyIndex.checkAndClear() ) {

            setImageIndex(Math.min(Math.max(model_widget.propInitialIndex().getValue(), 0), imagesList.size() - 1));

            imageView.setImage(( getImageIndex() >= 0 ) ? imagesList.get(getImageIndex()) : getDefaultSymbol());

        }

        if ( dirtyContent.checkAndClear() ) {

            value = model_widget.propArrayIndex().getValue();

            if ( !Objects.equals(value, arrayIndex) ) {
                arrayIndex = Math.max(0, (int) value);
            }

            setImageIndex(Math.min(Math.max(getImageIndex(), 0), imagesList.size() - 1));

            imageView.setImage(( getImageIndex() >= 0 ) ? imagesList.get(getImageIndex()) : getDefaultSymbol());

        }

        if ( dirtyStyle.checkAndClear() ) {

            value = model_widget.propPreserveRatio().getValue();

            if ( !Objects.equals(value, imageView.isPreserveRatio()) ) {
                imageView.setPreserveRatio((boolean) value);
            }

            value = model_widget.propEnabled().getValue();

            if ( !Objects.equals(value, enabled) ) {

                enabled = (boolean) value;

                Styles.update(jfx_node, Styles.NOT_ENABLED, !enabled);

            }

            value = model_widget.propShowIndex().getValue();

            if ( !Objects.equals(value, indexLabel.isVisible()) ) {
                indexLabel.setVisible((boolean) value);
                indexLabelBackground.setVisible((boolean) value);
            }

            if ( model_widget.propTransparent().getValue() ) {
                jfx_node.setBackground(null);
            } else {
                jfx_node.setBackground(new Background(new BackgroundFill(JFXUtil.convert(model_widget.propBackgroundColor().getValue()), CornerRadii.EMPTY, Insets.EMPTY)));
            }

        }

        if ( dirtyValue.checkAndClear() && updatingValue.compareAndSet(false, true) ) {

            int idx = -1;

            try {

                value = model_widget.runtimePropValue().getValue();

                if ( value != null ) {
                    if ( value instanceof VBoolean ) {
                        idx = ((VBoolean) value).getValue() ? 1 : 0;
                    } else if ( value instanceof VString ) {
                        try {
                            idx = Integer.parseInt(((VString) value).getValue());
                        } catch ( NumberFormatException nfex ) {
                            logger.log(Level.FINE, "Failure parsing the string value: {0} [{1}].", new Object[] { ((VString) value).getValue(), nfex.getMessage() });
                        }
                    } else if ( value instanceof VNumber ) {
                        idx = ((VNumber) value).getValue().intValue();
                    } else if ( value instanceof VEnum ) {
                        idx = ((VEnum) value).getIndex();
                    } else if ( value instanceof VNumberArray ) {

                        ListNumber array = ((VNumberArray) value).getData();

                        if ( array.size() > 0 ) {
                            idx = array.getInt(Math.min(arrayIndex, array.size() - 1));
                        }

                    } else if ( value instanceof VEnumArray ) {

                        ListInt array = ((VEnumArray) value).getIndexes();

                        if ( array.size() > 0 ) {
                            idx = array.getInt(Math.min(arrayIndex, array.size() - 1));
                        }

                    }
                }

            } finally {
                updatingValue.set(false);
            }

            setImageIndex(Math.min(Math.max(idx, 0), imagesList.size() - 1));

            imageView.setImage(( getImageIndex() >= 0 ) ? imagesList.get(getImageIndex()) : getDefaultSymbol());

        }

    }

    @Override
    protected AnchorPane createJFXNode ( ) throws Exception {

        updateSymbols(model_widget.propSymbols().getValue());

        autoSize = model_widget.propAutoSize().getValue();

        if ( autoSize ) {
            model_widget.propWidth().setValue((int) Math.round(maxSize.getWidth()));
            model_widget.propHeight().setValue((int) Math.round(maxSize.getHeight()));
        }

        AnchorPane symbol = new AnchorPane();

            BorderPane imagePane = new BorderPane();

                imageView = new ImageView();

                setImageIndex(Math.min(Math.max(model_widget.propInitialIndex().getValue(), 0), imagesList.size() - 1));

                imageView.setImage(imagesList.isEmpty() ? getDefaultSymbol() : imagesList.get(getImageIndex()));
                imageView.setPreserveRatio(model_widget.propPreserveRatio().getValue());
                imageView.setSmooth(true);
                imageView.setCache(true);
                imageView.fitHeightProperty().bind(symbol.prefHeightProperty());
                imageView.fitWidthProperty().bind(symbol.prefWidthProperty());

            imagePane.setCenter(imageView);
            imagePane.setPrefWidth(model_widget.propWidth().getValue());
            imagePane.setPrefHeight(model_widget.propHeight().getValue());

            AnchorPane.setLeftAnchor(imagePane, 0.0);
            AnchorPane.setRightAnchor(imagePane, 0.0);
            AnchorPane.setTopAnchor(imagePane, 0.0);
            AnchorPane.setBottomAnchor(imagePane, 0.0);

            BorderPane circlePane = new BorderPane();

                indexLabelBackground = new Circle(16.0, Color.BLACK.deriveColor(0.0, 0.0, 0.0, 0.75));

                indexLabelBackground.setStroke(Color.WHITE.deriveColor(0.0, 1.0, 1.0, 0.75));
                indexLabelBackground.setVisible(model_widget.propShowIndex().getValue());

            circlePane.setCenter(indexLabelBackground);

            AnchorPane.setLeftAnchor(circlePane, 0.0);
            AnchorPane.setRightAnchor(circlePane, 0.0);
            AnchorPane.setTopAnchor(circlePane, 0.0);
            AnchorPane.setBottomAnchor(circlePane, 0.0);

            indexLabel = new Label();

            indexLabel.setAlignment(Pos.CENTER);
            indexLabel.setFont(Font.font(indexLabel.getFont().getFamily(), FontWeight.BOLD, 16));
            indexLabel.setTextFill(Color.WHITE);
            indexLabel.setVisible(model_widget.propShowIndex().getValue());
            indexLabel.textProperty().bind(Bindings.convert(imageIndexProperty()));

            AnchorPane.setLeftAnchor(indexLabel, 0.0);
            AnchorPane.setRightAnchor(indexLabel, 0.0);
            AnchorPane.setTopAnchor(indexLabel, 0.0);
            AnchorPane.setBottomAnchor(indexLabel, 0.0);

        symbol.getChildren().addAll(imagePane, circlePane, indexLabel);

        if ( model_widget.propTransparent().getValue() ) {
            symbol.setBackground(null);
        } else {
            symbol.setBackground(new Background(new BackgroundFill(JFXUtil.convert(model_widget.propBackgroundColor().getValue()), CornerRadii.EMPTY, Insets.EMPTY)));
        }

        enabled = model_widget.propEnabled().getValue();

        Styles.update(symbol, Styles.NOT_ENABLED, !enabled);

        return symbol;

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propArrayIndex().addUntypedPropertyListener(this::contentChanged);
        model_widget.propPVName().addPropertyListener(this::contentChanged);

        model_widget.propSymbols().addPropertyListener(this::imagesChanged);
        model_widget.propSymbols().getValue().stream().forEach(p -> p.addPropertyListener(imagePropertyListener));

        model_widget.propInitialIndex().addPropertyListener(this::initialIndexChanged);

        model_widget.propAutoSize().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.propEnabled().addUntypedPropertyListener(this::styleChanged);
        model_widget.propPreserveRatio().addUntypedPropertyListener(this::styleChanged);
        model_widget.propShowIndex().addUntypedPropertyListener(this::styleChanged);
        model_widget.propTransparent().addUntypedPropertyListener(this::styleChanged);

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

    private void initialIndexChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyIndex.mark();
        toolkit.scheduleUpdate(this);
    }

    /**
     * Load the image for the given file name.
     *
     * @param fileName The file name of the image to be loaded.
     * @return The loaded {@link Image}, or {@code null} if no image was loaded.
     */
    private Image loadSymbol ( String fileName ) {

        String imageFileName = SymbolWidget.resolveImageFile(model_widget, fileName);
        Image image = null;

        if ( imageFileName != null ) {
            try {
                //  Open the image from the stream created from the resource file.
                image = new Image(ModelResourceUtil.openResourceStream(imageFileName));
            } catch ( Exception ex ) {
                logger.log(Level.WARNING, "Failure loading image: ({0}) {1} [{2}].", new Object[] { fileName, imageFileName, ex.getMessage() });
            }
        }

        if ( image == null ) {

            imageFileName = SymbolWidget.resolveImageFile(model_widget, SymbolWidget.DEFAULT_SYMBOL);

            try {
                //  Open the image from the stream created from the resource file.
                image = new Image(ModelResourceUtil.openResourceStream(imageFileName));

            } catch ( Exception ex ) {
                logger.log(Level.WARNING, "Failure loading image: ({0}) {1} [{2}].", new Object[] { fileName, imageFileName, ex.getMessage() });
            }

        }

        return image;

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

            setImageIndex(Math.min(Math.max(getImageIndex(), 0), imagesList.size() - 1));
            maxSize = computeMaximumSize(model_widget);

        }

    }

    private void valueChanged ( final WidgetProperty<? extends VType> property, final VType oldValue, final VType newValue ) {
        dirtyValue.mark();
        toolkit.scheduleUpdate(this);
    }

}
