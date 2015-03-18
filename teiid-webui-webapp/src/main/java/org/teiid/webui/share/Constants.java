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
package org.teiid.webui.share;

import java.util.HashSet;
import java.util.Set;

/**
 * Application constants
 * @author mdrillin@redhat.com
 */
public class Constants {

	public static final String COMMA = ",";
	
	public static final String WEBUI_USER = "webui";
	public static final String WEBUI_PASS = "webui";
	
	public static final String CREATE_DATA_SERVICE_SCREEN = "CreateDataServiceScreen";
	public static final String EDIT_DATA_SERVICE_SCREEN = "EditDataServiceScreen";
	public static final String DATA_SERVICE_DETAILS_SCREEN = "DataServiceDetailsScreen";
	public static final String DATA_SERVICES_LIBRARY_SCREEN = "DataServicesLibraryScreen";
	public static final String DATA_SERVICES_EMPTY_LIBRARY_SCREEN = "DataServicesEmptyLibraryScreen";
	public static final String MANAGE_SOURCES_SCREEN = "ManageSourcesScreen";
	public static final String UNKNOWN = "[unknown]";
	
	public static final String CONFIRMATION_DIALOG = "ConfirmationDialog";
	public static final String CONFIRMATION_DIALOG_TYPE = "ConfirmationType";
	public static final String CONFIRMATION_DIALOG_MESSAGE = "ConfirmationMessage";
    public static final String CONFIRMATION_DIALOG_DELETE_SERVICE = "ConfirmDeleteService";
    public static final String CONFIRMATION_DIALOG_EDIT_SERVICE_ABORT = "ConfirmEditServiceAbort";
    public static final String CONFIRMATION_DIALOG_SOURCE_RENAME = "ConfirmSourceRename";
    public static final String CONFIRMATION_DIALOG_SOURCE_REDEPLOY = "ConfirmSourceRedeploy";
    public static final String CONFIRMATION_DIALOG_SOURCE_CHANGETYPE = "ConfirmSourceChangeType";
    public static final String CONFIRMATION_DIALOG_SOURCE_DELETE = "ConfirmSourceDelete";
    public static final String CONFIRMATION_DIALOG_REPLACE_VIEW_DEFN = "ConfirmReplaceViewDefn";    
    
	public static final String ADD_VIEW_SRC_DIALOG = "AddViewSrcDialog";
	public static final String ADD_VIEW_SRC_AVAILABLE_SRCS = "AddViewAvailSrcs";
	public static final String UPLOAD_DRIVER_DIALOG = "UploadDriverDialog";
	
	public static final int DATASOURCES_TABLE_PAGE_SIZE = 15;
	public static final int DATASOURCE_TYPES_TABLE_PAGE_SIZE = 15;
	public static final int VDBS_TABLE_PAGE_SIZE = 15;
	public static final int VDB_MODELS_TABLE_PAGE_SIZE = 15;
	public static final int QUERY_RESULTS_TABLE_PAGE_SIZE = 15;
	public static final int QUERY_COLUMNS_TABLE_PAGE_SIZE = 6;
	
	public static final String FROM_SCREEN = "from-screen";
	
	public static final String SERVICE_TEST_VDB_PREFIX = "SvcTestVdb_";
	public static final String SERVICE_SOURCE_VDB_PREFIX = "SvcSourceVdb_";
	public static final String PREVIEW_VDB_PREFIX = "PREVIEW_";
	
	public static final String JNDI_PREFIX = "java:/";
	public static final String JBOSS_JNDI_PREFIX = "java:jboss/datasources/";
	public static final String MODESHAPE_JNDI_PREFIX = "java:/datasources/";
	public static final String SERVICE_NAME_KEY = "service-name";
	public static final String SERVICE_VIEW_NAME = "SvcView";
	public static final String CLONE_SERVICE_KEY = "clone-service";
	public static final String DELETE_SERVICE_KEY = "delete-service";
	
	public static final String TEST_CONNECTION_FAILED = "Could not connect - please check source properties and server log, then enter desired properties";
    public static final String TEST_CONNECTION_ERROR_LOADING_SOURCES = "Error - could not load the available sources";

	public static final String SERVICE_NAME_CANNOT_BE_EMPTY_MSG = "The name cannot be empty";
	public static final String SERVICE_NAME_FIRST_CHAR_MUST_BE_ALPHA_MSG = "The first character of the name must be an alphabetic character";

	public static final String REST_PROCNAME = "RestProc";
    public static final String REST_XML_GROUPTAG = "Elems";
    public static final String REST_XML_ELEMENTTAG = "Elem";
    public static final String REST_URI_PROPERTY = "rest";

    public static final String VDB_PROP_KEY_REST_AUTOGEN = "{http://teiid.org/rest}auto-generate";
    public static final String VDB_PROP_KEY_DATASERVICE_VIEWNAME = "data-service-view";
    
    public static final String DS_TYPE_ACCUMULO = "accumulo";
    public static final String DS_TYPE_FILE = "file";
	public static final String DS_TYPE_GOOGLE = "google";
	public static final String DS_TYPE_H2 = "h2";
	public static final String DS_TYPE_INFINISPAN = "infinispan";
	public static final String DS_TYPE_LDAP = "ldap";
	public static final String DS_TYPE_MODESHAPE = "modeshape";
	public static final String DS_TYPE_MONGODB = "mongodb";
	public static final String DS_TYPE_SALESFORCE = "salesforce";
	public static final String DS_TYPE_TEIID = "teiid";
	public static final String DS_TYPE_TEIID_LOCAL = "teiid-local";
	public static final String DS_TYPE_WEBSERVICE = "webservice";

	public static final String JOIN_TYPE_INNER = "Join-Inner";
	public static final String JOIN_TYPE_RIGHT_OUTER = "Join-RightOuter";
	public static final String JOIN_TYPE_LEFT_OUTER = "Join-LeftOuter";
	public static final String JOIN_TYPE_FULL_OUTER = "Join-FullOuter";
	
	public static final String MYSQL_FRAGMENT = "mysql";
	public static final String POSTGRES_FRAGMENT = "postgres";
	
	public static final String OPENSHIFT_HOST_PREFIX = "[OPENSHIFT]";
	
	public static final String OK = "OK";
	public static final String QUESTION_MARK = "?";
	public static final String DOT = ".";
	public static final String SELECT_STAR_FROM = "SELECT * FROM";
	public static final String LIMIT_10 = "LIMIT 10";
	public static final String SPACE = " ";
	public static final String BLANK = "";
	public static final String SUCCESS = "success";
	public static final String MODEL_TYPE_PHYSICAL = "PHYSICAL";
	public static final String MODEL_TYPE_VIRTUAL = "VIRTUAL";
	public static final char SQL_ESCAPE_CHAR = '\"';

	public static final String DYNAMIC_VDB_SUFFIX = "-vdb.xml";
	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_INACTIVE = "INACTIVE";
	public static final String STATUS_LOADING = "LOADING";
	public static final String STATUS_UNKNOWN = "Unknown";
	
	public static final String VDB_INACTIVE_DS_CONNECTION_ERROR = "INACTIVE: Data Source connection failed...";
	public static final String VDB_INACTIVE_SQL_VALIDATION_ERROR = "INACTIVE: Validation Error with SQL";
	public static final String VDB_INACTIVE_SQL_PARSE_ERROR = "INACTIVE: Parse Error with SQL";
	public static final String VDB_INACTIVE_METADATA_LOADING = "INACTIVE: Metadata loading in progress...";
	public static final String VDB_INACTIVE_UNKNOWN_ERROR = "INACTIVE: unknown issue";
	
	public static final int VDB_LOADING_TIMEOUT_SECS = 300;
	public static final String NO_TYPE_SELECTION = "[Select a Type]";
	public static final String NO_TRANSLATOR_SELECTION = "[Select a Translator]";
	public static final String NO_CRITERIA_SELECTION = "[Select Criteria Column]";
	public static final String NO_DATASOURCE_SELECTION = "[Select a DataSource]";
	public static final String NO_CRITERIA_COLUMNS = "[No columns]";
	
	public static final String SORT_COLID_NAME = "name"; //$NON-NLS-1$
    public static final String SORT_COLID_MODIFIED_ON = "lastModifiedTimestamp"; //$NON-NLS-1$
    
    public static final Set<String> RESERVED_WORDS = new HashSet<String>() {
        {
        	add("BIGDECIMAL");
        	add("BIGINTEGER"); 
        	add("BREAK"); 
        	add("BYTE"); 
        	add("CRITERIA"); 
        	add("ERROR");  
        	add("LIMIT"); 
        	add("LONG"); 
        	add("LOOP"); 
        	add("MAKEDEP"); 
        	add("MAKENOTDEP"); 
        	add("NOCACHE"); 
        	add("STRING"); 
        	add("VIRTUAL"); 
        	add("WHILE"); 

        	//SQL2003 keywords
        	add("ADD"); 
        	add("ANY"); 
        	add("ALL"); 
        	add("ALLOCATE"); 
        	add("ALTER"); 
        	add("AND"); 
        	add("ARE"); 
        	add("ARRAY");
        	add("AS"); 
        	add("ASC"); 
        	add("ASENSITIVE"); 
        	add("ASYMETRIC"); 
        	add("ATOMIC"); 
        	add("AUTHORIZATION"); 
        	add("BEGIN"); 
        	add("BETWEEN"); 
        	add("BIGINT"); 
        	add("BINARY"); 
        	add("BLOB"); 
        	add("BOTH"); 
        	add("BY"); 
        	add("CALL"); 
        	add("CALLED"); 
        	add("CASE"); 
        	add("CAST"); 
        	add("CASCADED"); 
        	add("CHAR"); 
        	add("CHARACTER"); 
        	add("CHECK"); 
        	add("CLOB"); 
        	add("CLOSE"); 
        	add("COLLATE"); 
        	add("COLUMN"); 
        	add("COMMIT"); 
        	add("CONNECT"); 
        	add("CONVERT"); 
        	add("CONSTRAINT"); 
        	add("CONTINUE"); 
        	add("CORRESPONDING"); 
        	add("CREATE"); 
        	add("CROSS"); 
        	add("CURRENT_DATE"); 
        	add("CURRENT_TIME"); 
        	add("CURRENT_TIMESTAMP"); 
        	add("CURRENT_USER"); 
        	add("CURSOR"); 
        	add("CYCLE"); 
        	add("DATE"); 
        	add("DAY"); 
        	add("DEALLOCATE"); 
        	add("DEC"); 
        	add("DECIMAL"); 
        	add("DECLARE");     
        	add("DEFAULT"); 
        	add("DELETE"); 
        	add("DEREF"); 
        	add("DESC"); 
        	add("DESCRIBE"); 
        	add("DETERMINISTIC"); 
        	add("DISCONNECT"); 
        	add("DISTINCT"); 
        	add("DOUBLE"); 
        	add("DROP"); 
        	add("DYNAMIC"); 
        	add("EACH"); 
        	add("ELEMENT"); 
        	add("ELSE");    
        	add("END"); 
        	add("ESCAPE"); 
        	add("EXCEPT"); 
        	add("EXEC"); 
        	add("EXECUTE"); 
        	add("EXISTS"); 
        	add("EXTERNAL"); 
        	add("FALSE"); 
        	add("FETCH"); 
        	add("FILTER"); 
        	add("FLOAT"); 
        	add("FOR");     
        	add("FOREIGN"); 
        	add("FREE"); 
        	add("FROM"); 
        	add("FULL"); 
        	add("FUNCTION"); 
        	add("GET"); 
        	add("GLOBAL"); 
        	add("GRANT"); 
        	add("GROUP"); 
        	add("GROUPING"); 
        	add("HAS");  
        	add("HAVING"); 
        	add("HOLD"); 
        	add("HOUR"); 
        	add("IDENTITY"); 
        	add("INDICATOR"); 
        	add("IF");     
        	add("IMMEDIATE"); 
        	add("IN"); 
        	add("INOUT"); 
        	add("INNER"); 
        	add("INPUT"); 
        	add("INSENSITIVE"); 
        	add("INSERT"); 
        	add("INTEGER"); 
        	add("INTERSECT"); 
        	add("INTERVAL"); 
        	add("INT"); 
        	add("INTO"); 
        	add("IS");     
        	add("ISOLATION"); 
        	add("JOIN"); 
        	add("LANGUAGE"); 
        	add("LARGE"); 
        	add("LATERAL"); 
        	add("LEADING"); 
        	add("LEAVE"); 
        	add("LEFT"); 
        	add("LIKE"); 
        	add("LIKE_REGEX"); 
        	add("LOCAL"); 
        	add("LOCALTIME"); 
        	add("LOCALTIMESTAMP"); 
        	add("MATCH"); 
        	add("MEMBER"); 
        	add("MERGE"); 
        	add("METHOD"); 
        	add("MINUTE"); 
        	add("MODIFIES"); 
        	add("MODULE"); 
        	add("MONTH"); 
        	add("MULTISET"); 
        	add("NATIONAL"); 
        	add("NATURAL"); 
        	add("NCHAR"); 
        	add("NCLOB"); 
        	add("NEW"); 
        	add("NO"); 
        	add("NONE"); 
        	add("NOT"); 
        	add("NULL"); 
        	add("NUMERIC"); 
        	add("OBJECT"); 
        	add("OF"); 
        	add("OFFSET"); 
        	add("OLD"); 
        	add("ON"); 
        	add("ONLY"); 
        	add("OPEN"); 
        	add("OR"); 
        	add("ORDER"); 
        	add("OUT"); 
        	add("OUTER"); 
        	add("OUTPUT"); 
        	add("OPTION"); 
        	add("OPTIONS"); 
        	add("OVER"); 
        	add("OVERLAPS"); 
        	add("PARAMETER"); 
        	add("PARTITION"); 
        	add("PRECISION"); 
        	add("PREPARE"); 
        	add("PRIMARY"); 
        	add("PROCEDURE"); 
        	add("RANGE"); 
        	add("READS"); 
        	add("REAL"); 
        	add("RECURSIVE"); 
        	add("REFERENCES"); 
        	add("REFERENCING"); 
        	add("RELEASE"); 
        	add("RETURN"); 
        	add("RETURNS"); 
        	add("REVOKE"); 
        	add("RIGHT"); 
        	add("ROLLBACK"); 
        	add("ROLLUP"); 
        	add("ROW"); 
        	add("ROWS"); 
        	add("SAVEPOINT"); 
        	add("SCROLL"); 
        	add("SEARCH"); 
        	add("SECOND"); 
        	add("SELECT"); 
        	add("SENSITIVE"); 
        	add("SESSION_USER"); 
        	add("SET"); 
        	add("SHORT"); 
        	add("SIMILAR"); 
        	add("SMALLINT"); 
        	add("SOME"); 
        	add("SPECIFIC"); 
        	add("SPECIFICTYPE"); 
        	add("SQL"); 
        	add("SQLEXCEPTION"); 
        	add("SQLSTATE"); 
        	add("SQLWARNING"); 
        	add("SUBMULTILIST"); 
        	add("START"); 
        	add("STATIC"); 
        	add("SYMETRIC"); 
        	add("SYSTEM"); 
        	add("SYSTEM_USER"); 
        	add("TABLE"); 
        	add("TEMPORARY"); 
        	add("THEN"); 
        	add("TIME"); 
        	add("TIMESTAMP"); 
        	add("TIMEZONE_HOUR"); 
        	add("TIMEZONE_MINUTE"); 
        	add("TO"); 
        	add("TREAT"); 
        	add("TRAILING"); 
        	add("TRANSLATE");  
        	add("TRANSLATION");  
        	add("TRIGGER"); 
        	add("TRUE"); 
        	add("UNION"); 
        	add("UNIQUE"); 
        	add("UNKNOWN"); 
        	add("UPDATE"); 
        	add("USER"); 
        	add("USING");  
        	add("VALUE"); 
        	add("VALUES"); 
        	add("VARCHAR"); 
        	add("VARYING"); 
        	add("WHEN");     
        	add("WHENEVER");     
        	add("WHERE"); 
        	add("WINDOW"); 
        	add("WITH");     
        	add("WITHIN"); 
        	add("WITHOUT"); 
        	add("YEAR"); 

        	// SQL 2008 words
        	add("ARRAY_AGG"); 

        	//SQL/XML
        	add("XML"); 
        	add("XMLAGG"); 
        	add("XMLATTRIBUTES"); 
        	add("XMLBINARY"); 
        	add("XMLCAST"); 
        	add("XMLCOMMENT"); 
        	add("XMLCONCAT"); 
        	add("XMLDOCUMENT"); 
        	add("XMLELEMENT"); 
        	add("XMLEXISTS"); 
        	add("XMLFOREST"); 
        	add("XMLITERATE"); 
        	add("XMLNAMESPACES"); 
        	add("XMLPARSE"); 
        	add("XMLPI"); 
        	add("XMLQUERY"); 
        	add("XMLSERIALIZE"); 
        	add("XMLTABLE"); 
        	add("XMLTEXT"); 
        	add("XMLVALIDATE"); 

        	//SQL/MED
        	add("DATALINK"); 
        	add("DLNEWCOPY"); 
        	add("DLPREVIOUSCOPY"); 
        	add("DLURLCOMPLETE"); 
        	add("DLURELCOMPLETEWRITE"); 
        	add("DLURLCOMPLETEONLY"); 
        	add("DLURLPATH"); 
        	add("DLURLPATHWRITE"); 
        	add("DLURLPATHONLY"); 
        	add("DLURLSCHEME"); 
        	add("DLURLSEVER"); 
        	add("DLVALUE"); 
        	add("IMPORT"); 
        }
    };

}
