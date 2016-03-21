package hudson.plugins.blazemeter.api;

import com.google.common.collect.LinkedHashMultimap;
import hudson.plugins.blazemeter.api.urlmanager.BmUrlManager;
import hudson.plugins.blazemeter.api.urlmanager.BmUrlManagerV3Impl;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JsonConstants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BlazemeterApiV3Impl implements BlazemeterApi {

    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);

    private final String apiKey;
    BmUrlManager urlManager;
    private BzmHttpWrapper bzmhc = null;

    public BlazemeterApiV3Impl(String apiKey, String blazeMeterUrl,
                               String proxyHost,String proxyPort,
                               String proxyUser,String proxyPass) {
        this.apiKey = apiKey;
        urlManager = new BmUrlManagerV3Impl(blazeMeterUrl);
        try {
            bzmhc = new BzmHttpWrapper(proxyHost, proxyPort,proxyUser,proxyPass);
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
            JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
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
            JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            if (result.has(JsonConstants.DATA_URL) && result.get(JsonConstants.DATA_URL) == null) {
                testStatus = TestStatus.NotFound;
            } else {
                if (result.has("status") && !result.getString("status").equals("ENDED")) {
                    testStatus = TestStatus.Running;
                } else {
                    if (result.has("errors") && !result.get("errors").equals(JSONObject.NULL)) {
                        logger.debug("Error received from server: " + result.get("errors").toString());
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
    public synchronized String startTest(String testId, TestType testType) throws JSONException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) return null;
        String url = "";
        switch (testType) {
            case multi:
                url = this.urlManager.collectionStart(APP_KEY, apiKey, testId);
                break;
            default:
                url = this.urlManager.testStart(APP_KEY, apiKey, testId);
        }
        JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.POST, JSONObject.class);

        if (jo==null) {
            if (logger.isDebugEnabled())
                logger.debug("Received NULL from server while start operation: will do 5 retries");
            boolean isActive=this.active(testId);
            if(!isActive){
                int retries = 1;
                while (retries < 6) {
                    try {
                        if (logger.isDebugEnabled())
                            logger.debug("Trying to repeat start request: " + retries + " retry.");
                        logger.debug("Pausing thread for " + 10*retries + " seconds before doing "+retries+" retry.");
                        Thread.sleep(10000*retries);
                        jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.POST, JSONObject.class);
                        if (jo!=null) {
                            break;
                        }
                    } catch (InterruptedException ie) {
                        if (logger.isDebugEnabled())
                            logger.debug("Start operation was interrupted at pause during " + retries + " request retry.");
                    } catch (Exception ex) {
                        if (logger.isDebugEnabled())
                            logger.debug("Received bad response from server while starting test: " + retries + " retry.");
                    }
                    finally {
                        retries++;
                    }
                }


            }
        }
        JSONObject result=null;
        try{
            result = (JSONObject) jo.get(JsonConstants.RESULT);
        }catch (Exception e){
            if (logger.isDebugEnabled())
                logger.debug("Error while starting test: ",e);
            throw new JSONException("Faild to get 'result' node "+e.getMessage());

        }
        return result.getString(JsonConstants.ID);
    }

    @Override
    public int getTestCount() throws JSONException, IOException, ServletException {
        if (StringUtils.isBlank(apiKey)) return 0;
        String url = this.urlManager.tests(APP_KEY, apiKey);

        try {
            JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
            if (jo == null) {
                return -1;
            } else {
                JSONArray result = (JSONArray) jo.get(JsonConstants.RESULT);
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

    /**
     * @param testId - test id
     *               //     * @throws IOException
     *               //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject stopTest(String testId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) return null;
        String url = this.urlManager.testStop(APP_KEY, apiKey, testId);
        JSONObject stopJSON = this.bzmhc.response(url, null, BzmHttpWrapper.Method.POST, JSONObject.class);
        return stopJSON;
    }

    @Override
    public void terminateTest(String testId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) return;

        String url = this.urlManager.testTerminate(APP_KEY, apiKey, testId);
        this.bzmhc.response(url, null, BzmHttpWrapper.Method.POST, JSONObject.class);
        return;
    }


    /**
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject testReport(String reportId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(reportId)) return null;

        String url = this.urlManager.testReport(APP_KEY, apiKey, reportId);
        JSONObject summary = null;
        JSONObject result = null;
        try {
            result = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class).getJSONObject(JsonConstants.RESULT);
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
                JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
                JSONArray result = null;
                if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONArray) jo.get(JsonConstants.RESULT);
                }
                if (result != null && result.length() > 0) {
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
                                id = en.getString(JsonConstants.ID);
                                name = en.has(JsonConstants.NAME) ? en.getString(JsonConstants.NAME).replaceAll("&", "&amp;") : "";
                                String testType = en.has(JsonConstants.TYPE) ? en.getString(JsonConstants.TYPE) : Constants.UNKNOWN_TYPE;
                                testListOrdered.put(name, id + "." + testType);

                            }
                        } catch (JSONException ie) {
                            logger.warn("Error with the JSON while populating test list, ", ie);
                        }
                    }
                }
            } catch (NullPointerException npe) {
                logger.warn("Error while receiving answer from server - check connection ", npe);
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
        JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
        return jo;
    }

    @Override
    public JSONObject getTestConfig(String testId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.getTestConfig(APP_KEY, apiKey, testId);
        JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
        return jo;
    }

    @Override
    public JSONObject postJsonConfig(String testId, JSONObject data) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.postJsonConfig(APP_KEY, apiKey, testId);
        JSONObject jo = this.bzmhc.response(url, data, BzmHttpWrapper.Method.POST, JSONObject.class);
        return jo;
    }

    @Override
    public JSONObject createTest(JSONObject data) {
        if (StringUtils.isBlank(apiKey)) return null;
        String url = this.urlManager.createTest(APP_KEY, apiKey);
        JSONObject jo = this.bzmhc.response(url, data, BzmHttpWrapper.Method.POST, JSONObject.class);
        return jo;
    }

    @Override
    public JSONObject getCIStatus(String sessionId) throws JSONException, NullPointerException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) return null;
        String url = this.urlManager.getCIStatus(APP_KEY, apiKey, sessionId);
        JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class).getJSONObject(JsonConstants.RESULT);
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
        String xmlJunit = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, String.class);

        return xmlJunit;
    }

    @Override
    public JSONObject retrieveJtlZip(String sessionId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) return null;
        logger.info("Trying to get JTLZIP url for the sessionId=" + sessionId);
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, apiKey, sessionId);
        logger.info("Trying to retrieve JTLZIP json for the sessionId=" + sessionId);
        JSONObject jtlzip = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
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
    public void setBzmHttpWr(BzmHttpWrapper bzmhc) {
        this.bzmhc = bzmhc;
    }

    public BmUrlManager getUrlManager() {
        return urlManager;
    }


    @Override
    public JSONObject putTestInfo(String testId, JSONObject data) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.getTestConfig(APP_KEY, apiKey, testId);
        JSONObject jo = this.bzmhc.response(url, data, BzmHttpWrapper.Method.PUT, JSONObject.class);
        return jo;
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) return null;

        String url = this.urlManager.generatePublicToken(APP_KEY, apiKey, sessionId);
        JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.POST, JSONObject.class);


        return jo;
    }

    @Override
    public BzmHttpWrapper getBzmHttpWr() {
        return this.bzmhc;
    }

    @Override
    public JSONObject getTestsJSON() {
        String url = this.urlManager.tests(APP_KEY, apiKey);
        logger.info("Getting testList with URL=" + url.substring(0, url.indexOf("?") + 14));
        JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
        return jo;
    }

    @Override
    public List<String> getListOfSessionIds(String masterId) {
        List<String> sessionsIds = new ArrayList<String>();
        String url = this.urlManager.listOfSessionIds(APP_KEY, apiKey, masterId);
        JSONObject jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
        try {
            JSONArray sessions = jo.getJSONObject(JsonConstants.RESULT).getJSONArray("sessions");
            int sessionsLength = sessions.length();
            for (int i = 0; i < sessionsLength; i++) {
                sessionsIds.add(sessions.getJSONObject(i).getString(JsonConstants.ID));
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
            jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
            JSONObject result = null;
            if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                result = (JSONObject) jo.get(JsonConstants.RESULT);
                JSONArray tests = (JSONArray) result.get(JsonConstants.TESTS);
                for(int i=0;i<tests.length();i++){
                    if(String.valueOf(tests.getInt(i)).equals(testId)){
                        isActive=true;
                        return isActive;
                    }
                }
                JSONArray collections = (JSONArray) result.get(JsonConstants.COLLECTIONS);
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
            jo = this.bzmhc.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
            ping=jo.isNull(JsonConstants.ERROR);
        }catch (Exception e){
            logger.info("Failed to ping server: "+jo,e);
            throw e;
        }
        return ping;
    }
}
