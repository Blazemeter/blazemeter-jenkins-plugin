package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.api.BlazemeterApiV3Impl;
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
    }

    @AfterClass
    public static void tearDown()throws IOException{
        MockedAPI.stopAPI();
    }

    @Test
    public void getUserEmail_positive() throws IOException,JSONException{
        String email=BzmServiceManager.getUserEmail(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        Assert.assertEquals(email, "dzmitry.kashlach@blazemeter.com");
    }

    @Test
    public void getUserEmail_negative() throws IOException,JSONException{
        String email=BzmServiceManager.getUserEmail(TestConstants.MOCKED_USER_KEY_INVALID, TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        Assert.assertEquals(email,"");
    }

    @Test
    public void getUserEmail_exception() throws IOException,JSONException{
        String email=BzmServiceManager.getUserEmail(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        Assert.assertEquals(email,"");
    }

    @Test
    public void validateUserKey_positive() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        Assert.assertEquals(validation.kind, FormValidation.Kind.OK);
        Assert.assertEquals(validation.getMessage(), "API key Valid. Email - dzmitry.kashlach@blazemeter.com");
    }

    @Test
    public void validateUserKey_negative() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey(TestConstants.MOCKED_USER_KEY_INVALID,
                TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
        Assert.assertEquals(validation.getMessage(),
                "API key is not valid: unexpected exception=JSONObject[\"mail\"] not found.");
    }

    @Test
    public void validateUserKey_exception() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey(TestConstants.MOCKED_USER_KEY_EXCEPTION,
                TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
        Assert.assertEquals(validation.getMessage(),
                "API key is not valid: API key=mock...tion blazemeterUrl="+TestConstants.mockedApiUrl+". Please, check manually.");
    }

    @Test
    public void validateUserKey_empty() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey("", TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
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
        BlazemeterApi api = new BlazemeterApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
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
    public void getReportUrl_pos(){
        String expectedReportUrl=TestConstants.mockedApiUrl+"/app/?public-token=ohImO6c8xstG4qBFqgRnsMSAluCBambtrqsTvAEYEXItmrCfgO#masters/testMasterId/summary";
        BlazemeterApi api = new BlazemeterApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        String actReportUrl=BzmServiceManager.getReportUrl(api, TestConstants.TEST_MASTER_ID, stdErrLog, stdErrLog);
        Assert.assertEquals(expectedReportUrl,actReportUrl);
    }

    @Test
    public void getReportUrl_neg(){
        String expectedReportUrl=TestConstants.mockedApiUrl+"/app/#masters/testMasterId/summary";
        BlazemeterApi api = new BlazemeterApiV3Impl(TestConstants.MOCKED_USER_KEY_INVALID, TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        String actReportUrl=BzmServiceManager.getReportUrl(api, TestConstants.TEST_MASTER_ID, stdErrLog, stdErrLog);
        Assert.assertEquals(expectedReportUrl,actReportUrl);
    }

    @Test
    public void getSessionId() throws JSONException, IOException {
        File getSessionId_v3=new File(TestConstants.RESOURCES+"/getSessionId_v3.json");
        String getSessionId_v3_str=FileUtils.readFileToString(getSessionId_v3);
        JSONObject getSession_json=new JSONObject(getSessionId_v3_str);
        String session=BzmServiceManager.getSessionId(getSession_json, stdErrLog, stdErrLog);
        Assert.assertEquals(session,"r-v3-55a6136b314bd");
    }

    @Test
    public void getCIStatus_success(){
        BlazemeterApi api = new BlazemeterApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        CIStatus ciStatus=BzmServiceManager.validateCIStatus(api, TestConstants.TEST_MASTER_SUCCESS, stdErrLog);
        Assert.assertEquals(CIStatus.success,ciStatus);
    }

    @Test
    public void getCIStatus_failure(){
        BlazemeterApi api = new BlazemeterApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        CIStatus ciStatus=BzmServiceManager.validateCIStatus(api, TestConstants.TEST_MASTER_FAILURE, stdErrLog);
        Assert.assertEquals(CIStatus.failures,ciStatus);
    }

    @Test
    public void getCIStatus_error(){
        BlazemeterApi api = new BlazemeterApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                "",TestConstants.proxyPort,"","");
        CIStatus ciStatus=BzmServiceManager.validateCIStatus(api, TestConstants.TEST_MASTER_ERROR, stdErrLog);
        Assert.assertEquals(CIStatus.errors,ciStatus);
    }
}
