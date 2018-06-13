/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.csstudio.display.builder.model.properties.Points;

import javafx.geometry.Bounds;
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

        initOwner(owner.getScene().getWindow());
        ModalityHack.forDialog(this);

        final Preferences pref = Preferences.userNodeForPackage(PointsDialog.class).node(PointsDialog.class.getSimpleName());
        final double prefX = pref.getDouble("dialog.x", Double.NaN);
        final double prefY = pref.getDouble("dialog.y", Double.NaN);
        final double prefWidth = pref.getDouble("dialog.width", Double.NaN);
        final double prefHeight = pref.getDouble("dialog.height", Double.NaN);

        if ( !Double.isNaN(prefX) && !Double.isNaN(prefY) ) {
            setX(prefX);
            setY(prefY);
        } else {

            Bounds pos = owner.localToScreen(owner.getBoundsInLocal());

            setX(pos.getMinX());
            setY(pos.getMinY() + pos.getHeight());

        }

        if ( !Double.isNaN(prefWidth) && !Double.isNaN(prefHeight) ) {
            setWidth(prefWidth);
            setHeight(prefHeight);
        }

        setOnHidden(event -> {

            Preferences prf = Preferences.userNodeForPackage(PointsDialog.class).node(PointsDialog.class.getSimpleName());

            prf.putDouble("dialog.x", getX());
            prf.putDouble("dialog.y", getY());
            prf.putDouble("dialog.width", getWidth());
            prf.putDouble("dialog.height", getHeight());

            try {
                pref.flush();
            } catch ( BackingStoreException ex ) {
                logger.log(Level.WARNING, "Unable to flush preferences", ex);
            }

        });

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
