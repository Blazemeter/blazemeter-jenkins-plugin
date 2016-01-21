package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.BzmHttpWrapper;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONObject;
import org.junit.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestBzmHttpWrapper {
    private Logger log = LogManager.getLogManager().getLogger("TEST");
    private String userKey = "1234567890";
    private String appKey = "jnk100x987c06f4e10c4";
    private String testId = "12345";
    private BzmHttpWrapper bzmHttpWrapper = new BzmHttpWrapper();

    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getMasterStatus();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        MockedAPI.stopAPI();
    }

    @Test
    public void response_25() throws IOException {
        String url = TestConstants.mockedApiUrl+"/api/latest/user?api_key=mockedAPIKeyValid&app_key=jnk100x987c06f4e10c4_clientId=CI_JENKINS&_clientVersion=2.1.-SNAPSHOT&";
        JSONObject response = bzmHttpWrapper.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
        Assert.assertTrue(response.length() == 25);
    }

    @Test
    public void response_null() throws IOException, RuntimeException {
        try {
            bzmHttpWrapper.response(null, null, BzmHttpWrapper.Method.GET, JSONObject.class);
        } catch (RuntimeException re) {

        }
    }


    @Test
    public void responseString_null() throws IOException, RuntimeException {
        try {
            bzmHttpWrapper.response(null, null, BzmHttpWrapper.Method.GET, String.class);
        } catch (RuntimeException re) {

        }
    }
}