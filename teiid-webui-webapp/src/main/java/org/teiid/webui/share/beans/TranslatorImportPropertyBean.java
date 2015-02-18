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
package org.teiid.webui.share.beans;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * A data bean for returning import property information for a source vdb. 
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class TranslatorImportPropertyBean {

    private String name;
    private String displayName;
    private String value;
    private String originalValue;
    private String defaultValue;
    private boolean isModifiable;
    private boolean isRequired;
    private boolean isMasked;
    private String description;

    /**
     * Constructor.
     */
    public TranslatorImportPropertyBean() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @return the name
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * @return the value
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }
    
    /**
     * @return the value
     */
    public String getOriginalValue() {
        return this.originalValue;
    }

    /**
     * @return the value
     */
    public boolean isModifiable() {
        return this.isModifiable;
    }
    
    /**
     * @return the value
     */
    public boolean isRequired() {
        return this.isRequired;
    }

    /**
     * @return the value
     */
    public boolean isMasked() {
        return this.isMasked;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * @param originalValue the originalValue to set
     */
    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    /**
     * @param isModifiable the isModifiable to set
     */
    public void setModifiable(boolean isModifiable) {
        this.isModifiable = isModifiable;
    }

    /**
     * @param isRequired the isRequired to set
     */
    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    /**
     * @param isMasked the isMasked to set
     */
    public void setMasked(boolean isMasked) {
        this.isMasked = isMasked;
    }
    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
}
