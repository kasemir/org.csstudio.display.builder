/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicInteger;

import org.csstudio.display.builder.model.Widget;
import org.junit.Test;

/** JUnit test of widget property subscriptions
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetPropertySubscriptionUnitTest
{
    /** Check subscription updates */
    @Test
    public void testBasicSubscription()
    {
        final AtomicInteger updates = new AtomicInteger(0);
        final Widget widget = new Widget("generic", "test5");
        final PropertyChangeListener listener = (final PropertyChangeEvent event) ->
        {
            updates.incrementAndGet();
            System.out.println(event);
        };
        widget.addPropertyListener(positionX.getName(), listener);

        // Noting, yet
        assertThat(updates.get(), equalTo(0));

        // Change once
        widget.getProperty(positionX).setValue(21);
        assertThat(updates.get(), equalTo(1));

        // Change again
        widget.getProperty(positionX).setValue(22);
        assertThat(updates.get(), equalTo(2));

        // No change, same value
        widget.getProperty(positionX).setValue(22);
        assertThat(updates.get(), equalTo(2));
    }

    /** Check subscription updates */
    @Test
    public void testSpecificSubscription()
    {
        final Widget widget = new Widget("generic", "test6");

        final AtomicInteger updates = new AtomicInteger(0);
        final PropertyChangeListener listener = (final PropertyChangeEvent event) ->
        {
            updates.incrementAndGet();
            System.out.println(event);
        };

        final AtomicInteger x_updates = new AtomicInteger(0);
        final PropertyChangeListener x_listener = (final PropertyChangeEvent event) ->
        {
            x_updates.incrementAndGet();
            System.out.println(event);
        };

        final AtomicInteger y_updates = new AtomicInteger(0);
        final PropertyChangeListener y_listener = (final PropertyChangeEvent event) ->
        {
            y_updates.incrementAndGet();
            System.out.println(event);
        };

        widget.addPropertyListener(listener);
        widget.addPropertyListener(positionX.getName(), x_listener);
        widget.addPropertyListener(positionY.getName(), y_listener);

        // Noting, yet
        assertThat(updates.get(), equalTo(0));
        assertThat(x_updates.get(), equalTo(0));
        assertThat(y_updates.get(), equalTo(0));

        // Change one
        widget.getProperty(positionX).setValue(21);
        assertThat(updates.get(), equalTo(1));
        assertThat(x_updates.get(), equalTo(1));
        assertThat(y_updates.get(), equalTo(0));

        // Change other
        widget.getProperty(positionY).setValue(21);
        assertThat(updates.get(), equalTo(2));
        assertThat(x_updates.get(), equalTo(1));
        assertThat(y_updates.get(), equalTo(1));
    }


    /** Check subscription updates */
    @Test
    public void testInvalidSubscription()
    {
        final Widget widget = new Widget("generic", "test6");
        final PropertyChangeListener listener = (p) -> {};

        try
        {
            widget.addPropertyListener("bogus", listener);
            fail("Didn't detect subscription to non-existing property");
        }
        catch (final IllegalArgumentException ex)
        {
            System.out.println("Detected: " + ex.getMessage());
        }
    }
}
