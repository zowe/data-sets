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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.connectors.zosmf.ZosmfConnectorJWTAuth;

@Service("ZosmfUnixFilesServiceV2")
public class ZosmfUnixFilesServiceV2 extends AbstractZosmfUnixFilesService {
    
    @Autowired
    ZosmfConnectorJWTAuth zosmfConnector;
    
    @Override
    ZosmfConnector getZosmfConnector() {
        return zosmfConnector;
    }
}
