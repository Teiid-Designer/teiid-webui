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

import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.NotificationService;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.webui.client.widgets.CheckableNameTypeRow;
import org.teiid.webui.client.widgets.table.TableNamesTable;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.TranslatorHelper;
import org.teiid.webui.share.beans.QueryColumnBean;
import org.teiid.webui.share.beans.QueryColumnResultSetBean;
import org.teiid.webui.share.beans.QueryTableProcBean;
import org.teiid.webui.share.services.StringUtils;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;

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
    protected QueryRpcService queryService;
    
    @Inject Event<UiEvent> uiEvent;
 
    @Inject @DataField("listbox-sources")
    private ListBox listboxSources;
    @Inject @DataField("tbl-source-tables")
    private TableNamesTable sourceTablesTable;
    
	private Map<String,String> shortToLongTableNameMap = new HashMap<String,String>();

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
        
        sourceTablesTable.setOwner(this.getClass().getName());
        sourceTablesTable.setShowHeader(false);
    }
    
    /**
     * Update the selector using the ViewEditorManager state
     */
    public void update() {
		populateSourcesListBox(editorManager.getAvailableSourceNames());
    }
        
    /**
     * Handles UiEvents
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// Table checkbox has been checked
    	if(dEvent.getType() == UiEventType.TABLE_NAME_TABLE_CHECKBOX_CHECKED) {
    		int nCurrentTables = editorManager.getTables().size();
    		// Maximum of 2 tables allowed
    		if(nCurrentTables>=2) return;

    		String selectedSourceTable = dEvent.getTableName();
    		String selectedSource = getSelectedSource();
    		
    		// If already one table, add first selection at second position
    		int indx = (nCurrentTables==1) ? 1 : 0;
    		
    		// Set the table at the specified index
    		editorManager.setTable(indx,selectedSourceTable);
    		// Does fetch of the Table Columns
    		setManagerColumnsForTable(selectedSource,selectedSourceTable,indx);
    		// Set source for Table 
    		editorManager.setSourceForTable(indx,selectedSource);

    		// Get Updated tables
    		List<TableListItem> newTables = editorManager.getTableItems();

    		boolean disableUnchecked = newTables.size() >= 2;
    		sourceTablesTable.setDisableUncheckedRows(disableUnchecked);
    		
    		fireSetTablesEvent(newTables);
    	// Table checkbox has been unchecked
    	} else if(dEvent.getType() == UiEventType.TABLE_NAME_TABLE_CHECKBOX_UNCHECKED) {
    		// Get the unchecked table
    		String uncheckedTableName = dEvent.getTableName();
    		String uncheckedSrcName = getSelectedSource();
    		
    		// Remove the table from the editor manager selections
    		List<TableListItem> tItems = editorManager.getTableItems();
    		int indx = -1;
    		for(int i=0; i<tItems.size(); i++) {
    			String tableName = tItems.get(i).getTableName();
    			String srcName = tItems.get(i).getSourceName();
    			if (StringUtils.equals(uncheckedTableName, tableName) && StringUtils.equals(uncheckedSrcName,srcName)) {
    				indx = i;
    				break;
    			}
    		}
    		// Table index found, remove it from manager and update
    		if(indx!=-1) {
    			editorManager.removeTable(indx);
    			
        		// Get Updated tables
        		List<TableListItem> newTables = editorManager.getTableItems();

        		// refresh disable checkbox status
        		boolean disableUnchecked = newTables.size() >= 2;
        		sourceTablesTable.setDisableUncheckedRows(disableUnchecked);
        		
        		fireSetTablesEvent(newTables);
    		}
    	// Selected Table was removed
    	} else if(dEvent.getType() == UiEventType.REMOVE_TABLE) {
    		// Set unchecked disablement based on number of tables
    		List<TableListItem> newTables = editorManager.getTableItems();
    		boolean disableUnchecked = newTables.size() >= 2;
    		sourceTablesTable.setDisableUncheckedRows(disableUnchecked);
    		
    		TableListItem tableItem = dEvent.getRemoveTable();
    		// If this source is currently selected, uncheck the table
    		if(StringUtils.equals(getSelectedSource(), tableItem.getSourceName())) {
        		// refresh disable checkbox status
        		sourceTablesTable.setCheckedState(tableItem.getTableName(),false);
    		}
    	}
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
    protected void doGetTablesAndProcs(final String dataSourceName) {
    	// Current table selections
    	final List<TableListItem> tableItems = editorManager.getTableItems();
		final boolean disableUnchecked = tableItems.size() >= 2;
    	
    	// Certain types, no need to do a server fetch
		String srcType = editorManager.getSourceType(dataSourceName);
		String translator = editorManager.getSourceTranslator(dataSourceName);
		if(srcType!=null) {
			if(srcType.equals(TranslatorHelper.TEIID_WEBSERVICE_DRIVER) && !translator.equalsIgnoreCase(TranslatorHelper.ODATA)) {
				List<CheckableNameTypeRow> procList = new ArrayList<CheckableNameTypeRow>();
				CheckableNameTypeRow row = new CheckableNameTypeRow();
				row.setName(TranslatorHelper.TEIID_WEBSERVICE_PROC);
				setRowCheckedState(row,dataSourceName,tableItems);
				procList.add(row);
	    		sourceTablesTable.setDisableUncheckedRows(disableUnchecked);
				sourceTablesTable.setData(procList);
				return;
			} else if(srcType.equalsIgnoreCase(TranslatorHelper.TEIID_FILE_DRIVER)) {
				List<CheckableNameTypeRow> procList = new ArrayList<CheckableNameTypeRow>();
				CheckableNameTypeRow row = new CheckableNameTypeRow();
				row.setName(TranslatorHelper.TEIID_FILE_PROC);
				setRowCheckedState(row,dataSourceName,tableItems);
				procList.add(row);
	    		sourceTablesTable.setDisableUncheckedRows(disableUnchecked);
				sourceTablesTable.setData(procList);
				return;
			}
		}
    	
    	String vdbSrcName = Constants.SERVICE_SOURCE_VDB_PREFIX+dataSourceName;
    	String vdbSrcJndi = Constants.JNDI_PREFIX+vdbSrcName;
		queryService.getTablesAndProcedures(vdbSrcJndi, vdbSrcName, new IRpcServiceInvocationHandler<List<QueryTableProcBean>>() {
			@Override
			public void onReturn(List<QueryTableProcBean> tablesAndProcs) {
				List<CheckableNameTypeRow> nameList = new ArrayList<CheckableNameTypeRow>();
				shortToLongTableNameMap.clear();
				for(QueryTableProcBean tp : tablesAndProcs) {
					String name = tp.getName();
					if(name!=null) {
						if(name.contains(".PUBLIC.")) {
							String shortName = name.substring(name.indexOf(".PUBLIC.")+".PUBLIC.".length());
							shortToLongTableNameMap.put(shortName, name);
							CheckableNameTypeRow row = new CheckableNameTypeRow();
							row.setName(shortName);
							setRowCheckedState(row,dataSourceName,tableItems);
							nameList.add(row);
						} else if(!name.contains(".INFORMATION_SCHEMA.")) {
							shortToLongTableNameMap.put(name, name);
							CheckableNameTypeRow row = new CheckableNameTypeRow();
							row.setName(name);
							setRowCheckedState(row,dataSourceName,tableItems);
							nameList.add(row);
						}
					}
				}
				//sourceTableSelectionModel.clear();
	    		sourceTablesTable.setDisableUncheckedRows(disableUnchecked);
				sourceTablesTable.setData(nameList);
			}
			@Override
			public void onError(Throwable error) {
				notificationService.sendErrorNotification(i18n.format("table-selector-widget.error-getting-tables-procs"), error); //$NON-NLS-1$
			}
		});

    }
    
    /**
     * Set the row checked state.
     * @param row the table row
     * @param dsName the current datasource
     * @param selectedTables the selected TableListItems
     */
    private void setRowCheckedState(CheckableNameTypeRow row, String dsName, List<TableListItem> selectedTables) {
    	boolean isChecked = false;
    	for(TableListItem tItem : selectedTables) {
    		if(!tItem.isPlaceHolder()) {
    			String selectedSourceName = tItem.getSourceName();
    			String selectedTableName = tItem.getTableName();
    			if(StringUtils.equals(selectedSourceName, dsName) && StringUtils.equals(selectedTableName, row.getName())) {
    				isChecked = true;
    				break;
    			}
    		}
    	}
    	row.setChecked(isChecked);
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
    		}
    		@Override
    		public void onError(Throwable error) {
    			notificationService.sendErrorNotification(i18n.format("table-selector-widget.error-getting-tablecols"), error); //$NON-NLS-1$
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