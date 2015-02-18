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

import java.util.Collections;

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
import org.teiid.webui.client.utils.DdlHelper;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

@Dependent
@Templated("./TemplatesEditorPanel.html")
public class TemplatesEditorPanel extends Composite {

    @Inject
    private ClientMessages i18n;
    
    @Inject Event<UiEvent> setDdlEvent;
        
    @Inject @DataField("lbl-template-editor-message")
    protected Label samplesMessageLabel;
    
    @Inject @DataField("listbox-template-editor-templates")
    protected ListBox ddlTemplatesListBox;

    @Inject @DataField("textarea-template-editor-ddl-area")
    protected TextArea sampleDdlTextArea;
    
    @Inject @DataField("btn-template-editor-apply")
    protected Button applySampleDdlButton;
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	samplesMessageLabel.setText(i18n.format("template-editor.message"));

    	populateDdlTemplatesListBox();
    	sampleDdlTextArea.setText(DdlHelper.getDdlTemplate(DdlHelper.DDL_TEMPLATE_SINGLE_SOURCE));
    	// Change Listener for Type ListBox
    	ddlTemplatesListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
        		String ddlSample = null;
        		String template = getSelectedDdlTemplate(); 
        		if(template.equals(DdlHelper.DDL_TEMPLATE_SINGLE_SOURCE)) {
        			ddlSample = DdlHelper.getDdlTemplate(DdlHelper.DDL_TEMPLATE_SINGLE_SOURCE);
        		} else if(template.equals(DdlHelper.DDL_TEMPLATE_TWO_SOURCE_JOIN)) {
        			ddlSample = DdlHelper.getDdlTemplate(DdlHelper.DDL_TEMPLATE_TWO_SOURCE_JOIN);
        		} else if(template.equals(DdlHelper.DDL_TEMPLATE_FLAT_FILE)) {
        			ddlSample = DdlHelper.getDdlTemplate(DdlHelper.DDL_TEMPLATE_FLAT_FILE);
        		} else if(template.equals(DdlHelper.DDL_TEMPLATE_WEBSERVICE)) {
        			ddlSample = DdlHelper.getDdlTemplate(DdlHelper.DDL_TEMPLATE_WEBSERVICE);
        		} 
        		sampleDdlTextArea.setText(ddlSample);
        	}
        });
    	
    	applySampleDdlButton.setEnabled(true);
    	
    	// Tooltips
    	sampleDdlTextArea.setTitle(i18n.format("template-editor.sampleDdlTextArea.tooltip"));
    	applySampleDdlButton.setTitle(i18n.format("template-editor.applySampleDdlButton.tooltip"));
    	ddlTemplatesListBox.setTitle(i18n.format("template-editor.ddlTemplatesListBox.tooltip"));
    }
    
    /**
     * Init the List of Service actions
     */
    private void populateDdlTemplatesListBox( ) {
    	// Make sure clear first
    	ddlTemplatesListBox.clear();

    	ddlTemplatesListBox.insertItem(DdlHelper.DDL_TEMPLATE_SINGLE_SOURCE, 0);
    	ddlTemplatesListBox.insertItem(DdlHelper.DDL_TEMPLATE_TWO_SOURCE_JOIN, 1);
    	ddlTemplatesListBox.insertItem(DdlHelper.DDL_TEMPLATE_FLAT_FILE, 2);
    	ddlTemplatesListBox.insertItem(DdlHelper.DDL_TEMPLATE_WEBSERVICE, 3);
    	
    	// Initialize by setting the selection to the first item.
    	ddlTemplatesListBox.setSelectedIndex(0);
    }
    
    /**
     * Get the selected action from the MoreActions dropdown
     * @return
     */
    private String getSelectedDdlTemplate() {
    	int index = ddlTemplatesListBox.getSelectedIndex();
    	return ddlTemplatesListBox.getValue(index);
    }
    
    /**
     * Event handler that fires when the user clicks the Apply button.
     * @param event
     */
    @EventHandler("btn-template-editor-apply")
    public void onApplyButtonClick(ClickEvent event) {
    	String ddlTemplate = sampleDdlTextArea.getText();
    	if(ddlTemplate.isEmpty()) {
    		Window.alert("Please select a template");
    		return;
    	}
		UiEvent uiEvent = new UiEvent(UiEventType.VIEW_DEFN_REPLACE_FROM_TEMPLATES_EDITOR);
		uiEvent.setViewDdl(ddlTemplate);
		uiEvent.setViewSources(Collections.<String>emptyList());
		
		setDdlEvent.fire(uiEvent);
    }
    
}