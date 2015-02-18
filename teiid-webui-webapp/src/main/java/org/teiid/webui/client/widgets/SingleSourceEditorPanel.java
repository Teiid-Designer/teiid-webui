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
import org.teiid.webui.client.utils.DdlHelper;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.DataSourcePageRow;
import org.teiid.webui.share.beans.QueryColumnBean;
import org.teiid.webui.share.beans.QueryColumnResultSetBean;
import org.teiid.webui.share.beans.QueryTableProcBean;
import org.teiid.webui.share.services.StringUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

@Dependent
@Templated("./SingleSourceEditorPanel.html")
public class SingleSourceEditorPanel extends Composite {

    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject Event<UiEvent> setDdlEvent;
        
    @Inject
    protected DataSourceNamesTable dsNamesTable;
    
    @Inject
    protected TablesProcNamesTable tablesAndProcsTable;
    
    @Inject
    protected ColumnNamesTable columnsTable;
    
    @Inject @DataField("lbl-ssrceditor-sources-message")
    protected Label sourcesMessageLabel;

    @Inject @DataField("picker-tables")
    protected HorizontalPanel horizPanel;
    
    @Inject @DataField("btn-ssrceditor-createDdl")
    protected Button createDdlButton;
    
    @Inject @DataField("btn-ssrceditor-addToDdl")
    protected Button addToDdlButton;
        
	private Map<String,String> shortToLongTableNameMap = new HashMap<String,String>();
	private String selectedTable = null;
	private String selectedDataSrcName = null;
	private SingleSelectionModel<String> dsSelectionModel;
	private SingleSelectionModel<String> tableSelectionModel;
	
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	tablesAndProcsTable.clear();
    	columnsTable.clear();

    	// Add the three picker tables to horizontal panel
    	horizPanel.setSpacing(0);
    	horizPanel.add(dsNamesTable);
    	horizPanel.add(tablesAndProcsTable);
    	horizPanel.add(columnsTable);
    	
    	doGetQueryableSources();

    	// SelectionModel to handle Source selection 
    	dsSelectionModel = new SingleSelectionModel<String>();
    	dsNamesTable.setSelectionModel(dsSelectionModel); 
    	dsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange( SelectionChangeEvent event) { 
    			tablesAndProcsTable.clear();
    			columnsTable.clear();
    			selectedTable = null;
    			String srcName = dsSelectionModel.getSelectedObject();
    			if(StringUtils.isEmpty(srcName)) {
        			selectedDataSrcName = null;
        			updateStatus();
    			} else {
    				selectedDataSrcName = srcName;
    				doGetTablesAndProcs(srcName);
    			}
    		} });

    	// SelectionModel to handle Table-procedure selection 
    	tableSelectionModel = new SingleSelectionModel<String>();
    	tablesAndProcsTable.setSelectionModel(tableSelectionModel); 
    	tableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange( SelectionChangeEvent event) { 
    			String selected = tableSelectionModel.getSelectedObject();
    			selectedTable = selected;
    			if(StringUtils.isEmpty(selectedTable)) {
    				selectedTable=null;
        			updateStatus();
    			} else {
    				String srcName = dsSelectionModel.getSelectedObject();
    				String longTableName = shortToLongTableNameMap.get(selectedTable);
    				doGetTableColumns(srcName, longTableName, 1);
    			}
    		} });
    	
    	sourcesMessageLabel.setText(i18n.format("ssource-editor-panel.picksource-message"));
    	
    	// Buttons initially disabled
    	createDdlButton.setEnabled(false);
    	addToDdlButton.setEnabled(false);
    	
    	// Tooltips
    	createDdlButton.setTitle(i18n.format("ssource-editor-panel.createDdlButton.tooltip"));
    	addToDdlButton.setTitle(i18n.format("ssource-editor-panel.addToDdlButton.tooltip"));
    	dsNamesTable.setTitle(i18n.format("ssource-editor-panel.dsNamesTable.tooltip"));
    	tablesAndProcsTable.setTitle(i18n.format("ssource-editor-panel.tablesAndProcsTable.tooltip"));
    	columnsTable.setTitle(i18n.format("ssource-editor-panel.columnsTable.tooltip"));
    }
    
    protected void doGetQueryableSources( ) {
    	teiidService.getDataSources("filter", Constants.SERVICE_SOURCE_VDB_PREFIX, new IRpcServiceInvocationHandler<List<DataSourcePageRow>>() {
    		@Override
    		public void onReturn(List<DataSourcePageRow> dsInfos) {
    			// Create list of DataSources that are accessible.  Only the Sources that have 'OK' state
    			// have an associated VDB source and are reachable...
            	List<String> dsList = new ArrayList<String>();
    			for(DataSourcePageRow row : dsInfos) {
    				if(row.getState()==DataSourcePageRow.State.OK) {
            			dsList.add(row.getName());
    				}
    			}
            	dsSelectionModel.clear();
             	dsNamesTable.setData(dsList);
            	updateStatus();
    		}
    		@Override
    		public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ssource-editor-panel.error-getting-svcsources"), error); //$NON-NLS-1$
    		}
    	});
    }
 
    /**
     * Get the Tables and Procs for the supplied data source
     * @param dataSourceName the name of the source
     */
    protected void doGetTablesAndProcs(String dataSourceName) {
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
				tableSelectionModel.clear();
				tablesAndProcsTable.setData(nameList);
            	updateStatus();
			}
			@Override
			public void onError(Throwable error) {
				notificationService.sendErrorNotification(i18n.format("ssource-editor-panel.error-getting-tables-procs"), error); //$NON-NLS-1$
			}
		});

    }
    
    /**
     * Search for QueryColumns based on the current page and filter settings.
     * @param page
     */
    protected void doGetTableColumns(String source, String table, int page) {
    	String filterText = "";
    	String vdbSrcJndi = Constants.JNDI_PREFIX+Constants.SERVICE_SOURCE_VDB_PREFIX+source;
//    	String filterText = (String)stateService.get(ApplicationStateKeys.QUERY_COLUMNS_FILTER_TEXT,"");
//        stateService.put(ApplicationStateKeys.QUERY_COLUMNS_PAGE, currentQueryColumnsPage);

    	queryService.getQueryColumnResultSet(page, 10000, filterText, vdbSrcJndi, table,
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
    			columnsTable.setData(colList);
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
    
    public List<String> getAllSourceNames() {
    	return dsNamesTable.getData();
    }
    
    /**
     * Event handler that fires when the user clicks the create view defn button.
     * @param event
     */
    @EventHandler("btn-ssrceditor-createDdl")
    public void onCreateDdlButtonClick(ClickEvent event) {
    	String theTable = (selectedTable==null) ? "NULL" : selectedTable;
    	
    	List<String> colNames = columnsTable.getSelectedColumnNames();
    	List<String> colTypes = columnsTable.getSelectedColumnTypes();
    	List<String> selectedSourceNames = new ArrayList<String>();
    	selectedSourceNames.add(this.selectedDataSrcName);
    	
    	if(colNames.isEmpty()) {
    		Window.alert("Please select one or more columns");
    		return;
    	}
     	String viewDdl = DdlHelper.getODataViewDdl(Constants.SERVICE_VIEW_NAME, theTable, colNames, colTypes);
     	
		UiEvent uiEvent = new UiEvent(UiEventType.VIEW_DEFN_REPLACE_FROM_SSOURCE_EDITOR);
		uiEvent.setViewDdl(viewDdl);
		uiEvent.setViewSources(selectedSourceNames);
		
		setDdlEvent.fire(uiEvent);
    }
    
    /**
     * Event handler that fires when the user clicks the Add to view defn button.
     * @param event
     */
    @EventHandler("btn-ssrceditor-addToDdl")
    public void onAddToDdlButtonClick(ClickEvent event) {
    	String colString = columnsTable.getSelectedRowString();
    	if(colString.isEmpty()) {
    		Window.alert("Please select one or more columns");
    		return;
    	}
		UiEvent uiEvent = new UiEvent(UiEventType.VIEW_DEFN_ADD_COLS_FROM_SSOURCE_EDITOR);
		uiEvent.setViewDdl(colString);
    	List<String> selectedSrcNames = new ArrayList<String>();
    	selectedSrcNames.add(this.selectedDataSrcName);
		uiEvent.setViewSources(selectedSrcNames);
		
		setDdlEvent.fire(uiEvent);
    }
        
    /**
     * Handles UiEvents from columnNamesTable
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// checkbox change event from column names table
    	if(dEvent.getType() == UiEventType.COLUMN_NAME_TABLE_CHECKBOX_CHANGED) {
    		updateStatus();
    	}
    }
    
    /**
     * Update panel status
     */
	private void updateStatus( ) {
    	String currentStatus = Constants.OK;
    	
    	if(StringUtils.isEmpty(this.selectedDataSrcName)) {
    		currentStatus = i18n.format("ssource-editor-panel.picksource-message");
    		sourcesMessageLabel.setText(currentStatus);
    	}

		// Ensure some columns are selected
    	if(Constants.OK.equals(currentStatus)) {
    		if(StringUtils.isEmpty(this.selectedTable)) {
        		currentStatus = i18n.format("ssource-editor-panel.picktable-message");
        		sourcesMessageLabel.setText(currentStatus);
    		}
    	}
    	
		// Ensure some columns are selected
    	if(Constants.OK.equals(currentStatus)) {
    		List<String> selectedColumns = columnsTable.getSelectedColumnNames();
    		if(selectedColumns.isEmpty()) {
    			currentStatus = i18n.format("ssource-editor-panel.pickcolumns-message");
        		sourcesMessageLabel.setText(currentStatus);
        		createDdlButton.setEnabled(false);
        		addToDdlButton.setEnabled(false);
    		} else {
    			currentStatus = i18n.format("ssource-editor-panel.click-to-create-message");
        		sourcesMessageLabel.setText(currentStatus);
        		createDdlButton.setEnabled(true);
        		addToDdlButton.setEnabled(true);
    		}
    	}
    }
	
}