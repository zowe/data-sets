package org.zowe.data.sets.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DataSetRenameRequestTest {
    
    @Test
    public void test_DataSetRenameRequest_model() throws Exception {
        String dsn = "TEST.JCL"; 
        
        DataSetRenameRequest req = DataSetRenameRequest.builder().newName(dsn).build();
        assertEquals(dsn, req.getNewName());
    }

}
