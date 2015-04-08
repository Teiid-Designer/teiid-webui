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

import org.jboss.errai.common.client.api.annotations.Portable;
import org.uberfire.paging.AbstractPageRow;

/**
 * A single row of a paged data
 */
@Portable
public class DataSourcePageRow extends AbstractPageRow {

    private String name;
    private String type;
    private State deploymentState = State.OK;
    private String errorMessage;

    public enum State {
        DEPLOYING, 
        OK,
        ERROR,
        NO_MATCHING_VDB,
        PLACEHOLDER
    }
    
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }

	public void setState(State dsState) {
		this.deploymentState = dsState;
	}
	
	public State getState() {
		return this.deploymentState;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String message) {
		this.errorMessage = message;
	}

}
