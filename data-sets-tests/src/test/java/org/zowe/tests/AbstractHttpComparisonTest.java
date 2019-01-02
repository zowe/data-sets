/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2016, 2018
 */
package org.zowe.tests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.zowe.api.common.utils.JsonUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public abstract class AbstractHttpComparisonTest {

    // TODO LATER - fix to use RestAssured

    private final static String SERVER_HOST = System.getProperty("server.host");
    private final static String SERVER_PORT = System.getProperty("server.port");

    protected final static String BASE_URL = "https://" + SERVER_HOST + ":" + SERVER_PORT + "/api/v1/";

    protected final static String USER = System.getProperty("server.username");
    private final static String PASSWORD = System.getProperty("server.password");

    public static final String EOL = System.getProperty("line.separator");

    @BeforeClass
    public static void setupClass() throws Exception {
        assertEquals("We are able to login in authenticated (if not check your password, so stop it getting revoked)",
                HttpStatus.SC_OK, sendGetRequest("jobs").getStatusLine().getStatusCode());
    }

    /**
     * Construct and send an HTTP request to baseAtlasURI+relativeURI, and compare
     * the return code and returned response body to expected values
     *
     * @param relativeURI            the relative URL path, which will be appended
     *                               to the base web application URL
     * @param HTTPmethodType         GET, POST, PUT, DELETE, etc
     * @param expectedResultFilePath the path to the file containing the expected
     *                               response body content. May be null, in which
     *                               case verification of the response body will be
     *                               skipped
     * @param expectedReturnCode     the expected HTTP return code for the request.
     *                               May be zero or negative, in which case
     *                               verification of the return code and response
     *                               body will be skipped
     */
    public void runAndVerifyHTTPRequest(String relativeURI, String HTTPmethodType, String expectedResultFilePath,
            int expectedReturnCode) {
        runAndVerifyHTTPRequest(relativeURI, HTTPmethodType, expectedResultFilePath, expectedReturnCode, null, false,
                false, null);
    }

    /**
     * Construct and send an HTTP request to baseAtlasURI+relativeURI, and compare
     * the return code and returned response body to expected values
     *
     * @param relativeURI            the relative URL path, which will be appended
     *                               to the base web application URL
     * @param HTTPmethodType         GET, POST, PUT, DELETE, etc
     * @param expectedResultFilePath the path to the file containing the expected
     *                               response body content. May be null, in which
     *                               case verification of the response body will be
     *                               skipped
     * @param expectedReturnCode     the expected HTTP return code for the request.
     *                               May be zero or negative, in which case
     *                               verification of the return code and response
     *                               body will be skipped
     */
    public void runAndVerifyHTTPRequest(String relativeURI, String HTTPmethodType, String expectedResultFilePath,
            int expectedReturnCode, Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex) {
        runAndVerifyHTTPRequest(relativeURI, HTTPmethodType, expectedResultFilePath, expectedReturnCode,
                substitutionVars, treatExpectedValueAsRegex, false, null);
    }

    /**
     * Construct and send an HTTP request to baseAtlasURI+relativeURI, and compare
     * the return code and returned response body to expected values
     *
     * @param relativeURI            the relative URL path, which will be appended
     *                               to the base web application URL
     * @param HTTPmethodType         GET, POST, PUT, DELETE, etc
     * @param expectedResultFilePath the path to the file containing the expected
     *                               response body content. May be null, in which
     *                               case verification of the response body will be
     *                               skipped
     * @param expectedReturnCode     the expected HTTP return code for the request.
     *                               May be zero or negative, in which case
     *                               verification of the return code and response
     *                               body will be skipped
     */
    private void runAndVerifyHTTPRequest(String relativeURI, String HTTPmethodType, String expectedResultFilePath,
            int expectedReturnCode, Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex,
            StringEntity jsonContent) {
        runAndVerifyHTTPRequest(relativeURI, HTTPmethodType, expectedResultFilePath, expectedReturnCode,
                substitutionVars, treatExpectedValueAsRegex, false, jsonContent);
    }

    /**
     * Construct and send an HTTP request to baseAtlasURI+relativeURI, and compare
     * the return code and returned response body to expected values
     *
     * @param relativeURI               the relative URL path, which will be
     *                                  appended to the base web application URL
     * @param HTTPmethodType            GET, POST, PUT, DELETE, etc
     * @param expectedResultFilePath    the path to the file containing the expected
     *                                  response body content. May be null, in which
     *                                  case verification of the response body will
     *                                  be skipped
     * @param expectedReturnCode        the expected HTTP return code for the
     *                                  request. May be zero or negative, in which
     *                                  case verification of the return code and
     *                                  response body will be skipped
     * @param substitutionVars          a map of variableName : variableValue, which
     *                                  will be used to substitute text into the
     *                                  expected result file before comparison with
     *                                  the actual result
     * @param treatExpectedValueAsRegex treat the expectedValue (the string value of
     *                                  a JSON object) as a regex when comparing to
     *                                  the actual result. Variable substitutions
     *                                  (if specified) are made before the regex
     *                                  compare
     * @param allowUnorderedJSONArrays  when comparing JSON arrays between
     *                                  expected/actual results, do not fail if the
     *                                  array elements are otherwise the same except
     *                                  for array order
     */
    private void runAndVerifyHTTPRequest(String relativeURI, String HTTPmethodType, String expectedResultFilePath,
            int expectedReturnCode, Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex,
            boolean allowUnorderedJSONArrays, StringEntity jsonContent) {
        try {
            System.out.println("\tRunning test: " + getClass().getSimpleName() + ". Expected Result File: "
                    + expectedResultFilePath + ". Using: " + HTTPmethodType + " for URI path: " + relativeURI);

            HttpResponse resp = null;
            switch (HTTPmethodType) {
            case HttpGet.METHOD_NAME:
                resp = sendGetRequest(relativeURI);
                break;
            case HttpPost.METHOD_NAME:
                resp = sendPostRequest(relativeURI, jsonContent);
                break;
            case HttpPut.METHOD_NAME:
                resp = sendPutRequest(relativeURI, jsonContent);
                break;
            case HttpDelete.METHOD_NAME:
                resp = sendDeleteRequest2(relativeURI);
                break;
            default:
                Assert.fail("Unknown HTTP method: " + HTTPmethodType);
                break;
            }

            if (expectedReturnCode <= 0)
                return; // skip verification of the return code and response body

            int actualReturnCode = resp.getStatusLine().getStatusCode();

            if (actualReturnCode != expectedReturnCode) {
                System.err.println(HTTPmethodType + " request for " + relativeURI + " returned: " + actualReturnCode
                        + ". Expected: " + expectedReturnCode);
                System.err.println("Response = " + resp);
                System.err.println("Body: " + getBodyFromResponse(resp));
            }

            Assert.assertTrue(HTTPmethodType + " request for " + relativeURI + " returned: " + actualReturnCode
                    + ". Expected: " + expectedReturnCode, actualReturnCode == expectedReturnCode);

            if (expectedResultFilePath == null) {
                return; // skip verification of the response body
            }

            String actualResponseBody = getBodyFromResponse(resp);
            String comparisonFailure = compareToExpectedResultsFile(expectedResultFilePath, actualResponseBody,
                    substitutionVars, treatExpectedValueAsRegex, allowUnorderedJSONArrays);

            if (comparisonFailure != null) {
                String failureMsg = "Compare failed in test " + getClass().getSimpleName() + ". Expected Result File: "
                        + expectedResultFilePath + ". URI path: " + relativeURI;
                System.err.println(failureMsg + EOL + comparisonFailure);
                Assert.fail(failureMsg + EOL + comparisonFailure);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception: " + e.getLocalizedMessage() + " in test: " + getClass().getSimpleName()
                    + ". Expected Result File: " + expectedResultFilePath + ". URI path: " + relativeURI);
        }
    }

    // TODO - rename once we move everything over to ITR
    public static IntegrationTestResponse sendGetRequest2(String relativeURI) throws Exception {
        HttpResponse response = buildAndExecuteClientMethod(new HttpGet(), relativeURI);
        return new IntegrationTestResponse(response);
    }

    public static HttpResponse sendGetRequest(String relativeURI) throws Exception {
        HttpGet method = new HttpGet();
        return buildAndExecuteClientMethod(method, relativeURI);
    }

    public static IntegrationTestResponse sendPostRequest(String relativeURI, JsonObject body) throws Exception {
        HttpResponse response = buildAndExecuteClientMethod(new HttpPost(), relativeURI,
                new StringEntity(body.toString()));
        return new IntegrationTestResponse(response);
    }

    public HttpResponse sendPostRequest(String relativeURI, StringEntity jsonContent) throws Exception {
        HttpPost method = new HttpPost();
        return buildAndExecuteClientMethod(method, relativeURI, jsonContent);
    }

    public static IntegrationTestResponse sendPutRequest(String relativeURI, JsonObject body) throws Exception {
        HttpResponse response = buildAndExecuteClientMethod(new HttpPut(), relativeURI,
                new StringEntity(body.toString()));
        return new IntegrationTestResponse(response);
    }

    public HttpResponse sendPutRequest(String relativeURI, StringEntity jsonContent) throws Exception {
        HttpPut method = new HttpPut();
        return buildAndExecuteClientMethod(method, relativeURI, jsonContent);
    }

    // TODO - rename once we move everything over to ITR
    public static IntegrationTestResponse sendDeleteRequest(String relativeURI) throws Exception {
        HttpResponse response = buildAndExecuteClientMethod(new HttpDelete(), relativeURI);
        return new IntegrationTestResponse(response);
    }

    public HttpResponse sendDeleteRequest2(String relativeURI) throws Exception {
        HttpDelete method = new HttpDelete();
        return buildAndExecuteClientMethod(method, relativeURI);
    }

    private static HttpResponse buildAndExecuteClientMethod(HttpRequestBase method, String relativeURI)
            throws Exception {

        // allow self-signed certificates and allow server hostnames that do not match
        // the hostname in the certificate
        HttpClient httpClient = createIgnoreSSLClient();
        HttpClientContext localContext = new HttpClientContext();

        // add user credentials to the request
        ensureAuthenticationCredentials(method, localContext);
        System.out.println(BASE_URL);
        URI uri = new URI(BASE_URL + relativeURI);
        method.setURI(uri);
        Header[] headers = method.getAllHeaders();
        String stringHeaders = "";
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                String headerValue = headers[i].getValue();
                stringHeaders += headers[i].getName() + ": "
                        + (headerValue.length() > 20 ? headerValue.substring(0, 14) + "..." : headerValue) + "; ";
            }
        }
        System.out.println("\tExecuting method: " + method.getClass().getSimpleName() + " for "
                + method.getURI().toString() + (headers.length > 0 ? " headers: " + stringHeaders : ""));
        HttpResponse resp = httpClient.execute(method, localContext);
        return resp;
    }

    private static HttpResponse buildAndExecuteClientMethod(HttpEntityEnclosingRequestBase method, String relativeURI,
            StringEntity jsonContent) throws Exception {

        // allow self-signed certificates and allow server hostnames that do not match
        // the hostname in the certificate
        HttpClient httpClient = createIgnoreSSLClient();
        HttpClientContext localContext = new HttpClientContext();

        // add user credentials to the request
        ensureAuthenticationCredentials(method, localContext);

        System.out.println(BASE_URL);
        URI uri = new URI(BASE_URL + relativeURI);
        method.setURI(uri);
        Header[] headers = method.getAllHeaders();
        String stringHeaders = "";
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                String headerValue = headers[i].getValue();
                stringHeaders += headers[i].getName() + ": "
                        + (headerValue.length() > 20 ? headerValue.substring(0, 14) + "..." : headerValue) + "; ";
            }
        }
        method.setEntity(jsonContent);
        method.setHeader("Content-type", ContentType.APPLICATION_JSON.toString());
        System.out.println("\tExecuting method: " + method.getClass().getSimpleName() + " for "
                + method.getURI().toString() + (headers.length > 0 ? " headers: " + stringHeaders : ""));
        HttpResponse resp = httpClient.execute(method, localContext);
        return resp;
    }

    /**
     * @return A client object for subsequent REST calls to z/OSMF. This method
     *         bypasses the self-signed certificate issue with z/OSMF
     * @throws Exception
     */
    public static HttpClient createIgnoreSSLClient() throws Exception {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(USER, PASSWORD));

        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

        } }, new java.security.SecureRandom());
        return HttpClientBuilder.create().setSSLContext(sslcontext).setDefaultCredentialsProvider(credentialsProvider)
                .setSSLHostnameVerifier(new HostnameVerifier() {

                    @Override
                    public boolean verify(String s1, SSLSession s2) {
                        return true;
                    }

                }).build();
    }

    private static void ensureAuthenticationCredentials(AbstractHttpMessage method, HttpClientContext localContext)
            throws Exception {

        // user credentials can be configured for the context, which sets up the
        // Authorization header automatically
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(USER, PASSWORD));
        localContext.setCredentialsProvider(credentialsProvider);

        // or user credentials can added by constructing the Authorization header
        // directly in the request
        // BASE64Encoder e = new BASE64Encoder();
        // byte[] encodedCredentialsArray = (userName+":"+userPassword).getBytes();
        // String encodedCredentials = e.encode(encodedCredentialsArray);
        // method.setHeader("Authorization", "Basic " + encodedCredentials);

        // or Traveler used different style of authentication with an
        // application-specific authToken in a cookie instead:
        // Header[] cookies = method.getHeaders("Cookie");
        // if(authToken!=null && cookies.length==0) {
        // method.setHeader("Cookie", IAuthConstants.AUTH_COOKIE_RDT+"="+authToken);
        // } //else throw new Exception("Authentication token is not set for request.");
        // -- support unauthenticated requests.
    }

    public static String getBodyFromResponse(HttpResponse resp) throws IOException {
        HttpEntity entity = resp.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    /**
     * @param expectedResultDataPath - a fully or partially qualified path to an
     *                               expected result file
     * @param actualResultsBody      - the string content to compare against the
     *                               expected result file contents
     * @return - a string describing the comparison error, if any. Returns null if
     *         comparison succeeds
     * @throws IOException
     */
    public static String compareToExpectedResultsFile(String expectedResultFileDataPath, String actualResultsBody,
            Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex, boolean allowUnorderedJSONArrays)
            throws IOException {
        // if the expected result file ends with .json, use a JSON-specific comparison
        if (expectedResultFileDataPath.endsWith(".json"))
            return compareToJSONExpectedResultsFile(expectedResultFileDataPath, actualResultsBody, substitutionVars,
                    treatExpectedValueAsRegex, allowUnorderedJSONArrays);

        // otherwise, treat the comparison as plain text
        return compareToTextExpectedResultsFile(expectedResultFileDataPath, actualResultsBody, substitutionVars,
                treatExpectedValueAsRegex);
    }

    /**
     * @param expectedResultDataPath - a fully or partially qualified path to an
     *                               expected result file in JSON format
     * @param actualResultsBody      - the string content to compare against the
     *                               expected result file contents
     * @return - a string describing the comparison error, if any. Returns null if
     *         comparison succeeds
     * @throws IOException
     */
    public static String compareToJSONExpectedResultsFile(String expectedResultFileDataPath, String actualResultsBody,
            Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex, boolean allowUnorderedArrays)
            throws IOException {

        JsonElement jsArtifact1 = JsonUtils.readFileAsJsonElement(Paths.get(expectedResultFileDataPath));
        JsonElement jsArtifact2 = JsonUtils.readAsJsonElement(actualResultsBody);
        String msg = JsonCompare.compare(jsArtifact1, jsArtifact2, substitutionVars, treatExpectedValueAsRegex,
                allowUnorderedArrays);

        if (msg != null) {
            String expectedResultBody = textFromFile(new File(expectedResultFileDataPath));
            String compareFailureMsg = msg + verboseComparisonFailureDetail(expectedResultBody, actualResultsBody);
            return compareFailureMsg;
        } else
            return null;
    }

    /**
     * @param expectedResultDataPath - a fully or partially qualified path to an
     *                               expected result file in text format
     * @param actualResultsBody      - the string content to compare against the
     *                               expected result file contents
     * @return - a string describing the comparison error, if any. Returns null if
     *         comparison succeeds
     * @throws IOException
     */
    public static String compareToTextExpectedResultsFile(String expectedResultFileDataPath, String actualResultsBody,
            Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex) throws IOException {
        String expectedResultBody = textFromFile(new File(expectedResultFileDataPath));

        // replace test-specific substitution variables in the expected result file with
        // supplied values
        if (substitutionVars != null) {
            for (Map.Entry<String, String> entry : substitutionVars.entrySet()) {
                expectedResultBody = String.valueOf(expectedResultBody).replaceAll("\\$\\{" + entry.getKey() + "\\}",
                        entry.getValue());
            }
        }

        // treat the expectedResultValue as a regex and see if the actual result matches
        // or not
        if (treatExpectedValueAsRegex) {
            Pattern pattern = Pattern.compile(expectedResultBody);
            Matcher matcher = pattern.matcher(actualResultsBody);
            return matcher.matches() ? null : verboseComparisonFailureDetail(expectedResultBody, actualResultsBody);
        }

        if (!expectedResultBody.trim().equals(actualResultsBody.trim())) {
            String compareFailureMsg = verboseComparisonFailureDetail(expectedResultBody, actualResultsBody);
            return compareFailureMsg;
        } else
            return null;
    }

    public static String verboseComparisonFailureDetail(String expectedResultBody, String actualResultsBody) {
        return EOL + "---------------------------------------------------------------------------------" + EOL
                + "Comparison failed!  Expected result:" + EOL
                + "---------------------------------------------------------------------------------" + EOL
                + expectedResultBody + EOL
                + "---------------------------------------------------------------------------------" + EOL
                + "Comparison failed!  Actual result:" + EOL
                + "---------------------------------------------------------------------------------" + EOL
                + actualResultsBody + EOL
                + "---------------------------------------------------------------------------------" + EOL;
    }

    public static String textFromFile(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);

        int bytes;
        byte[] buffer = new byte[1024];
        StringBuilder fileContents = new StringBuilder();
        while (-1 != (bytes = inputStream.read(buffer))) {
            String data = new String(buffer, 0, bytes);
            fileContents.append(data);
        }
        inputStream.close();
        return new String(fileContents);
    }
}
