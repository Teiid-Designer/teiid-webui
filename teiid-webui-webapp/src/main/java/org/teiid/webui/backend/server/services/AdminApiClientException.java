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
package org.teiid.webui.backend.server.services;

/**
 * Exception thrown by the Admin API client when an error of some kind is encountered.
 *
 * @author mdrillin@redhat.com
 */
public class AdminApiClientException extends Exception {

	private static final long serialVersionUID = -5164848692868850533L;

	/**
	 * Constructor.
	 */
	public AdminApiClientException() {
	}

	/**
	 * Constructor.
	 * @param message
	 */
	public AdminApiClientException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message
	 * @param cause
	 */
	public AdminApiClientException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * @param cause
	 */
	public AdminApiClientException(Throwable cause) {
		super(cause);
	}

}
