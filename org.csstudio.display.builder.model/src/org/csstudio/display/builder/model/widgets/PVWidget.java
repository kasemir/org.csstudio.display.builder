/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved
 * The contents of this file are subject to the terms of
 * the GNU General Public License Version 3 only ("GPL"
 * Version 3, or the "License"). You can obtain a copy of
 * the License at https://www.gnu.org/licenses/gpl-3.0.html
 * You may use, distribute and modify this code under the
 * terms of the GPL Version 3 license. See the License for
 * the specific language governing permissions and
 * limitations under the License.
 * When distributing the software, include this License
 * Header Notice in each file. If applicable, add the
 * following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying
 * information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.csstudio.display.builder.model.widgets;


import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propPVName;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.runtimePropValue;

import java.util.List;

import org.csstudio.display.builder.model.WidgetProperty;
import org.diirt.vtype.VType;


/**
 * A base class for all widgets having a PV variable.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 15 Sep 2016
 */
public class PVWidget extends VisibleWidget {

    private volatile WidgetProperty<String> pv_name;
    private volatile WidgetProperty<VType>  value;

    /**
     * @param type Widget type.
     */
    public PVWidget ( String type ) {
        super(type);
    }

    /**
     * @param type Widget type.
     * @param default_width Default widget width.
     * @param default_height Default widget height.
     */
    public PVWidget ( String type, int default_width, int default_height ) {
        super(type, default_width, default_height);
    }

    /** @return 'pv_name' property */
    public final WidgetProperty<String> propPVName ( ) {
        return pv_name;
    }

    /** @return Runtime 'value' property */
    public WidgetProperty<VType> runtimePropValue ( ) {
        return value;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(pv_name = propPVName.createProperty(this, ""));
        properties.add(value = runtimePropValue.createProperty(this, null));
        properties.add(propBorderAlarmSensitive.createProperty(this, true));

    }

}
