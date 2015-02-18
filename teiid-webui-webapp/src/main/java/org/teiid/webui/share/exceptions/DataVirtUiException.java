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
package org.teiid.webui.share.exceptions;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Exceptions thrown by the server part of the UI to the client.
 *
 * @author mdrillin@redhat.com
 */
@Portable
public class DataVirtUiException extends Exception {

    private static final long serialVersionUID = DataVirtUiException.class.hashCode();

    /**
     * Constructor.
     */
    public DataVirtUiException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public DataVirtUiException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message
     * @param cause
     */
    public DataVirtUiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param cause
     */
    public DataVirtUiException(Throwable cause) {
        super(cause);
    }

}
