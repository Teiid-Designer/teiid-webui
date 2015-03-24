package org.teiid.webui.client.widgets.validation;

import javax.enterprise.context.Dependent;

import org.teiid.webui.share.Constants;
import org.teiid.webui.share.services.StringUtils;

/** 
 * Validates a string value to ensure it is a valid service name.
 */
@Dependent
public class ServiceNameValidator implements TextValidator {

	private String errorMessage = null;
	
	@Override
	public boolean validate(String value) {
 		errorMessage = StringUtils.checkValidServiceName(value);
		if(!errorMessage.equals(Constants.OK)) {
			return false;
		}
		return true;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

}
