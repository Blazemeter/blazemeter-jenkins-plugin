package hudson.plugins.blazemeter;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Cookie;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.Parameter;

import java.util.concurrent.TimeUnit;

import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

/**
 * Created by zmicer on 8.7.15.
 */
public class TestBzmServiceManager {


    @Rule
    public MockServerRule mockServerRule = new MockServerRule(1234,this);

    private MockServerClient mockServerClient;

    @Test
    public void getUserEmail(){
          mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withPath("/login")
                        .withQueryStringParameters(
                                new Parameter("returnUrl", "/account")
                        )
                        .withCookies(
                                new Cookie("sessionId", "2By8LOhBmaW5nZXJwcmludCIlMDAzMW")
                        )
                        .withBody(exact("{username: 'foo', password: 'bar'}")),
                exactly(1)
        )
        .respond(
                response()
                        .withStatusCode(401)
                        .withHeaders(
                                new Header("Content-Type", "application/json; charset=utf-8"),
                                new Header("Cache-Control", "public, max-age=86400")
                        )
                        .withBody("{ message: 'incorrect username and password combination' }")
                        .withDelay(new Delay(TimeUnit.SECONDS, 1))
        );
}}
