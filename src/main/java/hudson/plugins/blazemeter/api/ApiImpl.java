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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
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

    private StdErrLog bzmLog = null;
    private Proxy proxy = null;
    private Authenticator auth = null;
    private final String credential;
    UrlManager urlManager;
    private OkHttpClient okhttp = null;
    private boolean legacy=false;

    public ApiImpl(String c, String blazeMeterUrl,boolean legacy){
        this(c, blazeMeterUrl,new HttpLoggingInterceptor(),null,legacy);
    }

    public ApiImpl(String c, String blazeMeterUrl,
                     HttpLoggingInterceptor httpLog,StdErrLog bzmLog,
                     boolean legacy) {
        this.legacy=legacy;
        this.credential = c;
        this.bzmLog = (bzmLog!=null?bzmLog:new StdErrLog(Constants.BZM_JEN));
        this.urlManager = new UrlManagerV3Impl(blazeMeterUrl);
        try {
            httpLog.setLevel(HttpLoggingInterceptor.Level.BODY);
            this.proxy = Proxy.NO_PROXY;
            this.auth = Authenticator.NONE;
            ProxyConfiguration proxyConf=null;
            try{
                proxyConf=ProxyConfiguration.load();
            }catch (NullPointerException e){
                this.bzmLog.info("Failed to load proxy configuration");
            }
            if (proxyConf != null) {
                if (!StringUtils.isBlank(proxyConf.name)) {
                    this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConf.name, proxyConf.port));
                }
                if (!StringUtils.isBlank(proxyConf.getUserName()) && !StringUtils.isBlank(proxyConf.getPassword())) {
                    final String proxyUser = proxyConf.getUserName();
                    final String proxyPass = proxyConf.getPassword();
                    this.auth = new Authenticator() {
                        @Override
                        public Request authenticate(Route route, Response response) throws IOException {
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
                .proxyAuthenticator(this.auth)
                .build();
        } catch (Exception ex) {
            this.bzmLog.warn("ERROR Instantiating HTTPClient. Exception received: ", ex);
        }
    }


    @Override
    public int getTestMasterStatusCode(String id) {
        int statusCode = 0;
        try {
            String url = this.urlManager.masterStatus(APP_KEY, id);
            Request r = new Request.Builder().url(url).get()
                .addHeader(ACCEPT, APP_JSON)
                .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
                .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            JSONObject result = (JSONObject) jo.get(JsonConsts.RESULT);
            statusCode = result.getInt("progress");
        } catch (Exception e) {
            bzmLog.warn("Error getting status ", e);
        } finally {
            {
                return statusCode;
            }
        }
    }

    @Override
    public TestStatus getTestStatus(String id) {
        TestStatus testStatus = null;

        try {
            String url = this.urlManager.masterStatus(APP_KEY, id);
            Request r = new Request.Builder().url(url).get()
            .addHeader(ACCEPT, APP_JSON)
                .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
                .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            JSONObject result = (JSONObject) jo.get(JsonConsts.RESULT);
            if (result.has(JsonConsts.DATA_URL) && result.get(JsonConsts.DATA_URL) == null) {
                testStatus = TestStatus.NotFound;
            } else {
                if (result.has(JsonConsts.STATUS) && !result.getString(JsonConsts.STATUS).equals("ENDED")) {
                    testStatus = TestStatus.Running;
                } else {
                    if (result.has(JsonConsts.ERRORS) && !result.get(JsonConsts.ERRORS).equals(JSONObject.NULL)) {
                        bzmLog.debug("Error received from server: " + result.get(JsonConsts.ERRORS).toString());
                        testStatus = TestStatus.Error;
                    } else {
                        testStatus = TestStatus.NotRunning;
                        bzmLog.info("Master with id="+id+" has status = "+TestStatus.NotRunning.name());
                    }
                }
            }
        } catch (Exception e) {
            bzmLog.warn("Error getting status ", e);
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
            url = this.urlManager.collectionStart(APP_KEY, testId);
        }else {
            url = this.urlManager.testStart(APP_KEY, testId);
        }
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody)
            .addHeader(ACCEPT, APP_JSON)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        Response rp = okhttp.newCall(r).execute();
        if (rp.code() == 500) {
            bzmLog.info("Server returned status = 500 while trying to start test.");
            bzmLog.info("Make sure that you're not trying to start test with non-existent OPL");
            return startResp;
        }
        JSONObject jo = new JSONObject(rp.body().string());
        if (jo == null) {
            if (bzmLog.isDebugEnabled())
                bzmLog.debug("Received NULL from server while start operation: will do 5 retries");
            boolean isActive = this.active(testId);
            if (!isActive) {
                int retries = 1;
                while (retries < 6) {
                    try {
                        if (bzmLog.isDebugEnabled())
                            bzmLog.debug("Trying to repeat start request: " + retries + " retry.");
                        bzmLog.debug("Pausing thread for " + 10 * retries + " seconds before doing " + retries + " retry.");
                        Thread.sleep(10000 * retries);
                        jo = new JSONObject(okhttp.newCall(r).execute().body().string());
                        if (jo != null) {
                            break;
                        }
                    } catch (InterruptedException ie) {
                        if (bzmLog.isDebugEnabled())
                            bzmLog.debug("Start operation was interrupted at pause during " + retries + " request retry.");
                    } catch (Exception ex) {
                        if (bzmLog.isDebugEnabled())
                            bzmLog.debug("Received bad response from server while starting test: " + retries + " retry.");
                    } finally {
                        retries++;
                    }
                }


            }
        }
        JSONObject result = null;
        try {
            result = (JSONObject) jo.get(JsonConsts.RESULT);
            startResp.put(JsonConsts.ID, result.getString(JsonConsts.ID));
            startResp.put(JsonConsts.TEST_ID, collection ? result.getString(JsonConsts.TEST_COLLECTION_ID) :
                    result.getString(JsonConsts.TEST_ID));
            startResp.put(JsonConsts.NAME, result.getString(JsonConsts.NAME));
        } catch (Exception e) {
            startResp.put(JsonConsts.ERROR, jo.get(JsonConsts.ERROR).toString());
        } finally {
            return startResp;
        }
    }

    @Override
    public int getTestCount() throws JSONException, IOException, ServletException {
        String url = this.urlManager.tests(APP_KEY);

        try {
            Request r = new Request.Builder().url(url).get()
                .addHeader(ACCEPT, APP_JSON)
                .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
                .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            if (jo == null) {
                return -1;
            } else {
                JSONArray result = (JSONArray) jo.get(JsonConsts.RESULT);
                return result.length();
            }
        } catch (JSONException e) {
            bzmLog.warn("Error getting response from server: ", e);
            return -1;
        } catch (RuntimeException e) {
            bzmLog.warn("Error getting response from server: ", e);
            return -1;
        } catch (Exception e) {
            bzmLog.warn("Error getting response from server: ", e);
            return -1;
        }
    }

    @Override
    public JSONObject stopTest(String testId) throws IOException, JSONException {
        String url = this.urlManager.testStop(APP_KEY, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody)
            .addHeader(ACCEPT, APP_JSON)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        return jo;
    }

    @Override
    public void terminateTest(String testId) throws IOException{
        String url = this.urlManager.testTerminate(APP_KEY, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody)
            .addHeader(ACCEPT, APP_JSON)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        okhttp.newCall(r).execute();
        return;
    }


   @Override
    public JSONObject testReport(String reportId) {

        String url = this.urlManager.testReport(APP_KEY, reportId);
        JSONObject summary = null;
        JSONObject result = null;
        try {
            Request r = new Request.Builder().url(url).get()
                .addHeader(ACCEPT, APP_JSON)
                .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
                .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            result = new JSONObject(okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConsts.RESULT);
            summary = (JSONObject) result.getJSONArray("summary")
                    .get(0);
        } catch (JSONException je) {
            bzmLog.warn("Aggregate report(result object): " + result);
            bzmLog.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
                    "is valid/not empty.", je);
        } catch (Exception e) {
            bzmLog.warn("Aggregate report(result object): " + result);
            bzmLog.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
                    "is valid/not empty.", e);
        } finally {
            return summary;
        }
    }

    @Override
    public LinkedHashMultimap<String, String> testsMultiMap() throws IOException, MessagingException {

        LinkedHashMultimap<String, String> testListOrdered = null;
            String url = this.urlManager.tests(APP_KEY);
            bzmLog.info("Getting testList with URL=" + url.substring(0, url.indexOf("?") + 14));
            try {
                Request r = new Request.Builder().url(url).get()
                    .addHeader(ACCEPT, APP_JSON)
                    .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
                    .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
                JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
                JSONArray result = null;

                if (jo.has(JsonConsts.ERROR) && (jo.get(JsonConsts.RESULT).equals(JSONObject.NULL)) &&
                        (((JSONObject   ) jo.get(JsonConsts.ERROR)).getInt(JsonConsts.CODE) == 401)) {
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
                                bzmLog.warn("Error with the JSON while populating test list, " + e);
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
                                bzmLog.warn("Error with the JSON while populating test list, ", ie);
                            }
                        }

                    } else {
                        testListOrdered = LinkedHashMultimap.create(0, 0);
                    }
                }
            } catch (NullPointerException npe) {
                bzmLog.warn("Error while receiving answer from server - check connection/proxy settings ", npe);
            } catch (IOException e) {
                bzmLog.warn("Error while populating test list, ", e);
                testListOrdered = LinkedHashMultimap.create(1, 1);
                testListOrdered.put("Please, check proxy settings - ",e.getMessage());
            } catch (Exception e) {
                bzmLog.warn("Error while populating test list, ", e);
            } finally {
                return testListOrdered;
            }
    }

    @Override
    public JSONObject getUser() throws IOException,JSONException {
        String url = this.urlManager.getUser(APP_KEY);
        Request r = new Request.Builder().url(url).get()
            .addHeader(ACCEPT, APP_JSON)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .build();

        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        return jo;
    }

    @Override
    public JSONObject getCIStatus(String sessionId) throws JSONException, NullPointerException, IOException {
        bzmLog.info("Trying to get JTLZIP url for the sessionId = " + sessionId);
        String url = this.urlManager.getCIStatus(APP_KEY, sessionId);
        Request r = new Request.Builder().url(url).get()
            .addHeader(ACCEPT, APP_JSON)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConsts.RESULT);
        return jo;
    }


    @Override
    public String getBlazeMeterURL() {
        return this.urlManager.getServerUrl();
    }

    @Override
    public String retrieveJUNITXML(String masterId) throws IOException{
        String url = this.urlManager.retrieveJUNITXML(APP_KEY, masterId);
        Request r = new Request.Builder().url(url).get()
            .addHeader(ACCEPT, APP_JSON)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        String xmlJunit = okhttp.newCall(r).execute().body().string();
        return xmlJunit;
    }

    @Override
    public JSONObject retrieveJtlZip(String sessionId) throws IOException, JSONException {
        bzmLog.info("Trying to get JTLZIP url for the sessionId=" + sessionId);
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, sessionId);
        bzmLog.info("Trying to retrieve JTLZIP json for the sessionId = " + sessionId);
        Request r = new Request.Builder().url(url).get()
            .addHeader(ACCEPT, APP_JSON)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jtlzip = new JSONObject(okhttp.newCall(r).execute().body().string());
        return jtlzip;
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) throws IOException,JSONException{
        String url = this.urlManager.generatePublicToken(APP_KEY, sessionId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody)
            .addHeader(ACCEPT, APP_JSON)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jo=new JSONObject(okhttp.newCall(r).execute().body().string());
        return jo;
    }

    @Override
    public List<String> getListOfSessionIds(String masterId) throws IOException, JSONException {
        List<String> sessionsIds = new ArrayList<String>();
        String url = this.urlManager.listOfSessionIds(APP_KEY, masterId);
        Request r = new Request.Builder().url(url).get()
            .addHeader(ACCEPT, APP_JSON)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        try {
            JSONArray sessions = jo.getJSONObject(JsonConsts.RESULT).getJSONArray("sessions");
            int sessionsLength = sessions.length();
            for (int i = 0; i < sessionsLength; i++) {
                sessionsIds.add(sessions.getJSONObject(i).getString(JsonConsts.ID));
            }
        } catch (JSONException je) {
            bzmLog.info("Failed to get list of sessions from JSONObject " + jo, je);
        } catch (Exception e) {
            bzmLog.info("Failed to get list of sessions from JSONObject " + jo, e);
        } finally {
            return sessionsIds;
        }
    }

    @Override
    public boolean active(String testId) {
        boolean isActive=false;
        String url = this.urlManager.activeTests(APP_KEY);
        JSONObject jo = null;
        try {
            Request r = new Request.Builder().url(url).get()
                .addHeader(ACCEPT, APP_JSON)
                .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
                .addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            jo = new JSONObject(okhttp.newCall(r).execute().body().string());
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
            bzmLog.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, je);
            return false;
        } catch (Exception e) {
            bzmLog.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, e);
            return false;
        }
    }

    @Override
    public boolean ping() throws Exception{
        String url = this.urlManager.version(APP_KEY);
        JSONObject jo=null;
        boolean ping=false;
        try{
            Request r = new Request.Builder().url(url).get()
                .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
                .addHeader(ACCEPT, APP_JSON).build();
            jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            ping=jo.isNull(JsonConsts.ERROR);
        }catch (Exception e){
            bzmLog.info("Failed to ping server: "+jo,e);
            throw e;
        }
        return ping;
    }

    @Override
    public boolean notes(String note, String masterId) throws Exception {
        String noteEsc = StringEscapeUtils.escapeJson("{'"+ JsonConsts.NOTE+"':'"+note+"'}");
        String url = this.urlManager.masterId(APP_KEY, masterId);
        JSONObject noteJson = new JSONObject(noteEsc);
        RequestBody body = RequestBody.create(TEXT,noteJson.toString());
        Request r = new Request.Builder().url(url).patch(body)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
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
        String url = this.urlManager.properties(APP_KEY, sessionId);
        RequestBody body = RequestBody.create(JSON,properties.toString());
        Request r = new Request.Builder().url(url).post(body)
            .addHeader(legacy?X_API_KEY:AUTHORIZATION,this.credential)
            .build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
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
}
