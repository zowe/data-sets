package org.zowe.data.sets.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataSetContentWithEtag {

    DataSetContent content;
    String etag;
}
