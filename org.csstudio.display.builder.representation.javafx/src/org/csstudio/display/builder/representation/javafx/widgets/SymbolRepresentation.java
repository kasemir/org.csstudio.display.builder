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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.csstudio.display.builder.model.widgets.SymbolWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.Styles;
import org.diirt.vtype.VType;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import se.europeanspallationsource.xaos.components.SVG;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 19 Jun 2017
 */
public class SymbolRepresentation extends RegionBaseRepresentation<StackPane, SymbolWidget> {

    private static final ExecutorService EXECUTOR         = Executors.newFixedThreadPool(8);
    private static final double          INDEX_LABEL_SIZE = 32.0;

    private int                                  arrayIndex             = 0;
    private volatile boolean                     autoSize               = false;
    private Symbol                               content;
    private DefaultSymbol                        defaultSymbol          = new DefaultSymbol();
    private final DirtyFlag                      dirtyContent           = new DirtyFlag();
    private final DirtyFlag                      dirtyGeometry          = new DirtyFlag();
    private final DirtyFlag                      dirtyIndex             = new DirtyFlag();
    private final DirtyFlag                      dirtyStyle             = new DirtyFlag();
    private final DirtyFlag                      dirtyValue             = new DirtyFlag();
    private volatile boolean                     enabled                = true;
    private final Label                          indexLabel             = new Label();
    private final Circle                         indexLabelBackground   = new Circle(INDEX_LABEL_SIZE / 2, Color.BLACK.deriveColor(0.0, 0.0, 0.0, 0.75));
    private Dimension2D                          maxSize                = new Dimension2D(0, 0);
    private final WidgetPropertyListener<String> symbolPropertyListener = this::symbolChanged;
    private final AtomicReference<List<Symbol>>  symbols                = new AtomicReference<>(Collections.emptyList());

    // ---- imageIndex property
    private IntegerProperty imageIndex = new SimpleIntegerProperty(-1);

    private int getImageIndex ( ) {
        return imageIndex.get();
    }

    private IntegerProperty imageIndexProperty ( ) {
        return imageIndex;
    }

    private void setImageIndex ( int imageIndex ) {
        toolkit.execute(() -> this.imageIndex.set(imageIndex));
    }

    public static String resolveImageFile ( SymbolWidget widget, String imageFileName ) {

        try {

            String expandedFileName = MacroHandler.replace(widget.getMacrosOrProperties(), imageFileName);

            // Resolve new image file relative to the source widget model (not 'top'!).
            // Get the display model from the widget tied to this representation.
            final DisplayModel widgetModel = widget.getDisplayModel();

            // Resolve the image path using the parent model file path.
            return ModelResourceUtil.resolveResource(widgetModel, expandedFileName);

        } catch ( Exception ex ) {

            logger.log(Level.WARNING, "Failure resolving image path: {0} [{1}].", new Object[] { imageFileName, ex.getMessage() });

            return null;

        }

    }

    private static boolean resourceExists ( String fileName ) {

        try {
            ModelResourceUtil.openResourceStream(fileName);
        } catch ( Exception ex ) {
            return false;
        }

        return true;

    }

    @Override
    public void dispose ( ) {

        model_widget.propSymbols().getValue().stream().forEach(p -> p.removePropertyListener(symbolPropertyListener));

        EXECUTOR.shutdown();

        try {
            while ( !EXECUTOR.awaitTermination(10, TimeUnit.SECONDS) ) {
                Thread.yield();
            }
        } catch ( InterruptedException ex ) {
            logger.log(
                Level.WARNING,
                MessageFormat.format("Interrupted while awaiting for shutdown termination [widget: {0} - {1}].", model_widget.getType(), model_widget.getName()),
                ex
            );
        }

        super.dispose();

    }

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        boolean needsSVGResize = false;
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

            double w = model_widget.propWidth().getValue();
            double h = model_widget.propHeight().getValue();

            if ( w < INDEX_LABEL_SIZE || h < INDEX_LABEL_SIZE ) {
                jfx_node.getChildren().remove(indexLabel);
                jfx_node.getChildren().remove(indexLabelBackground);
            } else {
                if ( !jfx_node.getChildren().contains(indexLabelBackground) ) {
                    jfx_node.getChildren().add(indexLabelBackground);
                }
                if ( !jfx_node.getChildren().contains(indexLabel) ) {
                    jfx_node.getChildren().add(indexLabel);
                }
            }

            jfx_node.setLayoutX(model_widget.propX().getValue());
            jfx_node.setLayoutY(model_widget.propY().getValue());
            jfx_node.setPrefSize(w, h);

            if ( content != null ) {
                if ( content instanceof DefaultSymbol ) {
                    ((DefaultSymbol) content).setSize(w, h);
                } else if ( content instanceof Region ) {
                    ((Region) content).setPrefSize(w, h);
                }
            }

            needsSVGResize = true;

        }

        if ( dirtyIndex.checkAndClear() ) {
            setImageIndex(model_widget.propInitialIndex().getValue());
        }

        if ( dirtyContent.checkAndClear() ) {

            value = model_widget.propArrayIndex().getValue();

            if ( !Objects.equals(value, arrayIndex) ) {
                arrayIndex = Math.max(0, (int) value);
            }

            dirtyValue.mark();

        }

        if ( dirtyStyle.checkAndClear() ) {

            value = model_widget.propPreserveRatio().getValue();

            if ( content instanceof ImageView && !Objects.equals(value, ((ImageView) content).isPreserveRatio()) ) {

                ((ImageView) content).setPreserveRatio((boolean) value);

                needsSVGResize = true;

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

            value = model_widget.propRotation().getValue();

            if ( !Objects.equals(value, content.getRotate()) ) {
                content.setRotate((double) value);
            }

        }

//        if ( dirtyValue.checkAndClear() && updatingValue.compareAndSet(false, true) ) {
//
//            int idx = Integer.MIN_VALUE;    // Marker indicating no valid value.
//
//            try {
//
//                value = model_widget.runtimePropValue().getValue();
//
//                if ( value != null ) {
//                    if ( PVWidget.RUNTIME_VALUE_NO_PV == value ) {
//                        idx = model_widget.propInitialIndex().getValue();
//                    } else if ( value instanceof VBoolean ) {
//                        idx = ( (VBoolean) value ).getValue() ? 1 : 0;
//                    } else if ( value instanceof VString ) {
//                        try {
//                            idx = Integer.parseInt(( (VString) value ).getValue());
//                        } catch ( NumberFormatException nfex ) {
//                            logger.log(Level.FINE, "Failure parsing the string value: {0} [{1}].", new Object[] { ( (VString) value ).getValue(), nfex.getMessage() });
//                        }
//                    } else if ( value instanceof VNumber ) {
//                        idx = ( (VNumber) value ).getValue().intValue();
//                    } else if ( value instanceof VEnum ) {
//                        idx = ( (VEnum) value ).getIndex();
//                    } else if ( value instanceof VNumberArray ) {
//
//                        ListNumber array = ( (VNumberArray) value ).getData();
//
//                        if ( array.size() > 0 ) {
//                            idx = array.getInt(Math.min(arrayIndex, array.size() - 1));
//                        }
//
//                    } else if ( value instanceof VEnumArray ) {
//
//                        ListInt array = ( (VEnumArray) value ).getIndexes();
//
//                        if ( array.size() > 0 ) {
//                            idx = array.getInt(Math.min(arrayIndex, array.size() - 1));
//                        }
//
//                    }
//                }
//
//            } finally {
//                updatingValue.set(false);
//            }
//
//            if ( idx != Integer.MIN_VALUE ) {
//                // Valid value.
//                setImageIndex(idx);
//            }
//
//        }
//
//        if ( needsSVGResize ) {
//            imagesList.stream().filter(ic -> ic.isSVG()).forEach(ic -> {
//
//                double widgetWidth = model_widget.propWidth().getValue().doubleValue();
//                double widgetHeight = model_widget.propHeight().getValue().doubleValue();
//                double symbolWidth = widgetWidth;
//                double symbolHeight = widgetHeight;
//
//                if ( model_widget.propPreserveRatio().getValue() ) {
//
//                    double wPrime = symbolHeight * ic.getOriginalRatio();
//                    double hPrime = symbolWidth / ic.getOriginalRatio();
//
//                    if ( wPrime < symbolWidth ) {
//                        symbolHeight = hPrime;
//                    } else if ( hPrime < symbolHeight ) {
//                        symbolWidth = wPrime;
//                    }
//
//                }
//
//                double finalSymbolWidth = symbolWidth;
//                double finalSymbolHeight = symbolHeight;
//                double cos_a = Math.cos(Math.toRadians(model_widget.propRotation().getValue()));
//                double sin_a = Math.sin(Math.toRadians(model_widget.propRotation().getValue()));
//                double pic_bb_w = symbolWidth * Math.abs(cos_a) + symbolHeight * Math.abs(sin_a);
//                double pic_bb_h = symbolWidth * Math.abs(sin_a) + symbolHeight * Math.abs(cos_a);
//                double scale_fac = Math.min(widgetWidth / pic_bb_w, widgetHeight / pic_bb_h);
//
//                if ( scale_fac < 1.0 ) {
//                    finalSymbolWidth = (int) Math.floor(scale_fac * symbolWidth);
//                    finalSymbolHeight = (int) Math.floor(scale_fac * symbolHeight);
//                }
//
//                SVG svg = ic.getSVG();
//                Bounds bounds = svg.getLayoutBounds();
//                double boundsWidth = bounds.getWidth();
//                double boundsHeight = bounds.getHeight();
//
//                svg.setScaleX(finalSymbolWidth / boundsWidth);
//                svg.setScaleY(finalSymbolHeight / boundsHeight);
//
//                if ( finalSymbolWidth < boundsWidth && widgetWidth <= boundsWidth ) {
//                    svg.setTranslateX(( widgetWidth - boundsWidth ) / 2.0);
//                } else {
//                    svg.setTranslateX(0);
//                }
//
//                if ( finalSymbolHeight < boundsHeight && widgetHeight <= boundsHeight ) {
//                    svg.setTranslateY(( widgetHeight - boundsHeight ) / 2.0);
//                } else {
//                    svg.setTranslateY(0);
//                }
//
//            });
//        }

    }

    @Override
    protected StackPane createJFXNode ( ) throws Exception {

        autoSize = model_widget.propAutoSize().getValue();
        content = defaultSymbol;

        StackPane symbol = new StackPane();

        indexLabelBackground.setStroke(Color.LIGHTGRAY.deriveColor(0.0, 1.0, 1.0, 0.75));
        indexLabelBackground.setVisible(model_widget.propShowIndex().getValue());

        indexLabel.setAlignment(Pos.CENTER);
        indexLabel.setFont(Font.font(indexLabel.getFont().getFamily(), FontWeight.BOLD, 16));
        indexLabel.setTextFill(Color.WHITE);
        indexLabel.setVisible(model_widget.propShowIndex().getValue());
        indexLabel.textProperty().bind(Bindings.convert(imageIndexProperty()));

        symbol.getChildren().addAll(content, indexLabelBackground, indexLabel);

        if ( model_widget.propTransparent().getValue() ) {
            symbol.setBackground(null);
        } else {
            symbol.setBackground(new Background(new BackgroundFill(JFXUtil.convert(model_widget.propBackgroundColor().getValue()), CornerRadii.EMPTY, Insets.EMPTY)));
        }

        enabled = model_widget.propEnabled().getValue();

        Styles.update(symbol, Styles.NOT_ENABLED, !enabled);

        initialIndexChanged(null, null, null);
        symbolChanged(null, null, null);

        return symbol;

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propArrayIndex().addUntypedPropertyListener(this::contentChanged);
        model_widget.propPVName().addPropertyListener(this::contentChanged);

        model_widget.propSymbols().addPropertyListener(this::symbolsChanged);
        model_widget.propSymbols().getValue().stream().forEach(p -> {
            p.removePropertyListener(symbolPropertyListener);
            p.addPropertyListener(symbolPropertyListener);
        });

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
        model_widget.propRotation().addUntypedPropertyListener(this::styleChanged);
        model_widget.propShowIndex().addUntypedPropertyListener(this::styleChanged);
        model_widget.propTransparent().addUntypedPropertyListener(this::styleChanged);

        if ( toolkit.isEditMode() ) {
            dirtyValue.checkAndClear();
        } else {
            model_widget.runtimePropValue().addPropertyListener(this::valueChanged);
            valueChanged(null, null, null);
        }

    }

    private void contentChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyContent.mark();
        toolkit.scheduleUpdate(this);
    }

    /**
     * Fix the file names imported from BOY.
     */
    private void fixImportedSymbolNames ( ) {

        try {

            ArrayWidgetProperty<WidgetProperty<String>> propSymbols = model_widget.propSymbols();
            List<String> fileNames = new ArrayList<>(2);

            switch ( model_widget.getImportedFrom() ) {
                case "org.csstudio.opibuilder.widgets.ImageBoolIndicator":
                    return;
                case "org.csstudio.opibuilder.widgets.symbol.bool.BoolMonitorWidget": {

                        String imageFileName = propSymbols.getElement(0).getValue();
                        int dotIndex = imageFileName.lastIndexOf('.');
                        String onImageFileName = imageFileName.substring(0, dotIndex) + " On" + imageFileName.substring(dotIndex);
                        String offImageFileName = imageFileName.substring(0, dotIndex) + " Off" + imageFileName.substring(dotIndex);

                        if ( resourceExists(resolveImageFile(model_widget, onImageFileName)) ) {
                            fileNames.add(onImageFileName);
                        } else {
                            fileNames.add(imageFileName);
                        }

                        if ( resourceExists(resolveImageFile(model_widget, offImageFileName)) ) {
                            fileNames.add(offImageFileName);
                        } else {
                            fileNames.add(imageFileName);
                        }

                    }
                    break;
                case "org.csstudio.opibuilder.widgets.symbol.multistate.MultistateMonitorWidget": {

                        String imageFileName = propSymbols.getElement(0).getValue();
                        int dotIndex = imageFileName.lastIndexOf('.');
                        int spaceIndex = imageFileName.lastIndexOf(' ');

                        try {
                            model_widget.propInitialIndex().setValue(Integer.parseInt(imageFileName.substring(1 + spaceIndex, dotIndex)));
                        } catch ( NumberFormatException nfex ) {
                            logger.log(Level.WARNING, "Imported image file doesn't contain state value [{0}].", imageFileName);
                        }

                        int index = 0;

                        while ( true ) {

                            String nthImageFileName = MessageFormat.format("{0} {1,number,#########0}{2}", imageFileName.substring(0, spaceIndex), index, imageFileName.substring(dotIndex));

                            if ( resourceExists(resolveImageFile(model_widget, nthImageFileName)) ) {
                                fileNames.add(nthImageFileName);
                            } else {
                                break;
                            }

                            index++;

                        }

                    }
                    break;
                default:
                    logger.log(Level.WARNING, "Invalid imported type [{0}].", model_widget.getImportedFrom());
                    return;
            }

            for ( int i = 0; i < fileNames.size(); i++ ) {
                model_widget.addOrReplaceSymbol(i, fileNames.get(i));
            }

        } finally {
            model_widget.clearImportedFrom();
        }

    }

    private void geometryChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyGeometry.mark();
        toolkit.scheduleUpdate(this);
    }

    private void initialIndexChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyIndex.mark();
        toolkit.scheduleUpdate(this);
    }

    private void styleChanged ( final WidgetProperty<?> property, final Object oldValue, final Object newValue ) {
        dirtyStyle.mark();
        toolkit.scheduleUpdate(this);
    }

    private void symbolChanged ( final WidgetProperty<String> property, final String oldValue, final String newValue ) {
        EXECUTOR.execute( ( ) -> {

            updateSymbols();

            dirtyContent.mark();
            toolkit.scheduleUpdate(this);

        });
    }

    private void symbolsChanged ( final WidgetProperty<List<WidgetProperty<String>>> property, final List<WidgetProperty<String>> oldValue, final List<WidgetProperty<String>> newValue ) {
        EXECUTOR.execute( ( ) -> {

            if ( oldValue != null ) {
                oldValue.stream().forEach(p -> p.removePropertyListener(symbolPropertyListener));
            }

            if ( newValue != null ) {
                newValue.stream().forEach(p -> p.addPropertyListener(symbolPropertyListener));
            }

            symbolChanged(null, null, null);

        });
    }

    private synchronized void updateSymbols ( ) {

        List<WidgetProperty<String>> fileNames = model_widget.propSymbols().getValue();
        List<Symbol> symbolsList = new ArrayList<>(fileNames.size());
        Map<String, Symbol> symbolsMap = new HashMap<>(fileNames.size());
        Map<String, Symbol> currentSymbolsMap = symbols.get().stream().collect(Collectors.toMap(Symbol::getFileName, sc -> sc));
        int currentIndex = getImageIndex();

        try {

            if ( model_widget.getImportedFrom() != null ) {
                fixImportedSymbolNames();
            }

            if ( fileNames == null ) {
                logger.log(Level.WARNING, "Empty list of file names.");
            } else {

                fileNames.stream().forEach(f -> {

                    String fileName = f.getValue();
                    Symbol symbol = symbolsMap.get(fileName);

                    if ( symbol == null ) {     //  Symbol not yet loaded...

                        symbol = currentSymbolsMap.get(fileName);

                        if ( symbol == null ) { //  Neither previously loaded.
                            symbol = new Symbol(fileName);
                        }

                        symbolsMap.put(fileName, symbol);

                    }

                    symbolsList.add(symbol);

                });

            }

        } finally {

            int newImageIndex = Math.min(Math.max(getImageIndex(), 0), symbolsList.size() - 1);

            maxSize = new Dimension2D(
                symbolsList.stream().mapToDouble(Symbol::getOriginalWidth).max().orElse(0.0),
                symbolsList.stream().mapToDouble(Symbol::getOriginalHeight).max().orElse(0.0)
            );

            if ( currentIndex == newImageIndex && currentIndex >= 0 ) {

                ImageContent imageContent = imagesList.get(getImageIndex());

                if ( imageContent.isSVG() || ( imageContent.isImage() && imagePane.getCenter() != imageView ) ) {
                    Platform.runLater( ( ) -> triggerContentUpdate());
                } else if ( imageContent.isImage() ) {
                    Platform.runLater( ( ) -> triggerImageUpdate());
                }

            } else if ( oldIndex != newImageIndex ) {
                setImageIndex(newImageIndex);
            }

            dirtyGeometry.mark();
            dirtyStyle.mark();
            dirtyValue.mark();
            toolkit.scheduleUpdate(this);

        }

    }

    private void valueChanged ( final WidgetProperty<? extends VType> property, final VType oldValue, final VType newValue ) {
        dirtyValue.mark();
        toolkit.scheduleUpdate(this);
    }

    private class DefaultSymbol extends Group {

        private final Rectangle r;
        private final Line l1;
        private final Line l2;

        DefaultSymbol() {

            setAutoSizeChildren(true);
            setManaged(true);

            int w = 100;
            int h = 100;

            r = new Rectangle(0, 0, w, h);

            r.setFill(null);
            r.setArcHeight(0);
            r.setArcWidth(0);
            r.setStroke(Color.BLACK);
            r.setStrokeType(StrokeType.INSIDE);

            l1 = new Line(0.5, 0.5, w - 0.5, h - 0.5);

            l1.setStroke(Color.BLACK);
            l1.setStrokeLineCap(StrokeLineCap.BUTT);

            l2 = new Line(0.5, h - 0.5, w - 0.5, 0.5);

            l2.setStroke(Color.BLACK);
            l2.setStrokeLineCap(StrokeLineCap.BUTT);

            getChildren().add(r);
            getChildren().add(l1);
            getChildren().add(l2);

        }

        public void setSize ( double w, double h ) {

            r.setWidth(w);
            r.setHeight(h);

            l1.setEndX(w - 0.5);
            l1.setEndY(h - 0.5);

            l2.setEndX(w - 0.5);
            l2.setStartY(h - 0.5);

        }

    }

    private class Symbol {

        private final String fileName;
        private double       originalHeight;
        private double       originalWidth;
        private Node         node;

        Symbol ( String fileName ) {

            this.fileName = fileName;

            boolean loadFailed = true;
            String imageFileName = resolveImageFile(model_widget, fileName);

            if ( imageFileName != null ) {
                if ( imageFileName.toLowerCase().endsWith(".svg") ) {
                    try {

                        // Open the image from the stream created from the resource file.
                        node = SVG.load(ModelResourceUtil.openResourceStream(imageFileName));

                        Bounds bounds = ((SVG) node).getLayoutBounds();

                        originalWidth = bounds.getWidth();
                        originalHeight = bounds.getHeight();
                        loadFailed = false;

                    } catch ( Exception ex ) {
                        logger.log(Level.WARNING, "Failure loading SVG image file: ({0}) {1} [{2}].", new Object[] { fileName, imageFileName, ex.getMessage() });
                    }
                } else {
                    try {

                        // Open the image from the stream created from the resource file.
                        Image image = new Image(ModelResourceUtil.openResourceStream(imageFileName));

                        node = new ImageView(image);
                        originalWidth = image.getWidth();
                        originalHeight = image.getHeight();
                        loadFailed = false;

                    } catch ( Exception ex ) {
                        logger.log(Level.WARNING, "Failure loading image: ({0}) {1} [{2}].", new Object[] { fileName, imageFileName, ex.getMessage() });
                    }
                }
            }

            if ( loadFailed ) {

                node = defaultSymbol;

                Bounds bounds = ((DefaultSymbol) node).getLayoutBounds();

                originalWidth = bounds.getWidth();
                originalHeight = bounds.getHeight();

            }

        }

        String getFileName() {
            return fileName;
        }

        double getOriginalHeight ( ) {
            return originalHeight;
        }

        double getOriginalRatio ( ) {
            return originalWidth / originalHeight;
        }

        double getOriginalWidth ( ) {
            return originalWidth;
        }

        Node getNode ( ) {
            return node;
        }

        boolean isImageView ( ) {
            return ( node instanceof ImageView );
        }

        boolean isSVG ( ) {
            return ( node instanceof SVG);
        }

        boolean isValid ( ) {
            return ( isImageView() || isSVG() );
        }

    }





























//    private final List<ImageContent>             imagesList            = Collections.synchronizedList(new ArrayList<>(4));
//    private final Map<String, ImageContent>      imagesMap             = Collections.synchronizedMap(new TreeMap<>());
//    private BorderPane                           imagePane;
//    private ImageView                            imageView;
//  private final ReentrantLock updatingSymbols      = new ReentrantLock();
//    private final AtomicBoolean                  updatingValue         = new AtomicBoolean(false);
//
//    //  ---- triggerContentUpdate property
//    private BooleanProperty triggerContentUpdate =  new SimpleBooleanProperty(false);
//
//    private boolean isTriggerContentUpdate ( ) {
//        return triggerContentUpdate.get();
//    }
//
//    private BooleanProperty triggerContentUpdateProperty ( ) {
//        return triggerContentUpdate;
//    }
//
//    private void setTriggerContentUpdate ( boolean triggerContentUpdate ) {
//        Platform.runLater(() -> this.triggerContentUpdate.set(triggerContentUpdate));
//    }
//
//    private void triggerContentUpdate() {
//        setTriggerContentUpdate(!isTriggerContentUpdate());
//    }
//
//    //  ---- triggerImageUpdate property
//    private BooleanProperty triggerImageUpdate =  new SimpleBooleanProperty(false);
//
//    private boolean isTriggerImageUpdate ( ) {
//        return triggerImageUpdate.get();
//    }
//
//    private BooleanProperty triggerImageUpdateProperty ( ) {
//        return triggerImageUpdate;
//    }
//
//    private void setTriggerImageUpdate ( boolean triggerImageUpdate ) {
//        Platform.runLater(() -> this.triggerImageUpdate.set(triggerImageUpdate));
//    }
//
//    private void triggerImageUpdate() {
//        setTriggerImageUpdate(!isTriggerImageUpdate());
//    }
//
//    /**
//     * Compute the maximum width and height of the given {@code widget} based on
//     * the its set of node images.
//     *
//     * @param widget The {@link SymbolWidget} whose size must be computed.
//     * @return A not {@code null} maximum dimension of the given {@code widget}.
//     */
//    public static Dimension2D computeMaximumSize ( final SymbolWidget widget ) {
//
//        Double[] max_size = new Double[] { 0.0, 0.0 };
//
//        widget.propSymbols().getValue().stream().forEach(s -> {
//
//            final String imageFile = s.getValue();
//
//            try {
//
//                final SymbolRepresentation representation = widget.getUserData(Widget.USER_DATA_REPRESENTATION);
//                final ImageContent ic = representation.imagesMap.containsKey(imageFile)
//                                      ? representation.imagesMap.get(imageFile)
//                                      : representation.createImageContent(imageFile);
//
//                if ( max_size[0] < ic.getOriginalWidth() ) {
//                    max_size[0] = ic.getOriginalWidth();
//                }
//                if ( max_size[1] < ic.getOriginalHeight() ) {
//                    max_size[1] = ic.getOriginalHeight();
//                }
//
//            } catch ( Exception ex ) {
//                //  The following message has proven to be annoying and not useful.
//                //logger.log(Level.WARNING, "Cannot obtain image size for {0} [{1}].", new Object[] { imageFile, ex.getMessage() });
//            }
//
//        });
//
//        return new Dimension2D(max_size[0], max_size[1]);
//
//    }
//
//    private <T> T  getDisplayable( Predicate<ImageContent> predicate, Function<ImageContent, T> getter, T defaultDisplayable ) {
//
//        int index = getImageIndex();
//
//        if ( index >= 0 && index < imagesList.size() ) {
//
//            ImageContent imageContent = imagesList.get(index);
//
//            if ( predicate.test(imageContent) ) {
//                return getter.apply(imageContent);
//            } else {
//                return defaultDisplayable;
//            }
//
//        } else {
//            return defaultDisplayable;
//        }
//
//    }
//
}
