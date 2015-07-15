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

    }

    public static void getSessionStatus() throws IOException{


        File jsonFile = new File(TestConstants.RESOURCES + "/sessionStatus_25.json");
        String testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_25)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/sessionStatus_70.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_70)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/sessionStatus_0.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_0)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_EXCEPTION)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/sessionStatus_100.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_100)
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(testStatus));


        jsonFile = new File(TestConstants.RESOURCES + "/sessionStatus_140.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_140)
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
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_NOT_FOUND)
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
                        .withMethod("GET")
                        .withPath("/api/latest/tests/"+TestConstants.TEST_SESSION_ID +"/terminate")
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
                        .withMethod("GET")
                        .withPath("/api/latest/tests/"+TestConstants.TEST_SESSION_ID +"/stop")
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
                        .withPath("/api/latest/tests/"+TestConstants.TEST_SESSION_ID +"/start")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(startTest));
    }

    public static void getTests() throws IOException{

        File jsonFile = new File(TestConstants.RESOURCES + "/getTests_10.json");
        String getTests= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/tests")
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
                        .withPath("/api/latest/tests")
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
                        .withPath("/api/latest/tests")
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
                        .withPath("/api/latest/tests")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_6_TESTS)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getTests));



    }

    public static void getTestReport()  throws IOException{
        File jsonFile = new File(TestConstants.RESOURCES + "/getTestReport.json");
        String getTestReport= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_ID +"/reports/main/summary")
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

    public static void getServerThresholds()  throws IOException{
        File jsonFile = new File(TestConstants.RESOURCES + "/serverThresholds_negative.json");
        String getServerThresholds= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_FAILURE +"/reports/thresholds")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getServerThresholds));


        jsonFile = new File(TestConstants.RESOURCES + "/serverThresholds_positive.json");
        getServerThresholds= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_SUCCESS +"/reports/thresholds")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(getServerThresholds));

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
                        .withPath("/api/latest/tests/"+TestConstants.TEST_SESSION_ID)
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
                        .withPath("/api/latest/tests/"+TestConstants.TEST_SESSION_ID)
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
        // https://a.blazemeter.com/api/latest/sessions/r-v3-55a4eaedbb17f/publicToken
        File jsonFile = new File(TestConstants.RESOURCES + "/getReportUrl_pos.json");
        String getReportUrl= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_ID+"/publicToken")
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
                        .withPath("/api/latest/sessions/"+TestConstants.TEST_SESSION_ID+"/publicToken")
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

    public static void createTest() throws IOException{
        File jsonFile = new File(TestConstants.RESOURCES + "/createTest.json");
        String createTest= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/tests/custom")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_VALID),
                                new Parameter("custom_test_type", TestConstants.YAHOO)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(createTest));

        jsonFile = new File(TestConstants.RESOURCES + "/not_found.json");
        createTest= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/tests/custom")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_INVALID),
                                new Parameter("custom_test_type", TestConstants.YAHOO)
                        ),
                unlimited()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(createTest));

    }


    public static void stopAPI(){
        mockServer.reset();
        mockServer.stop();
    }
}
