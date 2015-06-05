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

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.teiid.webui.share.beans.DataSourcePropertyBean;
import org.teiid.webui.share.beans.TranslatorImportPropertyBean;

/**
 * Local service responsible for firing events
 * 
 */
@ApplicationScoped
public class EventService {

    @Inject
    private Event<TranslatorImportPropertyBean> importPropEvent;
    @Inject
    private Event<DataSourcePropertyBean> propertyChangeEvent;
    
    /**
     * @return the service instance
     */
    public static EventService get() {
        Collection<IOCBeanDef<EventService>> beans = IOC.getBeanManager().lookupBeans(EventService.class);
        IOCBeanDef<EventService> beanDef = beans.iterator().next();
        return beanDef.getInstance();
    }
    
	/**
	 * Constructor.
	 */
	public EventService() {
	}
	
	/**
	 * Fires Import property changed
	 */
	public void fireImportPropertyChanged( ) {
		importPropEvent.fire(new TranslatorImportPropertyBean());
	}

	/**
	 * Fires Data Source property changed
	 */
	public void fireDataSourcePropertyChanged( ) {
		propertyChangeEvent.fire(new DataSourcePropertyBean());
	}
	
}
