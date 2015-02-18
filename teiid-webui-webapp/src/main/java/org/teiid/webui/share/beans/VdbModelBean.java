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

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.teiid.webui.share.Constants;

/**
 * A data bean for returning information for a VDB Model.
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class VdbModelBean {

    private String name;
    private String description;
    private String status;
    private String type;
    private String translator;
    private Map<String,String> importProps = new HashMap<String,String>();
    private String jndiSource;
    private String ddl;
    private boolean isVisible = false;

	/**
     * Constructor.
     */
    public VdbModelBean() {
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
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Determine if the model is a source model
     */
    public boolean isSource() {
    	boolean isSource = false;
    	if(Constants.MODEL_TYPE_PHYSICAL.equalsIgnoreCase(getType())) {
    		isSource = true;
    	}
    	return isSource;
    }
    
    /**
     * Determine if the model is a view model
     */
    public boolean isView() {
    	boolean isView = false;
    	if(Constants.MODEL_TYPE_VIRTUAL.equalsIgnoreCase(getType())) {
    		isView = true;
    	}
    	return isView;
    }
    
    /**
     * @return the translator
     */
    public String getTranslator() {
        return translator;
    }
    
    public Map<String,String> getImportProperties() {
		return importProps;
	}

	/**
     * @return the jndiSource
     */
    public String getJndiSource() {
        return jndiSource;
    }
    
    public String getDdl() {
		return ddl;
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
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param translator the translator to set
     */
    public void setTranslator(String translator) {
        this.translator = translator;
    }

    /**
     * @param props the properties to set
     */
	public void setImportProperties(Map<String,String> propMap) {
		this.importProps.clear();
		this.importProps.putAll(propMap);
	}

    /**
     * @param type the type to set
     */
    public void setJndiSource(String jndiSource) {
        this.jndiSource = jndiSource;
    }

	public void setDdl(String ddl) {
		this.ddl = ddl;
	}

    public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
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
