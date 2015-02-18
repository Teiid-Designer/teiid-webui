package org.teiid.webui.client.dialogs;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * An example popup
 */
@WorkbenchPopup(identifier = "MyTestPopUp")
public class SimplePopup {

    @Inject
    private PlaceManager placeManager;

    private PlaceRequest place;

    private final VerticalPanel view = new VerticalPanel();
    private Label l;
    private TextBox t;
    private Button b;

    @PostConstruct
    public void setup() {
        l = new Label( "Click to close" );
        t = new TextBox();
        b = new Button( "Close" );
        b.addClickHandler( new ClickHandler() {

            @Override
            public void onClick( final ClickEvent event ) {
                placeManager.closePlace( place );
            }
        } );
        view.add( l );
        view.add( t );
        view.add( b );
    }

    @OnStartup
    public void onStartup( final PlaceRequest place ) {
        this.place = place;
        b.setFocus( true );
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "MyPopUp Title here";
    }

    @WorkbenchPartView
    public Widget getView() {
        return view;
    }

}