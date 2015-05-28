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

import java.util.Collection;


public class TranslatorHelper {

	public static final String TEIID_ACCUMULO_DRIVER = "accumulo"; //$NON-NLS-1$
	public static final String TEIID_FILE_DRIVER = "file"; //$NON-NLS-1$
	public static final String TEIID_GOOGLE_DRIVER = "google"; //$NON-NLS-1$
	public static final String TEIID_INFINISPAN_DRIVER = "infinispan"; //$NON-NLS-1$
	public static final String TEIID_LDAP_DRIVER = "ldap"; //$NON-NLS-1$
	public static final String TEIID_MONGODB_DRIVER = "mongodb"; //$NON-NLS-1$
	public static final String TEIID_SALESORCE_DRIVER = "salesforce"; //$NON-NLS-1$
	public static final String TEIID_WEBSERVICE_DRIVER = "webservice"; //$NON-NLS-1$
	public static final String TEIID_ACCUMULO_DRIVER_DISPLAYNAME = "Accumulo"; //$NON-NLS-1$
	public static final String TEIID_FILE_DRIVER_DISPLAYNAME = "FlatFile"; //$NON-NLS-1$
	public static final String TEIID_GOOGLE_DRIVER_DISPLAYNAME = "Google"; //$NON-NLS-1$
	public static final String TEIID_INFINISPAN_DRIVER_DISPLAYNAME = "Infinispan"; //$NON-NLS-1$
	public static final String TEIID_LDAP_DRIVER_DISPLAYNAME = "LDAP"; //$NON-NLS-1$
	public static final String TEIID_MONGODB_DRIVER_DISPLAYNAME = "MongoDB"; //$NON-NLS-1$
	public static final String TEIID_SALESORCE_DRIVER_DISPLAYNAME = "Salesforce"; //$NON-NLS-1$
	public static final String TEIID_WEBSERVICE_DRIVER_DISPLAYNAME = "WebService"; //$NON-NLS-1$

	public static final String TEIID_ACCUMULO_CLASS = "org.teiid.resource.adapter.accumulo.AccumuloManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_FILE_CLASS = "org.teiid.resource.adapter.file.FileManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_GOOGLE_CLASS = "org.teiid.resource.adapter.google.SpreadsheetManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_INFINISPAN_CLASS = "org.teiid.resource.adapter.infinispan.InfinispanManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_LDAP_CLASS = "org.teiid.resource.adapter.ldap.LDAPManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_MONGODB_CLASS = "org.teiid.resource.adapter.mongodb.MongoDBManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_SALESORCE_CLASS = "org.teiid.resource.adapter.salesforce.SalesForceManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_WEBSERVICE_CLASS = "org.teiid.resource.adapter.ws.WSManagedConnectionFactory"; //$NON-NLS-1$
	
	public static final String GOOGLE_SOURCE_PROPERTY_KEY_AUTH_METHOD = "AuthMethod"; //$NON-NLS-1$
	public static final String GOOGLE_SOURCE_PROPERTY_VALUE_AUTH_CLIENT_LOGIN = "ClientLogin"; //$NON-NLS-1$
	public static final String GOOGLE_SOURCE_PROPERTY_VALUE_AUTH_OAUTH2 = "OAuth2"; //$NON-NLS-1$
	public static final String GOOGLE_SOURCE_PROPERTY_KEY_REFRESH_TOKEN = "RefreshToken"; //$NON-NLS-1$
	public static final String GOOGLE_SOURCE_PROPERTY_KEY_USERNAME = "Username"; //$NON-NLS-1$
	public static final String GOOGLE_SOURCE_PROPERTY_KEY_PASSWORD = "Password"; //$NON-NLS-1$
	public static final String GOOGLE_SOURCE_PROPERTY_KEY_SPREADSHEET_NAME = "SpreadsheetName"; //$NON-NLS-1$
	public static final String FILE_SOURCE_PROPERTY_KEY_ALLOW_PARENT_PATHS = "AllowParentPaths"; //$NON-NLS-1$
	public static final String FILE_SOURCE_PROPERTY_KEY_FILE_MAPPING = "FileMapping"; //$NON-NLS-1$
	
	public static final String ACCUMULO = "accumulo"; //$NON-NLS-1$
	public static final String ACCESS = "access"; //$NON-NLS-1$
	public static final String DB2 = "db2"; //$NON-NLS-1$
	public static final String DERBY = "derby"; //$NON-NLS-1$
	public static final String FILE = "file"; //$NON-NLS-1$
	public static final String GOOGLE_SPREADSHEET = "google-spreadsheet"; //$NON-NLS-1$
	public static final String H2 = "h2"; //$NON-NLS-1$
	public static final String HIVE = "hive"; //$NON-NLS-1$
	public static final String HSQL = "hsql"; //$NON-NLS-1$
	public static final String INFINISPAN = "infinispan-cache"; //$NON-NLS-1$
	public static final String INFORMIX = "informix"; //$NON-NLS-1$
	public static final String INGRES = "ingres"; //$NON-NLS-1$
	public static final String INGRES93 = "ingres93"; //$NON-NLS-1$
	public static final String INTERSYSTEMS_CACHE = "intersystems-cache"; //$NON-NLS-1$
	public static final String JDBC_ANSI = "jdbc-ansi"; //$NON-NLS-1$
	public static final String JDBC_SIMPLE = "jdbc-simple"; //$NON-NLS-1$
	public static final String JPA2 = "jpa2"; //$NON-NLS-1$
	public static final String LDAP = "ldap"; //$NON-NLS-1$
	public static final String LOOPBACK = "loopback"; //$NON-NLS-1$
	public static final String MAP_CACHE = "map-cache"; //$NON-NLS-1$
	public static final String METAMATRIX = "metamatrix"; //$NON-NLS-1$
	public static final String MODESHAPE = "modeshape"; //$NON-NLS-1$
	public static final String MONGODB = "mongodb"; //$NON-NLS-1$
	public static final String MYSQL = "mysql"; //$NON-NLS-1$
	public static final String MYSQL5 = "mysql5"; //$NON-NLS-1$
	public static final String NETEZZA = "netezza"; //$NON-NLS-1$
	public static final String ODATA = "odata"; //$NON-NLS-1$
	public static final String OLAP = "olap"; //$NON-NLS-1$
	public static final String ORACLE = "oracle"; //$NON-NLS-1$
	// NOTE: For PostgreSQL vendor leaves off the QL, so we need to be careful to map this correctly
	public static final String POSTGRES = "postgres"; //$NON-NLS-1$
	public static final String POSTGRESQL = "postgresql"; //$NON-NLS-1$
	public static final String SALESFORCE = "salesforce"; //$NON-NLS-1$
	public static final String SQLSERVER = "sqlserver"; //$NON-NLS-1$
	public static final String SYBASE = "sybase"; //$NON-NLS-1$
	public static final String TEIID = "teiid"; //$NON-NLS-1$
	public static final String TERADATA = "teradata"; //$NON-NLS-1$
	public static final String WS = "ws"; //$NON-NLS-1$

	public static final String URL_DB2 = "jdbc:db2://<host>:50000/<dbName>"; //$NON-NLS-1$
	public static final String URL_DERBY = "jdbc:derby://<host>:1527/<dbName>;create=true"; //$NON-NLS-1$
	public static final String URL_INFORMIX = "jdbc:informix-sqli://<host>:1526/<dbName>:INFORMIXSERVER=server"; //$NON-NLS-1$
	public static final String URL_INGRES = "jdbc:ingres://<host>:117/<dbName>"; //$NON-NLS-1$
	public static final String URL_MODESHAPE = "jdbc:jcr:http://<host>:8080/modeshape-rest/"; //$NON-NLS-1$
	public static final String URL_MYSQL = "jdbc:mysql://<host>:3306/<dbName>"; //$NON-NLS-1$
	public static final String URL_ORACLETHIN = "jdbc:oracle:thin:@<host>:1521:<dbName>"; //$NON-NLS-1$
	public static final String URL_POSTGRES = "jdbc:postgresql://<host>:5432/<dbName>"; //$NON-NLS-1$
	public static final String URL_SQLSERVER = "jdbc:sqlserver://<host>:1433;databaseName=<dbName>"; //$NON-NLS-1$
	public static final String URL_TEIID = "jdbc:teiid:<vdbName>@mms://<host>:31000"; //$NON-NLS-1$
	public static final String URL_TEIID_LOCAL = "jdbc:teiid:<vdbName>"; //$NON-NLS-1$
	public static final String URL_JDBC = "jdbc://<host>:<port>"; //$NON-NLS-1$
	public static final String URL_HIVE2 = "jdbc:hive2://<host>:10000/<db>";  //$NON-NLS-1$
	public static final String URL_H2 = "jdbc:h2:file:<fileLocation>"; //$NON-NLS-1$
    public static final String URL_SAP_HANA = "jdbc:sap://<host>:<port>"; //$NON-NLS-1$

    public static final String TEIID_WEBSERVICE_PROC = "invokeHttp"; //$NON-NLS-1$
    public static final String TEIID_FILE_PROC = "getTextFiles"; //$NON-NLS-1$
    
	/**
	 * Get the best fit translator, given the driverName and list of translator names
	 * @param driverName the driver name
	 * @param translatorNames the list of current translators
	 * @param teiidVersion the Teiid Version
	 * @return the best fit translator for the provided driver
	 */
	public static String getTranslator(String driverName, Collection<String> translatorNames) {
		if(isEmpty(driverName)) return Constants.STATUS_UNKNOWN;
		if(isEmpty(translatorNames)) return Constants.STATUS_UNKNOWN;

		// -----------------
		// Built-In drivers
		// -----------------
		if(driverName.equals(TEIID_FILE_DRIVER) && translatorNames.contains(FILE)) {
			return FILE;
		}

		if(driverName.equals(TEIID_GOOGLE_DRIVER) && translatorNames.contains(GOOGLE_SPREADSHEET)) {
			return GOOGLE_SPREADSHEET;
		}

		if(driverName.equals(TEIID_INFINISPAN_DRIVER) && translatorNames.contains(INFINISPAN)) {
			return INFINISPAN;
		}

		if(driverName.equals(TEIID_LDAP_DRIVER) && translatorNames.contains(LDAP)) {
			return LDAP;
		}

		if(driverName.equals(TEIID_MONGODB_DRIVER) && translatorNames.contains(MONGODB)) {
			return MONGODB;
		}

		if(driverName.equals(TEIID_SALESORCE_DRIVER) && translatorNames.contains(SALESFORCE)) {
			return SALESFORCE;
		}

		if(driverName.equals(TEIID_WEBSERVICE_DRIVER) && translatorNames.contains(WS)) {
			return WS;
		}
		
		if(driverName.equals(TEIID_ACCUMULO_DRIVER) && translatorNames.contains(ACCUMULO)) {
			return ACCUMULO;
		}

		// ------------------------------------------------------------------------------------------------------------------
		// User-uploaded types.  This 'matching' is more of a problem, since the user can name the driver whatever they want.
		// TODO: Consider adding user input on the driver upload form, so that the driver type can be identified later. 
		// ------------------------------------------------------------------------------------------------------------------
		
	    String driverNameLC = driverName.toLowerCase();
		if(driverNameLC.contains("derby")) { //$NON-NLS-1$
			return DERBY;
		}

		if(driverNameLC.contains("mysql")) { //$NON-NLS-1$
			return MYSQL5;
		}

		if(driverNameLC.contains("ojdbc") || driverNameLC.contains("oracle")) { //$NON-NLS-1$
			return ORACLE;
		}

		if(driverNameLC.contains("db2")) { //$NON-NLS-1$
			return DB2;
		}

		if(driverNameLC.contains("postgr")) { //$NON-NLS-1$
			return POSTGRESQL;
		}

		if(driverNameLC.contains("sqljdbc") || driverNameLC.contains("sqlserver")) { //$NON-NLS-1$
			return SQLSERVER;
		}

		if(driverNameLC.contains("teiid")) { //$NON-NLS-1$
			return TEIID;
		}

		if(driverNameLC.contains("mode")) { //$NON-NLS-1$
			return MODESHAPE;
		}
		
		if(driverNameLC.contains("h2")) { //$NON-NLS-1$
			return H2;
		}
		
	    if(driverNameLC.contains("ifxjdbc") || driverNameLC.contains("informix")) { //$NON-NLS-1$
	        return INFORMIX;
	    }
	    
	    if(driverNameLC.contains("iijdbc") || driverNameLC.contains("ingres")) { //$NON-NLS-1$
	        return INGRES;
	    }
	    
	    if(driverNameLC.contains("hive")) { //$NON-NLS-1$
	        return HIVE;
	    }

		return JDBC_ANSI;
	}

	/**
	 * Test if supplied string is null or zero length
	 * @param text
	 */
	private static boolean isEmpty(String text) {
		return (text == null || text.length() == 0);
	}

	/**
	 * Test if supplied list is null or zero length
	 * @param list
	 */
	private static boolean isEmpty(Collection<String> list) {
		return (list == null || list.size() == 0);
	}

	/**
	 * Get the Driver Name for the supplied class and server version
	 * @param driverClassName the driver class name
	 * @return the driver name
	 */
	public static String getDriverNameForClass(String driverClassName) {
		String driverName = null;
		if(!isEmpty(driverClassName)) {
			if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_FILE_CLASS)) {
				driverName = TranslatorHelper.TEIID_FILE_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_GOOGLE_CLASS)) {
				driverName = TranslatorHelper.TEIID_GOOGLE_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_INFINISPAN_CLASS)) {
				driverName = TranslatorHelper.TEIID_INFINISPAN_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_LDAP_CLASS)) {
				driverName = TranslatorHelper.TEIID_LDAP_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_MONGODB_CLASS)) {
				driverName = TranslatorHelper.TEIID_MONGODB_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_SALESORCE_CLASS)) {
				driverName = TranslatorHelper.TEIID_SALESORCE_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_WEBSERVICE_CLASS)) {
				driverName = TranslatorHelper.TEIID_WEBSERVICE_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_ACCUMULO_CLASS)) {
				driverName = TranslatorHelper.TEIID_ACCUMULO_DRIVER;
			}
		}
		return driverName;
	}

	/**
	 * Get the URL Template given a driver name
	 * @param driverName the driver name
	 * @return the URL Template
	 */
	public static String getUrlTemplate(String driverName) {
		if(isEmpty(driverName)) return Constants.STATUS_UNKNOWN; 

	    String driverNameLC = driverName.toLowerCase();
		if(driverNameLC.contains("derby")) { //$NON-NLS-1$
			return URL_DERBY;
		}

		if(driverNameLC.contains("mysql")) { //$NON-NLS-1$
			return URL_MYSQL;
		}

		if(driverNameLC.contains("ojdbc") || driverNameLC.contains("oracle")) { //$NON-NLS-1$
			return URL_ORACLETHIN;
		}

		if(driverNameLC.contains("db2")) { //$NON-NLS-1$
			return URL_DB2;
		}

		if(driverNameLC.contains("postgr")) { //$NON-NLS-1$
			return URL_POSTGRES;
		}

		if(driverNameLC.contains("sqljdbc") || driverNameLC.contains("sqlserv")) { //$NON-NLS-1$
			return URL_SQLSERVER;
		}

	    if(driverNameLC.contains("ifxjdbc") || driverNameLC.contains("informix")) { //$NON-NLS-1$
			return URL_INFORMIX;
		}

	    if(driverNameLC.contains("iijdbc") || driverNameLC.contains("ingres")) { //$NON-NLS-1$
			return URL_INGRES;
		}

		if(driverNameLC.contains("teiid-local")) { //$NON-NLS-1$
			return URL_TEIID_LOCAL;
		}

		if(driverNameLC.contains("teiid")) { //$NON-NLS-1$
			return URL_TEIID;
		}

		if(driverNameLC.contains("mode")) { //$NON-NLS-1$
			return URL_MODESHAPE;
		}
		
	    if(driverNameLC.contains("hive")) { //$NON-NLS-1$
	        return URL_HIVE2;
	    }
	    
		if(driverNameLC.contains("h2")) { //$NON-NLS-1$
			return URL_H2;
		}
		
        if( driverName.contains("sap") || driverName.contains("hgdbc")) { 
        	return URL_SAP_HANA;
        }

		return URL_JDBC;
	}
		
}
