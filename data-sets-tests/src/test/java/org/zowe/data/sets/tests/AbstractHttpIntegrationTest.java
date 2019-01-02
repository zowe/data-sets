package org.zowe.data.sets.tests;

import static io.restassured.RestAssured.preemptive;

import io.restassured.RestAssured;

import org.junit.BeforeClass;

//TODO NOW - refactor with Jobs
public abstract class AbstractHttpIntegrationTest {

    private final static String SERVER_HOST = System.getProperty("server.host");
    private final static String SERVER_PORT = System.getProperty("server.port");

    protected final static String BASE_URL = "https://" + SERVER_HOST + ":" + SERVER_PORT + "/api/v1/";

    protected final static String USER = System.getProperty("server.username");
    private final static String PASSWORD = System.getProperty("server.password");

    static final String JOB_IEFBR14 = "IEFBR14";
    static final String JOB_WITH_STEPS = "JOB1DD";

    @BeforeClass
    public static void setUpConnection() {
        RestAssured.port = Integer.valueOf(SERVER_PORT);
        RestAssured.baseURI = BASE_URL;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.authentication = preemptive().basic(USER, PASSWORD);
    }

}
