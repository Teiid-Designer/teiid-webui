package org.teiid.webui.client.widgets.table;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A version of CheckboxCell that can be disabled.  User can set a variable that determines if the 
 * unchecked checkboxes are disabled.
 */
public final class DisableableCheckboxCell extends CheckboxCell {
    /**
     * A HTML string representation of a checked input box.
     */
    private static final SafeHtml INPUT_CHECKED = SafeHtmlUtils
            .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked/>");

    /**
     * A HTML string representation of an unchecked input box.
     */
    private static final SafeHtml INPUT_UNCHECKED = SafeHtmlUtils
            .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\"/>");

//    /**
//     * A HTML string representation of a disabled checked input box.
//     */
//    private static final SafeHtml INPUT_CHECKED_DISABLED = SafeHtmlUtils
//            .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled=\"disabled\"/>");

    /**
     * A HTML string representation of an disabled unchecked input box.
     */
    private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils
            .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>");

    private boolean disableIfUnchecked = false;

    public DisableableCheckboxCell(final boolean disableIfUnchecked) {
        this.disableIfUnchecked = disableIfUnchecked;
    }

    public DisableableCheckboxCell(final boolean disableIfUnchecked, final boolean dependsOnSelection, final boolean handlesSelection) {
        super(dependsOnSelection, handlesSelection);
        this.disableIfUnchecked = disableIfUnchecked;
    }
    
    public void setDisableIfUnchecked(boolean disableUnchecked) {
    	this.disableIfUnchecked = disableUnchecked;
    }

    @Override
    public void render(final Context context, final Boolean value, final SafeHtmlBuilder sb) {
        if (value) {
            sb.append(INPUT_CHECKED);
        } else if (!value && disableIfUnchecked) {
            sb.append(INPUT_UNCHECKED_DISABLED);
        } else {
            sb.append(INPUT_UNCHECKED);
        }
    }
}