package hudson.plugins.blazemeter;

import org.apache.commons.io.FileUtils;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Parameter;

import java.io.File;
import java.io.IOException;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.unlimited;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Created by zmicer on 9.7.15.
 */
public class MockedAPI {
    private static ClientAndServer mockServer;

    private MockedAPI(){}

    public static void startAPI(){
        mockServer = startClientAndServer(TestConstants.mockedApiPort);

    }
    public static void userProfile() throws IOException{



        File jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_positive.json");
        String userProfile= FileUtils.readFileToString(jsonFile);

        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(userProfile));


        jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_negative.json");
        userProfile=FileUtils.readFileToString(jsonFile);

        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_INVALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(userProfile));


        jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_jexception.txt");
        userProfile=FileUtils.readFileToString(jsonFile);

        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_EXCEPTION)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(userProfile));

        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_RETRIES)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(""));


    }

    public static void getMasterStatus() throws IOException{


        File jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_25.json");
        String testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_25 +"/status")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_70.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_70 +"/status")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_0.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_0 +"/status")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_EXCEPTION)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_100.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_100 +"/status")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));


        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_140.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_140 +"/status")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/not_found.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_NOT_FOUND +"/status")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));


    }

    public static void stopTestSession() throws IOException{

        File jsonFile = new File(TestConstants.RESOURCES + "/terminateTest.json");
        String terminateTest= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_25 +"/terminate")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(terminateTest));
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_70 +"/terminate")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(terminateTest));


        jsonFile = new File(TestConstants.RESOURCES + "/stopTest.json");
        String stopTest= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_100 +"/stop")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(stopTest));
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_140 +"/stop")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(stopTest));

    }

    public static void startTest() throws IOException{

        File jsonFile = new File(TestConstants.RESOURCES + "/startTest.json");
        String startTest= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/tests/"+TestConstants.TEST_MASTER_ID +"/start")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(startTest));

        jsonFile = new File(TestConstants.RESOURCES + "/startCollection.json");
        String startCollection= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/collections/"+TestConstants.TEST_MASTER_ID +"/start")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(startCollection));
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/tests/"+TestConstants.TEST_MASTER_ID +"/start")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_RETRIES)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(""));
    }

    public static void getTests() throws IOException{
        String expectedPath="/api/web/tests";
        File jsonFile = new File(TestConstants.RESOURCES + "/getTests_10.json");
        String getTests= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getTests));

        jsonFile = new File(TestConstants.RESOURCES + "/getTests_1.json");
        getTests= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_1_TEST)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getTests));


        jsonFile = new File(TestConstants.RESOURCES + "/getTests_0.json");
        getTests= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_0_TESTS)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getTests));

        jsonFile = new File(TestConstants.RESOURCES + "/getTests_6.json");
        getTests= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_6_TESTS)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getTests));




        File returnFile=new File(TestConstants.RESOURCES+"/getTestType.json");
        String returnStr=FileUtils.readFileToString(returnFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_TEST_TYPE)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(returnStr));

    }

    public static void getTestReport()  throws IOException{
        File jsonFile = new File(TestConstants.RESOURCES + "/getTestReport.json");
        String getTestReport= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_ID +"/reports/main/summary")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getTestReport));

    }



    public static void autoDetectVersion()  throws IOException{
        File jsonFile = new File(TestConstants.RESOURCES + "/autoDetectVersion_v2.json");
        String autoDetectVersion= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_V2)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(autoDetectVersion));
}


    public static void getTestConfig()  throws IOException{
        File jsonFile = new File(TestConstants.RESOURCES + "/getTestConfig.json");
        String getTestConfig= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/tests/"+TestConstants.TEST_MASTER_ID)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getTestConfig));
}

    public static void putTestInfo()  throws IOException{
        File returnFile=new File(TestConstants.RESOURCES+"/updateTestDurationResult.json");
        String returnStr=FileUtils.readFileToString(returnFile);
        mockServer.when(
                request()
                        .withMethod("PUT")
                        .withPath("/api/latest/tests/" + TestConstants.TEST_MASTER_ID)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(returnStr));
    }


    public static void getCIStatus()  throws IOException{
        File returnFile=new File(TestConstants.RESOURCES+"/getCIStatus_failure.json");
        String returnStr=FileUtils.readFileToString(returnFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_FAILURE +"/ci-status")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(returnStr));

        returnFile=new File(TestConstants.RESOURCES+"/getCIStatus_success.json");
        returnStr=FileUtils.readFileToString(returnFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_SUCCESS +"/ci-status")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(returnStr));


        returnFile=new File(TestConstants.RESOURCES+"/getCIStatus_error.json");
        returnStr=FileUtils.readFileToString(returnFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_ERROR +"/ci-status")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(returnStr));

    }


    public static void getReportUrl() throws IOException{
        String expectedPath="/api/latest/masters/"+TestConstants.TEST_MASTER_ID +"/publicToken";
        File jsonFile = new File(TestConstants.RESOURCES + "/getReportUrl_pos.json");
        String getReportUrl= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath(expectedPath)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getReportUrl));

        jsonFile = new File(TestConstants.RESOURCES + "/not_found.json");
        getReportUrl= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath(expectedPath)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_INVALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getReportUrl));


    }


    public static void active() throws IOException{
        String expectedPath="/api/latest/web/active";
        File jsonFile = new File(TestConstants.RESOURCES + "/active.json");
        String active= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(active));
    }


    public static void stopAPI(){
        mockServer.reset();
        mockServer.stop();
    }
}
