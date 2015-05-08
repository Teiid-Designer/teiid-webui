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
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.widgets.CheckableNameTypeRow;
import org.teiid.webui.client.widgets.ColumnNamesTable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("./SelectTableColumnsPage.html")
/**
 * SelectTableColumnsPage
 * This page is used to select the projected columns for a single source 'relational' table
 *
 */
public class SelectTableColumnsPage extends Composite {

    @Inject
    private ClientMessages i18n;
    
    @Inject Event<UiEvent> setDdlEvent;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject @DataField("lbl-table-columns-title")
    protected Label titleLabel;
    @Inject @DataField("lbl-table-columns-message")
    protected Label messageLabel;
    @Inject @DataField("lbl-table-title")
    protected Label tableTitleLabel;
    @Inject @DataField("tbl-table-columns")
    protected ColumnNamesTable columnsTable;
    @Inject @DataField("btn-table-columns-createDdl")
    private Button replaceDdlButton;
    
    private ViewEditorWizardPanel wizard;
   
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	messageLabel.setText("Select the projected columns for your view");
    	
    	// Tooltips
    	columnsTable.setTitle(i18n.format("ssource-editor-panel.columnsTable.tooltip"));
    }
    
    /**
     * Used to update panel right before it is shown
     */
    public void update() {
        // Initialize based on the ViewEditorManager selections
    	ViewEditorManager editorManager = ViewEditorManager.getInstance();
    	
    	String tableName = editorManager.getTable(0);
        titleLabel.setText(i18n.format("select-columns-page.title", tableName));
        tableTitleLabel.setText(tableName);

    	List<CheckableNameTypeRow> allColumns = editorManager.getColumns(0);
        if(allColumns!=null) {
        	columnsTable.setData(allColumns);
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
     * Event handler that fires when the user clicks the Replace DDL button.
     * @param event
     */
    @EventHandler("btn-table-columns-createDdl")
    public void onReplaceDdlButtonClick(ClickEvent event) {
    	List<String> colNames = columnsTable.getSelectedColumnNames();
    	List<String> colTypes = columnsTable.getSelectedColumnTypes();
    	
    	if(colNames.isEmpty()) {
    		Window.alert("Please select one or more columns");
    		return;
    	}

    	// Set the selected columns before generating ddl
    	ViewEditorManager editorManager = ViewEditorManager.getInstance();
    	editorManager.setSelectedColumns(0, colNames);
    	editorManager.setSelectedColumnTypes(0, colTypes);
    	
    	String viewDdl = editorManager.getViewDdl();
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