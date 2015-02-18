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
 * Details of the source, including corresponding src VDB info (if source exists).  
 * In addition to the Server Source Connection, includes
 * 1) translator and 2) source VDB name
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class DataSourceWithVdbDetailsBean extends DataSourceDetailsBean implements Serializable {

    private static final long serialVersionUID = DataSourceWithVdbDetailsBean.class.hashCode();

    private String sourceVdbName;
    private String translator;
    private List<TranslatorImportPropertyBean> importProperties = new ArrayList<TranslatorImportPropertyBean>();
    
	public String getSourceVdbName() {
		return sourceVdbName;
	}
	public void setSourceVdbName(String sourceVdbName) {
		this.sourceVdbName = sourceVdbName;
	}
	public String getTranslator() {
		return translator;
	}
	public void setTranslator(String translator) {
		this.translator = translator;
	}    
    public List<TranslatorImportPropertyBean> getImportProperties() {
		return importProperties;
	}
	public void setImportProperties(List<TranslatorImportPropertyBean> properties) {
		this.importProperties = properties;
	}
	    
}
