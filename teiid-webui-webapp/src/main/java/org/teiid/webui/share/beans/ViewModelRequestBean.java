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

/**
 * ViewModelRequest
 * Object for passing the requested view model properties
 *
 */
public class ViewModelRequestBean {
	private String name;
	private String description;
	private String ddl;
	private boolean isVisible = false;
	private List<String> requiredImportVdbNames = new ArrayList<String>();
	private List<Integer> requiredImportVdbVersions = new ArrayList<Integer>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDdl() {
		return ddl;
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
	public List<String> getRequiredImportVdbNames() {
		return requiredImportVdbNames;
	}
	public void setRequiredImportVdbNames(List<String> requiredImportVdbNames) {
		this.requiredImportVdbNames = requiredImportVdbNames;
		
		// For now, assuming versions are all 1
		this.requiredImportVdbVersions.clear();
		for(int i=0; i<requiredImportVdbNames.size(); i++) {
			this.requiredImportVdbVersions.add(1);
		}
	}	
	public List<Integer> getRequiredImportVdbVersions() {
		return requiredImportVdbVersions;
	}
	public void setRequiredImportVdbVersions(List<Integer> requiredImportVdbVersions) {
		this.requiredImportVdbVersions = requiredImportVdbVersions;
	}	
}
