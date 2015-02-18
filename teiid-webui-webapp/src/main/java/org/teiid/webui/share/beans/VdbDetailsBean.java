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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Models the full details of a VDB
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class VdbDetailsBean extends VdbSummaryBean implements Serializable {

    private static final long serialVersionUID = VdbDetailsBean.class.hashCode();

    private String updatedBy;
    private String version;
    private Collection<VdbModelBean> models = new ArrayList<VdbModelBean>();
    private List<String> importedVdbNames = new ArrayList<String>();
	private int modelsPerPage;
    private int startIndex;
    private int endIndex;

    /**
     * Constructor.
     */
    public VdbDetailsBean() {
    }

    /**
     * @return the updatedBy
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy the updatedBy to set
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * @return the vdb models
     */
    public Collection<VdbModelBean> getModels() {
        return models;
    }

    /**
     * @return the vdb models names
     */
    public Collection<String> getModelNames() {
    	Collection<String> allNames = new ArrayList<String>(models.size());
        for(VdbModelBean model : models) {
        	allNames.add(model.getName());
        }
    	return allNames;
    }
    
    /**
     * @return the vdb model with the supplied name.  If not found, returns null
     * @param modelName the model name
     * @return the VdbModelBean
     */
    public VdbModelBean getModel(String modelName) {
    	VdbModelBean resultModel = null;
    	
    	Collection<VdbModelBean> models = getModels();
    	for(VdbModelBean aModel : models) {
    		if(aModel.getName().equalsIgnoreCase(modelName)) {
    			resultModel = aModel;
    			break;
    		}
    	}
    	
        return resultModel;
    }

    /**
     * Sets the classified by.
     * @param classifiedBy
     */
    public void setModels(Collection<VdbModelBean> models) {
        this.models = models;
    }

    /**
     * @param classification
     */
    public void addModel(VdbModelBean vdbModel) {
    	models.add(vdbModel);
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    public List<String> getImportedVdbNames() {
		return importedVdbNames;
	}

	public void setImportedVdbNames(List<String> importedVdbNames) {
		this.importedVdbNames = importedVdbNames;
	}
	
    /**
     * @return the total number of Models
     */
    public int getTotalModels() {
        return models.size();
    }

    /**
     * @return the modelsPerPage
     */
    public int getModelsPerPage() {
        return modelsPerPage;
    }

    /**
     * @param modelsPerPage the modelsPerPage to set
     */
    public void setModelsPerPage(int modelsPerPage) {
        this.modelsPerPage = modelsPerPage;
    }

    /**
     * @return the startIndex
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * @param startIndex the startIndex to set
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * @return the endIndex
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * @param endIndex the endIndex to set
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

}
