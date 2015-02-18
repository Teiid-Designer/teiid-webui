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
package org.teiid.webui.client.dialogs;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.widgets.IImportCompletionHandler;
import org.teiid.webui.client.widgets.ImportDataSourceTypeFormSubmitHandler;
import org.teiid.webui.client.widgets.TemplatedFormPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("./UploadContentPanel.html")
public class UploadContentPanel extends Composite {

    @Inject
    private ClientMessages i18n;
    
	@Inject @DataField("upload-content-form")
	protected TemplatedFormPanel form;

	@Inject @DataField("label-choose-file-message")
	protected Label chooseFileLabel;
	
	@Inject @DataField("label-enter-deployname-message")
	protected Label enterDeployNameLabel;

    @Inject
    private Instance<ImportDataSourceTypeFormSubmitHandler> formHandlerFactory;
    
    private ImportDataSourceTypeFormSubmitHandler formHandler;

	/**
	 * Called after construction.
	 */
	@PostConstruct
	protected void postConstruct() {
		chooseFileLabel.setText(i18n.format("upload-dtype-dialog.dialog-choosefile-message"));
		enterDeployNameLabel.setText(i18n.format("upload-dtype-dialog.dialog-entername-message"));
		
		formHandler = formHandlerFactory.get();
		form.addSubmitHandler(formHandler);
		form.addSubmitCompleteHandler(formHandler);
	}
	
    /**
     * @return the completionHandler
     */
    public IImportCompletionHandler getCompletionHandler() {
        return formHandler.getCompletionHandler();
    }

    /**
     * @param completionHandler the completionHandler to set
     */
    public void setCompletionHandler(IImportCompletionHandler completionHandler) {
        this.formHandler.setCompletionHandler(completionHandler);
    }
    
	/**
	 * Action for upload click
	 */
	public void doUpload( ) {
		form.setAction(getWebContext() + "/services/dataVirtUpload");
		form.submit();
	}  
	
    private String getWebContext() {
        String context = GWT.getModuleBaseURL().replace( GWT.getModuleName() + "/", "" );
        if ( context.endsWith( "/" ) ) {
            context = context.substring( 0, context.length() - 1 );
        }
        return context;
    }

}
