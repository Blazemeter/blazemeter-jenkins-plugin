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

import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.ApiV3Impl;
import hudson.plugins.blazemeter.entities.CIStatus;
import hudson.plugins.blazemeter.utils.JobUtility;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.util.FormValidation;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.json.JSONException;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

public class TestJobUtility {

    private static StdErrLog stdErrLog= Mockito.mock(StdErrLog.class);


    @BeforeClass
    public static void setUp()throws IOException{
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.stopTestSession();
        MockedAPI.getMasterStatus();
        MockedAPI.getCIStatus();
        MockedAPI.autoDetectVersion();
        MockedAPI.getReportUrl();
        MockedAPI.getTestConfig();
        MockedAPI.putTestInfo();
    }

    @AfterClass
    public static void tearDown()throws IOException{
        MockedAPI.stopAPI();
    }

    @Test
    public void getUserEmail_positive() throws IOException,JSONException{
        String email= JobUtility.getUserEmail(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(email, "dzmitry.kashlach@blazemeter.com");
    }

    @Test
    public void getUserEmail_negative() throws IOException,JSONException{
        String email= JobUtility.getUserEmail(TestConstants.MOCKED_USER_KEY_INVALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(email,"");
    }

    @Test
    public void getUserEmail_exception() throws IOException,JSONException{
        String email= JobUtility.getUserEmail(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl);
        Assert.assertEquals(email,"");
    }

    @Test
    public void validateUserKey_positive() throws IOException,JSONException{
        FormValidation validation= JobUtility.validateUserKey(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.OK);
        Assert.assertEquals(validation.getMessage(), Constants.API_KEY_VALID+"dzmitry.kashlach@blazemeter.com");
    }

    @Test
    public void validateUserKey_negative() throws IOException,JSONException{
        FormValidation validation= JobUtility.validateUserKey(TestConstants.MOCKED_USER_KEY_INVALID,
                TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
        Assert.assertEquals(validation.getMessage(),
                "API key is not valid: unexpected exception=JSONObject[\"mail\"] not found.");
    }

    @Test
    public void validateUserKey_exception() throws IOException,JSONException{
        FormValidation validation= JobUtility.validateUserKey(TestConstants.MOCKED_USER_KEY_EXCEPTION,
                TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
        Assert.assertEquals(validation.getMessage(),
                "API key is not valid: API key=moc... blazemeterUrl="+TestConstants.mockedApiUrl+". Please, check proxy settings, serverUrl and userKey.");
    }

    @Test
    public void validateUserKey_empty() throws IOException,JSONException{
        FormValidation validation= JobUtility.validateUserKey("", TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
        Assert.assertEquals(validation.getMessage(), Constants.API_KEY_EMPTY);
    }

    @Test
    public void getVersion() throws IOException,JSONException{
        String version= JobUtility.version();
        Assert.assertTrue(version.matches("^(\\d{1,}\\.+\\d{1,2}\\S*)$"));
    }

    @Test
    public void stopMaster(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        boolean terminate = JobUtility.stopTestSession(api, TestConstants.TEST_MASTER_25, stdErrLog);
        Assert.assertEquals(terminate, true);
        terminate = JobUtility.stopTestSession(api, TestConstants.TEST_MASTER_70, stdErrLog);
        Assert.assertEquals(terminate, true);
        terminate = JobUtility.stopTestSession(api, TestConstants.TEST_MASTER_100, stdErrLog);
        Assert.assertEquals(terminate, false);
        terminate = JobUtility.stopTestSession(api, TestConstants.TEST_MASTER_140, stdErrLog);
        Assert.assertEquals(terminate, false);
    }

    @Test
    public void getReportUrl_pos(){
        String expectedReportUrl=TestConstants.mockedApiUrl+"/app/?public-token=ohImO6c8xstG4qBFqgRnsMSAluCBambtrqsTvAEYEXItmrCfgO#masters/testMasterId/summary";
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        String actReportUrl= null;
        try {
            actReportUrl = JobUtility.getReportUrl(api, TestConstants.TEST_MASTER_ID, stdErrLog);
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertEquals(expectedReportUrl,actReportUrl);
    }

    @Test
    public void getReportUrl_neg(){
        String expectedReportUrl=TestConstants.mockedApiUrl+"/app/#masters/testMasterId/summary";
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_INVALID, TestConstants.mockedApiUrl);
        String actReportUrl= null;
        try {
            actReportUrl = JobUtility.getReportUrl(api, TestConstants.TEST_MASTER_ID, stdErrLog);
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertEquals(expectedReportUrl,actReportUrl);
    }

    @Test
    public void getSessionId() throws JSONException, IOException {
        File getSessionId_v3=new File(TestConstants.RESOURCES+"/getSessionId_v3.json");
        String getSessionId_v3_str=FileUtils.readFileToString(getSessionId_v3);
        JSONObject getSession_json=new JSONObject(getSessionId_v3_str);
        String session= JobUtility.getSessionId(getSession_json, stdErrLog);
        Assert.assertEquals(session,"r-v3-55a6136b314bd");
    }

    @Test
    public void getCIStatus_success(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        CIStatus ciStatus= JobUtility.validateCIStatus(api, TestConstants.TEST_MASTER_SUCCESS, stdErrLog, stdErrLog);
        Assert.assertEquals(CIStatus.success,ciStatus);
    }

    @Test
    public void getCIStatus_failure(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        CIStatus ciStatus= JobUtility.validateCIStatus(api, TestConstants.TEST_MASTER_FAILURE, stdErrLog, stdErrLog);
        Assert.assertEquals(CIStatus.failures,ciStatus);
    }

    @Test
    public void getCIStatus_error_61700(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        CIStatus ciStatus= JobUtility.validateCIStatus(api, TestConstants.TEST_MASTER_ERROR_61700, stdErrLog, stdErrLog);
        Assert.assertEquals(CIStatus.errors,ciStatus);
    }

    @Test
    public void getCIStatus_error_0(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        CIStatus ciStatus= JobUtility.validateCIStatus(api, TestConstants.TEST_MASTER_ERROR_0, stdErrLog, stdErrLog);
        Assert.assertEquals(CIStatus.failures,ciStatus);
    }

    @Test
    public void getCIStatus_error_70404(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        CIStatus ciStatus= JobUtility.validateCIStatus(api, TestConstants.TEST_MASTER_ERROR_70404, stdErrLog, stdErrLog);
        Assert.assertEquals(CIStatus.failures,ciStatus);
    }

    @Test
    public void errorsFailed_true_0() throws JSONException, IOException {
        File error_0=new File(TestConstants.RESOURCES+ "/ciStatus_error_0.json");
        String error_0_str=FileUtils.readFileToString(error_0);
        JSONArray error_0_json=new JSONArray(error_0_str);
        Assert.assertTrue(JobUtility.errorsFailed(error_0_json));
    }

    @Test
    public void errorsFailed_true_70404() throws JSONException, IOException {
        File error=new File(TestConstants.RESOURCES+ "/ciStatus_error_70404.json");
        String error_str=FileUtils.readFileToString(error);
        JSONArray error_json=new JSONArray(error_str);
        Assert.assertTrue(JobUtility.errorsFailed(error_json));
    }

    @Test
    public void errorsFailed_false_61700() throws JSONException, IOException {
        File error=new File(TestConstants.RESOURCES+ "/ciStatus_error_61700.json");
        String error_str=FileUtils.readFileToString(error);
        JSONArray error_json=new JSONArray(error_str);
        Assert.assertFalse(JobUtility.errorsFailed(error_json));
    }

}
