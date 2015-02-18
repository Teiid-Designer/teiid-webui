/*
 * Copyright 2012 JBoss Inc
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
package org.teiid.webui.client.resources;

import org.teiid.webui.client.widgets.table.TeiidSimplePager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * A ClientBundle that provides images for this widget.
 */
public interface TeiidSimplePagerResources
        extends
        ClientBundle {

    TeiidSimplePagerResources INSTANCE = GWT.create( TeiidSimplePagerResources.class );

    /**
     * The image used to skip ahead multiple pages.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerFastForward.png")
    ImageResource simplePagerFastForward();

    /**
     * The disabled "fast forward" image.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerFastForwardDisabled.png")
    ImageResource simplePagerFastForwardDisabled();

    /**
     * The image used to go to the first page.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerFirstPage.png")
    ImageResource simplePagerFirstPage();

    /**
     * The disabled first page image.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerFirstPageDisabled.png")
    ImageResource simplePagerFirstPageDisabled();

    /**
     * The image used to go to the last page.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerLastPage.png")
    ImageResource simplePagerLastPage();

    /**
     * The disabled last page image.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerLastPageDisabled.png")
    ImageResource simplePagerLastPageDisabled();

    /**
     * The image used to go to the next page.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerNextPage.png")
    ImageResource simplePagerNextPage();

    /**
     * The disabled next page image.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerNextPageDisabled.png")
    ImageResource simplePagerNextPageDisabled();

    /**
     * The image used to go to the previous page.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerPreviousPage.png")
    ImageResource simplePagerPreviousPage();

    /**
     * The disabled previous page image.
     */
    @ImageResource.ImageOptions(flipRtl = true)
    @Source("images/simplePagerPreviousPageDisabled.png")
    ImageResource simplePagerPreviousPageDisabled();

    /**
     * The styles used in this widget.
     */
    @Source("css/TeiidSimplePager.css")
    TeiidSimplePager.Style simplePagerStyle();

}
