package org.teiid.webui.client.widgets.validation;

/**
 * Text validator interface
 *
 */
public interface TextValidator {
	public boolean validate(String value);

	public String getErrorMessage(); 
}
