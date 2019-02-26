package org.zowe.data.sets.exceptions;

import org.springframework.http.HttpStatus;
import org.zowe.api.common.exceptions.ZoweApiRestException;

public class UnauthorisedDirectoryException extends ZoweApiRestException{

    /**
     * 
     */
    private static final long serialVersionUID = 5056221873442698156L;

    public UnauthorisedDirectoryException(String path) {
        super(HttpStatus.FORBIDDEN, "You are not authorised to access directory ''{0}''", path);
    }
}
