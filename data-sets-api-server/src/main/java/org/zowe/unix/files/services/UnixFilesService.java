/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.services;

import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixFileContentWithETag;

public interface UnixFilesService {
    UnixDirectoryAttributesWithChildren listUnixDirectory(String path);
    
    UnixFileContentWithETag getUnixFileContentWithETag(String path, boolean convert);
}
