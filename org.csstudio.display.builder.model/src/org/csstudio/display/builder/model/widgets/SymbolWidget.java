/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.model.widgets;


import static org.csstudio.display.builder.model.ModelPlugin.logger;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newFilenamePropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newIntegerPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propEnabled;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propTransparent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.display.builder.model.ArrayWidgetProperty;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.MacroHandler;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 19 Jun 2017
 */
public class SymbolWidget extends PVWidget {

    public final static String DEFAULT_SYMBOL = "platform:/plugin/org.csstudio.display.builder.model/icons/default_symbol.png"; //$NON-NLS-1$

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
        "symbol",
        WidgetCategory.MONITOR,
        "Symbol",
        "platform:/plugin/org.csstudio.display.builder.model/icons/symbol.png",
        "A container of symbols displayed depending of the value of a PV"
    ) {
        @Override
        public Widget createWidget ( ) {
            return new SymbolWidget();
        }
    };

    /** 'symbol' property: element for list of 'symbols' property */
    private static final WidgetPropertyDescriptor<String>                       propSymbol        = newFilenamePropertyDescriptor(WidgetPropertyCategory.WIDGET,   "symbol",         Messages.WidgetProperties_Symbol);

    public static final WidgetPropertyDescriptor<Integer>                       propInitialIndex  = newIntegerPropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "initial_index",  Messages.WidgetProperties_InitialIndex, 0, Integer.MAX_VALUE);
    public static final WidgetPropertyDescriptor<Boolean>                       propShowIndex     = newBooleanPropertyDescriptor (WidgetPropertyCategory.DISPLAY,  "show_index",     Messages.WidgetProperties_ShowIndex);

    public static final WidgetPropertyDescriptor<Integer>                       propArrayIndex    = newIntegerPropertyDescriptor (WidgetPropertyCategory.BEHAVIOR, "array_index",    Messages.WidgetProperties_ArrayIndex, 0, Integer.MAX_VALUE);
    public static final WidgetPropertyDescriptor<Boolean>                       propAutoSize      = newBooleanPropertyDescriptor (WidgetPropertyCategory.BEHAVIOR, "auto_size",      Messages.WidgetProperties_AutoSize);
    public static final WidgetPropertyDescriptor<Boolean>                       propPreserveRatio = newBooleanPropertyDescriptor (WidgetPropertyCategory.BEHAVIOR, "preserve_ratio", Messages.WidgetProperties_PreserveRatio);

    /** 'items' property: list of items (string properties) for combo box */
    public static final ArrayWidgetProperty.Descriptor<WidgetProperty<String> > propSymbols       = new ArrayWidgetProperty.Descriptor< WidgetProperty<String> >(
        WidgetPropertyCategory.WIDGET,
        "symbols",
        Messages.WidgetProperties_Symbols,
        (widget, index) -> propSymbol.createProperty(widget, DEFAULT_SYMBOL)
    );

    private volatile WidgetProperty<Boolean>                     auto_size;
    private volatile WidgetProperty<Integer>                     array_index;
    private volatile WidgetProperty<WidgetColor>                 background;
    private volatile WidgetProperty<Boolean>                     enabled;
    private volatile WidgetProperty<Integer>                     initial_index;
    private volatile WidgetProperty<Boolean>                     preserve_ratio;
    private volatile WidgetProperty<Boolean>                     show_index;
    private volatile ArrayWidgetProperty<WidgetProperty<String>> symbols;
    private volatile WidgetProperty<Boolean>                     transparent;

    public static String resolveImageFile ( SymbolWidget widget, String imageFileName ) {

        try {

            String expandedFileName = MacroHandler.replace(widget.getMacrosOrProperties(), imageFileName);

            //  Resolve new image file relative to the source widget model (not 'top'!).
            //  Get the display model from the widget tied to this representation.
            final DisplayModel widgetModel = widget.getDisplayModel();

            // Resolve the image path using the parent model file path.
            return ModelResourceUtil.resolveResource(widgetModel, expandedFileName);

        } catch ( Exception ex ) {

            logger.log(Level.WARNING, "Failure resolving image path: {0} [{1}].", new Object[] { imageFileName, ex.getMessage() });

            return null;

        }

    }

    /**
     * @param type Widget type.
     * @param default_width Default widget width.
     * @param default_height Default widget height.
     */
    public SymbolWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 100, 100);
    }

    public void addSymbol( String fileName ) {
        symbols.addElement(propSymbol.createProperty(this, fileName));
    }

    @Override
    public WidgetConfigurator getConfigurator ( final Version persistedVersion ) throws Exception {
        return new SymbolConfigurator(persistedVersion);
    }

    public WidgetProperty<Integer> propArrayIndex ( ) {
        return array_index;
    }

    public WidgetProperty<Boolean> propAutoSize ( ) {
        return auto_size;
    }

    public WidgetProperty<WidgetColor> propBackgroundColor ( ) {
        return background;
    }

    public WidgetProperty<Boolean> propEnabled ( ) {
        return enabled;
    }

    public WidgetProperty<Integer> propInitialIndex ( ) {
        return initial_index;
    }

    public WidgetProperty<Boolean> propPreserveRatio ( ) {
        return preserve_ratio;
    }

    public WidgetProperty<Boolean> propShowIndex ( ) {
        return show_index;
    }

    public ArrayWidgetProperty<WidgetProperty<String>> propSymbols ( ) {
        return symbols;
    }

    public WidgetProperty<Boolean> propTransparent ( ) {
        return transparent;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(symbols        = propSymbols.createProperty(this, Collections.emptyList()));

        properties.add(background     = propBackgroundColor.createProperty(this, WidgetColorService.getColor(NamedWidgetColors.BACKGROUND)));
        properties.add(initial_index  = propInitialIndex.createProperty(this, 0));
        properties.add(show_index     = propShowIndex.createProperty(this, false));
        properties.add(transparent    = propTransparent.createProperty(this, true));

        properties.add(array_index    = propArrayIndex.createProperty(this, 0));
        properties.add(auto_size      = propAutoSize.createProperty(this, false));
        properties.add(enabled        = propEnabled.createProperty(this, true));
        properties.add(preserve_ratio = propPreserveRatio.createProperty(this, true));

    }

    /**
     * Custom configurator to read legacy *.opi files.
     */
    protected static class SymbolConfigurator extends WidgetConfigurator {

        public SymbolConfigurator ( Version xmlVersion ) {
            super(xmlVersion);
        }

        @Override
        public boolean configureFromXML ( final ModelReader reader, final Widget widget, final Element xml ) throws Exception {

            if ( !super.configureFromXML(reader, widget, xml) ) {
                return false;
            }

            if ( xml_version.getMajor() < 2 ) {

                SymbolWidget symbol = (SymbolWidget) widget;
                String typeId = xml.getAttribute("typeId");
                List<String> fileNames = new ArrayList<>(2);

                switch ( typeId ) {
                    case "org.csstudio.opibuilder.widgets.ImageBoolIndicator":
                        XMLUtil.getChildString(xml, "off_image").ifPresent(f -> {

                            String imageFileName = resolveImageFile(symbol, f);

                            if ( resourceExists(imageFileName) ) {
                                fileNames.add(f);
                            } else {
                                logger.log(Level.WARNING, "OFF image file {0} does not exits [{1}].", new Object[] { f, imageFileName });
                                fileNames.add(DEFAULT_SYMBOL);
                            }

                        });
                        XMLUtil.getChildString(xml, "on_image").ifPresent(f -> {

                            String imageFileName = resolveImageFile(symbol, f);

                            if ( resourceExists(imageFileName) ) {
                                fileNames.add(f);
                            } else {
                                logger.log(Level.WARNING, "ON image file {0} does not exits [{1}].", new Object[] { f, imageFileName });
                                fileNames.add(DEFAULT_SYMBOL);
                            }

                        });
                        break;
                    case "org.csstudio.opibuilder.widgets.symbol.bool.BoolMonitorWidget":
//  TBD
                        break;
                    case "org.csstudio.opibuilder.widgets.symbol.multistate.MultistateMonitorWidget":
//  TBD
                        break;
                }

                ArrayWidgetProperty<WidgetProperty<String>> propSymbols = symbol.propSymbols();

                for ( int i = 0; i < fileNames.size(); i++ ) {
                    if ( i < propSymbols.size() ) {
                        propSymbols.getElement(i).setValue(fileNames.get(i));
                    } else {
                        symbol.addSymbol(fileNames.get(i));
                    }
                }

                XMLUtil.getChildBoolean(xml, "stretch_to_fit").ifPresent(stf -> symbol.propPreserveRatio().setValue(!stf));

            }

            return true;

        }

        private boolean resourceExists ( String fileName ) {

            try {
                ModelResourceUtil.openResourceStream(fileName);
            } catch ( Exception ex ) {
                return false;
            }

            return true;

        }

    }

}
