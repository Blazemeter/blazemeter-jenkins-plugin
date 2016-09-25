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
import hudson.ProxyConfiguration;
import hudson.plugins.blazemeter.api.urlmanager.UrlManager;
import hudson.plugins.blazemeter.api.urlmanager.UrlManagerV3Impl;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JsonConsts;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class ApiV3Impl implements Api {

    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);

    private final String apiKey;
    UrlManager urlManager;
    private HttpUtil bzmhc = null;

    public ApiV3Impl(String apiKey, String blazeMeterUrl){
        this(apiKey, blazeMeterUrl,null);
    }

    public ApiV3Impl(String apiKey, String blazeMeterUrl,
                     ProxyConfiguration proxy) {
        this.apiKey = apiKey;
        urlManager = new UrlManagerV3Impl(blazeMeterUrl);
        try {
            bzmhc = new HttpUtil(proxy);
        } catch (Exception ex) {
            logger.warn("ERROR Instantiating HTTPClient. Exception received: ", ex);
        }
    }


    @Override
    public int getTestMasterStatusCode(String id) {
        int statusCode = 0;
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(id)) {
            return statusCode;
        }
        try {
            String url = this.urlManager.masterStatus(APP_KEY, apiKey, id);
            JSONObject jo = this.bzmhc.response(url, null, Method.GET, JSONObject.class,null);
            JSONObject result = (JSONObject) jo.get(JsonConsts.RESULT);
            statusCode = result.getInt("progress");
        } catch (Exception e) {
            logger.warn("Error getting status ", e);
        } finally {
            {
                return statusCode;
            }
        }
    }

    @Override
    public TestStatus getTestStatus(String id) {
        TestStatus testStatus = null;

        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(id)) {
            testStatus = TestStatus.NotFound;
            return testStatus;
        }

        try {
            String url = this.urlManager.masterStatus(APP_KEY, apiKey, id);
            JSONObject jo = this.bzmhc.response(url, null, Method.GET, JSONObject.class,null);
            JSONObject result = (JSONObject) jo.get(JsonConsts.RESULT);
            if (result.has(JsonConsts.DATA_URL) && result.get(JsonConsts.DATA_URL) == null) {
                testStatus = TestStatus.NotFound;
            } else {
                if (result.has(JsonConsts.STATUS) && !result.getString(JsonConsts.STATUS).equals("ENDED")) {
                    testStatus = TestStatus.Running;
                } else {
                    if (result.has(JsonConsts.ERRORS) && !result.get(JsonConsts.ERRORS).equals(JSONObject.NULL)) {
                        logger.debug("Error received from server: " + result.get(JsonConsts.ERRORS).toString());
                        testStatus = TestStatus.Error;
                    } else {
                        testStatus = TestStatus.NotRunning;
                        logger.info("Master with id="+id+" has status = "+TestStatus.NotRunning.name());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error getting status ", e);
            testStatus = TestStatus.Error;
        }
        return testStatus;
    }

    @Override
    public synchronized HashMap<String, String> startTest(String testId, TestType testType) throws JSONException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) return null;
        String url = "";
        HashMap<String, String> startResp = new HashMap<String, String>();
        switch (testType) {
            case multi:
                url = this.urlManager.collectionStart(APP_KEY, apiKey, testId);
                break;
            default:
                url = this.urlManager.testStart(APP_KEY, apiKey, testId);
        }
        JSONObject jo = this.bzmhc.response(url, null, Method.POST, JSONObject.class, null);
        JSONObject result = null;
        try {
            result = (JSONObject) jo.get(JsonConsts.RESULT);
            startResp.put(JsonConsts.ID, result.getString(JsonConsts.ID));
            startResp.put(JsonConsts.TEST_ID, result.getString(JsonConsts.TEST_ID));
            startResp.put(JsonConsts.NAME, result.getString(JsonConsts.NAME));
        } catch (Exception e) {
            startResp.put(JsonConsts.ERROR, jo.get(JsonConsts.ERROR).toString());
        } finally {
            return startResp;
        }
    }

    @Override
    public int getTestCount() throws JSONException, IOException, ServletException {
        if (StringUtils.isBlank(apiKey)) return 0;
        String url = this.urlManager.tests(APP_KEY, apiKey);

        try {
            JSONObject jo = this.bzmhc.response(url, null, Method.GET, JSONObject.class,null);
            if (jo == null) {
                return -1;
            } else {
                JSONArray result = (JSONArray) jo.get(JsonConsts.RESULT);
                return result.length();
            }
        } catch (JSONException e) {
            logger.warn("Error getting response from server: ", e);
            return -1;
        } catch (RuntimeException e) {
            logger.warn("Error getting response from server: ", e);
            return -1;
        }
    }

    @Override
    public JSONObject stopTest(String testId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) return null;
        String url = this.urlManager.testStop(APP_KEY, apiKey, testId);
        JSONObject stopJSON = this.bzmhc.response(url, null, Method.POST, JSONObject.class,null);
        return stopJSON;
    }

    @Override
    public void terminateTest(String testId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) return;

        String url = this.urlManager.testTerminate(APP_KEY, apiKey, testId);
        this.bzmhc.response(url, null, Method.POST, JSONObject.class,null);
        return;
    }


   @Override
    public JSONObject testReport(String reportId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(reportId)) return null;

        String url = this.urlManager.testReport(APP_KEY, apiKey, reportId);
        JSONObject summary = null;
        JSONObject result = null;
        try {
            result = this.bzmhc.response(url, null, Method.GET, JSONObject.class,null).getJSONObject(JsonConsts.RESULT);
            summary = (JSONObject) result.getJSONArray("summary")
                    .get(0);
        } catch (JSONException je) {
            logger.warn("Aggregate report(result object): " + result);
            logger.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
                    "is valid/not empty.", je);
        } catch (Exception e) {
            logger.warn("Aggregate report(result object): " + result);
            logger.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
                    "is valid/not empty.", e);
        } finally {
            return summary;
        }
    }

    @Override
    public LinkedHashMultimap<String, String> getTestsMultiMap() throws IOException, MessagingException {

        LinkedHashMultimap<String, String> testListOrdered = null;
        if (StringUtils.isBlank(apiKey)) {
            return null;
        } else {
            String url = this.urlManager.tests(APP_KEY, apiKey);
            logger.info("Getting testList with URL=" + url.substring(0, url.indexOf("?") + 14));
            try {
                JSONObject jo = this.bzmhc.response(url, null, Method.GET, JSONObject.class, null);
                JSONArray result = null;

                if (jo.has(JsonConsts.ERROR) && (jo.get(JsonConsts.RESULT).equals(JSONObject.NULL)) &&
                        (((JSONObject) jo.get(JsonConsts.ERROR)).getInt(JsonConsts.CODE) == 401)) {
                    return testListOrdered;
                }
                if (jo.has(JsonConsts.RESULT) && (!jo.get(JsonConsts.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONArray) jo.get(JsonConsts.RESULT);
                }
                if (result != null) {
                    if (result.length() > 0) {

                        testListOrdered = LinkedHashMultimap.create(result.length(), result.length());
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject en = null;
                            try {
                                en = result.getJSONObject(i);
                            } catch (JSONException e) {
                                logger.warn("Error with the JSON while populating test list, " + e);
                            }
                            String id;
                            String name;
                            try {
                                if (en != null) {
                                    id = en.getString(JsonConsts.ID);
                                    name = en.has(JsonConsts.NAME) ? en.getString(JsonConsts.NAME).replaceAll("&", "&amp;") : "";
                                    String testType = en.has(JsonConsts.TYPE) ? en.getString(JsonConsts.TYPE) : Constants.UNKNOWN_TYPE;
                                    testListOrdered.put(name, id + "." + testType);

                                }
                            } catch (JSONException ie) {
                                logger.warn("Error with the JSON while populating test list, ", ie);
                            }
                        }

                    } else {
                        testListOrdered = LinkedHashMultimap.create(0, 0);
                    }
                }
            } catch (NullPointerException npe) {
                logger.warn("Error while receiving answer from server - check connection/proxy settings ", npe);
            } catch (Exception e) {
                logger.warn("Error while populating test list, ", e);
            } finally {
                return testListOrdered;
            }

        }
    }

    @Override
    public JSONObject getUser() throws ClassCastException {
        if (StringUtils.isBlank(apiKey)) return null;
        String url = this.urlManager.getUser(APP_KEY, apiKey);
        JSONObject jo = this.bzmhc.response(url, null, Method.GET, JSONObject.class,null);
        return jo;
    }

    @Override
    public JSONObject getCIStatus(String sessionId) throws JSONException, NullPointerException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) return null;
        String url = this.urlManager.getCIStatus(APP_KEY, apiKey, sessionId);
        JSONObject jo = this.bzmhc.response(url, null, Method.GET, JSONObject.class,null).getJSONObject(JsonConsts.RESULT);
        return jo;
    }


    void setBlazeMeterURL(String blazeMeterURL) {
        this.urlManager.setServerUrl(blazeMeterURL);
    }

    @Override
    public String getBlazeMeterURL() {
        return this.urlManager.getServerUrl();
    }

    @Override
    public String retrieveJUNITXML(String sessionId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) return null;
        String url = this.urlManager.retrieveJUNITXML(APP_KEY, apiKey, sessionId);
        String xmlJunit = this.bzmhc.response(url, null,Method.GET, String.class,null);

        return xmlJunit;
    }

    @Override
    public JSONObject retrieveJtlZip(String sessionId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) return null;
        logger.info("Trying to get JTLZIP url for the sessionId=" + sessionId);
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, apiKey, sessionId);
        logger.info("Trying to retrieve JTLZIP json for the sessionId=" + sessionId);
        JSONObject jtlzip = this.bzmhc.response(url, null, Method.GET, JSONObject.class,JSONObject.class);
        return jtlzip;
    }

    @Override
    public StdErrLog getLogger() {
        return logger;
    }

    @Override
    public void setLogger(StdErrLog logger) {
        this.logger = logger;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }


    @Override
    public void setHttpUtil(HttpUtil bzmhc) {
        this.bzmhc = bzmhc;
    }

    public UrlManager getUrlManager() {
        return urlManager;
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) return null;

        String url = this.urlManager.generatePublicToken(APP_KEY, apiKey, sessionId);
        JSONObject jo = this.bzmhc.response(url, null, Method.POST, JSONObject.class,JSONObject.class);


        return jo;
    }

    @Override
    public HttpUtil getHttp() {
        return this.bzmhc;
    }

    @Override
    public List<String> getListOfSessionIds(String masterId) {
        List<String> sessionsIds = new ArrayList<String>();
        String url = this.urlManager.listOfSessionIds(APP_KEY, apiKey, masterId);
        JSONObject jo = this.bzmhc.response(url, null, Method.GET, JSONObject.class,JSONObject.class);
        try {
            JSONArray sessions = jo.getJSONObject(JsonConsts.RESULT).getJSONArray("sessions");
            int sessionsLength = sessions.length();
            for (int i = 0; i < sessionsLength; i++) {
                sessionsIds.add(sessions.getJSONObject(i).getString(JsonConsts.ID));
            }
        } catch (JSONException je) {
            logger.info("Failed to get list of sessions from JSONObject " + jo, je);
        } catch (Exception e) {
            logger.info("Failed to get list of sessions from JSONObject " + jo, e);
        } finally {
            return sessionsIds;
        }
    }

    @Override
    public boolean active(String testId) {
        boolean isActive=false;
        String url = this.urlManager.activeTests(APP_KEY, apiKey);
        JSONObject jo = null;
        try {
            jo = this.bzmhc.response(url, null, Method.GET, JSONObject.class,JSONObject.class);
            JSONObject result = null;
            if (jo.has(JsonConsts.RESULT) && (!jo.get(JsonConsts.RESULT).equals(JSONObject.NULL))) {
                result = (JSONObject) jo.get(JsonConsts.RESULT);
                JSONArray tests = (JSONArray) result.get(JsonConsts.TESTS);
                for(int i=0;i<tests.length();i++){
                    if(String.valueOf(tests.getInt(i)).equals(testId)){
                        isActive=true;
                        return isActive;
                    }
                }
                JSONArray collections = (JSONArray) result.get(JsonConsts.COLLECTIONS);
                for(int i=0;i<collections.length();i++){
                    if(String.valueOf(collections.getInt(i)).equals(testId)){
                        isActive=true;
                        return isActive;
                    }
                }
            }
            return isActive;
        } catch (JSONException je) {
            logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, je);
            return false;
        } catch (Exception e) {
            logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, e);
            return false;
        }
    }

    @Override
    public boolean ping() throws Exception{
        String url = this.urlManager.version(APP_KEY);
        JSONObject jo=null;
        boolean ping=false;
        try{
            jo = this.bzmhc.response(url, null, Method.GET, JSONObject.class,JSONObject.class);
            ping=jo.isNull(JsonConsts.ERROR);
        }catch (Exception e){
            logger.info("Failed to ping server: "+jo,e);
            throw e;
        }
        return ping;
    }

    @Override
    public boolean notes(String note, String masterId) throws Exception {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(masterId)) return false;
        String noteEsc = StringEscapeUtils.escapeJson("{'"+ JsonConsts.NOTE+"':'"+note+"'}");
        JSONObject noteJson = new JSONObject(noteEsc);
        String url = this.urlManager.masterId(APP_KEY, apiKey, masterId);
        JSONObject jo = this.bzmhc.response(url, noteJson, Method.PATCH, JSONObject.class, JSONObject.class);
        try {
            if (!jo.get(JsonConsts.ERROR).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Failed to submit report notest to masterId=" + masterId, e);
        }
        return true;
    }

    @Override
    public boolean properties(JSONArray properties, String sessionId) throws Exception {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) return false;
        String url = this.urlManager.properties(APP_KEY, apiKey, sessionId);
        JSONObject jo = this.bzmhc.response(url, properties, Method.POST, JSONObject.class,JSONArray.class);
        try {
            if (jo.get(JsonConsts.RESULT).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Failed to submit report properties to sessionId=" + sessionId, e);
        }
        return true;
    }
}
