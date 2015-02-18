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
package org.teiid.webui.share.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.errai.bus.server.annotations.Remote;
import org.teiid.webui.share.beans.DataSourcePageRow;
import org.teiid.webui.share.beans.DataSourcePropertyBean;
import org.teiid.webui.share.beans.DataSourceWithVdbDetailsBean;
import org.teiid.webui.share.beans.TranslatorImportPropertyBean;
import org.teiid.webui.share.beans.VdbDetailsBean;
import org.teiid.webui.share.beans.ViewModelRequestBean;
import org.teiid.webui.share.exceptions.DataVirtUiException;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

/**
 * Provides interface for the Teiid remote services
 *
 * @author mdrillin@redhat.com
 */
@Remote
public interface ITeiidService {

    public List<DataSourcePageRow> getDataSources( final String filters, final String sourceVdbPrefix) throws DataVirtUiException;

    public PageResponse<DataSourcePageRow> getDataSources( final PageRequest pageRequest, final String filters) throws DataVirtUiException;
    		    
    public DataSourceWithVdbDetailsBean getDataSourceWithVdbDetails(String dsName) throws DataVirtUiException;

    public List<String> getDataSourceTypes( ) throws DataVirtUiException;

    public List<DataSourcePropertyBean> getDataSourceTypeProperties(String dsType) throws DataVirtUiException;

    public String getVdbXml(String vdbName) throws DataVirtUiException;
    
    public List<String> getDataSourceNames( ) throws DataVirtUiException;

    public List<String> getTranslators( ) throws DataVirtUiException;

    public Map<String,String> getDefaultTranslatorMap() throws DataVirtUiException;

    public Map<String,List<TranslatorImportPropertyBean>> getImportPropertiesMap(List<String> translators) throws DataVirtUiException;

    public List<TranslatorImportPropertyBean> getTranslatorImportProperties(String translatorName) throws DataVirtUiException;
    	
    public void createDataSourceWithVdb(DataSourceWithVdbDetailsBean dataSourceWithVdb) throws DataVirtUiException;
    
    public void createSourceVdbWithTeiidDS(DataSourceWithVdbDetailsBean bean) throws DataVirtUiException;

    public List<VdbDetailsBean> deleteDataSourceAndVdb(String dsName, String vdbName) throws DataVirtUiException;

    public void deleteDataSources(Collection<String> dsNames) throws DataVirtUiException;

    public List<DataSourcePageRow> deleteDataSourcesAndVdb(Collection<String> dsNames, String vdbName) throws DataVirtUiException;
    
    public void deleteSourcesAndVdbRedeployRenamed(Collection<String> dsNames, String vdbName, DataSourceWithVdbDetailsBean bean) throws DataVirtUiException;

    public void deleteTypes(Collection<String> dsTypes) throws DataVirtUiException;

    public VdbDetailsBean getVdbDetails(String vdbName) throws DataVirtUiException;
    
    public VdbDetailsBean deployNewVDB(final String vdbName, final int vdbVersion, final Map<String,String> vdbPropMap, final ViewModelRequestBean viewModelRequest) throws DataVirtUiException;

    public List<VdbDetailsBean> getDataServiceVdbs( ) throws DataVirtUiException;

    public Collection<String> getAllVdbNames( ) throws DataVirtUiException;
    
    public void deleteDynamicVdbsWithPrefix(String vdbPrefix) throws DataVirtUiException;
    
    public List<VdbDetailsBean> cloneDynamicVdbAddSource(String vdbName, int vdbVersion) throws DataVirtUiException;

}
