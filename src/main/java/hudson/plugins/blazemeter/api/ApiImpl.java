/**
 Copyright 2017 BlazeMeter Inc.

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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ApiImpl implements Api {

    private final StdErrLog bzmLog;
    private Proxy proxy;
    private final String credential;
    UrlManager urlManager;
    private OkHttpClient okhttp;
    private final boolean legacy;

    public ApiImpl(String c, String blazeMeterUrl,boolean legacy){
        this(c, blazeMeterUrl,new HttpLoggingInterceptor(),null,legacy);
    }

    public ApiImpl(String c, String blazeMeterUrl,
                     HttpLoggingInterceptor httpLog,StdErrLog bzmLog,
                     boolean legacy) {
        this.legacy=legacy;
        this.credential = c;
        this.bzmLog = bzmLog != null ? bzmLog : new StdErrLog(Constants.BZM_JEN);
        this.urlManager = new UrlManagerV3Impl(blazeMeterUrl);
        try {
            httpLog.setLevel(HttpLoggingInterceptor.Level.BODY);
            this.proxy = Proxy.NO_PROXY;
            ProxyConfiguration proxyConf=null;
            try{
                proxyConf=ProxyConfiguration.load();
            }catch (NullPointerException e){
                this.bzmLog.info("Failed to load proxy configuration");
            }
            Authenticator auth=Authenticator.NONE;
            if (proxyConf != null) {
                if (!StringUtils.isBlank(proxyConf.name)) {
                    this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConf.name, proxyConf.port));
                }
                if (!StringUtils.isBlank(proxyConf.getUserName()) && !StringUtils.isBlank(proxyConf.getPassword())) {
                    final String proxyUser = proxyConf.getUserName();
                    final String proxyPass = proxyConf.getPassword();
                    auth = new Authenticator() {
                        @Override
                        public Request authenticate(Route route, Response response) {
                            String credential = Credentials.basic(proxyUser, proxyPass);
                            if (response.request().header("Proxy-Authorization") != null) {
                                return null; // Give up, we've already attempted to authenticate.
                            }
                            return response.request().newBuilder()
                                    .header("Proxy-Authorization", credential)
                                    .build();
                        }
                    };
                }
            }
            this.okhttp = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(httpLog).proxy(this.proxy)
                .addInterceptor(new RetryInterceptor(bzmLog))
                .proxyAuthenticator(auth)
                .build();
        } catch (Exception ex) {
            this.bzmLog.warn("ERROR Instantiating HTTPClient. Exception received: ", ex);
        }
    }


    @Override
    public int getTestMasterStatusCode(String id) {
        int statusCode = 0;
        try {
            String url = this.urlManager.masterStatus(Api.APP_KEY, id);
            Request r = new Request.Builder().url(url).get()
                    .addHeader(Api.ACCEPT, Api.APP_JSON)
                    .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                    .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
            JSONObject result = (JSONObject) jo.get(JsonConsts.RESULT);
            statusCode = result.getInt("progress");
        } catch (Exception e) {
            this.bzmLog.warn("Error getting status ", e);
        } finally {
            return statusCode;
        }
    }

    @Override
    public TestStatus getTestStatus(String id) {
        TestStatus testStatus = null;

        try {
            String url = this.urlManager.masterStatus(Api.APP_KEY, id);
            Request r = new Request.Builder().url(url).get()
                    .addHeader(Api.ACCEPT, Api.APP_JSON)
                    .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                    .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
            JSONObject result = (JSONObject) jo.get(JsonConsts.RESULT);
            if (result.has(JsonConsts.DATA_URL) && result.get(JsonConsts.DATA_URL) == null) {
                testStatus = TestStatus.NotFound;
            } else {
                if (result.has(JsonConsts.STATUS) && !result.getString(JsonConsts.STATUS).equals("ENDED")) {
                    testStatus = TestStatus.Running;
                } else {
                    if (result.has(JsonConsts.ERRORS) && !result.get(JsonConsts.ERRORS).equals(JSONObject.NULL)) {
                        this.bzmLog.debug("Error received from server: " + result.get(JsonConsts.ERRORS).toString());
                        testStatus = TestStatus.Error;
                    } else {
                        testStatus = TestStatus.NotRunning;
                        this.bzmLog.info("Master with id=" + id + " has status = " + TestStatus.NotRunning.name());
                    }
                }
            }
        } catch (Exception e) {
            this.bzmLog.warn("Error getting status ", e);
            testStatus = TestStatus.Error;
        }
        return testStatus;
    }

    @Override
    public synchronized HashMap<String, String> startTest(String testId, boolean collection) throws JSONException,
            IOException {
        String url = "";
        HashMap<String, String> startResp = new HashMap<String, String>();
        if(collection){
            url = this.urlManager.collectionStart(Api.APP_KEY, testId);
        }else {
            url = this.urlManager.testStart(Api.APP_KEY, testId);
        }
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody)
                .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
        Response rp = this.okhttp.newCall(r).execute();
        if (rp.code() == 500) {
            this.bzmLog.info("Server returned status = 500 while trying to start test.");
            return startResp;
        }
        JSONObject jo = new JSONObject(rp.body().string());
        if (jo == null) {
            if (this.bzmLog.isDebugEnabled())
                this.bzmLog.debug("Received NULL from server while start operation: will do 5 retries");
        }
        JSONObject result = null;
        try {
            result = (JSONObject) jo.get(JsonConsts.RESULT);
            startResp.put(JsonConsts.ID, result.getString(JsonConsts.ID));
            startResp.put(JsonConsts.TEST_ID, result.getString(collection ? JsonConsts.TEST_COLLECTION_ID : JsonConsts.TEST_ID));
            startResp.put(JsonConsts.NAME, result.getString(JsonConsts.NAME));
        } catch (Exception e) {
            startResp.put(JsonConsts.ERROR, jo.get(JsonConsts.ERROR).toString());
        } finally {
            return startResp;
        }
    }

    @Override
    public JSONObject stopTest(String testId) throws IOException, JSONException {
        String url = this.urlManager.testStop(Api.APP_KEY, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody)
                .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
        return jo;
    }

    @Override
    public void terminateTest(String testId) throws IOException{
        String url = this.urlManager.testTerminate(Api.APP_KEY, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody)
                .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
        this.okhttp.newCall(r).execute();
        return;
    }


   @Override
    public JSONObject testReport(String reportId) {

       String url = this.urlManager.testReport(Api.APP_KEY, reportId);
        JSONObject summary = null;
        JSONObject result = null;
        try {
            Request r = new Request.Builder().url(url).get()
                    .addHeader(Api.ACCEPT, Api.APP_JSON)
                    .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                    .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
            result = new JSONObject(this.okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConsts.RESULT);
            summary = (JSONObject) result.getJSONArray("summary")
                    .get(0);
        } catch (JSONException je) {
            this.bzmLog.warn("Aggregate report(result object): " + result);
            this.bzmLog.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
                    "is valid/not empty.", je);
        } catch (Exception e) {
            this.bzmLog.warn("Aggregate report(result object): " + result);
            this.bzmLog.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
                    "is valid/not empty.", e);
        } finally {
            return summary;
        }
    }
    @Override
    public LinkedHashMultimap<String, String> testsMultiMap(int workspaceId) {
        LinkedHashMultimap<String, String> testListOrdered = LinkedHashMultimap.create();
        this.bzmLog.warn("Getting tests...");
            String url = this.urlManager.tests(Api.APP_KEY, workspaceId);
            try {
                Request r = new Request.Builder().url(url).get().addHeader(Api.ACCEPT, Api.APP_JSON)
                        .addHeader(Api.AUTHORIZATION, this.credential).
                                addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
                JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
                JSONArray result = null;
                this.bzmLog.warn("Received json: " + jo.toString());
                if (jo.has(JsonConsts.ERROR) && jo.get(JsonConsts.RESULT).equals(JSONObject.NULL) &&
                        ((JSONObject) jo.get(JsonConsts.ERROR)).getInt(JsonConsts.CODE) == 401) {
                    return testListOrdered;
                }
                if (jo.has(JsonConsts.RESULT) && !jo.get(JsonConsts.RESULT).equals(JSONObject.NULL)) {
                    result = (JSONArray) jo.get(JsonConsts.RESULT);
                }
                LinkedHashMultimap<String, String> wst = LinkedHashMultimap.create();
                LinkedHashMultimap<String, String> wsc = this.collectionsMultiMap(workspaceId);
                wst.putAll(wsc);
                if (result != null && result.length() > 0) {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject entry = null;
                        try {
                            entry = result.getJSONObject(i);
                        } catch (JSONException e) {
                            this.bzmLog.warn("JSONException while getting tests: " + e);
                        }
                        String id;
                        String name;
                        try {
                            if (entry != null) {
                                id = String.valueOf(entry.get(JsonConsts.ID));
                                name = entry.has(JsonConsts.NAME) ? entry.getString(JsonConsts.NAME).replaceAll("&", "&amp;") : "";
                                String testType = null;
                                try {
                                    testType = entry.getJSONObject(JsonConsts.CONFIGURATION).getString(JsonConsts.TYPE);
                                } catch (Exception e) {
                                    testType = Constants.UNKNOWN_TYPE;
                                }
                                wst.put(id + "." + testType, name + "(" + id + "." + testType + ")");
                            }
                        } catch (JSONException ie) {
                            this.bzmLog.warn("JSONException while getting tests: " + ie);
                        }
                    }
                }
                Comparator c = new Comparator<Map.Entry<String, String>>() {
                    @Override
                    public int compare(Map.Entry<String, String> e1, Map.Entry<String, String> e2) {
                        return e1.getValue().compareToIgnoreCase(e2.getValue());
                    }
                };
                wst.entries().stream().sorted(c).
                        forEach(entry -> testListOrdered.put(
                                ((Map.Entry<String, String>) entry).getKey(), ((Map.Entry<String, String>) entry).getValue()));
            } catch (Exception e) {
                this.bzmLog.warn("Exception while getting tests: ", e);
                this.bzmLog.warn("Check connection/proxy settings");
                testListOrdered.put(Constants.CHECK_SETTINGS, Constants.CHECK_SETTINGS);
            }
        return testListOrdered;
    }


    @Override
    public LinkedHashMultimap<String, String> collectionsMultiMap(int workspaceId) {
        LinkedHashMultimap<String, String> collectionsListOrdered = LinkedHashMultimap.create();
        this.bzmLog.warn("Getting collections...");
        String url = this.urlManager.multiTests(Api.APP_KEY, workspaceId);
        try {
            Request r = new Request.Builder().url(url).get().addHeader(Api.ACCEPT, Api.APP_JSON)
                    .addHeader(Api.AUTHORIZATION, this.credential).
                            addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
            JSONArray result = null;
            this.bzmLog.warn("Received json: " + jo.toString());
            if (jo.has(JsonConsts.ERROR) && jo.get(JsonConsts.RESULT).equals(JSONObject.NULL) &&
                    ((JSONObject) jo.get(JsonConsts.ERROR)).getInt(JsonConsts.CODE) == 401) {
                return collectionsListOrdered;
            }
            if (jo.has(JsonConsts.RESULT) && !jo.get(JsonConsts.RESULT).equals(JSONObject.NULL)) {
                result = (JSONArray) jo.get(JsonConsts.RESULT);
            }
            if (result != null && result.length() > 0) {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject entry = null;
                    try {
                        entry = result.getJSONObject(i);
                    } catch (JSONException e) {
                        this.bzmLog.warn("JSONException while getting tests: " + e);
                    }
                    String id;
                    String name;
                    try {
                        if (entry != null) {
                            id = String.valueOf(entry.get(JsonConsts.ID));
                            name = entry.has(JsonConsts.NAME) ? entry.getString(JsonConsts.NAME).replaceAll("&", "&amp;") : "";
                            String collectionsType = null;
                            try {
                                collectionsType = entry.getString(JsonConsts.COLLECTION_TYPE);
                            } catch (Exception e) {
                                collectionsType = Constants.UNKNOWN_TYPE;
                            }
                            collectionsListOrdered.put(id + "." + collectionsType, name + "(" + id + "." + collectionsType + ")");
                        }
                    } catch (JSONException ie) {
                        this.bzmLog.warn("JSONException while getting tests: " + ie);
                    }
                }
            }
        } catch (Exception e) {
            this.bzmLog.warn("Exception while getting tests: ", e);
            this.bzmLog.warn("Check connection/proxy settings");
            collectionsListOrdered.put(Constants.CHECK_SETTINGS, Constants.CHECK_SETTINGS);
        }
        return collectionsListOrdered;
    }


    @Override
    public JSONObject getUser() throws IOException,JSONException {
        String url = this.urlManager.getUser(Api.APP_KEY);
        Request r = new Request.Builder().url(url).get()
                .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
            .build();

        JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
        return jo;
    }

    @Override
    public JSONObject getCIStatus(String sessionId) throws JSONException, NullPointerException, IOException {
        this.bzmLog.info("Trying to get JTLZIP url for the sessionId = " + sessionId);
        String url = this.urlManager.getCIStatus(Api.APP_KEY, sessionId);
        Request r = new Request.Builder().url(url).get()
                .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConsts.RESULT);
        return jo;
    }


    @Override
    public String getBlazeMeterURL() {
        return this.urlManager.getServerUrl();
    }

    @Override
    public String retrieveJUNITXML(String masterId) throws IOException{
        String url = this.urlManager.retrieveJUNITXML(Api.APP_KEY, masterId);
        Request r = new Request.Builder().url(url).get()
                .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
        String xmlJunit = this.okhttp.newCall(r).execute().body().string();
        return xmlJunit;
    }

    @Override
    public JSONObject retrieveJtlZip(String sessionId) throws IOException, JSONException {
        this.bzmLog.info("Trying to get JTLZIP url for the sessionId=" + sessionId);
        String url = this.urlManager.retrieveJTLZIP(Api.APP_KEY, sessionId);
        this.bzmLog.info("Trying to retrieve JTLZIP json for the sessionId = " + sessionId);
        Request r = new Request.Builder().url(url).get()
                .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
        JSONObject jtlzip = new JSONObject(this.okhttp.newCall(r).execute().body().string());
        return jtlzip;
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) throws IOException,JSONException{
        String url = this.urlManager.generatePublicToken(Api.APP_KEY, sessionId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody)
                .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
        return jo;
    }

    @Override
    public List<String> getListOfSessionIds(String masterId) throws IOException, JSONException {
        List<String> sessionsIds = new ArrayList<String>();
        String url = this.urlManager.listOfSessionIds(Api.APP_KEY, masterId);
        Request r = new Request.Builder().url(url).get()
                .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
                .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();

        JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
        try {
            JSONArray sessions = jo.getJSONObject(JsonConsts.RESULT).getJSONArray("sessions");
            int sessionsLength = sessions.length();
            for (int i = 0; i < sessionsLength; i++) {
                sessionsIds.add(sessions.getJSONObject(i).getString(JsonConsts.ID));
            }
        } catch (JSONException je) {
            this.bzmLog.info("Failed to get list of sessions from JSONObject " + jo, je);
        } catch (Exception e) {
            this.bzmLog.info("Failed to get list of sessions from JSONObject " + jo, e);
        } finally {
            return sessionsIds;
        }
    }

    @Override
    public boolean notes(String note, String masterId) throws Exception {
        String noteEsc = StringEscapeUtils.escapeJson("{'"+ JsonConsts.NOTE+"':'"+note+"'}");
        String url = this.urlManager.masterId(Api.APP_KEY, masterId);
        JSONObject noteJson = new JSONObject(noteEsc);
        RequestBody body = RequestBody.create(Api.TEXT, noteJson.toString());
        Request r = new Request.Builder().url(url).patch(body)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
            .build();
        JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
        try {
            if (!jo.get(JsonConsts.ERROR).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Failed to submit report notest to masterId = " + masterId, e);
        }
        return true;
    }

    @Override
    public boolean properties(JSONArray properties, String sessionId) throws Exception {
        String url = this.urlManager.properties(Api.APP_KEY, sessionId);
        RequestBody body = RequestBody.create(Api.JSON, properties.toString());
        Request r = new Request.Builder().url(url).post(body)
                .addHeader(this.legacy ? Api.X_API_KEY : Api.AUTHORIZATION, this.credential)
            .build();
        JSONObject jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
        try {
            if (jo.get(JsonConsts.RESULT).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Failed to submit report properties to sessionId = " + sessionId, e);
        }
        return true;
    }

    @Override
    public String getCredential() {
        return this.credential;
    }
    @Override
    public JSONObject funcReport(final String masterId) throws Exception {

        String url = this.urlManager.masterId(Api.APP_KEY, masterId);
        JSONObject fSummary = null;
        JSONObject result = null;
        try {
            Request r = new Request.Builder().url(url).get()
                    .addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(Api.AUTHORIZATION, this.credential)
                    .addHeader(Api.CONTENT_TYPE, Api.APP_JSON_UTF_8).build();
            result = new JSONObject(this.okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConsts.RESULT);
            if (result.has("functionalSummary")) {
                fSummary = result.getJSONObject("functionalSummary");
            }
        } catch (JSONException je) {
            this.bzmLog.warn("Functional report(result object): " + result);
            this.bzmLog.warn("Error while parsing functional report: check common jenkins log and make sure that functional report" +
                "is valid/not empty.", je);
        } catch (Exception e) {
            this.bzmLog.warn("Functional report(result object): " + result);
            this.bzmLog.warn("Error while parsing functional report summary: check common jenkins log and make sure that functional report" +
                "is valid/not empty.", e);
        } finally {
            return fSummary;
        }
    }


    @Override
    public HashMap<Integer,String> accounts() {
        String url = this.urlManager.accounts(Api.APP_KEY);
        Request r = new Request.Builder().url(url).get().addHeader(Api.ACCEPT, Api.APP_JSON)
                .addHeader(Api.AUTHORIZATION, this.credential).build();
        JSONObject jo = null;
        JSONArray result = null;
        JSONObject dp = null;
        HashMap<Integer, String> acs = new HashMap<Integer, String>();
        try {
            jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
        } catch (Throwable e) {
            this.bzmLog.warn("Failed to get accounts: " + e);
            return acs;
        }
        try {
            result = jo.getJSONArray(JsonConsts.RESULT);
        } catch (Exception e) {
            this.bzmLog.warn("Failed to get accounts: " + e);
            return acs;
        }
        try {
            for (int i = 0; i < result.length(); i++) {
                JSONObject a = result.getJSONObject(i);
                acs.put(a.getInt(JsonConsts.ID),a.getString(JsonConsts.NAME));
            }
        } catch (Exception e) {
            this.bzmLog.warn("Failed to get accounts: " + e);
            return acs;
        }
        return acs;
    }

    @Override
    public HashMap<Integer, String> workspaces() {
        HashMap<Integer, String> acs = this.accounts();
        HashMap<Integer, String> ws = new HashMap<Integer, String>();

        Set<Integer> keys = acs.keySet();
        for (Integer key : keys) {
            String url = this.urlManager.workspaces(Api.APP_KEY, key);
            Request r = new Request.Builder().url(url).get().addHeader(Api.ACCEPT, Api.APP_JSON)
                    .addHeader(Api.AUTHORIZATION, this.credential).build();
            JSONObject jo = null;
            JSONArray result = null;
            try {
                jo = new JSONObject(this.okhttp.newCall(r).execute().body().string());
            } catch (Exception ioe) {
                this.bzmLog.warn("Failed to get workspaces: " + ioe);
                return ws;
            }
            try {
                result = jo.getJSONArray(JsonConsts.RESULT);
            } catch (Exception e) {
                this.bzmLog.warn("Failed to get workspaces: " + e);
                return ws;
            }
            try {

                for (int i = 0; i < result.length(); i++) {
                    JSONObject s = result.getJSONObject(i);
                    ws.put(s.getInt(JsonConsts.ID), s.getString(JsonConsts.NAME));
                }
            } catch (Exception e) {
                this.bzmLog.warn("Failed to get workspaces: " + e);
                return ws;
            }
        }
        return ws;
    }

}
