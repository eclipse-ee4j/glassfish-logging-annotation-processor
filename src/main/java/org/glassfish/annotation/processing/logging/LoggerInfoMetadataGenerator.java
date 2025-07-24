/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.glassfish.logging.annotation.LoggerInfo;

@SupportedAnnotationTypes({"org.glassfish.logging.annotation.LoggerInfo"})
public class LoggerInfoMetadataGenerator extends BaseLoggingProcessor {

    private static final String PUBLISH_SUFFIX = ".publish";
    private static final String SUBSYSTEM_SUFFIX = ".subsystem";
    private static final String DESCRIPTION_SUFFIX = ".description";
    // private static final String RBNAME = "loggerinfo.LoggerInfoMetadata";
    private static final String RBNAME = "META-INF/loggerinfo/LoggerInfoMetadata";
    private static final String VALID_PATTERN = "[a-z[A-Z]][^|]*";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process (Set<? extends TypeElement> annotations, 
            RoundEnvironment env) {

        debug("LoggerInfoMetadataGenerator invoked.");

        LoggingMetadata loggerMetadata = new LoggingMetadata();
        loadLogMessages(loggerMetadata, RBNAME);
        debug("Total Messages including ones found from disk so far: " + loggerMetadata);

        if (!env.processingOver()) {

            SortedMap<String, Element> loggerInfoElements = new TreeMap<String, Element>();

            Set<? extends Element> elements = env.getElementsAnnotatedWith(LoggerInfo.class);
            if (elements.isEmpty()) {
                return false;
            }
            
            Iterator<? extends Element> it = elements.iterator();
            while (it.hasNext()) {
                VariableElement element = (VariableElement)it.next();
                String loggerName = (String)element.getConstantValue();
                if (loggerName == null) {
                    StringBuffer buf = new StringBuffer();
                    buf.append("Logger name must be a constant string literal value, it cannot be a compile time computed expression.");
                    buf.append(System.getProperty("line.separator"));
                    buf.append("Please check if the LoggerInfo annotation is on the logger name constant.");
                    error(buf.toString());
                    return false;
                }
                debug("Processing: " + loggerName + " on element " + element.getSimpleName());
                debug("Enclosing type is " + element.getEnclosingElement().asType());
                
                LoggerInfo loggerInfo = element.getAnnotation(LoggerInfo.class);
                validateLoggerInfo(loggerInfo);
                // Save the log message...
                // Message ids must be unique
                if (loggerInfoElements.containsKey(loggerName)) {
                    // Previous entry with same logger name found.
                    LoggerInfo prevLoggerInfo = loggerInfoElements.get(loggerName).getAnnotation(LoggerInfo.class);
                    if (!compareLoggerInfos(loggerInfo, prevLoggerInfo)) {
                        warn("Overwriting entry for logger " + loggerName);
                    }
                } else {
                    renderLoggerInfo(loggerMetadata, loggerName, loggerInfo);
                    loggerInfoElements.put(loggerName, element);
                }
            }
            debug("Loggers found so far: " + loggerMetadata);
            info("Generating logger metadata service.");
            // Get the root logger element
            Element baseLoggerElement = loggerInfoElements.get(loggerInfoElements.firstKey());
            boolean result = generateLoggerInfoMetadataService(baseLoggerElement, loggerMetadata);
            info("Annotation processing finished successfully.");
            return result; // Claim the annotations
        } else {
            return false;
        }
    }
    
    private boolean compareLoggerInfos(LoggerInfo info1, LoggerInfo info2) {
        return (info1.description().equals(info2.description()) &&
                info1.subsystem().equals(info2.subsystem()) &&
                info1.publish() == info2.publish());
    }

    private void validateLoggerInfo(LoggerInfo loggerInfo) {
        if (!Pattern.matches(VALID_PATTERN, loggerInfo.subsystem())) {
            error("Subsystem name is not valid: " + loggerInfo.subsystem());
        }       
        if (!Pattern.matches(VALID_PATTERN, loggerInfo.description())) {
            error("Description for the Logger is not valid: " + loggerInfo.description());            
        }        
    }

    private boolean generateLoggerInfoMetadataService(Element element, LoggingMetadata loggerInfos) {
        String packageName = null;
        do {
            Element enclosing = element.getEnclosingElement();
            debug("Found enclosing element " + element);
            if (enclosing.getKind() == ElementKind.PACKAGE) {
                packageName = enclosing.toString();
            }
            element = enclosing;
        } while(packageName == null);
        
        try {
            // Now persist the resource bundle
            // String resourceName = packageName + "." + RBNAME;
            String resourceName = RBNAME;
            storeLogMessages(loggerInfos, resourceName);            
        } catch (Exception e) {
            error("Unable to generate LoggerMetadataInfoService class", e);
            return false;
        }
        return true;
    }

    private boolean renderLoggerInfo(LoggingMetadata loggerMetadata, 
            String loggerName, LoggerInfo loggerInfo) {
        loggerMetadata.put(loggerName + DESCRIPTION_SUFFIX, loggerInfo.description());
        loggerMetadata.put(loggerName + SUBSYSTEM_SUFFIX, loggerInfo.subsystem());
        loggerMetadata.put(loggerName + PUBLISH_SUFFIX, loggerInfo.publish());
        return true;
    }
    
}
