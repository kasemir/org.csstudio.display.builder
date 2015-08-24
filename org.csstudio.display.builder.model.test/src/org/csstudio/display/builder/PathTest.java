/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.csstudio.display.builder.model.util.ResourceUtil;
import org.junit.Test;

/** JUnit test of path handling
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PathTest
{
    @Test
    public void testPaths() throws Exception
    {
        String path;

        path = ResourceUtil.normalize("C:\\some path\\subdir\\file.opi");
        assertThat(path, equalTo("C:/some path/subdir/file.opi"));

        path = ResourceUtil.combineDisplayPaths(null, "example.opi");
        assertThat(path, equalTo("example.opi"));

        path = ResourceUtil.combineDisplayPaths("examples/dummy.opi", "example.opi");
        assertThat(path, equalTo("examples/example.opi"));

        path = ResourceUtil.combineDisplayPaths("examples/dummy.opi", "scripts/test.py");
        assertThat(path, equalTo("examples/scripts/test.py"));

        path = ResourceUtil.combineDisplayPaths("https://webopi.sns.gov/webopi/opi/Instruments.opi", "../../share/opi/Motors/motor.opi");
        assertThat(path, equalTo("https://webopi.sns.gov/share/opi/Motors/motor.opi"));

        path = ResourceUtil.combineDisplayPaths("https://webopi.sns.gov/webopi/opi/Instruments.opi", "/home/beamline/main.opi");
        assertThat(path, equalTo("/home/beamline/main.opi"));
    }
}
