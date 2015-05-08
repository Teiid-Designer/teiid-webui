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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.NotificationService;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.webui.client.widgets.validation.EmptyNameValidator;
import org.teiid.webui.client.widgets.validation.TextChangeListener;
import org.teiid.webui.client.widgets.validation.ValidatingTextArea;
import org.teiid.webui.client.widgets.vieweditor.ViewEditorManager;
import org.teiid.webui.client.widgets.vieweditor.ViewEditorWizardPanel;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.NotificationBean;
import org.teiid.webui.share.beans.VdbDetailsBean;
import org.teiid.webui.share.beans.VdbModelBean;
import org.teiid.webui.share.beans.ViewModelRequestBean;
import org.teiid.webui.share.services.StringUtils;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

@Templated("./ViewEditorPanel.html")
public class ViewEditorPanel extends Composite {

	private String serviceName = null;
	private boolean haveSuccessfullyTested = false;
	private String statusEnterName = null;
	private String statusEnterView = null;
	private String statusDefineViewSources = null;
	private String statusTestView = null;
	private String queryResultDefaultMsg = null;
	private String currentStatus = null;
	private String owner;
	//private List<String> availableSourceNames = new ArrayList<String>();
	
    @Inject
    private PlaceManager placeManager;
    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject @DataField("label-vieweditor-title")
    protected Label viewEditorPanelTitle;
    
    @Inject @DataField("label-vieweditor-description")
    protected Label viewEditorPanelDescription;
    
    @Inject @DataField("btn-vieweditor-manage-sources")
    protected Button manageSourceButton;
    
    @Inject @DataField("textarea-vieweditor-viewDdl")
    protected ValidatingTextArea viewDdlTextArea;
    
    @Inject @DataField("textarea-vieweditor-testQuery")
    protected TextArea testSqlTextArea;
 
    @Inject @DataField("panel-vieweditor-viewsources")
    protected ViewSourcePanel viewSourcePanel;
    
    @Inject @DataField("btn-vieweditor-test")
    protected Button testViewButton;
    
	private String workingDdl;
	private List<String> workingViewSrcNames;
	
    @Inject Event<UiEvent> stateChangedEvent;

    // Single Source Editor
    @Inject @DataField("view-editor-wizard")
    private ViewEditorWizardPanel viewEditorWizardPanel;
    
//    // Join Editor
//    @Inject @DataField("join-editor")
//    private JoinEditorPanel joinEditorPanel;
//    
//    // Templates Editor
//    @Inject @DataField("templates-editor")
//    private TemplatesEditorPanel templatesEditorPanel;
    
    // The results panel for display of example data
    private QueryResultsPanel queryResultsPanel;

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
		queryResultDefaultMsg = i18n.format("vieweditor-panel.query-results-default-message");
		statusEnterName = i18n.format("vieweditor-panel.status-label-enter-name");
		statusEnterView = i18n.format("vieweditor-panel.status-label-enter-view");
		statusDefineViewSources = i18n.format("vieweditor-panel.status-label-define-viewsources");
		statusTestView = i18n.format("vieweditor-panel.status-label-test-view");
		currentStatus = statusEnterView;

		viewDdlTextArea.addTextChangeListener(new TextChangeListener() {
            @Override
			public void textChanged(  ) {
            	haveSuccessfullyTested = false;
            	// Show default querypanel message
            	queryResultsPanel.showStatusMessage(queryResultDefaultMsg);
            	queryResultsPanel.setVisible(false);
            	// Update status
            	updateStatus();
            }
        });
		viewDdlTextArea.setVisibleLines(8);
		viewDdlTextArea.clearValidators();
		viewDdlTextArea.addValidator(new EmptyNameValidator());
		viewDdlTextArea.setText(Constants.BLANK);

    	// starting viewSources list is empty
    	List<String> sList = new ArrayList<String>();
    	viewSourcePanel.setData(sList,sList);
    	
    	// Tooltips
    	viewDdlTextArea.setTitle(i18n.format("vieweditor-panel.viewDdlTextArea.tooltip"));
    	testViewButton.setTitle(i18n.format("vieweditor-panel.testViewButton.tooltip"));
    	manageSourceButton.setTitle(i18n.format("vieweditor-panel.manageSourceButton.tooltip"));

    	updateStatus();
    }
    
    public void setTitle(String title) {
    	viewEditorPanelTitle.setText(title);
    }
    
    public void setDescription(String desc) {
    	viewEditorPanelDescription.setText(desc);
    }
    
    /**
     * Specify the results panel to be used for display of results
     * @param queryResultsPanel the results panel
     */
    public void setQueryResultsPanel(QueryResultsPanel queryResultsPanel) {
    	this.queryResultsPanel = queryResultsPanel;
    }
    
    /**
     * Refresh the available dataSources from the editorManager
     */
	public void refreshAvailableSources( ) {
		//this.availableSourceNames.clear();
		//this.availableSourceNames.addAll(availableSourceNames);
		//singleSourceEditorPanel.setAvailableSources(availableSourceNames);
		//joinEditorPanel.setAvailableSources(availableSourceNames);
		//ViewEditorManager.getInstance().setAvailableSources(availableSourceNames);
		viewEditorWizardPanel.refreshAvailableSources();
		viewSourcePanel.setAllAvailableSources(ViewEditorManager.getInstance().getAvailableSourceNames());
	}
    
    public void setViewDdl(String ddlStr) {
    	this.viewDdlTextArea.setText(ddlStr); 
    	updateStatus();
    }
    
    public void setViewSources(List<String> viewSources) {
    	this.viewSourcePanel.setData(viewSources,ViewEditorManager.getInstance().getAvailableSourceNames());
    	updateStatus();
    }
    
    public List<String> getViewSources( ) {
    	return this.viewSourcePanel.getData();
    }
    
    public String getViewDdl( ) {
    	return this.viewDdlTextArea.getText();    	
    }
    
    public void setServiceName(String svcName) {
    	this.serviceName = svcName;
    	this.testSqlTextArea.setText(Constants.BLANK);  // Force reset of test query if service name changes
    	this.viewEditorWizardPanel.reset();  // Resets any saved state
    	updateStatus();
    }

    public void setOwner(String owner) {
    	this.owner = owner;
    }
    
    public String getOwner() {
    	return this.owner;
    }
    
    /**
     * Handles UiEvents
     * @param dEvent
     */
    public void onDialogEvent(@Observes UiEvent dEvent) {
    	// User has OK'd source rename
    	if(dEvent.getType() == UiEventType.VIEW_DEFN_REPLACE_OK) {
    		replaceViewDefn(workingDdl,workingViewSrcNames);
    	// User has OK'd source redeploy
    	} else if(dEvent.getType() == UiEventType.VIEW_DEFN_REPLACE_CANCEL) {
    	} else if(dEvent.getType() == UiEventType.VIEW_SOURCES_CHANGED) {
    		updateStatus();
    	} else if(dEvent.getType() == UiEventType.VIEW_DEFN_REPLACE_FROM_JOIN_EDITOR) {
    		handleViewReplaceEvent(dEvent);
    	} else if(dEvent.getType() == UiEventType.VIEW_DEFN_REPLACE_FROM_SSOURCE_EDITOR) {
    		handleViewReplaceEvent(dEvent);
    	} else if(dEvent.getType() == UiEventType.VIEW_DEFN_ADD_COLS_FROM_SSOURCE_EDITOR) {
    		String ddl = viewDdlTextArea.getText();
    		String colString = dEvent.getViewDdl();
    		List<String> viewSrcs = dEvent.getViewSources();
    		replaceViewDefn(ddl+'\n'+colString,viewSrcs);
    	} else if(dEvent.getType() == UiEventType.VIEW_DEFN_REPLACE_FROM_TEMPLATES_EDITOR) {
    		handleViewReplaceEvent(dEvent);
    	}
    }
    
    /**
     * Handle event requesting replacement of the view
     * @param viewReplaceEvent
     */
    private void handleViewReplaceEvent(UiEvent viewReplaceEvent) {
    	String ddl = viewReplaceEvent.getViewDdl();
		List<String> viewSrcs = viewReplaceEvent.getViewSources();
    	
    	// Nothing in viewDDL area - safe to replace without confirmation
    	if(StringUtils.isEmpty(viewDdlTextArea.getText())) {
    		replaceViewDefn(ddl,viewSrcs);
        // has View DDL - confirm before replace.
    	} else {
    		// Sets working vars.  If confirmed, the working vars are used to replace content 
    		workingDdl = ddl;
    		workingViewSrcNames = viewSrcs;
    		showConfirmOverwriteDialog();
    	}
    }
    
    /**
     * Shows the confirmation dialog for overwrite of view defn
     */
    private void showConfirmOverwriteDialog() {
		// Display the Confirmation Dialog for replacing the view defn
		Map<String,String> parameters = new HashMap<String,String>();
    	String dMsg = i18n.format("ds-properties-panel.confirm-overwrite-dialog-message");
		parameters.put(Constants.CONFIRMATION_DIALOG_MESSAGE, dMsg);
		parameters.put(Constants.CONFIRMATION_DIALOG_TYPE, Constants.CONFIRMATION_DIALOG_REPLACE_VIEW_DEFN);
    	placeManager.goTo(new DefaultPlaceRequest(Constants.CONFIRMATION_DIALOG,parameters));
    }
    
    /**
     * Replace the ViewDefn TextArea with the supplied DDL
     * @param ddl the ddl
     * @param viewSrcNames the viewSrcNames needed for this view
     */
    private void replaceViewDefn(String ddl,List<String> viewSrcNames) {
    	viewDdlTextArea.setText(ddl);  
    	if(viewSrcNames!=null) {
    		viewSourcePanel.setData(viewSrcNames,ViewEditorManager.getInstance().getAvailableSourceNames());
    	}
    	
    	haveSuccessfullyTested = false;
    	queryResultsPanel.showStatusMessage(queryResultDefaultMsg);
    	updateStatus();
    }
    
    /**
     * Event handler that fires when the user clicks the Test button.
     * @param event
     */
    @EventHandler("btn-vieweditor-test")
    public void onTestViewButtonClick(ClickEvent event) {
    	doTestView();
    }
    
    /**
     * Event handler that fires when the user clicks the Manage Sources button.
     * @param event
     */
    @EventHandler("btn-vieweditor-manage-sources")
    public void onManageSourcesButtonClick(ClickEvent event) {
    	fireGoToManageSources();
    }
    
    private void doTestView() {
    	final String serviceName = this.serviceName;
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("vieweditor-panel.testing-service-title"), //$NON-NLS-1$
                i18n.format("vieweditor-panel.testing-service-msg", serviceName)); //$NON-NLS-1$
            	
    	String viewDdl = viewDdlTextArea.getText();
    	List<String> rqdImportVdbNames = getViewSourceVdbNames();
    	
    	ViewModelRequestBean viewModelRequest = new ViewModelRequestBean();
    	viewModelRequest.setName(serviceName);
    	viewModelRequest.setDescription("Test Service");
    	viewModelRequest.setDdl(viewDdl);
    	viewModelRequest.setVisible(true);
    	viewModelRequest.setRequiredImportVdbNames(rqdImportVdbNames);
    	    	
    	// VDB properties
    	Map<String,String> vdbPropMap = new HashMap<String,String>();
    	vdbPropMap.put(Constants.VDB_PROP_KEY_REST_AUTOGEN, "true");
    	    	
    	final String testVDBName = Constants.SERVICE_TEST_VDB_PREFIX+serviceName;
    	teiidService.deployNewVDB(testVDBName, 1, vdbPropMap, viewModelRequest, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("vieweditor-panel.testing-service-complete"), //$NON-NLS-1$
                        i18n.format("vieweditor-panel.testing-service-complete-msg")); //$NON-NLS-1$

                // If VDB is not active, show the status message
                if(!vdbDetailsBean.isActive()) {
                	haveSuccessfullyTested = false;
                	// Get ViewModel error.  Otherwise take any of the source model errors.
                	Collection<VdbModelBean> models = vdbDetailsBean.getModels();
                	String modelError = null;
                	String currentError = null;
                	for(VdbModelBean model : models) {
                		currentError = model.getStatus();
                		if(!currentError.equalsIgnoreCase(Constants.STATUS_ACTIVE)) {
                			modelError = currentError;
                			if(model.isView()) {
                				break;
                			}
                		}
                	}
                	queryResultsPanel.showErrorMessage(getUserReadableModelErrorMessage(modelError));
                	queryResultsPanel.setVisible(true);
                } else {
                	haveSuccessfullyTested = true;

                	String testVdbJndi = Constants.JNDI_PREFIX+testVDBName;
                	String serviceSampleSQL = testSqlTextArea.getText();
                	queryResultsPanel.showResultsTable(testVdbJndi, serviceSampleSQL);
                	queryResultsPanel.setVisible(true);
                }
                updateStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("vieweditor-panel.testing-service-error"), error); //$NON-NLS-1$
                haveSuccessfullyTested = false;
                updateStatus();
            }
        });           	
    }
    
    /**
     * Get a more user readable error message for display
     * @param modelError the model error
     * @return the user friendly error
     */
    private String getUserReadableModelErrorMessage(String modelError) {
    	String userReadableMessage = i18n.format("vieweditor-panel.testVdbError.genericError");
    	if(Constants.VDB_INACTIVE_DS_CONNECTION_ERROR.equalsIgnoreCase(modelError)) {
        	userReadableMessage = i18n.format("vieweditor-panel.testVdbError.sourceConnectionError");
    	} else if(modelError.startsWith(Constants.VDB_INACTIVE_SQL_PARSE_ERROR)) {
    		String errorDetail = modelError.substring(Constants.VDB_INACTIVE_SQL_PARSE_ERROR.length());
        	userReadableMessage = i18n.format("vieweditor-panel.testVdbError.sqlParseError")+" :  "+errorDetail;
    	} else if(modelError.startsWith(Constants.VDB_INACTIVE_SQL_VALIDATION_ERROR)) {
    		String errorDetail = modelError.substring(Constants.VDB_INACTIVE_SQL_VALIDATION_ERROR.length());
        	userReadableMessage = i18n.format("vieweditor-panel.testVdbError.sqlValidationError")+" :  "+errorDetail;
    	} else if(Constants.VDB_INACTIVE_METADATA_LOADING.equalsIgnoreCase(modelError)) {
        	userReadableMessage = i18n.format("vieweditor-panel.testVdbError.metadataLoadingError");
    	} else if(Constants.VDB_INACTIVE_UNKNOWN_ERROR.equalsIgnoreCase(modelError)) {
        	userReadableMessage = i18n.format("vieweditor-panel.testVdbError.genericError");
    	}
    	return userReadableMessage;  
    }
    
    /**
     * Get the corresponding SrcVdbNames for the View Source names table
     * @return
     */
    public List<String> getViewSourceVdbNames( ) {
    	List<String> viewSourceNames = viewSourcePanel.getData();
    	List<String> srcVdbNames = new ArrayList<String>(viewSourceNames.size());
    	for(String dsName : viewSourceNames) {
    		srcVdbNames.add(Constants.SERVICE_SOURCE_VDB_PREFIX+dsName);
    	}
    	return srcVdbNames;
    }
    
    /**
     * Fire state changed
     */
    public void fireStateChanged( ) {
    	stateChangedEvent.fire(new UiEvent(UiEventType.VIEW_EDITOR_CHANGED));
    }
    
    /**
     * Fire go to manage soruces
     */
    public void fireGoToManageSources( ) {
    	UiEvent event = new UiEvent(UiEventType.VIEW_EDITOR_GOTO_MANAGE_SOURCES);
    	event.setEventSource(getOwner());
    	stateChangedEvent.fire(event);
    }
    
	private void updateStatus( ) {
    	currentStatus = Constants.OK;
    	
    	// Must have the service name
    	if(StringUtils.isEmpty(this.serviceName)) {
    		currentStatus = statusEnterName;
    	}
    	
		// Check view DDL - if serviceName ok
    	if(Constants.OK.equals(currentStatus)) {
    		if(!viewDdlTextArea.isValid()) {
    			currentStatus = statusEnterView;
    		}
    	}
    	
		// Check at least one view source is defined
    	if(Constants.OK.equals(currentStatus)) {
    		if(getViewSources().isEmpty()) {
    			currentStatus = statusDefineViewSources;
    		}
    	}
    	
		// Force the user to successfully test the service first
    	if(Constants.OK.equals(currentStatus)) {
    		// Force the user to successfully test the service
    		if(!haveSuccessfullyTested) {
    			currentStatus = statusTestView;
    		}
    		testViewButton.setEnabled(true);
    		// Populate the Test Query area
    		if(StringUtils.isEmpty(testSqlTextArea.getText())) {
    			String testQuery = Constants.SELECT_STAR_FROM+Constants.SPACE + 
			           serviceName+Constants.DOT+Constants.SERVICE_VIEW_NAME+
			           Constants.SPACE+Constants.LIMIT_10;
    			testSqlTextArea.setText(testQuery);
    		}
			testSqlTextArea.setEnabled(true);
    	} else {
    		testViewButton.setEnabled(false);
			testSqlTextArea.setText(Constants.BLANK);
			testSqlTextArea.setEnabled(false);
    	}
    	
    	fireStateChanged();
    }
	
	public String getStatus() {
		return this.currentStatus;
	}
        
}