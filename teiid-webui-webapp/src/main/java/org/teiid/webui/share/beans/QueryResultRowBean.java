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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * A data bean for returning Query Table and Procedure information. 
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class QueryResultRowBean {

    private List<String> colResults = new ArrayList<String>();

    /**
     * Constructor.
     */
    public QueryResultRowBean() {
    }

    /**
     * @return the Column results
     */
    public List<String> getColumnResults() {
        return colResults;
    }

    /**
     * @param name the name to set
     */
    public void setColumnResults(List<String> colResults) {
        this.colResults = colResults;
    }

}
