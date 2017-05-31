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

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.google.common.collect.LinkedHashMultimap;
import hudson.EnvVars;
import hudson.plugins.blazemeter.api.ApiImpl;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.JobUtility;
import hudson.plugins.blazemeter.utils.JsonConsts;
import java.io.IOException;
import java.util.List;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import okhttp3.Credentials;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

public class TestApiImpl {
    private ApiImpl blazemeterApiV3 = null;
    private static StdErrLog stdErrLog = Mockito.mock(StdErrLog.class);

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getMasterStatus();
        MockedAPI.startTest();
        MockedAPI.active();
        MockedAPI.ping();
        MockedAPI.getTestReport();
        MockedAPI.getTests();
        MockedAPI.notes();
        MockedAPI.properties();
        MockedAPI.junit();
        MockedAPI.jtl();
        MockedAPI.getListOfSessionIds();
        MockedAPI.publicToken();

    }

    @AfterClass
    public static void tearDown() {
        MockedAPI.stopAPI();
    }

    @Test
    @Ignore
    public void startTest_single() throws JSONException, IOException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc,
            TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, false).get(JsonConsts.ID),
            "15102806");
    }

    @Test
    @Ignore
    public void startTest_collection() throws JSONException, IOException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());

        blazemeterApiV3 = new ApiImpl(bc,
            TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, true).get(JsonConsts.ID),
            "15105877");
    }

    @Test
    @Ignore
    public void active() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());

        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        boolean active = blazemeterApiV3.active("5133848");
        Assert.assertTrue(active);
    }

    @Test
    @Ignore
    public void activeNot() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());

        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        boolean active = blazemeterApiV3.active("51338483");
        Assert.assertFalse(active);
    }

    @Test
    @Ignore
    public void ping_true() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());

        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        boolean ping = false;
        try {
            ping = blazemeterApiV3.ping();
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertTrue(ping);
    }

    @Test
    @Ignore
    public void ping_false() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());

        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        boolean ping = false;
        try {
            ping = blazemeterApiV3.ping();
        } catch (Exception e) {
            Assert.assertFalse(ping);
        }
    }

    @Test
    @Ignore
    public void getTestReport() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        JSONObject testReport = blazemeterApiV3.testReport(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(testReport.length() == 33);
    }

    @Test
    @Ignore
    public void getTestCount_zero() {
        try {
            String bc = Credentials.basic("", "");

            blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
            Assert.assertEquals(blazemeterApiV3.getTestCount(), -1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void getTestsCount_4() throws IOException, JSONException, ServletException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 4);

    }

    @Test
    @Ignore
    public void notes() throws Exception {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        boolean notes = blazemeterApiV3.notes(TestConstants.MOCKED_NOTE, TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(notes);
    }

    @Test
    @Ignore
    public void properties_true() throws Exception {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        String prps = "v=r,v=i";
        JSONArray arr = JobUtility.prepareSessionProperties(prps, new EnvVars(), stdErrLog);
        boolean properties = blazemeterApiV3.properties(arr, TestConstants.MOCKED_SESSION);
        Assert.assertTrue(properties);
    }

    @Test
    @Ignore
    public void junit() throws IOException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        String xml = blazemeterApiV3.retrieveJUNITXML(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(xml.length() == 784);
    }

    @Test
    @Ignore
    public void getJtl() throws JSONException, IOException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        JSONObject jtl = blazemeterApiV3.retrieveJtlZip(TestConstants.MOCKED_SESSION);
        Assert.assertTrue(jtl.length() == 3);
        Assert.assertTrue(((JSONObject) jtl.get(JsonConsts.RESULT)).has(JsonConsts.DATA_URL));

    }

    @Test
    @Ignore
    public void getListOfSessionIds() throws IOException, JSONException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        List<String> ls = blazemeterApiV3.getListOfSessionIds(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(ls.size() == 1);
    }

    @Test
    public void publicToken() throws IOException, JSONException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        JSONObject jo = blazemeterApiV3.generatePublicToken(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(jo.length() == 3);
        Assert.assertTrue(jo.getJSONObject(JsonConsts.RESULT).has(JsonConsts.PUBLIC_TOKEN));
    }

    @Test
    @Ignore
    public void getTestSessionStatusCode_0() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_EXCEPTION_ID,
            TestConstants.MOCK_EXCEPTION_DESCRIPTION, TestConstants.MOCK_EXCEPTION_USER, TestConstants.MOCK_EXCEPTION_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_0);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void getTestSessionStatusCode_25() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status == 25);
    }

    @Test
    public void getTestsCount_0() throws IOException, JSONException, ServletException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_0_TEST_USER, TestConstants.MOCK_0_TEST_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 0);

    }

    @Test
    public void getTestSessionStatusCode_70() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status == 70);
    }

    @Test
    public void getTestSessionStatusCode_140() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_140);
        Assert.assertTrue(status == 140);
    }

    @Test
    public void getTestSessionStatusCode_100() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_100);
        Assert.assertTrue(status == 100);
    }

    @Test
    public void getTestStatus_Running() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }

    @Test
    public void getTestList_4_5() throws IOException, JSONException, ServletException, MessagingException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_5_TEST_USER, TestConstants.MOCK_5_TEST_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        LinkedHashMultimap<String, String> testList = blazemeterApiV3.testsMultiMap();
        Assert.assertTrue(testList.asMap().size() == 4);
        Assert.assertTrue(testList.size() == 5);

    }

    @Test
    public void getTestInfo_Error() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }
}
