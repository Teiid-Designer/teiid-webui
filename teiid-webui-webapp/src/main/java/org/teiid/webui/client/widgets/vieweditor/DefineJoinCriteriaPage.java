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
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.resources.AppResource;
import org.teiid.webui.client.utils.DdlHelper;
import org.teiid.webui.client.widgets.CheckableNameTypeRow;
import org.teiid.webui.client.widgets.table.ColumnNamesTable;
import org.teiid.webui.share.Constants;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

@Dependent
@Templated("./DefineJoinCriteriaPage.html")
/**
 * ViewEditor wizard page for definition of the join criteria
 */
public class DefineJoinCriteriaPage extends Composite {

    @Inject
    private ClientMessages i18n;
    
    @Inject Event<UiEvent> setDdlEvent;
    
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
	
	private ViewEditorManager editorManager = ViewEditorManager.getInstance();
	private ViewEditorWizardPanel wizard;
	
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	AppResource.INSTANCE.css().joinToggleStyle().ensureInjected();

    	lhsJoinTable.setOwner(this.getClass().getName());
    	rhsJoinTable.setOwner(this.getClass().getName());
    	
    	msgCheckOneOrMoreColumns = i18n.format("define-join-criteria-page.check-one-or-more-columns.message");
    	msgSelectLeftJoinCriteria = i18n.format("define-join-criteria-page.select-left-join-criteria.message");
    	msgSelectRightJoinCriteria= i18n.format("define-join-criteria-page.select-right-join-criteria.message");
    	msgClickApplyWhenFinished = i18n.format("define-join-criteria-page.click-apply-when-finished.message");
  	  
    	lhsTableTitleLabel.setText(i18n.format("define-join-criteria-page.lhs-table.title"));
    	rhsTableTitleLabel.setText(i18n.format("define-join-criteria-page.rhs-table.title"));
    	
    	joinInnerButton = new ToggleButton(new Image(AppResource.INSTANCE.images().joinInner_Image()));
        joinLeftOuterButton = new ToggleButton(new Image(AppResource.INSTANCE.images().joinLeftOuter_Image()));
        joinRightOuterButton = new ToggleButton(new Image(AppResource.INSTANCE.images().joinRightOuter_Image()));
        joinFullOuterButton = new ToggleButton(new Image(AppResource.INSTANCE.images().joinFullOuter_Image()));
        joinInnerButton.setTitle(i18n.format("define-join-criteria-page.inner-join.tooltip"));
        joinLeftOuterButton.setTitle(i18n.format("define-join-criteria-page.left-outer-join.tooltip"));
        joinRightOuterButton.setTitle(i18n.format("define-join-criteria-page.right-outer-join.tooltip"));
        joinFullOuterButton.setTitle(i18n.format("define-join-criteria-page.full-outer-join.tooltip"));
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
        setJoinButtonSelection(Constants.JOIN_TYPE_INNER);
        
        joinInnerButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
        		setJoinButtonStates(true,false,false,false);
    			editorManager.setJoinType(Constants.JOIN_TYPE_INNER);
    		}
    	});                	
        joinLeftOuterButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
        		setJoinButtonStates(false,true,false,false);
    			editorManager.setJoinType(Constants.JOIN_TYPE_LEFT_OUTER);
    		}
    	});                	
        joinRightOuterButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
        		setJoinButtonStates(false,false,true,false);
    			editorManager.setJoinType(Constants.JOIN_TYPE_RIGHT_OUTER);
    		}
    	});                	
        joinFullOuterButton.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
        		setJoinButtonStates(false,false,false,true);
    			editorManager.setJoinType(Constants.JOIN_TYPE_FULL_OUTER);
    		}
    	});  

    }
    
    /**
     * Refresh the panel using state from the ViewEditorManager
     */
    public void update() {
		// Determine page number, set title
		int pageNumber = 2;
		int nTemplatePages = editorManager.getNumberTablesRequiringTemplates();
		if(nTemplatePages==1) {
			pageNumber = 3;
		} else if(nTemplatePages==2) {
			pageNumber = 4;
		}
    	this.wizard.setWizardPageTitle(i18n.format("define-join-page.title", pageNumber));
    	
    	List<CheckableNameTypeRow> lhsColumns = editorManager.getColumns(0);
    	List<CheckableNameTypeRow> rhsColumns = editorManager.getColumns(1);

    	setTable(Side.LEFT, lhsColumns);
    	setTable(Side.RIGHT, rhsColumns);
    	
    	String lhCriteriaCol = editorManager.getJoinColumn(0);
    	String rhCriteriaCol = editorManager.getJoinColumn(1);
    	setLHCriteriaSelection(lhCriteriaCol);
    	setRHCriteriaSelection(rhCriteriaCol);
    	
    	setJoinButtonSelection(editorManager.getJoinType());
    	
		updateStatus();
    }
    
    /**
     * Set the owner wizardPanel
     * @param wizard the wizard
     */
    public void setWizard(ViewEditorWizardPanel wizard) {
    	this.wizard = wizard;
    }
    
    /**
     * Set the Columns table rows for the specified table.
     * @param side the left or right table side
     * @param colList the list of column rows
     */
    public void setTable(Side side, List<CheckableNameTypeRow> colList) {
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
	    	lhsTableTitleLabel.setText( lhTableName );
		} else if (side==Side.RIGHT) {
			if(colList!=null) rhsJoinTable.setData(colList);
	    	rhsTableTitleLabel.setText( rhTableName );
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
     * Sets the join button selection
     * @param joinType the join type
     */
    private void setJoinButtonSelection(String joinType) {
    	if(joinType.equals(Constants.JOIN_TYPE_INNER)) {
    		setJoinButtonStates(true,false,false,false);
    	} else if(joinType.equals(Constants.JOIN_TYPE_LEFT_OUTER)) {
    		setJoinButtonStates(false,true,false,false);
    	} else if(joinType.equals(Constants.JOIN_TYPE_RIGHT_OUTER)) {
    		setJoinButtonStates(false,false,true,false);
    	} else if(joinType.equals(Constants.JOIN_TYPE_FULL_OUTER)) {
    		setJoinButtonStates(false,false,false,true);
    	}
    }
    
    private void setJoinButtonStates(boolean innerState, boolean leftOuterStatus, 
    		                         boolean rightOuterState, boolean fullOuterState) {
        joinInnerButton.setValue(innerState);
        joinLeftOuterButton.setValue(leftOuterStatus);
        joinRightOuterButton.setValue(rightOuterState);
        joinFullOuterButton.setValue(fullOuterState);
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
    	
        if(side==Side.LEFT) {
        	// Add the change handler for status updates
            ChangeHandler criteriaChangeHandler = new ChangeHandler()
            {
            	// Changing the updates status
            	public void onChange(ChangeEvent event)
            	{
            		String sel = getLHCriteriaSelection();
            		editorManager.setJoinColumn(0, sel);
            		updateStatus();
            	}
            };
        	lhCriteriaHandlerRegistration = criteriaListBox.addChangeHandler(criteriaChangeHandler);
        } else {
        	// Add the change handler for status updates
            ChangeHandler criteriaChangeHandler = new ChangeHandler()
            {
            	// Changing the updates status
            	public void onChange(ChangeEvent event)
            	{
            		String sel = getRHCriteriaSelection();
            		editorManager.setJoinColumn(1, sel);
            		updateStatus();
            	}
            };
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
     * Handler for Replace DDL button click.
     */
    public void replaceDdlClicked( ) {
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
    	String jType = editorManager.getJoinType();
    	
    	// Gets either the table name or template SQL
    	String lhs = null;
    	String rhs = null;
    	if(editorManager.tableRequiresTemplate(0)) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("(");
    		sb.append(editorManager.getTemplateSQL(0));
    		sb.append(") AS A");
    		lhs = sb.toString();
    	} else {
    		lhs = lhsTableName+" AS A";
    		//lhs = StringUtils.escapeSQLName(lhsTableName);
    	}
    	if(editorManager.tableRequiresTemplate(1)) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("(");
    		sb.append(editorManager.getTemplateSQL(1));
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
    	if(dEvent.getType() == UiEventType.COLUMN_NAME_TABLE_CHECKBOX_CHANGED && dEvent.getEventSource().equals(this.getClass().getName())) {
    		List<String> lhsCols = lhsJoinTable.getSelectedColumnNames();
    		List<String> rhsCols = rhsJoinTable.getSelectedColumnNames();
    		List<String> lhsColTypes = lhsJoinTable.getSelectedColumnTypes();
    		List<String> rhsColTypes = rhsJoinTable.getSelectedColumnTypes();
    		editorManager.setSelectedColumns(0, lhsCols);
    		editorManager.setSelectedColumns(1, rhsCols);
    		editorManager.setSelectedColumnTypes(0, lhsColTypes);
    		editorManager.setSelectedColumnTypes(1, rhsColTypes);
    		updateStatus();
    	}
    }
    
    /**
     * Get the Left table name
     * @return the left table name
     */
    public String getLHTable() {
    	return this.lhTableName;
    }

    /**
     * Get the Right table name
     * @return the right table name
     */
    public String getRHTable() {
    	return this.rhTableName;
    }

    /**
     * Get the Left table data source
     * @return the left table source
     */
    public String getLHTableSource() {
    	return this.lhTableSource;
    }
    
    /**
     * Get the Right table data source
     * @return the right table source
     */
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
    		List<String> selectedLHColumns = editorManager.getSelectedColumns(0);
    		List<String> selectedRHColumns = editorManager.getSelectedColumns(1);
    		int nLHS = (selectedLHColumns==null) ? 0 : selectedLHColumns.size();
    		int nRHS = (selectedRHColumns==null) ? 0 : selectedRHColumns.size();
    		if(nLHS + nRHS == 0) {
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
    		this.wizard.setNextOrReplaceButton(true);
    	} else {
    		messageLabel.setText(currentStatus);
    		this.wizard.setNextOrReplaceButton(false);
    	}

    }
	
	/**
	 * Get the panel status
	 * @return the status
	 */
	public String getStatus() {
		return this.currentStatus;
	}
           
}