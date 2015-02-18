package org.teiid.webui.client.dialogs;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Button;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.share.Constants;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A generic confirmation dialog, used to confirm actions
 */
@Dependent
@WorkbenchPopup(identifier = "AddViewSrcDialog")
public class AddViewSrcDialog {

	@Inject
    private ClientMessages i18n;
	
	@Inject Event<UiEvent> buttonEvent;

    @Inject
	private PlaceManager placeManager;
	private PlaceRequest place;
	private final VerticalPanel view = new VerticalPanel();
	private HTMLPanel messagePanel;
    private ListBox allSourcesListBox = new ListBox();
	private Button okButton;
	private Button closeButton;
	
	@PostConstruct
	public void setup() {
		String dMsg = i18n.format("viewsource-panel.add-source-dialog-message");
    	messagePanel = new HTMLPanel("<p>"+dMsg+"</p>");

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
		view.add( allSourcesListBox );
		view.add( hPanel );
	}
	
	@OnStartup
	public void onStartup( final PlaceRequest place ) {
		this.place = place;
		
    	// Parameter passed for available sources
    	String availableSourceString = place.getParameter(Constants.ADD_VIEW_SRC_AVAILABLE_SRCS, "NONE");
    	List<String> availableSrcs = new ArrayList<String>();
    	String[] parts = availableSourceString.split(";");
    	for(String part : parts) {
    		availableSrcs.add(part);
    	}
    	populateListBox(availableSrcs);
    	
		okButton.setFocus( true );
	}
	
	@WorkbenchPartTitle
	public String getTitle() {
		String dialogTitle = i18n.format("viewsource-panel.add-source-dialog-title");
		return dialogTitle;
	}
	
	@WorkbenchPartView
	public Widget getView() {
		return view;
	}
	
    /**
     * Init the List of Available sources
     */
    private void populateListBox(List<String> allSources) {
    	// Make sure clear first
    	allSourcesListBox.clear();

    	for(int i=0; i<allSources.size(); i++) {
    		allSourcesListBox.insertItem(allSources.get(i), i);
    	}
    	
    	// Initialize by setting the selection to the first item.
    	allSourcesListBox.setSelectedIndex(0);
    }
    
    /**
     * Get the selected data source from the listbox
     * @return
     */
    public String getSelectedSource() {
    	int index = allSourcesListBox.getSelectedIndex();
    	return allSourcesListBox.getValue(index);
    }
    
	/*
	 * Fires different Cancel events, depending on the type of confirmation
	 */
	private void fireCancelEvent() {
		buttonEvent.fire(new UiEvent(UiEventType.ADD_VIEW_SOURCE_CANCEL));
	}

	/*
	 * Fires different OK events, depending on the type of confirmation
	 */
	private void fireOkEvent() {
    	UiEvent event = new UiEvent(UiEventType.ADD_VIEW_SOURCE_OK);
    	event.setDataSourceName(getSelectedSource());
		buttonEvent.fire(event);
	}

}
