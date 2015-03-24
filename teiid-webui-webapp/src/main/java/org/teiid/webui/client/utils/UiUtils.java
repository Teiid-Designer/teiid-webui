/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.teiid.webui.client.utils;

import com.google.gwt.user.client.ui.Label;

/**
 * Ui Utilities
 * Constants static methods for common ui tasks
 * @author mdrilling
 */
public class UiUtils {

	public enum MessageType {
        INFO, 
        WARNING,
        ERROR,
        SUCCESS
    }
	
	public static String getStatusMessageHtml(String message, MessageType msgType) {
		String htmlMsg = null;
    	if(msgType.equals(MessageType.INFO)) {
    		htmlMsg = "<h6 style=\"color:black;font-style:italic;font-weight:bold\">"+message+"</h6>";
    	} else if(msgType.equals(MessageType.WARNING)) {
    		htmlMsg = "<h6 style=\"color:black;font-style:italic;font-weight:bold\">"+message+"</h6>";
    	} else if(msgType.equals(MessageType.ERROR)) {
    		htmlMsg = "<h6 style=\"color:red;font-style:italic;font-weight:bold\">"+message+"</h6>";
    	} else if(msgType.equals(MessageType.SUCCESS)) {
    		htmlMsg = "<h6 style=\"color:black;font-style:italic;font-weight:bold\">"+message+"</h6>";
    	}
    	return htmlMsg;
	}
	
	/**
	 * Set the style type for alert labels
	 * @param statusLabel the label
	 * @param msgType the message type
	 */
    public static void setMessageStyle(Label statusLabel, MessageType msgType) {
    	statusLabel.removeStyleName("alert-info");
    	statusLabel.removeStyleName("alert-warning");
    	statusLabel.removeStyleName("alert-danger");
    	statusLabel.removeStyleName("alert-success");
    	if(msgType.equals(MessageType.INFO)) {
    		statusLabel.addStyleName("alert-info");
    	} else if(msgType.equals(MessageType.WARNING)) {
    		statusLabel.addStyleName("alert-warning");
    	} else if(msgType.equals(MessageType.ERROR)) {
    		statusLabel.addStyleName("alert-danger");
    	} else if(msgType.equals(MessageType.SUCCESS)) {
    		statusLabel.addStyleName("alert-success");
    	}
    }
		
}
