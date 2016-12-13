/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package hudson.plugins.blazemeter;

import com.google.common.collect.LinkedHashMultimap;
import hudson.plugins.blazemeter.api.*;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.JsonConsts;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

public class TestApiV3Impl {
    private ApiV3Impl blazemeterApiV3 = null;


    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getMasterStatus();
        MockedAPI.getTests();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
        MockedAPI.active();
        MockedAPI.ping();
        MockedAPI.jtl();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        MockedAPI.stopAPI();
    }



    @Test
    public void getTestStatus_Running() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }


    @Test
    public void getTestInfo_Error() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }

    @Test
    public void getTestInfo_NotFound() {
        blazemeterApiV3 = new ApiV3Impl("",TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.getTestStatus("");
        Assert.assertEquals(testStatus, TestStatus.NotFound);
    }


    @Test
    public void getTestCount_zero() {
        try {
            blazemeterApiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl);
            Assert.assertEquals(blazemeterApiV3.getTestCount(), 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void startTest_single() throws JSONException,IOException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, false).get(JsonConsts.ID),
                "15102806");
    }

    @Test
    public void startTest_collection() throws JSONException,IOException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, true).get(JsonConsts.ID),
                "15105877");
    }


    @Test
    public void getTestRunStatus_notFound() {
        blazemeterApiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApiV3.getTestStatus(null), TestStatus.NotFound);
    }

    @Test
    public void getTestList_4_5() throws IOException, JSONException, ServletException, MessagingException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_5_TESTS, TestConstants.mockedApiUrl);
        LinkedHashMultimap<String, String> testList = blazemeterApiV3.testsMultiMap();
        Assert.assertTrue(testList.asMap().size() == 4);
        Assert.assertTrue(testList.size() == 5);

    }

    @Test
    public void getTestReport() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        JSONObject testReport = blazemeterApiV3.testReport(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(testReport.length() == 33);


    }

    @Test
    public void getJtl() throws JSONException,IOException{
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        JSONObject jtl = blazemeterApiV3.retrieveJtlZip(TestConstants.MOCKED_JTL_SESSION);
        Assert.assertTrue(jtl.length() == 3);
        Assert.assertTrue(((JSONObject)jtl.get(JsonConsts.RESULT)).has(JsonConsts.DATA_URL));


    }

    @Test
    public void getTestsCount_4() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 4);

    }

    @Test
    public void getTestsCount_1() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_1_TEST, TestConstants.mockedApiUrl);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 1);

    }

    @Test
    public void getTestsCount_0() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_0_TESTS, TestConstants.mockedApiUrl);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 0);

    }

    @Test
    public void getTestSessionStatusCode_25() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status == 25);
    }

    @Test
    public void getTestSessionStatusCode_70() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status == 70);
    }

    @Test
    public void getTestSessionStatusCode_140() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_140);
        Assert.assertTrue(status == 140);
    }

    @Test
    public void getTestSessionStatusCode_100() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_100);
        Assert.assertTrue(status == 100);
    }

    @Test
    public void getTestSessionStatusCode_0() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_0);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void active() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        boolean active = blazemeterApiV3.active("5133848");
        Assert.assertTrue(active);
    }

    @Test
    public void activeNot() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        boolean active = blazemeterApiV3.active("51338483");
        Assert.assertFalse(active);
    }


    @Test
    public void ping_true() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        boolean ping = false;
        try {
            ping = blazemeterApiV3.ping();
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertTrue(ping);
    }

    @Test
    public void ping_false() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        boolean ping = false;
        try {
            ping = blazemeterApiV3.ping();
        } catch (Exception e) {
            Assert.assertFalse(ping);
        }
    }
}
