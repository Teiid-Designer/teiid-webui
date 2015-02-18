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
package org.teiid.webui.client.services;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.teiid.webui.client.services.rpc.DelegatingErrorCallback;
import org.teiid.webui.client.services.rpc.DelegatingRemoteCallback;
import org.teiid.webui.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.webui.share.beans.QueryColumnBean;
import org.teiid.webui.share.beans.QueryColumnResultSetBean;
import org.teiid.webui.share.beans.QueryResultPageRow;
import org.teiid.webui.share.beans.QueryResultSetBean;
import org.teiid.webui.share.beans.QueryTableProcBean;
import org.teiid.webui.share.exceptions.DataVirtUiException;
import org.teiid.webui.share.services.IQueryService;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

/**
 * Client-side service for making RPC calls to the remote Query service.
 *
 * @author mdrillin@redhat.com
 */
@ApplicationScoped
public class QueryRpcService {

    @Inject
    private Caller<IQueryService> remoteQueryService;

    /**
     * Constructor.
     */
    public QueryRpcService() {
    }

    public void testConnection(final String sourceJndiName, final String dsName, final IRpcServiceInvocationHandler<String> handler) {
        RemoteCallback<String> successCallback = new DelegatingRemoteCallback<String>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);

        remoteQueryService.call(successCallback, errorCallback).testConnection(sourceJndiName,dsName);
    }
    
    public void getDataSourceNames(final boolean teiidOnly, final IRpcServiceInvocationHandler<List<String>> handler) {
        RemoteCallback<List<String>> successCallback = new DelegatingRemoteCallback<List<String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).getDataSourceNames(teiidOnly);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getTablesAndProcedures(final String sourceJndiName, final String sourceName, final IRpcServiceInvocationHandler<List<QueryTableProcBean>> handler) {
        RemoteCallback<List<QueryTableProcBean>> successCallback = new DelegatingRemoteCallback<List<QueryTableProcBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).getTablesAndProcedures(sourceJndiName,sourceName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getQueryColumnResultSet(int page, int pageSize, String filterText, String dataSource, String fullTableName,
            final IRpcServiceInvocationHandler<QueryColumnResultSetBean> handler) {
        // TODO only allow one search at a time.  If another search comes in before the previous one
        // finished, cancel the previous one.  In other words, only return the results of the *last*
        // search performed.
        RemoteCallback<QueryColumnResultSetBean> successCallback = new DelegatingRemoteCallback<QueryColumnResultSetBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).getQueryColumnResultSet(page, pageSize, filterText, dataSource, fullTableName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void executeSql(int page, String dataSource, String sql, final IRpcServiceInvocationHandler<QueryResultSetBean> handler) {
        // TODO only allow one search at a time.  If another search comes in before the previous one
        // finished, cancel the previous one.  In other words, only return the results of the *last*
        // search performed.
        RemoteCallback<QueryResultSetBean> successCallback = new DelegatingRemoteCallback<QueryResultSetBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).executeSql(page, dataSource, sql);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getColumns(final String dataSource, final String sql, final IRpcServiceInvocationHandler<List<QueryColumnBean>> handler) {
        RemoteCallback<List<QueryColumnBean>> successCallback = new DelegatingRemoteCallback<List<QueryColumnBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).getColumns(dataSource,sql);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getQueryResults(final PageRequest request, final String dataSource, final String sql, final IRpcServiceInvocationHandler<PageResponse<QueryResultPageRow>> handler) {
        RemoteCallback<PageResponse<QueryResultPageRow>> successCallback = new DelegatingRemoteCallback<PageResponse<QueryResultPageRow>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).getQueryResults(request,dataSource,sql);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
}
