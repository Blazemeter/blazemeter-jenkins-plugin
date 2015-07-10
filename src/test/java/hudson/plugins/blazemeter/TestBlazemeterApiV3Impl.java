package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApiV3Impl;
import hudson.plugins.blazemeter.entities.TestInfo;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.Constants;
import org.json.JSONException;
import org.junit.*;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestBlazemeterApiV3Impl {
    private Logger log = LogManager.getLogManager().getLogger("TEST");
    private BlazemeterApiV3Impl blazemeterApiV3 =null;
    private String userKey="1234567890";
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="12345";


    @BeforeClass
    public static void setUp()throws IOException{
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getSessionStatus();
    }

    @AfterClass
    public static void tearDown()throws IOException{
        MockedAPI.stopAPI();
    }


    @Test
    public void createTest_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3, Constants.DEFAULT_BLAZEMETER_URL);
        Assert.assertEquals(blazemeterApiV3.createTest(null), null);
    }

    @Test
    public void retrieveJUNITXML_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
        Assert.assertEquals(blazemeterApiV3.retrieveJUNITXML(null), null);
    }


    @Test
    public void getTresholds_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
        Assert.assertEquals(blazemeterApiV3.getTresholds(null), null);
    }


    @Test
    public void updateTestInfo_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
        Assert.assertEquals(blazemeterApiV3.postJsonConfig(null, null), null);
    }

    @Test
    public void getTestInfo_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
        Assert.assertEquals(blazemeterApiV3.getTestConfig(null), null);
    }

    @Test
    public void getTestInfo_Running(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,ApiVersion.v3,
                TestConstants.mockedApiUrl);
        TestInfo ti=blazemeterApiV3.getTestInfo(TestConstants.TEST_SESSION_100);
        Assert.assertEquals(ti.getId(), "5039530");
        Assert.assertEquals(ti.getName(), "FAILED-2");
        Assert.assertEquals(ti.getStatus(), TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,ApiVersion.v3,
                TestConstants.mockedApiUrl);
        TestInfo ti=blazemeterApiV3.getTestInfo(TestConstants.TEST_SESSION_140);
        Assert.assertEquals(ti.getId(), "5039532");
        Assert.assertEquals(ti.getName(), "PASSED-1");
        Assert.assertEquals(ti.getStatus(), TestStatus.NotRunning);
    }


    @Test
    public void getTestInfo_Error(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,ApiVersion.v3,
                TestConstants.mockedApiUrl);
        TestInfo ti=blazemeterApiV3.getTestInfo(TestConstants.TEST_SESSION_NOT_FOUND);
        Assert.assertEquals(ti.getId(), null);
        Assert.assertEquals(ti.getName(), null);
        Assert.assertEquals(ti.getStatus(), TestStatus.Error);
    }

    @Test
    public void getTestInfo_NotFound(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI("",ApiVersion.v3,
                TestConstants.mockedApiUrl);
        TestInfo ti=blazemeterApiV3.getTestInfo("");
        Assert.assertEquals(ti.getId(), null);
        Assert.assertEquals(ti.getName(), null);
        Assert.assertEquals(ti.getStatus(), TestStatus.NotFound);
    }



    @Test
    public void getUser_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
        Assert.assertEquals(blazemeterApiV3.getUser(), null);
    }
 
    @Test
    public void getTestList_null(){
        try {
            blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
            Assert.assertEquals(blazemeterApiV3.getTestList(), null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getTestCount_zero(){
        try {
            blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
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
    public void testReport_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
        Assert.assertEquals(blazemeterApiV3.testReport(null), null);
    }

   @Test
    public void stopTest_null(){
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
       Assert.assertEquals(blazemeterApiV3.stopTest(null), null);
    }

   @Test
    public void startTest_null(){
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
       Assert.assertEquals(blazemeterApiV3.startTest(null), null);
    }


   @Test
    public void getTestRunStatus_notFound(){
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
       Assert.assertEquals(blazemeterApiV3.getTestInfo(null).getStatus(), TestStatus.NotFound);
    }

    @Test
    public void uploadBinaryFile_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);
        Assert.assertEquals(blazemeterApiV3.uploadBinaryFile(null, null), null);
    }

    @Ignore
    @Test
    public void getTestsList(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);

    }

    @Ignore
    @Test
    public void getTestsCount(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);

    }

    @Test
    public void getTestSessionStatusCode_25(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                ApiVersion.v3,TestConstants.mockedApiUrl);
        int status=blazemeterApiV3.getTestSessionStatusCode(TestConstants.TEST_SESSION_25);
        Assert.assertTrue(status==25);
    }

    @Test
    public void getTestSessionStatusCode_70(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                ApiVersion.v3,TestConstants.mockedApiUrl);
        int status=blazemeterApiV3.getTestSessionStatusCode(TestConstants.TEST_SESSION_70);
        Assert.assertTrue(status==70);
    }

    @Test
    public void getTestSessionStatusCode_140(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                ApiVersion.v3,TestConstants.mockedApiUrl);
        int status=blazemeterApiV3.getTestSessionStatusCode(TestConstants.TEST_SESSION_140);
        Assert.assertTrue(status==140);
    }


    @Test
    public void getTestSessionStatusCode_100(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                ApiVersion.v3,TestConstants.mockedApiUrl);
        int status=blazemeterApiV3.getTestSessionStatusCode(TestConstants.TEST_SESSION_100);
        Assert.assertTrue(status==100);
    }

    @Test
    public void getTestSessionStatusCode_0(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_EXCEPTION,
                ApiVersion.v3,TestConstants.mockedApiUrl);
        int status=blazemeterApiV3.getTestSessionStatusCode(TestConstants.TEST_SESSION_0);
        Assert.assertTrue(status==0);
    }



    @Ignore
    @Test
    public void putTestInfo(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,ApiVersion.v3,Constants.DEFAULT_BLAZEMETER_URL);

    }
}