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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.TreeMap;

class LoggingMetadata extends TreeMap<String,Object>{

    private static final long serialVersionUID = 4958871376396137141L;
        
    private static final String COMMENT = ".MSG_COMMENT";
    
    public LoggingMetadata() { }

    public LoggingMetadata load(BufferedReader reader) throws IOException {
        String line; 
        String unusedLine = null; 
        int pos;

        try {
            //Read File Line By Line
            while ((line = nextLine(unusedLine, reader)) != null)   {
                unusedLine = null;

                // We found a comment...
                if (line.indexOf('#') == 0) {
                    String commentLine = line;

                    // We expect a prop=value after the comment.
                    if ((line = reader.readLine()) == null) continue;

                    // A comment may follow a comment.   Ignore the first
                    // comment.
                    pos = line.indexOf('#');
                    if (line.indexOf('#') == 0) {
                        unusedLine = line;
                        continue;
                    }

                    pos = line.indexOf('=');
                    if (pos == -1) {
                        // The previous line was a lone comment - ignore it.
                        // Push the current line back to be reread during
                        // the next pass.
                        unusedLine = line;
                        continue;
                    } else {
                        unusedLine = null;
                        setProperty(line, pos, commentLine);
                    }
                    continue;
                } else if ((pos = line.indexOf('=')) != -1) {
                    setProperty(line, pos);
                    continue;
                }

                // We ignore all whitespace or lines that are not prop/values
                // or comments.
            }
        } finally {
            if (reader != null)
                reader.close();
        }

        return this;
    }

    /**
     * Store the resource bundle to the Writer object.   
     *    @return if contents were written to the object.
     */
    public boolean store(Writer out) throws IOException {

        // Noting to store.
        if (isEmpty()) return false;

        out.write(GPLCopyright.getCopyright());

        for (String key : keySet()) {
            // Skip comments until needed after writing prop. 
            if (key.endsWith(COMMENT)) continue;

            if (containsKey(key + COMMENT)) 
                out.write(get(key + COMMENT) + "\n");
            Object value = get(key);
            if (value != null) {
                value = escapeSpecialChars(value.toString());
            } else {
                value = "";
            }
            out.write(key + "=" + value + "\n\n");
        }
        out.flush();

        return true;
    }

    private String escapeSpecialChars(String str) {
        return str.replaceAll("\\n", "\\\\n");
    }

    public void putComment(String key, String comment) {
        put(key + COMMENT, "# " + comment);
    }

    private void setProperty(String line, int pos, String commentLine) {
        String key = line.substring(0, pos).trim();
        String value = line.substring(pos + 1).trim();
        put(key, value);
        put(key + COMMENT, commentLine);
    }

    private void setProperty(String line, int pos) {
        String key = line.substring(0, pos).trim();
        String value = line.substring(pos + 1).trim();
        put(key, value);
    }

    // Returns either the unusedLine or reads a new line if the 
    // unusedLine is null.
    private String nextLine(String unusedLine, BufferedReader reader) 
            throws java.io.IOException {

        if (unusedLine != null) return unusedLine;

        return reader.readLine();
    }
}
