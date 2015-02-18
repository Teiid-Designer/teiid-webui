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
package org.teiid.webui.client.widgets;

import org.jboss.errai.ui.client.widget.ListWidget;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * ServiceFlowPanel - extends Errai ListWidget, providing the desired Flow behavior for the DataServicesLibraryScreen
 * 
 */
public class ServiceFlowListWidget extends ListWidget<ServiceRow, LibraryServiceWidget> {
	 
	  public ServiceFlowListWidget() {
	    super(new FlowPanel());
	  }
	  
	  @Override
	  public Class<LibraryServiceWidget> getItemWidgetType() {
	    return LibraryServiceWidget.class;
	  }
}