/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */

package org.zowe.data.sets.mapper;

import com.google.gson.JsonObject;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.zowe.data.sets.model.DataSet;
import org.zowe.data.sets.model.DataSetAttributes;

@Mapper(uses = FieldMapper.class)
public interface DataSetMapper {
    DataSetMapper INSTANCE = Mappers.getMapper(DataSetMapper.class);

    @Mappings({
            @Mapping(source = "zosObject", target = "name", qualifiedBy = FieldMapper.dsname.class),
            @Mapping(source = "zosObject", target = "migrated", qualifiedBy = FieldMapper.migr.class)
    })
    DataSet zosToDataSetDTO(JsonObject zosObject);

    @Mappings({
            @Mapping(source = "zosObject", target = "name", qualifiedBy = FieldMapper.dsname.class),
            @Mapping(source = "zosObject", target = "volumeSerial", qualifiedBy = FieldMapper.vols.class),
            @Mapping(source = "zosObject", target = "deviceType", qualifiedBy = FieldMapper.dev.class),
            @Mapping(source = "zosObject", target = "dataSetOrganization", qualifiedBy = FieldMapper.dsorg.class),
            @Mapping(source = "zosObject", target = "allocationUnit", qualifiedBy = FieldMapper.spacu.class),
            @Mapping(source = "zosObject", target = "recordFormat", qualifiedBy = FieldMapper.recfm.class),
            @Mapping(source = "zosObject", target = "blockSize", qualifiedBy = FieldMapper.blksz.class),
            @Mapping(source = "zosObject", target = "recordLength", qualifiedBy = FieldMapper.lrecl.class),
            @Mapping(source = "zosObject", target = "allocatedSize", qualifiedBy = FieldMapper.sizex.class),
            @Mapping(source = "zosObject", target = "used", qualifiedBy = FieldMapper.used.class),
            @Mapping(source = "zosObject", target = "migrated", qualifiedBy = FieldMapper.migr.class),
            @Mapping(source = "zosObject", target = "catalogName", qualifiedBy = FieldMapper.catnm.class),
            @Mapping(source = "zosObject", target = "creationDate", qualifiedBy = FieldMapper.cdate.class),
            @Mapping(source = "zosObject", target = "expirationDate", qualifiedBy = FieldMapper.edate.class),
            @Mapping(source = "zosObject", target = "primary", ignore =true),
            @Mapping(source = "zosObject", target = "secondary", ignore =true),
            @Mapping(source = "zosObject", target = "directoryBlocks", ignore =true),
            @Mapping(source = "zosObject", target = "averageBlock", ignore =true)
    })
    DataSetAttributes zosToDataSetAttributesDTO(JsonObject zosObject);
}