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
package org.teiid.webui.client.screens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.ApplicationStateKeys;
import org.teiid.webui.client.services.ApplicationStateService;
import org.teiid.webui.client.services.NotificationService;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.webui.client.utils.DdlHelper;
import org.teiid.webui.client.utils.UiUtils;
import org.teiid.webui.client.widgets.ViewEditorPanel;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.DataSourcePageRow;
import org.teiid.webui.share.beans.NotificationBean;
import org.teiid.webui.share.beans.VdbDetailsBean;
import org.teiid.webui.share.beans.ViewModelRequestBean;
import org.teiid.webui.share.services.StringUtils;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

/**
 * CreateDataServiceScreen - used for creation of Data Services
 *
 */
@Dependent
@Templated("./CreateDataServiceScreen.html#page")
@WorkbenchScreen(identifier = "CreateDataServiceScreen")
public class CreateDataServiceScreen extends Composite {

	private String statusEnterName = null;
	private String statusClickCreate = null;
	private Collection<String> existingVdbNames;
	
    @Inject
    private PlaceManager placeManager;
    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    @Inject
    private ApplicationStateService stateService;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject @DataField("textbox-create-service-name")
    protected TextBox serviceNameTextBox;
    
    @Inject @DataField("textarea-create-service-description")
    protected TextArea serviceDescriptionTextBox;
    
    @Inject @DataField("checkbox-create-service-visibility")
    protected CheckBox serviceVisibleCheckbox;
    
    @Inject @DataField("label-create-service-status")
    protected Label statusLabel;
 
    @Inject @DataField("view-editor-create-service")
    protected ViewEditorPanel viewEditorPanel;
    
    @Inject @DataField("btn-create-service-create")
    protected Button createServiceButton;
    
    @Inject @DataField("btn-create-service-cancel")
    protected Button cancelButton;
    
    @Override
    @WorkbenchPartTitle
    public String getTitle() {
      return Constants.BLANK;
    }
    
    @WorkbenchPartView
    public IsWidget getView() {
        return this;
    }
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
        doGetQueryableSources();
        
		statusEnterName = i18n.format("createdataservice.status-label-enter-name");
		statusClickCreate = i18n.format("createdataservice.status-label-click-create");
		
		viewEditorPanel.setTitle(i18n.format("createdataservice.vieweditor-title"));
		viewEditorPanel.setDescription(i18n.format("createdataservice.vieweditor-description"));
		viewEditorPanel.setOwner(Constants.CREATE_DATA_SERVICE_SCREEN);
		
    	serviceVisibleCheckbox.setValue(true);
    	
    	// Tooltips
    	serviceNameTextBox.setTitle(i18n.format("createdataservice.serviceNameTextBox.tooltip"));
    	serviceDescriptionTextBox.setTitle(i18n.format("createdataservice.serviceDescriptionTextBox.tooltip"));
    	createServiceButton.setTitle(i18n.format("createdataservice.createServiceButton.tooltip"));
    	cancelButton.setTitle(i18n.format("createdataservice.cancelButton.tooltip"));
    }
    
    @OnStartup
    public void onStartup( final PlaceRequest place ) {
    	String fromScreen = place.getParameter(Constants.FROM_SCREEN,Constants.UNKNOWN);
    	if(fromScreen!=null && fromScreen.equals(Constants.MANAGE_SOURCES_SCREEN)) {
    		restoreServiceState();
    		updateStatus();
    	} else {
        	serviceNameTextBox.setText(Constants.BLANK);
        	
        	viewEditorPanel.setServiceName(Constants.BLANK);
    	}
    	
    	serviceNameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	viewEditorPanel.setServiceName(serviceNameTextBox.getText());
            	// Update status
            	updateStatus();
            }
        });
    	    	
    	// Populates list of existing vdb names
    	doGetAllVdbNames();
    }
    
    /**
     * Update the screen status.  Ensures that the service name is valid, then checks status
     * of the viewEditor panel.
     */
	private void updateStatus( ) {
    	boolean isOK = true;
    	
    	// Warning for missing service name
    	String serviceName = serviceNameTextBox.getText();
    	if(StringUtils.isEmpty(serviceName)) {
    		statusLabel.setText(statusEnterName);
        	UiUtils.setMessageStyle(statusLabel, UiUtils.MessageType.ERROR);
    		isOK = false;
    	}
    	
    	// Check for valid service name
    	if(isOK) {
    		String nameStatus = StringUtils.checkValidServiceName(serviceName);
    		if(!nameStatus.equals(Constants.OK)) {
    			statusLabel.setText(nameStatus);
            	UiUtils.setMessageStyle(statusLabel, UiUtils.MessageType.ERROR);
    			isOK = false;
    		}
    	}
    	
    	// Ensure that service name is not already being used
    	if(isOK) {
    		String nameStatus = checkServiceNameInUse(serviceName);
    		if(!nameStatus.equals(Constants.OK)) {
    			statusLabel.setText(nameStatus);
            	UiUtils.setMessageStyle(statusLabel, UiUtils.MessageType.ERROR);
    			isOK = false;
    		}
    	}
    	
		// Check for missing view DDL - if serviceName passed
    	if(isOK) {
    		String viewEditorStatus = viewEditorPanel.getStatus();
    		if(!Constants.OK.equals(viewEditorStatus)) {
    			statusLabel.setText(viewEditorStatus);
            	UiUtils.setMessageStyle(statusLabel, UiUtils.MessageType.ERROR);
    			isOK = false;
    		}
    	}
    	
    	if(isOK) {
    		statusLabel.setText(statusClickCreate);
        	UiUtils.setMessageStyle(statusLabel, UiUtils.MessageType.SUCCESS);
    		createServiceButton.setEnabled(true);
    	} else {
    		createServiceButton.setEnabled(false);
    	}
    }
	
    /**
     * Handles UiEvents from viewEditorPanel
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// change received from viewEditor
    	if(dEvent.getType() == UiEventType.VIEW_EDITOR_CHANGED) {
    		updateStatus();
    	} else if(dEvent.getType() == UiEventType.VIEW_EDITOR_GOTO_MANAGE_SOURCES) {
    		String eventSource = dEvent.getEventSource();
    		if(eventSource.equals(Constants.CREATE_DATA_SERVICE_SCREEN)) {
    			// Save in-progress changes
    			saveServiceState();
    			
    			// transition to ManageSourcesScreen
    			Map<String,String> parameters = new HashMap<String,String>();
    			parameters.put(Constants.FROM_SCREEN, Constants.CREATE_DATA_SERVICE_SCREEN);
    	    	placeManager.goTo(new DefaultPlaceRequest(Constants.MANAGE_SOURCES_SCREEN,parameters));
    		}
    	}
    }
        
    /**
     * Save in-progress Service state
     */
    private void saveServiceState() {
    	String svcName = serviceNameTextBox.getText();
    	String svcDescription = serviceDescriptionTextBox.getText();
    	boolean isVisible = serviceVisibleCheckbox.getValue();
    	String viewDdl = viewEditorPanel.getViewDdl();
    	List<String> viewSources = viewEditorPanel.getViewSources();
		stateService.put(ApplicationStateKeys.IN_PROGRESS_SVC_NAME, svcName);
		stateService.put(ApplicationStateKeys.IN_PROGRESS_SVC_DESC, svcDescription);
		stateService.put(ApplicationStateKeys.IN_PROGRESS_SVC_VISIBILITY, isVisible);
		stateService.put(ApplicationStateKeys.IN_PROGRESS_SVC_VIEW_DDL, viewDdl);
		stateService.put(ApplicationStateKeys.IN_PROGRESS_SVC_VIEW_SRCS, viewSources);
    }
    
    /**
     * Restore in-progress Service state
     */
    private void restoreServiceState() {
    	String svcName = (String)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_NAME);
    	String svcDesc = (String)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_DESC);
    	Boolean svcVisibility = (Boolean)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_VISIBILITY);
    	String svcViewDdl = (String)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_VIEW_DDL);
    	@SuppressWarnings("unchecked")
		List<String> svcViewSrcs = (List<String>)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_VIEW_SRCS);
    	
    	serviceNameTextBox.setText(svcName);
    	serviceDescriptionTextBox.setText(svcDesc);
    	serviceVisibleCheckbox.setValue(svcVisibility);
    	viewEditorPanel.setViewDdl(svcViewDdl);
    	viewEditorPanel.setViewSources(svcViewSrcs);
    	viewEditorPanel.setServiceName(svcName);
    }

    /**
     * Event handler that fires when the user clicks the Create button.
     * @param event
     */
    @EventHandler("btn-create-service-create")
    public void onPublishServiceButtonClick(ClickEvent event) {
    	doCreateService();
    }
    
    /**
     * Create and deploy the Service dynamic VDB
     */
    private void doCreateService() {
    	final String serviceName = this.serviceNameTextBox.getText();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("createdataservice.creating-service-title"), //$NON-NLS-1$
                i18n.format("createdataservice.creating-service-msg", serviceName)); //$NON-NLS-1$
            	
    	String serviceDescription = this.serviceDescriptionTextBox.getText();
    	final String viewModel = serviceName;
    	// DDL for the View
    	String viewDdl = viewEditorPanel.getViewDdl();

    	// DDL for the rest procedure
    	String restProcDdl = DdlHelper.getRestProcDdlFromViewDdl(Constants.REST_PROCNAME,viewDdl,Constants.REST_XML_GROUPTAG,Constants.REST_XML_ELEMENTTAG,
    			                                                 Constants.SERVICE_VIEW_NAME,Constants.REST_URI_PROPERTY);
    	
    	// Combined DDL is the combined view and procedure
    	String modelDDL = viewDdl + restProcDdl;
    	
    	boolean isVisible = serviceVisibleCheckbox.getValue();
    	List<String> rqdImportVdbNames = viewEditorPanel.getViewSourceVdbNames();
    	
    	ViewModelRequestBean viewModelRequest = new ViewModelRequestBean();
    	viewModelRequest.setName(serviceName);
    	viewModelRequest.setDescription(serviceDescription);
    	viewModelRequest.setDdl(modelDDL);
    	viewModelRequest.setVisible(isVisible);
    	viewModelRequest.setRequiredImportVdbNames(rqdImportVdbNames);
    	
    	// VDB properties
    	Map<String,String> vdbPropMap = new HashMap<String,String>();
    	vdbPropMap.put(Constants.VDB_PROP_KEY_REST_AUTOGEN, "true");
    	vdbPropMap.put(Constants.VDB_PROP_KEY_DATASERVICE_VIEWNAME, Constants.SERVICE_VIEW_NAME);
    	    	
    	final String svcVdbName = serviceName;
    	teiidService.deployNewVDB(svcVdbName, 1, vdbPropMap, viewModelRequest, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("createdataservice.creating-service-complete"), //$NON-NLS-1$
                        i18n.format("createdataservice.creating-service-complete-msg")); //$NON-NLS-1$

                cleanupTestVdbs();
            	
            	Map<String,String> parameters = new HashMap<String,String>();
            	parameters.put(Constants.SERVICE_NAME_KEY, viewModel);
            	placeManager.goTo(new DefaultPlaceRequest(Constants.DATA_SERVICE_DETAILS_SCREEN,parameters));
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("createdataservice.creating-service-error"), error); //$NON-NLS-1$
//                addModelInProgressMessage.setVisible(false);
            }
        });           	
    }
       
    private String checkServiceNameInUse(String serviceName) {
    	String statusMsg = Constants.OK;
    	
    	if(this.existingVdbNames!=null && this.existingVdbNames.contains(serviceName)) {
    		statusMsg = i18n.format("createdataservice.service-name-exists-msg",serviceName); //$NON-NLS-1$
    	}
    	return statusMsg;
    }
    
    /**
     * Populate list of all current VDB names
     */
    protected void doGetAllVdbNames( ) {
    	teiidService.getAllVdbNames(new IRpcServiceInvocationHandler<Collection<String>>() {
    		@Override
    		public void onReturn(Collection<String> vdbNames) {
    			existingVdbNames=vdbNames;
    		}
    		@Override
    		public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("createdataservice.getvdbnames-error"), error); //$NON-NLS-1$
    		}
    	});
    }
    
    /**
     * Get all available source for building the service
     */
    protected void doGetQueryableSources( ) {
    	teiidService.getDataSources("filter", Constants.SERVICE_SOURCE_VDB_PREFIX, new IRpcServiceInvocationHandler<List<DataSourcePageRow>>() {
    		@Override
    		public void onReturn(List<DataSourcePageRow> dsInfos) {
    			// Update the list of queryable data sources
    			List<String> queryableDSNames = new ArrayList<String>();
    			for(DataSourcePageRow row : dsInfos) {
    				if(row.getState()==DataSourcePageRow.State.OK) {
            			queryableDSNames.add(row.getName());
    				}
    			}
    			viewEditorPanel.setAvailableSources(queryableDSNames);
    		}
    		@Override
    		public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("createdataservice.error-getting-svcsources"), error); //$NON-NLS-1$
    		}
    	});
    }
    
    /**
     * Do a clean-up of any temporary VDBs that may have not been undeployed
     */
 	private void cleanupTestVdbs( ) {
 		teiidService.deleteDynamicVdbsWithPrefix(Constants.SERVICE_TEST_VDB_PREFIX, new IRpcServiceInvocationHandler<Void>() {
    		@Override
    		public void onReturn(Void data) {
    		}
    		@Override
    		public void onError(Throwable error) {
    		}
    	});
 	}
   
    /**
     * Event handler that fires when the user clicks the Cancel button.
     * @param event
     */
    @EventHandler("btn-create-service-cancel")
    public void onCancelButtonClick(ClickEvent event) {
    	placeManager.goTo(Constants.DATA_SERVICES_LIBRARY_SCREEN);
    }
        
}
