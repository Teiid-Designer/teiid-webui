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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.TextBox;
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
import org.teiid.webui.client.widgets.ServiceFlowListWidget;
import org.teiid.webui.client.widgets.ServiceRow;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.NotificationBean;
import org.teiid.webui.share.beans.VdbDetailsBean;
import org.teiid.webui.share.beans.VdbModelBean;
import org.teiid.webui.share.services.StringUtils;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * DataServicesLibraryScreen - shows all published Data Services.
 *
 */
@Dependent
@Templated("./DataServicesLibraryScreen.html#page")
@WorkbenchScreen(identifier = "DataServicesLibraryScreen")
public class DataServicesLibraryScreen extends Composite {

	List<ServiceRow> currentServices = new ArrayList<ServiceRow>();    
	private String deleteServiceName = null;
	
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
 
    @Inject
    private PlaceManager placeManager;
    
    @Inject ServiceFlowListWidget serviceFlowListWidget;
    
    @Inject @DataField("textbox-filter-services")
    protected TextBox filterServicesTextBox;
    
    @Inject @DataField("btn-create-service")
    protected Button createServiceButton;

    @Inject @DataField("btn-manage-sources")
    protected Button manageSourcesButton;
        
    @Inject @DataField("grid-services")
    protected VerticalPanel servicesPanel;
    
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
    	servicesPanel.add(serviceFlowListWidget);

    	String filterTxt = (String)stateService.get(ApplicationStateKeys.SERVICES_LIBRARY_FILTER_TEXT);
    	filterServicesTextBox.setText(filterTxt);
    	
	    filterServicesTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	stateService.put(ApplicationStateKeys.SERVICES_LIBRARY_FILTER_TEXT, filterServicesTextBox.getText());
            	// Update status
            	populateGrid(currentServices);
            }
        });
	    
    	// Tooltips
    	createServiceButton.setTitle(i18n.format("dslibrary.createServiceButton.tooltip"));
    	manageSourcesButton.setTitle(i18n.format("dslibrary.manageSourcesButton.tooltip"));
    }
    
    @OnStartup
    public void onStartup( final PlaceRequest place ) {
    	// Process delete and clone requests from serviceWidget
    	String deleteName = place.getParameter(Constants.DELETE_SERVICE_KEY, "NONE");
    	String cloneName = place.getParameter(Constants.CLONE_SERVICE_KEY, "NONE");
    	if(!deleteName.equals("NONE")) {
			// Display the Confirmation Dialog for deleting a Service
    		deleteServiceName = deleteName;
			Map<String,String> parameters = new HashMap<String,String>();
			parameters.put(Constants.CONFIRMATION_DIALOG_MESSAGE, i18n.format("dslibrary.confirm-delete-dialog-message",deleteServiceName));
			parameters.put(Constants.CONFIRMATION_DIALOG_TYPE, Constants.CONFIRMATION_DIALOG_DELETE_SERVICE);
	    	placeManager.goTo(new DefaultPlaceRequest(Constants.CONFIRMATION_DIALOG,parameters));
	    	//placeManager.goTo(new DefaultPlaceRequest("MyTestPopUp"));
    	} else if(!cloneName.equals("NONE")) {
    		doCloneService(cloneName);
    	} else {
    		doGetServices();
    	}
     	cleanupTestVdbs();
    }
    
    /**
     * Handles UiEvents
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// User has OK'd source deletion
    	if(dEvent.getType() == UiEventType.DELETE_SERVICE_OK) {
    		if(deleteServiceName!=null) {
    			doRemoveService(deleteServiceName);
    		}
        // User has cancelled service deletion
    	} else if(dEvent.getType() == UiEventType.DELETE_SERVICE_CANCEL) {
    		doGetServices();
    	// User requesting save service to file
    	} else if(dEvent.getType() == UiEventType.SAVE_SERVICE) {
    		doSaveServiceToFile(dEvent.getDataServiceName());    		
    	}
    }
    
    private void populateGrid(List<ServiceRow> serviceList) {
    	List<ServiceRow> filteredServices = filterServices(serviceList);
        serviceFlowListWidget.setItems(filteredServices);
    }
    
    /**
     * Filter the services based on the contents of the search box
     * @param serviceList the list of services
     * @return the filtered list of services
     */
    private List<ServiceRow> filterServices(List<ServiceRow> serviceList) {
    	List<ServiceRow> resultRows = new ArrayList<ServiceRow>();
    	
    	String filterTxt = (String)stateService.get(ApplicationStateKeys.SERVICES_LIBRARY_FILTER_TEXT);
    	for(ServiceRow row: serviceList) {
    		if(StringUtils.isEmpty(filterTxt)) {
    			resultRows.add(row);
    		} else if(row.getName().toLowerCase().startsWith(filterTxt.trim().toLowerCase())) {
    			resultRows.add(row);
    		}
    	}
    
    	return resultRows;
    }
    
    /**
     * Get the public services for the supplied VDB
     */
    protected void doGetServices( ) {
    	teiidService.getDataServiceVdbs(new IRpcServiceInvocationHandler<List<VdbDetailsBean>>() {
    		@Override
    		public void onReturn(List<VdbDetailsBean> serviceVdbs) {
                // Convert VDBDetails to rows for the display
    			List<ServiceRow> serviceTableRows = getServiceRows(serviceVdbs);
    			
    			currentServices.clear();
    			currentServices.addAll(serviceTableRows);

    			if(serviceTableRows.isEmpty()) {
    				placeManager.goTo(Constants.DATA_SERVICES_EMPTY_LIBRARY_SCREEN);
    			} else {
    		     	populateGrid(serviceTableRows);
    			}
    		}
    		@Override
    		public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("dslibrary.fetch-services-error"), error); //$NON-NLS-1$
    		}
    	});
    }
    
   protected void doRemoveService(String serviceName) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("dslibrary.service-deleting"), //$NON-NLS-1$
                i18n.format("dslibrary.service-deleting-msg", serviceName)); //$NON-NLS-1$
        
        String vdbSrcName = serviceName;
    	teiidService.deleteDataSourceAndVdb(vdbSrcName, vdbSrcName, new IRpcServiceInvocationHandler<List<VdbDetailsBean>>() {
    		@Override
    		public void onReturn(List<VdbDetailsBean> serviceVdbs) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("dslibrary.delete-success"), //$NON-NLS-1$
                        i18n.format("dslibrary.delete-success-msg")); //$NON-NLS-1$
                
                // Convert VDBDetails to rows for the display
    			List<ServiceRow> serviceTableRows = getServiceRows(serviceVdbs);

    			currentServices.clear();
    			currentServices.addAll(serviceTableRows);
    			
    			if(serviceTableRows.isEmpty()) {
    				placeManager.goTo(Constants.DATA_SERVICES_EMPTY_LIBRARY_SCREEN);
    			} else {
    		     	populateGrid(serviceTableRows);
    			}
    		}
    		@Override
    		public void onError(Throwable error) {
    			notificationService.completeProgressNotification(notificationBean.getUuid(),
    					i18n.format("dslibrary.service-delete-error"), //$NON-NLS-1$
    					error);
    		}
    	});
    }
    
    protected void doCloneService(String serviceName) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("dslibrary.service-cloning"), //$NON-NLS-1$
                i18n.format("dslibrary.service-cloning-msg", serviceName)); //$NON-NLS-1$
        
        String vdbName = serviceName;
        teiidService.cloneDynamicVdbAddSource(vdbName, 1, new IRpcServiceInvocationHandler<List<VdbDetailsBean>>() {
    		@Override
    		public void onReturn(List<VdbDetailsBean> serviceVdbs) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("dslibrary.clone-success"), //$NON-NLS-1$
                        i18n.format("dslibrary.clone-success-msg")); //$NON-NLS-1$
                
                // Convert VDBDetails to rows for the display
    			List<ServiceRow> serviceTableRows = getServiceRows(serviceVdbs);

    			currentServices.clear();
    			currentServices.addAll(serviceTableRows);
    			
    			if(serviceTableRows.isEmpty()) {
    				placeManager.goTo(Constants.DATA_SERVICES_EMPTY_LIBRARY_SCREEN);
    			} else {
    		     	populateGrid(serviceTableRows);
    			}
    		}
    		@Override
    		public void onError(Throwable error) {
    			notificationService.completeProgressNotification(notificationBean.getUuid(),
    					i18n.format("dslibrary.service-clone-error"), //$NON-NLS-1$
    					error);
    		}
    	});
    }
    
    /**
     * Convert the list of VdbDetailsBeans to ServiceTableRows for the displayer
     * @param serviceVdbs list of service VDB details
     * @return the list of ServiceTableRows
     */
    private List<ServiceRow> getServiceRows(List<VdbDetailsBean> serviceVdbs) {
    	if(serviceVdbs.isEmpty()) return Collections.emptyList();
    	
		List<ServiceRow> serviceRows = new ArrayList<ServiceRow>();
		// Each service VDB contains a single view that represents the service
		for(VdbDetailsBean serviceVdb : serviceVdbs) {
			Collection<VdbModelBean> modelList = serviceVdb.getModels();
			for(VdbModelBean model : modelList) {
				String modelName = model.getName();
				String description = model.getDescription();
				String modelType = model.getType();
				boolean isVisible = model.isVisible();
				String status = model.getStatus();
				if(modelType.equals(Constants.MODEL_TYPE_VIRTUAL)) {
					ServiceRow srow = new ServiceRow();
					srow.setName(modelName);
					srow.setDescription(description);
					srow.setVisible(isVisible);
					srow.setStatus(status);
					serviceRows.add(srow);
				}
			}
		}
		return serviceRows;
    }
    
    protected void doSaveServiceToFile(String serviceName) {
        String contentUrl = getWebContext() + "/services/dataVirtDownload?vdbname="+serviceName; //$NON-NLS-1$
       	Window.open(contentUrl,"_blank",null);
    }
     	
    private String getWebContext() {
        String context = GWT.getModuleBaseURL().replace( GWT.getModuleName() + "/", "" );
        if ( context.endsWith( "/" ) ) {
            context = context.substring( 0, context.length() - 1 );
        }
        return context;
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
     * Event handler that fires when the user clicks the CreateService button.
     * @param event
     */
    @EventHandler("btn-create-service")
    public void onCreateServiceButtonClick(ClickEvent event) {
    	doCreateService();
    }
    
    /**
     * Event handler that fires when the user clicks the Manage Sources button.
     * @param event
     */
    @EventHandler("btn-manage-sources")
    public void onManageSourcesButtonClick(ClickEvent event) {
    	doManageSources();
    }
    
    /**
     * Create Service - transitions to the Create Services page
     */
    protected void doCreateService() {
    	placeManager.goTo(Constants.CREATE_DATA_SERVICE_SCREEN);
    }
    
    /**
     * Create Service - transitions to the Manage Sources page
     */
    protected void doManageSources() {
		// transition to ManageSourcesScreen
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.FROM_SCREEN, Constants.DATA_SERVICES_LIBRARY_SCREEN);
    	placeManager.goTo(new DefaultPlaceRequest(Constants.MANAGE_SOURCES_SCREEN,parameters));
    }
    
}
