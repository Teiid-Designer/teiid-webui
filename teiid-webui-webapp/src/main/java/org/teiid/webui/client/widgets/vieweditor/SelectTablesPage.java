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
package org.teiid.webui.client.widgets.vieweditor;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.share.Constants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

@Dependent
@Templated("./SelectTablesPage.html")
/**
 * ViewEditor wizard page for selection of the view's tables
 */
public class SelectTablesPage extends Composite {

    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject Event<UiEvent> setDdlEvent;
 
    @Inject @DataField("deckpanel")
    private DeckPanel leftDeckPanel;
    @Inject @DataField("listwidget-tableitem-panel")
    private TableFlowListWidget selectedTablesList;
    
    @Inject 
    private TableSelectorWidget tableSelector;
    
    private String currentStatus = "";
    private ViewEditorWizardPanel wizard;

	private ViewEditorManager editorManager = ViewEditorManager.getInstance();
	   
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
        Button addButton = new Button("Add a Table");
        // Add Button click swaps the Left DeckPanel to show selectors
        addButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( final ClickEvent event ) {
				tableSelector.resetSourceSelection();
		    	leftDeckPanel.showWidget(2);
			}
		} );
        VerticalPanel buttonPanel = new VerticalPanel();
        buttonPanel.add(addButton);
        
        HTMLPanel noMoreSourcesPanel = new HTMLPanel("<p>Proceed to next page, or remove table to replace it</p>");
        
    	// Add properties panel and Select label to deckPanel
        leftDeckPanel.add(buttonPanel);
        leftDeckPanel.add(noMoreSourcesPanel);
        leftDeckPanel.add(tableSelector);
        leftDeckPanel.showWidget(0);
    }
    
    
    /**
     * Refresh the panel using state from the ViewEditorManager
     */
    public void update() {
        tableSelector.refreshAvailableSources();
        updateStatus();

    	// Selected tables obtained from manager
    	List<TableListItem> selectedTables = editorManager.getTableItems();
    	selectedTablesList.setItems(selectedTables);
    	
    	// Show message if two tables
    	if(selectedTables.size()>=2) {
    		leftDeckPanel.showWidget(1);
    	// Show add button if less than two tables
    	} else {
    		leftDeckPanel.showWidget(0);
    	}
    	
    	// Set next button enabled state
    	if(selectedTables.isEmpty()) {
    		this.wizard.setNextButtonEnabled(false);
    	} else {
    		this.wizard.setNextButtonEnabled(true);
    	}
    }
    
	/**
	 * Handles UiEvents from viewEditorPanel
	 * @param dEvent
	 */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// change received from viewEditor
    	if(dEvent.getType() == UiEventType.REMOVE_TABLE) {
        	TableListItem tableItem = dEvent.getRemoveTable();
        	int tableIndx = editorManager.getTableIndex(tableItem.getTableName());
        	if(tableIndx>=0) {
        		editorManager.removeTable(tableIndx);
        		List<TableListItem> newTables = editorManager.getTableItems();
        		selectedTablesList.setItems(newTables);
        	}
        	updateDeckPanelAndWizardButtons(editorManager);
    	} else if(dEvent.getType() == UiEventType.SET_TABLES) {
    		List<TableListItem> tableItems = dEvent.getNewTables();
    		selectedTablesList.setItems(tableItems);
        	updateDeckPanelAndWizardButtons(editorManager);
    	}
    }
    
    private void updateDeckPanelAndWizardButtons(ViewEditorManager editorManager) {
    	// Show message if two tables
    	if(editorManager.getTables().size()>=2) {
    		leftDeckPanel.showWidget(1);
    	// Show add button if less than two tables
    	} else {
    		leftDeckPanel.showWidget(0);
    	}
    	
    	if(editorManager.getTables().size()>0) {
    		wizard.setNextButtonEnabled(true);
    	} else {
    		wizard.setNextButtonEnabled(false);
    	}
    }
    
    /**
     * Set the owner wizardPanel
     * @param wizard the wizard
     */
    public void setWizard(ViewEditorWizardPanel wizard) {
    	this.wizard = wizard;
    }
    
    /** 
     * Refreshes the sources listBox with the available sources for constructing the data services.
     * The available sources are obtained from the ViewEditorManager
     */
    public void refreshAvailableSources( ) {
    	tableSelector.update();
    	updateStatus();
	}
    
    /**
     * Update panel status
     */
	private void updateStatus( ) {
    	currentStatus = Constants.OK;
    	
    	// Ensure at least one table is selected
    	List<String> selectedTables = editorManager.getTables();
    	if(selectedTables.isEmpty()) {
    		currentStatus = "Select at least one table";
    	}
    	
		// Enable setDdlButton button if OK
    	if(Constants.OK.equals(currentStatus)) {
    		this.wizard.setNextButtonEnabled(true);
    	} else {
    		this.wizard.setNextButtonEnabled(false);
    	}

    }
	
	public String getStatus() {
		return this.currentStatus;
	}

           
}