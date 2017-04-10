/**
 * Copyright 2016 BlazeMeter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.urlmanager.UrlManager;
import java.io.File;
import java.io.IOException;
import okhttp3.Credentials;
import org.apache.commons.io.FileUtils;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.matchers.Times.unlimited;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockedAPI {
    private static ClientAndServer mockServer;

    private MockedAPI() {
    }

    public static void startAPI() {
        mockServer = startClientAndServer(TestConstants.mockedApiPort);
    }

    public static void ping() throws IOException {
        File jsonFile = new File(TestConstants.RESOURCES + "/ping_true.json");
        String ping_true = FileUtils.readFileToString(jsonFile);

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + "/web/version")
                .withHeader("Accept", "application/json"),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(ping_true));

    }

    public static void stopMaster() throws IOException {

        String credential = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);

        File jsonFile = new File(TestConstants.RESOURCES + "/terminateTest.json");
        String terminateTest = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_25 + "/terminate")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(terminateTest));

        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_70 + "/terminate")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(terminateTest));

        jsonFile = new File(TestConstants.RESOURCES + "/stopTest.json");
        String stopTest = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_100 + "/stop")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(stopTest));
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_140 + "/stop")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(stopTest));

    }

    public static void getMasterStatus() throws IOException {

        String credential = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        File jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_25.json");
        String testStatus = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_25 + "/status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(testStatus));
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_15102806 + "/status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            exactly(1)
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(testStatus));


        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_100.json");
        testStatus = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_WAIT_FOR_FINISH + "/status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential), exactly(1)

        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_140.json");
        testStatus = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_140 + "/status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_100_notes.json");
        testStatus = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_100_notes + "/status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/not_found.json");
        testStatus = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_NOT_FOUND + "/status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(testStatus));


        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_100.json");
        testStatus = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_100 + "/status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(testStatus));


        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_70.json");
        testStatus = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_70 + "/status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(testStatus));


        credential = Credentials.basic(TestConstants.MOCK_EXCEPTION_USER, TestConstants.MOCK_EXCEPTION_PASSWORD);
        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_0.json");
        testStatus = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_0 + "/status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION,credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(testStatus));

    }


    public static void startTest() throws IOException {

        String credential = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        File jsonFile = new File(TestConstants.RESOURCES + "/startTest.json");
        String startTest = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(UrlManager.V4 + "/tests/" + TestConstants.TEST_MASTER_ID + "/start")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(startTest));

        jsonFile = new File(TestConstants.RESOURCES + "/startCollection.json");
        String startCollection = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(UrlManager.V4 + "/collections/" + TestConstants.TEST_MASTER_ID + "/start")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(startCollection));

        /*TODO
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(UrlManager.V4 + "/tests/" + TestConstants.TEST_MASTER_ID + "/start")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, TestConstants.MOCKED_USER_KEY_RETRIES),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(""));
    */}


    public static void userProfile() throws IOException {

        String credential = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        File jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_positive.json");
        String userProfile = FileUtils.readFileToString(jsonFile);

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + "/user")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(userProfile));

        credential = Credentials.basic(TestConstants.MOCK_INVALID_USER, TestConstants.MOCK_INVALID_PASSWORD);
        jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_negative.json");
        userProfile = FileUtils.readFileToString(jsonFile);

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + "/user")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(userProfile));

        credential = Credentials.basic(TestConstants.MOCK_EXCEPTION_USER, TestConstants.MOCK_EXCEPTION_PASSWORD);
        jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_jexception.txt");
        userProfile = FileUtils.readFileToString(jsonFile);

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + "/user")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(userProfile));


/* TODO
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + "/user")
                .withHeader("Accept", "application/json")
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_RETRIES),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(""));
*/

    }

    public static void getTestReport() throws IOException {
        String credential = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        File jsonFile = new File(TestConstants.RESOURCES + "/getTestReport.json");
        String getTestReport = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_ID + "/reports/main/summary")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(getTestReport));

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_FAILURE + "/reports/main/summary")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(getTestReport));

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_15102806 + "/reports/main/summary")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(getTestReport));

    }



    public static void getCIStatus() throws IOException {
        String credential = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        File returnFile = new File(TestConstants.RESOURCES + "/getCIStatus_failure.json");
        String returnStr = FileUtils.readFileToString(returnFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_FAILURE + UrlManager.CI_STATUS)
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(returnStr));

        returnFile = new File(TestConstants.RESOURCES + "/getCIStatus_success.json");
        returnStr = FileUtils.readFileToString(returnFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_SUCCESS + "/ci-status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(returnStr));

        returnFile = new File(TestConstants.RESOURCES + "/getCIStatus_error_61700.json");
        returnStr = FileUtils.readFileToString(returnFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_ERROR_61700
                    + UrlManager.CI_STATUS)
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(returnStr));

        returnFile = new File(TestConstants.RESOURCES + "/getCIStatus_error_0.json");
        returnStr = FileUtils.readFileToString(returnFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_ERROR_0 + "/ci-status")
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(returnStr));

        returnFile = new File(TestConstants.RESOURCES + "/getCIStatus_error_70404.json");
        returnStr = FileUtils.readFileToString(returnFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_ERROR_70404 +
                    UrlManager.CI_STATUS)
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(returnStr));

    }



    public static void getReportUrl() throws IOException {
        String credential = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" + TestConstants.TEST_MASTER_ID + "/publicToken";
        File jsonFile = new File(TestConstants.RESOURCES + "/getReportUrl_pos.json");
        String getReportUrl = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(getReportUrl));

        credential = Credentials.basic(TestConstants.MOCK_INVALID_USER, TestConstants.MOCK_INVALID_PASSWORD);
        jsonFile = new File(TestConstants.RESOURCES + "/not_found.json");
        getReportUrl = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(getReportUrl));

    }


    public static void getTests() throws IOException {
        String credential = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String expectedPath = UrlManager.V4+"/web/tests";
        File jsonFile = new File(TestConstants.RESOURCES + "/getTests_10.json");
        String getTests = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader(Api.ACCEPT, Api.APP_JSON)
                .withHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8)
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(getTests));

        credential = Credentials.basic(TestConstants.MOCK_1_TEST_USER, TestConstants.MOCK_1_TEST_PASSWORD);
        jsonFile = new File(TestConstants.RESOURCES + "/getTests_1.json");
        getTests = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(getTests));

        credential = Credentials.basic(TestConstants.MOCK_0_TEST_USER, TestConstants.MOCK_0_TEST_PASSWORD);
        jsonFile = new File(TestConstants.RESOURCES + "/getTests_0.json");
        getTests = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(getTests));

        credential = Credentials.basic(TestConstants.MOCK_5_TEST_USER, TestConstants.MOCK_5_TEST_PASSWORD);
        jsonFile = new File(TestConstants.RESOURCES + "/getTests_5.json");
        getTests = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(getTests));
/*

        File returnFile = new File(TestConstants.RESOURCES + "/getTestType.json");
        String returnStr = FileUtils.readFileToString(returnFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_TEST_TYPE),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(returnStr));

*/
    }


/* TODO

    public static void jtl() throws IOException {
        String expectedPath = UrlManager.V4 + "/" +
            "sessions/" + TestConstants.MOCKED_SESSION + "/reports/logs";

        File jsonFile = new File(TestConstants.RESOURCES + "/jtl.json");
        String jtl = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(jtl));
    }

    public static void jtl_zip() throws IOException {
        String expectedPath = "/users/1689/tests/5283127/reports/r-v3-585114ca535ed/jtls_and_more.zip";

        File jtlBody = new File(TestConstants.RESOURCES + "/jtl.zip");
        InputStream bs = new FileInputStream(jtlBody);
        byte[] bas = IOUtils.toByteArray(bs);
        mockServer.when(
            request()
                .withPath(expectedPath),
            unlimited()
        )
            .respond(
                response().withStatusCode(200).withBody(bas));
    }

    public static void junit() throws IOException {
        String expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" +
            TestConstants.TEST_MASTER_ID + "/reports/thresholds";

        File xmlFile = new File(TestConstants.RESOURCES + "/junit.xml");
        String xml = FileUtils.readFileToString(xmlFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(xml));

        expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" +
            TestConstants.TEST_MASTER_FAILURE + "/reports/thresholds";

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(xml));
    }

    public static void publicToken() throws IOException {
        String expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" +
            TestConstants.TEST_MASTER_ID + "/publicToken";

        File jf = new File(TestConstants.RESOURCES + "/publicToken.json");
        String jo = FileUtils.readFileToString(jf);
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(jo));

        expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" +
            TestConstants.TEST_MASTER_15102806 + "/publicToken";

        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(jo));

    }

    public static void getListOfSessionIds() throws IOException {
        String expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" +
            TestConstants.TEST_MASTER_ID + UrlManager.SESSIONS;

        File jf = new File(TestConstants.RESOURCES + "/listOfsessionIds.json");
        String jo = FileUtils.readFileToString(jf);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(jo));

        expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" +
            TestConstants.TEST_MASTER_SUCCESS + UrlManager.SESSIONS;

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(jo));
    }

    public static void notes() throws IOException {
        String expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" +
            TestConstants.TEST_MASTER_ID;

        File jf = new File(TestConstants.RESOURCES + "/notes.json");
        String jo = FileUtils.readFileToString(jf);
        mockServer.when(
            request()
                .withMethod("PATCH")
                .withPath(expectedPath)
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(jo));

        expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" +
            TestConstants.TEST_MASTER_100_notes;
        mockServer.when(
            request()
                .withMethod("PATCH")
                .withPath(expectedPath)
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(jo));

        expectedPath = UrlManager.V4 + UrlManager.MASTERS + "/" +
            TestConstants.TEST_MASTER_15102806;
        mockServer.when(
            request()
                .withMethod("PATCH")
                .withPath(expectedPath)
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(jo));

    }

    public static void properties() throws IOException {
        String expectedPath = UrlManager.V4 + UrlManager.SESSIONS + "/" +
            TestConstants.MOCKED_SESSION + "/properties";

        File jf = new File(TestConstants.RESOURCES + "/properties.json");
        String jo = FileUtils.readFileToString(jf);
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath(expectedPath)
                .withHeader(Api.X_API_KEY, TestConstants.MOCKED_USER_KEY_VALID)
                .withQueryStringParameters(
                    new Parameter("target", "all")
                ),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(jo));
    }

*/


    public static void active() throws IOException {
        String credential = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);

        String expectedPath = UrlManager.V4 + "/web/active";
        File jsonFile = new File(TestConstants.RESOURCES + "/active.json");
        String active = FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(expectedPath)
                .withHeader("Accept", "application/json")
                .withHeader(Api.AUTHORIZATION, credential),
            unlimited()
        )
            .respond(
                response().withHeader("application/json")
                    .withStatusCode(200).withBody(active));
    }
    public static void stopAPI() {
        mockServer.reset();
        mockServer.stop();
    }

}
