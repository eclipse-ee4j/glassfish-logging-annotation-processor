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

package org.glassfish.annotation.processing.logging;

import java.util.Calendar;

/*
 * Generates a String representation of the GPL copyright using the 
 * current year.
 */

public class GPLCopyright {

    private static final String YEAR_PATTERN = "XX_YEAR_XX";

    private static final String GPLCOPYRIGHT_UNDATED =
 "#\n" +
 "# Copyright (c) XX_YEAR_XX Oracle and/or its affiliates. All rights reserved.\n" +
 "#\n" +
 "# This program and the accompanying materials are made available under the\n" +
 "# terms of the Eclipse Public License v. 2.0, which is available at\n" +
 "# http://www.eclipse.org/legal/epl-2.0.\n" +
 "#\n" +
 "# This Source Code may also be made available under the following Secondary\n" +
 "# Licenses when the conditions for such availability set forth in the\n" +
 "# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,\n" +
 "# version 2 with the GNU Classpath Exception, which is available at\n" +
 "# https://www.gnu.org/software/classpath/license.html.\n" +
 "# \n" +
 "# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0\n" +
 "#\n\n";

    public GPLCopyright() { }

    public static String getCopyright() {
        int year = Calendar.getInstance().get(Calendar.YEAR);

        return GPLCOPYRIGHT_UNDATED.replaceFirst(
                YEAR_PATTERN, String.valueOf(year));
    }
}
