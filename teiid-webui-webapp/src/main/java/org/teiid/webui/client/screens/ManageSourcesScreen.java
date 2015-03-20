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
import org.teiid.webui.client.resources.AppResource;
import org.teiid.webui.client.services.NotificationService;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.webui.client.widgets.DataSourceListPanel;
import org.teiid.webui.client.widgets.DataSourcePropertiesPanel;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.DataSourcePageRow;
import org.teiid.webui.share.beans.NotificationBean;
import org.teiid.webui.share.exceptions.DataVirtUiException;
import org.teiid.webui.share.services.StringUtils;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * ManageSourcesScreen - used for management of Data Sources
 *
 */
@Dependent
@Templated("./ManageSourcesScreen.html#page")
@WorkbenchScreen(identifier = "ManageSourcesScreen")
public class ManageSourcesScreen extends Composite {

	private Map<String,String> defaultTranslatorMap = new HashMap<String,String>();
	private SingleSelectionModel<DataSourcePageRow> listSelectionModel;
	private List<DataSourcePageRow> currentDataSourceList = new ArrayList<DataSourcePageRow>();
	private String requestingScreen;
	private String previousDSSelection = null;
	private DataSourcePageRow currentRowSelection = null;

    @Inject
    protected ClientMessages i18n;
    
	@Inject
	private NotificationService notificationService;
	
    @Inject
    private PlaceManager placeManager;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject @DataField("anchor-goback")
    protected Anchor goBackAnchor;
    
    @Inject @DataField("dslist-deckpanel")
    protected DeckPanel dsListDeckPanel;
    
    @Inject
    protected DataSourceListPanel dsListPanel;
    
    @Inject @DataField("details-deckpanel")
    protected DeckPanel detailsDeckPanel;
    
    @Inject 
    protected DataSourcePropertiesPanel propsPanel;
    
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
    	String selectSourcePanelHtml = i18n.format("managesources.select-source-text");
        HTMLPanel selectSourcePanel = new HTMLPanel(selectSourcePanelHtml);
        
    	// Add properties panel and Select label to deckPanel
    	detailsDeckPanel.add(propsPanel);
    	detailsDeckPanel.add(selectSourcePanel);
    	showDetailsPanelBlankMessage();
    	
    	// Deck panel for DataSource list
    	HTMLPanel spinnerPanel = new HTMLPanel(AbstractImagePrototype.create(AppResource.INSTANCE.images().spinnner24x24Image()).getHTML());
    	dsListDeckPanel.add(spinnerPanel);
    	dsListDeckPanel.add(dsListPanel);
    	doGetDataSourceInfos(null);

    	// Selection model for the dsList
    	listSelectionModel = new SingleSelectionModel<DataSourcePageRow>();
    	dsListPanel.setSelectionModel(listSelectionModel);
    	listSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange(SelectionChangeEvent event) {
    			DataSourcePageRow row = listSelectionModel.getSelectedObject();
    			currentRowSelection = row;
    			if(row!=null) {
    				showDetailsPanelProperties(row,currentDataSourceList);
        			propsPanel.setExternalError(row.getErrorMessage());
        	    	// Dont allow deletion of DV6.1 built-in sources
        	    	if(Constants.BUILTIN_SOURCES.contains(row.getName())) {
            			dsListPanel.setDeleteButtonEnabled(false);
        	    	} else {
            			dsListPanel.setDeleteButtonEnabled(true);
        	    	}
        			// Keep track of previous non-placeholder selection
        			if(row.getState()!=DataSourcePageRow.State.PLACEHOLDER) {
        				previousDSSelection = row.getName();
        			}
    			} else {
    				showDetailsPanelBlankMessage();
    				dsListPanel.setDeleteButtonEnabled(false);
    			}
    		}
    	});
    }
    
    @OnStartup
    public void onStartup( final PlaceRequest place ) {
    	String fromScreen = place.getParameter(Constants.FROM_SCREEN,Constants.UNKNOWN);
    	if(fromScreen!=null && !fromScreen.equals(Constants.UNKNOWN)) {
    		setRequestingScreen(fromScreen);
    	}
    }
    
    private void showDetailsPanelProperties(DataSourcePageRow dsRow, List<DataSourcePageRow> allDSRows) {
		propsPanel.setDataSource(dsRow,allDSRows);
		detailsDeckPanel.showWidget(0);
    }
    
    private void showDetailsPanelBlankMessage() {
		detailsDeckPanel.showWidget(1);
    }
    
    private void showDSListPanelSpinner() {
    	dsListDeckPanel.showWidget(0);
    }
    
    private void showDSListPanelList() {
    	dsListDeckPanel.showWidget(1);
    }
    
    /**
     * Handles UiEvents
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// User has OK'd source deletion
    	if(dEvent.getType() == UiEventType.DATA_SOURCE_ADD) {
    		onAddButtonClicked();
    	} else if(dEvent.getType() == UiEventType.DATA_SOURCE_DELETE) {
    		onDeleteButtonClicked();
    	} else if(dEvent.getType() == UiEventType.DELETE_SOURCE_OK) {
    		onDeleteConfirm();
    	// User cancelled edits on propery page
    	} else if(dEvent.getType() == UiEventType.SOURCE_EDIT_CANCEL) {
			// Cancel on new Source create.  Deletes the placeholder and selects previous selection
			if(currentRowSelection!=null && currentRowSelection.getState()==DataSourcePageRow.State.PLACEHOLDER) {
				currentDataSourceList.remove(currentRowSelection);
				dsListPanel.setData(currentDataSourceList);
				// Set previous selection, or first item
				if(!StringUtils.isEmpty(previousDSSelection)) {
					dsListPanel.setSelection(previousDSSelection);
				} else {
					dsListPanel.selectFirstItem();
				}
			// Cancel existing source changes.  just reselect the source to refresh.
			} else if(currentRowSelection!=null){
				dsListPanel.setSelection(currentRowSelection.getName());
			}
    	// User has cancelled source deletion
    	} else if(dEvent.getType() == UiEventType.DELETE_SOURCE_CANCEL) {
    	} else if(dEvent.getType() == UiEventType.DATA_SOURCE_DEPLOY_STARTING) {
        	updateDataSourceInfos(dEvent.getDataSourceName(), UiEventType.DATA_SOURCE_DEPLOY_STARTING);
    	} else if(dEvent.getType() == UiEventType.DATA_SOURCE_DEPLOY_COMPLETE) {
    		doGetDataSourceInfos(dEvent.getDataSourceName());
    	} else if(dEvent.getType() == UiEventType.DATA_SOURCE_DEPLOY_FAIL) {
    		doGetDataSourceInfos(dEvent.getDataSourceName());
    	}
    }
    
    /**
     * Just update the current DS in the DS List with the given event
     * @param dsName the datasource that changed
     * @param eventType the event type
     */
    private void updateDataSourceInfos(String dsName, UiEventType eventType) {
    	for(DataSourcePageRow dsRow : this.currentDataSourceList) {
    		if(dsRow.getName().equals(dsName)) {
    			if(eventType==UiEventType.DATA_SOURCE_DEPLOY_STARTING) {
    				dsRow.setState(DataSourcePageRow.State.DEPLOYING);
    			} else if(eventType==UiEventType.DATA_SOURCE_DEPLOY_COMPLETE) {
    				dsRow.setState(DataSourcePageRow.State.OK);
    			} else if(eventType==UiEventType.DATA_SOURCE_DEPLOY_FAIL) {
    				dsRow.setState(DataSourcePageRow.State.ERROR);
    			}
    		} else if(dsRow.getState()==DataSourcePageRow.State.PLACEHOLDER) {
    			dsRow.setState(DataSourcePageRow.State.DEPLOYING);
    		}
    	}
    	dsListPanel.setData(this.currentDataSourceList);
    }
    
    /**
     * Handler for DataSource added button clicks
     * @param event
     */
    public void onAddButtonClicked() {
    	DataSourcePageRow newDSRow = new DataSourcePageRow();
    	newDSRow.setName("New Data Source");
    	newDSRow.setState(DataSourcePageRow.State.PLACEHOLDER);
    	
    	currentDataSourceList.add(newDSRow);
    	
		dsListPanel.setData(currentDataSourceList);
		dsListPanel.setSelection("New Data Source");
    }

    /**
     * Handler for DataSource delete button clicks
     */
    public void onDeleteButtonClicked() {
		DataSourcePageRow row = listSelectionModel.getSelectedObject();
		DataSourcePageRow.State dsState = row.getState();
		// If row is a placeholder, go ahead and delete
		if(dsState==DataSourcePageRow.State.PLACEHOLDER) {
			this.currentDataSourceList.remove(row);
			this.dsListPanel.setData(this.currentDataSourceList);
			if(previousDSSelection!=null) {
				this.dsListPanel.setSelection(previousDSSelection);
			} else {
				showDetailsPanelBlankMessage();
			}
		// Confirm the delete
		} else {
	    	showConfirmDeleteDialog();
		}
    }
    
    /**
     * Shows the confirmation dialog for deleting a DataSource
     */
    private void showConfirmDeleteDialog() {
		DataSourcePageRow row = listSelectionModel.getSelectedObject();
		String dsName = row.getName();

		// Display the Confirmation Dialog for source rename
		Map<String,String> parameters = new HashMap<String,String>();
    	String dMsg = i18n.format("managesources.confirm-delete-dialog-message",dsName);
		parameters.put(Constants.CONFIRMATION_DIALOG_MESSAGE, dMsg);
		parameters.put(Constants.CONFIRMATION_DIALOG_TYPE, Constants.CONFIRMATION_DIALOG_SOURCE_DELETE);
    	placeManager.goTo(new DefaultPlaceRequest(Constants.CONFIRMATION_DIALOG,parameters));
    }
    
    /**
     * Does the DataSource deletion upon user confirmation
     */
    private void onDeleteConfirm() {
		DataSourcePageRow row = listSelectionModel.getSelectedObject();
    	List<String> dsNames = new ArrayList<String>();
    	String dsName = row.getName();
		dsNames.add(Constants.SERVICE_SOURCE_VDB_PREFIX+dsName);
    	dsNames.add(dsName);
		doDeleteDataSourcesAndVdb(dsNames,Constants.SERVICE_SOURCE_VDB_PREFIX+dsName,previousDSSelection);
    }
    
    /**
     * Called when the user confirms the dataSource deletion.
     */
    private void doDeleteDataSourcesAndVdb(final List<String> dsNames, final String vdbName, final String selectedDS) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("managesources.deleting-datasource-title"), //$NON-NLS-1$
                i18n.format("managesources.deleting-datasource-msg", "sourceList")); //$NON-NLS-1$
        teiidService.deleteDataSourcesAndVdb(dsNames, vdbName, new IRpcServiceInvocationHandler<List<DataSourcePageRow>>() {
            @Override
            public void onReturn(List<DataSourcePageRow> dsInfos) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("managesources.datasource-deleted"), //$NON-NLS-1$
                        i18n.format("managesources.delete-success-msg")); //$NON-NLS-1$
                
                // Update the page
                update(dsInfos,selectedDS);
            }
            @Override
            public void onError(Throwable error) {
            	showDSListPanelList();
            	notificationService.completeProgressNotification(notificationBean.getUuid(),
            			i18n.format("managesources.delete-error"), //$NON-NLS-1$
            			error);
            }
        });
    }
    
    /**
     * Populate the DataSource List.
     * @param selectedDS the selected DataSource, if selection is desired.
     */
    protected void doGetDataSourceInfos(final String selectedDS) {
    	showDSListPanelSpinner();
    	teiidService.getDataSources("filter", Constants.SERVICE_SOURCE_VDB_PREFIX, new IRpcServiceInvocationHandler<List<DataSourcePageRow>>() {
    		@Override
    		public void onReturn(List<DataSourcePageRow> dsInfos) {    			
    			// If selectedDS is null - page initialization.  If not null, this is in response to a specific source - display a 
    			// notification message if source has an error
    			for(DataSourcePageRow row : dsInfos) {
    				String name = row.getName();
    				if(!name.startsWith(Constants.SERVICE_SOURCE_VDB_PREFIX)) {
    					if(name.equals(selectedDS) && row.getState()==DataSourcePageRow.State.ERROR) {
    						String errorTitle = i18n.format("managesources.datasource-connection-error.title");
    						String errorMsg = i18n.format("managesources.datasource-connection-error.msg");
    		    			notificationService.sendErrorNotification(errorTitle, new DataVirtUiException(errorMsg));
    					}
    				}
    			}
                // Update the page
                update(dsInfos,selectedDS);
    		}
    		@Override
    		public void onError(Throwable error) {
            	showDSListPanelList();
    			notificationService.sendErrorNotification(i18n.format("managesources.error-getting-datasources"), error); //$NON-NLS-1$
    		}
    	});
    }
    
    /**
     * Update the dsList and details panel selection
     * @param dsRows the data source rows
     * @param selectedDS the selected data source
     */
    private void update(final List<DataSourcePageRow> dsRows, final String selectedDS) {
		DataSourcePageRow selectedRow = null;
		
		// Filter out the sources starting with SERVICE_SOURCE_VDB_PREFIX and SERVICES_VDBs
		List<DataSourcePageRow> tableRowList = new ArrayList<DataSourcePageRow>();
		for(DataSourcePageRow row : dsRows) {
			String name = row.getName();
			if(!name.startsWith(Constants.SERVICE_SOURCE_VDB_PREFIX)) {
				if(name.equals(selectedDS)) {
					selectedRow = row;
				}
				tableRowList.add(row);
			}
		}
		currentDataSourceList.clear();
		currentDataSourceList.addAll(tableRowList);
		
		dsListPanel.setData(tableRowList);
		if(selectedDS!=null) {
			dsListPanel.setSelection(selectedDS);
			showDetailsPanelProperties(selectedRow,tableRowList);
		} else {
			showDetailsPanelBlankMessage();
		}
		
    	doPopulateDefaultTranslatorMappings();
    	showDSListPanelList();
    }
    
    /**
     * Cache the Default Translator Mappings for later use.
     */
    protected void doPopulateDefaultTranslatorMappings() {
    	teiidService.getDefaultTranslatorMap(new IRpcServiceInvocationHandler<Map<String,String>>() {
    		@Override
    		public void onReturn(Map<String,String> defaultTranslatorsMap) {
    			defaultTranslatorMap = defaultTranslatorsMap;
    			propsPanel.setDefaultTranslatorMappings(defaultTranslatorMap);
    		}
    		@Override
    		public void onError(Throwable error) {
    			defaultTranslatorMap = new HashMap<String,String>();
    			notificationService.sendErrorNotification(i18n.format("managesources.error-populating-translatormappings"), error); //$NON-NLS-1$
    		}
    	});
    }
    
    public void setRequestingScreen(String screen) {
    	this.requestingScreen = screen;
    }
    
    /**
     * Event handler that fires when the user clicks the GoTo Library anchor.
     * @param event
     */
    @EventHandler("anchor-goback")
    public void onGoBackAnchorClick(ClickEvent event) {
    	Map<String,String> parameters = new HashMap<String,String>();
    	parameters.put(Constants.FROM_SCREEN, Constants.MANAGE_SOURCES_SCREEN);
    	if(this.requestingScreen!=null && this.requestingScreen.equals(Constants.EDIT_DATA_SERVICE_SCREEN)) {
        	placeManager.goTo(new DefaultPlaceRequest(Constants.EDIT_DATA_SERVICE_SCREEN,parameters));
    	} else {
        	placeManager.goTo(new DefaultPlaceRequest(Constants.CREATE_DATA_SERVICE_SCREEN,parameters));
    	}
    }
    
}
