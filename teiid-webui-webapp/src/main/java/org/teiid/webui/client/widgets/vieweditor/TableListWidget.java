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
package org.teiid.webui.client.widgets.vieweditor;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Image;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.resources.ImageHelper;
import org.teiid.webui.share.Constants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

@Dependent
/**
 * TableListWidget
 * Used for display of a selected table in the ViewEditor
 */
public class TableListWidget extends Composite implements HasModel<TableListItem> {

	private static final String STYLE_PLACEHOLDER = "selectPlaceholderStyle";
	private static final String STYLE_NORMAL = "selectTableStyle";
	
    interface TableListWidgetBinder extends UiBinder<Widget, TableListWidget> {}
    private static TableListWidgetBinder uiBinder = GWT.create(TableListWidgetBinder.class);
    
    @Inject
    private ClientMessages i18n;

	@Inject @AutoBound DataBinder<TableListItem> serviceBinder;

	@UiField
	Container theContainer;
    @UiField
    Heading sourceNameText;
    @UiField
    Heading tableNameText;
    @UiField
    Image sourceTypeImage;
    @UiField
    Button removeTableButton;

    @Inject Event<UiEvent> uiEvent;
    
    /**
     * Constructor
     */
    public TableListWidget() {
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        
        // other initialization
        init();
    }
    
    /**
     * post-construction init
     */
    private void init() {    	
        // Click Handler for ViewService button
    	removeTableButton.addClickHandler(new ClickHandler()
        {
			@Override
			public void onClick( final ClickEvent event ) {
				fireRemoveTableEvent(getModel().getSourceName(),getModel().getTableName());
			}
        });

    	// Tooltips
    	removeTableButton.setTitle(i18n.format("table-list-widget.remove-button.tooltip"));
    }
    
	public TableListItem getModel() {
		return serviceBinder.getModel();
	}

	public void setModel(TableListItem service) {
		serviceBinder.setModel(service);
		
		if(getModel().isPlaceHolder()) {
			this.theContainer.removeStyleName(STYLE_NORMAL);
			this.theContainer.addStyleName(STYLE_PLACEHOLDER);
			this.sourceNameText.setText(Constants.BLANK);
			this.tableNameText.setText(Constants.BLANK);
			this.sourceTypeImage=null;
			this.removeTableButton.setVisible(false);
		} else {
			this.theContainer.removeStyleName(STYLE_PLACEHOLDER);
			this.theContainer.addStyleName(STYLE_NORMAL);
			// Set data source name
			String srcName = getModel().getSourceName();
			this.sourceNameText.setText(srcName);

			// Set table name
			String tableName = getModel().getTableName();
			this.tableNameText.setText(tableName);

			// Source Image
			ImageResource srcImage = ImageHelper.getInstance().getDataSourceSmallImageForType(getModel().getType());
			this.sourceTypeImage.setResource(srcImage);
			
			this.removeTableButton.setVisible(true);
		}
	}
	
    
    /**
     * Fire status event for a dataSource
     * @param sourceName the source name
     * @param sourceTableName the table name
     */
    private void fireRemoveTableEvent(String sourceName, String sourceTableName) {
		UiEvent sEvent = new UiEvent(UiEventType.REMOVE_TABLE_FROM_LIST);
		TableListItem tableItem = new TableListItem();
		tableItem.setSourceName(sourceName);
		tableItem.setTableName(sourceTableName);
		sEvent.setRemoveTable(tableItem);
		uiEvent.fire(sEvent);
    }
    
}