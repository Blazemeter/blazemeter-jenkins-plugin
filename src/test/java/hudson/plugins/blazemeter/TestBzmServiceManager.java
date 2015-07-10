package hudson.plugins.blazemeter;

import hudson.model.Result;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.testresult.TestResult;
import hudson.plugins.blazemeter.utils.BzmServiceManager;
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
        MockedAPI.configure_userProfile();
        MockedAPI.configure_stopTestSession();
    }

    @AfterClass
    public static void tearDown()throws IOException{
        MockedAPI.stopAPI();
    }

    @Test
    public void getUserEmail_positive() throws IOException,JSONException{
        String email=BzmServiceManager.getUserEmail(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(email,"dzmitry.kashlach@blazemeter.com");
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
    }

    @Test
    public void validateUserKey_negative() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey(TestConstants.MOCKED_USER_KEY_INVALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
    }

    @Test
    public void validateUserKey_exception() throws IOException,JSONException{
        FormValidation validation=BzmServiceManager.validateUserKey(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl);
        Assert.assertEquals(validation.kind, FormValidation.Kind.ERROR);
    }

    @Test
    public void getVersion() throws IOException,JSONException{
        String version=BzmServiceManager.getVersion();
        Assert.assertTrue(version.matches("^(\\d{1,}\\.+\\d{1,2}\\S*)$"));
    }

    @Test
    public void validateLocalTr() throws IOException,JSONException{
        File summaryFile = new File(TestConstants.RESOURCES + "/summary.json");
        String summaryStr= FileUtils.readFileToString(summaryFile);
        JSONObject summaryJson=new JSONObject(summaryStr);
        TestResult testResult=new TestResult(summaryJson);
        Result result= null;
        result = BzmServiceManager.validateLocalTresholds(testResult, "1", "", "", "", stdErrLog);
        Assert.assertEquals(result, Result.UNSTABLE);
        result = BzmServiceManager.validateLocalTresholds(testResult, "", "1", "", "", stdErrLog);
        Assert.assertEquals(result, Result.FAILURE);
        result = BzmServiceManager.validateLocalTresholds(testResult, "", "", "1", "", stdErrLog);
        Assert.assertEquals(result,Result.UNSTABLE);
        result=BzmServiceManager.validateLocalTresholds(testResult,"","","","1",stdErrLog);
        Assert.assertEquals(result,Result.FAILURE);
        result=BzmServiceManager.validateLocalTresholds(testResult,"1","2","","",stdErrLog);
        Assert.assertEquals(result,Result.FAILURE);
        result=BzmServiceManager.validateLocalTresholds(testResult,"1","2","3","4",stdErrLog);
        Assert.assertEquals(result,Result.FAILURE);
        result=BzmServiceManager.validateLocalTresholds(testResult,"","","1","2",stdErrLog);
        Assert.assertEquals(result,Result.FAILURE);
        result = BzmServiceManager.validateLocalTresholds(testResult, "50", "", "", "", stdErrLog);
        Assert.assertEquals(result, null);
        result = BzmServiceManager.validateLocalTresholds(testResult, "", "50", "", "", stdErrLog);
        Assert.assertEquals(result, null);
        result = BzmServiceManager.validateLocalTresholds(testResult, "50", "50", "", "", stdErrLog);
        Assert.assertEquals(result, null);
        result = BzmServiceManager.validateLocalTresholds(testResult, "", "", "60", "", stdErrLog);
        Assert.assertEquals(result, null);
        result = BzmServiceManager.validateLocalTresholds(testResult, "", "", "", "60", stdErrLog);
        Assert.assertEquals(result, null);
        result=BzmServiceManager.validateLocalTresholds(testResult,"","","60","60",stdErrLog);
        Assert.assertEquals(result,null);
        result=BzmServiceManager.validateLocalTresholds(testResult,"0","","","",stdErrLog);
        Assert.assertEquals(result,Result.UNSTABLE);
        result=BzmServiceManager.validateLocalTresholds(testResult,"","","0","",stdErrLog);
        Assert.assertEquals(result,Result.UNSTABLE);
        result=BzmServiceManager.validateLocalTresholds(testResult,"","0","","",stdErrLog);
        Assert.assertEquals(result,Result.FAILURE);
        result=BzmServiceManager.validateLocalTresholds(testResult,"","","","0",stdErrLog);
        Assert.assertEquals(result,Result.FAILURE);

    }

    @Test
    public void stopTestSession(){
        BlazemeterApi api = APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID, ApiVersion.v3, TestConstants.mockedApiUrl);
        boolean terminate = BzmServiceManager.stopTestSession(api, TestConstants.TEST_ID, TestConstants.TEST_SESSION_25, stdErrLog);
        Assert.assertEquals(terminate, true);
        terminate = BzmServiceManager.stopTestSession(api, TestConstants.TEST_ID, TestConstants.TEST_SESSION_70, stdErrLog);
        Assert.assertEquals(terminate, true);
        terminate = BzmServiceManager.stopTestSession(api, TestConstants.TEST_ID, TestConstants.TEST_SESSION_100, stdErrLog);
        Assert.assertEquals(terminate, false);
        terminate = BzmServiceManager.stopTestSession(api, TestConstants.TEST_ID, TestConstants.TEST_SESSION_140, stdErrLog);
        Assert.assertEquals(terminate, false);
    }
}
