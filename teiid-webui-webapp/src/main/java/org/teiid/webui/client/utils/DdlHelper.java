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
package org.teiid.webui.client.utils;

import java.util.ArrayList;
import java.util.List;

import org.teiid.webui.share.Constants;
import org.teiid.webui.share.services.StringUtils;


public class DdlHelper {

	public static final String DDL_TEMPLATE_SINGLE_SOURCE = "Single Source";
	public static final String DDL_TEMPLATE_TWO_SOURCE_JOIN = "Two Source Join";
	public static final String DDL_TEMPLATE_FLAT_FILE = "Flat File";
	public static final String DDL_TEMPLATE_WEBSERVICE = "Web Service";
	
	/**
	 * Get the View DDL for the supplied params
	 * @param viewName the view name
	 * @param sourceName the source name
	 * @param columnNames the list of column names
	 * @return the View DDL
	 */
	public static String getViewDdl(String viewName, String sourceName, List<String> columnNames) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE VIEW ");
		sb.append(viewName);
		sb.append(" AS SELECT ");
		sb.append(getColString(columnNames));
		sb.append(" FROM ");
		sb.append(StringUtils.escapeSQLName(sourceName));
		sb.append(";");
		
		return sb.toString();
	}
	
	/**
	 * Generated View DDL that supports the Teiid OData requirement - that views must have a Primary Key - to get auto-generated.
	 * @param viewName the view name
	 * @param sourceName the source name
	 * @param columnNames the list of column names
	 * @return the View DDL
	 */
	public static String getODataViewDdl(String viewName, String sourceName, List<String> columnNames, List<String> typeNames) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE VIEW ");
		sb.append(viewName);
		sb.append(" (RowId integer PRIMARY KEY, ");
		sb.append(getColWithTypeString(columnNames,typeNames));
		sb.append(") AS \nSELECT ");
		sb.append(" ROW_NUMBER() OVER (ORDER BY ");
		sb.append(StringUtils.escapeSQLName(columnNames.get(0)));
		sb.append(") , ");
		sb.append(getColString(columnNames));
		sb.append(" \nFROM ");
		sb.append(StringUtils.escapeSQLName(sourceName)); 
		sb.append(";");
		
		return sb.toString();
	}
	
	/**
	 * Generated View DDL that supports the Teiid OData requirement - that views must have a Primary Key - to get auto-generated.
	 * @param viewName the view name
	 * @param sourceName the source name
	 * @param columnNames the list of column names
	 * @return the View DDL
	 */
	public static String getODataViewJoinDdl(String viewName, String lhsTableName, List<String> lhsColNames, List<String> lhsColTypes, String lhsCriteriaCol,
			                                                  String rhsTableName, List<String> rhsColNames, List<String> rhsColTypes, String rhsCriteriaCol, String joinType) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE VIEW ");
		sb.append(viewName);
		sb.append(" (RowId integer PRIMARY KEY, ");
		sb.append(getColWithTypeString(lhsColNames,lhsColTypes));
		sb.append(", ");
		sb.append(getColWithTypeString(rhsColNames,rhsColTypes));
		sb.append(") AS \nSELECT ");
		sb.append(" ROW_NUMBER() OVER (ORDER BY ");
		sb.append(StringUtils.escapeSQLName(lhsColNames.get(0)));
		sb.append(") , ");
		sb.append(getColString(lhsColNames));
		sb.append(", ");
		sb.append(getColString(rhsColNames));
		sb.append(" \nFROM ");
		sb.append(StringUtils.escapeSQLName(lhsTableName));
		if(Constants.JOIN_TYPE_INNER.equals(joinType)) {
			sb.append("\n  INNER JOIN "+StringUtils.escapeSQLName(rhsTableName));
		} else if(Constants.JOIN_TYPE_LEFT_OUTER.equals(joinType)) {
			sb.append("\n  LEFT OUTER JOIN "+StringUtils.escapeSQLName(rhsTableName));
		} else if(Constants.JOIN_TYPE_RIGHT_OUTER.equals(joinType)) {
			sb.append("\n  RIGHT OUTER JOIN "+StringUtils.escapeSQLName(rhsTableName));
		} else if(Constants.JOIN_TYPE_FULL_OUTER.equals(joinType)) {
			sb.append("\n FULL OUTER JOIN "+StringUtils.escapeSQLName(rhsTableName));
		} else {
			sb.append("\n  INNER JOIN "+StringUtils.escapeSQLName(rhsTableName));
		}
		sb.append(" ON ");
		sb.append(StringUtils.escapeSQLName(lhsCriteriaCol)+" = "+StringUtils.escapeSQLName(rhsCriteriaCol));
		sb.append(";");
		
		return sb.toString();
	}

	private static String getColString(List<String> columnNames) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<columnNames.size(); i++) {
			if(i!=0 ) {
				sb.append(",");
			}
			sb.append(StringUtils.escapeSQLName(columnNames.get(i))); 
		}
		return sb.toString();
	}
	
	private static String getColWithTypeString(List<String> columnNames, List<String> typeNames) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<columnNames.size(); i++) {
			if(i!=0 ) {
				sb.append(",");
			}
			sb.append(StringUtils.escapeSQLName(columnNames.get(i)));
			sb.append(" ");
			sb.append(typeNames.get(i));
		}
		return sb.toString();
	}
	
	public static String getDdlTemplate(String templateType) {
		StringBuilder sb = new StringBuilder();
		List<String> columnNames = new ArrayList<String>(2);
		columnNames.add("<COL1>");
		columnNames.add("<COL2>");
		List<String> columnTypes = new ArrayList<String>(2);
		columnTypes.add("string");
		columnTypes.add("string");
		if(templateType.equals(DDL_TEMPLATE_SINGLE_SOURCE)) {
			sb.append("CREATE VIEW SvcView");
			sb.append("  (RowId integer PRIMARY KEY, ");
			sb.append(getColWithTypeString(columnNames,columnTypes));
			sb.append(") AS \n");
			sb.append("SELECT \n");
			sb.append("  ROW_NUMBER() OVER (ORDER BY ");
			sb.append(StringUtils.escapeSQLName(columnNames.get(0)));
			sb.append(") , ");
			sb.append(getColString(columnNames) + "\n");
			sb.append("FROM \n");
			sb.append("  <SOURCE_NAME> ;");
		} else if(templateType.equals(DDL_TEMPLATE_TWO_SOURCE_JOIN)) {
			sb.append("CREATE VIEW SvcView");
			sb.append("  (RowId integer PRIMARY KEY, ");
			sb.append(getColWithTypeString(columnNames,columnTypes));
			sb.append(") AS \n");
			sb.append("SELECT \n");
			sb.append("  ROW_NUMBER() OVER (ORDER BY ");
			sb.append(StringUtils.escapeSQLName(columnNames.get(0)));
			sb.append(") , ");
			sb.append(getColString(columnNames) + "\n");
			sb.append("FROM \n");
			sb.append("  <SOURCE_1>, <SOURCE_2> ");
			sb.append("WHERE \n");
			sb.append("  <SOURCE_1>.TABLEX.COL1 = <SOURCE_2>.TABLEY.COL1 ;");
		} else if(templateType.equals(DDL_TEMPLATE_FLAT_FILE)) {
			sb.append("CREATE VIEW SvcView (LastName string, FirstName string) AS \n");
			sb.append("SELECT \n");
			sb.append("  A.LastName, A.FirstName \n");
			sb.append("FROM \n");
			sb.append("  (EXEC FlatFileSource.getTextFiles('EMPLOYEEDATA.txt')) AS f,");
			sb.append("  TEXTTABLE(f.file COLUMNS LastName string, FirstName string HEADER) AS A; ");
		} else if(templateType.equals(DDL_TEMPLATE_WEBSERVICE)) {
			sb.append("CREATE VIEW SvcView (COMMON string, BOTANICAL string) AS \n");
			sb.append("SELECT \n");
			sb.append("  A.COMMON, A.BOTANICAL \n");
			sb.append("FROM \n");
			sb.append("  (EXEC WebServiceSource.invokeHttp('GET', null, 'http://www.w3schools.com/xml/Plant_Catalog.xml', 'TRUE')) AS f,");
			sb.append("  XMLTABLE('/CATALOG/PLANT' PASSING XMLPARSE(DOCUMENT f.result) ");
			sb.append("  COLUMNS COMMON string PATH 'COMMON/text()', BOTANICAL string PATH 'BOTANICAL/text()' ) AS A;");
		}
		return sb.toString();
	}
	
	/**
	 * Get the procedure for exposing a view as REST
	 * @param xmlTagGroup the outer tag for element grouping
	 * @param xmlTagIndiv the individual element tag
	 * @param colNames the list of columns
	 * @param srcView the view source
	 * @return the REST procedure
	 */
	public static String getRestProcedureDdl(String procName, String xmlTagGroup, String xmlTagIndiv, List<String> colNames, String srcView, String resturi) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\nSET NAMESPACE 'http://teiid.org/rest' AS REST;\n");
		sb.append("CREATE VIRTUAL PROCEDURE ");
		sb.append(procName);
		sb.append(" () RETURNS (result XML) ");
		sb.append(" OPTIONS (\"REST:METHOD\" 'GET', \"REST:URI\" '"+resturi+"') AS \n");
		sb.append("  BEGIN \n");
		sb.append("  SELECT XMLELEMENT(NAME ");
		sb.append(xmlTagGroup);
		sb.append(", XMLAGG(XMLELEMENT(NAME ");
		sb.append(xmlTagIndiv);
		sb.append(", XMLFOREST(");
		sb.append(getColumnsString(colNames));
		sb.append(")))) AS result \n");
		sb.append("  FROM ");
		sb.append(StringUtils.escapeSQLName(srcView));
		sb.append("; \n");
		sb.append("  END;");

		return sb.toString();
	}
	
	/**
	 * Get stringified column name list, separated by commas
	 * @param colList
	 * @return column string
	 */
	private static String getColumnsString(List<String> colList) {
		StringBuilder sb = new StringBuilder();

		for(String colName : colList) {
			if(!sb.toString().isEmpty()) {
				sb.append(",");
			}
			sb.append(StringUtils.escapeSQLName(colName));
		}
		return sb.toString();
	}
	
    /**
     * Construct a REST procedure DDL using the view DDL
     * @param procName a name for the procedure
     * @param viewDdl the View DDL to build procedure from
     * @param xmlTagGroup the xml tag for the group of elements
     * @param xmlTagIndiv the xml tag for an individual element
     * @param srcViewName the name of the source view
     * @param restUri the rest endpoint uriProperty
     * @return
     */
    public static String getRestProcDdlFromViewDdl(String procName, String viewDdl, String xmlTagGroup, String xmlTagIndiv, String srcViewName, String resturi) {
    	List<String> viewCols = getColNamesFromViewDdl(viewDdl);
    	return getRestProcedureDdl(procName, xmlTagGroup, xmlTagIndiv, viewCols, srcViewName, resturi);
    }
    
    /**
     * Parse the list of column names from the View DDL
     *   Example View DDL: CREATE VIEW SvcView (RowId integer PRIMARY KEY, Id string,Name string,Type string)	
     * @param viewDdl the view DDL
     * @return
     */
    private static List<String> getColNamesFromViewDdl(String viewDdl) {
    	List<String> colNames = new ArrayList<String>();
    	
    	int openParensIndx = viewDdl.indexOf('(');
    	int closeParensIndx = viewDdl.indexOf(')');
    	// String containing all column info - RowId integer PRIMARY KEY, Id string,Name string,Type string
    	String allColString = viewDdl.substring(openParensIndx+1,closeParensIndx);
    	
    	// Get individual column strings.
    	String[] fullColStrs = allColString.split(",");
    	for(int i=0; i<fullColStrs.length; i++) {
    		String fullColStr = fullColStrs[i];
    		// Split column string at spaces
    		String[] colParts = fullColStr.trim().split(" ");
    		// First part is the name
    		colNames.add(colParts[0].trim());
    	}
    	
    	return colNames;
    }
	
}
