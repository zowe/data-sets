/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;
import org.zowe.unix.files.services.UnixFilesService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/unixfiles")
@Api(value = "Unix Files APIs")
public class UnixFilesController {
    
    @Autowired
    private UnixFilesService unixFileService;
    
    @GetMapping(value = "", produces = { "application/json" })
    @ApiOperation(value = "Get a list of a directories contents", nickname = "getDirectoryListing", notes = "This API gets a list of files and directories for a given path", tags = "Unix Files APIs")
    @ApiResponses({ @ApiResponse(code = 200, message = "Ok", response = UnixDirectoryAttributesWithChildren.class)})
    public UnixDirectoryAttributesWithChildren getUnixDirectoryListing(
            @ApiParam(value = "Path of Directory to be listed", required = true) @RequestParam String path) {
        return unixFileService.listUnixDirectory(path);
    }
    
    @GetMapping(value = "{path}/**", produces = { "application/json" })
    @ApiOperation(value = "Get the contents of a Unix file", nickname = "getUnixFileContents", 
    notes = "This API gets a the contetns of a Unix file. Try it out function will not work due to the encoding of forward slashes, "
            + "it should be noted that requests to this endpoint should only contain unencoded slashes", tags = "Unix Files APIs")
    @ApiResponses({ @ApiResponse(code = 200, message = "Ok", response = UnixFileContent.class)})
    public ResponseEntity<UnixFileContent> getUnixFileContent(@PathVariable String path, HttpServletRequest request) {
        String requestPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        String fullPath = requestPath.substring(requestPath.indexOf("/api/v1/unixfiles") + 17);
        UnixFileContentWithETag content = unixFileService.getUnixFileContentWithETag(fullPath);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Expose-Headers", "ETag");
        headers.add("ETag", content.getETag());
        return new ResponseEntity<>(content.getContent(), headers, HttpStatus.OK);
    }
}
