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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
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
import org.teiid.webui.client.widgets.CheckableNameTypeRow;
import org.teiid.webui.client.widgets.TablesProcNamesTable;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.QueryColumnBean;
import org.teiid.webui.share.beans.QueryColumnResultSetBean;
import org.teiid.webui.share.beans.QueryTableProcBean;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

@Dependent
@Templated("./SelectTablesPage.html")
public class SelectTablesPageSAVE extends Composite {

    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject Event<UiEvent> setDdlEvent;
 
    @Inject @DataField("btn-add-table")
    private Button addTableButton;
    @Inject @DataField("lbl-select-tables-message")
    private Label lblMessage;
    @Inject @DataField("listbox-sources")
    private ListBox listboxSources;
    @Inject @DataField("tbl-source-tables")
    private TablesProcNamesTable sourceTablesTable;
    @Inject @DataField("listwidget-tableitem-panel")
    private TableFlowListWidget selectedTablesList;
    @Inject @DataField("btn-move-to-selected")
    private Button moveToSelectedButton;
//    @Inject @DataField("btn-remove-selected")
//    private Button removeSelectedButton;
    
    
	private Map<String,String> shortToLongTableNameMap = new HashMap<String,String>();
    private String currentStatus = "";
    private ViewEditorWizardPanel wizard;
	private String selectedSourceTable;
	//private String selectedTargetTable;
	private SingleSelectionModel<String> sourceTableSelectionModel;
	//private SingleSelectionModel<String> selectedTableSelectionModel;

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	lblMessage.setText("Select one or two tables from the available sources for your view");
    	sourceTablesTable.clear();
    	addTableButton.setEnabled(true);
    	
    	// Selected tables obtained from manager
    	ViewEditorManager editorManager = ViewEditorManager.getInstance();
    	List<TableListItem> tableItems = editorManager.getTableItems();
    	selectedTablesList.setItems(tableItems);
    	
        // Change Listener for Available sources ListBox
        listboxSources.addChangeHandler(new ChangeHandler()
        {
        	// Changing the updates status
        	public void onChange(ChangeEvent event)
        	{
        		sourceTablesTable.clear();
    			String srcName = getSelectedSource();
    			if (srcName != null) {
    				doGetTablesAndProcs(srcName);
    			}
        	}
        });
        
    	// SelectionModel to handle Table-procedure selection 
    	sourceTableSelectionModel = new SingleSelectionModel<String>();
    	sourceTablesTable.setSelectionModel(sourceTableSelectionModel); 
    	sourceTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange( SelectionChangeEvent event) { 
    			String selected = sourceTableSelectionModel.getSelectedObject();
    			selectedSourceTable = selected;
    			if (selected != null) {
    				moveToSelectedButton.setEnabled(true);
    			} else {
    				moveToSelectedButton.setEnabled(false);
    			}
    		} });
    	
    	// SelectionModel to handle selectedTables selection 
//    	selectedTableSelectionModel = new SingleSelectionModel<String>();
//    	selectedTablesTable.setSelectionModel(selectedTableSelectionModel); 
//    	selectedTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
//    		public void onSelectionChange( SelectionChangeEvent event) { 
//    			String selected = selectedTableSelectionModel.getSelectedObject();
//    			selectedTargetTable = selected;
//    			if (selected != null) {
//    				removeSelectedButton.setEnabled(true);
//    			} else {
//    				removeSelectedButton.setEnabled(false);
//    			}
//    		} });
    	moveToSelectedButton.setEnabled(false);
//    	removeSelectedButton.setEnabled(false);
    }
    
    /**
     * Used to update panel right before it is shown
     */
    public void update() {
    	// Selected tables obtained from manager
    	List<TableListItem> selectedTables = ViewEditorManager.getInstance().getTableItems();
    	selectedTablesList.setItems(selectedTables);
    	
    	// Set next button enabled state
    	if(selectedTables.isEmpty()) {
    		this.wizard.setNextButtonEnabled(false);
    	} else {
    		this.wizard.setNextButtonEnabled(true);
    	}
    }
    
    /**
     * Get the list of selected tables
     * @return the selected tables
     */
//    public List<String> getSelectedTables() {
//    	return this.selectedTablesTable.getData();
//    }
    
    /**
     * Event handler that fires when the user clicks the add table button.
     * @param event
     */
    @EventHandler("btn-add-table")
    public void onAddTableButtonClick(ClickEvent event) {
        listboxSources.setVisible(true);
        sourceTablesTable.setVisible(true);
        moveToSelectedButton.setVisible(true);
    }

    /**
     * Event handler that fires when the user clicks the Move to Selected Tables button.
     * @param event
     */
    @EventHandler("btn-move-to-selected")
    public void onMoveToSelectedButtonClick(ClickEvent event) {
    	int nSelectedTables = selectedTablesList.getWidgetCount();
    	// Maximum of 2 tables allowed
    	if(nSelectedTables>=2) return;
    	int newIndx = nSelectedTables;

    	ViewEditorManager editorManager = ViewEditorManager.getInstance();
    	// Set the table at the specified index
    	editorManager.setTable(newIndx,selectedSourceTable);
    	// Does fetch of the Table Columns
    	setManagerColumnsForTable(getSelectedSource(),selectedSourceTable,newIndx);
    	// Set source for Table 
    	editorManager.setSourceForTable(newIndx,getSelectedSource());
    	// Get Updated tables
    	List<TableListItem> newTables = editorManager.getTableItems();
    	selectedTablesList.setItems(newTables);

    	if(newTables.size()>0) {
    		wizard.setNextButtonEnabled(true);
    	} else {
    		wizard.setNextButtonEnabled(false);
    	}

    	sourceTableSelectionModel.clear();
    	
        listboxSources.setVisible(false);
        sourceTablesTable.setVisible(false);
        moveToSelectedButton.setVisible(false);
    }
    
    /**
     * Makes remote call to get the columns for the specified table.  Updates the manager columns for the table.
     * @param selectedSource the selected data source
     * @param selectedTable the selected table
     */
    private void setManagerColumnsForTable(String selectedSource, String selectedTable, int tableIndx) {
		doGetTableColumns(selectedSource, selectedTable, tableIndx, 1);
    }
    
	/**
	 * Handles UiEvents from viewEditorPanel
	 * @param dEvent
	 */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// change received from viewEditor
    	if(dEvent.getType() == UiEventType.REMOVE_TABLE) {
        	ViewEditorManager editorManager = ViewEditorManager.getInstance();
        	String tableName = dEvent.getRemoveTable().getTableName();
        	//String srcName = dEvent.getDataSourceName();
        	int tableIndx = editorManager.getTableIndex(tableName);
        	if(tableIndx>=0) {
        		editorManager.removeTable(tableIndx);
        		List<TableListItem> newTables = editorManager.getTableItems();
        		selectedTablesList.setItems(newTables);
            	if(newTables.size()>0) {
            		wizard.setNextButtonEnabled(true);
            	} else {
            		wizard.setNextButtonEnabled(false);
            	}
        	}
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
		populateSourcesListBox(ViewEditorManager.getInstance().getAvailableSourceNames());
    	updateStatus();
	}
    
    /**
     * Get the Tables and Procs for the supplied data source
     * @param dataSourceName the name of the source
     */
    protected void doGetTablesAndProcs(String dataSourceName) {
    	// Certain types, no need to do a server fetch
    	ViewEditorManager editorManager = ViewEditorManager.getInstance();
		String srcType = editorManager.getSourceType(dataSourceName);
		String translator = editorManager.getSourceTranslator(dataSourceName);
		if(srcType!=null) {
			if(srcType.equals("webservice") && !translator.equalsIgnoreCase("odata")) {
				List<String> procList = new ArrayList<String>();
				procList.add("invokeHttp");
				sourceTableSelectionModel.clear();
				sourceTablesTable.setData(procList);
				return;
			} else if(srcType.equalsIgnoreCase("file")) {
				List<String> procList = new ArrayList<String>();
				procList.add("getTextFiles");
				sourceTableSelectionModel.clear();
				sourceTablesTable.setData(procList);
				return;
			}
		}
    	
    	String vdbSrcName = Constants.SERVICE_SOURCE_VDB_PREFIX+dataSourceName;
    	String vdbSrcJndi = Constants.JNDI_PREFIX+vdbSrcName;
		queryService.getTablesAndProcedures(vdbSrcJndi, vdbSrcName, new IRpcServiceInvocationHandler<List<QueryTableProcBean>>() {
			@Override
			public void onReturn(List<QueryTableProcBean> tablesAndProcs) {
				List<String> nameList = new ArrayList<String>();
				shortToLongTableNameMap.clear();
				for(QueryTableProcBean tp : tablesAndProcs) {
					String name = tp.getName();
					if(name!=null) {
						if(name.contains(".PUBLIC.")) {
							String shortName = name.substring(name.indexOf(".PUBLIC.")+".PUBLIC.".length());
							shortToLongTableNameMap.put(shortName, name);
							nameList.add(shortName);
						} else if(!name.contains(".INFORMATION_SCHEMA.")) {
							shortToLongTableNameMap.put(name, name);
							nameList.add(name);
						}
					}
				}
				sourceTableSelectionModel.clear();
				sourceTablesTable.setData(nameList);
            	updateStatus();
			}
			@Override
			public void onError(Throwable error) {
				notificationService.sendErrorNotification(i18n.format("joineditor-panel.error-getting-tables-procs"), error); //$NON-NLS-1$
			}
		});

    }
    
    /**
     * Search for QueryColumns based on the current page and filter settings.
     * @param page
     */
    protected void doGetTableColumns(final String source, final String shortTableName, final int tableIndx, int page) {
		String longTableName = shortToLongTableNameMap.get(shortTableName);
    	String filterText = "";
    	String vdbSrcJndi = Constants.JNDI_PREFIX+Constants.SERVICE_SOURCE_VDB_PREFIX+source;
//    	String filterText = (String)stateService.get(ApplicationStateKeys.QUERY_COLUMNS_FILTER_TEXT,"");
//        stateService.put(ApplicationStateKeys.QUERY_COLUMNS_PAGE, currentQueryColumnsPage);

    	queryService.getQueryColumnResultSet(page, 10000, filterText, vdbSrcJndi, longTableName,
    			new IRpcServiceInvocationHandler<QueryColumnResultSetBean>() {
    		@Override
    		public void onReturn(QueryColumnResultSetBean data) {
    			List<CheckableNameTypeRow> colList = new ArrayList<CheckableNameTypeRow>();
    			List<QueryColumnBean> qColumns = data.getQueryColumns();
    			for(QueryColumnBean col : qColumns) {
    				CheckableNameTypeRow cRow = new CheckableNameTypeRow();
    				cRow.setName(col.getName());
    				cRow.setType(col.getType());
    				colList.add(cRow);
    			}
    			ViewEditorManager.getInstance().setColumns(tableIndx, colList);
    			//columnsTable.setData(colList);
            	updateStatus();
    		}
    		@Override
    		public void onError(Throwable error) {
    			notificationService.sendErrorNotification(i18n.format("ssource-editor-panel.error-getting-tablecols"), error); //$NON-NLS-1$
    			// noColumnsMessage.setVisible(true);
    			// columnFetchInProgressMessage.setVisible(false);
    		}
    	});

    }
    
    private void populateSourcesListBox(List<String> dsNames) {
    	// Make sure clear first
    	listboxSources.clear();

    	listboxSources.insertItem(Constants.NO_DATASOURCE_SELECTION, 0);
    	
    	// Repopulate the ListBox with column names
    	int i = 1;
    	for(String dsName: dsNames) {
    		listboxSources.insertItem(dsName, i);
    		i++;
    	}

    	// If only one source, init the combox box to it.
    	if(dsNames.size()==1) {
    		listboxSources.setSelectedIndex(1);
    		doGetTablesAndProcs(dsNames.get(0));
    	} else {
    		// Initialize by setting the selection to the first item.
    		listboxSources.setSelectedIndex(0);
    		sourceTablesTable.clear();
    	}
    }
    
    /**
     * Get the selected DataSource
     * @return
     */
    public String getSelectedSource() {
    	int index = listboxSources.getSelectedIndex();
    	return listboxSources.getValue(index);
    }
    
    /**
     * Update panel status
     */
	private void updateStatus( ) {
    	currentStatus = Constants.OK;
    	
    	// Ensure at least one table is selected
    	List<String> selectedTables = ViewEditorManager.getInstance().getTables();
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