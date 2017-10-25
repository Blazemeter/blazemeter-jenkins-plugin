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

import hudson.EnvVars;
import hudson.plugins.blazemeter.api.ApiImpl;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.JobUtility;
import hudson.plugins.blazemeter.utils.JsonConsts;
import java.io.IOException;
import java.util.List;
import okhttp3.Credentials;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class TestApiImpl {
    private ApiImpl blazemeterApiV3;
    private static final StdErrLog stdErrLog = Mockito.mock(StdErrLog.class);

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
        MockedAPI.projectId();
        MockedAPI.workspaceId();

    }

    @AfterClass
    public static void tearDown() {
        MockedAPI.stopAPI();
    }

    @Test
    public void startTest_single() throws JSONException, IOException {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR,TestConstants.mockedApiUrl,false);
        Assert.assertEquals(this.blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, false).get(JsonConsts.ID),
            "15102806");
    }

    @Test
    public void startTest_collection() throws JSONException, IOException {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR,TestConstants.mockedApiUrl,false);
        Assert.assertEquals(this.blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, true).get(JsonConsts.ID),
            "15105877");
    }

    @Test
    public void getTestReport() {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        JSONObject testReport = this.blazemeterApiV3.testReport(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(testReport.length() == 33);
    }

    @Test
    public void notes() throws Exception {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        boolean notes = this.blazemeterApiV3.notes(TestConstants.MOCKED_NOTE, TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(notes);
    }

    @Test
    public void properties_true() throws Exception {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        String prps = "v=r,v=i";
        JSONArray arr = JobUtility.prepareSessionProperties(prps, new EnvVars(), TestApiImpl.stdErrLog);
        boolean properties = this.blazemeterApiV3.properties(arr, TestConstants.MOCKED_SESSION);
        Assert.assertTrue(properties);
    }

    @Test
    public void junit() throws IOException {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        String xml = this.blazemeterApiV3.retrieveJUNITXML(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(xml.length() == 784);
    }

    @Test
    public void getJtl() throws JSONException, IOException {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        JSONObject jtl = this.blazemeterApiV3.retrieveJtlZip(TestConstants.MOCKED_SESSION);
        Assert.assertTrue(jtl.length() == 3);
        Assert.assertTrue(((JSONObject) jtl.get(JsonConsts.RESULT)).has(JsonConsts.DATA_URL));

    }

    @Test
    public void getListOfSessionIds() throws IOException, JSONException {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        List<String> ls = this.blazemeterApiV3.getListOfSessionIds(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(ls.size() == 1);
    }

    @Test
    public void publicToken() throws IOException, JSONException {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        JSONObject jo = this.blazemeterApiV3.generatePublicToken(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(jo.length() == 3);
        Assert.assertTrue(jo.getJSONObject(JsonConsts.RESULT).has(JsonConsts.PUBLIC_TOKEN));
    }

    @Test
    public void getTestSessionStatusCode_0() {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_EXCEPTION_CR, TestConstants.mockedApiUrl,false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_0);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void getTestSessionStatusCode_25() {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status == 25);
    }


    @Test
    public void getTestSessionStatusCode_70() {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status == 70);
    }

    @Test
    public void getTestSessionStatusCode_140() {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_140);
        Assert.assertTrue(status == 140);
    }

    @Test
    public void getTestSessionStatusCode_100() {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        int status = this.blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_100);
        Assert.assertTrue(status == 100);
    }

    @Test
    public void getTestStatus_Running() {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        TestStatus testStatus = this.blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        TestStatus testStatus = this.blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }

    @Test
    public void getTestInfo_Error() {
        String bc = Credentials.basic(TestConstants.MOCK_VALID_USER, TestConstants.MOCK_VALID_PASSWORD);
        this.blazemeterApiV3 = new ApiImpl(bc, TestConstants.mockedApiUrl,false);
        TestStatus testStatus = this.blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }

    @Test
    public void projectId() throws Exception{
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        int id = this.blazemeterApiV3.projectId(TestConstants.TEST_ID);
        Assert.assertTrue(id == 11715);

    }
    @Test
    public void workspaceId() throws Exception{
        this.blazemeterApiV3 = new ApiImpl(TestConstants.MOCK_VALID_CR, TestConstants.mockedApiUrl,false);
        int id = this.blazemeterApiV3.workspaceId(TestConstants.TEST_PROJECT_ID);
        Assert.assertTrue(id == 784);
    }
}
