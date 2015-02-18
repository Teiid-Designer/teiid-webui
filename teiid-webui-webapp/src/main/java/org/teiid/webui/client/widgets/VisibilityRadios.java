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

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class VisibilityRadios extends Composite {
	
	interface VisibilityRadiosUiBinder extends UiBinder<Widget, VisibilityRadios> {
	}

	public final class RadioButtonClickHandler implements ClickHandler {
		private Boolean value;

		public RadioButtonClickHandler(Boolean value) {
			this.value = value;
		}

		@Override
		public void onClick(ClickEvent event) {
			visibleValue = value;
		}
	}

	@UiField
	@Ignore
	RadioButton visible;

	@UiField
	@Ignore
	RadioButton notVisible;

	Boolean visibleValue;

	private static VisibilityRadiosUiBinder uiBinder = GWT.create(VisibilityRadiosUiBinder.class);

	public VisibilityRadios() {
		initWidget(uiBinder.createAndBindUi(this));
		setupHandlers();
	}

	private void setupHandlers() {
		visible.addClickHandler(new RadioButtonClickHandler(true));
		notVisible.addClickHandler(new RadioButtonClickHandler(false));
	}

	public void setValue(Boolean value) {
		this.visibleValue = value;
		if (this.visibleValue) {
			visible.setValue(true);
		} else {
			notVisible.setValue(true);
		}
	}
	
	public boolean isVisibleSelected() {
		return visible.getValue();
	}
	
	public void setReadOnly(Boolean readOnly) {
		visible.setEnabled(!readOnly);
		notVisible.setEnabled(!readOnly);
	}
}
