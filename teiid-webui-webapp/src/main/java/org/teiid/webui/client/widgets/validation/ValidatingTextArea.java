package org.teiid.webui.client.widgets.validation;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.teiid.webui.client.dialogs.UiEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
    TextArea fgTextArea;
    
    @UiField
    HelpBlock fgHelpBlock;
    
    @Inject Event<UiEvent> kUpEvent;
    private String bSpaceText;
       
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
    	fgTextArea.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
            	// Update status
    			int keyCode = event.getNativeEvent().getKeyCode();
    			if(keyCode==KeyCodes.KEY_BACKSPACE) {
    		    	bSpaceText = fgTextArea.getText();
    			}
            }
        });
    	fgTextArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	// Update status
    			int keyCode = event.getNativeEvent().getKeyCode();
    			if(keyCode==KeyCodes.KEY_BACKSPACE) {
    		    	if(bSpaceText!=null && bSpaceText.length()>0) {
        		    	updateStatus(bSpaceText.substring(0,bSpaceText.length()-1));
        		    	notifyListeners();
    		    	}
    			} else {
    		    	updateStatus(fgTextArea.getText());
    		    	notifyListeners();
    			}
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