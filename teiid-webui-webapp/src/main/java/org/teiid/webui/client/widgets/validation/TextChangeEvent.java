package org.teiid.webui.client.widgets.validation;

import com.google.gwt.event.shared.GwtEvent;

public class TextChangeEvent extends GwtEvent<TextChangeEventHandler> {

	public static final Type<TextChangeEventHandler> TYPE = new Type<TextChangeEventHandler>();

	@Override
	public Type<TextChangeEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(TextChangeEventHandler handler) {
		handler.onTextChange(this);
	}
}
