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

import hudson.plugins.blazemeter.api.HttpUtil;
import hudson.plugins.blazemeter.api.Method;
import org.json.JSONObject;
import org.junit.*;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class TestHttpUtil {
    private Logger log = LogManager.getLogManager().getLogger("TEST");
    private String userKey = "1234567890";
    private String appKey = "jnk100x987c06f4e10c4";
    private String testId = "12345";
    private static HttpUtil httpUtil = null;

    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getMasterStatus();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
        httpUtil =new HttpUtil(MockedAPI.proxyConfig);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        MockedAPI.stopAPI();
    }

    @Test
    public void response_25() throws IOException {
        String url = TestConstants.mockedApiUrl+"/api/latest/user?api_key=mockedAPIKeyValid&app_key=jnk100x987c06f4e10c4_clientId=CI_JENKINS&_clientVersion=2.1.-SNAPSHOT&";
        JSONObject response = httpUtil.response(url, null, Method.GET, JSONObject.class,null);
        Assert.assertTrue(response.length() == 25);
    }

    @Test
    public void response_null() throws IOException, RuntimeException {
        try {
            httpUtil.response(null, null, Method.GET, JSONObject.class,null);
        } catch (RuntimeException re) {

        }
    }


    @Test
    public void responseString_null() throws IOException, RuntimeException {
        try {
            httpUtil.response(null, null, Method.GET, String.class,null);
        } catch (RuntimeException re) {

        }
    }
}