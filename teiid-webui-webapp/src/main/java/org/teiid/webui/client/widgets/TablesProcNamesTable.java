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

import java.util.Collections;
import java.util.List;

import org.teiid.webui.client.widgets.table.SimpleTable;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;

/**
 * Composite for display of Table and Procedure names
 */
public class TablesProcNamesTable extends Composite {

	private static String COLUMN_HEADER_NAME = "Tables";
	private static int TABLE_HEIGHT_PX = 200;
	private static int TABLE_WIDTH_PX = 250;
	private static int TABLE_VISIBLE_ROWS = 7;

    protected VerticalPanel panel = new VerticalPanel();
    protected Label label = new Label();

    private SimpleTable<String> table;

    public TablesProcNamesTable() {
        initWidget( panel );
        panel.add(createTablePanel());
    }
    
    /**
     * Create the panel
     * @return the panel widget
     */
    protected Widget createTablePanel() {
    	table = new SimpleTable<String>(TABLE_HEIGHT_PX,TABLE_WIDTH_PX,TABLE_VISIBLE_ROWS);
        TextColumn<String> nameColumn = new TextColumn<String>() {
            public String getValue( String row ) {
                return row;
            }
        };
        table.addColumn( nameColumn, COLUMN_HEADER_NAME );
        table.setColumnWidth(nameColumn, 200, Unit.PX);
        
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(table);
        return verticalPanel;
    }
    
    public void clear() {
    	setData(Collections.<String>emptyList());
    }
    
    public void setData(List<String> rows) {
    	table.setRowData(rows);
    }

    public List<String> getData() {
    	return table.getRowData();
    }
    
    public void setSelectionModel( final SelectionModel<String> selectionModel ) {
        table.setSelectionModel( selectionModel );
    }
    
}
