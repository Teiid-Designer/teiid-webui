package org.teiid.webui.client.widgets.validation;

import org.gwtbootstrap3.client.ui.TextArea;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Event;

/**
 * Extended to handle copy-paste into the box
 */
public class ExtendedTextArea extends TextArea implements HasHandlers {

	private HandlerManager handlerManager;

	public ExtendedTextArea() {
		super();

		handlerManager = new HandlerManager(this);

		// For all browsers - catch onKeyUp
		sinkEvents(Event.ONKEYUP);

		// For IE and Firefox - catch onPaste
		sinkEvents(Event.ONPASTE);

		// For Opera - catch onInput
		//sinkEvents(Event.ONINPUT);
	}

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);

		switch (event.getTypeInt()) {
		case Event.ONKEYUP:
		case Event.ONPASTE:
		{
			// Scheduler needed so pasted data shows up in TextBox before we fire event
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					fireEvent(new TextChangeEvent());
				}
			});
			break;
		}
		default:
			// Do nothing
		}
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

	public HandlerRegistration addTextChangeEventHandler(TextChangeEventHandler handler) {
		return handlerManager.addHandler(TextChangeEvent.TYPE, handler);
	}
}