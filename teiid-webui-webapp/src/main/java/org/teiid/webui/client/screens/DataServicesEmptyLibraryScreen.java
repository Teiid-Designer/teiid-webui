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
package org.teiid.webui.client.screens;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.resources.AppResource;
import org.teiid.webui.share.Constants;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * DataServicesEmptyLibraryScreen - shows intro graphic if no data services currently
 *
 */
@Dependent
@Templated("./DataServicesEmptyLibraryScreen.html#page")
@WorkbenchScreen(identifier = "DataServicesEmptyLibraryScreen")
public class DataServicesEmptyLibraryScreen extends Composite {

    @Inject
    private PlaceManager placeManager;
    
    @Inject
    private ClientMessages i18n;
    
    @Inject @DataField("btn-create-service")
    protected Button createServiceButton;
    
    @Inject @DataField("btn-manage-sources")
    protected Button manageSourcesButton;
    
    @Inject @DataField("image-empty-library")
    public Image emptyLibImage;
    
    @Override
    @WorkbenchPartTitle
    public String getTitle() {
      return Constants.BLANK;
    }
        
    @WorkbenchPartView
    public IsWidget getView() {
        return this;
    }
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	emptyLibImage.setResource(AppResource.INSTANCE.images().emptyLibraryImage());
    	
    	// Tooltips
    	createServiceButton.setTitle(i18n.format("dslibrary-empty.createServiceButton.tooltip"));
    	manageSourcesButton.setTitle(i18n.format("dslibrary-empty.manageSourcesButton.tooltip"));
    }
    
    /**
     * Event handler that fires when the user clicks the CreateService button.
     * @param event
     */
    @EventHandler("btn-create-service")
    public void onCreateServiceButtonClick(ClickEvent event) {
    	doCreateService();
    }
    
    /**
     * Event handler that fires when the user clicks the ManageSources button.
     * @param event
     */
    @EventHandler("btn-manage-sources")
    public void onManageSourcesButtonClick(ClickEvent event) {
    	doManageSources();
    }
    
    /**
     * Create Service - transitions to the Create Services page
     */
    protected void doCreateService() {
    	placeManager.goTo(Constants.CREATE_DATA_SERVICE_SCREEN);
    }
    
    /**
     * Manage Sources - transitions to the Manage Sources page
     */
    protected void doManageSources() {
		// transition to ManageSourcesScreen
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.FROM_SCREEN, Constants.DATA_SERVICES_EMPTY_LIBRARY_SCREEN);
    	placeManager.goTo(new DefaultPlaceRequest(Constants.MANAGE_SOURCES_SCREEN,parameters));
    }
    
}
