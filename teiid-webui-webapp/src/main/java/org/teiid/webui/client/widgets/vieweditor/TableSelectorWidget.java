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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

@Dependent
@Templated("./TableSelectorWidget.html")
/**
 * TableSelectorWidget
 * This widget contains all of the controls for selection of a table
 * from the list of available sources/tables
 */
public class TableSelectorWidget extends Composite {

    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject Event<UiEvent> uiEvent;
 
    @Inject @DataField("listbox-sources")
    private ListBox listboxSources;
    @Inject @DataField("tbl-source-tables")
    private TablesProcNamesTable sourceTablesTable;
    @Inject @DataField("btn-move-to-selected")
    private Button moveToSelectedButton;
    
	private Map<String,String> shortToLongTableNameMap = new HashMap<String,String>();
	private Set<String> selectedSourceTables = new HashSet<String>();
	private MultiSelectionModel<String> sourceTableSelectionModel;

	private ViewEditorManager editorManager = ViewEditorManager.getInstance();
	
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	sourceTablesTable.clear();
    	
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
    	sourceTableSelectionModel = new MultiSelectionModel<String>();
    	sourceTablesTable.setSelectionModel(sourceTableSelectionModel); 
    	sourceTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange( SelectionChangeEvent event) { 
    			Set<String> selected = sourceTableSelectionModel.getSelectedSet();
    			selectedSourceTables.clear();
    			selectedSourceTables.addAll(selected);
    			if (selected != null) {
    				moveToSelectedButton.setEnabled(true);
    			} else {
    				moveToSelectedButton.setEnabled(false);
    			}
    		} });
    	

    	moveToSelectedButton.setEnabled(false);
    }
    
    /**
     * Update the selector using the ViewEditorManager state
     */
    public void update() {
		populateSourcesListBox(editorManager.getAvailableSourceNames());
    }
        
    /**
     * Event handler that fires when the user clicks the Move to Selected Tables button.
     * @param event
     */
    @EventHandler("btn-move-to-selected")
    public void onMoveToSelectedButtonClick(ClickEvent event) {
    	int nCurrentTables = editorManager.getTables().size();
    	// Maximum of 2 tables allowed
    	if(nCurrentTables>=2) return;

    	Object[] tableArray = selectedSourceTables.toArray();
    	// If already one table, add first selection at second position
    	if(nCurrentTables==1) {
    		String selectedSourceTable = (String)tableArray[0];
        	// Set the table at the specified index
        	editorManager.setTable(1,selectedSourceTable);
        	// Does fetch of the Table Columns
        	setManagerColumnsForTable(getSelectedSource(),selectedSourceTable,1);
        	// Set source for Table 
        	editorManager.setSourceForTable(1,getSelectedSource());
        // No current tables - will allow up to 2 additionally
    	} else {
    		int nTables = (tableArray.length > 2) ? 2 : tableArray.length;
    		for(int i=0; i<nTables; i++) {
    			String selectedSourceTable = (String)tableArray[i];
    			// Set the table at the specified index
    			editorManager.setTable(i,selectedSourceTable);
    			// Does fetch of the Table Columns
    			setManagerColumnsForTable(getSelectedSource(),selectedSourceTable,i);
    			// Set source for Table 
    			editorManager.setSourceForTable(i,getSelectedSource());
    		}
    	}

    	// Get Updated tables
    	List<TableListItem> newTables = editorManager.getTableItems();

    	sourceTableSelectionModel.clear();
    	
    	fireSetTablesEvent(newTables);
    }
    
    /**
     * Fire status event for a dataSource
     * @param sourceName the source name
     * @param sourceTableName the table name
     */
    private void fireSetTablesEvent(List<TableListItem> tables) {
		UiEvent sEvent = new UiEvent(UiEventType.SET_TABLES);
		sEvent.setNewTables(tables);
		uiEvent.fire(sEvent);
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
     * Refreshes the sources listBox with the available sources for constructing the data services.
     * The available sources are obtained from the ViewEditorManager
     */
    public void refreshAvailableSources( ) {
		populateSourcesListBox(editorManager.getAvailableSourceNames());
	}
    
    /**
     * Get the Tables and Procs for the supplied data source
     * @param dataSourceName the name of the source
     */
    protected void doGetTablesAndProcs(String dataSourceName) {
    	// Certain types, no need to do a server fetch
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
    			editorManager.setColumns(tableIndx, colList);
    			//columnsTable.setData(colList);
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

    	resetSourceSelection();
    }
    
    /**
     * Reset the listbox datasource selection
     */
    public void resetSourceSelection() {
    	int nSources = listboxSources.getItemCount();
    	// If only one source, init the combox box to it.
    	if(nSources==2) {
    		listboxSources.setSelectedIndex(1);
    		doGetTablesAndProcs(getSelectedSource());
    	} else {
    		// Initialize by setting the selection to the first item.
    		listboxSources.setSelectedIndex(0);
    		sourceTablesTable.clear();
    	}
    	moveToSelectedButton.setEnabled(false);
    }

    /**
     * Get the selected DataSource
     * @return
     */
    public String getSelectedSource() {
    	int index = listboxSources.getSelectedIndex();
    	return listboxSources.getValue(index);
    }
               
}