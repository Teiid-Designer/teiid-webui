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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.teiid.webui.share.Constants;

/**
 * A data bean for returning summary information for a VDB.
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class VdbSummaryBean {

    private String model;
    private String type;
    private String status;
    private String name;
    private String description;
    private String createdBy;
    private Date createdOn;
    private Date updatedOn;
    private Map<String, String> properties = new HashMap<String, String>();
    private boolean testable = false;
    private String serverHost;
    
    /**
     * Constructor.
     */
    public VdbSummaryBean() {
    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    
    public boolean isDynamic() {
    	boolean isDyn = false;
    	String vdbType = getType();
    	if(vdbType.equalsIgnoreCase("dynamic")) {
    		isDyn = true;
    	}
    	return isDyn;
    }
    
    public boolean isActive() {
    	boolean isActive = false;
    	String status = getStatus();
    	if(Constants.STATUS_ACTIVE.equalsIgnoreCase(status)) {
    		isActive = true;
    	}
    	return isActive;
    }
    
    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * @return the updatedOn
     */
    public Date getUpdatedOn() {
        return updatedOn;
    }

    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * @return the isTestable state
     */
    public boolean isTestable() {
        return testable;
    }
    
    /**
     * Get the server hostname this vdb is running on
     * @return the server hostname
     */
    public String getServerHost() {
        return serverHost;
    }

    /**
     * @param model the model to set
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * @param updatedOn the updatedOn to set
     */
    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Gets a property by name.
     * @param propName
     */
    public String getProperty(String propName) {
        return this.properties.get(propName);
    }

    /**
     * Sets a property.
     * @param propName
     * @param propVal
     */
    public void setProperty(String propName, String propVal) {
        this.properties.put(propName, propVal);
    }

    /**
     * @return the names of all the custom properties
     */
    public Set<String> getPropertyNames() {
        return this.properties.keySet();
    }

    /**
     * @param testable 'true' if testable on test page
     */
    public void setTestable(boolean testable) {
        this.testable = testable;
    }
    
    /**
     * Set the server hostname this vdb is running on
     * @return the server hostname
     */
    public void setServerHost(String hostName) {
        this.serverHost = hostName;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        return result;
    }

}
