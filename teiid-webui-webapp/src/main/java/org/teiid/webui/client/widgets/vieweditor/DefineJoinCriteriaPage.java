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

import java.util.ArrayList;
import java.util.List;

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
import org.teiid.webui.client.resources.AppResource;
import org.teiid.webui.client.services.NotificationService;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.utils.DdlHelper;
import org.teiid.webui.client.widgets.CheckableNameTypeRow;
import org.teiid.webui.client.widgets.ColumnNamesTable;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.services.StringUtils;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

@Dependent
@Templated("./DefineJoinCriteriaPage.html")
public class DefineJoinCriteriaPage extends Composite {

    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject Event<UiEvent> setDdlEvent;
    
    @Inject @DataField("lbl-define-join-title")
    protected Label titleLabel;
    @Inject @DataField("lbl-define-join-message")
    protected Label messageLabel;
    @Inject @DataField("lbl-joineditor-lhsTableTitle")
    protected Label lhsTableTitleLabel;
    @Inject @DataField("lbl-joineditor-rhsTableTitle")
    protected Label rhsTableTitleLabel;

    @Inject @DataField("tbl-lhs-columns")
    private ColumnNamesTable lhsJoinTable;
    @Inject @DataField("tbl-rhs-columns")
    private ColumnNamesTable rhsJoinTable;
    @Inject @DataField("btn-joineditor-setDdl")
    protected Button setDdlButton;
    
    @Inject @DataField("btn-joineditor-togglePanel")
    protected VerticalPanel togglePanel;
    
    protected ToggleButton joinInnerButton;
    protected ToggleButton joinLeftOuterButton;
    protected ToggleButton joinRightOuterButton;
    protected ToggleButton joinFullOuterButton;
    
    @Inject @DataField("listbox-lhcriteria")
    protected ListBox lhCriteriaListBox;
    @Inject @DataField("listbox-rhcriteria")
    protected ListBox rhCriteriaListBox;

    private String currentStatus = "";
	private String msgCheckOneOrMoreColumns;
	private String msgSelectLeftJoinCriteria;
	private String msgSelectRightJoinCriteria;
	private String msgClickApplyWhenFinished;

	private ViewEditorWizardPanel wizard;
    private String joinType = Constants.JOIN_TYPE_INNER;
    private String lhTableName;
    private String rhTableName;
    private String lhTableSource;
    private String rhTableSource;
    private HandlerRegistration lhCriteriaHandlerRegistration;
    private HandlerRegistration rhCriteriaHandlerRegistration;
    
	private enum Side {
		LEFT,
		RIGHT
	}
	
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	AppResource.INSTANCE.css().joinToggleStyle().ensureInjected();
    	
    	msgCheckOneOrMoreColumns = i18n.format("joineditor.check-one-or-more-columns.message");
    	msgSelectLeftJoinCriteria = i18n.format("joineditor.select-left-join-criteria.message");
    	msgSelectRightJoinCriteria= i18n.format("joineditor.select-right-join-criteria.message");
    	msgClickApplyWhenFinished = i18n.format("joineditor.click-apply-when-finished.message");
  	  
    	lhsTableTitleLabel.setText(i18n.format("joineditor.lhs-table.title"));
    	rhsTableTitleLabel.setText(i18n.format("joineditor.rhs-table.title"));
    	
    	joinInnerButton = new ToggleButton(new Image(AppResource.INSTANCE.images().joinInner_Image()));
        joinLeftOuterButton = new ToggleButton(new Image(AppResource.INSTANCE.images().joinLeftOuter_Image()));
        joinRightOuterButton = new ToggleButton(new Image(AppResource.INSTANCE.images().joinRightOuter_Image()));
        joinFullOuterButton = new ToggleButton(new Image(AppResource.INSTANCE.images().joinFullOuter_Image()));
        joinInnerButton.setTitle(i18n.format("joineditor.inner-join.tooltip"));
        joinLeftOuterButton.setTitle(i18n.format("joineditor.left-outer-join.tooltip"));
        joinRightOuterButton.setTitle(i18n.format("joineditor.right-outer-join.tooltip"));
        joinFullOuterButton.setTitle(i18n.format("joineditor.full-outer-join.tooltip"));
        joinInnerButton.setStylePrimaryName("joinToggle");
        joinLeftOuterButton.setStylePrimaryName("joinToggle");
        joinRightOuterButton.setStylePrimaryName("joinToggle");
        joinFullOuterButton.setStylePrimaryName("joinToggle");
        
    	DOM.setStyleAttribute(joinInnerButton.getElement(), "padding", "0px");
    	DOM.setStyleAttribute(joinLeftOuterButton.getElement(), "padding", "0px");
    	DOM.setStyleAttribute(joinRightOuterButton.getElement(), "padding", "0px");
    	DOM.setStyleAttribute(joinFullOuterButton.getElement(), "padding", "0px");
    	DOM.setStyleAttribute(joinInnerButton.getElement(), "margin", "0px 0px 0px 0px");
    	DOM.setStyleAttribute(joinLeftOuterButton.getElement(), "margin", "5px 0px 0px 0px");
    	DOM.setStyleAttribute(joinRightOuterButton.getElement(), "margin", "5px 0px 0px 0px");
    	DOM.setStyleAttribute(joinFullOuterButton.getElement(), "margin", "5px 0px 0px 0px");

        togglePanel.add(joinInnerButton);
        togglePanel.add(joinLeftOuterButton);
        togglePanel.add(joinRightOuterButton);
        togglePanel.add(joinFullOuterButton);
        
        // Default to inner join
        joinInnerButton.setValue(true);
        joinLeftOuterButton.setValue(false);
        joinRightOuterButton.setValue(false);
        joinFullOuterButton.setValue(false);
        joinType=Constants.JOIN_TYPE_INNER;
        
        joinInnerButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
    			joinType=Constants.JOIN_TYPE_INNER;
    			joinInnerButton.setValue(true);
    			joinLeftOuterButton.setValue(false);
    			joinRightOuterButton.setValue(false);
    			joinFullOuterButton.setValue(false);
    		}
    	});                	
        joinLeftOuterButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
    			joinType=Constants.JOIN_TYPE_LEFT_OUTER;
    			joinInnerButton.setValue(false);
    			joinLeftOuterButton.setValue(true);
    			joinRightOuterButton.setValue(false);
    			joinFullOuterButton.setValue(false);
    		}
    	});                	
        joinRightOuterButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
    			joinType=Constants.JOIN_TYPE_RIGHT_OUTER;
    			joinInnerButton.setValue(false);
    			joinLeftOuterButton.setValue(false);
    			joinRightOuterButton.setValue(true);
    			joinFullOuterButton.setValue(false);
    		}
    	});                	
        joinFullOuterButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
    			joinType=Constants.JOIN_TYPE_FULL_OUTER;
    			joinInnerButton.setValue(false);
    			joinLeftOuterButton.setValue(false);
    			joinRightOuterButton.setValue(false);
    			joinFullOuterButton.setValue(true);
    		}
    	});  
        
    }
    
    /**
     * Used to update panel right before it is shown
     */
    public void update() {
    	ViewEditorManager viewEditor = ViewEditorManager.getInstance();

		// Determine page number, set title
		int pageNumber = 2;
		int nTemplatePages = viewEditor.getNumberTablesRequiringTemplates();
		if(nTemplatePages==1) {
			pageNumber = 3;
		} else if(nTemplatePages==2) {
			pageNumber = 4;
		}
        titleLabel.setText(i18n.format("define-join-page.title", pageNumber));
    	
    	List<CheckableNameTypeRow> lhsColumns = viewEditor.getColumns(0);
    	List<CheckableNameTypeRow> rhsColumns = viewEditor.getColumns(1);

    	setTable(Side.LEFT, lhsColumns, viewEditor);
    	setTable(Side.RIGHT, rhsColumns, viewEditor);
    	
		updateStatus();
    }
    
    /**
     * Set the owner wizardPanel
     * @param wizard the wizard
     */
    public void setWizard(ViewEditorWizardPanel wizard) {
    	this.wizard = wizard;
    }
    
    public void setTable(Side side, List<CheckableNameTypeRow> colList, ViewEditorManager editorManager) {
    	// Set table name and source name
    	if(side==Side.LEFT) {
    		lhTableName = editorManager.getTable(0);
    		lhTableSource = editorManager.getSourceNameForTable(0);
    	} else {
    		rhTableName = editorManager.getTable(1);
    		rhTableSource = editorManager.getSourceNameForTable(1);
    	}
        
    	// Set table data and title
		if(side==Side.LEFT) {
			if(colList!=null) lhsJoinTable.setData(colList);
	    	lhsTableTitleLabel.setText("LHS (" + lhTableName + ")");
		} else if (side==Side.RIGHT) {
			if(colList!=null) rhsJoinTable.setData(colList);
	    	rhsTableTitleLabel.setText("RHS (" + rhTableName + ")");
		}
		
		// Set the criteria listbox
		if(colList!=null) {
			List<String> colNames = new ArrayList<String>(colList.size());
			for(CheckableNameTypeRow row : colList) {
				colNames.add(row.getName());
			}
			populateCriteriaListBox(side, colNames);
		}
    }

    /**
     * Populate left or right criteria listBox
     * @param side left or right
     * @param columnNames list of columnNames for the listBox
     */
    private void populateCriteriaListBox(Side side, List<String> columnNames) {
    	ListBox criteriaListBox = null;
    	if(side==Side.LEFT) {
    		if(this.lhCriteriaHandlerRegistration!=null) this.lhCriteriaHandlerRegistration.removeHandler();
    		criteriaListBox = this.lhCriteriaListBox;
    	} else {
    		if(this.rhCriteriaHandlerRegistration!=null) this.rhCriteriaHandlerRegistration.removeHandler();
    		criteriaListBox = this.rhCriteriaListBox;
    	}
    	
    	// Make sure clear first
    	criteriaListBox.clear();

    	// For no columns, the displayed item0 is different
    	String item0 = null;
    	if(columnNames.size()==0) {
    		item0 = Constants.NO_CRITERIA_COLUMNS;
    	} else {
    		item0 = Constants.NO_CRITERIA_SELECTION;
    	}
    	
    	criteriaListBox.insertItem(item0, 0);
    	
    	// Repopulate the ListBox with column names
    	int i = 1;
    	for(String columnName: columnNames) {
    		criteriaListBox.insertItem(columnName, i);
    		i++;
    	}

    	// Initialize by setting the selection to the first item.
    	criteriaListBox.setSelectedIndex(0);
    	
    	// Add the change handler for status updates
        ChangeHandler criteriaChangeHandler = new ChangeHandler()
        {
        	// Changing the updates status
        	public void onChange(ChangeEvent event)
        	{
        		updateStatus();
        	}
        };
        if(side==Side.LEFT) {
        	lhCriteriaHandlerRegistration = criteriaListBox.addChangeHandler(criteriaChangeHandler);
        } else {
        	rhCriteriaHandlerRegistration = criteriaListBox.addChangeHandler(criteriaChangeHandler);
        }
    }
    
    /**
     * Get the selected LH Criteria Column
     * @return
     */
    public String getLHCriteriaSelection() {
    	int index = lhCriteriaListBox.getSelectedIndex();
    	return lhCriteriaListBox.getValue(index);
    }
    
    /**
     * Set the selected LH Criteria Column
     * @return
     */
	public void setLHCriteriaSelection(String columnName) {
		int indx = 0;
		int nItems = lhCriteriaListBox.getItemCount();
		for(int i=0; i<nItems; i++) {
			String itemText = lhCriteriaListBox.getItemText(i);
			if(itemText.equalsIgnoreCase(columnName)) {
				indx = i;
				break;
			}
		}
		lhCriteriaListBox.setSelectedIndex(indx);
	}
	
    /**
     * Get the selected RH Criteria Column
     * @return
     */
    public String getRHCriteriaSelection() {
    	int index = rhCriteriaListBox.getSelectedIndex();
    	return rhCriteriaListBox.getValue(index);
    }
    
    /**
     * Set the selected RH Criteria Column
     * @return
     */
	public void setRHCriteriaSelection(String columnName) {
		int indx = 0;
		int nItems = rhCriteriaListBox.getItemCount();
		for(int i=0; i<nItems; i++) {
			String itemText = rhCriteriaListBox.getItemText(i);
			if(itemText.equalsIgnoreCase(columnName)) {
				indx = i;
				break;
			}
		}
		rhCriteriaListBox.setSelectedIndex(indx);
	}

	/**
     * Event handler that fires when the user clicks the Add to view defn button.
     * @param event
     */
    @EventHandler("btn-joineditor-setDdl")
    public void onSetDdlButtonClick(ClickEvent event) {
    	String ddl = buildDdl();
    	
		UiEvent uiEvent = new UiEvent(UiEventType.VIEW_DEFN_REPLACE_FROM_JOIN_EDITOR);
		uiEvent.setViewDdl(ddl);
		List<String> viewSources = new ArrayList<String>();
		viewSources.add(getLHTableSource());
		String rhTable = getRHTableSource();
		if(!viewSources.contains(rhTable)) {
			viewSources.add(rhTable);
		}
		uiEvent.setViewSources(viewSources);
		
		setDdlEvent.fire(uiEvent);
    }
    
    /**
     * Build the DDL from the editor selections
     * @return the DDL
     */
    private String buildDdl( ) {
    	List<String> lhsColNames = lhsJoinTable.getSelectedColumnNames();
    	List<String> lhsColTypes = lhsJoinTable.getSelectedColumnTypes();
    	List<String> rhsColNames = rhsJoinTable.getSelectedColumnNames();
    	List<String> rhsColTypes = rhsJoinTable.getSelectedColumnTypes();
    	String lhsCriteriaCol = getLHCriteriaSelection();
    	String rhsCriteriaCol = getRHCriteriaSelection();
    	String lhsTableName = getLHTable();
    	String rhsTableName = getRHTable();
    	String jType = this.joinType;
    	
    	ViewEditorManager editorManager = ViewEditorManager.getInstance();
    	// Gets either the table name or template SQL
    	String lhs = null;
    	String rhs = null;
    	if(editorManager.tableRequiresTemplate(0)) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("(");
    		sb.append(editorManager.getSourceTransformationSQL(0));
    		sb.append(") AS A");
    		lhs = sb.toString();
    	} else {
    		lhs = lhsTableName+" AS A";
    		//lhs = StringUtils.escapeSQLName(lhsTableName);
    	}
    	if(editorManager.tableRequiresTemplate(1)) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("(");
    		sb.append(editorManager.getSourceTransformationSQL(1));
    		sb.append(") AS B");
    		rhs = sb.toString();
    	} else {
    		rhs = rhsTableName+" AS B";
    		//rhs = StringUtils.escapeSQLName(rhsTableName);
    	}
    	
     	String viewDdl = DdlHelper.getODataViewJoinDdl(Constants.SERVICE_VIEW_NAME, lhs, lhsColNames, lhsColTypes, lhsCriteriaCol,
     			                                                                    rhs, rhsColNames, rhsColTypes, rhsCriteriaCol, jType);
    	
     	return viewDdl;
    }
    
    /**
     * Handles UiEvents from columnNamesTable
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// checkbox change event from column names table
    	if(dEvent.getType() == UiEventType.COLUMN_NAME_TABLE_CHECKBOX_CHANGED) {
    		updateStatus();
    	}
    }
    
    public String getLHTable() {
    	return this.lhTableName;
    }
    public String getRHTable() {
    	return this.rhTableName;
    }
    public String getLHTableSource() {
    	return this.lhTableSource;
    }
    public String getRHTableSource() {
    	return this.rhTableSource;
    }
    
    /**
     * Update panel status
     */
	private void updateStatus( ) {
    	currentStatus = Constants.OK;
    	
		// Ensure some columns are selected
    	if(Constants.OK.equals(currentStatus)) {
    		List<String> selectedLHColumns = lhsJoinTable.getSelectedColumnNames();
    		List<String> selectedRHColumns = rhsJoinTable.getSelectedColumnNames();
    		if(selectedLHColumns.isEmpty() && selectedRHColumns.isEmpty()) {
    			currentStatus = msgCheckOneOrMoreColumns;
    		}
    	}
    	
		// Make sure LH Criteria is selected
    	if(Constants.OK.equals(currentStatus)) {
    		String lhCritColumn = getLHCriteriaSelection();
    		if(Constants.NO_CRITERIA_SELECTION.equals(lhCritColumn)) {
    			currentStatus = msgSelectLeftJoinCriteria;
    		}
    	}

    	// Make sure RH Criteria is selected
    	if(Constants.OK.equals(currentStatus)) {
    		String rhCritColumn = getRHCriteriaSelection();
    		if(Constants.NO_CRITERIA_SELECTION.equals(rhCritColumn)) {
    			currentStatus = msgSelectRightJoinCriteria;
    		}
    	}
    	
		// Enable setDdlButton button if OK
    	if(Constants.OK.equals(currentStatus)) {
    		messageLabel.setText(msgClickApplyWhenFinished);
    		setDdlButton.setEnabled(true);
    	} else {
    		messageLabel.setText(currentStatus);
    		setDdlButton.setEnabled(false);
    	}

    }
	
	public String getStatus() {
		return this.currentStatus;
	}
           
}