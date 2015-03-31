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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.webui.client.dialogs.UiEvent;
import org.teiid.webui.client.dialogs.UiEventType;
import org.teiid.webui.client.messages.ClientMessages;
import org.teiid.webui.client.resources.AppResource;
import org.teiid.webui.client.resources.ImageHelper;
import org.teiid.webui.client.services.ApplicationStateKeys;
import org.teiid.webui.client.services.ApplicationStateService;
import org.teiid.webui.client.services.NotificationService;
import org.teiid.webui.client.services.TeiidRpcService;
import org.teiid.webui.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.webui.client.utils.UiUtils;
import org.teiid.webui.client.widgets.validation.DuplicateNameValidator;
import org.teiid.webui.client.widgets.validation.EmptyNameValidator;
import org.teiid.webui.client.widgets.validation.IntegerValueValidator;
import org.teiid.webui.client.widgets.validation.NamedListBox;
import org.teiid.webui.client.widgets.validation.ServiceNameValidator;
import org.teiid.webui.client.widgets.validation.TextChangeListener;
import org.teiid.webui.client.widgets.validation.ValidatingTextBoxHoriz;
import org.teiid.webui.share.Constants;
import org.teiid.webui.share.TranslatorHelper;
import org.teiid.webui.share.beans.DataSourcePageRow;
import org.teiid.webui.share.beans.DataSourcePropertyBean;
import org.teiid.webui.share.beans.DataSourceWithVdbDetailsBean;
import org.teiid.webui.share.beans.NotificationBean;
import org.teiid.webui.share.beans.PropertyBeanComparator;
import org.teiid.webui.share.beans.TranslatorImportPropertyBean;
import org.teiid.webui.share.services.StringUtils;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

@Dependent
@Templated("./DataSourcePropertiesPanel.html")
public class DataSourcePropertiesPanel extends Composite {

    @Inject
    private PlaceManager placeManager;
    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    @Inject
    private ApplicationStateService stateService;
    
	private String statusSelectType = null;
	private String statusEnterProps = null;
	private String externalError = null;
	
	private String clickedSourceType;
    
	// List of all available translators
	private List<String> allTranslators = new ArrayList<String>();
	private Map<String,List<TranslatorImportPropertyBean>> importPropertyMap = new HashMap<String,List<TranslatorImportPropertyBean>>();
	
	// Map of server sourceName to corresponding default translator
	private Map<String,String> defaultTranslatorMap = new HashMap<String,String>();
	// Current properties
    private List<DataSourcePropertyBean> currentPropList = new ArrayList<DataSourcePropertyBean>();
	// Current import properties
    private List<TranslatorImportPropertyBean> currentImportPropList = new ArrayList<TranslatorImportPropertyBean>();
	// The type of the current source
    private String selectedSourceType;
    // List of Data Source type Toggle Buttons
    private List<ToggleButton> dsTypeButtons = new ArrayList<ToggleButton>();
    // Button for adding new type
    private ToggleButton addTypeButton;
    
    // Keeps track of original type, name and translator, before any edits are made
    private String originalType;
    private String originalName;
    private String originalTranslator;
    private boolean isNewSource = false;  // Tracks if DataSourcePageRow is for a brand new source
    private List<String> existingDSNames = new ArrayList<String>();
    
    @Inject
    protected TeiidRpcService teiidService;
    
    @Inject @DataField("label-dsprops-title")
    protected Label dsDetailsPanelTitle;
    
    @Inject @DataField("text-dsprops-status")
    protected HTML statusText;
    
    @Inject @DataField("textbox-dsprops-name")
    protected ValidatingTextBoxHoriz nameTextBox;
    @Inject @DataField("dtypes-button-panel")
    protected FlowPanel dTypesButtonPanel;
    
    @Inject @DataField("listbox-dsprops-translator")
    protected NamedListBox namedTranslatorListBox;
    
    @Inject @DataField("textbox-dsprops-timeout")
    protected ValidatingTextBoxHoriz timeoutTextBox;
    
    private ListBox translatorListBox;
    
    @Inject @DataField("label-coreprops-title")
    protected Label corePropsTitle;
    
    @Inject @DataField("editor-dsprops-core-properties")
    protected DataSourcePropertyEditor dataSourceCorePropertyEditor;
    
    @Inject @DataField("advanced-props-accordion")
    protected AdvancedPropsAccordion advPropsAccordion;
    @Inject @DataField("import-props-accordion")
    protected ImportPropsAccordion importPropsAccordion;
    
    @Inject @DataField("btn-dsprops-save")
    protected Button saveSourceChanges;
    @Inject @DataField("btn-dsprops-cancel")
    protected Button cancelSourceChanges;
    
    @Inject Event<UiEvent> statusEvent;
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	AppResource.INSTANCE.css().customToggleStyle().ensureInjected();
    	
    	dsDetailsPanelTitle.setText("[New Source]");
    	corePropsTitle.setText("Connection Properties");
    	
		statusSelectType = i18n.format("ds-properties-panel.status-select-type");
		statusEnterProps = i18n.format("ds-properties-panel.status-enter-props");
		
    	doPopulateSourceTypesPanel(null);
    	
    	dataSourceCorePropertyEditor.clear();
    	
    	advPropsAccordion.setText("Advanced Properties");
    	advPropsAccordion.clearProperties();
    	importPropsAccordion.setText("Import Properties");
    	importPropsAccordion.clearProperties();
    	
    	nameTextBox.setLabelHTML("<div><h3>Source Name</h3></div>");
    	nameTextBox.setLabelVisible(true);
    	nameTextBox.addTextChangeListener(new TextChangeListener() {
            @Override
			public void textChanged(  ) {
            	updateStatus();
            }
        });

    	namedTranslatorListBox.setLabelHTML("<div><h3>Translator</h3></div>");
    	namedTranslatorListBox.setLabelVisible(true);
    	translatorListBox = namedTranslatorListBox.getListBox();
        // Change Listener for Type ListBox
        translatorListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
        		currentImportPropList = importPropertyMap.get(getSelectedTranslator());
        		populateImportPropertiesTableWithCurrent();
                updateStatus();
        	}
        });
        
    	timeoutTextBox.setLabelHTML("<div><h6>Deployment Timeout (sec)</h6></div>");
        timeoutTextBox.setLabelVisible(true);
        timeoutTextBox.setText(String.valueOf(Constants.VDB_LOADING_TIMEOUT_SECS));
    	timeoutTextBox.addTextChangeListener(new TextChangeListener() {
            @Override
			public void textChanged(  ) {
            	updateStatus();
            }
        });
    	timeoutTextBox.addValidator(new EmptyNameValidator());
    	timeoutTextBox.addValidator(new IntegerValueValidator());
    	        
        // Tooltips
        cancelSourceChanges.setTitle(i18n.format("ds-properties-panel.cancelSourceChanges.tooltip"));
        saveSourceChanges.setTitle(i18n.format("ds-properties-panel.saveSourceChanges.tooltip"));
        nameTextBox.setTitle(i18n.format("ds-properties-panel.nameTextBox.tooltip"));
        namedTranslatorListBox.setTitle(i18n.format("ds-properties-panel.translatorListBox.tooltip"));
        timeoutTextBox.setTitle(i18n.format("ds-properties-panel.timeoutTextBox.tooltip"));
    }
    
    /**
     * Event from properties table when property is edited
     * @param propertyBean
     */
    public void onPropertyChanged(@Observes DataSourcePropertyBean propertyBean) {
    	updateStatus();
    }

    /**
     * Event from translator import table when property is edited
     * @param propertyBean
     */
    public void onImportPropertyChanged(@Observes TranslatorImportPropertyBean propertyBean) {
    	updateStatus();
    }
    
    /**
     * Event handler that fires when the user clicks the save button.
     * @param event
     */
    @EventHandler("btn-dsprops-save")
    public void onSaveChangesButtonClick(ClickEvent event) {
    	saveChangesButtonClick(event);
    }
    
    /**
     * Event handler that fires when the user clicks the cancel button.
     * @param event
     */
    @EventHandler("btn-dsprops-cancel")
    public void onCancelButtonClick(ClickEvent event) {
    	cancelButtonClick(event);
    }
    
    private void saveChangesButtonClick(ClickEvent event) {
    	// Pre-Validate before submit.  This handles special case where some properties are 'conditionally' required. 
    	// For example, some google property requirements depend on the entered value of the 'AuthMethod' property
    	String validationMsg = preValidateOnSave();
    	
    	if(validationMsg.equals(Constants.OK)) {
    		// New source - bypass confirm dialog and create
    		if(isNewSource) {
    			onRedeployConfirmed();    		
    			// Only the translator changed.  No need to muck with DS - just redeploy VDB and its source
    		} else if(!hasNameChange() && !hasPropertyChanges() && !hasDataSourceTypeChange() && hasTranslatorChange()) {
    			DataSourceWithVdbDetailsBean sourceBean = getDetailsBean();
    			doCreateSourceVdbWithTeiidDS(sourceBean,Integer.valueOf(timeoutTextBox.getText()));
    			// No name change
    		} else if(!hasNameChange()) {
    			showConfirmSourceRedeployDialog();
    			// Name change - confirm the rename first
    		} else {
    			showConfirmRenameDialog();
    		}
    	} else {
    		Window.alert(validationMsg);
    	}
    }
    
    /** 
     * Handles special sources like google, which require some properties conditionally
     * @return the validation status string
     */
    private String preValidateOnSave() {
    	// Validate google entries before submittal
    	if(DataSourceHelper.isGoogleSource(advPropsAccordion.getProperties())) {
    		List<DataSourcePropertyBean> props = dataSourceCorePropertyEditor.getProperties();
			String authMethod = DataSourceHelper.getPropertyValue(TranslatorHelper.GOOGLE_SOURCE_PROPERTY_KEY_AUTH_METHOD, props);
			
			// Verify the AuthMethod entry
			if( !authMethod.equals("OAuth2") && !authMethod.equals("ClientLogin")) {
				return i18n.format("ds-properties-panel.google-validation.invalid-authmethod-message");
			}
			
			// Verify entries for AuthMethod 'OAuth2'
    		if(authMethod.equals("OAuth2")) {
    			String refreshToken = DataSourceHelper.getPropertyValue(TranslatorHelper.GOOGLE_SOURCE_PROPERTY_KEY_REFRESH_TOKEN, props);
    			if(StringUtils.isEmpty(refreshToken)) {
    				return i18n.format("ds-properties-panel.google-validation.requires-refresh-token-message");
    			}
    		}
    		
			// Verify entries for AuthMethod 'ClientLogin'
    		if(authMethod.equals("ClientLogin")) {
    			String username = DataSourceHelper.getPropertyValue(TranslatorHelper.GOOGLE_SOURCE_PROPERTY_KEY_USERNAME, props);
    			String password = DataSourceHelper.getPropertyValue(TranslatorHelper.GOOGLE_SOURCE_PROPERTY_KEY_PASSWORD, props);
    			if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
    				return i18n.format("ds-properties-panel.google-validation.requires-namepass-message");
    			}
    		}
    	}
    	return Constants.OK;
    }
    
    private void cancelButtonClick(ClickEvent event) {
    	// refresh panel if editing existing source
    	if(!isNewSource) {
    		doGetDataSourceDetails(this.originalName);
    	}
		
		statusEvent.fire(new UiEvent(UiEventType.SOURCE_EDIT_CANCEL));
    }
    
    /**
     * Shows the confirmation dialog for renaming a DataSource
     */
    private void showConfirmRenameDialog() {
		// Display the Confirmation Dialog for source rename
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.CONFIRMATION_DIALOG_MESSAGE, i18n.format("ds-properties-panel.confirm-rename-dialog-message"));
		parameters.put(Constants.CONFIRMATION_DIALOG_TYPE, Constants.CONFIRMATION_DIALOG_SOURCE_RENAME);
    	placeManager.goTo(new DefaultPlaceRequest(Constants.CONFIRMATION_DIALOG,parameters));
    }
    
    /**
     * Shows the confirmation dialog for redeploy of a DataSource
     */
    private void showConfirmSourceRedeployDialog() {
		// Display the Confirmation Dialog for source redeploy
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.CONFIRMATION_DIALOG_MESSAGE, i18n.format("ds-properties-panel.confirm-redeploy-dialog-message"));
		parameters.put(Constants.CONFIRMATION_DIALOG_TYPE, Constants.CONFIRMATION_DIALOG_SOURCE_REDEPLOY);
    	placeManager.goTo(new DefaultPlaceRequest(Constants.CONFIRMATION_DIALOG,parameters));    	
    }
    
    /**
     * Shows the confirmation dialog for changing a DataSource type
     */
    private void showConfirmChangeTypeDialog() {
		// Display the Confirmation Dialog for source type change
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.CONFIRMATION_DIALOG_MESSAGE, i18n.format("ds-properties-panel.confirm-changetype-dialog-message"));
		parameters.put(Constants.CONFIRMATION_DIALOG_TYPE, Constants.CONFIRMATION_DIALOG_SOURCE_CHANGETYPE);
    	placeManager.goTo(new DefaultPlaceRequest(Constants.CONFIRMATION_DIALOG,parameters));    	
    }
    
    /**
     * Handles UiEvents
     * @param dEvent
     */
    public void onDialogEvent(@Observes UiEvent dEvent) {
    	// User has OK'd source rename
    	if(dEvent.getType() == UiEventType.SOURCE_RENAME_OK) {
    		onRenameConfirmed();
    	// User has OK'd source redeploy
    	} else if(dEvent.getType() == UiEventType.SOURCE_REDEPLOY_OK) {
    		onRedeployConfirmed();
    	// User has OK'd source type change
    	} else if(dEvent.getType() == UiEventType.SOURCE_CHANGETYPE_OK) {
    		onChangeTypeConfirmed();
    		setSelectedDataSourceType(this.selectedSourceType);
    	// User has cancelled source rename
    	} else if(dEvent.getType() == UiEventType.SOURCE_RENAME_CANCEL) {
    	// User has cancelled source redeploy
    	} else if(dEvent.getType() == UiEventType.SOURCE_REDEPLOY_CANCEL) {
    	// User has cancelled source type change
    	} else if(dEvent.getType() == UiEventType.SOURCE_CHANGETYPE_CANCEL) {
    		setSelectedDataSourceType(this.selectedSourceType);
    	} else if(dEvent.getType() == UiEventType.UPLOAD_DRIVER_COMPLETE) {
        	doPopulateSourceTypesPanel(selectedSourceType);
    	} 
    }
    
    private void onRenameConfirmed() {
        DataSourceWithVdbDetailsBean sourceBean = getDetailsBean();
        
        // DataSources to delete - server source and Vdb source.
    	List<String> originalDsNames = new ArrayList<String>();
    	originalDsNames.add(this.originalName);
    	originalDsNames.add(Constants.SERVICE_SOURCE_VDB_PREFIX+this.originalName);
    	
    	// Also must delete the Source Vdb
    	String srcVdbName = Constants.SERVICE_SOURCE_VDB_PREFIX+this.originalName;
    	
    	doDeleteThenCreateDataSource(originalDsNames,srcVdbName,sourceBean);
    }
    
    private void onRedeployConfirmed() {
        DataSourceWithVdbDetailsBean sourceBean = getDetailsBean();
    	doCreateDataSource(sourceBean,Integer.valueOf(timeoutTextBox.getText()));
    }
    
    private void onChangeTypeConfirmed() {
		// Get default translator for the selected type
    	String sourceType = clickedSourceType;
		String defaultTranslator = TranslatorHelper.getTranslator(sourceType, allTranslators);
		setSelectedTranslator(defaultTranslator);
		this.currentImportPropList.clear();
		this.currentImportPropList = importPropertyMap.get(getSelectedTranslator());
		populateImportPropertiesTableWithCurrent();

		doPopulatePropertiesTable(sourceType);
		selectedSourceType = sourceType;
    }
    
    private DataSourceWithVdbDetailsBean getDetailsBean() {
    	DataSourceWithVdbDetailsBean resultBean = new DataSourceWithVdbDetailsBean();
    	String dsName = nameTextBox.getText();
    	resultBean.setName(dsName);
    	resultBean.setType(this.selectedSourceType);
    	resultBean.setTranslator(getSelectedTranslator());
    	resultBean.setSourceVdbName(Constants.SERVICE_SOURCE_VDB_PREFIX+dsName);

    	// Set the Source properties
    	List<DataSourcePropertyBean> props = new ArrayList<DataSourcePropertyBean>();
    	List<DataSourcePropertyBean> coreProps = dataSourceCorePropertyEditor.getBeansWithRequiredOrNonDefaultValue();
    	List<DataSourcePropertyBean> advancedProps = advPropsAccordion.getBeansWithRequiredOrNonDefaultValue();
    	props.addAll(coreProps);
    	props.addAll(advancedProps);
    	resultBean.setProperties(props);

    	// Set the translator import properties
    	List<TranslatorImportPropertyBean> importProps = importPropsAccordion.getBeansWithRequiredOrNonDefaultValue();
    	resultBean.setImportProperties(importProps);
    	
    	return resultBean;
    }
    
    /**
     * Populate the Data Source Types Panel
     */
    protected void doPopulateSourceTypesPanel(final String selectedType) {
    	teiidService.getDataSourceTypes(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> dsTypes) {
            	dTypesButtonPanel.clear();
            	dsTypeButtons.clear();
            	// Generates toggle buttons for each type
                for(String dType : dsTypes) {
                	ImageResource img = ImageHelper.getInstance().getDataSourceImageForType(dType);
                	Image buttonImage = null;
                	if(img!=null) {
                		buttonImage = new Image(img);
                	}
                	ToggleButton button;
                	if(!ImageHelper.getInstance().hasKnownImage(dType)) {
                    	button = new ToggleButton(dType,dType);
                	} else {
                    	button = new ToggleButton(buttonImage);
                	}
            		button.setStylePrimaryName("customToggle");
                	button.getElement().setId(dType);
                	button.addClickHandler(new ClickHandler() {
                		public void onClick(ClickEvent event) {
            				Widget sourceWidget = (Widget)event.getSource();
            				String sourceType = sourceWidget.getElement().getId();
            				
            				clickedSourceType = sourceType;
            				// No current selection, just set it
            				if(StringUtils.isEmpty(selectedSourceType)) {
            		    		statusEvent.fire(new UiEvent(UiEventType.SOURCE_CHANGETYPE_OK));
            		    	// Have current selection, confirm first
            				} else {
            					showConfirmChangeTypeDialog();
            				}
                		}
                	});                	
                	DOM.setStyleAttribute(button.getElement(), "cssFloat", "left");
                	DOM.setStyleAttribute(button.getElement(), "margin", "5px");
                	DOM.setStyleAttribute(button.getElement(), "padding", "0px");
                	dTypesButtonPanel.add(button);
                	dsTypeButtons.add(button);
                }
                
                // Add button for AddType
                ImageResource addTypeImg = AppResource.INSTANCE.images().dsType_addtype_Image();
                final ToggleButton addTypeButton = new ToggleButton(new Image(addTypeImg));
            	addTypeButton.setStylePrimaryName("customToggle");
                addTypeButton.addClickHandler(new ClickHandler() {
            		public void onClick(ClickEvent event) {
            			addTypeButton.setValue(false);
            			placeManager.goTo(Constants.UPLOAD_DRIVER_DIALOG);
            		}
            	});                	
            	DOM.setStyleAttribute(addTypeButton.getElement(), "cssFloat", "left");
            	DOM.setStyleAttribute(addTypeButton.getElement(), "margin", "5px");
            	DOM.setStyleAttribute(addTypeButton.getElement(), "padding", "0px");
                addTypeButton.setTitle(i18n.format("ds-properties-panel.addTypeButton.tooltip"));
            	dTypesButtonPanel.add(addTypeButton);
            	
            	if(selectedType!=null) setSelectedDataSourceType(selectedType);
            	
            	// Populate Translator ListBox
            	doPopulateTranslatorListBox();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ds-properties-panel.error-populating-dstypes"), error); //$NON-NLS-1$
            }
        });
    }
    
    /**
     * Gets the selected Data Source type
     * @return the currently selected type
     */
    public String getSelectedDataSourceType( ) {
    	return this.selectedSourceType;
    }
    
    /**
     * Sets the selected Data Source type button to the down position.
     * This method does not fire an event, just changes toggle position
     * @param dsType the data source type
     */
    public void setSelectedDataSourceType(String dsType) {
    	if(dsType!=null) {
    		// de-select all toggle buttons
    		deselectDSTypeButtons();
    		
    		// Set new button toggle state down
    		for(ToggleButton tButton : dsTypeButtons) {
    			String buttonId = tButton.getElement().getId();
    			if(buttonId.equals(dsType)) {
    				tButton.setValue(true);
    				this.selectedSourceType = dsType;
    			}
    		}
    	}
    	updateStatus();
    }
    
    private void deselectDSTypeButtons() {
		// First de-select all to clear current toggle
		for(ToggleButton tButton : dsTypeButtons) {
				tButton.setValue(false);
		}
		// Also make sure addType button is unToggled
		if(this.addTypeButton!=null) {
			this.addTypeButton.setValue(false);
		}
    }
        
    /**
     * Populate the Data Source Type ListBox
     */
    protected void doPopulateTranslatorListBox() {
    	teiidService.getTranslators(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> translators) {
            	allTranslators.clear();
            	allTranslators.addAll(translators);
                populateTranslatorListBox(translators);
                doPopulateImportPropertyMap(translators);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ds-properties-panel.error-populating-translators"), error); //$NON-NLS-1$
            }
        });
    }
    
    protected void doPopulateImportPropertyMap(List<String> translators) {
    	teiidService.getImportPropertiesMap(translators, new IRpcServiceInvocationHandler<Map<String,List<TranslatorImportPropertyBean>>>() {
            @Override
            public void onReturn(Map<String,List<TranslatorImportPropertyBean>> importPropMap) {
            	importPropertyMap.clear();
            	importPropertyMap.putAll(importPropMap);
                updateStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ds-properties-panel.error-populating-translators"), error); //$NON-NLS-1$
            }
        });
    }

    private void populateTranslatorListBox(List<String> translators) {
    	// Make sure clear first
    	translatorListBox.clear();

    	translatorListBox.insertItem(Constants.NO_TRANSLATOR_SELECTION, 0);
    	
    	// Repopulate the ListBox. The actual names 
    	int i = 1;
    	for(String translatorName: translators) {
    		translatorListBox.insertItem(translatorName, i);
    		i++;
    	}

    	// Initialize by setting the selection to the first item.
    	translatorListBox.setSelectedIndex(0);
    }
    
    /**
     * Get the selected translator from the translator dropdown
     * @return
     */
    public String getSelectedTranslator() {
    	int index = translatorListBox.getSelectedIndex();
    	return translatorListBox.getValue(index);
    }
    
	public void setSelectedTranslator(String translatorName) {
		int indx = 0;
		int nItems = translatorListBox.getItemCount();
		for(int i=0; i<nItems; i++) {
			String itemText = translatorListBox.getItemText(i);
			if(itemText.equalsIgnoreCase(translatorName)) {
				indx = i;
				break;
			}
		}
		translatorListBox.setSelectedIndex(indx);
	}
	
	public void selectTranslatorForSource(String sourceName) {
		String translator = this.defaultTranslatorMap.get(sourceName);
		if(!StringUtils.isEmpty(translator)) {
			setSelectedTranslator(translator);
		} else {
			setSelectedTranslator(Constants.NO_TRANSLATOR_SELECTION);
		}
	}

	public void setDefaultTranslatorMappings(Map<String,String> defaultTranslatorMap) {
		this.defaultTranslatorMap.clear();
		this.defaultTranslatorMap.putAll(defaultTranslatorMap);
	}

    /**
     * Populate the properties table for the supplied Source Type
     * @param selectedType the selected SourceType
     */
    protected void doPopulatePropertiesTable(String selectedType) {
        if(selectedType.equals(Constants.NO_TYPE_SELECTION)) {
        	dataSourceCorePropertyEditor.clear();
        	advPropsAccordion.clearProperties();
        	importPropsAccordion.clearProperties();
        	return;
        }

        teiidService.getDataSourceTypeProperties(selectedType, new IRpcServiceInvocationHandler<List<DataSourcePropertyBean>>() {
            @Override
            public void onReturn(List<DataSourcePropertyBean> propList) {
            	currentPropList.clear();
            	currentPropList.addAll(propList);
                populateCorePropertiesTable();
                populateAdvancedPropertiesTable();
        		updateStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ds-properties-panel.error-populating-properties"), error); //$NON-NLS-1$
            }
        });
    }
    
    /**
     * Create a VDB and corresponding teiid source.  Used when there are no changes to the underlying source.
     * @param dsDetailsBean the data source details
     */
    private void doCreateSourceVdbWithTeiidDS(final DataSourceWithVdbDetailsBean detailsBean, final int vdbDeployTimeoutSec) {
    	final String dsName = detailsBean.getName();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("ds-properties-panel.creating-vdbwsource-title"), //$NON-NLS-1$
                i18n.format("ds-properties-panel.creating-vdbwsource-msg", dsName)); //$NON-NLS-1$
    	
        // fire event
        fireStatusEvent(UiEventType.DATA_SOURCE_DEPLOY_STARTING,dsName,null);

        teiidService.createSourceVdbWithTeiidDS(detailsBean, vdbDeployTimeoutSec, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.vdbwsource-created"), //$NON-NLS-1$
                        i18n.format("ds-properties-panel.create-vdbwsource-complete-msg")); //$NON-NLS-1$

            	// fire event - Deployment complete (but there may be a connection issue)
                fireStatusEvent(UiEventType.DATA_SOURCE_DEPLOY_COMPLETE,dsName,null);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.create-vdbwsource-error"), //$NON-NLS-1$
                        error);
                
            	// fire event
                fireStatusEvent(UiEventType.DATA_SOURCE_DEPLOY_FAIL,dsName,null);
            }
        });
    }
    
    /**
     * Creates a DataSource
     * @param dsDetailsBean the data source details
     */
    private void doCreateDataSource(final DataSourceWithVdbDetailsBean detailsBean, final int vdbDeployTimeoutSec) {
    	final String dsName = detailsBean.getName();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("ds-properties-panel.creating-datasource-title"), //$NON-NLS-1$
                i18n.format("ds-properties-panel.creating-datasource-msg", dsName)); //$NON-NLS-1$

        // fire event
        fireStatusEvent(UiEventType.DATA_SOURCE_DEPLOY_STARTING,dsName,null);

        teiidService.createDataSourceWithVdb(detailsBean, vdbDeployTimeoutSec, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.datasource-created"), //$NON-NLS-1$
                        i18n.format("ds-properties-panel.create-datasource-complete-msg")); //$NON-NLS-1$

            	// fire event - Deployment complete (but there still may be a connection issue)
                fireStatusEvent(UiEventType.DATA_SOURCE_DEPLOY_COMPLETE,dsName,null);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.create-error"), //$NON-NLS-1$
                        error);
                
            	// fire event
                fireStatusEvent(UiEventType.DATA_SOURCE_DEPLOY_FAIL,dsName,null);
            }
        });
    }
    
    /**
     * Fire status event for a dataSource
     * @param eventType the type of event
     * @param dataSourceName the datasource name
     */
    private void fireStatusEvent(UiEventType eventType, String dataSourceName, String message) {
		UiEvent uiEvent = new UiEvent(eventType);
		uiEvent.setDataSourceName(dataSourceName);
		statusEvent.fire(uiEvent);
    }
    
    /**
     * Called when the user confirms the dataSource deletion.
     */
    private void doDeleteThenCreateDataSource(final List<String> dsNamesToDelete, final String srcVdbName, final DataSourceWithVdbDetailsBean detailsBean) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("ds-properties-panel.creating-datasource-title"), //$NON-NLS-1$
                i18n.format("ds-properties-panel.creating-datasource-msg")); //$NON-NLS-1$
        
        // fire event to show in progress
        fireStatusEvent(UiEventType.DATA_SOURCE_DEPLOY_STARTING,detailsBean.getName(),null);

        teiidService.deleteSourcesAndVdbRedeployRenamed(dsNamesToDelete, srcVdbName, detailsBean, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.datasource-created"), //$NON-NLS-1$
                        i18n.format("ds-properties-panel.create-success-msg")); //$NON-NLS-1$
                
                // if the in-process edit contains the renamed source, update the state service accordingly
                List<String> updatedViewSrcs = new ArrayList<String>();
            	@SuppressWarnings("unchecked")
        		List<String> svcViewSrcs = (List<String>)stateService.get(ApplicationStateKeys.IN_PROGRESS_SVC_VIEW_SRCS);
            	for(String svcViewSrc : svcViewSrcs) {
            		if(svcViewSrc.equals(originalName)) {
            			updatedViewSrcs.add(detailsBean.getName());
            		} else {
            			updatedViewSrcs.add(svcViewSrc);
            		}
            	}
            	stateService.put(ApplicationStateKeys.IN_PROGRESS_SVC_VIEW_SRCS, updatedViewSrcs);

            	// fire event
                fireStatusEvent(UiEventType.DATA_SOURCE_DEPLOY_COMPLETE,detailsBean.getName(),null);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.create-error"), //$NON-NLS-1$
                        error);
            	// fire event
                fireStatusEvent(UiEventType.DATA_SOURCE_DEPLOY_FAIL,detailsBean.getName(),null);
            }
        });
    }
    
    /*
     * Populate the core properties table
     */
    private void populateCorePropertiesTable( ) {
    	dataSourceCorePropertyEditor.clear();

    	List<DataSourcePropertyBean> corePropList = getPropList(this.currentPropList, true, true);
    	dataSourceCorePropertyEditor.setProperties(corePropList);
    }

    /*
     * Populate the advanced properties table
     */
    private void populateAdvancedPropertiesTable() {
    	advPropsAccordion.clearProperties();

    	List<DataSourcePropertyBean> advPropList = getPropList(this.currentPropList, false, true);
    	advPropsAccordion.setProperties(advPropList);
    }
    
    /*
     * Populate the import properties table with current prop list
     */
    private void populateImportPropertiesTableWithCurrent( ) {
    	importPropsAccordion.clearProperties();

    	importPropsAccordion.setProperties(this.currentImportPropList);
    }
    
    /*
     * Filters the supplied list by correct type and order
     * @param propList the complete list of properties
     * @param getCore if 'true', returns the core properties.  if 'false' returns the advanced properties
     * @param acending if 'true', sorts in ascending name order.  descending if 'false'
     */
    private List<DataSourcePropertyBean> getPropList(List<DataSourcePropertyBean> propList, boolean getCore, boolean ascending) {
    	List<DataSourcePropertyBean> resultList = new ArrayList<DataSourcePropertyBean>();
    	
    	// Put only the desired property type into the resultList
    	for(DataSourcePropertyBean prop : propList) {
    		if(getCore && DataSourceHelper.isCoreProperty(prop, propList)) {
    			resultList.add(prop);
    		} else if(!getCore && !DataSourceHelper.isCoreProperty(prop, propList)) {
    			resultList.add(prop);    			
    		}
    	}
    	
    	// Sort by name in the desired order
    	Collections.sort(resultList,new PropertyBeanComparator(ascending));
    	
    	return resultList;
    }
    
    public void setDataSource(DataSourcePageRow dsRow, List<DataSourcePageRow> allDSRows) {
    	updateExistingDSNames(dsRow,allDSRows);
    	
    	// PlaceHolder
    	if(dsRow.getState()==DataSourcePageRow.State.PLACEHOLDER) {
    		isNewSource = true;
    		
    		String title = i18n.format("ds-properties-panel.createnew-title");
        	dsDetailsPanelTitle.setText(title);
        	
    		setNameValidators();
        	nameTextBox.setText(Constants.BLANK);
        	originalName = Constants.BLANK;
        	
        	deselectDSTypeButtons();
        	this.selectedSourceType = dsRow.getType();
        	originalType = selectedSourceType;
        	
   			setSelectedTranslator(Constants.NO_TRANSLATOR_SELECTION);
   			originalTranslator=Constants.NO_TRANSLATOR_SELECTION;

        	currentPropList.clear();
        	populateCorePropertiesTable();
        	populateAdvancedPropertiesTable();
        	
        	currentImportPropList.clear();
        	populateImportPropertiesTableWithCurrent();
        	
        	updateStatus();
    	// Existing Source
    	} else {
    		isNewSource = false;
    		doGetDataSourceDetails(dsRow.getName());
    	}
    }
    
    private void setWidgetsVisibility(boolean isPlaceHolder) {
    	// placeholder (new source) - display widgets as selections are made
    	if(isPlaceHolder) {
    		// If no type is selected, hide components
    		boolean typeSelected = getSelectedDataSourceType()!=null;
    		nameTextBox.setVisible(typeSelected);
    		namedTranslatorListBox.setVisible(typeSelected);
    		timeoutTextBox.setVisible(typeSelected);

    		boolean translatorSelected = !getSelectedTranslator().equals(Constants.NO_TRANSLATOR_SELECTION);
			corePropsTitle.setVisible(translatorSelected);
    	    dataSourceCorePropertyEditor.setVisible(translatorSelected);
    	    saveSourceChanges.setVisible(translatorSelected);
    		
    		if(!translatorSelected) {
        	    advPropsAccordion.setVisible(false);
        	    importPropsAccordion.setVisible(false);
    		} else {
        	    advPropsAccordion.setVisible(advPropsAccordion.hasProperties());
        	    importPropsAccordion.setVisible(importPropsAccordion.hasProperties());
    		}
    	// Not a placeholder - show all widgets
    	} else {
    		nameTextBox.setVisible(true);
    		namedTranslatorListBox.setVisible(true);
    		timeoutTextBox.setVisible(true);
			corePropsTitle.setVisible(true);
    	    dataSourceCorePropertyEditor.setVisible(true);
    	    advPropsAccordion.setVisible(advPropsAccordion.hasProperties());
    	    importPropsAccordion.setVisible(importPropsAccordion.hasProperties());
    	    saveSourceChanges.setVisible(true);
    	}
    }
    
    /*
     * Updates the list of DSNames used for checking existing sources when user enters a name.  The selectedRow name is omitted from the list
     * since it is by definition valid.
     */
    private void updateExistingDSNames(DataSourcePageRow selectedRow, List<DataSourcePageRow> allDSRows) {
    	this.existingDSNames.clear();
    	for(DataSourcePageRow aRow : allDSRows) {
    		if(aRow.getState()!=DataSourcePageRow.State.PLACEHOLDER && !aRow.getName().equals(selectedRow.getName())) {
    			this.existingDSNames.add(aRow.getName());
    		}
    	}
    }
    
    /**
     * Get the Data Source With Source VDB details for the current Data Source name.
     * Populate the PropertiesPanel with it's properties
     * @param dataSourceName the data source name
     */
    protected void doGetDataSourceDetails(String dataSourceName) {

    	teiidService.getDataSourceWithVdbDetails(dataSourceName, new IRpcServiceInvocationHandler<DataSourceWithVdbDetailsBean>() {
            @Override
            public void onReturn(DataSourceWithVdbDetailsBean dsDetailsBean) {
            	String title = dsDetailsBean.getName();
            	dsDetailsPanelTitle.setText(title);
            	
            	setNameValidators();
            	nameTextBox.setText(dsDetailsBean.getName());
            	originalName = dsDetailsBean.getName();
            	
            	setSelectedDataSourceType(dsDetailsBean.getType());
            	originalType = dsDetailsBean.getType();
            	
            	String translator = dsDetailsBean.getTranslator();
            	if(!StringUtils.isEmpty(translator)) {
            		setSelectedTranslator(translator);
            		originalTranslator=translator;
            	} else {
        			setSelectedTranslator(Constants.NO_TRANSLATOR_SELECTION);
        			originalTranslator=Constants.NO_TRANSLATOR_SELECTION;
            	}

            	currentPropList.clear();
            	List<DataSourcePropertyBean> props = dsDetailsBean.getProperties();
            	currentPropList.addAll(props);
            	populateCorePropertiesTable();
            	populateAdvancedPropertiesTable();
            	
            	currentImportPropList.clear();
            	List<TranslatorImportPropertyBean> importProps = dsDetailsBean.getImportProperties();
            	currentImportPropList.addAll(importProps);
            	populateImportPropertiesTableWithCurrent();
            	
            	updateStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ds-properties-panel.details-fetch-error"), error); //$NON-NLS-1$
            }
        });       
        
    }
    
    /**
     * Set the name validators
     */
    private void setNameValidators( ) {
    	nameTextBox.clearValidators();
    	nameTextBox.addValidator(new EmptyNameValidator());
    	nameTextBox.addValidator(new ServiceNameValidator());
    	nameTextBox.addValidator(new DuplicateNameValidator(this.existingDSNames));
    }
    
    private void updateStatus( ) {
    	setWidgetsVisibility(this.isNewSource);
    	
    	String status = Constants.OK;
    			
    	// Warn for no type selection
    	String dsType = getSelectedDataSourceType();
    	if(StringUtils.isEmpty(dsType)) {
    		status = statusSelectType;
    	}
    	
        // Checks validity of service name entry
    	if(status.equals(Constants.OK)) {
    		boolean isOK = nameTextBox.isValid();
    		if(!isOK) {
    			status = statusEnterProps;
    		}
    	}
		    	
		// Warn for translator not selected
    	if(status.equals(Constants.OK)) {
        	String translator = getSelectedTranslator();
    		if(translator!=null && translator.equals(Constants.NO_TRANSLATOR_SELECTION)) {
    			status = statusEnterProps;
    		}
    	}
    	
		// Get properties message
    	if(status.equals(Constants.OK)) {
    		status = getPropertyStatus();
        	// Replace with generic message if not ok
        	if(!status.equals(Constants.OK)) {
        		status = statusEnterProps;
        	}
    	}
    	
		// Get import properties message
    	if(status.equals(Constants.OK)) {
    		status = getImportPropertyStatus();
        	// Replace with generic message if not ok
        	if(!status.equals(Constants.OK)) {
        		status = statusEnterProps;
        	}
    	}
    	
        // Checks validity of timeout entry
    	if(status.equals(Constants.OK)) {
    		boolean isOK = timeoutTextBox.isValid();
    		if(!isOK) {
    			status = statusEnterProps;
    		}
    	}
    	
		boolean hasNameChange = hasNameChange();
		boolean hasTypeChange = hasDataSourceTypeChange();
		boolean hasTranslatorChange = hasTranslatorChange();
    	boolean hasPropChanges = hasPropertyChanges();
    	boolean hasImportPropChanges = hasImportPropertyChanges();
		if(this.isNewSource) {
			cancelSourceChanges.setEnabled(true);
		} else {
    		if(hasNameChange || hasTypeChange || hasTranslatorChange || hasPropChanges || hasImportPropChanges) {
    			cancelSourceChanges.setEnabled(true);
    		} else {
    			cancelSourceChanges.setEnabled(false);
    		}
		}
		
    	// Determine if any properties were changed
    	if(status.equals(Constants.OK)) {
    		setInfoMessage(statusEnterProps);
    		if(hasNameChange || hasTypeChange || hasTranslatorChange || hasPropChanges || hasImportPropChanges) {
        		saveSourceChanges.setEnabled(true);
        		cancelSourceChanges.setEnabled(true);
    		} else {
    			// External error was set - show it
    			if(!StringUtils.isEmpty(this.externalError)) {
    				setErrorMessage(this.externalError);
    			}
        		saveSourceChanges.setEnabled(false);
    		}
    	} else {
    		setInfoMessage(status);
    		saveSourceChanges.setEnabled(false);
    	}
    }
    
    /**
     * Set the status info message
     */
    private void setInfoMessage(String statusMsg) {
    	statusText.setHTML(UiUtils.getStatusMessageHtml(statusMsg,UiUtils.MessageType.INFO));
    }
    
    /**
     * Allows externally setting an error prefix.  If error is set,
     * then it is shown as a standard error message instead of standard info message
     */
    public void setExternalError(String externalError) {
    	this.externalError = externalError;
    }
    
    /**
     * Set the status error message
     */
    private void setErrorMessage(String statusMsg) {
    	statusText.setHTML(UiUtils.getStatusMessageHtml(statusMsg,UiUtils.MessageType.ERROR));
    }
    
    /**
     * Returns 'true' if the name has changed, false if not
     * @return name changed status
     */
    private boolean hasNameChange() {
    	return !StringUtils.valuesAreEqual(this.originalName, this.nameTextBox.getText());
    }
    
    /**
     * Returns 'true' if the type has changed, false if not
     * @return type changed status
     */
    private boolean hasDataSourceTypeChange() {
    	return !StringUtils.valuesAreEqual(this.originalType, getSelectedDataSourceType());
    }
    
    /**
     * Returns 'true' if the translator has changed, false if not
     * @return translator changed status
     */
    private boolean hasTranslatorChange() {
    	return !StringUtils.valuesAreEqual(this.originalTranslator, getSelectedTranslator());
    }
    
    /**
     * Returns 'true' if any properties have changed, false if no changes
     * @return property changed status
     */
    private boolean hasPropertyChanges() {
    	boolean coreTableHasChanges = this.dataSourceCorePropertyEditor.anyPropertyHasChanged();
    	boolean advTableHasChanges = this.advPropsAccordion.anyPropertyHasChanged();
    	
    	return (coreTableHasChanges || advTableHasChanges) ? true : false;
    }
    
    private String getPropertyStatus() {
    	// ------------------
    	// Set status message
    	// ------------------
    	String propStatus = this.dataSourceCorePropertyEditor.getStatus();
    	if(propStatus.equalsIgnoreCase(Constants.OK)) {
    		propStatus = this.advPropsAccordion.getStatus();
    	}

    	return propStatus;
    }
    
    /**
     * Returns 'true' if any import properties have changed, false if no changes
     * @return import property changed status
     */
    private boolean hasImportPropertyChanges() {
    	return this.importPropsAccordion.anyPropertyHasChanged();
    }
    
    private String getImportPropertyStatus() {
    	// ------------------
    	// Set status message
    	// ------------------
    	String propStatus = this.importPropsAccordion.getStatus();
    	if(propStatus.equalsIgnoreCase(Constants.OK)) {
    		propStatus = this.importPropsAccordion.getStatus();
    	}

    	return propStatus;
    }
    
}