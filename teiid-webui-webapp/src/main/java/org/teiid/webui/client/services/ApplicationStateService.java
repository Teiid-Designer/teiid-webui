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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

/**
 * Local service responsible for holding application state.
 * 
 */
@ApplicationScoped
public class ApplicationStateService {

	private Map<String, Object> state = new HashMap<String, Object>();

	/**
	 * Constructor.
	 */
	public ApplicationStateService() {
	}

	/**
	 * Gets application state by key.
	 * @param key
	 */
	public Object get(String key) {
		return state.get(key);
	}

	/**
	 * Gets the application state by key, returning the given default if
	 * not found in the state map.
	 * @param key
	 * @param defaultValue
	 */
	public Object get(String key, Object defaultValue) {
		Object value = get(key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Store some application state by key.
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value) {
		state.put(key, value);
	}
}
