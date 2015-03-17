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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.jboss.as.controller.client.ModelControllerClient;
import org.teiid.adminapi.Admin;
import org.teiid.adminapi.AdminException;
import org.teiid.adminapi.AdminFactory;
import org.teiid.adminapi.PropertyDefinition;
import org.teiid.adminapi.Translator;
import org.teiid.adminapi.VDB;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.webui.backend.server.services.util.VdbHelper;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.TranslatorHelper;
import org.teiid.webui.share.services.StringUtils;

/**
 * Class used to communicate with the Teiid Server Admin API
 *
 * @author mdrillin@redhat.com
 */
public class AdminApiClient {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String DRIVER_KEY = "driver-name";
    private static final String CLASSNAME_KEY = "class-name";
    private static final String JNDINAME_KEY = "jndi-name";

	private Admin admin;
	private boolean validating;
//	private AuthenticationProvider authProvider;
	private Locale locale;
    private String serverHost;

    /**
     * Constructor.
     * @param endpoint
     * @param username
     * @param password
     * @param validating
     * @throws AdminApiClientException
     */
    public AdminApiClient(final String username, final String password, final boolean validating) throws AdminApiClientException {
        //this(new BasicAuthenticationProvider(), validating);
        this(validating);
    }

    /**
     * Constructor.
     * @param endpoint
     * @param authenticationProvider
     * @param validating
     * @throws AdminApiClientException
     */
    public AdminApiClient(final boolean validating) throws AdminApiClientException {
        // Establish serverHost this is running on
        if(this.serverHost==null) establishServerHost();

        //this.authProvider = authenticationProvider;
        this.validating = validating;
        if (this.validating) {
			try {
				// Currently the user/password can be anything - does not authenticate...
	        	this.admin = getAdminApi(this.serverHost,9999,"admin","admin");
			} catch (Exception e) {
				throw new AdminApiClientException(e.getMessage());
			}
//            discoverAvailableFeatures();
        }
    }
	
    /**
     * Get an admin api connection with the supplied credentials
     * @param serverHost the server hostname
     * @param serverPort the server port number
     * @param userName the username
     * @param password the password
     * @return the admin api
     */
    private Admin getAdminApi (String serverHost, int serverPort, String userName, String password) throws Exception {
            Admin admin = null;
            try {
                    //admin = AdminFactory.getInstance().createAdmin(serverHost, serverPort, userName, password.toCharArray());
                    admin = AdminFactory.getInstance().createAdmin(ModelControllerClient.Factory.create(serverHost, serverPort));
            } catch (Exception e) {
                    throw new Exception(e.getMessage());
            }
            if(admin==null) {
                    StringBuffer sb = new StringBuffer("Unable to establish Admin API connection. Please check the supplied credentials: \n");
                    sb.append("\n [Host]: "+serverHost);
                    sb.append("\n [Port]: "+serverPort);
                    
                    throw new Exception(sb.toString());
            }
         
            return admin;
    }
    
    /**
     * Establish the serverHost this is running on. Only need to do this once.
     */
    private void establishServerHost() {
    	serverHost = LOCALHOST;

    	// First priority is use OpenShift - if running on OpenShift.
    	// Try both the JBOSSEAP and JBOSSAS system vars. Also try OPENSHIFT_TEIID_ID and OPENSHIFT_DV_IP (Cartridges)
    	String serverIP = System.getenv("OPENSHIFT_JBOSSEAP_IP");
    	if(serverIP==null || serverIP.trim().isEmpty()) {
    		serverIP = System.getenv("OPENSHIFT_JBOSSAS_IP");
    	}
    	if(serverIP==null || serverIP.trim().isEmpty()) {
    		serverIP = System.getenv("OPENSHIFT_TEIID_IP");
    	}
    	if(serverIP==null || serverIP.trim().isEmpty()) {
    		serverIP = System.getenv("OPENSHIFT_DV_IP");
    	}

    	// If no OpenShift detected, check if management port has been set
    	if(serverIP==null || serverIP.trim().isEmpty()) {
    		serverIP = System.getProperty("jboss.management.native.port");
    	}

    	// If management port has been set, it overrides LOCALHOST (127.0.0.1)
    	// If management port has NOT been set, defaults to LOCALHOST
    	if(serverIP!=null && !serverIP.trim().isEmpty()) {
    		serverHost = serverIP;
    	}
    }

    /*
     * Get the Collection of DataSource Summary properties
     * @return the Map of DataSource name - type
     */
	public Collection<Properties> getDataSourceSummaryPropsCollection() throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		Collection<Properties> dsSummaryPropCollection = new ArrayList<Properties>();
		
		// Get Collection of DataSource Names
		Collection<String> sourceNames = getDataSourceNames();
		// For each datasource, get the 'summary' properties
		for(String sourceName : sourceNames) {
			Properties summaryProperties = new Properties();
			// DataSource name
			summaryProperties.put("name", sourceName);
			
			Properties dsProps = getDataSourceProperties(sourceName);
			
			// DataSource jndiName
			String jndiName = getDataSourceJndiName(dsProps); 
			if(jndiName!=null) {
				summaryProperties.put("jndi-name", jndiName);
			}
			
			// DataSource type
			String sourceType = getDataSourceType(dsProps);
			if(sourceType!=null) {
				summaryProperties.put("type",sourceType);
			}
			
			dsSummaryPropCollection.add(summaryProperties);
		}
		
		return dsSummaryPropCollection;
	}
	
    /**
     * Create a Data Source
     * @param deploymentName the name of the deployment
     * @param templateName the source template name
     * @param props the datasource properties
     */
	public void createDataSource(String deploymentName, String templateName, Properties props) throws AdminApiClientException {
		if(this.admin!=null) {
			try {
				this.admin.createDataSource(deploymentName, templateName, props);
			} catch (AdminException e) {
				throw new AdminApiClientException(e.getMessage());
			}
		}
	}
	
    /**
     * Get the current Collection of DataSource names
     * @return the collection of DataSource names
     */
	public Collection<String> getDataSourceNames() throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of DataSource Names
		Collection<String> sourceNames = null;
		try {
			sourceNames = this.admin.getDataSourceNames();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		return (sourceNames!=null) ? sourceNames : Collections.<String>emptyList();
	}

	/*
     * Get the current Collection of Translators
     * @return the collection of translators
     */
	public Collection<String> getTranslatorNames() throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of Translators
		Collection<? extends Translator> translators = null;
		try {
			translators = this.admin.getTranslators();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

        // return the names
		List<String> transNames = new ArrayList<String>(translators.size());
		for(Translator translator : translators) {
			if(translator!=null) {
				transNames.add(translator.getName());
			}
		}

		return transNames;
	}
	
	/*
     * Get the current Collection of Translator Import property definitions
     * @return the collection of translator import properties
     */
	public Collection<? extends PropertyDefinition> getTranslatorImportProperties(String translatorName) throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of Translators
		Collection<? extends PropertyDefinition> importProps = null;
		try {
			importProps = this.admin.getTranslatorPropertyDefinitions(translatorName, Admin.TranlatorPropertyType.IMPORT);
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		if(importProps!=null) {
			return importProps;
		} else {
			return Collections.emptyList();
		}
	}
	
    /*
     * Get the Properties for the supplied DataSource name
     * @param dataSourceName the name of the datasource
     * @return the properties for the DataSource names
     */
	public Properties getDataSourceProperties(String dataSourceName) throws AdminApiClientException {
		if(this.admin==null) return null;

		// Get the DataSource properties
		Properties dsProps = null;
		try {
			dsProps = admin.getDataSource(dataSourceName);
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		return dsProps;
	}
	
	/**
	 * Get a Map of default translators for the dataSources
	 * @return the dsName to default translator mappings
	 */
	public Map<String,String> getDefaultTranslatorMap( ) throws AdminApiClientException {
		Map<String,String> defaultTranslatorsMap = new HashMap<String,String>();

		Collection<String> sourceNames = getDataSourceNames();
		Collection<String> translators = getTranslatorNames();

		for(String sourceName : sourceNames) {
			String dsDriver = getDataSourceType(sourceName);
			String defaultTranslator = TranslatorHelper.getTranslator(dsDriver,translators);
			defaultTranslatorsMap.put(sourceName,defaultTranslator);
		}
		return defaultTranslatorsMap;
	}
    
    /**
     * Get the Type(driver) for the supplied DataSource name
     * @param dataSourceName the name of the data source
     * @return the DataSource type
     */
	public String getDataSourceType(String dataSourceName) throws AdminApiClientException {
		if(this.admin==null) return null;

		// Get the DataSource properties
		Properties dsProps = getDataSourceProperties(dataSourceName);
		
		return getDataSourceType(dsProps);
	}
	
	/*
	 * Get the source type from the provided properties
	 * @param dsProps the data source properties
	 * @return the dataSource type name
	 */
	private String getDataSourceType(Properties dsProps) {
		if(dsProps==null) return Constants.STATUS_UNKNOWN;

		String driverName = dsProps.getProperty(DRIVER_KEY);
		// If driver-name not found, look for class name and match up the .rar
		if(StringUtils.isEmpty(driverName)) {
			String className = dsProps.getProperty(CLASSNAME_KEY);
			if(!StringUtils.isEmpty(className)) {
				driverName = TranslatorHelper.getDriverNameForClass(className);
			}
		}
		return driverName;
	}

	/*
	 * Get the source jndiName from the provided properties
	 * @param dsProps the data source properties
	 * @return the dataSource jndi name
	 */
	private String getDataSourceJndiName(Properties dsProps) {
		if(dsProps==null) return Constants.STATUS_UNKNOWN;

		return dsProps.getProperty(JNDINAME_KEY);
	}
	    
    /*
     * Get the current Collection of DataSource names
     * @return the collection of DataSource names
     */
	public Collection<String> getDataSourceTypes() throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of DataSource Names
		Collection<String> sourceTypes = null;
		try {
			sourceTypes = this.admin.getDataSourceTemplateNames();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		if(sourceTypes!=null) {
			return sourceTypes;
		} else {
			return Collections.emptyList();
		}
	}

	public Collection<? extends PropertyDefinition> getDataSourceTypePropertyDefns(String typeName) throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of DataSource Names
		Collection<? extends PropertyDefinition> propDefns = null;
		try {
			propDefns = this.admin.getTemplatePropertyDefinitions(typeName);
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		if(propDefns!=null) {
			return propDefns;
		} else {
			return Collections.emptyList();
		}
	}

	/*
     * Delete the supplied DataSource. 
     * @param dataSourceName the DataSource name
     */
	public void deleteDataSource(String dataSourceName) throws AdminApiClientException {
		if(this.admin==null) return;

		// Get list of DataSource Names. If 'sourceName' is found, delete it...
		Collection<String> dsNames;
		try {
			dsNames = admin.getDataSourceNames();
		} catch (AdminException e1) {
			throw new AdminApiClientException(e1.getMessage());
		}
		if(dsNames.contains(dataSourceName)) {
			try {
				// Undeploy the working VDB
				admin.deleteDataSource(dataSourceName);
			} catch (Exception e) {
				throw new AdminApiClientException(e.getMessage());
			}
		}
	}

	/*
     * Delete the supplied list of DataSources
     * @param dataSourceNames the collection of datasources
     */
	public void deleteDataSources(Collection<String> dataSourceNames) throws AdminApiClientException {
		for(String dsName : dataSourceNames) {
			deleteDataSource(dsName);
		}
	}
	
	/*
	 * Delete the supplied list of DataSource types
	 * @param dataSourceTypeNames the collection of datasource types
	 */
	public void deleteDataSourceTypes(Collection<String> dataSourceTypeNames) throws AdminApiClientException {
		if(this.admin==null) return;

		// Delete the specified DataSource types
		try {
			for(String dsTypeName : dataSourceTypeNames) {
				// Get current list of Drivers. If 'driverName' is found, undeploy it...
				Collection<String> currentTypeNames = (Collection<String>) this.admin.getDataSourceTemplateNames();

				if(currentTypeNames.contains(dsTypeName)) {
					admin.undeploy(dsTypeName);
				}
			}
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}
	}
	
	/*
     * Deploy the supplied content
     * @param deploymentName the deployment name
     * @param content the deployment content
     */
	public void deploy(String deploymentName, InputStream content) throws AdminApiClientException {
		if(this.admin==null) return;

		// Delete the specified DataSource types
		try {
			// Undeploy the datasource type
			admin.deploy(deploymentName, content);
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}
	}
	
	/*
     * Undeploy the supplied content
     * @param deploymentName the deployment name
     */
	public void undeploy(String deploymentName) throws AdminApiClientException {
		if(this.admin==null) return;

		// Delete the specified DataSource types
		try {
			// Undeploy the datasource type
			admin.undeploy(deploymentName);
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}
	}
	
    /*
     * Get the current List of DataService Vdb names
     * @return the list of DataService Vdb names
     */
	public List<String> getDataServiceVdbNames( ) throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of VDB Names
		Collection<? extends VDB> vdbs = null;
		try {
			vdbs = this.admin.getVDBs();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		if(vdbs!=null) {
			// Get VDB names
			List<String> vdbNames = new ArrayList<String>();
			for(VDB vdb : vdbs) {
				String serviceViewName = vdb.getPropertyValue(Constants.VDB_PROP_KEY_DATASERVICE_VIEWNAME);
				if(!StringUtils.isEmpty(serviceViewName)) {
					String vdbName = ((VDBMetaData)vdb).getName();
					vdbNames.add(vdbName);
				}
			}
			return vdbNames;
		} else {
			return Collections.emptyList();
		}
	}
	
    /*
     * Get the current Collection of Vdb names
     * @return the collection of Vdb names
     */
	public Collection<String> getVdbNames(boolean includeDynamic, boolean includeArchive, boolean includePreview) throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of VDB Names
		Collection<? extends VDB> vdbs = null;
		try {
			vdbs = this.admin.getVDBs();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		if(vdbs!=null) {
			// Get VDB names
			Collection<String> vdbNames = new ArrayList<String>();
			for(VDB vdb : vdbs) {
				VDBMetaData vdbMeta = (VDBMetaData)vdb;
				String vdbName = vdbMeta.getName();
				boolean isDynamic = vdbMeta.isXmlDeployment();
				boolean isPreview = vdbMeta.isPreview();
				
				// Dynamic VDB
				if(isDynamic) {
					if(includeDynamic) vdbNames.add(vdbName);
				// Archive VDB
				} else if(includeArchive) {
					if(!isPreview) {
						vdbNames.add(vdbName);
					} else if(includePreview) {
						vdbNames.add(vdbName);
					}
				}
			}
			return vdbNames;
		} else {
			return Collections.emptyList();
		}
	}
	
    /*
     * Get the current Collection of Vdb properties
     * @return the collection of Vdb properties
     */
	public Collection<Properties> getVdbSummaryPropCollection(boolean includeDynamic, boolean includeArchive, boolean includePreview) throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of VDB Names
		Collection<? extends VDB> vdbs = getVdbs();

		if(vdbs!=null) {
			VdbHelper vdbHelper = VdbHelper.getInstance();
			// Get VDB names
			Collection<Properties> vdbSummaryProps = new ArrayList<Properties>();
			for(VDB vdb : vdbs) {
				VDBMetaData vdbMeta = (VDBMetaData)vdb;
				String vdbName = vdbMeta.getName();
				boolean isDynamic = vdbMeta.isXmlDeployment();
				boolean isPreview = vdbMeta.isPreview();
				
				String vdbStatus = vdbHelper.getVdbStatus(vdbMeta);
				
				// Dynamic VDB
				if(isDynamic) {
					if(includeDynamic) {
						Properties vdbSummary = new Properties();
						vdbSummary.put("name", vdbName);
						vdbSummary.put("type", "dynamic");
						vdbSummary.put("status", vdbStatus);
						vdbSummaryProps.add(vdbSummary);
					}
				// Archive VDB
				} else if(includeArchive) {
					if(!isPreview) {
						Properties vdbSummary = new Properties();
						vdbSummary.put("name", vdbName);
						vdbSummary.put("type", "archive");
						vdbSummary.put("status", vdbStatus);
						vdbSummaryProps.add(vdbSummary);
					} else if(includePreview) {
						Properties vdbSummary = new Properties();
						vdbSummary.put("name", vdbName);
						vdbSummary.put("type", "archive");
						vdbSummary.put("status", vdbStatus);
						vdbSummaryProps.add(vdbSummary);
					}
				}
			}
			return vdbSummaryProps;
		} else {
			return Collections.emptyList();
		}
	}
	
	public Collection<? extends VDB> getVdbs() throws AdminApiClientException {
		// Get list of VDB Names
		Collection<? extends VDB> vdbs = null;
		try {
			vdbs = this.admin.getVDBs();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}
		return vdbs;
	}
	
	public VDBMetaData getVDB(String vdbName, int vdbVersion) throws AdminApiClientException {
		// Get list of VDBS - get the named VDB
		Collection<? extends VDB> vdbs = getVdbs();

		VDBMetaData vdb = null;
		for(VDB aVdb : vdbs) {
			VDBMetaData vdbMeta = (VDBMetaData)aVdb;
			if(vdbMeta.getName()!=null && vdbMeta.getName().equalsIgnoreCase(vdbName) && vdbMeta.getVersion()==vdbVersion) {
				vdb = vdbMeta;
				break;
			}
		}
		return vdb;
	}

    /*
     * Delete the Dynamic VDBs.  This just undeploys the VDB - does *not* undeploy corresponding datasources, etc.
     * @param vdbName name of the VDB to delete
     */
	public void deleteVDB(String vdbName) throws AdminApiClientException {
		VDBMetaData vdb = getVDB(vdbName,1);
		if(vdb==null) return;
		
		// Get the VDB deployment name
		String deploymentName = VdbHelper.getInstance().getVdbDeploymentName(vdb);
				
		// Undeploy
		if(deploymentName!=null) {                        
			try {
				// Undeploy the VDB
				this.admin.undeploy(deploymentName);
			} catch (Exception e) {
				throw new AdminApiClientException(e.getMessage());
			}
		}
	}
	
    /*
     * Delete the Collection of Dynamic VDBs.  This just undeploys the VDB - does *not* undeploy corresponding datasources, etc.
     * @param vdbName name of the VDB to delete
     */
	public void deleteVDBs(Collection<String> vdbNames) throws AdminApiClientException {
		for(String vdbName : vdbNames) {
			deleteVDB(vdbName);
		}
	}
        
    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

}
