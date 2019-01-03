/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.NoZosmfResponseEntityException;
import org.zowe.api.common.exceptions.ServerErrorException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.api.common.utils.ResponseUtils;
import org.zowe.data.sets.exceptions.DataSetAlreadyExists;
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;
import org.zowe.data.sets.model.DataSetCreateRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ZosmfDataSetService implements DataSetService {

    private static final String AUTHORIZATION_FAILURE = "ISRZ002 Authorization failed";
    private static final String DATA_SET_NOT_FOUND = "ISRZ002 Data set not cataloged";

    @Autowired
    ZosmfConnector zosmfConnector;

    // TODO - review error handling, serviceability, refactor out error handling?
    // TODO - use the zomsf error categories to work out errors
    // https://www.ibm.com/support/knowledgecenter/SSLTBW_2.3.0/com.ibm.zos.v2r3.izua700/IZUHPINFO_API_RESTFILES_Error_Categories.htm

    @Override
    public List<String> listDataSetMembers(String dataSetName) {
        String urlPath = String.format("restfiles/ds/%s/member", dataSetName);
        String requestUrl = zosmfConnector.getFullUrl(urlPath); // $NON-NLS-1$
        try {
            HttpResponse response = zosmfConnector.request(RequestBuilder.get(requestUrl));
            int statusCode = ResponseUtils.getStatus(response);
            if (statusCode == HttpStatus.SC_OK) {
                List<String> memberNames = new ArrayList<>();
                JsonObject memberResponse = ResponseUtils.getEntityAsJsonObject(response);
                JsonElement memberJsonArray = memberResponse.get("items");
                for (JsonElement jsonElement : memberJsonArray.getAsJsonArray()) {
                    memberNames.add(jsonElement.getAsJsonObject().get("member").getAsString());
                }
                return memberNames;
            } else {
                HttpEntity entity = response.getEntity();
                // TODO - work out how to tidy when brain is sharper
                if (entity != null) {
                    ContentType contentType = ContentType.get(entity);
                    String mimeType = contentType.getMimeType();
                    if (mimeType.equals(ContentType.APPLICATION_JSON.getMimeType())) {
                        JsonObject jsonResponse = ResponseUtils.getEntityAsJsonObject(response);
                        JsonElement details = jsonResponse.get("details");
                        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                            if (details.toString().contains(AUTHORIZATION_FAILURE)) {
                                throw new UnauthorisedDataSetException(dataSetName);
                            }
                        } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                            if (details.toString().contains(DATA_SET_NOT_FOUND)) {
                                throw new DataSetNotFoundException(dataSetName);
                            }
                        }
                        throw new ZoweApiRestException(getSpringHttpStatusFromCode(statusCode), details.toString());
                    } else {
                        throw new ZoweApiRestException(getSpringHttpStatusFromCode(statusCode), entity.toString());
                    }
                } else {
                    throw new NoZosmfResponseEntityException(getSpringHttpStatusFromCode(statusCode), urlPath);
                }
            }
        } catch (IOException e) {
            log.error("listDataSetMembers", e);
            throw new ServerErrorException(e);
        }
    }

    @Override
    public String createDataSet(DataSetCreateRequest input) {
        String dataSetName = input.getName();
        String urlPath = String.format("restfiles/ds/%s", dataSetName);
        String requestUrl = zosmfConnector.getFullUrl(urlPath);
        try {
            JsonObject requestBody = convertIntoZosmfRequestJson(input);

            StringEntity requestEntity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
            RequestBuilder requestBuilder = RequestBuilder.post(requestUrl).setEntity(requestEntity);
            HttpResponse response = zosmfConnector.request(requestBuilder);
            int statusCode = ResponseUtils.getStatus(response);
            if (statusCode == HttpStatus.SC_CREATED) {
                return dataSetName;
            } else {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    ContentType contentType = ContentType.get(entity);
                    String mimeType = contentType.getMimeType();
                    if (mimeType.equals(ContentType.APPLICATION_JSON.getMimeType())) {
                        JsonObject jsonResponse = ResponseUtils.getEntityAsJsonObject(response);
                        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                            String zosmfMessage = jsonResponse.get("message").getAsString();
                            if ("Dynamic allocation Error".equals(zosmfMessage)) {
                                throw new DataSetAlreadyExists(dataSetName);
                            }
                        }
                    }
                    throw new ZoweApiRestException(getSpringHttpStatusFromCode(statusCode), entity.toString());
                }
                throw new NoZosmfResponseEntityException(getSpringHttpStatusFromCode(statusCode), urlPath);
            }
        } catch (IOException e) {
            log.error("createDataSet", e);
            throw new ServerErrorException(e);
        }
    }

    private JsonObject convertIntoZosmfRequestJson(DataSetCreateRequest input) throws IOException {
        JsonObject requestBody = JsonUtils.convertToJsonObject(input);
        // zosmf doesn't have name as a parameter
        requestBody.remove("name");
        // ZOSMF has only limited alcunit support
        switch (input.getAlcunit()) {
        case TRACK:
            requestBody.remove("alcunit");
            requestBody.addProperty("alcunit", "TRK");
            break;
        case CYLINDER:
            requestBody.remove("alcunit");
            requestBody.addProperty("alcunit", "CYL");
            break;
        default:
            throw new IllegalArgumentException(
                    "Creating data sets with a z/OS MF connector only supports allocation unit type of track and cylinder");
        }
        return requestBody;
    }

    // @Override
    // public void deleteDataSet(String dataSetName) {
    // String requestUrl = ZosmfConnector.getFullUrl(String.format("restfiles/ds/%s", dataSetName));
    // try {
    // HttpResponse response = zosmfconnector.request(RequestBuilder.delete(requestUrl));
    // if (ResponseUtils.getStatus(response) == HttpStatus.SC_NO_CONTENT) {
    // return;
    // } else {
    // throw new RuntimeException("Error");
    // // error handling
    // // if (responseJSON.get("details").toString().contains(AUTHORIZATION_FAILURE)) {
    // // throw
    // // createAuthorizationFailureException(responseJSON.get("details").toString());
    // // }
    // // String error =
    // // String.format(Messages.getString("ZOSMFService.ListFailedForDataset"), dsn);
    // // //$NON-NLS-1$
    // // Response errorResponse =
    // // Response.status(response.getStatus()).entity(error).type(MediaType.TEXT_PLAIN)
    // // .build();
    // // throw new WebApplicationException(errorResponse);
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

    // TODO LATER - push up into common once created
    private org.springframework.http.HttpStatus getSpringHttpStatusFromCode(int statusCode) {
        return org.springframework.http.HttpStatus.resolve(statusCode);
    }
}
