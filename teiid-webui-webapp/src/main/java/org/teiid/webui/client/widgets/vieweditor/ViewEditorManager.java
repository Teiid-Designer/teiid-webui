package org.teiid.webui.client.widgets.vieweditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.teiid.webui.client.utils.DdlHelper;
import org.teiid.webui.client.widgets.CheckableNameTypeRow;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.DataSourcePageRow;
import org.teiid.webui.share.services.StringUtils;

import com.google.gwt.user.client.Window;


/**
 * ViewEditorManager
 * Business object that wizard pages can access.
 * Currently the Manager is set up for one or two tables.  Can be expanded in future to handle more if required.
 *
 */
public class ViewEditorManager {
	
	private static ViewEditorManager instance = new ViewEditorManager();
	
	private List<String> tables = new ArrayList<String>();
	// Maps are keyed on the table index.  Not keyed on name in case of duplicate names.
	private Map<Integer,List<CheckableNameTypeRow>> allColumnsMap = new HashMap<Integer,List<CheckableNameTypeRow>>();
	private Map<Integer,List<String>> selectedColumnNamesMap = new HashMap<Integer,List<String>>();
	private Map<Integer,List<String>> selectedColumnTypesMap = new HashMap<Integer,List<String>>();
	private Map<Integer,String> joinColumnMap = new HashMap<Integer,String>();
	private Map<Integer,String> sourceForTableMap = new HashMap<Integer,String>();
	private List<DataSourcePageRow> allAvailableSources = new ArrayList<DataSourcePageRow>();
	private Map<Integer,String> sourceTransformationSQLMap = new HashMap<Integer,String>();
	private String joinType = "INNER";
	private int defineTemplateTableIndx = 0;

	/**
	 * Get the singleton instance
	 *
	 * @return instance
	 */
	public static ViewEditorManager getInstance() {
		return instance;
	}

	/*
	 * Create a ViewEditorManager
	 */
	private ViewEditorManager() {
	}
	
	/**
	 * Clears all of the selections
	 */
	public void clear( ) {
		tables.clear();
		allColumnsMap.clear();
		selectedColumnNamesMap.clear();
		selectedColumnTypesMap.clear();
		joinColumnMap.clear();
		sourceForTableMap.clear();
		sourceTransformationSQLMap.clear();
		joinType = "INNER";
		defineTemplateTableIndx = 0;
	}
	
	/**
	 * Sets the sources corresponding to a table
	 * @param tableIndx the table index
	 * @param sourceName the source name
	 */
	public void setSourceForTable(int tableIndx, String sourceName) {
		this.sourceForTableMap.put(tableIndx, sourceName);
	}
	
	/**
	 * Get the source name for the specified table
	 * @param tableIndx the table index
	 * @return the source name
	 */
	public String getSourceNameForTable(int tableIndx) {
		return this.sourceForTableMap.get(tableIndx);
	}

	/**
	 * Get the source type for the specified table
	 * @param tableIndx the table index
	 * @return the source type
	 */
	public String getSourceTypeForTable(int tableIndx) {
		// The name of the source
		String sourceName = getSourceNameForTable(tableIndx);
		for(DataSourcePageRow source : getAvailableSources()) {
			if(source.getName().equalsIgnoreCase(sourceName)) {
				return source.getType();
			}
		}
		return null;
	}
	
	/**
	 * Return template required states for the current tables.
	 * @return list of "template required" states for the tables.
	 */
	public List<Boolean> getTableTemplateRequiredStates() {
		int nTables = getTables().size();
		List<Boolean> requiredStates = new ArrayList<Boolean>(nTables);
		for(int i=0; i<nTables; i++) {
			String sourceType = getSourceTypeForTable(i);
			if(sourceType.equalsIgnoreCase("webservice") || sourceType.equalsIgnoreCase("file")) {
				requiredStates.add(true);
			} else {
				requiredStates.add(false);
			}
		}
		return requiredStates;
	}

	/**
	 * Get the number of tables requiring template definitions
	 * @return the number of tables
	 */
	public int getNumberTablesRequiringTemplates() {
		int rqdTemplates = 0;
		int nTables = getTables().size();
		for(int i=0; i<nTables; i++) {
			if(tableRequiresTemplate(i)) {
				rqdTemplates++;
			}
		}
		return rqdTemplates;
	}
	
	public boolean tableRequiresTemplate(int tableIndex) {
		String sourceType = getSourceTypeForTable(tableIndex);
		if(sourceType!=null && (sourceType.equalsIgnoreCase("webservice") || sourceType.equalsIgnoreCase("file"))) {
			return true;
		} 
		return false;
	}

	/**
	 * Gets the sources for the currently specified tables
	 * @return the sources
	 */
	public List<String> getSources() {
		List<String> resultList = new ArrayList<String>();
		resultList.addAll(this.sourceForTableMap.values());
		return resultList;
	}
	
	/**
	 * Sets the tables
	 * @param tables
	 */
	public void setTables(List<String> tables) {
		for(int i=0; i<tables.size(); i++) {
			setTable(i,tables.get(i));
			clearMaps(i);
		}
	}
	
	public void setTable(int index, String table) {
		int nTables = this.tables.size();
		if(nTables >= index+1) {
			this.tables.remove(index);
		}
		this.tables.add(index, table);
	}
	
	public void removeTable(int index) {
		int nTables = this.tables.size();
		// If removing last table, ok to remove
		if(index>=nTables-1) {
			tables.remove(index);
			clearMaps(index);
		// If existing table after this index, move it down in position
		} else {
			allColumnsMap.put(index, allColumnsMap.get(index+1));
			selectedColumnNamesMap.put(index, selectedColumnNamesMap.get(index+1));
			selectedColumnTypesMap.put(index, selectedColumnTypesMap.get(index+1));
			joinColumnMap.put(index, joinColumnMap.get(index+1));;
			sourceForTableMap.put(index, sourceForTableMap.get(index+1));
			sourceTransformationSQLMap.put(index, sourceTransformationSQLMap.get(index+1));
			clearMaps(index+1);
			tables.remove(index);
		}
	}
	
	public void clearMaps(int tableIndx) {
		// Clear all maps that depend on table indx
		allColumnsMap.put(tableIndx, null);
		selectedColumnNamesMap.put(tableIndx, null);
		selectedColumnTypesMap.put(tableIndx, null);
		joinColumnMap.put(tableIndx, null);;
		sourceForTableMap.put(tableIndx, null);
		sourceTransformationSQLMap.put(tableIndx, null);
	}
	
	/**
	 * Gets the tables for the wizard
	 * @return the tables
	 */
	public List<String> getTables() {
		return tables;
	}

	/**
	 * Gets the table for the specified index
	 * @return the table
	 */
	public String getTable(int index) {
		if(tables.size() >= (index+1)) {
			return tables.get(index);
		}
		return null;
	}
	
	public int getTableIndex(String tableName) {
		int index = -1;
		for(int i=0; i<tables.size(); i++) {
			String name = tables.get(i);
			if(name!=null && name.equals(tableName)) {
				index = i;
				break;
			}
		}
		return index;
	}

	/**
	 * Sets this list of all available sources currently on the server
	 * @param allSources all available sources
	 */
	public void setAvailableSources(List<DataSourcePageRow> allSources) {
		this.allAvailableSources.clear();
		this.allAvailableSources.addAll(allSources);
	}

	/**
	 * Gets the list of all available sources for building views
	 * @return the list of all sources
	 */
	public List<DataSourcePageRow> getAvailableSources() {
		return this.allAvailableSources;
	}
	
	/**
	 * Gets the list of all available source names for building views
	 * @return the list of all sources
	 */
	public List<String> getAvailableSourceNames() {
		List<String> sourceNames = new ArrayList<String>();
		for(DataSourcePageRow source : getAvailableSources()) {
			sourceNames.add(source.getName());
		}
		return sourceNames;
	}
	
	/**
	 * Sets the available columns for the specified table
	 * @param tableIndx the table index
	 * @param columns the list of available columns
	 */
	public void setColumns(Integer tableIndx,List<CheckableNameTypeRow> columns) {
		allColumnsMap.put(tableIndx,columns);
	}
	
	/**
	 * Gets the list of all columns for the specified table
	 * @param tableIndx the table index
	 * @return the list of available columns
	 */
	public List<CheckableNameTypeRow> getColumns(Integer tableIndx) {
		// If the table requires a template, parse the columns in the template sql
		if(tableRequiresTemplate(tableIndx)) {
			String sqlTemplate = getSourceTransformationSQL(tableIndx);
			return getColumnsFromSQL(sqlTemplate);
		// No template - get columns from map
		} else {
			return allColumnsMap.get(tableIndx);
		}
	}
	
	private List<CheckableNameTypeRow> getColumnsFromSQL(String sql) {
		List<CheckableNameTypeRow> rows = new ArrayList<CheckableNameTypeRow>();
		
		// Parse the column names between SELECT and FROM
		int startIndx = sql.toUpperCase().indexOf(Constants.SELECT) + Constants.SELECT.length();
		int endIndx = sql.toUpperCase().indexOf("FROM");
		String colString = sql.substring(startIndx, endIndx);
		
		String[] cols = colString.split(",");
		for(String col : cols) {
			CheckableNameTypeRow row = new CheckableNameTypeRow();
			row.setName(col.trim());
			row.setType("string");
			rows.add(row);
		}

		return rows;
	}
	
	/**
	 * Sets the selected column names for the specified table
	 * @param tableIndx the table index
	 * @param columnNames the list of selected columns
	 */
	public void setSelectedColumns(Integer tableIndx, List<String> columnNames) {
		selectedColumnNamesMap.put(tableIndx,columnNames);
	}
	
	/**
	 * Gets the selected column names for the specified table
	 * @param tableIndx the table index
	 * @return the list of selected column names
	 */
	public List<String> getSelectedColumns(Integer tableIndx) {
		return selectedColumnNamesMap.get(tableIndx);
	}
	
	/**
	 * Sets the selected column types for the specified table
	 * @param tableIndx the table index
	 * @param columnTypes the list of selected column types
	 */
	public void setSelectedColumnTypes(Integer tableIndx, List<String> columnTypes) {
		selectedColumnTypesMap.put(tableIndx,columnTypes);
	}
	
	/**
	 * Gets the selected column types for the specified table
	 * @param tableIndx the table index
	 * @return the list of selected column types
	 */
	public List<String> getSelectedColumnTypes(Integer tableIndx) {
		return selectedColumnTypesMap.get(tableIndx);
	}
	
	/**
	 * Sets the join column for the specified table
	 * @param tableIndx the table index
	 * @param columnName the join column name
	 */
	public void setJoinColumn(Integer tableIndx, String columnName) {
		joinColumnMap.put(tableIndx,columnName);
	}
	
	/**
	 * Gets the join column for the specified table
	 * @param tableIndx the table index
	 * @return the join column name
	 */
	public String getJoinColumn(Integer tableIndx) {
		return joinColumnMap.get(tableIndx);
	}
	
	/**
	 * Set the Source Transformation SQL for the specified table.  Only required for 'templated' sources like webservices or file sources
	 * @param tableIndx the table index
	 * @param sql the source transformation SQL
	 */
	public void setSourceTransformationSQL(Integer tableIndx, String sql) {
		this.sourceTransformationSQLMap.put(tableIndx, sql);
	}
	
	/**
	 * Set the Source Transformation SQL for the specified table.  Only required for 'templated' sources like webservices or file sources
	 * @param tableIndx the table index
	 * @return the source transformation SQL
	 */
	public String getSourceTransformationSQL(Integer tableIndx) {
		return this.sourceTransformationSQLMap.get(tableIndx);
	}

	/**
	 * Sets the join type
	 * @param joinType the join type
	 */
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	
	/**
	 * Gets the join type
	 * @return the join type
	 */
	public String getJoinType() {
		return joinType;
	}
	
	/**
	 * Sets the Define Template table index
	 * @param tableIndx the table index
	 */
	public void setDefineTemplateTableIndex(int tableIndx) {
		this.defineTemplateTableIndx = tableIndx;
	}
	
	/**
	 * Gets the Define Template table index
	 * @return the table index
	 */
	public int getDefineTemplateTableIndex( ) {
		return this.defineTemplateTableIndx;
	}
	
	/**
	 * Constructs the DDL for the view, based on the current selections
	 */
	public String getViewDdl() {
		if(getTables().size()==1) {
			String selectedTable = getTable(0);
			List<String> colNames = getSelectedColumns(0);
			List<String> colTypes = getSelectedColumnTypes(0);

			return DdlHelper.getODataViewDdl(Constants.SERVICE_VIEW_NAME, selectedTable, colNames, colTypes);
		} else {
			return buildJoinDdl();
		}
	}
	
    /**
     * Build the Join DDL from the selections
     * @return the DDL
     */
    private String buildJoinDdl( ) {
    	String leftTable = getTable(0);
    	String rightTable = getTable(1);
    	
		List<String> lhsColNames = getSelectedColumns(0);
		List<String> lhsColTypes = getSelectedColumnTypes(0);
		List<String> rhsColNames = getSelectedColumns(1);
		List<String> rhsColTypes = getSelectedColumnTypes(1);

    	String lhsCriteriaCol = getJoinColumn(0);
    	String rhsCriteriaCol = getJoinColumn(1);
    	String jType = getJoinType();
    	
    	// Gets either the table name or template SQL
    	String lhs = null;
    	String rhs = null;
    	if(tableRequiresTemplate(0)) {
    		lhs = getSourceTransformationSQL(0);
    	} else {
    		lhs = StringUtils.escapeSQLName(getTable(0));
    	}
    	if(tableRequiresTemplate(1)) {
    		rhs = getSourceTransformationSQL(1);
    	} else {
    		rhs = StringUtils.escapeSQLName(getTable(1));
    	}
    	
     	String viewDdl = DdlHelper.getODataViewJoinDdl(Constants.SERVICE_VIEW_NAME, lhs, lhsColNames, lhsColTypes, lhsCriteriaCol,
     			                                                                    rhs, rhsColNames, rhsColTypes, rhsCriteriaCol, jType);
    	
     	return viewDdl;
    }
    

}
