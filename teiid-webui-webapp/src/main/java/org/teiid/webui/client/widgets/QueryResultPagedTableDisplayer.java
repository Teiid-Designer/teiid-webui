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

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.webui.client.widgets.table.PagedTable;
import org.teiid.webui.share.beans.QueryColumnBean;
import org.teiid.webui.share.beans.QueryResultPageRow;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * Composite for display of Query Results
 */
@Dependent
public class QueryResultPagedTableDisplayer extends Composite {

	private static int NUMBER_ROWS_DEFAULT = 10;
	private static String COLUMN_HEADER_NAME = "Name";
	private static String NO_ERRORS = "No Errors";
	
	private int numberRows = NUMBER_ROWS_DEFAULT;
	
    protected HorizontalPanel mainPanel = new HorizontalPanel();
    private HorizontalPanel tablePanel;
    
    protected Label label = new Label();

    private PagedTable<QueryResultPageRow> table;
    private String errorMessage;

    @Inject
    protected QueryRpcService queryService;

    @Inject Event<UiEvent> refreshCompleteEvent;
    
    public QueryResultPagedTableDisplayer() {
    	mainPanel.setWidth("100%");
        initWidget( mainPanel );
        tablePanel = createDefaultTablePanel();
        tablePanel.setWidth("100%");
        mainPanel.add(tablePanel);
    }
    
    public void setNumberRows(int numberRows) {
		this.numberRows = numberRows;
	}
    
    public void setWidth(String widthStr) {
    	table.setWidth(widthStr);
	}
    
    /**
     * Get error message if refresh has failed
     * @return the error message
     */
    public String getErrorMessage() {
    	return this.errorMessage;
    }

	/**
     * Set the data provider for the table
     * @param dataProvider the data provider
     */
    public void setDataProvider(String dataSource, String sql) {
    	// Reset error message
    	this.errorMessage = NO_ERRORS;
    	
    	// Remove current table panel
    	mainPanel.remove(tablePanel);
    	// updates the tablePanel for supplied source.
    	updateTablePanelForQuery(dataSource,sql);
    }

    /**
     * Create the panel
     * @return the panel widget
     */
    protected HorizontalPanel createDefaultTablePanel() {
    	table = new PagedTable<QueryResultPageRow>(this.numberRows);
        TextColumn<QueryResultPageRow> nameColumn = new TextColumn<QueryResultPageRow>() {
            public String getValue( QueryResultPageRow row ) {
                return row.getColumnData().get(0);
            }
        };
        table.addColumn( nameColumn, COLUMN_HEADER_NAME );

        final Button refreshButton = new Button();
        refreshButton.setIcon( IconType.REFRESH );
        refreshButton.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                table.refresh();
            }
        } );
        table.getToolbar().add( refreshButton );
        
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(table);
        return hPanel;
    }
    
    /**
     * This recreates the panel based on the query.  The number of columns and column labels will vary between queries
     * @return the panel widget
     */
    protected void updateTablePanelForQuery(final String dataSource, final String sql) {
    	final HorizontalPanel hPanel = new HorizontalPanel();
    	final int nRows = this.numberRows;

		queryService.getColumns(dataSource, sql, new IRpcServiceInvocationHandler<List<QueryColumnBean>>() {
			@Override
			public void onReturn(final List<QueryColumnBean> columns) {
		    	table = new PagedTable<QueryResultPageRow>(nRows);
		    	for(int i=0; i<columns.size(); i++) {
		    		final int colIndx = i;
		    		TextColumn<QueryResultPageRow> col = new TextColumn<QueryResultPageRow>() {
		    			public String getValue( QueryResultPageRow row ) {
		    				return row.getColumnData().get(colIndx);
		    			}
		    		};
		    		table.addColumn(col,columns.get(i).getName());
		    	}
	    		
	            final Button refreshButton = new Button();
	            refreshButton.setIcon( IconType.REFRESH );
	            refreshButton.addClickHandler( new ClickHandler() {
	                @Override
	                public void onClick( ClickEvent event ) {
	                    table.refresh();
	                }
	            } );
	            table.getToolbar().add( refreshButton );
	           
	            hPanel.add(table);
	            tablePanel = hPanel;
	            tablePanel.setWidth("100%");
	            
	            mainPanel.add(tablePanel);
	        	table.setDataProvider(createDataProvider(dataSource,sql));
			}
			@Override
			public void onError(Throwable error) {
		    	errorMessage = error.getMessage();
				refreshCompleteEvent.fire(new UiEvent(UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_ERROR));
			}
		});
		
    }
    
    /**
     * Create DataProvider for the DataSources table
     * @return the data provider
     */
    private AsyncDataProvider<QueryResultPageRow> createDataProvider(final String source, final String sql) {
    	AsyncDataProvider<QueryResultPageRow> dataProvider = new AsyncDataProvider<QueryResultPageRow>() {
    		protected void onRangeChanged( HasData<QueryResultPageRow> display ) {
    			final Range range = display.getVisibleRange();
    			PageRequest request = new PageRequest( range.getStart(), range.getLength() );

    			queryService.getQueryResults(request, source, sql, new IRpcServiceInvocationHandler<PageResponse<QueryResultPageRow>>() {
    				@Override
    				public void onReturn(final PageResponse<QueryResultPageRow> response) {
    					updateRowCount( response.getTotalRowSize(), response.isTotalRowSizeExact() );
    					updateRowData( response.getStartRowIndex(), response.getPageRowList() );
    					if(response.getTotalRowSize()==0) {
    						refreshCompleteEvent.fire(new UiEvent(UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_NOROWS));
    					} else {
    						refreshCompleteEvent.fire(new UiEvent(UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_OK));
    					}
    				}
    				@Override
    				public void onError(Throwable error) {
    			    	errorMessage = error.getMessage();
    					refreshCompleteEvent.fire(new UiEvent(UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_ERROR));
    				}
    			});
    		}
    	};

    	return dataProvider;
    }

}
