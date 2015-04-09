package org.teiid.webui.client.widgets.validation;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.constants.ValidationState;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * TextArea which validates user input
 */
@Dependent
public class ValidatingTextArea extends Composite {
    
    interface ValidatingTextBoxBinder extends UiBinder<Widget, ValidatingTextArea> {}
    private static ValidatingTextBoxBinder uiBinder = GWT.create(ValidatingTextBoxBinder.class);
 
    @UiField
    FormGroup formgroup;

    @UiField
    ExtendedTextArea fgTextArea;
    
    @UiField
    HelpBlock fgHelpBlock;
    
    private List<TextValidator> validators = new ArrayList<TextValidator>();
    private boolean isValid = true;
    private List<TextChangeListener> listeners = new ArrayList<TextChangeListener>();
    
    /**
     * Constructor
     */
    public ValidatingTextArea() {
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        
        // other initialization
        init();
    }
    
    /**
     * performs initialization after binding is complete
     */
    private void init() {
    	fgTextArea.addTextChangeEventHandler(new TextChangeEventHandler() {
    		@Override
    		public void onTextChange(TextChangeEvent event) {
    			updateStatus(fgTextArea.getText());
    			notifyListeners();
    		}
    	});
    }
    
    /**
     * Clear the list of validators
     */
    public void clearValidators( ) {
    	validators.clear();
    }

    /**
     * Add a validator
     * @param validator
     */
    public void addValidator(TextValidator validator) {
    	validators.add(validator);
    }
    
    /**
     * Add a text change listener
     * @param listener
     */
    public void addTextChangeListener(TextChangeListener listener) {
    	listeners.add(listener);
    }
    
    /**
     * Remove a text change listener
     * @param listener
     */
    public void removeTextChangeListener(TextChangeListener listener) {
    	listeners.remove(listener);
    }
    
    private void notifyListeners() {
    	for(TextChangeListener listener : listeners) {
    		listener.textChanged();
    	}
    }
    
    /**
     * Set the textBox text
     * @param textBoxText
     */
    public void setText(String textBoxText) {
    	fgTextArea.setText(textBoxText);
    	updateStatus(textBoxText);
    }

    /**
     * Get the textbox text
     * @return
     */
    public String getText( ) {
    	return fgTextArea.getText();
    }

    /**
     * Get the TextArea widget
     * @return
     */
    public TextArea getTextArea( ) {
    	return fgTextArea;
    }
    
    /**
     * Determine if Text area is valid
     * @return
     */
    public boolean isValid() {
    	return isValid;
    }
    
    /**
     * Set the number of visible lines in the text area
     * @param nLines
     */
    public void setVisibleLines(int nLines) {
    	fgTextArea.setVisibleLines(nLines);
    }
    
    /**
     * Update the status
     */
    private void updateStatus(String textValue) {
    	isValid = true;
    	String msg = null;
    	
    	for(TextValidator vdater : validators) {
    		if(!vdater.validate(textValue)) {
    			isValid = false;
    			msg = vdater.getErrorMessage();
    			break;
    		}
    	}
    	
    	if(isValid) {
    		formgroup.setValidationState(ValidationState.NONE);
    		fgHelpBlock.setText(null);
    	} else {
    		formgroup.setValidationState(ValidationState.ERROR);
    		fgHelpBlock.setText(msg);
    	}
    	
    }
}