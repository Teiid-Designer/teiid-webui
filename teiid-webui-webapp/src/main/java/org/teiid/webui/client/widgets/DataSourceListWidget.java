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
import java.util.List;

import org.teiid.webui.client.resources.AppResource;
import org.teiid.webui.client.resources.CellListResources;
import org.teiid.webui.client.resources.ImageHelper;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.beans.DataSourcePageRow;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.SelectionModel;

/**
 * Composite for display of DataSource names
 */
public class DataSourceListWidget extends Composite {

    protected ScrollPanel scrollPanel = new ScrollPanel();
    protected Label label = new Label();

    private CellList<DataSourcePageRow> dsList;

    public DataSourceListWidget() {
        initWidget( scrollPanel );
        createListPanel();
    }
    
    /**
     * Create the panel
     * @return the panel widget
     */
    protected void createListPanel() {
       	CellList.Resources resources = GWT.create(CellListResources.class);
        DataSourceCell dataSourceCell = new DataSourceCell( );
        
    	dsList = new CellList<DataSourcePageRow>(dataSourceCell,resources);
    	dsList.setPageSize(3);
    	
        scrollPanel.add(dsList);
        scrollPanel.setHeight("600px");
    }
    
    public void clear() {
    	setData(Collections.<DataSourcePageRow>emptyList());
    }
    
    public void setData(List<DataSourcePageRow> rows) {
    	dsList.setRowData(rows);
    }
    
    public List<DataSourcePageRow> getData( ) {
    	return dsList.getVisibleItems();
    }
    
    public List<String> getDataSourceNames() {
    	List<String> dsNames = new ArrayList<String>();
    	for(DataSourcePageRow dsRow : getData()) {
    		dsNames.add(dsRow.getName());
    	}
    	return dsNames;
    }
    
    public void setSelection(String dsName) {
    	SelectionModel<? super DataSourcePageRow> selModel = dsList.getSelectionModel();
    	for(DataSourcePageRow dSource : getData()) {
    		if(dSource.getName().equals(dsName)) {
    			selModel.setSelected(dSource, true);
    			break;
    		}
    	}
    }
    
    public void selectFirstItem() {
    	SelectionModel<? super DataSourcePageRow> selModel = dsList.getSelectionModel();
    	DataSourcePageRow firstRow = getData().get(0);
    	if(firstRow!=null) {
    		selModel.setSelected(firstRow,true);
    	} 
    }
    
    public void setSelectionModel( final SelectionModel<DataSourcePageRow> selectionModel ) {
    	dsList.setSelectionModel( selectionModel );
    }
    
    /**
     * The Cell used to render a {@link ContactInfo}.
     */
    static class DataSourceCell extends AbstractCell<DataSourcePageRow> {

      /**
       * The html of the images used for ok or error.
       */
        private final String okImageHtml;
        private final String deployingImageHtml;
        private final String errorImageHtml;

      public DataSourceCell( ) {
        this.okImageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().okIcon16x16Image()).getHTML();
        this.deployingImageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().spinnner16x16Image()).getHTML();
        this.errorImageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().errorIcon16x16Image()).getHTML();
      }

      @Override
      public void render(Context context, DataSourcePageRow dsRow, SafeHtmlBuilder sb) {
        // Value can be null, so do a null check..
        if (dsRow == null) {
          return;
        }

        String statusImageHtml = null;
        if(dsRow.getState()==DataSourcePageRow.State.OK) {
        	statusImageHtml = this.okImageHtml;
        } else if(dsRow.getState()==DataSourcePageRow.State.DEPLOYING) {
            statusImageHtml = this.deployingImageHtml;
        } else if(dsRow.getState()==DataSourcePageRow.State.PLACEHOLDER) {
            statusImageHtml = Constants.BLANK;
        } else {
        	statusImageHtml = this.errorImageHtml;
        }
        String dType = dsRow.getType();
        String dTypeImageHtml = Constants.BLANK;
        if(dType!=null) {
        	dTypeImageHtml = ImageHelper.getInstance().getDataSourceForTypeSmallImageHtml(dType);
        }
                
        sb.appendHtmlConstant("<table>");

        // Add the contact image.
        sb.appendHtmlConstant("<tr><td>");
        sb.appendHtmlConstant(statusImageHtml);
        sb.appendHtmlConstant("</td>");
        
        sb.appendHtmlConstant("<td style=\"width:72px;height:42px;padding: 2px 0px 0px 2px;\">");
        sb.appendHtmlConstant(dTypeImageHtml);
        sb.appendHtmlConstant("</td>");
        
        // Add the name and address.
        sb.appendHtmlConstant("<td style=\"width:3px;\"></td><td><em>");
        sb.appendEscaped(dsRow.getName());
        sb.appendHtmlConstant("</em></td></tr></table>");
      }
    }  
}
