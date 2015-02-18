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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.services.StringUtils;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Composite for display of ViewSource list and controls
 */
@Dependent
@Templated("./ViewSourcePanel.html")
public class ViewSourcePanel extends Composite {

	@Inject
	private PlaceManager placeManager;
	
    @Inject
    private ClientMessages i18n;
    
    @Inject @DataField("btn-viewsource-panel-add")
    protected Button addButton;
    @Inject @DataField("btn-viewsource-panel-delete")
    protected Button deleteButton;
    @Inject @DataField("table-viewsource-panel")
    protected ViewSourceNamesTable viewSourceNamesTable;
    
    private SingleSelectionModel<String> dsSelectionModel;
    
    @Inject Event<UiEvent> sourcesChangedEvent;
    
    private String selectedSource;
    private List<String> allAvailableSources = new ArrayList<String>();
        
    public ViewSourcePanel() {
    }
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	// SelectionModel to handle Source selection 
    	dsSelectionModel = new SingleSelectionModel<String>();
    	viewSourceNamesTable.setSelectionModel(dsSelectionModel); 
    	dsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange( SelectionChangeEvent event) { 
    			String selectedRow = dsSelectionModel.getSelectedObject();
    			selectedSource = selectedRow;
    			if(selectedSource!=null) {
    				deleteButton.setEnabled(true);
    			} else {
    				deleteButton.setEnabled(false);
    			}
    		} });
    	
    	// Initially disabled.  Enables when user makes a selection
    	deleteButton.setEnabled(false);
    	
    	// Tooltips
    	addButton.setTitle(i18n.format("viewsource-panel.addButton.tooltip"));
    	deleteButton.setTitle(i18n.format("viewsource-panel.deleteButton.tooltip"));
    	viewSourceNamesTable.setTitle(i18n.format("viewsource-panel.viewSourceNamesTable.tooltip"));
    }
    
    /**
     * Event handler that fires when the user clicks the add button.
     * @param event
     */
    @EventHandler("btn-viewsource-panel-add")
    public void onAddButtonClick(ClickEvent event) {
		// Display the DataSource selection dialog
		Map<String,String> parameters = new HashMap<String,String>();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String src : allAvailableSources) {
			if(!first) {
				sb.append(";");
			}
			sb.append(src);
			first=false;
		}
		parameters.put(Constants.ADD_VIEW_SRC_AVAILABLE_SRCS, sb.toString());
    	placeManager.goTo(new DefaultPlaceRequest(Constants.ADD_VIEW_SRC_DIALOG,parameters));    	
    }
    
    /**
     * Handles UiEvents
     * @param dEvent
     */
    public void onDialogEvent(@Observes UiEvent dEvent) {
    	// User has OK'd source rename
    	if(dEvent.getType() == UiEventType.ADD_VIEW_SOURCE_OK) {
    		String ds = dEvent.getDataSourceName();
    		if(!StringUtils.isEmpty(ds)) {
    			onAddConfirm(ds);
    		}
    	// User has OK'd source redeploy
    	} else if(dEvent.getType() == UiEventType.ADD_VIEW_SOURCE_CANCEL) {
    	} 
    }
    
    private void onAddConfirm(String newSource) {
    	List<String> currentSources = viewSourceNamesTable.getData();
    	List<String> newSources = new ArrayList<String>();
    	newSources.addAll(currentSources);
    	if(!newSources.contains(newSource)) {
    		newSources.add(newSource);
    	}
    	
    	viewSourceNamesTable.setData(newSources);
    	fireSourcesChanged();
    }

    /**
     * Event handler that fires when the user clicks the delete button.
     * @param event
     */
    @EventHandler("btn-viewsource-panel-delete")
    public void onDeleteButtonClick(ClickEvent event) {
    	List<String> currentSources = viewSourceNamesTable.getData();
    	List<String> newSources = new ArrayList<String>();
    	newSources.addAll(currentSources);
    	if(selectedSource!=null) {
    		newSources.remove(selectedSource);
    	}
    	viewSourceNamesTable.setData(newSources);
    	dsSelectionModel.clear();
		deleteButton.setEnabled(false);
    	fireSourcesChanged();
    }
 
    public void setData(List<String> tableSourceNames, List<String> allSources) {
    	viewSourceNamesTable.setData(tableSourceNames);
    	setAllAvailableSources(allSources);
    }
    
    public void addData(List<String> tableSourceNames, List<String> allSources) {
    	viewSourceNamesTable.addData(tableSourceNames);
    	setAllAvailableSources(allSources);
    }
    
    public List<String> getData( ) {
    	return viewSourceNamesTable.getData();
    }
    
    public void setAllAvailableSources(List<String> allSources) {
    	this.allAvailableSources = allSources;
    }
    
    public void setSelectionModel( final SelectionModel<String> selectionModel ) {
    	viewSourceNamesTable.setSelectionModel( selectionModel );
    }
    
    /**
     * Fire state changed
     */
    public void fireSourcesChanged( ) {
    	sourcesChangedEvent.fire(new UiEvent(UiEventType.VIEW_SOURCES_CHANGED));
    }
    
}
