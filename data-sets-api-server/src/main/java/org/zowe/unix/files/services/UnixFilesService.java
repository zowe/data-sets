package org.zowe.unix.files.services;

import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixFileContent;

public interface UnixFilesService {
    UnixDirectoryAttributesWithChildren listUnixDirectory(String path);
    
    UnixFileContent getUnixFileContent(String path);
}
