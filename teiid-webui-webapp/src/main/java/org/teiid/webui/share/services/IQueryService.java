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

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.teiid.webui.share.beans.QueryColumnBean;
import org.teiid.webui.share.beans.QueryColumnResultSetBean;
import org.teiid.webui.share.beans.QueryResultPageRow;
import org.teiid.webui.share.beans.QueryResultSetBean;
import org.teiid.webui.share.beans.QueryTableProcBean;
import org.teiid.webui.share.exceptions.DataVirtUiException;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

/**
 * Provides service for running queries against jdbc sources on the server
 *
 * @author mdrillin@redhat.com
 */
@Remote
public interface IQueryService {

    public String testConnection(String dataSourceJndiName,String dsName);
    
    public List<String> getDataSourceNames(boolean teiidOnly) throws DataVirtUiException;

    public List<QueryTableProcBean> getTablesAndProcedures(String dataSourceJndiName, String dsName) throws DataVirtUiException;

    public QueryColumnResultSetBean getQueryColumnResultSet(int page, int pageSize, String filterText, String dataSource, String fullTableName) throws DataVirtUiException;

    public QueryResultSetBean executeSql(int page, String dataSource, String sql) throws DataVirtUiException;

    public List<QueryColumnBean> getColumns( String dataSource, String sql ) throws DataVirtUiException;

    public PageResponse<QueryResultPageRow> getQueryResults( final PageRequest pageRequest, String dataSource, String sql ) throws DataVirtUiException;

}
