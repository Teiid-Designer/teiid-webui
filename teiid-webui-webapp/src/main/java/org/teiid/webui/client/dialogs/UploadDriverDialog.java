package org.teiid.webui.client.dialogs;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Button;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.widgets.IImportCompletionHandler;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dialog for upload of jdbc drivers
 */
@Dependent
@WorkbenchPopup(identifier = "UploadDriverDialog")
public class UploadDriverDialog {

	@Inject
    private ClientMessages i18n;
	
	@Inject Event<UiEvent> buttonEvent;

    @Inject
	private PlaceManager placeManager;
    
    @Inject 
    private UploadContentPanel uploadContent;
    
	private PlaceRequest place;
	private final VerticalPanel view = new VerticalPanel();
	private Button uploadButton;
	private Button closeButton;
	
	@PostConstruct
	public void setup() {
		closeButton = new Button( "Cancel" );
		closeButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				fireCancelEvent();
				placeManager.closePlace( place );
			}
		} );
		uploadButton = new Button( "Upload" );
		uploadButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				uploadContent.doUpload();
				//placeManager.closePlace( place );
			}
		} );
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.add(uploadButton);
		hPanel.add(closeButton);
		
		uploadContent.setCompletionHandler(new IImportCompletionHandler() {
			@Override
			public void onImportComplete() {
				buttonEvent.fire(new UiEvent(UiEventType.UPLOAD_DRIVER_COMPLETE));
				placeManager.closePlace( place );
			}
		});

		view.add( uploadContent );
		view.add( hPanel );
	}
	
	@OnStartup
	public void onStartup( final PlaceRequest place ) {
		this.place = place;
		
		uploadButton.setFocus( true );
	}
	
	@WorkbenchPartTitle
	public String getTitle() {
		String dialogTitle = i18n.format("upload-dtype-dialog.dialog-title");
		return dialogTitle;
	}
	
	@WorkbenchPartView
	public Widget getView() {
		return view;
	}
    
	/*
	 * Fires different Cancel events, depending on the type of confirmation
	 */
	private void fireCancelEvent() {
		buttonEvent.fire(new UiEvent(UiEventType.UPLOAD_DRIVER_CANCEL));
	}

}
