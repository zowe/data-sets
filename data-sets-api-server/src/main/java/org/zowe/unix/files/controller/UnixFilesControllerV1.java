package org.zowe.unix.files.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.unix.files.services.UnixFilesService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/v1/unixfiles")
@Api(value = "Unix Files APIs V1", tags = "Unix Files APIs V1")
public class UnixFilesControllerV1 extends AbstractUnixFilesController {

    @Autowired
    private UnixFilesService unixFilesService; 
    
    @Override
    UnixFilesService getUnixFileService() {
        return unixFilesService;
    }

}
