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

package hudson.plugins.blazemeter.api;

import com.google.common.collect.LinkedHashMultimap;
import hudson.plugins.blazemeter.api.urlmanager.UrlManager;
import hudson.plugins.blazemeter.entities.TestStatus;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public interface Api {

    String APP_KEY = "jnk100x987c06f4e10c4";

    TestStatus getTestStatus(String id);

    int getTestMasterStatusCode(String id);

    HashMap<String,String> startTest(String testId, TestType testType) throws JSONException;

    int getTestCount() throws JSONException, IOException, ServletException;

    JSONObject stopTest(String testId);

    void terminateTest(String testId);

    JSONObject testReport(String reportId);

    LinkedHashMultimap<String, String> getTestsMultiMap() throws IOException, MessagingException;

//    JSONObject getTestsJSON();

    JSONObject getUser();

    JSONObject getCIStatus(String sessionId) throws JSONException;

    JSONObject testConfig(String testId);

    boolean active(String testId);

    String retrieveJUNITXML(String sessionId);

    JSONObject retrieveJtlZip(String sessionId);

    List<String> getListOfSessionIds(String masterId);

    void setHttpUtil(HttpUtil bzmhc);

    HttpUtil getHttp();

    StdErrLog getLogger();

    void setLogger(StdErrLog logger);

    JSONObject generatePublicToken(String sessionId);

    String getApiKey();

    String getBlazeMeterURL();

    UrlManager getUrlManager();

    boolean ping() throws Exception;

    boolean notes(String note,String masterId)throws Exception;

    boolean properties(JSONArray properties, String sessionId) throws Exception;
}
