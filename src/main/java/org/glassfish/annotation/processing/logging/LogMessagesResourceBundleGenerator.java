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

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

@SupportedAnnotationTypes({"org.glassfish.logging.annotation.LogMessageInfo","org.glassfish.logging.annotation.LogMessagesResourceBundle"})
public class LogMessagesResourceBundleGenerator extends BaseLoggingProcessor {

    private static final String DETAILS_SUFFIX = "_details";

    private static final String RESOURCE_BUNDLE_KEY = "resourceBundle";

    private static final String VALIDATE_LEVELS[] = {
      "EMERGENCY",
      "ALERT",
      "SEVERE",
    };
    
    private static final String LOG_MESSAGES_METADATA = "META-INF/logmessages/LogMessagesMetadata";
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process (Set<? extends TypeElement> annotations, 
            RoundEnvironment env) {

        debug("LogMessagesResourceBundleGenerator invoked.");
        
        if (!env.processingOver()) {

            LoggingMetadata logMessagesMap = new LoggingMetadata();
            LoggingMetadata logMessagesDetails = new LoggingMetadata();
            LoggingMetadata logMessagesMetada = new LoggingMetadata();

            Set<? extends Element> logMessageElements = env.getElementsAnnotatedWith(LogMessageInfo.class);
            Set<? extends Element> logMessagesResourceBundleElements = env.getElementsAnnotatedWith(LogMessagesResourceBundle.class);

            Set<String> rbNames = new HashSet<String>();

            if (logMessagesResourceBundleElements.isEmpty() || logMessageElements.isEmpty()) {
                loadLogMessages(logMessagesMetada, LOG_MESSAGES_METADATA);
                if (logMessagesMetada.containsKey(RESOURCE_BUNDLE_KEY)) {
                    String rb = (String) logMessagesMetada.get(RESOURCE_BUNDLE_KEY);
                    if (rb != null && !rb.isEmpty()) {
                        rbNames.add(rb);
                    }
                } else {
                    warn("Skipping LogMessages resource bundle generation, either the LogMessageInfo or LogMessagesResourceBundle annotation is not specified in the current compilation round.");
                    return false;                    
                }
            }
                        
            for (Element rbElem : logMessagesResourceBundleElements) {
                if (!(rbElem instanceof VariableElement)) {
                    error("The LogMessagesResourceBundle annotation is applied on an invalid element.");
                    return false;
                }
                Object rbValue = ((VariableElement) rbElem).getConstantValue();
                if (rbValue == null) {
                    error("The resource bundle name value could not be computed. Specify the LogMessagesResourceBundle annotation only on a compile time constant String literal field in the class.");
                    return false;                    
                }
                rbNames.add(rbValue.toString());
            }
            if (rbNames.isEmpty()) {
                error("No resource bundle name found. Atleast one String literal constant needs to be decorated with the LogMessagesResourceBundle annotation.");
                return false;                
            }
            if (rbNames.size() > 1) {
                error("More than one resource bundle name specified. Found the following resource bundle names: " 
                        + rbNames + ". Please specify only one resource bundle name per module.");
                return false;
            }
            
            String rbName = rbNames.iterator().next();
            if (!rbName.endsWith("LogMessages")) {
                error("The resource bundle name '" + rbName + "' annotated by @LogMessagesResourceBundle does not end with 'LogMessages'");
                return false;
            }

            Iterator<? extends Element> it = logMessageElements.iterator();
            Set<String> messageIds = new HashSet<String>();
            
            loadLogMessages(logMessagesMap, rbName);
            loadLogMessages(logMessagesDetails, rbName + DETAILS_SUFFIX);
            debug("Initial messages found so far: " + logMessagesMap);

            while (it.hasNext()) {
                Element elem = it.next();
                if (!(elem instanceof VariableElement)) {
                    error("The LogMessageInfo annotation is applied on an invalid element.");
                    return false;
                }
                VariableElement varElem = (VariableElement)elem;
                String msgId = (String)varElem.getConstantValue();
                if (msgId == null) {
                    error("The LogMessageInfo annotation is not applied on a String constant field.");
                    return false;                    
                }
                debug("Processing: " + msgId);
                // Message ids must be unique
                if (!messageIds.contains(msgId)) {
                    LogMessageInfo lmi = varElem.getAnnotation(LogMessageInfo.class);
                    checkLogMessageInfo(msgId, lmi);

                    // Save the log message...
                    logMessagesMap.put(msgId, lmi.message());
                    // Save the message's comment if it has one...
                    String comment = lmi.comment();
                    if (comment != null && !comment.isEmpty()) {
                        logMessagesMap.putComment(msgId, comment);
                        logMessagesDetails.put(msgId+".comment", comment);
                    }
                    String cause = lmi.cause();
                    if (cause == null) {
                        cause = "";
                    }
                    String action = lmi.action();
                    if (action == null) {
                        action = "";
                    }
                    String level = lmi.level();
                    if (level == null || level.isEmpty()) {
                        level = "INFO";
                    }
                    logMessagesDetails.put(msgId+".cause", cause);
                    logMessagesDetails.put(msgId+".action", action);
                    logMessagesDetails.put(msgId+".level", level);
                    messageIds.add(msgId);
                } else {
                    error("Duplicate use of message-id " + msgId);
                }
            }
            debug("Total Messages including ones found from disk so far: " + logMessagesMap);
            storeLogMessages(logMessagesMap, rbName);
            storeLogMessages(logMessagesDetails, rbName + DETAILS_SUFFIX);
            // Store the package name of the LogMessages resource
            logMessagesMetada.put(RESOURCE_BUNDLE_KEY, rbName);
            storeLogMessages(logMessagesMetada, LOG_MESSAGES_METADATA);
            info("Annotation processing finished successfully.");
            return true; // Claim the annotations
        } else {
            return false;
        }
    }    

    private void checkLogMessageInfo(String msgId, LogMessageInfo lmi) {
      boolean needsCheck = false;
      for (String checkLevel : VALIDATE_LEVELS) {
        if (checkLevel.equals(lmi.level())) {
          needsCheck = true;
        }
      }
      debug("Message " + msgId + " needs checking for cause/action: " + needsCheck);
      if (needsCheck) {
        if (lmi.cause().trim().length() == 0) {
          error("Missing cause for message id '" + msgId + "' for levels SEVERE and above.");
        }
        if (lmi.action().trim().length() == 0) {
          error("Missing action for message id '" + msgId + "' for levels SEVERE and above.");
        }
      }
    }
    
}
