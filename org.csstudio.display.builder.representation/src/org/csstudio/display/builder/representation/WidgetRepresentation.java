/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation;

import java.util.logging.Logger;

import org.csstudio.display.builder.model.Widget;

/** Toolkit representation for a model widget
 *
 *  <p>Creates a toolkit item, for example a JavaFX Label,
 *  for the corresponding model widget, i.e. a LabelWidget.
 *
 *  @author Kay Kasemir
 *  @param <TWP> Toolkit widget parent class
 *  @param <TW> Toolkit widget base class
 *  @param <MW> Toolkit widget base class
 */
abstract public class WidgetRepresentation<TWP, TW, MW extends Widget>
{
    protected final Logger logger = Logger.getLogger(getClass().getName());

    /** Toolkit helper */
    protected final ToolkitRepresentation<TWP, TW> toolkit;

    /** Model widget that is represented in toolkit */
    protected final MW model_widget;

    /** Construct representation for a model widget
     *  @param toolkit Toolkit helper
     *  @param model_widget Model widget
     */
    public WidgetRepresentation(final ToolkitRepresentation<TWP, TW> toolkit,
                                final MW model_widget)
    {
        this.toolkit = toolkit;
        this.model_widget = model_widget;
    }

    /** Initialize the toolkit item(s)
     *
     *  <p>If this widget is a container, it returns a
     *  new parent for its child widgets.
     *  Plain widgets return the same parent that's passed
     *  as an argument.
     *
     *  @param parent Toolkit parent for this item
     *  @return New parent to use for child items
     *  @throws Exception on error
     */
    abstract public TWP init(final TWP parent) throws Exception;

    /** Update toolkit representation to match model.
     *
     *  <p>Invoked by toolkit's update throttle after
     *  <code>ToolkitRepresentation.scheduleUpdate()</code>
     *  has requested an update.
     *
     *  <p>Ideally based on listeners and 'dirty' markers to only update
     *  aspects of model that really changed.
     *
     *  <p>Override must call base class.
     */
    abstract public void updateChanges();

    /** Remove toolkit items.
     *
     *  <p>Called when model widget has been removed.
     */
    abstract public void dispose();
}

