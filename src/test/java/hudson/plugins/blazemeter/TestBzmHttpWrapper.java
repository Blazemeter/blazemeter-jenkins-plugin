package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.BzmHttpWrapper;
import org.json.JSONObject;
import org.junit.*;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestBzmHttpWrapper {
    private Logger log = LogManager.getLogManager().getLogger("TEST");
    private String userKey="1234567890";
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="12345";
    private BzmHttpWrapper bzmHttpWrapper=new BzmHttpWrapper();

    @BeforeClass
    public static void setUp()throws IOException{
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getSessionStatus();
        MockedAPI.getTests();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
    }

    @AfterClass
    public static void tearDown()throws IOException{
        MockedAPI.stopAPI();
    }

    @Test
    public void getResponseAsJson_25() throws IOException {
        String url="http://127.0.0.1:1234/api/latest/user?api_key=mockedAPIKeyValid&app_key=jnk100x987c06f4e10c4_clientId=CI_JENKINS&_clientVersion=2.1.-SNAPSHOT&";
        JSONObject response=bzmHttpWrapper.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
        Assert.assertTrue(response.length()==25);
    }

    @Test
    public void getResponseAsJson_null() throws IOException {
        JSONObject response=bzmHttpWrapper.getResponseAsJson(null, null, BzmHttpWrapper.Method.GET);
        Assert.assertTrue(response==null);
    }


    @Test
    public void getResponseAsString_5438() throws IOException {
        String url="http://127.0.0.1:1234/api/latest/user?api_key=mockedAPIKeyValid&app_key=jnk100x987c06f4e10c4_clientId=CI_JENKINS&_clientVersion=2.1.-SNAPSHOT&";
        String response=bzmHttpWrapper.getResponseAsString(url, null, BzmHttpWrapper.Method.GET);
        Assert.assertTrue(response.length()==5438);
    }


    @Test
    public void getResponseAsString_null() throws IOException {
        String response=bzmHttpWrapper.getResponseAsString(null, null, BzmHttpWrapper.Method.GET);
        Assert.assertTrue(response==null);
    }


}