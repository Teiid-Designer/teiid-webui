package org.teiid.webui.client.widgets.validation;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.share.services.StringUtils;

/** 
 * Validates a string value against the list of existing names, to check whether the value
 * already exists.
 */
@Dependent
public class DuplicateNameValidator implements TextValidator {

	@Inject ClientMessages i18n;
	
	private String errorMessage = null;
	private Collection<String> existingNames;
	
	public DuplicateNameValidator() {
		this.existingNames = new ArrayList<String>();
	}
	
	public DuplicateNameValidator(Collection<String> existingNames) {
		this.existingNames = existingNames;
	}
	
	@Override
	public boolean validate(String value) {
		errorMessage = null;
		if(StringUtils.isEmpty(value)) return false;
		
		if(this.existingNames!=null && this.existingNames.contains(value)) {
			errorMessage = i18n.format("duplicate-name-validator.name-exists-msg",value); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

}
