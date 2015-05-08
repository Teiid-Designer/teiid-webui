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
import org.teiid.webui.client.widgets.QueryResultsPanel;
import org.teiid.webui.client.widgets.ViewEditorPanel;
import org.teiid.webui.client.widgets.validation.DuplicateNameValidator;
import org.teiid.webui.client.widgets.validation.EmptyNameValidator;
import org.teiid.webui.client.widgets.validation.ServiceNameValidator;
import org.teiid.webui.client.widgets.validation.TextChangeListener;
import org.teiid.webui.client.widgets.validation.ValidatingTextBox;
import org.teiid.webui.client.widgets.vieweditor.ViewEditorManager;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.DataSourcePageRow;
import org.teiid.webui.share.beans.NotificationBean;
import org.teiid.webui.share.beans.VdbDetailsBean;
import org.teiid.webui.share.beans.VdbModelBean;
import org.teiid.webui.share.beans.ViewModelRequestBean;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextArea;

/**
 * EditDataServiceScreen - used to edit existing Data Services
 *
 */
@Dependent
@Templated("./EditDataServiceScreen.html#page")
@WorkbenchScreen(identifier = "EditDataServiceScreen")
public class EditDataServiceScreen extends Composite {

	private String statusClickSave;
	private String serviceOriginalName;
	
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
    protected ValidatingTextBox serviceNameTextBox;
    
    @Inject @DataField("textarea-edit-service-description")
    protected TextArea serviceDescriptionTextBox;
    
    @Inject @DataField("checkbox-edit-service-visibility")
    protected CheckBox serviceVisibleCheckBox;
    
    @Inject @DataField("text-edit-service-status")
    protected HTML statusText;
    
    @Inject @DataField("view-editor-edit-service")
    protected ViewEditorPanel viewEditorPanel;
    
    @Inject @DataField("btn-edit-service-save")
    protected Button saveServiceButton;
    
    @Inject @DataField("btn-edit-service-cancel")
    protected Button cancelButton;
    
    @Inject @DataField("panel-queryResults")
    protected QueryResultsPanel queryResultsPanel;
    
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
        
		statusClickSave = i18n.format("editdataservice.status-label-click-save");
		
		viewEditorPanel.setTitle(i18n.format("editdataservice.vieweditor-title"));
		viewEditorPanel.setDescription(i18n.format("editdataservice.vieweditor-description"));
		viewEditorPanel.setOwner(Constants.EDIT_DATA_SERVICE_SCREEN);
		viewEditorPanel.setQueryResultsPanel(queryResultsPanel);
    	queryResultsPanel.setVisible(false);
		
		serviceNameTextBox.addTextChangeListener(new TextChangeListener() {
            @Override
			public void textChanged(  ) {
            	viewEditorPanel.setServiceName(serviceNameTextBox.getText());
            	updateStatus();
            }
        });
		
    	// Tooltips
    	serviceNameTextBox.setTitle(i18n.format("editdataservice.serviceNameTextBox.tooltip"));
    	serviceDescriptionTextBox.setTitle(i18n.format("editdataservice.serviceDescriptionTextBox.tooltip"));
    	saveServiceButton.setTitle(i18n.format("editdataservice.saveServiceButton.tooltip"));
    	cancelButton.setTitle(i18n.format("editdataservice.cancelButton.tooltip"));
    }
    
    @OnStartup
    public void onStartup( final PlaceRequest place ) {
    	// Init validators
    	doInitValidators();
    	
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
    }
    
    /**
     * Update the screen status.  Ensures that the service name is valid, then checks status
     * of the viewEditor panel.
     */
	private void updateStatus( ) {
        // Checks validity of service name entry
		boolean isOK = serviceNameTextBox.isValid();
		if(!isOK) {
			String resolveEntriesMsg = i18n.format("editdataservice.status-resolve-form-entries");
        	statusText.setHTML(UiUtils.getStatusMessageHtml(resolveEntriesMsg,UiUtils.MessageType.SUCCESS));
		}
    	
		// Check for missing view DDL - if serviceName passed
    	if(isOK) {
    		String viewEditorStatus = viewEditorPanel.getStatus();
    		if(!Constants.OK.equals(viewEditorStatus)) {
            	statusText.setHTML(UiUtils.getStatusMessageHtml(viewEditorStatus,UiUtils.MessageType.SUCCESS));
    			isOK = false;
    		}
    	}
    	
    	if(isOK) {
        	statusText.setHTML(UiUtils.getStatusMessageHtml(statusClickSave,UiUtils.MessageType.SUCCESS));
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
    
    /**
     * Inits the validators - one will init after the service call - it requires the existing vdbnames
     */
    protected void doInitValidators( ) {
    	serviceNameTextBox.clearValidators();
		serviceNameTextBox.addValidator(new EmptyNameValidator());
		serviceNameTextBox.addValidator(new ServiceNameValidator());
		
    	teiidService.getAllVdbNames(new IRpcServiceInvocationHandler<Collection<String>>() {
    		@Override
    		public void onReturn(Collection<String> vdbNames) {
    			vdbNames.remove(serviceOriginalName);
    			serviceNameTextBox.addValidator(new DuplicateNameValidator(vdbNames));
    		}
    		@Override
    		public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.getvdbnames-error"), error); //$NON-NLS-1$
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
    			List<DataSourcePageRow> queryableSources = new ArrayList<DataSourcePageRow>();
    			for(DataSourcePageRow row : dsInfos) {
    				if(row.getState()==DataSourcePageRow.State.OK) {
    					queryableSources.add(row);
    				}
    			}
    			// Set available sources on the editorManager
    			ViewEditorManager.getInstance().setAvailableSources(queryableSources);
    			// Tells editor wizard to refresh with the manager available sources
    			viewEditorPanel.refreshAvailableSources();
    		}
    		@Override
    		public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.error-getting-svcsources"), error); //$NON-NLS-1$
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
