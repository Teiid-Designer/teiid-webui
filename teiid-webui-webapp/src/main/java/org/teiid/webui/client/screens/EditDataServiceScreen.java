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
import org.teiid.webui.share.beans.NotificationBean;
import org.teiid.webui.share.beans.VdbDetailsBean;
import org.teiid.webui.share.beans.VdbModelBean;
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
 * EditDataServiceScreen - used to edit existing Data Services
 *
 */
@Dependent
@Templated("./EditDataServiceScreen.html#page")
@WorkbenchScreen(identifier = "EditDataServiceScreen")
public class EditDataServiceScreen extends Composite {

	private String statusEnterName;
	private String statusClickSave;
	private String serviceOriginalName;
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

    @Inject @DataField("textbox-edit-service-name")
    protected TextBox serviceNameTextBox;
    
    @Inject @DataField("textarea-edit-service-description")
    protected TextArea serviceDescriptionTextBox;
    
    @Inject @DataField("checkbox-edit-service-visibility")
    protected CheckBox serviceVisibleCheckBox;
    
    @Inject @DataField("label-edit-service-status")
    protected Label statusLabel;
    
    @Inject @DataField("view-editor-edit-service")
    protected ViewEditorPanel viewEditorPanel;
    
    @Inject @DataField("btn-edit-service-save")
    protected Button saveServiceButton;
    
    @Inject @DataField("btn-edit-service-cancel")
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
		statusEnterName = i18n.format("editdataservice.status-label-enter-name");
		statusClickSave = i18n.format("editdataservice.status-label-click-save");
		
		viewEditorPanel.setTitle(i18n.format("editdataservice.vieweditor-title"));
		viewEditorPanel.setDescription(i18n.format("editdataservice.vieweditor-description"));
		viewEditorPanel.setOwner(Constants.EDIT_DATA_SERVICE_SCREEN);
		
    	// Tooltips
    	serviceNameTextBox.setTitle(i18n.format("editdataservice.serviceNameTextBox.tooltip"));
    	serviceDescriptionTextBox.setTitle(i18n.format("editdataservice.serviceDescriptionTextBox.tooltip"));
    	saveServiceButton.setTitle(i18n.format("editdataservice.saveServiceButton.tooltip"));
    	cancelButton.setTitle(i18n.format("editdataservice.cancelButton.tooltip"));
    }
    
    @OnStartup
    public void onStartup( final PlaceRequest place ) {
    	String fromScreen = place.getParameter(Constants.FROM_SCREEN,Constants.UNKNOWN);
    	if(fromScreen!=null && fromScreen.equals(Constants.MANAGE_SOURCES_SCREEN)) {
    		restoreServiceState();
    		updateStatus();
    	} else {
        	String serviceName = place.getParameter(Constants.SERVICE_NAME_KEY, Constants.UNKNOWN);
        	serviceNameTextBox.setText(serviceName);
        	serviceOriginalName = serviceName;
        	
        	viewEditorPanel.setServiceName(serviceName);
        	// Get details for this service
        	doGetDataServiceDetails(serviceName);
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
    		if(!serviceName.equals(this.serviceOriginalName)) {
    			String nameStatus = checkServiceNameInUse(serviceName);
    			if(!nameStatus.equals(Constants.OK)) {
    				statusLabel.setText(nameStatus);
                	UiUtils.setMessageStyle(statusLabel, UiUtils.MessageType.ERROR);
    				isOK = false;
    			}
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
    		statusLabel.setText(statusClickSave);
        	UiUtils.setMessageStyle(statusLabel, UiUtils.MessageType.SUCCESS);
    		saveServiceButton.setEnabled(true);
    	} else {
    		saveServiceButton.setEnabled(false);
    	}
    }
        
    /**
     * Get the Data Service details to populate the page
     * @param serviceName the name of the service
     */
    protected void doGetDataServiceDetails(final String serviceName) {
    	final String serviceVdb = serviceName;
    	teiidService.getVdbDetails(serviceVdb, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {
            	Collection<VdbModelBean> vdbModels = vdbDetailsBean.getModels();
            	for(VdbModelBean vdbModel : vdbModels) {
            		if(vdbModel.getName().equals(serviceName)) {
            			String description = vdbModel.getDescription();
            			serviceDescriptionTextBox.setText(description);
            			
            			boolean isVisible = vdbModel.isVisible();
            			serviceVisibleCheckBox.setValue(isVisible);
            			
            			String ddl = vdbModel.getDdl();
            			// Remove the REST procedure from the model DDL.  It will be re-generated when the service is saved.
            			String editorDdl = removeRestProcDdl(ddl);
            			viewEditorPanel.setViewDdl(editorDdl);
            		}
            	}
    			List<String> importVdbs = vdbDetailsBean.getImportedVdbNames();
    			List<String> srcNames = new ArrayList<String>(importVdbs.size());
    			for(String importVdbName : importVdbs) {
    				if(importVdbName.startsWith(Constants.SERVICE_SOURCE_VDB_PREFIX)) {
    			    	// The source VDB name, but without the prefix
    			    	String srcName = importVdbName.substring(importVdbName.indexOf(Constants.SERVICE_SOURCE_VDB_PREFIX)+Constants.SERVICE_SOURCE_VDB_PREFIX.length());
    					srcNames.add(srcName);
    				}
    			}
    			viewEditorPanel.setViewSources(srcNames);
            	
            	// Set the initial status
            	updateStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.error-getting-svc-details"), error); //$NON-NLS-1$
//                noDataMessage.setVisible(true);
            }
        });       
    }
    
    /**
     * Take the entire view model DDL (including REST proc) and remove the REST portion
     * @param viewDdl the entire view DDL
     * @return the ddl without REST procedure
     */
    private String removeRestProcDdl(String viewDdl) {
    	int restStartIndx = viewDdl.indexOf("SET NAMESPACE");
    	return (restStartIndx!=-1) ? viewDdl.substring(0,restStartIndx) : viewDdl;
    }
    /**
     * Event handler that fires when the user clicks the SaveChanges button.
     * @param event
     */
    @EventHandler("btn-edit-service-save")
    private void onSaveServiceButtonClick(ClickEvent event) {
    	final String serviceName = this.serviceNameTextBox.getText();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("editdataservice.saving-service-title"), //$NON-NLS-1$
                i18n.format("editdataservice.saving-service-msg", serviceName)); //$NON-NLS-1$
            	
    	final String viewModel = serviceName;
    	String serviceDescription = this.serviceDescriptionTextBox.getText();
    	boolean isVisible = serviceVisibleCheckBox.getValue();
    	List<String> rqdImportVdbNames = viewEditorPanel.getViewSourceVdbNames();
    	
    	// DDL for the View
    	String viewDdl = viewEditorPanel.getViewDdl();

    	// DDL for the rest procedure
    	String restProcDdl = DdlHelper.getRestProcDdlFromViewDdl(Constants.REST_PROCNAME,viewDdl,Constants.REST_XML_GROUPTAG,Constants.REST_XML_ELEMENTTAG,
    			                                                 Constants.SERVICE_VIEW_NAME,Constants.REST_URI_PROPERTY);
    	// Model DDL is the combination of View and Rest proc
    	String modelDdl = viewDdl + restProcDdl;

    	ViewModelRequestBean viewModelRequest = new ViewModelRequestBean();
    	viewModelRequest.setName(serviceName);
    	viewModelRequest.setDescription(serviceDescription);
    	viewModelRequest.setDdl(modelDdl);
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
                        i18n.format("editdataservice.saving-service-complete"), //$NON-NLS-1$
                        i18n.format("editdataservice.saving-service-complete-msg")); //$NON-NLS-1$

                // Delete the original named VDB if there was a rename
                if(!serviceName.equals(serviceOriginalName)) {
                	deleteVdb(serviceOriginalName);
                }
                
                // Cleanup test VDBs
                cleanupTestVdbs();
            	
            	Map<String,String> parameters = new HashMap<String,String>();
            	parameters.put(Constants.SERVICE_NAME_KEY, viewModel);
            	placeManager.goTo(new DefaultPlaceRequest(Constants.DATA_SERVICE_DETAILS_SCREEN,parameters));
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.saving-service-error"), error); //$NON-NLS-1$
//                addModelInProgressMessage.setVisible(false);
            }
        });           	
    }
    
    private String checkServiceNameInUse(String serviceName) {
    	String statusMsg = Constants.OK;
    	
    	if(this.existingVdbNames!=null && this.existingVdbNames.contains(serviceName)) {
    		statusMsg = i18n.format("editdataservice.service-name-exists-msg",serviceName); //$NON-NLS-1$
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
                notificationService.sendErrorNotification(i18n.format("editdataservice.getvdbnames-error"), error); //$NON-NLS-1$
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
     * Deletes the original VDB on a VDB rename operation
     */
 	private void deleteVdb(String vdbName) {
 		teiidService.deleteDataSourceAndVdb(vdbName,vdbName, new IRpcServiceInvocationHandler<List<VdbDetailsBean>>() {
    		@Override
    		public void onReturn(List<VdbDetailsBean> data) {
    		}
    		@Override
    		public void onError(Throwable error) {
    		}
    	});
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
    		if(eventSource.equals(Constants.EDIT_DATA_SERVICE_SCREEN)) {
    			// Save in-progress changes
    			saveServiceState();
    			
    			// transition to ManageSourcesScreen
    			Map<String,String> parameters = new HashMap<String,String>();
    			parameters.put(Constants.FROM_SCREEN, Constants.EDIT_DATA_SERVICE_SCREEN);
    	    	placeManager.goTo(new DefaultPlaceRequest(Constants.MANAGE_SOURCES_SCREEN,parameters));
    		}
    	} else if(dEvent.getType() == UiEventType.EDIT_SERVICE_ABORT_OK) {
        	placeManager.goTo(Constants.DATA_SERVICES_LIBRARY_SCREEN);
    	} else if(dEvent.getType() == UiEventType.EDIT_SERVICE_ABORT_CANCEL) {
    	}
    }
    
    /**
     * Save in-progress Service state
     */
    private void saveServiceState() {
    	String svcName = serviceNameTextBox.getText();
    	String svcDescription = serviceDescriptionTextBox.getText();
    	boolean isVisible = serviceVisibleCheckBox.getValue();
    	String viewDdl = viewEditorPanel.getViewDdl();
    	List<String> viewSources = viewEditorPanel.getViewSources();
    	stateService.put(ApplicationStateKeys.IN_PROGRESS_SVC_ORIGINAL_NAME, serviceOriginalName);
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
    	serviceOriginalName = (String)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_ORIGINAL_NAME);
    	String svcName = (String)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_NAME);
    	String svcDesc = (String)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_DESC);
    	Boolean svcVisibility = (Boolean)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_VISIBILITY);
    	String svcViewDdl = (String)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_VIEW_DDL);
    	@SuppressWarnings("unchecked")
		List<String> svcViewSrcs = (List<String>)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_VIEW_SRCS);
    	
    	serviceNameTextBox.setText(svcName);
    	serviceDescriptionTextBox.setText(svcDesc);
    	serviceVisibleCheckBox.setValue(svcVisibility);
    	viewEditorPanel.setViewDdl(svcViewDdl);
    	viewEditorPanel.setViewSources(svcViewSrcs);
    	viewEditorPanel.setServiceName(svcName);
    }

    /**
     * Event handler that fires when the user clicks the Cancel button.
     * @param event
     */
    @EventHandler("btn-edit-service-cancel")
    public void onCancelButtonClick(ClickEvent event) {
    	// user must confirm if there are pending changes
    	if(saveServiceButton.isEnabled()) {
			// Display the Confirmation Dialog for abortint the edits
			Map<String,String> parameters = new HashMap<String,String>();
			parameters.put(Constants.CONFIRMATION_DIALOG_MESSAGE, i18n.format("editdataservice.confirm-abort-edit-dialog-message"));
			parameters.put(Constants.CONFIRMATION_DIALOG_TYPE, Constants.CONFIRMATION_DIALOG_EDIT_SERVICE_ABORT);
	    	placeManager.goTo(new DefaultPlaceRequest(Constants.CONFIRMATION_DIALOG,parameters));
    	// no pending changes
    	} else {
        	placeManager.goTo(Constants.DATA_SERVICES_LIBRARY_SCREEN);
    	}
    }
    
}
