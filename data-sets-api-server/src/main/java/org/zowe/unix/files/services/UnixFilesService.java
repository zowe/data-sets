/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019, 2020
 */
package org.zowe.unix.files.services;

import lombok.Setter;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.zowe.unix.files.model.UnixCreateAssetRequest;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixFileContentWithETag;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Setter
public abstract class UnixFilesService {
    
    private HttpServletRequest request;
    
    public List<Header> getIbmHeadersFromRequest() {
        ArrayList<Header> ibmHeaders = new ArrayList<Header>();
        if (request != null ) {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement().toUpperCase();
                if (headerName.contains("X-IBM")) {
                    Header newHeader = new BasicHeader(headerName, request.getHeader(headerName));
                    ibmHeaders.add(newHeader);
                }
            }
        }
        return ibmHeaders;
    }
    
    public abstract UnixDirectoryAttributesWithChildren listUnixDirectory(String path, String hypermediaLinkToBase);
    
    public abstract UnixFileContentWithETag getUnixFileContentWithETag(String path, boolean convert, boolean decode);
    
    public abstract String putUnixFileContent(String path, UnixFileContentWithETag content, boolean convert);
    
    public abstract boolean shouldUnixFileConvert(String path);
    
    public abstract String getUnixFileChtag(String path);
    
    public abstract void deleteUnixFileContent(String path, boolean isRecursive);
    
    public abstract void createUnixAsset(String path, UnixCreateAssetRequest request);
}
