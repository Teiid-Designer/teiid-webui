package org.teiid.webui.client.widgets.validation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.teiid.webui.client.messages.ClientMessages;

/** 
 * Validates a the entered string value to ensure it is an integer
 */
@Dependent
public class IntegerValueValidator implements TextValidator {

	@Inject ClientMessages i18n;
	
	private String errorMessage = null;
	
	@Override
	public boolean validate(String value) {
		errorMessage = null;
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
	    	// i18n not initialized
			errorMessage = i18n.format("integer-value-validator.notvalid-message");
			return false;
		}
		return true;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

}
