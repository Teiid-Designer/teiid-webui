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
package org.teiid.webui.share.services;

import org.teiid.webui.share.Constants;


public final class StringUtils {
	
	public static String checkValidServiceName(String serviceName) {
		String statusMsg = Constants.OK;
				
		// Check that service name is not empty
        if(isEmpty(serviceName)) {
        	return Constants.SERVICE_NAME_CANNOT_BE_EMPTY_MSG;
        }
        
        // Must start with a letter
        char c = serviceName.charAt(0);
        if(!Character.isLetter(c)) {
        	return Constants.SERVICE_NAME_FIRST_CHAR_MUST_BE_ALPHA_MSG;
        }
        
        // Check that remaining chars are 1) letter, 2) digit, or 3) underscore
        int length = serviceName.length();
        for (int index = 1; index < length; index++) {
        	c = serviceName.charAt(index);
        	if(!Character.isLetter(c) && !Character.isDigit(c) && !(c=='_') ) {
        		statusMsg = "The service name character '"+ c + "' at position ["+index+"] is invalid";
        	}
        	if(!statusMsg.equals(Constants.OK)) {
        		break;
        	}
        }
        
        return statusMsg;
	}
	
	/**
	 * <p>
	 * Returns whether the specified text is either empty or null.
	 * </p>
	 *
	 * @param text The text to check; may be null;
	 * @return True if the specified text is either empty or null.
	 * @since 4.0
	 */
	public static boolean isEmpty( final String text ) {
		return (text == null || text.length() == 0);
	}

	/**
	 * Compare string values - considered equal if either are null or empty.
	 *
	 * @param thisValue the first value being compared (can be <code>null</code> or empty)
	 * @param thatValue the other value being compared (can be <code>null</code> or empty)
	 * @return <code>true</code> if values are equal or both values are empty
	 */
	public static boolean valuesAreEqual( String thisValue,
			String thatValue ) {
		if (isEmpty(thisValue) && isEmpty(thatValue)) {
			return true;
		}

		return equals(thisValue, thatValue);
	}

	/**
	 * @param thisString the first string being compared (may be <code>null</code>)
	 * @param thatString the other string being compared (may be <code>null</code>)
	 * @return <code>true</code> if the supplied strings are both <code>null</code> or have equal values
	 */
	public static boolean equals( final String thisString,
			final String thatString ) {
		if (thisString == null) {
			return (thatString == null);
		}

		return thisString.equals(thatString);
	}
	    
}
