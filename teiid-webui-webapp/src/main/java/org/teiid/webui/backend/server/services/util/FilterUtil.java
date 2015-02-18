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
package org.teiid.webui.backend.server.services.util;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.teiid.webui.share.services.StringUtils;

public class FilterUtil {

	/**
	 * Tests if the given string matches the filter.
	 *
	 * @param inputStr
	 * @param filter
	 * @return true if matches and false if either null or no match
	 */
	public static boolean matchFilter(String inputStr, String filter) {

		if (inputStr == null)
			return false;
		
		if(StringUtils.isEmpty(filter)) {
			filter = "*";
		} else {
			if(!filter.endsWith("*")) {
				filter += "*";
			}
		}

		StringBuffer f = new StringBuffer();

		for (StringTokenizer st = new StringTokenizer(filter, "?*", true); st.hasMoreTokens();) {
			String t = st.nextToken();
			if (t.equals("?"))
				f.append(".");
			else if (t.equals("*"))
				f.append(".*");
			else
				f.append(Pattern.quote(t));
		}
		return inputStr.matches(f.toString());
	}


}
