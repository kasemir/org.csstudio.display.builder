/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.runtime.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.python.core.PySystemState;

/**
 * This suite was necessary to impose {@link JythonTest} being executed
 * before {@link JythonScriptTest}, otherwise the {@link PySystemState}
 * will be already initialized.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 27 Sep 2018
 */
@RunWith( Suite.class )
@Suite.SuiteClasses( {
    //  Keep the following classes in this specifi order.
    JythonTest.class,
    JythonScriptTest.class,
    RulesJythonScriptTest.class,
    //  The following classes can be in any order.
    ArrayPVDispatcherTest.class,
    CommandExecutorTest.class,
    PVFactoryTest.class,
    PythonGatewaySupportTest.class,
    PythonScriptTest.class,
    TextPatchTest.class,
} )
@SuppressWarnings( { "ClassMayBeInterface", "ClassWithoutLogger" } )
public class RuntimeTestsSuite {

}
