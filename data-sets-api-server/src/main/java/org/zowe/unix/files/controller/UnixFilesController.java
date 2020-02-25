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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zowe.unix.files.model.UnixCreateAssetRequest;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;
import org.zowe.unix.files.services.UnixFilesService;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/unixfiles")
@Api(value = "Unix Files APIs", tags = "Unix Files APIs")
public class UnixFilesController {

    @Autowired
    private UnixFilesService unixFileService;

    @GetMapping(value = "", produces = { "application/json" })
    @ApiOperation(value = "Get a list of a directories contents", nickname = "getDirectoryListing", notes = "This API gets a list of files and directories for a given path", tags = "Unix Files APIs")
    @ApiResponses({ @ApiResponse(code = 200, message = "Ok", response = UnixDirectoryAttributesWithChildren.class) })
    public UnixDirectoryAttributesWithChildren getUnixDirectoryListing(
            @ApiParam(value = "Path of Directory to be listed", required = true) @RequestParam String path) {
        return unixFileService.listUnixDirectory(path);
    }

    private String getPathFromRequest(HttpServletRequest request) {
        String requestPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        String fullPath = requestPath.substring(requestPath.indexOf("/api/v1/unixfiles") + 17);
        return fullPath;
    }

    @GetMapping(value = "{path}/**", produces = { "application/json" })
    @ApiOperation(value = "Get the contents of a Unix file", nickname = "getUnixFileContents", notes = "This API gets a the contetns of a Unix file. Try it out function will not work due to the encoding of forward slashes, "
            + "it should be noted that requests to this endpoint should only contain unencoded slashes and not include wild card characters", tags = "Unix Files APIs")
    @ApiResponses({ @ApiResponse(code = 200, message = "Ok", response = UnixFileContent.class) })
    public ResponseEntity<UnixFileContent> getUnixFileContent(@PathVariable String path, HttpServletRequest request,
            @RequestHeader(value = "Convert", required = false) Boolean convert) {

        String fullPath = getPathFromRequest(request);

        if (convert == null) {
            convert = unixFileService.shouldUnixFileConvert(fullPath);
        }
        UnixFileContentWithETag content = unixFileService.getUnixFileContentWithETag(fullPath, convert);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Expose-Headers", "ETag");
        headers.add("ETag", content.getETag());
        return new ResponseEntity<>(content.getContent(), headers, HttpStatus.OK);
    }

    @PutMapping(value = "{path}/**", produces = { "application/json" })
    @ApiOperation(value = "Update the contents of a Unix file", nickname = "putUnixFileContents", notes = "This API will update the contents of a Unix file. Try it out function will not work due to the encoding of forward slashes, "
            + "it should be noted that requests to this endpoint should only contain unencoded slashes and not include wild card characters", tags = "Unix Files APIs")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Ok") })
    public ResponseEntity<?> putUnixFileContent(@PathVariable String path, HttpServletRequest request,
            @RequestBody UnixFileContent input, @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestHeader(value = "Convert", required = false) Boolean convert) {

        UnixFileContentWithETag contentWithETag = new UnixFileContentWithETag(input, ifMatch);
        String fullPath = getPathFromRequest(request);

        // Ensure file already exists
        unixFileService.getUnixFileContentWithETag(fullPath, false);

        if (convert == null) {
            convert = unixFileService.shouldUnixFileConvert(fullPath);
        }
        String putETag = unixFileService.putUnixFileContent(fullPath, contentWithETag, convert);

        return ResponseEntity.noContent().eTag(putETag).build();
    }

    @DeleteMapping(value = "{path}/**", produces = { "application/json" })
    @ApiOperation(value = "Delete a Unix file", nickname = "deleteUnixFile", notes = "This API deletes a Unix file or directory. Try it out function will not work due to the encoding of forward slashes, "
            + "it should be noted that requests to this endpoint should only contain unencoded slashes", tags = "Unix Files APIs")
    @ApiResponses({ @ApiResponse(code = 204, message = "Unix file successfully deleted") })
    public ResponseEntity<?> deleteUnixFile(@PathVariable String path, HttpServletRequest request,
            @RequestHeader(value = "recursive", required = false, defaultValue = "false") boolean isRecursive) {
        String fullPath = getPathFromRequest(request);
        unixFileService.deleteUnixFileContent(fullPath, isRecursive);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "{path}/**", produces = { "application/json" })
    @ApiOperation(value = "Create a new Unix File or Diretory", nickname = "postUnixFileOrDirectory", notes = "This API will create a new UnixFile or Directory. Try it out function not functional due to encoding of slashes and auto insertion of wildcard characters, "
            + "an example request path would be /api/v1/unixFiles/u/ibmuser/newDirectory", tags = "Unix Files APIs")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created") })
    public ResponseEntity<?> createUnifFileOrDirectory(@PathVariable String path, HttpServletRequest request,
            @RequestBody UnixCreateAssetRequest input) {

        unixFileService.createUnixAsset(getPathFromRequest(request), input);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
        return ResponseEntity.created(location).build();
    }
}
