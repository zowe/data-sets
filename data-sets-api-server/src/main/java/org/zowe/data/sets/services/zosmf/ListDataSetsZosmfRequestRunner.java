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
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;
import org.zowe.data.sets.model.AllocationUnitType;
import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetAttributes.DataSetAttributesBuilder;
import org.zowe.data.sets.model.DataSetOrganisationType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ListDataSetsZosmfRequestRunner extends AbstractZosmfDataSetsRequestRunner<List<DataSetAttributes>> {

    private String filter;

    public ListDataSetsZosmfRequestRunner(String filter) {
        this.filter = filter;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        String query = String.format("dslevel=%s", filter);
        URI requestUrl = zosmfConnector.getFullUrl("restfiles/ds", query); // $NON-NLS-1$
        RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
        requestBuilder.addHeader("X-IBM-Attributes", "base");
        return requestBuilder;
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_OK };
    }

    @Override
    protected List<DataSetAttributes> getResult(ResponseCache responseCache) throws IOException {
        JsonObject dataSetsResponse = responseCache.getEntityAsJsonObject();
        JsonElement dataSetJsonArray = dataSetsResponse.get("items");

        List<DataSetAttributes> dataSets = new ArrayList<>();
        for (JsonElement jsonElement : dataSetJsonArray.getAsJsonArray()) {
            try {
                DataSetAttributes dataSet = getDataSetFromJson(jsonElement.getAsJsonObject());
                dataSets.add(dataSet);
            } catch (IllegalArgumentException e) {
                log.error("listDataSets", e);
            }
        }
        return dataSets;
    }

    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        JsonElement details = jsonResponse.get("details");
        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            if (details.toString().contains(AUTHORIZATION_FAILURE)) {
                throw new UnauthorisedDataSetException(filter);
            }
        } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
            if (details.toString().contains(DATA_SET_NOT_FOUND)) {
                throw new DataSetNotFoundException(filter);
            }
        }
        return null;
    }

    private DataSetAttributes getDataSetFromJson(JsonObject returned) {
        DataSetAttributesBuilder builder = DataSetAttributes.builder().catalogName(getStringOrNull(returned, "catnm"))
            .name(getStringOrNull(returned, "dsname")).migrated("YES".equals(getStringOrNull(returned, "migr")))
            .volumeSerial(getStringOrNull(returned, "vols")).blockSize(getIntegerOrNull(returned, "blksz"))
            .deviceType(getStringOrNull(returned, "dev")).expirationDate(getStringOrNull(returned, "edate"))
            .creationDate(getStringOrNull(returned, "cdate")).recordLength(getIntegerOrNull(returned, "lrecl"))
            .recordFormat(getStringOrNull(returned, "recfm")).allocatedSize(getIntegerOrNull(returned, "sizex"))
            .used(getIntegerOrNull(returned, "used"));

        String dsorg = getStringOrNull(returned, "dsorg");
        if (dsorg != null) {
            builder.dataSetOrganization(DataSetOrganisationType.getByZosmfName(dsorg));
        }
        String spacu = getStringOrNull(returned, "spacu");
        if (spacu != null) {
            // SJH : spacu returns a plural string, so strip 's' off the end
            builder.allocationUnit(AllocationUnitType.valueOf(spacu.substring(0, spacu.length() - 1)));
        }
        return builder.build();
    }
}
