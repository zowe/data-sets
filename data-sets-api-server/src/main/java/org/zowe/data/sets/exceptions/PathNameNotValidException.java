package org.zowe.data.sets.exceptions;

import org.springframework.http.HttpStatus;
import org.zowe.api.common.exceptions.ZoweApiRestException;

public class PathNameNotValidException  extends ZoweApiRestException{
    
    /**
     * 
     */
    private static final long serialVersionUID = -2079479272866830096L;

    public PathNameNotValidException(String path) {
        super(HttpStatus.BAD_REQUEST, "Requested path ''{0}'' is not valid", path);
    }

}
