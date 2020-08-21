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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.zosmf.services.AbstractZosmfRequestRunner;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;

import java.util.List;

public abstract class AbstractZosmfUnixFilesRequestRunner<T> extends AbstractZosmfRequestRunner<T> {
    
    public AbstractZosmfUnixFilesRequestRunner(List<Header> headers) {
        super(headers);
    }

    protected ZoweApiRestException createUnixFileException(JsonObject jsonResponse, int statusCode, String path) {
        JsonElement details = jsonResponse.get("details");
        if (null != details) {
            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                if (details.toString().contains("EDC5111I Permission denied.")) {
                    throw new UnauthorisedFileException(path);
                }
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                if (details.toString().contains("EDC5129I No such file or directory.")) {
                    throw new FileNotFoundException(path);
                }
            }
        }
        return null;
    }
}
