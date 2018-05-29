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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

public abstract class BaseLoggingProcessor extends AbstractProcessor {

    protected void debug(String msg) {
        processingEnv.getMessager().printMessage(Kind.OTHER, msg);
    }

    protected void debug(String msg, Throwable t) {
        processingEnv.getMessager().printMessage(Kind.OTHER, t.getMessage() + ":" + msg);
    }

    protected void info(String msg) {
        processingEnv.getMessager().printMessage(Kind.NOTE, 
            getClass().getName() + ": " + msg);
    }

    protected void warn(String msg) {
        processingEnv.getMessager().printMessage(Kind.WARNING, 
                getClass().getName() + ": " + msg);
    }

    protected void warn(String msg, Throwable t) {
        String errMsg = msg + ": " + t.getMessage();
        processingEnv.getMessager().printMessage(Kind.WARNING, 
                getClass().getName() + ": " + errMsg);
    }

    protected void error(String msg) {
        processingEnv.getMessager().printMessage(Kind.ERROR, 
                getClass().getName() + ": " + msg);
    }

    protected void error(String msg, Throwable t) {
        String errMsg = msg + ": " + t.getMessage();
        processingEnv.getMessager().printMessage(Kind.ERROR, 
                getClass().getName() + ": " + errMsg);
    }

    /**
     * This method, given a pkg name will determine the path to the resource,
     * create the LogResourceBundle for that path and load any resources
     * from the existing resource bundle file.
     * 
     * @param rbName the package the resource bundle is relative
     * @return a LogResourceBundle
     */
    protected void loadLogMessages(LoggingMetadata lrb, String rbName) {
    
        BufferedReader bufferedReader = null;
        try {
            FileObject rbFileObject = getRBFileObject(rbName, true);
            if (rbFileObject.getLastModified() > 0) {
                bufferedReader = new BufferedReader(new InputStreamReader(
                        rbFileObject.openInputStream()));
                lrb.load(bufferedReader);                
            }
        } catch (IllegalArgumentException e) {
            error("Unable to load resource bundle: " + 
                    rbName, e);
        } catch (IOException e) {
            debug("Unable to load resource bundle: " +
                    rbName, e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    error("Unable to close reader for resource bundle: " +
                            rbName, e);
                }
            }
        }
    }

    protected void storeLogMessages(LoggingMetadata lrb, String rbName) {
        BufferedWriter bufferedWriter = null; 
        try {
            FileObject rbFileObject = getRBFileObject(rbName, false);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    rbFileObject.openOutputStream()));
            lrb.store(bufferedWriter);
        } catch (IllegalArgumentException e) {
            error("Unable to store resource bundle: " +
                    rbName, e);
        } catch (IOException e) {
            error("Unable to store resource bundle: " +
                    rbName, e);
        }  finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    error("Unable to store resource bundle: " +
                            rbName, e);
                }
            }
        } 
    }

    /**
     * We cache paths to the resource bundle because the compiler does not
     * allow us to call createResource() more than once for an object.
     * Note that in Java 7 getResource() throws FileNotFound if the
     * target resource does not exist.   The behavior is different in Java 6.
     * 
     * @param rbName
     * @return path to resource bundle relative to pkg 
     */
    private FileObject getRBFileObject(String rbName, boolean readObject) 
    throws IllegalArgumentException, IOException 
    {        
        String rbFileName = rbName;
        String rbPkg = "";
        int lastIndex = rbName.lastIndexOf('.');
        if (lastIndex > 0) {
            rbFileName = rbName.substring(lastIndex + 1);
            rbPkg = rbName.substring(0, lastIndex);
        }
        rbFileName = rbFileName + ".properties";
        if (readObject) {
            return processingEnv.getFiler().getResource(
                    StandardLocation.CLASS_OUTPUT, rbPkg, rbFileName);
        } else {
            return processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT, rbPkg, rbFileName,
                    (javax.lang.model.element.Element[]) null);
        }
    }
}
