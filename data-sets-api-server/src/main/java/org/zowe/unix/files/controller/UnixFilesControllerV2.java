/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2020
 */

package org.zowe.unix.files.controller;

import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.unix.files.services.UnixFilesService;

@RestController
@RequestMapping("/api/v2/unixfiles")
@Api(value = "Unix Files APIs V2", tags = "Unix Files APIs V2")
public class UnixFilesControllerV2 extends AbstractUnixFilesController {

    private static final String ENDPOINT_ROOT = "/api/v2/unixfiles";
    
    @Autowired
    @Qualifier("ZosmfUnixFilesService2")
    private UnixFilesService unixFilesService; 
    
    @Override
    UnixFilesService getUnixFileService() {
        return unixFilesService;
    }
    
    @Override
    String getEndPointRoot() {
        return ENDPOINT_ROOT;
    }
    
    

}
