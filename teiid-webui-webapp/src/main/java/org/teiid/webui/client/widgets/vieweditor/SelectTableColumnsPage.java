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
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.widgets.CheckableNameTypeRow;
import org.teiid.webui.client.widgets.table.ColumnNamesTable;
import org.teiid.webui.share.Constants;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("./SelectTableColumnsPage.html")
/**
 * ViewEditor wizard page for selection of a single table's columns
 */
public class SelectTableColumnsPage extends Composite {

    @Inject
    private ClientMessages i18n;
    
    @Inject Event<UiEvent> setDdlEvent;
    
    @Inject @DataField("lbl-table-columns-message")
    protected Label messageLabel;
    @Inject @DataField("lbl-table-title")
    protected Label tableTitleLabel;
    @Inject @DataField("tbl-table-columns")
    protected ColumnNamesTable columnsTable;
    
	private ViewEditorWizardPanel wizard;
	private ViewEditorManager editorManager = ViewEditorManager.getInstance();
	private String currentStatus = Constants.BLANK;
	private String msgCheckOneOrMoreColumns;
	private String msgClickApplyWhenFinished;
	   
   /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	columnsTable.setOwner(this.getClass().getName());
 
    	msgCheckOneOrMoreColumns = i18n.format("select-table-columns-page.check-one-or-more-columns.message");
    	msgClickApplyWhenFinished = i18n.format("select-table-columns-page.click-apply-when-finished.message");
    	
    	messageLabel.setText(msgCheckOneOrMoreColumns);
    	
    	// Tooltips
    	columnsTable.setTitle(i18n.format("select-table-columns-page.columnsTable.tooltip"));
    }
    
    /**
     * Refresh the panel using state from the ViewEditorManager
     */
    public void update() {
    	String tableName = editorManager.getTable(0);
    	this.wizard.setWizardPageTitle(i18n.format("select-table-columns-page.title"));
        tableTitleLabel.setText(tableName);

    	List<CheckableNameTypeRow> allColumns = editorManager.getColumns(0);
        if(allColumns!=null) {
        	columnsTable.setData(allColumns);
        }
        updateStatus();
    }
    
    /**
     * Set the owner wizardPanel
     * @param wizard the wizard
     */
    public void setWizard(ViewEditorWizardPanel wizard) {
    	this.wizard = wizard;
    }
    
    /**
     * Handles UiEvents from columnNamesTable
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// checkbox change event from column names table
    	if(dEvent.getType()==UiEventType.COLUMN_NAME_TABLE_CHECKBOX_CHANGED && dEvent.getEventSource().equals(this.getClass().getName())) {
    		List<String> selectedColumns = columnsTable.getSelectedColumnNames();
    		List<String> selectedColumnTypes = columnsTable.getSelectedColumnTypes();
    		editorManager.setSelectedColumns(0, selectedColumns);
    		editorManager.setSelectedColumnTypes(0, selectedColumnTypes);
    		updateStatus();
    	}
    }
    
    /**
     * Update panel status
     */
	private void updateStatus( ) {
    	currentStatus = Constants.OK;
    	
		// Ensure some columns are selected
    	if(Constants.OK.equals(currentStatus)) {
    		List<String> selectedColumns = editorManager.getSelectedColumns(0);
    		int nCols = (selectedColumns==null) ? 0 : selectedColumns.size();
    		if(nCols == 0) {
    			currentStatus = msgCheckOneOrMoreColumns;
    		}
    	}
    	
		// Enable setDdlButton button if OK
    	if(Constants.OK.equals(currentStatus)) {
    		messageLabel.setText(msgClickApplyWhenFinished);
    		this.wizard.setNextOrReplaceButton(true);
    	} else {
    		messageLabel.setText(currentStatus);
    		this.wizard.setNextOrReplaceButton(false);
    	}
    	
    }
	
    /**
     * Handles when the user clicks the Replace DDL button.
     */
    public void replaceDdlClicked( ) {
    	List<String> colNames = columnsTable.getSelectedColumnNames();
    	List<String> colTypes = columnsTable.getSelectedColumnTypes();
    	
    	if(colNames.isEmpty()) {
    		Window.alert("Please select one or more columns");
    		return;
    	}

    	// Set the selected columns before generating ddl
    	editorManager.setSelectedColumns(0, colNames);
    	editorManager.setSelectedColumnTypes(0, colTypes);
    	
    	// Builds a new View DDL based on selections and moves it to the View Defn area.
    	String viewDdl = editorManager.buildViewDdl();
    	List<String> sources = editorManager.getSources();
     	
		UiEvent uiEvent = new UiEvent(UiEventType.VIEW_DEFN_REPLACE_FROM_SSOURCE_EDITOR);
		uiEvent.setViewDdl(viewDdl);
		uiEvent.setViewSources(sources);
		
		setDdlEvent.fire(uiEvent);
    }
    
    /**
     * Get the List of selected Column names
     * @return the column names
     */
    public List<String> getSelectedColumnNames() {
    	return columnsTable.getSelectedColumnNames();
    }
    
    /**
     * Get the List of selected Column types
     * @return the column types
     */
    public List<String> getSelectedColumnTypes() {
    	return columnsTable.getSelectedColumnTypes();
    }
               
}