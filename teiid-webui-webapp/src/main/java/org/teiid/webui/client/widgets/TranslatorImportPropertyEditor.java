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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.TranslatorImportPropertyBean;
import org.teiid.webui.share.services.StringUtils;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Composite for display of Translator Import property names
 */
@Dependent
public class TranslatorImportPropertyEditor extends Composite {

	private static final String NAME_WIDTH = "220px";
	private static final String VALUE_WIDTH = "600px";
	
    protected VerticalPanel panel = new VerticalPanel();
    protected Label label = new Label();

    private List<TranslatorImportPropertyBean> propertyList = new ArrayList<TranslatorImportPropertyBean>();
    private Map<String,TextBox> nameTextBoxMap = new HashMap<String,TextBox>();
    
    @Inject
    private ClientMessages i18n;
    
	@Inject Event<TranslatorImportPropertyBean> propertyChangeEvent;
	
    public TranslatorImportPropertyEditor() {
        initWidget( panel );
    }
    
    public void setProperties(List<TranslatorImportPropertyBean> properties) {
    	this.propertyList.clear();
    	this.nameTextBoxMap.clear();
    	VerticalPanel allPropsPanel = new VerticalPanel();
    	
    	for(TranslatorImportPropertyBean prop : properties) {     		
        	HorizontalPanel nameValuePanel = new HorizontalPanel();
    		Label nameLabel = new Label();
        	DOM.setStyleAttribute(nameLabel.getElement(), "fontWeight", "bold");
    		nameLabel.setWidth(NAME_WIDTH);
    		nameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    		nameLabel.setText(prop.getDisplayName()+" --");
    		String descrip = prop.getDescription();
    		if(!StringUtils.isEmpty(descrip)) {
    			nameLabel.setTitle(descrip);
    		}
    		nameValuePanel.add(nameLabel);
    		
            TextBox valueTextBox = new TextBox();

            	valueTextBox.setWidth(VALUE_WIDTH);
    		valueTextBox.setText(prop.getValue());
    		if(!StringUtils.isEmpty(descrip)) {
    			valueTextBox.setTitle(descrip);
    		}
    		valueTextBox.addKeyUpHandler(new KeyUpHandler() {
    			@Override
    			public void onKeyUp(KeyUpEvent event) {
    				updatePropertyValues();
    			}
    		});
    		valueTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
    			@Override
    			public void onValueChange(ValueChangeEvent<String> event) {
    				updatePropertyValues();
    			}
    		});
    		nameValuePanel.add(valueTextBox);
    		
    		allPropsPanel.add(nameValuePanel);
    		
    		this.propertyList.add(prop);
    		this.nameTextBoxMap.put(prop.getName(), valueTextBox);
    	}
    	panel.clear();
    	if(properties.size()>0) {
    		panel.add(allPropsPanel);
    	}
    }
    
    
    public List<TranslatorImportPropertyBean> getBeansWithRequiredOrNonDefaultValue() {
    	List<TranslatorImportPropertyBean> tableRows = getProperties();
    	
    	List<TranslatorImportPropertyBean> resultBeans = new ArrayList<TranslatorImportPropertyBean>();
    	for(TranslatorImportPropertyBean propBean : tableRows) {
    		if(propBean.isRequired()) {
    			resultBeans.add(propBean);
    		} else {
        		String defaultValue = propBean.getDefaultValue();
        		String value = propBean.getValue();
        		if(!StringUtils.valuesAreEqual(value, defaultValue)) {
        			resultBeans.add(propBean);
        		}
    		}
    	}
    	return resultBeans;
    }
    
    public void updatePropertyValues() {
    	for(TranslatorImportPropertyBean propBean : getProperties()) {
    		String propName = propBean.getName();
    		TextBox textBox = this.nameTextBoxMap.get(propName);
    		propBean.setValue(textBox.getText());
    	}
        propertyChangeEvent.fire(new TranslatorImportPropertyBean());
    }
    
    public void clear() {
    	setProperties(Collections.<TranslatorImportPropertyBean>emptyList());
    }
    
    public List<TranslatorImportPropertyBean> getProperties( ) {
    	return this.propertyList;
    }
        
    /*
     * Returns an overall status of the table properties.  Currently the only check is that required properties
     * have a value, but this can be expanded in the future.  If all properties pass, the status is 'OK'. If not, a
     * String identifying the problem is returned.
     * @return the status - 'OK' if no problems.
     */
    public String getStatus() {
    	// Assume 'OK' until a problem is found
    	String status = Constants.OK;

    	for(TranslatorImportPropertyBean propBean : getProperties()) {
    		String propName = propBean.getName();
    		String propValue = propBean.getValue();
    		boolean isRequired = propBean.isRequired();

    		// Check that required properties have a value
    		if(isRequired) {
    			if(propValue==null || propValue.trim().length()==0) {
    				status = i18n.format( "ds-import-properties-editor.value-required-message",propName);
    				break;
    			}
    		}
    	}

    	return status;
    }
    
   
    
    public boolean anyPropertyHasChanged() {
    	boolean hasChanges = false;
    	for(TranslatorImportPropertyBean propBean : getProperties()) {
    		String originalValue = propBean.getOriginalValue();
    		String value = propBean.getValue();
    		if(!StringUtils.valuesAreEqual(value, originalValue)) {
    			hasChanges = true;
    			break;
    		}
    	}
    	return hasChanges;
    }
    
}
