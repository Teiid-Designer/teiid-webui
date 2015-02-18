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
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Models the full details of a Data Source.
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class DataSourceDetailsBean extends DataSourceSummaryBean implements Serializable {

    private static final long serialVersionUID = DataSourceDetailsBean.class.hashCode();

    private String updatedBy;
    private String version;
    private List<DataSourcePropertyBean> properties = new ArrayList<DataSourcePropertyBean>();

	/**
     * Constructor.
     */
    public DataSourceDetailsBean() {
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

    public List<DataSourcePropertyBean> getProperties() {
		return properties;
	}

	public void setProperties(List<DataSourcePropertyBean> properties) {
		this.properties = properties;
	}
	    
}
