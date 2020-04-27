/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019, 2020
 */
package org.zowe.unix.files.services.zosmf;

import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.unix.files.model.UnixCreateAssetRequest;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixFileContentWithETag;
import org.zowe.unix.files.services.UnixFilesService;

public abstract class AbstractZosmfUnixFilesService implements UnixFilesService {
    
    abstract ZosmfConnector getZosmfConnector();
    
    @Override
    public UnixDirectoryAttributesWithChildren listUnixDirectory(String path, String hypermediaLinkToBase) {
        ListUnixDirectoryZosmfRunner runner = new ListUnixDirectoryZosmfRunner(path, hypermediaLinkToBase);
        return runner.run(getZosmfConnector());
    }

    @Override
    public UnixFileContentWithETag getUnixFileContentWithETag(String path, boolean convert, boolean decode) {
        GetUnixFileContentZosmfRunner runner = new GetUnixFileContentZosmfRunner(path, convert, decode);
        return runner.run(getZosmfConnector());
    }

    
    @Override
    public String putUnixFileContent(String path, UnixFileContentWithETag content, boolean convert) {
        PutUnixFileContentZosmfRunner runner = new PutUnixFileContentZosmfRunner(path, content, convert);
        return runner.run(getZosmfConnector());
    }
    
    @Override
    public boolean shouldUnixFileConvert(String path) {
        String codepage = getUnixFileChtag(path);
        if (codepage.contains("ISO8859") || codepage.contains("IBM-850") || codepage.contains("UTF")) {
           return true;
        } 
        return false;
    }

    @Override
    public String getUnixFileChtag(String path) {
        GetUnixFileChtagZosmfRunner runner = new GetUnixFileChtagZosmfRunner(path);
        return runner.run(getZosmfConnector());
    }
    
    @Override
    public void deleteUnixFileContent(String path, boolean isRecursive) {
        DeleteUnixFileZosmfRunner runner = new DeleteUnixFileZosmfRunner(path, isRecursive);
        runner.run(getZosmfConnector());
    }

    @Override
    public void createUnixAsset(String path, UnixCreateAssetRequest request) {
        CreateUnixAssetZosmfRunner runner = new CreateUnixAssetZosmfRunner(path, request);
        runner.run(getZosmfConnector());
    }
}
