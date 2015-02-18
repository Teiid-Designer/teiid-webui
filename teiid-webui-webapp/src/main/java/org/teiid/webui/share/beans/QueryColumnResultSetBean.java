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

/**
 * Models the set of QueryColumns returned by a retrieval.
 *
 * @author mdrillin@redhat.com
 */
@Portable
public class QueryColumnResultSetBean implements Serializable {

    private static final long serialVersionUID = QueryColumnResultSetBean.class.hashCode();

    private List<QueryColumnBean> queryColumns = new ArrayList<QueryColumnBean>();
    private long totalResults;
    private int itemsPerPage;
    private int startIndex;

    /**
     * Constructor.
     */
    public QueryColumnResultSetBean() {
    }

    /**
     * @return the Query columns
     */
    public List<QueryColumnBean> getQueryColumns() {
        return queryColumns;
    }

    /**
     * @param queryColumns the query columns to set
     */
    public void setQueryColumns(List<QueryColumnBean> queryColumns) {
        this.queryColumns = queryColumns;
    }
    
    /**
     * @return the totalResults
     */
    public long getTotalResults() {
        return totalResults;
    }

    /**
     * @param totalResults the totalResults to set
     */
    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    /**
     * @return the itemsPerPage
     */
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    /**
     * @param itemsPerPage the itemsPerPage to set
     */
    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
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

}
