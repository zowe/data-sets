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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.NoZosmfResponseEntityException;
import org.zowe.api.common.exceptions.PreconditionFailedException;
import org.zowe.api.common.exceptions.ServerErrorException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.api.common.utils.ResponseUtils;
import org.zowe.data.sets.exceptions.InvalidDirectoryBlockException;
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;
import org.zowe.data.sets.mapper.DataSetMapper;
import org.zowe.data.sets.model.*;
import org.zowe.data.sets.model.DataSetAttributes.DataSetAttributesBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
        try {
            URI requestUrl = zosmfConnector.getFullUrl(urlPath); // $NON-NLS-1$
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
        } catch (IOException | URISyntaxException e) {
            log.error("listDataSetMembers", e);
            throw new ServerErrorException(e);
        }
    }

    @Override
    public List<DataSetAttributes> listDataSetAttributes(String filter) {
        try {
            String query = String.format("dslevel=%s", filter);
            URI requestUrl = zosmfConnector.getFullUrl("restfiles/ds", query); // $NON-NLS-1$
            RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
            requestBuilder.addHeader("X-IBM-Attributes", "base");
            HttpResponse response = zosmfConnector.request(requestBuilder);
            List<DataSetAttributes> dataSets = new ArrayList<>();
            int statusCode = ResponseUtils.getStatus(response);
            if (statusCode == HttpStatus.SC_OK) {
                JsonObject dataSetsResponse = ResponseUtils.getEntityAsJsonObject(response);
                JsonElement dataSetJsonArray = dataSetsResponse.get("items");
                for (JsonElement jsonElement : dataSetJsonArray.getAsJsonArray()) {
                    try {
                        DataSetAttributes dataSet = getDataSetAttributesFromJson(jsonElement.getAsJsonObject());
                        dataSets.add(dataSet);
                    } catch (IllegalArgumentException e) {
                        log.error("listDataSetAttributes", e);
                    }
                }
                return dataSets;
            } else {
                // TODO - work out how to tidy when brain is sharper
                getHttpError(requestUrl, response, statusCode, response.getEntity());
                return dataSets;
            }
        } catch (IOException | URISyntaxException e) {
            log.error("listDataSetAttributes", e);
            throw new ServerErrorException(e);
        }
    }

    @Override
    public List<DataSet> listDataSet(String filter) {
        try {
            String query = String.format("dslevel=%s", filter);
            URI requestUrl = zosmfConnector.getFullUrl("restfiles/ds", query); // $NON-NLS-1$
            RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
            requestBuilder.addHeader("X-IBM-Attributes", "dsname");
            HttpResponse response = zosmfConnector.request(requestBuilder);
            int statusCode = ResponseUtils.getStatus(response);
            List<DataSet> dataSets = new ArrayList<>();
            if (statusCode == HttpStatus.SC_OK) {
                JsonObject dataSetsResponse = ResponseUtils.getEntityAsJsonObject(response);
                JsonElement dataSetJsonArray = dataSetsResponse.get("items");
                for (JsonElement jsonElement : dataSetJsonArray.getAsJsonArray()) {
                    try {
                        dataSets.add(getDataSetFromJson(jsonElement.getAsJsonObject()));
                    } catch (IllegalArgumentException e) {
                        log.error("listDataSet", e);
                    }
                }
                return dataSets;
            } else {
                // TODO - work out how to tidy when brain is sharper
                getHttpError(requestUrl, response, statusCode, response.getEntity());
                return dataSets;
            }
        } catch (IOException | URISyntaxException e) {
            log.error("listDataSet", e);
            throw new ServerErrorException(e);
        }
    }

    private HttpEntity getHttpError(URI requestUrl, HttpResponse response, int statusCode, HttpEntity entity) throws IOException {
        if (entity != null) {
            ContentType contentType = ContentType.get(entity);
            String mimeType = contentType.getMimeType();
            if (mimeType.equals(ContentType.APPLICATION_JSON.getMimeType())) {
                JsonObject jsonResponse = ResponseUtils.getEntityAsJsonObject(response);
                JsonElement details = jsonResponse.get("details");
                throw new ZoweApiRestException(getSpringHttpStatusFromCode(statusCode), details.toString());
            } else {
                throw new ZoweApiRestException(getSpringHttpStatusFromCode(statusCode), entity.toString());
            }
        } else {
            throw new NoZosmfResponseEntityException(getSpringHttpStatusFromCode(statusCode),
                    requestUrl.toString());
        }
    }

    @Override
    public DataSetContentWithEtag getContent(String dataSetName) {
        String urlPath = String.format("restfiles/ds/%s", dataSetName);
        try {
            URI requestUrl = zosmfConnector.getFullUrl(urlPath); // $NON-NLS-1$
            RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
            requestBuilder.addHeader("X-IBM-Return-Etag", "true");
            HttpResponse response = zosmfConnector.request(requestBuilder);
            int statusCode = ResponseUtils.getStatus(response);
            if (statusCode == HttpStatus.SC_OK) {
                DataSetContent content = new DataSetContent(ResponseUtils.getEntity(response));
                String eTag = null;
                Header etagHeader = response.getFirstHeader("ETag");
                if (etagHeader != null) {
                    eTag = etagHeader.getValue();
                }
                return new DataSetContentWithEtag(content, eTag);
            } else {
                HttpEntity entity = response.getEntity();
                // TODO - work out how to tidy when brain is sharper
                if (entity != null) {
                    ContentType contentType = ContentType.get(entity);
                    String mimeType = contentType.getMimeType();
                    if (mimeType.equals(ContentType.APPLICATION_JSON.getMimeType())) {
                        JsonObject jsonResponse = ResponseUtils.getEntityAsJsonObject(response);
                        String zosmfMessage = jsonResponse.get("message").getAsString();
                        if ("Data set not found.".equals(zosmfMessage)) {
                            throw new DataSetNotFoundException(dataSetName);
                        } else if ("Member not found".equals(zosmfMessage)) {
                            throw new DataSetNotFoundException(dataSetName);
                        }

                        JsonElement details = jsonResponse.get("details");
                        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                            if (details.toString().contains(AUTHORIZATION_FAILURE)) {
                                throw new UnauthorisedDataSetException(dataSetName);
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
        } catch (IOException | URISyntaxException e) {
            log.error("getContent", e);
            throw new ServerErrorException(e);
        }
    }

    @Override
    public String putContent(String dataSetName, DataSetContentWithEtag contentWithEtag) {
        String urlPath = String.format("restfiles/ds/%s", dataSetName);
        try {
            URI requestUrl = zosmfConnector.getFullUrl(urlPath); // $NON-NLS-1$
            DataSetContent content = contentWithEtag.getContent();
            StringEntity requestEntity = new StringEntity(content.getRecords());
            RequestBuilder requestBuilder = RequestBuilder.put(requestUrl).setEntity(requestEntity);
            requestBuilder.addHeader("Content-type", ContentType.TEXT_PLAIN.getMimeType());
            String ifMatch = contentWithEtag.getEtag();
            if (StringUtils.hasText(ifMatch)) {
                requestBuilder.addHeader("If-Match", ifMatch.replaceAll("\"", ""));// zosmf doesn't conform to spec where ifmatch is in double quotes
            }
            HttpResponse response = zosmfConnector.request(requestBuilder);
            int statusCode = ResponseUtils.getStatus(response);
            if (statusCode == HttpStatus.SC_NO_CONTENT || statusCode == HttpStatus.SC_CREATED) {
                Header etagHeader = response.getFirstHeader("ETag");
                return etagHeader.getValue();
            } else if (statusCode == HttpStatus.SC_PRECONDITION_FAILED) {
                throw new PreconditionFailedException(dataSetName);
            } else {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    ContentType contentType = ContentType.get(entity);
                    String mimeType = contentType.getMimeType();
                    if (mimeType.equals(ContentType.APPLICATION_JSON.getMimeType())) {
                        JsonObject jsonResponse = ResponseUtils.getEntityAsJsonObject(response);
                        String zosmfMessage = jsonResponse.get("message").getAsString();
                        if ("Data set not found.".equals(zosmfMessage)) {
                            throw new DataSetNotFoundException(dataSetName);
                        }
                        JsonElement details = jsonResponse.get("details");
                        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                            if (details.toString().contains(AUTHORIZATION_FAILURE)) {
                                throw new UnauthorisedDataSetException(dataSetName);
                            }
                        }
                    }
                    throw new ZoweApiRestException(getSpringHttpStatusFromCode(statusCode), entity.toString());
                }
                throw new NoZosmfResponseEntityException(getSpringHttpStatusFromCode(statusCode), urlPath);
            }
        } catch (IOException | URISyntaxException e) {
            log.error("putContent", e);
            throw new ServerErrorException(e);
        }
    }

    @Override
    public String createDataSet(DataSetCreateRequest input) {
        String dataSetName = input.getName();
        // TODO MAYBE - consider extracting to common validation mechanism
        if (input.getDataSetOrganization() == DataSetOrganisationType.PS && input.getDirectoryBlocks() != null
                && input.getDirectoryBlocks() != 0) {
            throw new InvalidDirectoryBlockException(dataSetName);
        }
        String urlPath = String.format("restfiles/ds/%s", dataSetName);
        try {
            URI requestUrl = zosmfConnector.getFullUrl(urlPath);
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
                        // TODO - work out how to decipher the dynamic allocation error codes
//                        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
//                            String zosmfMessage = jsonResponse.get("message").getAsString();
//                            if ("Dynamic allocation Error".equals(zosmfMessage)
//                                    && jsonResponse.get("rc").getAsInt() == -26868) {
//                                throw new DataSetAlreadyExists(dataSetName);
//                            }
//                        }
                        throw new ZoweApiRestException(getSpringHttpStatusFromCode(statusCode),
                                jsonResponse.toString());
                    }
                    throw new ZoweApiRestException(getSpringHttpStatusFromCode(statusCode), entity.toString());
                }
                throw new NoZosmfResponseEntityException(getSpringHttpStatusFromCode(statusCode), urlPath);
            }
        } catch (IOException | URISyntaxException e) {
            log.error("createDataSet", e);
            throw new ServerErrorException(e);
        }
    }

    private JsonObject convertIntoZosmfRequestJson(DataSetCreateRequest input) throws IOException {
        ZosmfCreateRequest request = ZosmfCreateRequest.createFromDataSetCreateRequest(input);
        return JsonUtils.convertToJsonObject(request);
    }

    @Override
    public void deleteDataSet(String dataSetName) {
        try {
            URI requestUrl = zosmfConnector.getFullUrl(String.format("restfiles/ds/%s", dataSetName));
            HttpResponse response = zosmfConnector.request(RequestBuilder.delete(requestUrl));
            int statusCode = ResponseUtils.getStatus(response);
            if (statusCode == HttpStatus.SC_NO_CONTENT) {
                return;
            } else {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    ContentType contentType = ContentType.get(entity);
                    String mimeType = contentType.getMimeType();
                    if (mimeType.equals(ContentType.APPLICATION_JSON.getMimeType())) {
                        JsonObject jsonResponse = ResponseUtils.getEntityAsJsonObject(response);
                        JsonElement details = jsonResponse.get("details");
                        if (statusCode == HttpStatus.SC_NOT_FOUND) {
                            if (details.toString().contains(String.format(
                                    "ISRZ002 Data set not cataloged - '%s' was not found in catalog.", dataSetName))) {
                                throw new DataSetNotFoundException(dataSetName);
                            }
                        }
                    }
                    throw new ZoweApiRestException(getSpringHttpStatusFromCode(statusCode), entity.toString());
                }
                throw new NoZosmfResponseEntityException(getSpringHttpStatusFromCode(statusCode),
                        requestUrl.toString());
            }
        } catch (IOException | URISyntaxException e) {
            log.error("deleteDataSet", e);
            throw new ServerErrorException(e);
        }
    }

    private static DataSetAttributes getDataSetAttributesFromJson(JsonObject returned) {
        return DataSetMapper.INSTANCE.zosToDataSetAttributesDTO(returned);
    }

    private static DataSet getDataSetFromJson(JsonObject returned) {
        return DataSetMapper.INSTANCE.zosToDataSetDTO(returned);
    }

    // TODO LATER - push up into common
    private org.springframework.http.HttpStatus getSpringHttpStatusFromCode(int statusCode) {
        return org.springframework.http.HttpStatus.resolve(statusCode);
    }
}
