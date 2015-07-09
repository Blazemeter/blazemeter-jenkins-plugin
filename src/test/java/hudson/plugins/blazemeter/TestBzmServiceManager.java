package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.utils.BzmServiceManager;
import org.junit.Assert;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

import java.io.File;
import java.io.IOException;

import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

/**
 * Created by zmicer on 8.7.15.
 */
public class TestBzmServiceManager {


    private String mockedUserKey ="mockedUserKey";
    private int mockedApiPort=1234;
    private String mockedApiUrl="http://127.0.0.1:"+mockedApiPort;
    private String mockedApiHost ="127.0.0.1";

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(mockedApiPort, mockedApiHost);

    private MockServerClient mockServerClient = new MockServerClient(mockedApiHost,mockedApiPort);

    @Test
    public void getUserEmail_positive() throws IOException,JSONException{
        File jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_positive.json");
        String userProfile=FileUtils.readFileToString(jsonFile);

        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", mockedUserKey)
                        ),
                once()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(userProfile));
        String email=BzmServiceManager.getUserEmail(mockedUserKey, mockedApiUrl);
        Assert.assertEquals(email,"dzmitry.kashlach@blazemeter.com");
    }

    @Test
    public void getUserEmail_negative() throws IOException,JSONException{
        File jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_negative.json");
        String userProfile=FileUtils.readFileToString(jsonFile);

        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", mockedUserKey)
                        ),
                once()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(userProfile));
        String email=BzmServiceManager.getUserEmail(mockedUserKey, mockedApiUrl);
        Assert.assertEquals(email,"");
    }
    @Test
    public void getUserEmail_exception() throws IOException,JSONException{
        File jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_jexception.txt");
        String userProfile=FileUtils.readFileToString(jsonFile);

        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader("Accept", "application/json")
                        .withQueryStringParameters(
                                new Parameter("api_key", mockedUserKey)
                        ),
                once()
        )
                .respond(
                        response().withHeader("application/json")
                                .withStatusCode(200).withBody(userProfile));
        String email=BzmServiceManager.getUserEmail(mockedUserKey, mockedApiUrl);
        Assert.assertEquals(email,"");
    }
}
