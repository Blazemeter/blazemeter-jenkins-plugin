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
    private ApiImpl blazemeterApiV3;
    private static final StdErrLog stdErrLog = Mockito.mock(StdErrLog.class);

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getMasterStatus();
        MockedAPI.startTest();
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
    public void startTest_single() throws JSONException, IOException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc,
            TestConstants.mockedApiUrl,/*TODO*/false);
        Assert.assertEquals(this.blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, false).get(JsonConsts.ID),
            "15102806");
    }

    @Test
    public void startTest_collection() throws JSONException, IOException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());

        this.blazemeterApiV3 = new ApiImpl(bc,
            TestConstants.mockedApiUrl,/*TODO*/false);
        Assert.assertEquals(this.blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, true).get(JsonConsts.ID),
            "15105877");
    }

    @Test
    public void getTestReport() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        JSONObject testReport = this.blazemeterApiV3.testReport(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(testReport.length() == 33);
    }

    @Test
    public void notes() throws Exception {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        boolean notes = this.blazemeterApiV3.notes(TestConstants.MOCKED_NOTE, TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(notes);
    }

    @Test
    public void properties_true() throws Exception {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        String prps = "v=r,v=i";
        JSONArray arr = JobUtility.prepareSessionProperties(prps, new EnvVars(), TestApiImpl.stdErrLog);
        boolean properties = this.blazemeterApiV3.properties(arr, TestConstants.MOCKED_SESSION);
        Assert.assertTrue(properties);
    }

    @Test
    public void junit() throws IOException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        String xml = this.blazemeterApiV3.retrieveJUNITXML(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(xml.length() == 784);
    }

    @Test
    public void getJtl() throws JSONException, IOException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        JSONObject jtl = this.blazemeterApiV3.retrieveJtlZip(TestConstants.MOCKED_SESSION);
        Assert.assertTrue(jtl.length() == 3);
        Assert.assertTrue(((JSONObject) jtl.get(JsonConsts.RESULT)).has(JsonConsts.DATA_URL));

    }

    @Test
    public void getListOfSessionIds() throws IOException, JSONException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        List<String> ls = this.blazemeterApiV3.getListOfSessionIds(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(ls.size() == 1);
    }

    @Test
    public void publicToken() throws IOException, JSONException {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        JSONObject jo = this.blazemeterApiV3.generatePublicToken(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(jo.length() == 3);
        Assert.assertTrue(jo.getJSONObject(JsonConsts.RESULT).has(JsonConsts.PUBLIC_TOKEN));
    }

    @Test
    public void getTestSessionStatusCode_0() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_EXCEPTION_ID,
            TestConstants.MOCK_EXCEPTION_DESCRIPTION, TestConstants.MOCK_EXCEPTION_USER, TestConstants.MOCK_EXCEPTION_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_0);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void getTestSessionStatusCode_25() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status == 25);
    }


    @Test
    public void getTestSessionStatusCode_70() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status == 70);
    }

    @Test
    public void getTestSessionStatusCode_140() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_140);
        Assert.assertTrue(status == 140);
    }

    @Test
    public void getTestSessionStatusCode_100() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_100);
        Assert.assertTrue(status == 100);
    }

    @Test
    public void getTestStatus_Running() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        TestStatus testStatus = this.blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        TestStatus testStatus = this.blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }

    @Test
    public void getTestInfo_Error() {
        BlazemeterCredentialsBAImpl c = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, TestConstants.MOCK_VALID_ID,
            TestConstants.MOCK_VALID_DESCRIPTION, TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        String bc = Credentials.basic(c.getUsername(), c.getPassword().getPlainText());
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,/*TODO*/false);
        TestStatus testStatus = this.blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }

    @Test
    public void projectId(){
        Assert.fail();
    }
    @Test
    public void workspaceId(){
        Assert.fail();
    }
}
