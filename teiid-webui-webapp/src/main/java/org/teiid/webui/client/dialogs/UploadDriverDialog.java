package org.teiid.webui.client.dialogs;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.teiid.webui.backend.server.servlets.DataVirtUploadServlet;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.services.NotificationService;
import org.teiid.webui.share.beans.NotificationBean;
import org.teiid.webui.share.exceptions.DataVirtUiException;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dialog for upload of jdbc drivers
 */
@Dependent
@WorkbenchPopup(identifier = "UploadDriverDialog")
public class UploadDriverDialog {

	@Inject
    private ClientMessages i18n;
	
	@Inject
    private NotificationService notificationService;
	
	private NotificationBean notification;
	
	@Inject Event<UiEvent> uiEvent;

    @Inject
	private PlaceManager placeManager;
	private PlaceRequest place;
	private final VerticalPanel view = new VerticalPanel();
	
	@PostConstruct
	public void setup() {
		view.add(new UploadDriverPanel(this));
	}
	
	@OnStartup
	public void onStartup( final PlaceRequest place ) {
		this.place = place;
	}
	
	@WorkbenchPartTitle
	public String getTitle() {
		String dialogTitle = i18n.format("upload-dtype-dialog.dialog-title");
		return dialogTitle;
	}
	
	@WorkbenchPartView
	public Widget getView() {
		return view;
	}
	
	/**
	 * Closes the Dialog
	 */
	public void close() {
		placeManager.closePlace(place);
	}
	
	/**
	 * Shows notification that upload is starting
	 */
	public void uploadStarting() {
		notification = notificationService.startProgressNotification(
		i18n.format("import-datasource-type-submit.uploading.title"), //$NON-NLS-1$
		i18n.format("import-datasource-type-submit.uploading.msg")); //$NON-NLS-1$
	}
	
	/**
	 * Shows notification when upload is complete
	 * @param resultStr the servlet response text
	 */
	public void uploadComplete(String resultStr) {
        ImportResult results = ImportResult.fromResult(resultStr);
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

			uiEvent.fire(new UiEvent(UiEventType.UPLOAD_DRIVER_COMPLETE));
			this.close();
		}
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
