/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.data.sets.services.zosmf;

import com.google.gson.JsonElement;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.methods.RequestBuilder;
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.data.sets.mapper.DataSetMapper;
import org.zowe.data.sets.model.DataSetAttributes;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ListDataSetsAttributesZosmfRequestRunner extends AbstractListDataSetsZosmfRequestRunner<ItemsWrapper<DataSetAttributes>> {

    public ListDataSetsAttributesZosmfRequestRunner(String filter) {
        super(filter);
    }

    @Override
    protected void addHeaders(RequestBuilder builder) {
        builder.addHeader("X-IBM-Attributes", "base");
    }

    @Override
    protected ItemsWrapper<DataSetAttributes> retrieveItems(JsonElement items) {
        List<DataSetAttributes> dataSets = new ArrayList<>();
        for (JsonElement jsonElement : items.getAsJsonArray()) {
            try {
                dataSets.add(DataSetMapper.INSTANCE.zosToDataSetAttributesDTO(jsonElement.getAsJsonObject()));
            } catch (IllegalArgumentException e) {
                log.error("listDataSetAttributes", e);
            }
        }
        return new ItemsWrapper<>(dataSets);
    }

}
