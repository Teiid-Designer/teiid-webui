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

import java.util.Comparator;


/*
 * Comparator for ordering the VdbModelBeans
 */
public class VdbModelBeanComparator implements Comparator<VdbModelBean> {

	boolean ascending = true;
	
	public VdbModelBeanComparator(boolean ascending) {
		this.ascending = ascending;
	}
	
	public int compare(VdbModelBean s1, VdbModelBean s2) {
		String s1Name = s1.getName();
		String s2Name = s2.getName();
		
		if(ascending) {
			return s1Name.compareToIgnoreCase(s2Name);
		}
		return s2Name.compareToIgnoreCase(s1Name);
	}        

}
