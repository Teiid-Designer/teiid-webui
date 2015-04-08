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
package org.teiid.webui.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * GWT managed images for Workbench
 */
public interface AppImages extends ClientBundle {

    @Source("images/teiid_user_logo.png")
    ImageResource ufUserLogo();
    
    @Source("images/EmptyLibraryImage.png")
    ImageResource emptyLibraryImage();
    
    @Source("images/spinner_16.gif")
    ImageResource spinnner16x16Image();
    
    @Source("images/spinner_24.gif")
    ImageResource spinnner24x24Image();
    
    @Source("images/Error_16x16.png")
    ImageResource errorIcon16x16Image();
    
    @Source("images/Error_32x32.png")
    ImageResource errorIcon32x32Image();
    
    @Source("images/Warning_16x16.png")
    ImageResource warningIcon16x16Image();
    
    @Source("images/Warning_32x32.png")
    ImageResource warningIcon32x32Image();
    
    @Source("images/Ok_16x16.png")
    ImageResource okIcon16x16Image();
    
    @Source("images/Ok_32x32.png")
    ImageResource okIcon32x32Image();
    
    @Source("images/bullet_yellow.png")
    ImageResource yellowBulletImage();

    @Source("images/Google_dv_logos_170x50.png")
    ImageResource dsType_google();

    @Source("images/H2_dv_logos_170x50.png")
    ImageResource dsType_h2();

    @Source("images/Infinispan_dv_logos_170x50.png")
    ImageResource dsType_infinispan();

    @Source("images/ModeShape_dv_logos_170x50.png")
    ImageResource dsType_modeshape();

    @Source("images/MongoDB_dv_logos_170x50.png")
    ImageResource dsType_mongodb();

    @Source("images/MySQL_dv_logos_170x50.png")
    ImageResource dsType_mysql();

    @Source("images/PostgresSql_dv_logos_170x50.png")
    ImageResource dsType_postgres();

    @Source("images/Salesforce_dv_logos_170x50.png")
    ImageResource dsType_salesforce();

    @Source("images/Teiid_dv_logos_170x50.png")
    ImageResource dsType_teiid();

    @Source("images/Teiid_dv_logos_170x50.png")
    ImageResource dsType_teiid_local();
    
    @Source("images/LDAP_dv_logos_170x50.png")
    ImageResource dsType_ldap();
    
    @Source("images/FileSystem_dv_logos_170x50.png")
    ImageResource dsType_file();
    
    @Source("images/WebService_dv_logos_170x50.png")
    ImageResource dsType_webservice();
    
    @Source("images/addtype_dv_logos_170x50.png")
    ImageResource dsType_addtype_Image();

    @Source("images/dstype_blankbox_small.png")
    ImageResource dsType_blankbox_small_Image();

    @Source("images/Google_dv_logos_70x40.png")
    ImageResource dsType_google_small_Image();

    @Source("images/H2_dv_logos_70x40.png")
    ImageResource dsType_h2_small_Image();

    @Source("images/Infinispan_dv_logos_70x40.png")
    ImageResource dsType_infinispan_small_Image();

    @Source("images/ModeShape_dv_logos_70x40.png")
    ImageResource dsType_modeshape_small_Image();

    @Source("images/MongoDB_dv_logos_70x40.png")
    ImageResource dsType_mongodb_small_Image();

    @Source("images/MySQL_dv_logos_70x40.png")
    ImageResource dsType_mysql_small_Image();

    @Source("images/PostgresSql_dv_logos_70x40.png")
    ImageResource dsType_postgres_small_Image();

    @Source("images/Salesforce_dv_logos_70x40.png")
    ImageResource dsType_salesforce_small_Image();

    @Source("images/Teiid_dv_logos_70x40.png")
    ImageResource dsType_teiid_small_Image();
    
    @Source("images/LDAP_dv_logos_70x40.png")
    ImageResource dsType_ldap_small_Image();
    
    @Source("images/FileSystem_dv_logos_70x40.png")
    ImageResource dsType_file_small_Image();
    
    @Source("images/WebService_dv_logos_70x40.png")
    ImageResource dsType_webservice_small_Image();
    
    @Source("images/JoinInner.png")
    ImageResource joinInner_Image();
        
    @Source("images/JoinFullOuter.png")
    ImageResource joinFullOuter_Image();
    
    @Source("images/JoinLeftOuter.png")
    ImageResource joinLeftOuter_Image();
    
    @Source("images/JoinRightOuter.png")
    ImageResource joinRightOuter_Image();
}
