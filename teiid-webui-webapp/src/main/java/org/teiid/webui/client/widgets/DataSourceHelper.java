package org.teiid.webui.client.widgets;

import java.util.List;

import org.teiid.webui.share.TranslatorHelper;
import org.teiid.webui.share.beans.DataSourcePropertyBean;

/**
 * Contains common methods for working with DataSource properties
 */
public class DataSourceHelper {

	/**
	 * Check whether the source is considered a 'core' property
	 * @param prop the property item
	 * @param propList the list of all property items
	 * @return 'true' if core property, 'false' if not
	 */
    public static boolean isCoreProperty(DataSourcePropertyBean prop, List<DataSourcePropertyBean> propList) {
    	
    	// If property is marked as core, return
    	if(prop.isCoreProperty()) {
    		return true;
    	// Other specific source properties should be considered 'core'
    	} else {
    		String propName = prop.getName();
    		if( isGoogleSource(propList) && 
    				( propName.equals(TranslatorHelper.GOOGLE_SOURCE_PROPERTY_KEY_SPREADSHEET_NAME) 
    		       || propName.equals(TranslatorHelper.GOOGLE_SOURCE_PROPERTY_KEY_AUTH_METHOD)
    			   || propName.equals(TranslatorHelper.GOOGLE_SOURCE_PROPERTY_KEY_USERNAME)
    			   || propName.equals(TranslatorHelper.GOOGLE_SOURCE_PROPERTY_KEY_PASSWORD)
    			   || propName.equals(TranslatorHelper.GOOGLE_SOURCE_PROPERTY_KEY_REFRESH_TOKEN))) {
    			return true;
    		} else if( isFileSource(propList) && 
    				( propName.equals(TranslatorHelper.FILE_SOURCE_PROPERTY_KEY_ALLOW_PARENT_PATHS) 
    			   || propName.equals(TranslatorHelper.FILE_SOURCE_PROPERTY_KEY_FILE_MAPPING) ) ) {
    			return true;
    		}
    	}
    	
    	return false;
    }

    /**
     * Determine if a source is Google type
     * @param propertyItems the list of properties for the source
     * @return 'true' if google type, 'false' if not
     */
    public static boolean isGoogleSource(List<DataSourcePropertyBean> propertyItems) {
    	boolean isGoogle = false;
    	String classNameValue = getPropertyValue("class-name", propertyItems);  //$NON-NLS-1$
    	if(classNameValue!=null && classNameValue.equalsIgnoreCase(TranslatorHelper.TEIID_GOOGLE_CLASS)) {
    		isGoogle = true;
    	}
    	return isGoogle;
    }
    
    /**
     * Determine if a source is File type
     * @param propertyItems the list of properties for the source
     * @return 'true' if file type, 'false' if not
     */
    private static boolean isFileSource(List<DataSourcePropertyBean> propertyItems) {
    	boolean isFile = false;
    	String classNameValue = getPropertyValue("class-name", propertyItems);  //$NON-NLS-1$
    	if(classNameValue!=null && classNameValue.equalsIgnoreCase(TranslatorHelper.TEIID_FILE_CLASS)) {
    		isFile = true;
    	}
    	return isFile;
    }
    
    /**
     * Gets the property value from the propertyItems for the supplied key.  If a property matching the supplied key is not found,
     * returns null
     * @param propKey the property key
     * @param propertyItems the list of property items
     * @return the property value for the supplied key
     */
    public static String getPropertyValue(String propKey, List<DataSourcePropertyBean> propertyItems) {
    	String propValue = null;
    	for(DataSourcePropertyBean propItem : propertyItems) {
    		String propName = propItem.getName();
    		if(propName!=null && propName.equalsIgnoreCase(propKey)) {
    			propValue = propItem.getValue();
    			break;
    		}
    	}
    	return propValue;
    }
    
}
