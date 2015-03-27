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
import org.teiid.webui.client.widgets.validation.EmptyNameValidator;
import org.teiid.webui.client.widgets.validation.TextChangeListener;
import org.teiid.webui.client.widgets.validation.ValidatingTextBoxHoriz;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.DataSourcePropertyBean;
import org.teiid.webui.share.services.StringUtils;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Composite for display of DataSource names
 */
@Dependent
public class DataSourcePropertyEditor extends Composite {

	private static final String PASSWORD_KEY = "password"; //$NON-NLS-1$
	
    protected VerticalPanel panel = new VerticalPanel();
    protected Label label = new Label();

    private List<DataSourcePropertyBean> propertyList = new ArrayList<DataSourcePropertyBean>();
    private Map<String,Composite> nameTextBoxMap = new HashMap<String,Composite>();
    
    @Inject
    private ClientMessages i18n;
    
	@Inject Event<DataSourcePropertyBean> propertyChangeEvent;
	
    public DataSourcePropertyEditor() {
        initWidget( panel );
    }
    
    public void setProperties(List<DataSourcePropertyBean> properties) {
    	this.propertyList.clear();
    	this.nameTextBoxMap.clear();
    	VerticalPanel allPropsPanel = new VerticalPanel();
    	
    	for(DataSourcePropertyBean prop : properties) {  
    		// Create text box for the property
    		ValidatingTextBoxHoriz widget = createTextBox(prop);
    		
    		// Add widget to the panel
    		allPropsPanel.add(widget);
    		
    		this.propertyList.add(prop);
    		this.nameTextBoxMap.put(prop.getName(), widget);
    	}
    	panel.clear();
    	if(properties.size()>0) {
    		panel.add(allPropsPanel);
    	}
    }
    
    private boolean isPassword(DataSourcePropertyBean prop) {
		String propName = prop.getName();
		if( propName!=null && propName.equalsIgnoreCase(PASSWORD_KEY) ) {
			return true;
		}
		return false;
    }
    
    /*
     * Create a textBox for the property
     */
    private ValidatingTextBoxHoriz createTextBox(DataSourcePropertyBean prop) {
		ValidatingTextBoxHoriz textBox = new ValidatingTextBoxHoriz();
		
		// If property is required, validate for empty name
		if(prop.isRequired() && prop.isModifiable()) {
			textBox.addValidator(new EmptyNameValidator());
		}
		
		// Label is the display name of the property
		textBox.setLabel(prop.getDisplayName());
		
		// Tooltip is the description
		String descrip = prop.getDescription();
		if(!StringUtils.isEmpty(descrip)) {
			textBox.setTitle(descrip);
		}
		
		// If property is a password, it is masked
		if(isPassword(prop)) {
			textBox.setPassword(true);
		}
		
		textBox.addTextChangeListener(new TextChangeListener() {
            @Override
			public void textChanged(  ) {
            	updatePropertyValues();
            }
        });
		
		textBox.setText(prop.getValue());
		
		return textBox;
    }
    
    /**
     * Get the property beans that are required or have a non-default value
     * @return the list
     */
    public List<DataSourcePropertyBean> getBeansWithRequiredOrNonDefaultValue() {
    	List<DataSourcePropertyBean> tableRows = getProperties();
    	
    	List<DataSourcePropertyBean> resultBeans = new ArrayList<DataSourcePropertyBean>();
    	for(DataSourcePropertyBean propBean : tableRows) {
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
    	for(DataSourcePropertyBean propBean : getProperties()) {
    		String propName = propBean.getName();
    		ValidatingTextBoxHoriz textBox = (ValidatingTextBoxHoriz)this.nameTextBoxMap.get(propName);
    		propBean.setValue(textBox.getText());
    	}
        propertyChangeEvent.fire(new DataSourcePropertyBean());
    }
    
    public void clear() {
    	setProperties(Collections.<DataSourcePropertyBean>emptyList());
    }
    
    public List<DataSourcePropertyBean> getProperties( ) {
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

    	for(DataSourcePropertyBean propBean : getProperties()) {
    		String propName = propBean.getName();
    		String propValue = propBean.getValue();
    		boolean isRequired = propBean.isRequired();

    		// Check that required properties have a value
    		if(isRequired) {
    			if(propValue==null || propValue.trim().length()==0) {
    				status = i18n.format( "ds-properties-editor.value-required-message",propName);
    				break;
    			}
    		}
    	}

    	return status;
    }
    
   
    
    public boolean anyPropertyHasChanged() {
    	boolean hasChanges = false;
    	for(DataSourcePropertyBean propBean : getProperties()) {
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
