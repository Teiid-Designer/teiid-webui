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

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("./SelectTablesPage.html")
/**
 * ViewEditor wizard page for selection of the view's tables
 */
public class SelectTablesPage extends Composite {

    @Inject
    private ClientMessages i18n;
    
    @Inject Event<UiEvent> setDdlEvent;
    @Inject Event<UiEvent> uiEvent;
 
    @Inject @DataField("deckpanel")
    private DeckPanel leftDeckPanel;
    @Inject @DataField("listwidget-tableitem-panel")
    private TableFlowListWidget selectedTablesList;
    @Inject @DataField("lbl-number-selected-message")
    private Label numberSelectedMsg;
    @Inject @DataField("icon-info")
    private Icon infoIcon;

    @Inject 
    private TableSelectorWidget tableSelector;
    
    private ViewEditorWizardPanel wizard;

	private ViewEditorManager editorManager = ViewEditorManager.getInstance();
	   
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	numberSelectedMsg.setText(i18n.format("select-tables-page.nSelected.message",0));
    	infoIcon.setType(IconType.INFO_CIRCLE);
    	
    	// Add table selector to deckpanel
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
    	setListTables(selectedTables);
    	
    	this.wizard.setWizardPageTitle(i18n.format("select-tables-page.title"));
    	
    	// Set next button enabled state
    	updateWizardButtons(editorManager);
    	
    	// Update the numberSelected message
    	updateNumberSelectedMessage(editorManager);
    }
    
    /**
     * Update the number of selected message
     * @param editorManager the editor manager
     */
    private void updateNumberSelectedMessage(ViewEditorManager editorManager) {
    	int nTables = editorManager.getTables().size();
    	if(nTables==1) {
        	numberSelectedMsg.setText(i18n.format("select-tables-page.oneSelected.message"));
    	} else {
        	numberSelectedMsg.setText(i18n.format("select-tables-page.nSelected.message",nTables));
    	}
    }
    
	/**
	 * Handles UiEvents from viewEditorPanel
	 * @param dEvent
	 */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// change received from viewEditor
    	if(dEvent.getType() == UiEventType.REMOVE_TABLE_FROM_LIST) {
        	TableListItem tableItem = dEvent.getRemoveTable();
        	int tableIndx = editorManager.getTableIndex(tableItem.getTableName());
        	if(tableIndx>=0) {
        		editorManager.removeTable(tableIndx);
        		List<TableListItem> newTables = editorManager.getTableItems();
            	setListTables(newTables);
            	// Refire so checklist can update
            	fireRemoveTableEvent(tableItem);
        	}
        	updateNumberSelectedMessage(editorManager);
        	updateWizardButtons(editorManager);
    	} else if(dEvent.getType() == UiEventType.SET_TABLES) {
    		List<TableListItem> tableItems = dEvent.getNewTables();
        	setListTables(tableItems);
        	updateNumberSelectedMessage(editorManager);
        	updateWizardButtons(editorManager);
    	}
    }
    
    /**
     * Fire status event for a dataSource
     * @param sourceName the source name
     * @param sourceTableName the table name
     */
    private void fireRemoveTableEvent(TableListItem tableItem) {
		UiEvent sEvent = new UiEvent(UiEventType.REMOVE_TABLE);
		sEvent.setRemoveTable(tableItem);
		uiEvent.fire(sEvent);
    }
    
    /**
     * Sets the list tables using the supplied manager tables.  Adds placeholder
     * entries as needed and resets the table list
     * @param managerTables the list of tables from the manager
     */
    private void setListTables(List<TableListItem> managerTables) {
    	if(managerTables.size()==0) {
    		TableListItem item1 = new TableListItem();
    		item1.setPlaceHolder(true);
    		TableListItem item2 = new TableListItem();
    		item2.setPlaceHolder(true);
    		managerTables.add(item1);
    		managerTables.add(item2);
    	} else if(managerTables.size()==1) {
    		TableListItem item = new TableListItem();
    		item.setPlaceHolder(true);
    		managerTables.add(item);
    	}
    	selectedTablesList.setItems(managerTables);
    }
    
    private void updateWizardButtons(ViewEditorManager editorManager) {
    	if(editorManager.getTables().size()>0) {
    		wizard.setNextOrReplaceButton(true);
    	} else {
    		wizard.setNextOrReplaceButton(false);
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
    	boolean okToProceed = true;;
    	
    	// Ensure at least one table is selected
    	List<TableListItem> selectedTables = editorManager.getTableItems();
    	if(selectedTables.size()==0) {
    		okToProceed = false;
    	}
    	
		// Enable setDdlButton button if OK
    	if(okToProceed) {
    		this.wizard.setNextOrReplaceButton(true);
    	} else {
    		this.wizard.setNextOrReplaceButton(false);
    	}

    }
           
}