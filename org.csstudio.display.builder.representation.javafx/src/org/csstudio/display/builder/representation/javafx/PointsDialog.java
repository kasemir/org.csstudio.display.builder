/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import org.csstudio.display.builder.model.properties.Points;
import org.csstudio.javafx.DialogHelper;
import org.csstudio.javafx.PreferencesHelper;

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/** Dialog for table of {@link Points}
 *  @author Kay Kasemir
 */
public class PointsDialog extends Dialog<Points>
{

    /**
     * @param initial_points Initial value.
     * @param owner The node starting this dialog.
     **/
    public PointsDialog ( final Points initial_points, final Node owner ) {
        this(initial_points);
        DialogHelper.positionAndSize(this, owner, PreferencesHelper.userNodeForClass(PointsDialog.class));
    }

    /** @param initial_points Initial value */
    public PointsDialog(final Points initial_points)
    {
        final Points points = initial_points.clone();

        setTitle(Messages.PointsDialog_Title);
        setHeaderText(Messages.PointsDialog_Info);

        final PointsTable table = new PointsTable(points);
        getDialogPane().setContent(table.create());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);

        setResultConverter(button ->
        {
            if (button == ButtonType.OK)
                return points;
            return null;
        });
    }
}
