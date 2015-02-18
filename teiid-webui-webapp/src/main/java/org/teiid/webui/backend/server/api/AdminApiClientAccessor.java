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
package org.teiid.webui.backend.server.api;

import java.util.Locale;

import javax.inject.Singleton;

import org.teiid.webui.backend.server.services.AdminApiClient;

/**
 * The class used whenever an Atom API request for data needs to be made.
 *
 * @author mdrillin@redhat.com
 */
@Singleton
public class AdminApiClientAccessor {

    private transient static ThreadLocal<AdminApiClient> client = new ThreadLocal<AdminApiClient>();
    private transient static ThreadLocal<Locale> tlocale = new ThreadLocal<Locale>();
    public static void setLocale(Locale locale) {
        tlocale.set(locale);
    }
    public static void clearLocale() {
        tlocale.set(null);
    }

//    @Inject
//    private DataVirtUIConfig config;

	/**
	 * C'tor.
	 */
	public AdminApiClientAccessor() {
	}

    /**
     * Creates a new s-ramp client from configuration.
     * @param config
     */
    protected AdminApiClient createClient() {
//        boolean validating = "true".equals(config.getConfig().getString(DataVirtUIConfig.DATAVIRT_API_VALIDATING)); //$NON-NLS-1$
//        AuthenticationProvider authProvider = null;
//        String authProviderClass = config.getConfig().getString(DataVirtUIConfig.DATAVIRT_API_AUTH_PROVIDER);
        try {
//            if (authProviderClass != null && authProviderClass.trim().length() > 0) {
//                Class<?> c = Class.forName(authProviderClass);
//                authProvider = (AuthenticationProvider) c.newInstance();
//            }
//            return new AdminApiClient((AuthenticationProvider)authProvider, validating);
            return new AdminApiClient(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//    	return new AdminApiClient();
    }

	/**
	 * @return the atom api client
	 */
	public AdminApiClient getClient() {
	    if (client.get() == null) {
	        client.set(createClient());
	    }
	    //client.get().setLocale(tlocale.get());
	    return client.get();
	}

}
