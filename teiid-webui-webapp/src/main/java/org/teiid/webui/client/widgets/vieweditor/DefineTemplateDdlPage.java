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

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.NotificationService;
import org.teiid.webui.client.services.QueryRpcService;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.utils.DdlHelper;
import org.teiid.webui.client.widgets.validation.ExtendedTextArea;
import org.teiid.webui.client.widgets.validation.TextChangeEvent;
import org.teiid.webui.client.widgets.validation.TextChangeEventHandler;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.services.StringUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("./DefineTemplateDdlPage.html")
public class DefineTemplateDdlPage extends Composite {

    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
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
    protected ExtendedTextArea ddlTextArea;
    
    @Inject @DataField("btn-replace")
    protected Button replaceDdlButton;
    
    private ViewEditorWizardPanel wizard;
	private ViewEditorManager editorManager = ViewEditorManager.getInstance();
   
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	messageLabel.setText("This type of sources requires a SQL transformation - please edit");
    	
    	ddlTextArea.addTextChangeEventHandler(new TextChangeEventHandler() {
            @Override
			public void onTextChange(TextChangeEvent event) {
            	int tableIndx = editorManager.getDefineTemplateTableIndex();
            	editorManager.setSourceTransformationSQL(tableIndx, ddlTextArea.getText());
            }
        });
    }
    
    /**
     * Update the panel right before it is shown
     */
    public void update() {
    	ViewEditorManager editorManager = ViewEditorManager.getInstance();
    	
    	// Get the table index of the Template table
    	int tableIndx = editorManager.getDefineTemplateTableIndex();

		String sourceType = editorManager.getSourceTypeForTable(tableIndx);
		String sourceName = editorManager.getSourceNameForTable(tableIndx);
		
    	// More than one table - the transformation SQL will be displayed.
    	if(editorManager.getTables().size()>1) {
    		String alias = (tableIndx==0) ? "X" : "Y";
    		String sql = DdlHelper.getSourceTransformationSQLTemplate(sourceType,sourceName,alias);
    	    editorManager.setSourceTransformationSQL(tableIndx, sql);
    		setDdl(sql);
    		
			setReplaceDdlButtonVisible(false);
    	// Only one table - set the View DDL and enable the replace button
    	} else {
			String ddl = DdlHelper.getODataViewDdlTemplate(sourceType, Constants.SERVICE_VIEW_NAME, sourceName, "A");
    		setDdl(ddl);
    		
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
    	this.wizard = wizard;
    }
    
    /**
     * Gets the DDL displayed in the DDL area
     * @return the DDL
     */
    public String getDdl() {
    	return this.ddlTextArea.getText();
    }
    
    /**
     * Sets the DDL displayed in the DDL area
     * @param ddl the DDL
     */
    public void setDdl(String ddl) {
    	this.ddlTextArea.setText(ddl);
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
    	String viewDdl = this.ddlTextArea.getText();
    	List<String> sources = ViewEditorManager.getInstance().getSources();
     	
		UiEvent uiEvent = new UiEvent(UiEventType.VIEW_DEFN_REPLACE_FROM_SSOURCE_EDITOR);
		uiEvent.setViewDdl(viewDdl);
		uiEvent.setViewSources(sources);
		
		setDdlEvent.fire(uiEvent);
    }
           
}