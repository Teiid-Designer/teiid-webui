package org.teiid.webui.client.widgets.validation;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.teiid.webui.client.dialogs.UiEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * TextBox which validates user input
 */
@Dependent
public class ValidatingTextBoxHoriz extends Composite {
    
    interface ValidatingTextBoxHorizBinder extends UiBinder<Widget, ValidatingTextBoxHoriz> {}
    private static ValidatingTextBoxHorizBinder uiBinder = GWT.create(ValidatingTextBoxHorizBinder.class);
 
    @UiField
    FormGroup formgroup;

    @UiField
    ExtendedTextBox fgTextBox;
    
    @UiField
    FormLabel fgLabel;

    @UiField
    HelpBlock fgHelpBlock;
    
    @Inject Event<UiEvent> kUpEvent;
       
    private List<TextValidator> validators = new ArrayList<TextValidator>();
    private boolean isValid = true;
    private List<TextChangeListener> listeners = new ArrayList<TextChangeListener>();
    
    /**
     * Constructor
     */
    public ValidatingTextBoxHoriz() {
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        
        // other initialization
        init();
    }
    
    /**
     * performs initialization after Ui binding is complete
     */
    private void init() {
    	fgTextBox.addTextChangeEventHandler(new TextChangeEventHandler() {
    		@Override
    		public void onTextChange(TextChangeEvent event) {
    			updateStatus(fgTextBox.getText());
    			notifyListeners();
    		}
    	});
    }
    
    public void setPassword(boolean isPassword) {
    	if(isPassword) {
        	this.fgTextBox.getElement().setAttribute("type", "password");
    	}
    }
    
    public void setLabelVisible(boolean isVisible) {
    	this.fgLabel.setVisible(isVisible);
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
     * Add a Text ChangeListener
     * @param listener
     */
    public void addTextChangeListener(TextChangeListener listener) {
    	listeners.add(listener);
    }
    
    /**
     * Remove a Text ChangeListener
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
     * Set the label text
     * @param labelTxt
     */
    public void setLabel(String labelTxt) {
    	fgLabel.setText(labelTxt);
    }
    
    /**
     * Set the label HTML
     * @param label Html
     */
    public void setLabelHTML(String labelHtml) {
    	fgLabel.setHTML(labelHtml);
    }
    
    /**
     * Set the text box text
     * @param textBoxText
     */
    public void setText(String textBoxText) {
    	fgTextBox.setText(textBoxText);
    	updateStatus(textBoxText);
    }

    /**
     * Get the TextBox text
     * @return
     */
    public String getText( ) {
    	return fgTextBox.getText();
    }

    /**
     * Get the text box widget
     * @return
     */
    public TextBox getTextBox( ) {
    	return fgTextBox;
    }
    
    /**
     * Determine validity status
     * @return
     */
    public boolean isValid() {
    	return isValid;
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