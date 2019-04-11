/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.services.zosmf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixFileContentWithETag;
import org.zowe.unix.files.services.UnixFilesService;

@Service
public class ZosmfUnixFilesService implements UnixFilesService {
    
    @Autowired
    ZosmfConnector zosmfConnector;
    
    @Override
    public UnixDirectoryAttributesWithChildren listUnixDirectory(String path) {
        ListUnixDirectoryZosmfRunner runner = new ListUnixDirectoryZosmfRunner(path);
        return runner.run(zosmfConnector);
    }

    @Override
    public UnixFileContentWithETag getUnixFileContentWithETag(String path, boolean convert) {
        GetUnixFileContentRunner runner = new GetUnixFileContentRunner(path, convert);
        return runner.run(zosmfConnector);
    }
    
    @Override
    public String putUnixFileContent(String path, UnixFileContentWithETag content, boolean convert) {
        PutUnixFileContentZosmfRunner runner = new PutUnixFileContentZosmfRunner(path, content, convert);
        return runner.run(zosmfConnector);
    }
    
    @Override
    public String getUnixFileChtag(String path) {
        GetUnixFileChtagRunner runner = new GetUnixFileChtagRunner(path);
        return runner.run(zosmfConnector);
    }
}
