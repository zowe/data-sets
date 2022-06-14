/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019, 2020
 */
package org.zowe.unix.files.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.zowe.unix.files.model.UnixCreateAssetRequest;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;
import org.zowe.unix.files.services.UnixFilesService;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractUnixFilesController {

    abstract UnixFilesService getUnixFileService();

    abstract String getEndPointRoot();

    private String getPathFromRequest(HttpServletRequest request) {
        String requestPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        String fullPath = requestPath.substring(requestPath.indexOf(getEndPointRoot()) + 17);
        return fullPath;
    }

    private UriComponents getLinkToBaseURI(HttpServletRequest request) {
        String requestMappingPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        UriComponents baseURIForLinkTo = ServletUriComponentsBuilder.fromCurrentContextPath().port(System.getProperty("connection.httpsPort"))
                .path(requestMappingPath).build();
        return baseURIForLinkTo;
    }

    @GetMapping(value = "", produces = {"application/json"})
    @Operation(summary = "Get a list of a directories contents", operationId = "getDirectoryListing", description = "This API gets a list of files and directories for a given path")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Ok")})
    public UnixDirectoryAttributesWithChildren getUnixDirectoryListing(
            @Parameter(description = "Path of Directory to be listed", required = true) @RequestParam String path, HttpServletRequest request) {

        String hypermediaLinkToBase = getLinkToBaseURI(request).toString();
        return getUnixFileService().listUnixDirectory(path, hypermediaLinkToBase);
    }

    @GetMapping(value = "{path}/**", produces = {"application/json"})
    @Operation(summary = "Get the contents of a Unix file", operationId = "getUnixFileContents", description = "This API gets a the contetns of a Unix file. Try it out function will not work due to the encoding of forward slashes, "
            + "it should be noted that requests to this endpoint should only contain unencoded slashes and not include wild card characters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Ok")})
    public ResponseEntity<UnixFileContent> getUnixFileContent(
            @PathVariable String path, HttpServletRequest request,
            @RequestHeader(value = "Convert", required = false) Boolean convert,
            @RequestHeader(value = "X-Return-Etag", required = false) String etagHeader) {

        String fullPath = getPathFromRequest(request);

        boolean decode = false;
        if (convert == null) {
            decode = getUnixFileService().shouldUnixFileConvert(fullPath);
            convert = decode;
        }
        UnixFileContentWithETag content = getUnixFileService().getUnixFileContentWithETag(fullPath, convert, decode);

        HttpHeaders headers = new HttpHeaders();

        if ("true".equalsIgnoreCase(etagHeader)) {
            headers.add("Access-Control-Expose-Headers", "ETag");
            headers.add("ETag", content.getETag());
        }
        return new ResponseEntity<>(content.getContent(), headers, HttpStatus.OK);
    }

    @PutMapping(value = "{path}/**", produces = {"application/json"})
    @Operation(summary = "Update the contents of a Unix file", operationId = "putUnixFileContents", description = "This API will update the contents of a Unix file. Try it out function will not work due to the encoding of forward slashes, "
            + "it should be noted that requests to this endpoint should only contain unencoded slashes and not include wild card characters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Ok")})
    public ResponseEntity<?> putUnixFileContent(
            @PathVariable String path, HttpServletRequest request,
            @RequestBody UnixFileContent input, @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestHeader(value = "Convert", required = false) Boolean convert,
            @RequestHeader(value = "X-Return-Etag", required = false) String etagHeader) {

        UnixFileContentWithETag contentWithETag = new UnixFileContentWithETag(input, ifMatch);
        String fullPath = getPathFromRequest(request);

        // Ensure file already exists
        getUnixFileService().getUnixFileContentWithETag(fullPath, false, false);

        if (convert == null) {
            convert = getUnixFileService().shouldUnixFileConvert(fullPath);
        }
        String putETag = getUnixFileService().putUnixFileContent(fullPath, contentWithETag, convert);

        HttpHeaders headers = new HttpHeaders();
        if ("true".equalsIgnoreCase(etagHeader)) {
            headers.add("Access-Control-Expose-Headers", "ETag");
            headers.add("ETag", "\"" + putETag + "\"");
        }
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "{path}/**", produces = {"application/json"})
    @Operation(summary = "Delete a Unix file", operationId = "deleteUnixFile", description = "This API deletes a Unix file or directory. Try it out function will not work due to the encoding of forward slashes, "
            + "it should be noted that requests to this endpoint should only contain unencoded slashes")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Unix file successfully deleted")})
    public ResponseEntity<?> deleteUnixFile(@PathVariable String path, HttpServletRequest request,
                                            @RequestHeader(value = "recursive", required = false, defaultValue = "false") boolean isRecursive) {
        String fullPath = getPathFromRequest(request);
        getUnixFileService().deleteUnixFileContent(fullPath, isRecursive);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "{path}/**", produces = {"application/json"})
    @Operation(summary = "Create a new Unix File or Diretory", operationId = "postUnixFileOrDirectory", description = "This API will create a new UnixFile or Directory. Try it out function not functional due to encoding of slashes and auto insertion of wildcard characters, "
            + "an example request path would be /api/v1/unixFiles/u/ibmuser/newDirectory")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Created")})
    public ResponseEntity<?> createUnifFileOrDirectory(@PathVariable String path, HttpServletRequest request,
                                                       @RequestBody UnixCreateAssetRequest input) {
        getUnixFileService().createUnixAsset(getPathFromRequest(request), input);
        return ResponseEntity.created(getLinkToBaseURI(request).toUri()).build();
    }
}
