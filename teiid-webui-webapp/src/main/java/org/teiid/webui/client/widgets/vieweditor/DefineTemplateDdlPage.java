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
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.utils.DdlHelper;
import org.teiid.webui.client.widgets.validation.ExtendedTextArea;
import org.teiid.webui.client.widgets.validation.TextChangeEvent;
import org.teiid.webui.client.widgets.validation.TextChangeEventHandler;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.services.StringUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("./DefineTemplateDdlPage.html")
/**
 * ViewEditor wizard page for definition of a template table's transformation
 */
public class DefineTemplateDdlPage extends Composite {

    @Inject
    private ClientMessages i18n;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject Event<UiEvent> setDdlEvent;
    
    @Inject @DataField("lbl-define-template-title")
    protected Label titleLabel;
    @Inject @DataField("lbl-define-template-message")
    protected Label messageLabel;
    
    @Inject @DataField("textarea-define-template-ddl")
    protected ExtendedTextArea textArea;
    
    @Inject @DataField("btn-replace")
    protected Button replaceDdlButton;
    
	private ViewEditorManager editorManager = ViewEditorManager.getInstance();
   
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	messageLabel.setText("This type of sources requires a SQL transformation - please edit");
    	
    	textArea.addTextChangeEventHandler(new TextChangeEventHandler() {
            @Override
			public void onTextChange(TextChangeEvent event) {
            	int tableIndx = editorManager.getTemplateTableIndex();
            	int nTables = editorManager.getTables().size();
            	// Update editor manager
            	if(nTables==1) {
            		editorManager.setTemplateDDL(tableIndx, textArea.getText());
            	} else {
            		editorManager.setTemplateSQL(tableIndx, textArea.getText());
            	}
            }
        });
    	
    	replaceDdlButton.setIcon(IconType.ARROW_DOWN);
    }
    
    /**
     * Refresh the panel using state from the ViewEditorManager
     */
    public void update() {
    	// Get the table index of the Template table
    	int tableIndx = editorManager.getTemplateTableIndex();
    	int nTables = editorManager.getTables().size();
    	
    	// Get any existing DDL and SQL
    	String existingDdl = editorManager.getTemplateDDL(tableIndx);
    	String existingSql = editorManager.getTemplateSQL(tableIndx);
    	
    	// Get source and type for the table
		String sourceType = editorManager.getSourceTypeForTable(tableIndx);
		String sourceName = editorManager.getSourceNameForTable(tableIndx);
		
    	// More than one table - the transformation SQL will be displayed.
    	if(nTables>1) {
    		// Clear any previous ddl
    		editorManager.setTemplateDDL(tableIndx, null);
    		// Has existing SQL - use it
    		if(!StringUtils.isEmpty(existingSql)) {
    			setText(existingSql);
    	    // No existing SQL - generate new
    		} else {
        		String alias = (tableIndx==0) ? "X" : "Y";
        		String sql = DdlHelper.getSourceTransformationSQLTemplate(sourceType,sourceName,alias);
        	    editorManager.setTemplateSQL(tableIndx, sql);
        		setText(sql);
    		}
			setReplaceDdlButtonVisible(false);
    	// Only one table - set the View DDL and enable the replace button
    	} else {
    		// Clear any previous SQL
    		editorManager.setTemplateSQL(tableIndx, null);
    		// Has existing DDL - use it
    		if(!StringUtils.isEmpty(existingDdl)) {
    			setText(existingDdl);
    	    // No existing DDL - generate new
    		} else {
    			String ddl = DdlHelper.getODataViewDdlTemplate(sourceType, Constants.SERVICE_VIEW_NAME, sourceName, "A");
        	    editorManager.setTemplateDDL(tableIndx, ddl);
        		setText(ddl);
    		}
			setReplaceDdlButtonVisible(true);
    	}
		
		// Determine page number, set title
		int pageNumber = 2;
		int nTemplatePages = editorManager.getNumberTablesRequiringTemplates();
		if(nTemplatePages==2 && tableIndx==1) {
			pageNumber = 3;
		}
		String srcName = editorManager.getSourceNameForTable(tableIndx);
        titleLabel.setText(i18n.format("define-template-page.title", pageNumber, srcName));
    }
    
    /**
     * Set the owner wizardPanel
     * @param wizard the wizard
     */
    public void setWizard(ViewEditorWizardPanel wizard) {
    }
    
    /**
     * Gets the text displayed in the text area
     * @return the text
     */
    public String getText() {
    	return this.textArea.getText();
    }
    
    /**
     * Sets the text displayed in the text area
     * @param text the text
     */
    public void setText(String text) {
    	this.textArea.setText(text);
    }
    
    /**
     * Sets whether the Replace DDL button is shown.
     * @param isVisible 'true' if the replace button is shown, 'false' if not.
     */
    public void setReplaceDdlButtonVisible(boolean isVisible) {
    	this.replaceDdlButton.setVisible(isVisible);
    }
    
    /**
     * Event handler that fires when the user clicks the Replace DDL button.
     * @param event
     */
    @EventHandler("btn-replace")
    public void onReplaceDdlButtonClick(ClickEvent event) {
    	String viewDdl = this.textArea.getText();
    	List<String> sources = editorManager.getSources();
     	
		UiEvent uiEvent = new UiEvent(UiEventType.VIEW_DEFN_REPLACE_FROM_SSOURCE_EDITOR);
		uiEvent.setViewDdl(viewDdl);
		uiEvent.setViewSources(sources);
		
		setDdlEvent.fire(uiEvent);
    }
           
}