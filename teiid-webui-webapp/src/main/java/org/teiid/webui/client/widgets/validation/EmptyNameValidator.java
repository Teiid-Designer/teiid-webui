package org.teiid.webui.client.widgets.validation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.teiid.webui.client.messages.ClientMessages;

/** 
 * Validates a string value to ensure its not null or empty.
 */
@Dependent
public class EmptyNameValidator implements TextValidator {

	@Inject ClientMessages i18n;
	
	private String errorMessage = null;
	
	@Override
	public boolean validate(String value) {
		errorMessage = null;
		if(value==null || value.trim().length()==0) {
	    	// i18n not initialized
			errorMessage = i18n.format("empty-text-validator.empty-message");
			return false;
		}
		return true;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

}
