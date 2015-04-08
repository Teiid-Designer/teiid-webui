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
package org.teiid.webui.backend.server.services;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;

import org.jboss.errai.bus.server.annotations.Service;
import org.teiid.webui.backend.server.services.util.FilterUtil;
import org.teiid.webui.backend.server.services.util.JdbcSourceHelper;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.QueryColumnBean;
import org.teiid.webui.share.beans.QueryColumnResultSetBean;
import org.teiid.webui.share.beans.QueryResultPageRow;
import org.teiid.webui.share.beans.QueryResultRowBean;
import org.teiid.webui.share.beans.QueryResultSetBean;
import org.teiid.webui.share.beans.QueryTableProcBean;
import org.teiid.webui.share.exceptions.DataVirtUiException;
import org.teiid.webui.share.services.IQueryService;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

/**
 * Concrete implementation of the DataSource service.
 *
 * @author mdrillin@redhat.com
 */
@Service
public class QueryService implements IQueryService {

	private static final String UPDATE = "UPDATE";
	private static final String INSERT = "INSERT";
	private static final String DELETE = "DELETE";
	private static final String TABLE_NAME = "TABLE_NAME";
	private static final String TABLE_SCHEM = "TABLE_SCHEM";
	private static final String PROCEDURE_NAME = "PROCEDURE_NAME";
	private static final String PROCEDURE_SCHEM = "PROCEDURE_SCHEM";
	private static final String SYS = "SYS";
	private static final String SYSADMIN = "SYSADMIN";
	private static final String COLUMN_NAME = "COLUMN_NAME";
	private static final String COLUMN_TYPE = "COLUMN_TYPE";
	private static final String TYPE_NAME = "TYPE_NAME";

    /**
     * Constructor.
     */
    public QueryService() {
    }
    
    @Override
    public QueryColumnResultSetBean getQueryColumnResultSet(int page, int pageSize, String filterText, String dataSource, String fullTableName) throws DataVirtUiException {
    	// Get DataSources Map
    	Map<String,DataSource> mDatasources = JdbcSourceHelper.getInstance().getDataSourceMap();

    	// Get a connection for the supplied data source name
    	Connection connection;
		try {
			connection = getConnection(dataSource, mDatasources);
		} catch (SQLException e) {
			throw new DataVirtUiException(e.getMessage());
		}

    	QueryColumnResultSetBean resultSetBean = new QueryColumnResultSetBean();
        
        List<QueryColumnBean> allColumnBeans = getTeiidColumnsForTable(connection, fullTableName);
        allColumnBeans.addAll(getColumnsForProcedure(connection,fullTableName));

        // Filter based on supplied filter text
        filterColumns(allColumnBeans, filterText);
        
        int totalCols = allColumnBeans.size();
        
        // Start and End Index for this page
        int page_startIndex = (page - 1) * pageSize;
        int page_endIndex = page_startIndex + (pageSize-1);
        // If page endIndex greater than total rows, reset to end
        if(page_endIndex > (totalCols-1)) {
        	page_endIndex = totalCols-1;
        }
        
        List<QueryColumnBean> rows = new ArrayList<QueryColumnBean>();
        for(int i=page_startIndex; i<=page_endIndex; i++) {
        	rows.add(allColumnBeans.get(i));
        }
        
        resultSetBean.setQueryColumns(rows);
        resultSetBean.setItemsPerPage(pageSize);
        resultSetBean.setStartIndex(page_startIndex);
        resultSetBean.setTotalResults(totalCols);

		// Close Connection
		try {
			closeConnection(connection);
		} catch (SQLException e) {
			throw new DataVirtUiException(e.getMessage());
		}

        return resultSetBean;
    }

    private void filterColumns(List<QueryColumnBean> allColumnBeans, String filterText) {
    	Iterator<QueryColumnBean> beanIter = allColumnBeans.iterator();
    	while(beanIter.hasNext()) {
    		QueryColumnBean bean = beanIter.next();
    		String colName = bean.getName();
    		if ( !FilterUtil.matchFilter(colName, filterText) ) {
    			beanIter.remove();
    		}    		
    	}
    }
    
    /*
     * Get List of all available Datasource Names. The 'Datasource Names' are the jndi names of the
     * queryable jdbc sources on the server.
     * @param teiidOnly 'true' if only Teiid sources are to be returned, 'false' otherwise.
     * @return the list of datasource names
     */
    public List<String> getDataSourceNames(boolean teiidOnly) throws DataVirtUiException {
    	// Get DataSources Map
    	JdbcSourceHelper jdbcHelper = JdbcSourceHelper.getInstance();
    	Map<String,DataSource> mDatasources = jdbcHelper.getDataSourceMap();

    	// Get DataSource names
    	List<String> resultList = new ArrayList<String>();

    	Set<String> dsNames = mDatasources.keySet();
    	Iterator<String> nameIter = dsNames.iterator();
    	while(nameIter.hasNext()) {
    		String dsName = nameIter.next();
    		if(dsName!=null && !dsName.startsWith(Constants.JNDI_PREFIX+Constants.PREVIEW_VDB_PREFIX)) {
    			DataSource ds = mDatasources.get(dsName);
    			if(!teiidOnly) {
    				resultList.add(dsName);
    			} else if(jdbcHelper.isTeiidSource(ds)) {
    				resultList.add(dsName);
    			}
    		}
    	}
    	
    	return resultList;
    }
    
    /*
     * Get List of tables and procedures for the supplied Datasource Name. 
     * @param dataSource the name of the data source.
     * @return the list of table and procedure names
     */
    public List<QueryTableProcBean> getTablesAndProcedures(String dataSourceJndiName, String dsName) throws DataVirtUiException {
    	// Get DataSources Mape
    	Map<String,DataSource> mDatasources = JdbcSourceHelper.getInstance().getDataSourceMap();

    	// Get a connection for the supplied data source name
    	Connection connection;
		try {
			connection = getConnection(dataSourceJndiName, mDatasources);
		} catch (SQLException e) {
			throw new DataVirtUiException(e.getMessage());
		}

    	// Result List of Tables and Procedures
    	List<QueryTableProcBean> tablesAndProcs = new ArrayList<QueryTableProcBean>();

    	// Get Tables and Procedures for the Datasource
    	if( connection!=null) {
    		int indx = dsName.indexOf(Constants.SERVICE_SOURCE_VDB_PREFIX);
    		int startIndx = indx + Constants.SERVICE_SOURCE_VDB_PREFIX.length();
    		String schemaName = dsName.substring(startIndx);
    		tablesAndProcs.addAll(getTeiidTables(connection,dsName,schemaName));
    		tablesAndProcs.addAll(getTeiidProcedures(connection));
    	}

		// Close Connection
		try {
			closeConnection(connection);
		} catch (SQLException e) {
			throw new DataVirtUiException(e.getMessage());
		}

    	return tablesAndProcs;
    }

    /*
     * Test the connection to the supplied DataSource.  Return status message of connection attempt.
     * @param dataSourceJndiName the jndi name of the data source.
     * @param dsName the name of the data source
     * @return the status message - 'OK' if connection is ok.
     */
    public String testConnection(String dataSourceJndiName,String dsName) {
    	String statusMessage = null;
    	
    	// Get DataSources Map
    	Map<String, DataSource> mDatasources;
		try {
			mDatasources = JdbcSourceHelper.getInstance().getDataSourceMap();
		} catch (DataVirtUiException e1) {
			statusMessage = Constants.TEST_CONNECTION_ERROR_LOADING_SOURCES;
			return statusMessage;
		}

    	// Get a connection for the supplied data source name
    	Connection connection;
		try {
			connection = getConnection(dataSourceJndiName, mDatasources);
		} catch (SQLException e) {
			statusMessage = Constants.TEST_CONNECTION_FAILED;
			return statusMessage;
		}
		if(connection==null) return Constants.TEST_CONNECTION_FAILED;
		
    	// Attempt to get tables - to test the connection
		String schemaName = dsName.substring(dsName.indexOf(Constants.SERVICE_SOURCE_VDB_PREFIX)+Constants.SERVICE_SOURCE_VDB_PREFIX.length());
		try {
			getTeiidTables(connection,dsName,schemaName);
		} catch (DataVirtUiException e1) {
			statusMessage = Constants.TEST_CONNECTION_FAILED;
			return statusMessage;
		} finally {
			// Close Connection
			try {
				closeConnection(connection);
			} catch (SQLException e) {
			}
		}
		
		return Constants.OK;
    }
    
    /*
     * (non-Javadoc)
     * @see org.teiid.tools.webquery.client.TeiidService#executeSql(java.lang.String, java.lang.String)
     */
    public QueryResultSetBean executeSql(int page, String dataSource, String sql) throws DataVirtUiException {
    	int pageSize = Constants.QUERY_RESULTS_TABLE_PAGE_SIZE;
    	
    	// Get DataSources Mape
    	Map<String,DataSource> mDatasources = JdbcSourceHelper.getInstance().getDataSourceMap();

    	// Get a connection for the supplied data source name
    	Connection connection;
		try {
			connection = getConnection(dataSource, mDatasources);
		} catch (SQLException e) {
			throw new DataVirtUiException(e.getMessage());
		}

    	// ResultSet Bean
    	QueryResultSetBean resultSetBean = new QueryResultSetBean();
    	
    	// Bean to store a single row
    	List<QueryResultRowBean> rowList = new ArrayList<QueryResultRowBean>();

    	try {
    		if(connection!=null && sql!=null && sql.trim().length()>0) {
    			sql = sql.trim();
    			Statement stmt = connection.createStatement();
    			String sqlUpperCase = sql.toUpperCase();

    			// INSERT / UPDATE / DELETE - execute as an Update
    			if(sqlUpperCase.startsWith(INSERT) || sqlUpperCase.startsWith(UPDATE) || sqlUpperCase.startsWith(DELETE)) {
    				int rowCount = stmt.executeUpdate(sql);
    				
    				// Row contains single item - message of number rows updated
    				List<String> row = new ArrayList<String>();
    				row.add(rowCount+" Rows Updated");
    				
    				// Create Row Bean and add to the row list
    				QueryResultRowBean rowBean = new QueryResultRowBean();
    				rowBean.setColumnResults(row);
    				rowList.add(rowBean);
   				// SELECT
    			} else {
    				ResultSet resultSet = stmt.executeQuery(sql);

    				// List of the column names
    				int columnCount = resultSet.getMetaData().getColumnCount();
    				List<String> columnNames = new ArrayList<String>(columnCount);
    				for (int i=1 ; i<=columnCount ; ++i) {
    					columnNames.add(resultSet.getMetaData().getColumnName(i));
    				}
    				resultSetBean.setResultColumnNames(columnNames);

    				// Add all result rows
    				boolean gotTypes = false;
    				if (!resultSet.isAfterLast()) {
    					while (resultSet.next()) {
    						// RowBean holds values
    						QueryResultRowBean rowBean = new QueryResultRowBean();
    						
    						// Get types once
    						if(!gotTypes) {
        						// Get types for each row
        	    				List<String> types = new ArrayList<String>(columnCount);
        						for (int i=1 ; i<=columnCount ; ++i) {
        							types.add(getColType(resultSet,i));
        						}
        						resultSetBean.setResultColumnTypes(types);
    							gotTypes = true;
    						}
    						
    						// Get values for each row
    	    				List<String> row = new ArrayList<String>(columnCount);
    						for (int i=1 ; i<=columnCount ; ++i) {
    							row.add(getColValue(resultSet,i));
    						}
    						
    	    				// Create Row Bean and add to the row list
    	    				rowBean.setColumnResults(row);
    	    				rowList.add(rowBean);
    					}
    				}
    				resultSet.close();
    			}
    			stmt.close();
    		}
    	} catch (Exception e) {
    		try {
				closeConnection(connection);
			} catch (SQLException e1) {
			}
    		throw new DataVirtUiException(e.getMessage());
    	} finally {
    		try {
				closeConnection(connection);
			} catch (SQLException e1) {
			}
    	}

    	int totalRows = rowList.size();
        
        // Start and End Index for this page
        int page_startIndex = (page - 1) * pageSize;
        int page_endIndex = page_startIndex + (pageSize-1);
        // If page endIndex greater than total rows, reset to end
        if(page_endIndex > (totalRows-1)) {
        	page_endIndex = totalRows-1;
        }
        
        // Keep only the desired rows
        List<QueryResultRowBean> rows = new ArrayList<QueryResultRowBean>();
        for(int i=page_startIndex; i<=page_endIndex; i++) {
        	rows.add(rowList.get(i));
        }
        
        resultSetBean.setResultRows(rows);
        resultSetBean.setItemsPerPage(pageSize);
        resultSetBean.setStartIndex(page_startIndex);
        resultSetBean.setTotalResults(totalRows);

        return resultSetBean;
    }
    
    public List<QueryColumnBean> getColumns( String dataSource, String selectSql ) throws DataVirtUiException {

    	// Get DataSources Map
    	Map<String,DataSource> mDatasources = JdbcSourceHelper.getInstance().getDataSourceMap();

    	// Get a connection for the supplied data source name
    	Connection connection;
		try {
			connection = getConnection(dataSource, mDatasources);
		} catch (SQLException e) {
			throw new DataVirtUiException(e.getMessage());
		}

		List<QueryColumnBean> columns = new ArrayList<QueryColumnBean>();
		
    	try {
    		if(connection!=null && selectSql!=null && selectSql.trim().length()>0) {
    			selectSql = selectSql.trim();
    			Statement stmt = connection.createStatement();
    			String sqlUpperCase = selectSql.toUpperCase();

    			ResultSet resultSet = stmt.executeQuery(sqlUpperCase);

    			// List of the column names
    			int columnCount = resultSet.getMetaData().getColumnCount();
    			for (int i=1 ; i<=columnCount ; ++i) {
    				QueryColumnBean qColumn = new QueryColumnBean();
    				String colName = resultSet.getMetaData().getColumnName(i);
    				String colType = resultSet.getMetaData().getColumnTypeName(i);
    				qColumn.setName(colName);
    				qColumn.setType(colType);
    				columns.add(qColumn);
    			}
    			resultSet.close();
    			stmt.close();
    		}
    	} catch (Exception e) {
    		try {
				closeConnection(connection);
			} catch (SQLException e1) {
			}
    		throw new DataVirtUiException(e.getMessage());
    	} finally {
    		try {
				closeConnection(connection);
			} catch (SQLException e1) {
			}
    	}
    	return columns;
    }
    
    public PageResponse<QueryResultPageRow> getQueryResults( final PageRequest pageRequest, String dataSource, String sql ) throws DataVirtUiException {

    	// Get DataSources Mape
    	Map<String,DataSource> mDatasources = JdbcSourceHelper.getInstance().getDataSourceMap();

    	// Get a connection for the supplied data source name
    	Connection connection;
		try {
			connection = getConnection(dataSource, mDatasources);
		} catch (SQLException e) {
			throw new DataVirtUiException(e.getMessage());
		}

    	// ResultSet Bean
    	QueryResultSetBean resultSetBean = new QueryResultSetBean();
    	
    	// Bean to store a single row
    	List<QueryResultRowBean> rowList = new ArrayList<QueryResultRowBean>();

    	try {
    		if(connection!=null && sql!=null && sql.trim().length()>0) {
    			sql = sql.trim();
    			Statement stmt = connection.createStatement();
    			String sqlUpperCase = sql.toUpperCase();

    			// INSERT / UPDATE / DELETE - execute as an Update
    			if(sqlUpperCase.startsWith(INSERT) || sqlUpperCase.startsWith(UPDATE) || sqlUpperCase.startsWith(DELETE)) {
    				int rowCount = stmt.executeUpdate(sql);
    				
    				// Row contains single item - message of number rows updated
    				List<String> row = new ArrayList<String>();
    				row.add(rowCount+" Rows Updated");
    				
    				// Create Row Bean and add to the row list
    				QueryResultRowBean rowBean = new QueryResultRowBean();
    				rowBean.setColumnResults(row);
    				rowList.add(rowBean);
   				// SELECT
    			} else {
    				ResultSet resultSet = stmt.executeQuery(sql);

    				// List of the column names
    				int columnCount = resultSet.getMetaData().getColumnCount();
    				List<String> columnNames = new ArrayList<String>(columnCount);
    				for (int i=1 ; i<=columnCount ; ++i) {
    					columnNames.add(resultSet.getMetaData().getColumnName(i));
    				}
    				resultSetBean.setResultColumnNames(columnNames);

    				// Add all result rows
    				boolean gotTypes = false;
    				if (!resultSet.isAfterLast()) {
    					while (resultSet.next()) {
    						// RowBean holds values
    						QueryResultRowBean rowBean = new QueryResultRowBean();
    						
    						// Get types once
    						if(!gotTypes) {
        						// Get types for each row
        	    				List<String> types = new ArrayList<String>(columnCount);
        						for (int i=1 ; i<=columnCount ; ++i) {
        							types.add(getColType(resultSet,i));
        						}
        						resultSetBean.setResultColumnTypes(types);
    							gotTypes = true;
    						}
    						
    						// Get values for each row
    	    				List<String> row = new ArrayList<String>(columnCount);
    						for (int i=1 ; i<=columnCount ; ++i) {
    							row.add(getColValue(resultSet,i));
    						}
    						
    	    				// Create Row Bean and add to the row list
    	    				rowBean.setColumnResults(row);
    	    				rowList.add(rowBean);
    					}
    				}
    				resultSet.close();
    			}
    			stmt.close();
    		}
    	} catch (Exception e) {
    		try {
				closeConnection(connection);
			} catch (SQLException e1) {
			}
    		throw new DataVirtUiException(e.getMessage());
    	} finally {
    		try {
				closeConnection(connection);
			} catch (SQLException e1) {
			}
    	}
    	resultSetBean.setResultRows(rowList);
    	
    	final PageResponse<QueryResultPageRow> response = new PageResponse<QueryResultPageRow>();
    	final List<QueryResultPageRow> queryResultPageRowList = new ArrayList<QueryResultPageRow>();

    	int i = 0;
    	for ( QueryResultRowBean resultRow : resultSetBean.getResultRows() ) {
    		if ( i >= pageRequest.getStartRowIndex() + pageRequest.getPageSize() ) {
    			break;
    		}
    		if ( i >= pageRequest.getStartRowIndex() ) {
    			QueryResultPageRow queryResultPageRow = new QueryResultPageRow();
    			List<String> colResults = resultRow.getColumnResults();
    			queryResultPageRow.setColumnData( colResults );
    			//queryResultPageRow.setType(typeList.get(i));
    			queryResultPageRowList.add( queryResultPageRow );
    		}
    		i++;
    	}

    	response.setPageRowList( queryResultPageRowList );
    	response.setStartRowIndex( pageRequest.getStartRowIndex() );
    	response.setTotalRowSize( resultSetBean.getResultRows().size() );
    	response.setTotalRowSizeExact( true );
    	//response.setLastPage(true);

    	return response;
    }
    
    /*
     * Create a DataItem to pass back to client for each result
     * @param resultSet the SQL ResultSet
     * @param index the ResultSet index for the object
     * @return the DataItem result
     */
    private String getColValue(ResultSet resultSet, int index) throws SQLException {
    	String colString = null;
    	//String type = "string";
    	Object obj = resultSet.getObject(index);

    	if(obj instanceof javax.sql.rowset.serial.SerialBlob) {
    		byte[] bytes = ((SerialBlob)obj).getBytes(1, 500);
    		colString = Arrays.toString(bytes);
    	} if(obj instanceof Clob) {
    		colString = "[no transform]";
    	} if(obj instanceof Blob) {
    		colString = "[no transform]";
    	} else {
    		String value = null;
    		try {
    			value = resultSet.getString(index);
			} catch (Exception e) {
				value = "[no transform]";
			}
    		colString = value;
    	}
    	return colString;
    }
    
    /*
     * Create a DataItem to pass back to client for each result
     * @param resultSet the SQL ResultSet
     * @param index the ResultSet index for the object
     * @return the DataItem result
     */
    private String getColType(ResultSet resultSet, int index) throws SQLException {
    	String type = "string";
    	Object obj = resultSet.getObject(index);

    	String className = null;
    	if(obj!=null) {
    		className = obj.getClass().getName();
    		if(className.equals("org.teiid.core.types.SQLXMLImpl")) {
    			type = "xml";
    		}
    	}
    	return type;
    }

    /*
     * Get List of Tables using the supplied connection
     * @param connection the JDBC connection
     * @return the list of table names
     */
//    private List<QueryTableProcBean> getTables(Connection connection) {
//    	// Get the list of Tables
//    	List<String> tableNameList = new ArrayList<String>();
//    	List<String> tableSchemaList = new ArrayList<String>();
//    	if(connection!=null) {
//    		try {
//    			ResultSet resultSet = connection.getMetaData().getTables(null, null, "%", new String[]{"DOCUMENT", "TABLE", "VIEW"});
//    			int columnCount = resultSet.getMetaData().getColumnCount();
//    			while (resultSet.next()) {
//    				String tableName = null;
//    				String tableSchema = null;
//    				for (int i=1 ; i<=columnCount ; ++i) {
//    					String colName = resultSet.getMetaData().getColumnName(i);
//    					String value = resultSet.getString(i);
//    					if (colName.equalsIgnoreCase(TABLE_NAME)) {
//    						tableName = value;
//    					} else if(colName.equalsIgnoreCase(TABLE_SCHEM)) {
//    						tableSchema = value;
//    					}
//    				}
//    				tableNameList.add(tableName);
//    				tableSchemaList.add(tableSchema);
//    			}
//    			resultSet.close();
//    		} catch (Exception e) {
//    		}
//    	}
//
//    	List<QueryTableProcBean> resultList = new ArrayList<QueryTableProcBean>(tableNameList.size());
//    	
//    	// Build full names if schemaName is present
//    	for(int i=0; i<tableNameList.size(); i++) {
//    		QueryTableProcBean tableProcBean = new QueryTableProcBean();
//    		
//    		String schemaName = tableSchemaList.get(i);
//    		if(schemaName!=null && schemaName.length()>0) {
//    			tableProcBean.setName(schemaName+"."+tableNameList.get(i));
//    			tableProcBean.setType(QueryTableProcBean.TABLE);
//    		} else {
//    			tableProcBean.setName(tableNameList.get(i));
//    			tableProcBean.setType(QueryTableProcBean.TABLE);
//    		}
//    		if(tableProcBean.getName()!=null) {
//    			resultList.add(tableProcBean);
//    		}
//    	}
//    	return resultList;
//    }

    /*
     * Get List of Tables using the supplied connection
     * @param connection the JDBC connection
     * @return the list of table names
     */
    private List<QueryTableProcBean> getTeiidTables(Connection connection, String catalogName, String schemaName) throws DataVirtUiException {
    	// Get the list of Tables
    	List<String> tableNameList = new ArrayList<String>();
    	List<String> tableSchemaList = new ArrayList<String>();
    	if(connection!=null) {
    		try {
    			ResultSet resultSet = connection.getMetaData().getTables(catalogName, schemaName, "%", new String[]{"DOCUMENT", "TABLE", "VIEW"});
    			int columnCount = resultSet.getMetaData().getColumnCount();
    			while (resultSet.next()) {
    				String tableName = null;
    				String tableSchema = null;
    				for (int i=1 ; i<=columnCount ; ++i) {
    					String colName = resultSet.getMetaData().getColumnName(i);
    					String value = resultSet.getString(i);
    					if (colName.equalsIgnoreCase(TABLE_NAME)) {
    						tableName = value;
    					} else if(colName.equalsIgnoreCase(TABLE_SCHEM)) {
    						tableSchema = value;
    					}
    				}
    				tableNameList.add(tableName);
    				tableSchemaList.add(tableSchema);
    			}
    			resultSet.close();
    		} catch (Exception e) {
    			throw new DataVirtUiException(e);
    		}
    	}

    	List<QueryTableProcBean> resultList = new ArrayList<QueryTableProcBean>(tableNameList.size());
    	
    	// Build full names if schemaName is present
    	for(int i=0; i<tableNameList.size(); i++) {
    		QueryTableProcBean tableProcBean = new QueryTableProcBean();
    		
    		String schName = tableSchemaList.get(i);
    		if(schName!=null && schName.length()>0) {
    			tableProcBean.setName(tableNameList.get(i));
    			tableProcBean.setType(QueryTableProcBean.TABLE);
    		} else {
    			tableProcBean.setName(tableNameList.get(i));
    			tableProcBean.setType(QueryTableProcBean.TABLE);
    		}
    		if(tableProcBean.getName()!=null) {
    			resultList.add(tableProcBean);
    		}
    	}
    	return resultList;
    }
    
    /*
     * Get List of Procedures using the supplied connection
     * @param connection the JDBC connection
     * @return the list of procedure names
     */
//    private List<QueryTableProcBean> getProcedures(Connection connection) {
//    	// Get the list of Procedures
//    	List<String> procNameList = new ArrayList<String>();
//    	List<String> procSchemaList = new ArrayList<String>();
//    	if(connection!=null) {
//    		try {
//    			ResultSet resultSet = connection.getMetaData().getProcedures(null, null, "%");
//    			int columnCount = resultSet.getMetaData().getColumnCount();
//    			while (resultSet.next()) {
//    				String procName = null;
//    				String procSchema = null;
//    				for (int i=1 ; i<=columnCount ; ++i) {
//    					String colName = resultSet.getMetaData().getColumnName(i);
//    					String value = resultSet.getString(i);
//    					if (colName.equalsIgnoreCase(PROCEDURE_NAME)) {
//    						procName = value;
//    					} else if(colName.equalsIgnoreCase(PROCEDURE_SCHEM)) {
//    						procSchema = value;
//    					}
//    				}
//    				if(procSchema!=null && !procSchema.equalsIgnoreCase(SYS) && !procSchema.equalsIgnoreCase(SYSADMIN)) {
//    					procNameList.add(procName);
//    					procSchemaList.add(procSchema);
//    				}
//    			}
//    			resultSet.close();
//    		} catch (Exception e) {
//    		}
//    	}
//    	
//    	List<QueryTableProcBean> resultList = new ArrayList<QueryTableProcBean>(procNameList.size());
//    	
//    	// Build full names if schemaName is present
//    	for(int i=0; i<procNameList.size(); i++) {
//    		QueryTableProcBean tableProcBean = new QueryTableProcBean();
//
//    		String schemaName = procSchemaList.get(i);
//    		if(schemaName!=null && schemaName.length()>0) {
//    			tableProcBean.setName(schemaName+"."+procNameList.get(i));
//    			tableProcBean.setType(QueryTableProcBean.PROCEDURE);
//    		} else {
//    			tableProcBean.setName(procNameList.get(i));
//    			tableProcBean.setType(QueryTableProcBean.PROCEDURE);
//    		}
//    		resultList.add(tableProcBean);
//    	}
//    	return resultList;
//    }
    
    /*
     * Get List of Procedures using the supplied connection
     * @param connection the JDBC connection
     * @return the list of procedure names
     */
    private List<QueryTableProcBean> getTeiidProcedures(Connection connection) throws DataVirtUiException {
    	// Get the list of Procedures
    	List<String> procNameList = new ArrayList<String>();
    	List<String> procSchemaList = new ArrayList<String>();
    	if(connection!=null) {
    		try {
    			ResultSet resultSet = connection.getMetaData().getProcedures(null, null, "%");
    			int columnCount = resultSet.getMetaData().getColumnCount();
    			while (resultSet.next()) {
    				String procName = null;
    				String procSchema = null;
    				for (int i=1 ; i<=columnCount ; ++i) {
    					String colName = resultSet.getMetaData().getColumnName(i);
    					String value = resultSet.getString(i);
    					if (colName.equalsIgnoreCase(PROCEDURE_NAME)) {
    						procName = value;
    					} else if(colName.equalsIgnoreCase(PROCEDURE_SCHEM)) {
    						procSchema = value;
    					}
    				}
    				if(procSchema!=null && !procSchema.equalsIgnoreCase(SYS) && !procSchema.equalsIgnoreCase(SYSADMIN)) {
    					procNameList.add(procName);
    					procSchemaList.add(procSchema);
    				}
    			}
    			resultSet.close();
    		} catch (Exception e) {
    			throw new DataVirtUiException(e);
    		}
    	}
    	
    	List<QueryTableProcBean> resultList = new ArrayList<QueryTableProcBean>(procNameList.size());
    	
    	// Build full names if schemaName is present
    	for(int i=0; i<procNameList.size(); i++) {
    		QueryTableProcBean tableProcBean = new QueryTableProcBean();

    		String schemaName = procSchemaList.get(i);
    		if(schemaName!=null && schemaName.length()>0) {
    			tableProcBean.setName(schemaName+"."+procNameList.get(i));
    			tableProcBean.setType(QueryTableProcBean.PROCEDURE);
    		} else {
    			tableProcBean.setName(procNameList.get(i));
    			tableProcBean.setType(QueryTableProcBean.PROCEDURE);
    		}
    		resultList.add(tableProcBean);
    	}
    	return resultList;
    }

    /*
     * Get List of Column names using the supplied connection and table name
     * @param connection the JDBC connection
     * @param fullTableName the Table name to get columns
     * @return the list of QueryColumnBeans
     */
//    private List<QueryColumnBean> getColumnsForTable(Connection connection, String fullTableName) throws DataVirtUiException {
//    	
//    	List<QueryColumnBean> resultList = new ArrayList<QueryColumnBean>();
//    	
//    	if(connection==null || fullTableName==null || fullTableName.trim().isEmpty()) {
//    		return resultList;
//    	}
//
//    	String schemaName = null;
//    	String tableName = null;
//    	int indx = fullTableName.lastIndexOf(".");
//    	if(indx!=-1) {
//    		schemaName = fullTableName.substring(0, indx);
//    		tableName = fullTableName.substring(indx+1);
//    	} else {
//    		tableName = fullTableName;
//    	}
//
//    	// Get the column name and type for the supplied schema and tableName
//    	try {
//    		ResultSet resultSet = connection.getMetaData().getColumns(null, null, tableName, null);
//    		while(resultSet.next()) {
//    			String columnName = resultSet.getString(COLUMN_NAME);
//    			String columnType = resultSet.getString(TYPE_NAME);
//    			QueryColumnBean colBean = new QueryColumnBean();
//    			colBean.setName(columnName);
//    			colBean.setType(columnType);
//    			resultList.add(colBean);
//    		}
//    		resultSet.close();
//    	} catch (Exception e) {
//    	}
//
//    	return resultList;
//    }
    
    /*
     * Get List of Column names using the supplied connection and table name
     * @param connection the JDBC connection
     * @param fullTableName the Table name to get columns
     * @return the list of QueryColumnBeans
     */
    private List<QueryColumnBean> getTeiidColumnsForTable(Connection connection, String fullTableName) throws DataVirtUiException {
    	
    	List<QueryColumnBean> resultList = new ArrayList<QueryColumnBean>();
    	
    	if(connection==null || fullTableName==null || fullTableName.trim().isEmpty()) {
    		return resultList;
    	}

    	// Get the column name and type for the supplied schema and tableName
    	try {
    		ResultSet resultSet = connection.getMetaData().getColumns(null, null, fullTableName, null);
    		while(resultSet.next()) {
    			String columnName = resultSet.getString(COLUMN_NAME);
    			String columnType = resultSet.getString(TYPE_NAME);
    			QueryColumnBean colBean = new QueryColumnBean();
    			colBean.setName(columnName);
    			colBean.setType(columnType);
    			resultList.add(colBean);
    		}
    		resultSet.close();
    	} catch (Exception e) {
    	}

    	return resultList;
    }

    /*
     * Get List of Column names using the supplied connection and procedure name
     * @param connection the JDBC connection
     * @param fullProcName the Procedure name to get columns
     * @return the array of Column names
     */
    private List<QueryColumnBean> getColumnsForProcedure(Connection connection,String fullProcName) throws DataVirtUiException {

    	List<QueryColumnBean> resultList = new ArrayList<QueryColumnBean>();
    	
    	if(connection==null || fullProcName==null || fullProcName.trim().isEmpty()) {
    		return resultList;
    	}

    	String schemaName = null;
    	String procName = null;
    	int indx = fullProcName.lastIndexOf(".");
    	if(indx!=-1) {
    		schemaName = fullProcName.substring(0, indx);
    		procName = fullProcName.substring(indx+1);
    	} else {
    		procName = fullProcName;
    	}

    	// Get the column name and type for the supplied schema and procName
    	try {
    		ResultSet resultSet = connection.getMetaData().getProcedureColumns(null, schemaName, procName, null);
    		while(resultSet.next()) {
    			String columnName = resultSet.getString(COLUMN_NAME);
    			String columnType = resultSet.getString(COLUMN_TYPE);
    			String columnDataType = resultSet.getString(TYPE_NAME);
    			QueryColumnBean colBean = new QueryColumnBean();
    			colBean.setName(columnName);
    			colBean.setType(columnDataType);
    			colBean.setDirType(getProcColumnDirType(columnType));
    			resultList.add(colBean);
    		}
    		resultSet.close();
    	} catch (Exception e) {
    	}

    	return resultList;
    }

    /*
     * Interprets the procedure column type codes from jdbc call to strings
     * @intStr the stringified code
     * @return the direction type
     */
    private String getProcColumnDirType(String intStr) {
    	String result = "UNKNOWN";
    	if(intStr!=null) {
    		if(intStr.trim().equals("1")) {
    			result = "IN";
    		} else if(intStr.trim().equals("2")) {
    			result = "INOUT";
    		} else if(intStr.trim().equals("4")) {
    			result = "OUT";
    		} else if(intStr.trim().equals("3")) {
    			result = "RETURN";
    		} else if(intStr.trim().equals("5")) {
    			result = "RESULT";
    		}                 
    	}
    	return result;
    }

    /*
     * Get Connection for the specified DataSource Name from the Map of DataSources
     */
    private Connection getConnection (String datasourceName, Map<String,DataSource> mDatasources) throws SQLException {
    	Connection connection = null;
    	if(mDatasources!=null) {
    		DataSource dataSource = (DataSource) mDatasources.get(datasourceName);
    		if(dataSource!=null) {
    			connection = dataSource.getConnection();
    		}
    	}
    	return connection;
    }

    /*
     * Close the supplied connection
     */
    private void closeConnection(Connection conn) throws SQLException {
    	if(conn!=null) {
    		conn.close();
    	}
    }
        
}
