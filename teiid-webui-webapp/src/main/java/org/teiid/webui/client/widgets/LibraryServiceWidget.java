/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.teiid.webui.client.widgets;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.resources.AppResource;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.share.Constants;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

@Dependent
@Templated("./LibraryServiceWidget.html")
public class LibraryServiceWidget extends Composite implements HasModel<ServiceRow> {

	private static final String MORE_ACTIONS = "More Actions";
	private static final String EDIT_ACTION = "Edit Service";
	private static final String DUPLICATE_ACTION = "Duplicate Service";
	private static final String TEST_ACTION = "Test Service";
	private static final String DELETE_ACTION = "Delete Service";
	private static final String SAVE_TO_FILE_ACTION = "Save to File";
	
    @Inject
    private ClientMessages i18n;

	@Inject
	private PlaceManager placeManager;

    @Inject
    protected TeiidRpcService teiidService;
    
	@Inject @AutoBound DataBinder<ServiceRow> serviceBinder;

	@Inject @Bound @DataField("label-servicewidget-name") Label name;

	@Inject @Bound @DataField("label-servicewidget-description") Label description;

	@Inject @DataField("image-servicewidget-status")
	protected Image viewServiceStatusImage;
	
	@Inject @DataField("label-servicewidget-visibility")
	protected Label viewServiceVisibilityLabel;
	
	@Inject @DataField("btn-servicewidget-view")
	protected Button viewServiceButton;

    @Inject @DataField("listbox-servicewidget-more-actions")
    protected ListBox moreActionsListBox;
    
    @Inject Event<UiEvent> uiEvent;
    
	public ServiceRow getModel() {
		return serviceBinder.getModel();
	}

	public void setModel(ServiceRow service) {
		serviceBinder.setModel(service);
		
		// Set the status image
    	String rStatus = getModel().getStatus();
    	if(Constants.STATUS_ACTIVE.equals(rStatus)) {
        	this.viewServiceStatusImage.setResource(AppResource.INSTANCE.images().okIcon16x16Image());
    	} else {
        	this.viewServiceStatusImage.setResource(AppResource.INSTANCE.images().errorIcon16x16Image());
    	}
    	
//    	ServiceRow row = getModel();
//    	if(row.isVisible()) {
//    		this.viewServiceVisibilityLabel.removeStyleName("glyphicon-eye-close");
//    		this.viewServiceVisibilityLabel.addStyleName("glyphicon glyphicon-eye-open");
//    	} else {
//    		this.viewServiceVisibilityLabel.removeStyleName("glyphicon-eye-open");
//    		this.viewServiceVisibilityLabel.addStyleName("glyphicon glyphicon-eye-close");
//    	}
    	
	}

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	populateMoreActionsListBox();
    	
        // Change Listener for Type ListBox
    	moreActionsListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
        		String action = getSelectedAction();    
        		if(action.equals(EDIT_ACTION)) {
        			doEditService();
        		} else if(action.equals(DUPLICATE_ACTION)) {
        			doCloneService();
        		} else if(action.equals(TEST_ACTION)) {
        			doViewService();
        		} else if(action.equals(DELETE_ACTION)) {
        			doRemoveService();
        		} else if(action.equals(SAVE_TO_FILE_ACTION)) {
        			doSaveToFile();
        		}
        	}
        });

    	// Tooltips
    	viewServiceButton.setTitle(i18n.format("lib-service-widget.viewServiceButton.tooltip"));
    	moreActionsListBox.setTitle(i18n.format("lib-service-widget.moreActionsListBox.tooltip"));
    }
    
    /**
     * Init the List of Service actions
     */
    private void populateMoreActionsListBox( ) {
    	// Make sure clear first
    	moreActionsListBox.clear();

    	moreActionsListBox.insertItem(MORE_ACTIONS, 0);
    	moreActionsListBox.insertItem(EDIT_ACTION, 1);
    	moreActionsListBox.insertItem(DUPLICATE_ACTION, 2);
    	moreActionsListBox.insertItem(TEST_ACTION, 3);
    	moreActionsListBox.insertItem(DELETE_ACTION, 4);
    	moreActionsListBox.insertItem(SAVE_TO_FILE_ACTION, 5);
    	
    	// Initialize by setting the selection to the first item.
    	moreActionsListBox.setSelectedIndex(0);
    }
    
    /**
     * Get the selected action from the MoreActions dropdown
     * @return
     */
    private String getSelectedAction() {
    	int index = moreActionsListBox.getSelectedIndex();
    	return moreActionsListBox.getValue(index);
    }
    
    /**
     * Fire status event for a dataSource
     * @param eventType the type of event
     * @param dataSourceName the datasource name
     */
    private void fireUiEvent(UiEventType eventType, String serviceName) {
		UiEvent sEvent = new UiEvent(eventType);
		sEvent.setDataServiceName(serviceName);
		uiEvent.fire(sEvent);
    }
    
	/**
	 * Event handler that fires when the user clicks the ViewService button.
	 * @param event
	 */
	@EventHandler("btn-servicewidget-view")
	public void onViewServiceButtonClick(ClickEvent event) {
		doViewService();
	}

	/**
	 * View Service - transitions to ViewDataServiceScreen
	 */
	protected void doViewService() {
		String svcName = getModel().getName();
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.SERVICE_NAME_KEY, svcName);

		placeManager.goTo(new DefaultPlaceRequest(Constants.DATA_SERVICE_DETAILS_SCREEN,parameters));
	}
    
	/**
	 * Clone Service - put name of service as CLONE_SERVICE_KEY, then transition to DataServicesLibraryScreen.
	 */
    protected void doCloneService( ) {
		String svcName = getModel().getName();
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.CLONE_SERVICE_KEY, svcName);
		
    	// re-init list selection
    	populateMoreActionsListBox();
    	
		placeManager.goTo(new DefaultPlaceRequest(Constants.DATA_SERVICES_LIBRARY_SCREEN,parameters));
    }
    
	/**
	 * Edit Service - transitions to EditDataServiceScreen
	 */
	protected void doEditService() {
		String svcName = getModel().getName();
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.SERVICE_NAME_KEY, svcName);

		placeManager.goTo(new DefaultPlaceRequest(Constants.EDIT_DATA_SERVICE_SCREEN,parameters));
	}
	
	/**
	 * Delete Service - put name of service as DELETE_SERVICE_KEY, then transition to DataServicesLibraryScreen.
	 */
    protected void doRemoveService( ) {
		String svcName = getModel().getName();
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.DELETE_SERVICE_KEY, svcName);
		
		placeManager.goTo(new DefaultPlaceRequest(Constants.DATA_SERVICES_LIBRARY_SCREEN,parameters));
    }
    
    protected void doSaveToFile( ) {
    	fireUiEvent(UiEventType.SAVE_SERVICE, getModel().getName());
    }

}