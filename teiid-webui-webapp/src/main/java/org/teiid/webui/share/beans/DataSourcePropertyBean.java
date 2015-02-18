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
package org.teiid.webui.share.beans;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * A data bean for returning property information for a DataSource. 
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class DataSourcePropertyBean {

    private String name;
    private String displayName;
    private String value;
    private String originalValue;
    private String defaultValue;
    private boolean isModifiable;
    private boolean isRequired;
    private boolean isMasked;
    private String description;

    /**
     * Constructor.
     */
    public DataSourcePropertyBean() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @return the name
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * @return the value
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }
    
    /**
     * @return the value
     */
    public String getOriginalValue() {
        return this.originalValue;
    }

    /**
     * @return the value
     */
    public boolean isModifiable() {
        return this.isModifiable;
    }
    
    /**
     * @return the value
     */
    public boolean isRequired() {
        return this.isRequired;
    }

    /**
     * @return the value
     */
    public boolean isMasked() {
        return this.isMasked;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Determine if this is a 'core' property.  Core property is assumed to be required or other property like username, password, url, etc.
     * @return 'true' if this is a core property, 'false' if not.
     */
    public boolean isCoreProperty() {
    	boolean isCore = false;
		String displayName = getDisplayName();
    	if(this.isRequired) {
    		isCore = true;
    		// If classname - not core - dont want to show up in core properties table
    		if(isClassName(displayName)) {
    			isCore = false;
    		}
    	} else {
    		if(isUserName(displayName) || isPassword(displayName) || isConnectionUrl(displayName)) {
    			isCore = true;
    		}
    	}
    	return isCore;
    }
    
    /*
     * Determine if the supplied property name is ClassName
     * @param displayName the property display name
     * @return 'true' if classname property, 'false' if not.
     */
    private boolean isClassName(String displayName) {
    	boolean found = false;
    	if(displayName!=null) {
    		if(displayName.equalsIgnoreCase("class-name") || displayName.equalsIgnoreCase("Class Name") ) {
    			found = true;
    		}
    	}
    	return found;
    }

    /*
     * Determine if the supplied property name is UserName
     * @param displayName the property display name
     * @return 'true' if username property, 'false' if not.
     */
    private boolean isUserName(String displayName) {
    	boolean found = false;
    	if(displayName!=null) {
    		if(displayName.equalsIgnoreCase("user-name") ||
    				displayName.equalsIgnoreCase("User Name") ||
    				displayName.equalsIgnoreCase("Google Account username") ||
    				displayName.equalsIgnoreCase("Ldap Admin User DN") ||
    				displayName.equalsIgnoreCase("Authentication User Name")) {
    			found = true;
    		}
    	}
    	return found;
    }
    
    /*
     * Determine if the supplied property name is Password
     * @param displayName the property display name
     * @return 'true' if password property, 'false' if not.
     */
    private boolean isPassword(String displayName) {
    	boolean found = false;
    	if(displayName!=null) {
    		if(displayName.equalsIgnoreCase("password") ||
    				displayName.equalsIgnoreCase("Google Account password") ||
    				displayName.equalsIgnoreCase("Ldap Admin Password") ||
    				displayName.equalsIgnoreCase("Authentication User Password")) {
    			found = true;
    		}
    	}
    	return found;
    }
    
    /*
     * Determine if the supplied property name is Connection URL
     * @param displayName the property display name
     * @return 'true' if ConnectionUrl property, 'false' if not.
     */
    private boolean isConnectionUrl(String displayName) {
    	boolean found = false;
    	if(displayName!=null) {
    		if(displayName.equalsIgnoreCase("connection-url") ||
    				displayName.equalsIgnoreCase("Salesforce url") ||
    				displayName.equalsIgnoreCase("Ldap URL") ||
    				displayName.equalsIgnoreCase("Connection url")) {
    			found = true;
    		}
    	}
    	return found;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * @param originalValue the originalValue to set
     */
    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    /**
     * @param isModifiable the isModifiable to set
     */
    public void setModifiable(boolean isModifiable) {
        this.isModifiable = isModifiable;
    }

    /**
     * @param isRequired the isRequired to set
     */
    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    /**
     * @param isMasked the isMasked to set
     */
    public void setMasked(boolean isMasked) {
        this.isMasked = isMasked;
    }
    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
