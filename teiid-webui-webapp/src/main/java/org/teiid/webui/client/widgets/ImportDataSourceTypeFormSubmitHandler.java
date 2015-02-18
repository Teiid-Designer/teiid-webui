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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.teiid.webui.backend.server.servlets.DataVirtUploadServlet;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.NotificationService;
import org.teiid.webui.share.beans.NotificationBean;
import org.teiid.webui.share.exceptions.DataVirtUiException;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The form submit handler used by the {@link ImportDataSourceTypeDialog}.
 *
 * @author mdrillin@redhat.com
 */
@Dependent
public class ImportDataSourceTypeFormSubmitHandler implements SubmitHandler, SubmitCompleteHandler {

    @Inject
    protected ClientMessages i18n;
    @Inject
    private NotificationService notificationService;

    private NotificationBean notification;
    private IImportCompletionHandler completionHandler;

    /**
     * Constructor.
     */
    public ImportDataSourceTypeFormSubmitHandler() {
    }

    /**
     * @see com.google.gwt.user.client.ui.FormPanel.SubmitHandler#onSubmit(com.google.gwt.user.client.ui.FormPanel.SubmitEvent)
     */
    @Override
    public void onSubmit(SubmitEvent event) {
        notification = notificationService.startProgressNotification(
                i18n.format("import-datasource-type-submit.uploading.title"), //$NON-NLS-1$
                i18n.format("import-datasource-type-submit.uploading.msg")); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler#onSubmitComplete(com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent)
     */
    @Override
    public void onSubmitComplete(SubmitCompleteEvent event) {
        ImportResult results = ImportResult.fromResult(event.getResults());
        if (results.isError()) {
            if (results.getError() != null) {
                notificationService.completeProgressNotification(
                        notification.getUuid(),
                        i18n.format("import-datasource-type-submit.upload-error.title"), //$NON-NLS-1$
                        results.getError());
            } else {
                notificationService.completeProgressNotification(
                        notification.getUuid(),
                        i18n.format("import-datasource-type-submit.upload-error.title"), //$NON-NLS-1$
                        i18n.format("import-datasource-type-submit.upload-error.msg")); //$NON-NLS-1$
            }
        } else {
            Widget ty = new InlineLabel(i18n.format("import-datasource-type-submit.upload-complete.msg")); //$NON-NLS-1$
            FlowPanel body = new FlowPanel();
            body.add(ty);
            notificationService.completeProgressNotification(
                    notification.getUuid(),
                    i18n.format("import-datasource-type-submit.upload-complete.title"), //$NON-NLS-1$
                    body);
            if (completionHandler != null) {
                completionHandler.onImportComplete();
            }
        }
    }

    /**
     * @return the completionHandler
     */
    public IImportCompletionHandler getCompletionHandler() {
        return completionHandler;
    }

    /**
     * @param completionHandler the completionHandler to set
     */
    public void setCompletionHandler(IImportCompletionHandler completionHandler) {
        this.completionHandler = completionHandler;
    }

    /**
     * The {@link DataVirtUploadServlet} returns a JSON map as the response.
     * @author mdrillin@redhat.com
     */
    private static class ImportResult extends JavaScriptObject {

        /**
         * Constructor.
         */
        protected ImportResult() {
        }

        /**
         * Convert the string returned by the {@link DataVirtUploadServlet} into JSON and
         * then from there into an {@link ImportResult} bean.
         * @param resultData
         */
        public static final ImportResult fromResult(String resultData) {
            int startIdx = resultData.indexOf('{');
            int endIdx = resultData.lastIndexOf('}') + 1;
            resultData = "(" + resultData.substring(startIdx, endIdx) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            return fromJSON(resultData);
        }

        /**
         * Gets a value from the map.
         * @param key
         */
        public final native String get(String key) /*-{
            if (this[key])
                return this[key];
            else
                return null;
        }-*/;

        /**
         * @return the uuid
         */
        public final String getUuid() {
            return get("uuid"); //$NON-NLS-1$
        }

        /**
         * @return the model
         */
        public final String getModel() {
            return get("model"); //$NON-NLS-1$
        }

        /**
         * @return the type
         */
        public final String getType() {
            return get("type"); //$NON-NLS-1$
        }

        /**
         * Returns true if the response is an error response.
         */
        public final boolean isError() {
            return "true".equals(get("exception")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /**
         * @return true if the response is due to a s-ramp package upload
         */
        public final boolean isBatch() {
            return "true".equals(get("batch")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /**
         * @return the total number of items in the s-ramp package
         */
        public final int getBatchTotal() {
            return new Integer(get("batchTotal")); //$NON-NLS-1$
        }

        /**
         * @return the number of successful items in the package
         */
        public final int getBatchNumSuccess() {
            return new Integer(get("batchNumSuccess")); //$NON-NLS-1$
        }

        /**
         * @return the number of failed items in the package
         */
        public final int getBatchNumFailed() {
            return new Integer(get("batchNumFailed")); //$NON-NLS-1$
        }

        /**
         * Gets the error.
         */
        public final DataVirtUiException getError() {
            String errorMessage = get("exception-message"); //$NON-NLS-1$
            DataVirtUiException error = new DataVirtUiException(errorMessage);
            return error;
        }

        /**
         * Convert a string of json data into a useful bean.
         * @param jsonData
         */
        public static final native ImportResult fromJSON(String jsonData) /*-{ return eval(jsonData); }-*/;

    }
}
