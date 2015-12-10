package hudson.plugins.blazemeter;

import hudson.model.Result;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.ApiVersion;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.entities.CIStatus;
import hudson.plugins.blazemeter.utils.BzmServiceManager;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JsonConstants;
import hudson.util.FormValidation;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONObject;
import org.junit.*;
import org.json.JSONException;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

/**
 * Created by zmicer on 8.7.15.
 */
public class TestBzmServiceManager {

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
        MockedAPI.getTests();
    }

    @AfterClass
    public static void tearDown()throws IOException{
        MockedAPI.stopAPI();
    }

    @Test
    public void getUserEmail_positive() throws IOException,JSONException{
        String email=BzmServiceManager.getUserEmail(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(email, "dzmitry.kashlach@blazemeter.com");
    }

    @Test
    public void getUserEmail_negative() throws IOException,JSONException{
        String email=BzmServiceManager.getUserEmail(TestConstants.MOCKED_USER_KEY_INVALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(email,"");
    }

    @Test
    public void getUserEmail_exception() throws IOException,JSONException{
        String email=BzmServiceManager.getUserEmail(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl);
        Assert.assertEquals(email,"");
    }

    @Test
    public void validateUserKey_positive() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.OK);
        Assert.assertEquals(validation.getMessage(), "API key Valid. Email - dzmitry.kashlach@blazemeter.com");
    }

    @Test
    public void validateUserKey_negative() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey(TestConstants.MOCKED_USER_KEY_INVALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
        Assert.assertEquals(validation.getMessage(),
                "API key is not valid: unexpected exception=JSONObject[\"mail\"] not found.");
    }

    @Test
    public void validateUserKey_exception() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
        Assert.assertEquals(validation.getMessage(),
                "API key is not valid: API key=mock...tion blazemeterUrl=http://127.0.0.1:1234. Please, check manually.");
    }

    @Test
    public void validateUserKey_empty() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey("", TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
        Assert.assertEquals(validation.getMessage(), Constants.API_KEY_EMPTY);
    }

    @Test
    public void getVersion() throws IOException,JSONException{
        String version=BzmServiceManager.getVersion();
        Assert.assertTrue(version.matches("^(\\d{1,}\\.+\\d{1,2}\\S*)$"));
    }

    @Test
    public void stopMaster(){
        BlazemeterApi api = APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID, ApiVersion.v3, TestConstants.mockedApiUrl);
        boolean terminate = BzmServiceManager.stopTestSession(api, TestConstants.TEST_MASTER_25, stdErrLog);
        Assert.assertEquals(terminate, true);
        terminate = BzmServiceManager.stopTestSession(api, TestConstants.TEST_MASTER_70, stdErrLog);
        Assert.assertEquals(terminate, true);
        terminate = BzmServiceManager.stopTestSession(api, TestConstants.TEST_MASTER_100, stdErrLog);
        Assert.assertEquals(terminate, false);
        terminate = BzmServiceManager.stopTestSession(api, TestConstants.TEST_MASTER_140, stdErrLog);
        Assert.assertEquals(terminate, false);
    }

    @Test
    public void autoDetectApiVersion_v2(){
        String apiVersion=BzmServiceManager.autoDetectApiVersion(TestConstants.MOCKED_USER_KEY_V2, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiVersion,"v2");
    }


    @Test
    public void autoDetectApiVersion_v3(){
        String apiVersion=BzmServiceManager.autoDetectApiVersion(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiVersion,"v3");
    }


    @Test
    public void getReportUrl_pos(){
        String expectedReportUrl="http://127.0.0.1:1234/app/?public-token=ohImO6c8xstG4qBFqgRnsMSAluCBambtrqsTvAEYEXItmrCfgO#masters/testMasterId/summary";
        BlazemeterApi api = APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID, ApiVersion.v3, TestConstants.mockedApiUrl);
        String actReportUrl=BzmServiceManager.getReportUrl(api, TestConstants.TEST_MASTER_ID, stdErrLog, stdErrLog);
        Assert.assertEquals(expectedReportUrl,actReportUrl);
    }

    @Test
    public void getReportUrl_neg(){
        String expectedReportUrl="http://127.0.0.1:1234/app/#masters/testMasterId/summary";
        BlazemeterApi api = APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_INVALID, ApiVersion.v3, TestConstants.mockedApiUrl);
        String actReportUrl=BzmServiceManager.getReportUrl(api, TestConstants.TEST_MASTER_ID, stdErrLog, stdErrLog);
        Assert.assertEquals(expectedReportUrl,actReportUrl);
    }

    @Test
    public void updateTestDuration() throws JSONException, IOException {
        BlazemeterApi api = APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID, ApiVersion.v3, TestConstants.mockedApiUrl);
        JSONObject updateTestDuration=BzmServiceManager.updateTestDuration(api, TestConstants.TEST_MASTER_ID, "6", stdErrLog);
        String testDuration=updateTestDuration.getJSONObject(JsonConstants.TEST).getJSONObject(JsonConstants.CONFIGURATION).
                getJSONObject(JsonConstants.PLUGINS).getJSONObject(JsonConstants.HTTP).
                getJSONObject(JsonConstants.OVERRIDE).getString(JsonConstants.DURATION);
        Assert.assertEquals(testDuration,"6");
    }

    @Test
    public void getSessionId_v3() throws JSONException, IOException {
        File getSessionId_v3=new File(TestConstants.RESOURCES+"/getSessionId_v3.json");
        String getSessionId_v3_str=FileUtils.readFileToString(getSessionId_v3);
        JSONObject getSession_json=new JSONObject(getSessionId_v3_str);
        String session=BzmServiceManager.getSessionId(getSession_json, ApiVersion.v3, stdErrLog, stdErrLog);
        Assert.assertEquals(session,"r-v3-55a6136b314bd");
    }

    @Test
    public void getSessionId_v2() throws JSONException, IOException {
        File getSessionId_v2=new File(TestConstants.RESOURCES+"/getSessionId_v2.json");
        String getSessionId_v2_str=FileUtils.readFileToString(getSessionId_v2);
        JSONObject getSession_json=new JSONObject(getSessionId_v2_str);
        String session=BzmServiceManager.getSessionId(getSession_json, ApiVersion.v2, stdErrLog, stdErrLog);
        Assert.assertEquals(session,"r-ec255a6160ec7b39");
    }

    @Test
    public void getSessionId_empty() throws JSONException, IOException {
        File getSessionId_v2=new File(TestConstants.RESOURCES+"/getSessionId_v2_500.json");
        String getSessionId_v2_str=FileUtils.readFileToString(getSessionId_v2);
        JSONObject getSession_json=new JSONObject(getSessionId_v2_str);
        String session=BzmServiceManager.getSessionId(getSession_json,ApiVersion.v2,stdErrLog,stdErrLog);
        Assert.assertEquals(session,"");
    }

    @Test
    public void getCIStatus_success(){
        BlazemeterApi api = APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID, ApiVersion.v3, TestConstants.mockedApiUrl);
        CIStatus ciStatus=BzmServiceManager.validateCIStatus(api, TestConstants.TEST_MASTER_SUCCESS, stdErrLog);
        Assert.assertEquals(CIStatus.success,ciStatus);
    }

    @Test
    public void getCIStatus_failure(){
        BlazemeterApi api = APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID, ApiVersion.v3, TestConstants.mockedApiUrl);
        CIStatus ciStatus=BzmServiceManager.validateCIStatus(api, TestConstants.TEST_MASTER_FAILURE, stdErrLog);
        Assert.assertEquals(CIStatus.failures,ciStatus);
    }

    @Test
    public void getCIStatus_error(){
        BlazemeterApi api = APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID, ApiVersion.v3, TestConstants.mockedApiUrl);
        CIStatus ciStatus=BzmServiceManager.validateCIStatus(api, TestConstants.TEST_MASTER_ERROR, stdErrLog);
        Assert.assertEquals(CIStatus.errors,ciStatus);
    }
}
