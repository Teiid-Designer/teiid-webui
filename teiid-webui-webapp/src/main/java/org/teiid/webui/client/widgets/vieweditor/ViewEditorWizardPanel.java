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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconPosition;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.resources.AppResource;
import org.teiid.webui.client.services.TeiidRpcService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("./ViewEditorWizardPanel.html")
/**
 * ViewEditorWizardPanel
 * The ViewEditor wizard panel which controls wizard page display
 */
public class ViewEditorWizardPanel extends Composite {

	private static final int FIRST_PAGE_SELECT_TABLES_INDX = 0;
	private static final int LAST_PAGE_SELECT_TABLE_COLS_INDX = 1;
	private static final int DEFINE_TEMPLATE_PAGE_INDX = 2;
	private static final int LAST_PAGE_DEFINE_JOIN_INDX = 3;
	
	private static final String PREVIOUS_BUTTON_TEXT = "Previous";
	private static final String NEXT_BUTTON_TEXT = "Next";
	private static final String REPLACE_BUTTON_TEXT = "Replace";
	
    @Inject
    private ClientMessages i18n;
    
    @Inject
    protected TeiidRpcService teiidService;
    
    @Inject @DataField("wizard-deckpanel")
    protected DeckPanel wizardDeckPanel;
    
    @Inject @DataField("lbl-page-title")
    protected Label pageTitleLabel;
    @Inject @DataField("btn-manage-sources")
    protected Button manageSourceButton;
    
    @Inject @DataField("btn-resetPage")
    protected Button resetPageButton;
    @Inject @DataField("btn-previousPage")
    protected Button previousPageButton;
    @Inject @DataField("btn-replaceDdlOrNext")
    protected Button nextOrReplaceButton;

    @Inject 
    protected SelectTablesPage selectTablesPage;
    @Inject 
    protected SelectTableColumnsPage selectTableColumnsPage;
    @Inject 
    protected DefineTemplateDdlPage defineTemplateDdlPage;
    @Inject 
    protected DefineJoinCriteriaPage defineJoinCriteriaPage;
    
    @Inject Event<UiEvent> uiEvent;

    private String owner;
	private enum NextReplaceButton {
		NEXT,
		REPLACE
	}
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	AppResource.INSTANCE.css().vcenterStyle().ensureInjected();
    	
    	selectTablesPage.setWizard(this);
    	selectTableColumnsPage.setWizard(this);
    	defineTemplateDdlPage.setWizard(this);
    	defineJoinCriteriaPage.setWizard(this);
    	
    	manageSourceButton.setTitle(i18n.format("vieweditor-panel.manageSourceButton.tooltip"));
    	
    	previousPageButton.setIcon(IconType.ANGLE_LEFT);
    	previousPageButton.setIconPosition(IconPosition.LEFT);
    	previousPageButton.setText(PREVIOUS_BUTTON_TEXT);
    	previousPageButton.setEnabled(false);
    	resetPageButton.setEnabled(false);
    	setNextReplaceButton(NextReplaceButton.NEXT, false);
		
    	// Add all of the panels to wizard deckPanel
    	wizardDeckPanel.add(selectTablesPage);
    	wizardDeckPanel.add(selectTableColumnsPage);
    	wizardDeckPanel.add(defineTemplateDdlPage);
    	wizardDeckPanel.add(defineJoinCriteriaPage);

    	reset();
    	wizardDeckPanel.showWidget(FIRST_PAGE_SELECT_TABLES_INDX);
    }  
    
    /** 
     * Sets the available sources for constructing the data services
     * @param availableSourceNames the available sources
     */
    public void refreshAvailableSources( ) {
    	// ViewEditorManager.setAvailableSources(availableSourceNames);
    	selectTablesPage.refreshAvailableSources( );
	}
    
    public void setWizardPageTitle(String pageTitle) {
    	pageTitleLabel.setText(pageTitle);
    }
    
    /**
     * Resets any saved state
     */
    public void reset() {
    	// Start Over clicked - clear the Manager contents
    	ViewEditorManager.getInstance().clear();
    	
    	// Set next button in disable state
    	setNextReplaceButton(NextReplaceButton.NEXT,false);
    	this.manageSourceButton.setVisible(true);
    	this.previousPageButton.setEnabled(false);
    	this.resetPageButton.setEnabled(false);
    	
    	// Updates the starting page
    	updatePage(FIRST_PAGE_SELECT_TABLES_INDX);
    }
    
    /**
     * Set enabled state of the Next or Replace Button
     * @param enabled the enabled statue
     */
    public void setNextOrReplaceButton(boolean enabled) {
    	boolean isEnabled = this.nextOrReplaceButton.isEnabled();
    	if(isEnabled!=enabled) this.nextOrReplaceButton.setEnabled(enabled);
    	if(enabled) {
    		this.nextOrReplaceButton.setType(ButtonType.PRIMARY);
    	} else {
    		this.nextOrReplaceButton.setType(ButtonType.DEFAULT);
    	}
    }
    
    /**
     * Event handler that fires when the user clicks the ResetPage button.
     * @param event
     */
    @EventHandler("btn-resetPage")
    public void onResetPageButtonClick(ClickEvent event) {
    	// Reset any saved state
    	reset();

    	// Show the first page
    	wizardDeckPanel.showWidget(FIRST_PAGE_SELECT_TABLES_INDX);
    }
    
    /**
     * Event handler that fires when the user clicks the Manage Sources button.
     * @param event
     */
    @EventHandler("btn-manage-sources")
    public void onManageSourcesButtonClick(ClickEvent event) {
    	fireGoToManageSources();
    }
    
    /**
     * Event handler that fires when the user clicks the Next or Replace button.
     * @param event
     */
    @EventHandler("btn-replaceDdlOrNext")
    public void onNextPageButtonClick(ClickEvent event) {
    	if(nextOrReplaceButton.getText().equalsIgnoreCase("Next")) {
    		showNextPage();
    	} else {
    		int visiblePage = wizardDeckPanel.getVisibleWidget();
    		if(visiblePage==LAST_PAGE_SELECT_TABLE_COLS_INDX) {
    			selectTableColumnsPage.replaceDdlClicked();
    		} else if(visiblePage==DEFINE_TEMPLATE_PAGE_INDX) {
    			defineTemplateDdlPage.replaceDdlClicked();
    		} else if(visiblePage==LAST_PAGE_DEFINE_JOIN_INDX) {
    			defineJoinCriteriaPage.replaceDdlClicked();
    		}
    	}
    }
    
    /**
     * Event handler that fires when the user clicks the PreviousPage button.
     * @param event
     */
    @EventHandler("btn-previousPage")
    public void onPreviousPageButtonClick(ClickEvent event) {
    	showPreviousPage();
    }
    
    /**
     * Fire go to manage soruces
     */
    public void fireGoToManageSources( ) {
    	UiEvent event = new UiEvent(UiEventType.VIEW_EDITOR_GOTO_MANAGE_SOURCES);
    	event.setEventSource(getOwner());
    	uiEvent.fire(event);
    }
    
    public void setOwner(String owner) {
    	this.owner = owner;
    }
    
    public String getOwner() {
    	return this.owner;
    }
    
    /**
     * Show the next page of the wizard
     */
    public void showNextPage() {
    	// Get currently visible page
    	int indx = wizardDeckPanel.getVisibleWidget();
    	
    	// If going to next page, previous and startOver buttons are enabled
    	previousPageButton.setEnabled(true);
 
    	int nextIndx = determineNextPage(indx);
    	
    	// Update page prior to showing
    	updatePage(nextIndx);
    	
    	wizardDeckPanel.showWidget(nextIndx);
    	
    	// Will not be on first page, enable the button
    	this.resetPageButton.setEnabled(true);
    	this.manageSourceButton.setVisible(false);
    	
    	// If on last page, next button has 'replace' text
    	if(nextIndx==LAST_PAGE_SELECT_TABLE_COLS_INDX || 
    	   nextIndx==LAST_PAGE_DEFINE_JOIN_INDX ||
    	   (nextIndx==DEFINE_TEMPLATE_PAGE_INDX && ViewEditorManager.getInstance().getTables().size()==1)) {
    		setNextReplaceButton(NextReplaceButton.REPLACE);
    	} else {
    		setNextReplaceButton(NextReplaceButton.NEXT);
    	}
    }
    
    /**
     * Determine the next page index
     * @param currentIndx the current page index
     * @return the next page index
     */
    public int determineNextPage(int currentIndx) {
    	int resultIndx = currentIndx;
    	
    	ViewEditorManager editorManager = ViewEditorManager.getInstance();
		// Save current page selections to manager
		savePageSelectionsToManager(currentIndx);
		
    	// ------------------------------------------------------------------------
    	// On page one.  The second page is determined by the page one selections
    	// ------------------------------------------------------------------------
    	if(currentIndx==FIRST_PAGE_SELECT_TABLES_INDX) {
    		// One table selected
    		if(editorManager.getTables().size()==1) {
    			// If the table requires a template, show template page
        		if(editorManager.getTableTemplateRequiredStates().get(0)) {
        			editorManager.setTemplateTableIndex(0);
        			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
        		// No template required, show the 'pick columns' page
        		} else {
        			resultIndx = LAST_PAGE_SELECT_TABLE_COLS_INDX;
        		}
    		} else if(editorManager.getTables().size()==2) {
    			List<Boolean> tableRequiredStates = editorManager.getTableTemplateRequiredStates();
    			// Determine if table one requires a template
        		if(tableRequiredStates.get(0)) {
        			editorManager.setTemplateTableIndex(0);
        			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
        		// Determine if table two requires a template
        		} else if(tableRequiredStates.get(1)) {
        			editorManager.setTemplateTableIndex(1);
        			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
        		} else {
        			resultIndx = LAST_PAGE_DEFINE_JOIN_INDX;
        		}
    		}
        // ------------------------------------------------------------------------
        // On "Define template page".  Next page depends on how many of the
        //    sources require a template definition
        // ------------------------------------------------------------------------
    	} else if(currentIndx==DEFINE_TEMPLATE_PAGE_INDX) {
    		// If getTables().size() == 1, this will be the last page.  (no next)
    		// Two table sources.  
    		if(editorManager.getTables().size()==2) {
    			int tablesNeedingTemplates = editorManager.getNumberTablesRequiringTemplates();
    			// If only one source needs template, you are on it.  Next page is DEFINE_JOIN
        		if(tablesNeedingTemplates==1) {
        			resultIndx = LAST_PAGE_DEFINE_JOIN_INDX;
        		} else if(tablesNeedingTemplates==2) {
        			// If on the second table, then go to DEFINE_JOIN
        			int tableIndx = editorManager.getTemplateTableIndex();
        			if(tableIndx==1) {
            			resultIndx = LAST_PAGE_DEFINE_JOIN_INDX;
        			} else if(tableIndx==0) {
            			editorManager.setTemplateTableIndex(1);
        				resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
        			}
        		}
    		}
    	} else {
    		resultIndx = currentIndx + 1;
    	}
    	
    	return resultIndx;
    }
    
    /**
     * Show the previous page of the wizard
     */
    public void showPreviousPage() {
    	// Get currently visible page
    	int indx = wizardDeckPanel.getVisibleWidget();
    	
    	// If going to previous page, next button is visible and enabled. Replace DDL is hidden and disabled
    	setNextReplaceButton(NextReplaceButton.NEXT,true);
    	
    	int prevIndx = determinePreviousPage(indx);
    	updatePage(prevIndx);
    	wizardDeckPanel.showWidget(prevIndx);
    	
    	// If on first page, previous and start over buttons disabled
    	if(prevIndx==FIRST_PAGE_SELECT_TABLES_INDX) {
        	previousPageButton.setEnabled(false);
        	resetPageButton.setEnabled(false);
        	manageSourceButton.setVisible(true);
    	} else {
        	previousPageButton.setEnabled(true);
    	}
    }
    
    /**
     * Determine the next page index
     * @param currentIndx the current page index
     * @return the next page index
     */
    public int determinePreviousPage(int currentIndx) {
    	int resultIndx = currentIndx;
    	
    	ViewEditorManager editorManager = ViewEditorManager.getInstance();
		// Save current page selections to manager
		savePageSelectionsToManager(currentIndx);
    	// ---------------------------------------------------------------------------
    	// On SELECT_TABLE_COLS page.  The previous is always the first page
    	// ---------------------------------------------------------------------------
    	if(currentIndx==LAST_PAGE_SELECT_TABLE_COLS_INDX) {
    		resultIndx = FIRST_PAGE_SELECT_TABLES_INDX;
        // ------------------------------------------------------------------------------
        // On DEFINE_TEMPLATE page.  The previous depends on number of template sources
        // ------------------------------------------------------------------------------
    	} else if(currentIndx==DEFINE_TEMPLATE_PAGE_INDX) {
    		int tablesNeedingTemplates = editorManager.getNumberTablesRequiringTemplates();
    		if(tablesNeedingTemplates==1) {
    			resultIndx = FIRST_PAGE_SELECT_TABLES_INDX;
    		} else if(tablesNeedingTemplates==2) {
    			// If on the second table, then stay on the DEFINE_TEMPLATE_PAGE - but update for first table
    			if(editorManager.getTemplateTableIndex()==1) {
        			editorManager.setTemplateTableIndex(0);
        			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
        		// If on the first table, then go to start page
    			} else if(editorManager.getTemplateTableIndex()==0) {
    				resultIndx = FIRST_PAGE_SELECT_TABLES_INDX;
    			}
    		}
        // ------------------------------------------------------------------------------
        // On DEFINE_JOIN page.  The previous depends on number of template sources
        // ------------------------------------------------------------------------------
    	} else if(currentIndx==LAST_PAGE_DEFINE_JOIN_INDX) {
    		int tablesNeedingTemplates = editorManager.getNumberTablesRequiringTemplates();
    		// No template tables.  go to first page.
    		if(tablesNeedingTemplates==0) {
    			resultIndx = FIRST_PAGE_SELECT_TABLES_INDX;
    		// One template table.  go to DEFINE_TEMPLATE page populated with it.
    		} else if(tablesNeedingTemplates==1) {
    			// determine which of the two tables requires the template
    			if(editorManager.tableRequiresTemplate(1)) {
    				editorManager.setTemplateTableIndex(1);
    			} else {
    				editorManager.setTemplateTableIndex(0);
    			}
    			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
    		// Two template tables.  go to DEFINE_TEMPLATE page, populated with second table
    		} else if(tablesNeedingTemplates==2) {
    			editorManager.setTemplateTableIndex(1);
    			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
    		}
    	} else {
    		resultIndx = currentIndx -1;
    	}
    	
    	return resultIndx;
    }
    
    /**
     * Save the page selections to the editorManager
     * @param indx the page index
     */
    private void savePageSelectionsToManager(int indx) {
		ViewEditorManager editorManager = ViewEditorManager.getInstance();
		if(indx==LAST_PAGE_SELECT_TABLE_COLS_INDX) {
    		List<String> selectedColumnNames = selectTableColumnsPage.getSelectedColumnNames();
    		List<String> selectedColumnTypes = selectTableColumnsPage.getSelectedColumnTypes();
    		editorManager.setSelectedColumns(0, selectedColumnNames);
    		editorManager.setSelectedColumnTypes(0, selectedColumnTypes);
		}
    }
    
    private void updatePage(int indx) {
    	if(indx==FIRST_PAGE_SELECT_TABLES_INDX) {
    		selectTablesPage.update();
    	} else if(indx==LAST_PAGE_SELECT_TABLE_COLS_INDX) {
    		selectTableColumnsPage.update();
    	} else if(indx==DEFINE_TEMPLATE_PAGE_INDX) {
    		defineTemplateDdlPage.update();
    	} else if(indx==LAST_PAGE_DEFINE_JOIN_INDX) {
    		defineJoinCriteriaPage.update();
    	}
    }
    
    /**
     * Set button text and enabled state
     * @param type the button type
     * @enabled 'true' if enabled, 'false' if not.
     */
    public void setNextReplaceButton(NextReplaceButton type, boolean enabled) {
    	if(type==NextReplaceButton.NEXT) {
    		nextOrReplaceButton.setIcon(IconType.ANGLE_RIGHT);
    		nextOrReplaceButton.setIconPosition(IconPosition.RIGHT);
    		nextOrReplaceButton.setText(NEXT_BUTTON_TEXT);
        	nextOrReplaceButton.setEnabled(enabled);
    	} else {
    		nextOrReplaceButton.setIcon(IconType.ARROW_DOWN);
    		nextOrReplaceButton.setIconPosition(IconPosition.RIGHT);
    		nextOrReplaceButton.setText(REPLACE_BUTTON_TEXT);
    		nextOrReplaceButton.setEnabled(enabled);
    	}
    	if(enabled) {
    		nextOrReplaceButton.setType(ButtonType.PRIMARY);
    	} else {
    		nextOrReplaceButton.setType(ButtonType.DEFAULT);
    	}
    }
    
    /**
     * Set button text and icon
     * @param type the button type
     */
    public void setNextReplaceButton(NextReplaceButton type) {
    	if(type==NextReplaceButton.NEXT) {
    		nextOrReplaceButton.setIcon(IconType.ANGLE_RIGHT);
    		nextOrReplaceButton.setIconPosition(IconPosition.RIGHT);
    		nextOrReplaceButton.setText(NEXT_BUTTON_TEXT);
    	} else {
    		nextOrReplaceButton.setIcon(IconType.ARROW_DOWN);
    		nextOrReplaceButton.setIconPosition(IconPosition.RIGHT);
    		nextOrReplaceButton.setText(REPLACE_BUTTON_TEXT);
    	}
    	boolean isEnabled = nextOrReplaceButton.isEnabled();
    	if(isEnabled) {
    		nextOrReplaceButton.setType(ButtonType.PRIMARY);
    	} else {
    		nextOrReplaceButton.setType(ButtonType.DEFAULT);
    	}
    }
    
}