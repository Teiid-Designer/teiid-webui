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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.utils.UiUtils;
import org.teiid.webui.share.Constants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;

@Dependent
@Templated("./QueryResultsPanel.html")
public class QueryResultsPanel extends Composite {

    protected HTML statusText = new HTML();
	private boolean resultTableVisible = false;
	private String fetchingDataMessage;
	private String noRowsMessage;
	
    @Inject
    private ClientMessages i18n;
    
    @Inject
    protected TeiidRpcService teiidService;
    
    @Inject @DataField("textarea-testQuery")
    protected TextArea testQueryArea;
    
    @Inject @DataField("btn-runQuery")
    protected Button runQueryButton;
    
    @Inject @DataField("content-deckpanel")
    protected DeckPanel contentDeckPanel;
        
    @Inject
    protected QueryResultPagedTableDisplayer queryResultsTablePaged;
    
    private String currentSourceJndiName;
    private String currentSql;
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	fetchingDataMessage = i18n.format("query-resultpanel.status-fetch-data-message");
    	noRowsMessage = i18n.format("query-resultpanel.status-norows-message");

    	runQueryButton.setIcon( IconType.REFRESH );
    	
    	statusText.setHTML(UiUtils.getStatusMessageHtml(Constants.BLANK,UiUtils.MessageType.INFO));
    	
    	// Add properties panel and Select label to deckPanel
    	contentDeckPanel.add(statusText);
    	contentDeckPanel.add(queryResultsTablePaged);
    	contentDeckPanel.showWidget(0);
    	resultTableVisible=false;
    	
    	// Tooltips
    	queryResultsTablePaged.setTitle(i18n.format("query-resultpanel.result-table.tooltip"));
    }   

    /**
     * Event handler that fires when the user clicks the Run button.
     * @param event
     */
    @EventHandler("btn-runQuery")
    public void onRunButtonClick(ClickEvent event) {
    	String query = testQueryArea.getText();
    	
    	showStatusMessage(fetchingDataMessage);
    	
    	// Set the provider and sql.  UiEvent is fired when it completes.
    	this.currentSql = query;
    	queryResultsTablePaged.setDataProvider(this.currentSourceJndiName, this.currentSql);    
    }
    
    /**
     * Set the status message
     */
    public void showStatusMessage(String statusMsg) {
    	statusText.setHTML(UiUtils.getStatusMessageHtml(statusMsg,UiUtils.MessageType.SUCCESS));
    	showMessage();
    }
    
    /**
     * Set the status message
     */
    public void showErrorMessage(String statusMsg) {
    	statusText.setHTML(UiUtils.getStatusMessageHtml(statusMsg,UiUtils.MessageType.ERROR));
    	showMessage();
    }
    
    private void showMessage() {
    	if(resultTableVisible) {
    		contentDeckPanel.showWidget(0);
    		resultTableVisible = false;
    	}
    }
    
    /**
     * Set the Source JNDI and the query for the panel
     */
    public void showResultsTable(String sourceJndiName, String sql) {
    	showStatusMessage(fetchingDataMessage);
    	
    	// Save initial source and sql
    	this.currentSourceJndiName = sourceJndiName;
    	this.currentSql = sql;
    	testQueryArea.setText(sql);   
    	
    	// Set the provider and sql.  UiEvent is fired when it completes.
    	queryResultsTablePaged.setDataProvider(sourceJndiName, sql);    
    }
    
    private void showQueryTable() {
    	if(!resultTableVisible) {
    		contentDeckPanel.showWidget(1);
    		resultTableVisible = true;
    	}
    }
    
    /**
     * Handles Events from QueryResultPagedTableDisplayer
     * @param dEvent
     */
    public void onRefreshedEvent(@Observes UiEvent dEvent) {
    	// Table refresh has completed successfully.  Show the table
    	if(dEvent.getType() == UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_OK) {
    		showQueryTable();
    	// Refresh ok, but no data rows.
    	} else if(dEvent.getType() == UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_NOROWS) {
    		showErrorMessage(noRowsMessage);
        // Table refresh failed.  Show error message
    	} else if(dEvent.getType() == UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_ERROR) {
    		String errorMsg = queryResultsTablePaged.getErrorMessage();
    		showErrorMessage(errorMsg);
    	}
    }

}