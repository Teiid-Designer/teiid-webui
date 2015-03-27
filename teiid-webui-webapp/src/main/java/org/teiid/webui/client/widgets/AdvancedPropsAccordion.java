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

import java.util.List;

import javax.enterprise.context.Dependent;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.PanelCollapse;
import org.teiid.webui.share.beans.DataSourcePropertyBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class AdvancedPropsAccordion extends Composite {
    
    interface AdvancedPropsAccordionBinder extends UiBinder<Widget, AdvancedPropsAccordion> {}
    private static AdvancedPropsAccordionBinder uiBinder = GWT.create(AdvancedPropsAccordionBinder.class);
 
    @UiField
    PanelCollapse collapseAccordion;

    @UiField(provided = true)
    DataSourcePropertyEditor propertyEditor;
    
    @UiField
    Anchor accordionAnchor;


    public AdvancedPropsAccordion() {
    	propertyEditor = new DataSourcePropertyEditor();
    	
        // Init the accordion from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        
        // init after widgets created
        init();
    }
    
    private void init() {
    }
    
    public boolean hasProperties() {
    	return !propertyEditor.getProperties().isEmpty();
    }
    
    public void clearProperties() {
    	propertyEditor.clear();
    }
    
    public void setProperties(List<DataSourcePropertyBean> properties) {
    	propertyEditor.setProperties(properties);
    }
    
    public void setText(String txt) {
    	accordionAnchor.setText(txt);
    }
    
    public String getStatus() {
    	return propertyEditor.getStatus();
    }
    
    public boolean anyPropertyHasChanged() {
    	return propertyEditor.anyPropertyHasChanged();
    }
    
    public List<DataSourcePropertyBean> getBeansWithRequiredOrNonDefaultValue() {
    	return propertyEditor.getBeansWithRequiredOrNonDefaultValue();
    }
    
}