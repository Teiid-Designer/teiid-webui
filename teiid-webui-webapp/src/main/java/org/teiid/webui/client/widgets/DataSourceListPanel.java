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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.share.beans.DataSourcePageRow;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.SelectionModel;

/**
 * Composite for display of DataSource list and controls
 */
@Templated("./DataSourceListPanel.html")
public class DataSourceListPanel extends Composite {

    @Inject
    private ClientMessages i18n;

    @Inject @DataField("btn-dslist-panel-add")
    protected Button addButton;
    @Inject @DataField("btn-dslist-panel-delete")
    protected Button deleteButton;
    @Inject @DataField("listwidget-dslist-panel")
    protected DataSourceListWidget listWidget;
    
    @Inject Event<UiEvent> buttonEvent;
    
    public DataSourceListPanel() {
    }
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	// Tooltips
    	addButton.setTitle(i18n.format("dslist-panel.addButton.tooltip"));
    	deleteButton.setTitle(i18n.format("dslist-panel.deleteButton.tooltip"));
    	deleteButton.setEnabled(false);
    }
    
    /**
     * Event handler that fires when the user clicks the add button.
     * @param event
     */
    @EventHandler("btn-dslist-panel-add")
    public void onAddButtonClick(ClickEvent event) {
    	buttonEvent.fire(new UiEvent(UiEventType.DATA_SOURCE_ADD));
    }

    /**
     * Event handler that fires when the user clicks the delete button.
     * @param event
     */
    @EventHandler("btn-dslist-panel-delete")
    public void onDeleteButtonClick(ClickEvent event) {
    	buttonEvent.fire(new UiEvent(UiEventType.DATA_SOURCE_DELETE));
    }
    
    public void setDeleteButtonEnabled(boolean enable) {
    	deleteButton.setEnabled(enable);
    }
 
    public void setData(List<DataSourcePageRow> rows) {
    	listWidget.setData(rows);
    	updateAddButtonState();
    }
    
    public List<DataSourcePageRow> getData( ) {
    	return listWidget.getData();
    }
    
    public List<String> getDataSourceNames() {
    	return listWidget.getDataSourceNames();
    }
    
    private boolean hasNewSourcePlaceholder( ) {
    	boolean hasPlaceholder = false;
    	List<DataSourcePageRow> allRows = listWidget.getData();
    	for(DataSourcePageRow aRow : allRows) {
    		if(aRow.getState()==DataSourcePageRow.State.PLACEHOLDER) {
    			hasPlaceholder = true;
    			break;
    		}
     	}
    	return hasPlaceholder;
    }
    
    /*
     * Only allow one 'NewSource' place holder to be created.
     */
    public void updateAddButtonState() {
    	if(hasNewSourcePlaceholder()) {
    		addButton.setEnabled(false);
    	} else {
    		addButton.setEnabled(true);
    	}
    }

    public void selectFirstItem() {
    	listWidget.selectFirstItem();
    }
    
    public void setSelection(String dsName) {
    	listWidget.setSelection(dsName);
    	deleteButton.setEnabled(true);
    }
    
    public void setSelectionModel( final SelectionModel<DataSourcePageRow> selectionModel ) {
    	listWidget.setSelectionModel( selectionModel );
    }

}
