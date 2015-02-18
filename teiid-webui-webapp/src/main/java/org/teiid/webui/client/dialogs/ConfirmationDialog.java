package org.teiid.webui.client.dialogs;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.share.Constants;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * A generic confirmation dialog, used to confirm actions
 */
@Dependent
@WorkbenchPopup(identifier = "ConfirmationDialog")
public class ConfirmationDialog {

	@Inject
    private ClientMessages i18n;
	
	@Inject Event<UiEvent> buttonEvent;

    @Inject
	private PlaceManager placeManager;
	private PlaceRequest place;
	private final FlowPanel view = new FlowPanel();
	private HTMLPanel messagePanel;
	private Button okButton;
	private Button closeButton;
	private String dialogType;
	
	@PostConstruct
	public void setup() {
		messagePanel = new HTMLPanel("<p>Click to close</p>");
		closeButton = new Button( "Cancel" );
		closeButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				fireCancelEvent();
				placeManager.closePlace( place );
			}
		} );
		okButton = new Button( "Ok" );
		okButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				fireOkEvent();
				placeManager.closePlace( place );
			}
		} );
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.add(okButton);
		hPanel.add(closeButton);
		view.add( messagePanel );
		view.add( hPanel );
	}
	
	@OnStartup
	public void onStartup( final PlaceRequest place ) {
		this.place = place;
		
    	// Parameters are passed in for the message content and the confirmation dialog type
    	String message = place.getParameter(Constants.CONFIRMATION_DIALOG_MESSAGE, "NONE");
    	dialogType = place.getParameter(Constants.CONFIRMATION_DIALOG_TYPE, "NONE");
    	
    	view.remove(messagePanel);
    	messagePanel = new HTMLPanel("<p>"+message+"</p>");
    	view.insert(messagePanel, 0);
    	
		okButton.setFocus( true );
	}
	
	@OnClose
	public void onClose( ) {
		fireCancelEvent();
	}
		
	@WorkbenchPartTitle
	public String getTitle() {
		String dialogTitle = "Confirm the Operation";
		
		if(dialogType==null) {
			return dialogTitle;
		}
    	if(Constants.CONFIRMATION_DIALOG_DELETE_SERVICE.equals(dialogType)) {
			dialogTitle = i18n.format("dslibrary.confirm-delete-dialog-title");
    	} else if(Constants.CONFIRMATION_DIALOG_EDIT_SERVICE_ABORT.equals(dialogType)) {
			dialogTitle = i18n.format("editdataservice.confirm-abort-edit-dialog-title");
    	} else if(Constants.CONFIRMATION_DIALOG_SOURCE_RENAME.equals(dialogType)) {
			dialogTitle = i18n.format("ds-properties-panel.confirm-rename-dialog-title");
    	} else if(Constants.CONFIRMATION_DIALOG_SOURCE_REDEPLOY.equals(dialogType)) {
			dialogTitle = i18n.format("ds-properties-panel.confirm-redeploy-dialog-title");
    	} else if(Constants.CONFIRMATION_DIALOG_SOURCE_CHANGETYPE.equals(dialogType)) {
			dialogTitle = i18n.format("ds-properties-panel.confirm-changetype-dialog-title");
		} else if(Constants.CONFIRMATION_DIALOG_SOURCE_DELETE.equals(dialogType)) {
			dialogTitle = i18n.format("managesources.confirm-delete-dialog-title");
		} else if(Constants.CONFIRMATION_DIALOG_REPLACE_VIEW_DEFN.equals(dialogType)) {
			dialogTitle = i18n.format("ds-properties-panel.confirm-overwrite-dialog-title");
		}
		return dialogTitle;
	}
	
	@WorkbenchPartView
	public IsWidget getView() {
		return view;
	}

	/*
	 * Fires different Cancel events, depending on the type of confirmation
	 */
	private void fireCancelEvent() {
    	if(Constants.CONFIRMATION_DIALOG_DELETE_SERVICE.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.DELETE_SERVICE_CANCEL));
    	} else if(Constants.CONFIRMATION_DIALOG_EDIT_SERVICE_ABORT.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.EDIT_SERVICE_ABORT_CANCEL));
    	} else if(Constants.CONFIRMATION_DIALOG_SOURCE_RENAME.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.SOURCE_RENAME_CANCEL));
    	} else if(Constants.CONFIRMATION_DIALOG_SOURCE_REDEPLOY.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.SOURCE_REDEPLOY_CANCEL));
    	} else if(Constants.CONFIRMATION_DIALOG_SOURCE_CHANGETYPE.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.SOURCE_CHANGETYPE_CANCEL));
		} else if(Constants.CONFIRMATION_DIALOG_SOURCE_DELETE.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.DELETE_SOURCE_CANCEL));
		} else if(Constants.CONFIRMATION_DIALOG_REPLACE_VIEW_DEFN.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.VIEW_DEFN_REPLACE_CANCEL));
    	}
	}

	/*
	 * Fires different OK events, depending on the type of confirmation
	 */
	private void fireOkEvent() {
    	if(Constants.CONFIRMATION_DIALOG_DELETE_SERVICE.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.DELETE_SERVICE_OK));
    	} else if(Constants.CONFIRMATION_DIALOG_EDIT_SERVICE_ABORT.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.EDIT_SERVICE_ABORT_OK));
    	} else if(Constants.CONFIRMATION_DIALOG_SOURCE_RENAME.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.SOURCE_RENAME_OK));
    	} else if(Constants.CONFIRMATION_DIALOG_SOURCE_REDEPLOY.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.SOURCE_REDEPLOY_OK));
    	} else if(Constants.CONFIRMATION_DIALOG_SOURCE_CHANGETYPE.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.SOURCE_CHANGETYPE_OK));
		} else if(Constants.CONFIRMATION_DIALOG_SOURCE_DELETE.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.DELETE_SOURCE_OK));
		} else if(Constants.CONFIRMATION_DIALOG_REPLACE_VIEW_DEFN.equals(dialogType)) {
    		buttonEvent.fire(new UiEvent(UiEventType.VIEW_DEFN_REPLACE_OK));
    	}
	}

}
