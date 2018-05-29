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

package org.glassfish.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * LogMessageInfo annotation definition.
 *
 * message: The message to log.
 * comment: A comment which appears above the message in the
 *          LogMessages.properties file.  Useful for localization.
 * level:   The log level.  (default: INFO)
 * cause:   Describes what caused this message to be generated.
 * action:  Describes what the user/admin can do to resolve the problem.
 * publish: Boolean value indicates whether this log message should be
 *          published in the Error Reference guide. (default: true)
 *
 *  Example:
 *
 *     @LogMessageInfo(
 *              message = "This is the log message to be localized.",
 *              comment = "This is a comment about the above message.",
 *              level = "WARNING",
 *              cause = "This describes the cause of the problem...",
 *              action = "This describes the action to fix the problem...",
 *              publish = false)
 *     private static final String EJB005 = "AS-EJB-00005";
 *
 */


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface LogMessageInfo {
    String message();
    String comment() default "";
    String level() default "INFO";
    String cause() default ""; 
    String action() default "";
    boolean publish() default true;
}
