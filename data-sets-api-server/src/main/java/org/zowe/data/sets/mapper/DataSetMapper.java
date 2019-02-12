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
    @Mapping(source = "zosObject", target = "name", qualifiedBy = FieldMapper.dsname.class ),
    @Mapping(source = "zosObject", target = "migrated", qualifiedBy = FieldMapper.migr.class )
    })
    DataSet zosToDataSetDTO(JsonObject zosObject);
    @Mappings({
            @Mapping(source = "zosObject", target = "name", qualifiedBy = FieldMapper.dsname.class ),
            @Mapping(source = "zosObject", target = "migrated", qualifiedBy = FieldMapper.migr.class )
    })
    DataSetAttributes zosToDataSetAttributesDTO(JsonObject zosObject);
}