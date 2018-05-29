/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.foo.bar;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * The @LogMessagesResourceBundle annotation has a non-standard value which
 * causes a warning messages during the compilation. 
 */
public class Coffee {

    // The resourceBundle name to be used for the module's log messages
    @LogMessagesResourceBundle
    public static final String LOGMESSAGES_RB = "com.foo.bar.EJBLogMessagesCatalog";

    @LoggerInfo(subsystem="EJB", description="Main EJB Logger", publish=true)
    public static final String EJB_LOGGER_NAME = "javax.enterprise.ejb";
    
    private static final Logger EJB_LOGGER =
      Logger.getLogger(EJB_LOGGER_NAME, LOGMESSAGES_RB);

    // Define the log message
    @LogMessageInfo(
        message = "EJB subsystem initialized.",
        comment = "This message indicates that the EJB container initialized successfully.",
        level = "INFO")
    public static final String EJB_SYSTEM_INITIALIZED = "AS-EJB-00003";

    @LogMessageInfo(
        message = "EJB module {0} failed to deploy.",
        comment = "This log message indicates a failure to deploy the given EJB module.",
        level = "SEVERE",
        cause = "Could not deploy an EJB module",
        action = "Verify the deployment descriptor.")
    public static final String EJB_DEPLOYMENT_FAILED = "AS-EJB-00004";

    public void initialize() {
        EJB_LOGGER.log(Level.INFO, EJB_SYSTEM_INITIALIZED);
    }

    public void deploy(String module) {
        EJB_LOGGER.log(Level.SEVERE, EJB_DEPLOYMENT_FAILED, module);
    }
    
}
