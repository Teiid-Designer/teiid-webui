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
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.utils.DdlHelper;
import org.uberfire.lifecycle.OnStartup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;

@Dependent
@Templated("./ViewEditorWizardPanel.html")
public class ViewEditorWizardPanel extends Composite {

	private static final int FIRST_PAGE_SELECT_TABLES_INDX = 0;
	private static final int LAST_PAGE_SELECT_TABLE_COLS_INDX = 1;
	private static final int DEFINE_TEMPLATE_PAGE_INDX = 2;
	private static final int LAST_PAGE_DEFINE_JOIN_INDX = 3;
	
    @Inject
    private ClientMessages i18n;
    
    @Inject
    protected TeiidRpcService teiidService;
    
    @Inject @DataField("wizard-deckpanel")
    protected DeckPanel wizardDeckPanel;
    
    @Inject @DataField("btn-resetPage")
    protected Button resetPageButton;
    @Inject @DataField("btn-previousPage")
    protected Button previousPageButton;
    @Inject @DataField("btn-nextPage")
    protected Button nextPageButton;

    @Inject 
    protected SelectTablesPage selectTablesPage;
    @Inject 
    protected SelectTableColumnsPage selectTableColumnsPage;
    @Inject 
    protected DefineTemplateDdlPage defineTemplateDdlPage;
    @Inject 
    protected DefineJoinCriteriaPage defineJoinCriteriaPage;
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	selectTablesPage.setWizard(this);
    	selectTableColumnsPage.setWizard(this);
    	defineTemplateDdlPage.setWizard(this);
    	defineJoinCriteriaPage.setWizard(this);
    	
    	resetPageButton.setEnabled(true);
    	previousPageButton.setEnabled(false);
    	nextPageButton.setEnabled(false);
    	
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
    
    /**
     * Resets any saved state
     */
    public void reset() {
    	// Start Over clicked - clear the Manager contents
    	ViewEditorManager.getInstance().clear();
    	
    	// Updates the starting page
    	updatePage(FIRST_PAGE_SELECT_TABLES_INDX);
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
     * Event handler that fires when the user clicks the NextPage button.
     * @param event
     */
    @EventHandler("btn-nextPage")
    public void onNextPageButtonClick(ClickEvent event) {
    	showNextPage();
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
     * Show the next page of the wizard
     */
    public void showNextPage() {
    	// Get currently visible page
    	int indx = wizardDeckPanel.getVisibleWidget();
    	
    	// If going to next page, previous and startOver buttons are enabled
    	setPreviousButtonEnabled(true);
 
    	int nextIndx = determineNextPage(indx);
    	
    	// Update next page, then show it
    	updatePage(nextIndx);
    	wizardDeckPanel.showWidget(nextIndx);
    	    	
    	// If on last page, next button is enabled
    	if(nextIndx==LAST_PAGE_SELECT_TABLE_COLS_INDX || 
    	   nextIndx==LAST_PAGE_DEFINE_JOIN_INDX ||
    	   (nextIndx==DEFINE_TEMPLATE_PAGE_INDX && ViewEditorManager.getInstance().getTables().size()==1)) {
    		setNextButtonEnabled(false);
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
        			editorManager.setDefineTemplateTableIndex(0);
        			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
        		// No template required, show the 'pick columns' page
        		} else {
        			resultIndx = LAST_PAGE_SELECT_TABLE_COLS_INDX;
        		}
    		} else if(editorManager.getTables().size()==2) {
    			List<Boolean> tableRequiredStates = editorManager.getTableTemplateRequiredStates();
    			// Determine if table one requires a template
        		if(tableRequiredStates.get(0)) {
        			editorManager.setDefineTemplateTableIndex(0);
        			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
        		// Determine if table two requires a template
        		} else if(tableRequiredStates.get(1)) {
        			editorManager.setDefineTemplateTableIndex(1);
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
        			int tableIndx = editorManager.getDefineTemplateTableIndex();
        			if(tableIndx==1) {
            			resultIndx = LAST_PAGE_DEFINE_JOIN_INDX;
        			} else if(tableIndx==0) {
            			editorManager.setDefineTemplateTableIndex(1);
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
    	
    	// If going to previous page, next button is enabled
    	setNextButtonEnabled(true);
    	
    	int prevIndx = determinePreviousPage(indx);
    	updatePage(prevIndx);
    	wizardDeckPanel.showWidget(prevIndx);
    	
    	// If on first page, previous and start buttons disabled
    	if(prevIndx==FIRST_PAGE_SELECT_TABLES_INDX) {
    		setPreviousButtonEnabled(false);
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
    			if(editorManager.getDefineTemplateTableIndex()==1) {
        			editorManager.setDefineTemplateTableIndex(0);
        			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
        		// If on the first table, then go to start page
    			} else if(editorManager.getDefineTemplateTableIndex()==0) {
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
    			editorManager.setDefineTemplateTableIndex(0);
    			resultIndx = DEFINE_TEMPLATE_PAGE_INDX;
    		// Two template tables.  go to DEFINE_TEMPLATE page, populated with second table
    		} else if(tablesNeedingTemplates==2) {
    			editorManager.setDefineTemplateTableIndex(1);
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
    	switch (indx) {
    	case FIRST_PAGE_SELECT_TABLES_INDX :
    		//List<String> selctedTables = selectTablesPage.getSelectedTables();
    		//editorManager.setTables(selctedTables);
    	case LAST_PAGE_SELECT_TABLE_COLS_INDX :
    		List<String> selectedColumnNames = selectTableColumnsPage.getSelectedColumnNames();
    		List<String> selectedColumnTypes = selectTableColumnsPage.getSelectedColumnTypes();
    		editorManager.setSelectedColumns(0, selectedColumnNames);
    		editorManager.setSelectedColumnTypes(0, selectedColumnTypes);
    	case DEFINE_TEMPLATE_PAGE_INDX :
    		String ddl = defineTemplateDdlPage.getDdl();
    		int tableIndx = editorManager.getDefineTemplateTableIndex();
    		editorManager.setSourceTransformationSQL(tableIndx, ddl);
    	case LAST_PAGE_DEFINE_JOIN_INDX :
    		//defineJoinCriteriaPage.update();
    	default: 

    	}
    }
    
    private void updatePage(int indx) {

    	switch (indx) {
    	case FIRST_PAGE_SELECT_TABLES_INDX :
    		selectTablesPage.update();
    	case LAST_PAGE_SELECT_TABLE_COLS_INDX :
    		selectTableColumnsPage.update();
    	case DEFINE_TEMPLATE_PAGE_INDX :
    		defineTemplateDdlPage.update();
    	case LAST_PAGE_DEFINE_JOIN_INDX :
    		defineJoinCriteriaPage.update();
    	default: 

    	}
    }
    
    /**
     * Set enabled state of Next button
     * @enabled 'true' if enabled, 'false' if not.
     */
    public void setNextButtonEnabled(boolean enabled) {
    	this.nextPageButton.setEnabled(enabled);
    }

    /**
     * Set enabled state of Previous button
     * @enabled 'true' if enabled, 'false' if not.
     */
    public void setPreviousButtonEnabled(boolean enabled) {
    	this.previousPageButton.setEnabled(enabled);
    }
        
}