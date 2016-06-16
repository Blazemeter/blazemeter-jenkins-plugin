 package hudson.plugins.blazemeter;

import com.google.common.collect.LinkedHashMultimap;
import hudson.plugins.blazemeter.api.*;
import hudson.plugins.blazemeter.entities.TestStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.mockito.Mockito;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
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
    }

    @AfterClass
    public static void tearDown() throws IOException {
        MockedAPI.stopAPI();
    }


    @Test
    public void retrieveJUNITXML_null() {
        blazemeterApiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.retrieveJUNITXML(null), null);
    }


    @Test
    public void getTestInfo_null() {
        blazemeterApiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.getTestConfig(null), null);
    }

    @Test
    public void getTestStatus_Running() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,MockedAPI.proxyConfig);
        TestStatus testStatus = blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,MockedAPI.proxyConfig);
        TestStatus testStatus = blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }


    @Test
    public void getTestInfo_Error() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,MockedAPI.proxyConfig);
        TestStatus testStatus = blazemeterApiV3.getTestStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }

    @Test
    public void getTestInfo_NotFound() {
        blazemeterApiV3 = new ApiV3Impl("",TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        TestStatus testStatus = blazemeterApiV3.getTestStatus("");
        Assert.assertEquals(testStatus, TestStatus.NotFound);
    }


    @Test
    public void getUser_null() {
        blazemeterApiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.getUser(), null);
    }

    @Test
    public void getTestCount_zero() {
        try {
            blazemeterApiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl,
                    MockedAPI.proxyConfig);
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
    public void testReport_null() {
        blazemeterApiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.testReport(null), null);
    }

    @Test
    public void stopTest_null() {
        blazemeterApiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.stopTest(null), null);
    }

    @Test
    public void startTest_null() throws JSONException {
        blazemeterApiV3 = new ApiV3Impl(null, null,MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.startTest(null,null), null);
    }

    @Test
    public void startTest_http() throws JSONException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.http), "15102806");
    }

    @Test
    public void startTest_jmeter() throws JSONException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.jmeter), "15102806");
    }

    @Test
    public void startTest_followme() throws JSONException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.followme), "15102806");
    }

    @Test
    public void startTest_multi() throws JSONException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.multi), "15105877");
    }

    @Ignore
    @Test
    public void startTest_Retries() throws JSONException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_RETRIES,
                TestConstants.mockedApiUrl,MockedAPI.proxyConfig);
        Api spyApi = Mockito.spy(blazemeterApiV3);
        HttpUtil spyWrapper = Mockito.spy(blazemeterApiV3.getHttp());
        spyApi.setHttpUtil(spyWrapper);
        try {
            spyApi.startTest(TestConstants.TEST_MASTER_ID, TestType.http);
        } catch (JSONException je) {
            Mockito.verify(spyApi, Mockito.times(1)).active(TestConstants.TEST_MASTER_ID);
            String url = "http://127.0.0.1:1234/api/latest/tests/testMasterId/start?" +
                    "api_key=mockedAPIKeyRetries&app_key=jnk100x987c06f4e10c4_clientId=CI_JENKINS&_clientVersion=2.2.-SNAPSHOT&​";
            Mockito.verify(spyWrapper, Mockito.times(6)).response(url, null, Method.POST, JSONObject.class,null);

        }
    }


    @Test
    public void getTestRunStatus_notFound() {
        blazemeterApiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        Assert.assertEquals(blazemeterApiV3.getTestStatus(null), TestStatus.NotFound);
    }

    @Test
    public void getTestList_6_10() throws IOException, JSONException, ServletException, MessagingException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        LinkedHashMultimap<String, String> testList = blazemeterApiV3.getTestsMultiMap();
        Assert.assertTrue(testList.asMap().size() == 6);
        Assert.assertTrue(testList.size() == 10);

    }

    @Test
    public void getTestList_6_6() throws IOException, JSONException, ServletException, MessagingException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_6_TESTS, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        LinkedHashMultimap<String, String> testList = blazemeterApiV3.getTestsMultiMap();
        Assert.assertTrue(testList.asMap().size() == 6);
        Assert.assertTrue(testList.size() == 6);

    }

    @Test
    public void getTestReport() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        JSONObject testReport = blazemeterApiV3.testReport(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(testReport.length() == 33);


    }

    @Test
    public void getTestList_null() throws IOException, JSONException, ServletException, MessagingException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        LinkedHashMultimap<String, String> testList = blazemeterApiV3.getTestsMultiMap();
        Assert.assertTrue(testList == null);

    }

    @Test
    public void getTestsCount_10() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 10);

    }

    @Test
    public void getTestsCount_1() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_1_TEST, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 1);

    }

    @Test
    public void getTestsCount_0() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_0_TESTS, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 0);

    }

    @Test
    public void getTestsCount_null() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_INVALID, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == -1);

    }

    @Test
    public void getTestSessionStatusCode_25() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status == 25);
    }

    @Test
    public void getTestSessionStatusCode_70() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status == 70);
    }

    @Test
    public void getTestSessionStatusCode_140() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl,
                MockedAPI.proxyConfig);
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
