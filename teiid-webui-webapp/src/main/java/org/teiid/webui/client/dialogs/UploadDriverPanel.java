package org.teiid.webui.client.dialogs;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.teiid.webui.client.utils.UiUtils;
import org.teiid.webui.client.utils.UiUtils.MessageType;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.services.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 *  Dialog Panel for Upload of JDBC Drivers 
 */
public class UploadDriverPanel extends Composite {

    interface UploadDriverPanelBinder extends UiBinder<Widget, UploadDriverPanel> {}
    private static UploadDriverPanelBinder uiBinder = GWT.create(UploadDriverPanelBinder.class);
    
	@UiField
	FormPanel form;
	
	@UiField(provided = true)
	FileUpload fileUpload;
	
	@UiField
	TextBox nameTextBox;
	
	@UiField
	Button submitButton;

	@UiField
	Button cancelButton;
	
	@UiField
	Label msgLabel;
	
	UploadDriverDialog parentDialog;
	
	private static final String STATUS_SELECT_DRIVER = "Please choose a JDBC Type 4 driver";
	private static final String STATUS_ENTER_NAME = "Please enter a name for the new type";
	private static final String STATUS_CLICK_UPLOAD = "Click 'Upload' to add the new type";
	
	/**
	 * Constructor
	 * @param parentDialog the owner dialog
	 */
	public UploadDriverPanel(UploadDriverDialog parentDialog) {
		this.parentDialog = parentDialog;
		fileUpload = createFileUpload();
		
        initWidget(uiBinder.createAndBindUi(this));
        
        init();
	}
	
	private FileUpload createFileUpload() {
	    // Create a FileUpload widget.
	    FileUpload upload = new FileUpload();
	    upload.setName("uploadFormElement");

	    return upload;
	}
	
	private void init() {
		form.setAction(getWebContext() + "/services/dataVirtUpload");

	    form.setEncoding(FormPanel.ENCODING_MULTIPART);
	    form.setMethod(FormPanel.METHOD_POST);
		
	    // Add an event handler to the form.
	    form.addSubmitHandler(new FormPanel.SubmitHandler() {
	    	public void onSubmit(SubmitEvent event) {
	    		// This event is fired just before the form is submitted. Can do additional validation here if desired.
	    		if (nameTextBox.getText().length() == 0) {
	    			Window.alert("The text box must not be empty");
	    			event.cancel();
	    		} else {
	    			parentDialog.uploadStarting();
	    		}
	    	}
	    });
	    
	    form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
	    	public void onSubmitComplete(SubmitCompleteEvent event) {
	    		// When the form submission is successfully completed, this event is fired.
    			String results = event.getResults();
    			parentDialog.uploadComplete(results);
	    	}
	    });
	    
	    fileUpload.addChangeHandler(new ChangeHandler() {
	        public void onChange(ChangeEvent event) {
            	// Update status
            	updateStatus();
	        }
	    });
	    
	    msgLabel.setText(STATUS_SELECT_DRIVER);
	    setMessageStyle(msgLabel, UiUtils.MessageType.ERROR);
	    
	    nameTextBox.setName("driverDeploymentName");
	    nameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	// Update status
            	updateStatus();
            }
        });

	    submitButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
    	        form.submit();
    		}
    	});  
	    submitButton.setEnabled(false);
	    
	    cancelButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
    	        parentDialog.close();
    		}
    	});   
	    
	}
	
	private void updateStatus() {
		String status = Constants.OK;
		
		// Ensure that a file is selected
		if(StringUtils.isEmpty(fileUpload.getFilename())) {
			status = STATUS_SELECT_DRIVER;
    		msgLabel.setText(status);
        	setMessageStyle(msgLabel, UiUtils.MessageType.ERROR);
		}
		
		if(status.equals(Constants.OK)) {
			if(StringUtils.isEmpty(nameTextBox.getText())) {
				status = STATUS_ENTER_NAME;
	    		msgLabel.setText(status);
	        	setMessageStyle(msgLabel, UiUtils.MessageType.ERROR);
			}
		}
		
		if(status.equals(Constants.OK)) {
			String nameValidationMsg = StringUtils.checkValidServiceName(nameTextBox.getText());
			if(!nameValidationMsg.equals(Constants.OK)) {
				status = nameValidationMsg;
	    		msgLabel.setText(status);
	        	setMessageStyle(msgLabel, UiUtils.MessageType.ERROR);
			}
		}
		
		if(status.equals(Constants.OK)) {
    		msgLabel.setText(STATUS_CLICK_UPLOAD);
        	setMessageStyle(msgLabel, UiUtils.MessageType.SUCCESS);
    		submitButton.setEnabled(true);
    	} else {
    		submitButton.setEnabled(false);
    	}
		
	}
	
    private String getWebContext() {
        String context = GWT.getModuleBaseURL().replace( GWT.getModuleName() + "/", "" );
        if ( context.endsWith( "/" ) ) {
            context = context.substring( 0, context.length() - 1 );
        }
        return context;
    }
    
	/**
	 * Set the style type for alert labels
	 * @param statusLabel the label
	 * @param msgType the message type
	 */
    private static void setMessageStyle(Label statusLabel, MessageType msgType) {
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
