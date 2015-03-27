package org.teiid.webui.client.widgets.validation;

import javax.enterprise.context.Dependent;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.ListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * TextBox which validates user input
 */
@Dependent
public class NamedListBox extends Composite {
    
    interface NamedListBoxBinder extends UiBinder<Widget, NamedListBox> {}
    private static NamedListBoxBinder uiBinder = GWT.create(NamedListBoxBinder.class);
 
    @UiField
    FormGroup formgroup;

    @UiField
    ListBox fgListBox;
    
    @UiField
    FormLabel fgLabel;

    /**
     * Constructor
     */
    public NamedListBox() {
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        
        // other initialization
        init();
    }
    
    /**
     * performs initialization after Ui binding is complete
     */
    private void init() {
    }
    
    /**
     * Set the label text
     * @param labelTxt
     */
    public void setLabel(String labelTxt) {
    	fgLabel.setText(labelTxt);
    }
    
    /**
     * Set the label HTML
     * @param label Html
     */
    public void setLabelHTML(String labelHtml) {
    	fgLabel.setHTML(labelHtml);
    }
    
    public void setLabelVisible(boolean isVisible) {
    	this.fgLabel.setVisible(isVisible);
    }
    
    /**
     * Get the list box widget
     * @return
     */
    public ListBox getListBox( ) {
    	return fgListBox;
    }
    
}