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
package org.teiid.webui.backend.server.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.teiid.webui.backend.server.api.AdminApiClientAccessor;
import org.teiid.webui.backend.server.services.TeiidService;
import org.teiid.webui.share.Constants;

/**
 * A standard servlet that file content is POSTed to in order to add new files (drivers and vdbs) to the Teiid Server.
 *
 * @author mdrillin@redhat.com
 */
public class DataVirtUploadServlet extends HttpServlet {

	private static final long serialVersionUID = DataVirtUploadServlet.class.hashCode();

    @Inject
    private AdminApiClientAccessor clientAccessor;
    
    @Inject
    protected TeiidService teiidService;

	/**
	 * Constructor.
	 */
	public DataVirtUploadServlet() {
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		// Extract the relevant content from the POST'd form
		if (ServletFileUpload.isMultipartContent(req)) {
			Map<String, String> responseMap;
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			String deploymentName = null;
			String fileName = null;
			InputStream fileContent = null;
			try {
				List<FileItem> items = upload.parseRequest(req);
				boolean isDriver = false;
				for (FileItem item : items) {
					// Deployment name for drivers
					if (item.isFormField()) {
						if (item.getFieldName().equals("driverDeploymentName")) {
							deploymentName = item.getString();
							isDriver=true;
						}
					// File Content
					} else {
						fileName = item.getName();
						if (fileName != null)
							fileName = FilenameUtils.getName(fileName);
						fileContent = item.getInputStream();
					}
				}

				// Now that the content has been extracted, upload
				if(isDriver) {
					responseMap = uploadDriver(deploymentName, fileContent);
				} else {
					responseMap = uploadVdb(fileName, fileContent);
				}
			} catch (Throwable e) {
				responseMap = new HashMap<String, String>();
				responseMap.put("exception", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				responseMap.put("exception-message", e.getMessage()); //$NON-NLS-1$
//				responseMap.put("exception-stack", ExceptionUtils.getRootStackTrace(e)); //$NON-NLS-1$
			} finally {
				IOUtils.closeQuietly(fileContent);
			}
			writeToResponse(responseMap, response);
		} else {
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
			        "Content type not supported"); //$NON-NLS-1$
		}
	}

    /**
     * Uploads a driver to the Teiid Server
     * @param driverDeploymentName
     * @param driverContent
     * @return responseParams
     * @throws Exception
     */
    private Map<String, String> uploadDriver(String driverDeploymentName, InputStream driverContent) throws Exception {

    	Map<String, String> responseParams = new HashMap<String, String>();
    	
		// Deploy the driver
        InputStream contentStream = null;
		try {
			clientAccessor.getClient().deploy(driverDeploymentName, driverContent);
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
		
		return responseParams;		
    }
    
    /**
     * Uploads a VDB to the Teiid Server
     * @param driverDeploymentName
     * @param driverContent
     * @return responseParams
     * @throws Exception
     */
    private Map<String, String> uploadVdb(String vdbFileName, InputStream vdbContent) throws Exception {

    	Map<String, String> responseParams = new HashMap<String, String>();

    	if(vdbFileName.endsWith(Constants.DYNAMIC_VDB_SUFFIX) || vdbFileName.endsWith(".vdb")) {
    		teiidService.deployVdbWithSource(vdbFileName, vdbContent);
    	}
		
		return responseParams;
		
    }
        
	/**
	 * Writes the response values back to the http response.  This allows the calling code to
	 * parse the response values for display to the user.
	 *
	 * @param responseMap the response params to write to the http response
	 * @param response the http response
	 * @throws IOException
	 */
	private static void writeToResponse(Map<String, String> responseMap, HttpServletResponse response) throws IOException {
        // Note: setting the content-type to text/html because otherwise IE prompt the user to download
        // the result rather than handing it off to the GWT form response handler.
        // See JIRA issue https://issues.jboss.org/browse/SRAMPUI-103
		response.setContentType("text/html; charset=UTF8"); //$NON-NLS-1$
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
        g.useDefaultPrettyPrinter();
        g.writeStartObject();
        for (java.util.Map.Entry<String, String> entry : responseMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            g.writeStringField(key, val);
        }
        g.writeEndObject();
        g.flush();
        g.close();
	}
}
