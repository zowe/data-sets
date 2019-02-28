package org.zowe.unix.files.exceptions;

import org.springframework.http.HttpStatus;
import org.zowe.api.common.exceptions.ZoweApiRestException;

public class UnauthorisedFileException extends ZoweApiRestException {
    
    /**
     * 
     */
    private static final long serialVersionUID = 6715020740317057740L;

    public UnauthorisedFileException(String path) {
        super(HttpStatus.FORBIDDEN, "You are not authorised to access file ''{0}''", path);
    }

}
