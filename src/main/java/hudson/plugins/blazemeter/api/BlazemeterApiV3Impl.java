package hudson.plugins.blazemeter.api;

import com.google.common.collect.LinkedHashMultimap;
import hudson.plugins.blazemeter.api.urlmanager.BmUrlManager;
import hudson.plugins.blazemeter.api.urlmanager.UrlManagerFactory;
import hudson.plugins.blazemeter.entities.TestInfo;
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
import java.io.File;
import java.io.IOException;

/**
 * User: Vitali
 * Date: 4/2/12
 * Time: 14:05
 * <p/>
 * Updated
 * User: Doron
 * Date: 8/7/12
 * <p/>
 * Updated (proxy)
 * User: Marcel
 * Date: 9/23/13
 */

public class BlazemeterApiV3Impl implements BlazemeterApi {

    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);

    private final String apiKey;
    BmUrlManager urlManager;
    private BzmHttpWrapper bzmhc = null;

    BlazemeterApiV3Impl(String apiKey) {
        this.apiKey = apiKey;
        urlManager = UrlManagerFactory.getURLFactory().
                getURLManager(UrlManagerFactory.ApiVersion.v3, Constants.DEFAULT_BLAZEMETER_URL);
        try {
            bzmhc = new BzmHttpWrapper();
            bzmhc.configureProxy();
        } catch (Exception ex) {
            logger.warn("ERROR Instantiating HTTPClient. Exception received: ", ex);
        }
    }


    /**
     * @param testId - test id
     * @param file   - jmx file
     *               //     * @return test id
     *               //     * @throws java.io.IOException
     *               //     * @throws org.json.JSONException
     */
    @Override
    public synchronized void uploadJmx(String testId, File file) {
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return;

        String url = this.urlManager.scriptUpload(APP_KEY, apiKey, testId, file.getName());
        JSONObject json = this.bzmhc.getFileUploadJsonResponse(url, file);
        try {
            if (!json.get(JsonConstants.RESPONSE_CODE).equals(200)) {
                logger.warn("Could not upload file " + file.getName() + " " + json.get(JsonConstants.ERROR).toString());
            }
        } catch (JSONException e) {
            logger.warn("Could not upload file " + file.getName() + " ", e);
        }
    }

    /**
     * @param testId - test id
     * @param file   - the file (Java class) you like to upload
     * @return test id
     * //     * @throws java.io.IOException
     * //     * @throws org.json.JSONException
     */

    @Override
    public synchronized JSONObject uploadBinaryFile(String testId, File file) {
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.fileUpload(APP_KEY, apiKey, testId, file.getName());

        return this.bzmhc.getFileUploadJsonResponse(url, file);
    }


    @Override
    public int getTestSessionStatusCode(String id) {
        int statusCode=0;
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(id))
        {
            return statusCode;
        }
        try {
            String url = this.urlManager.testSessionStatus(APP_KEY, apiKey, id);
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            statusCode=result.getInt("statusCode");
        } catch (Exception e) {
            logger.warn("Error getting status ", e);
        }finally {
            {
                return statusCode;
            }
        }
    }

    @Override
    public TestInfo getTestInfo(String id) {
        TestInfo ti = new TestInfo();

        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(id))
        {
            ti.setStatus(TestStatus.NotFound);
            return ti;
        }

        try {
            String url = this.urlManager.testSessionStatus(APP_KEY, apiKey, id);
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            if (result.get(JsonConstants.DATA_URL) == null) {
                ti.setStatus(TestStatus.NotFound);
            } else {
                ti.setId(result.getString("testId"));
                ti.setName(result.getString(JsonConstants.NAME));
                if (!result.has("ended")||result.getString("ended").equals(JSONObject.NULL)||result.getString("ended").isEmpty()) {
                    ti.setStatus(TestStatus.Running);
                } else {
                    logger.info("Test is not running. Quiting job...");
                     if(result.has("errors")&&!result.get("errors").equals(JSONObject.NULL)){
                         logger.debug("Error received from server: "+result.get("errors").toString());
                         ti.setStatus(TestStatus.Error);
                     }else {
                        ti.setStatus(TestStatus.NotRunning);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error getting status ", e);
            ti.setStatus(TestStatus.Error);
        }
        return ti;
    }

    @Override
    public synchronized JSONObject startTest(String testId) {
    if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return null;
        logger.info("Calling urlManager with parameters: APP_KEY="+APP_KEY+" apiKey="+apiKey+" testId="+testId);
        String url = this.urlManager.testStart(APP_KEY, apiKey, testId);
        return this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.POST);
    }

    @Override
    public int getTestCount() throws JSONException, IOException, ServletException {
        if(StringUtils.isBlank(apiKey)) return 0;
        String url = this.urlManager.getTests(APP_KEY, apiKey);

        try {
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
            if (jo == null) {
                return -1;
            } else {
                JSONArray result = (JSONArray) jo.get(JsonConstants.RESULT);
                if (result.length() == 0) {
                    return 0;
                } else {
                    return result.length();
                }
            }
        } catch (JSONException e) {
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
    if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.testStop(APP_KEY, apiKey, testId);
        return this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
    }

    @Override
    public JSONObject terminateTest(String testId) {
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.testTerminate(APP_KEY, apiKey, testId);
        return this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
    }


    /**
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject testReport(String reportId) {
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(reportId)) return null;

        String url = this.urlManager.testReport(APP_KEY, apiKey, reportId);
        JSONObject summary = null;
        try {
            summary = (JSONObject) this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET).getJSONObject(JsonConstants.RESULT)
                    .getJSONArray("summary")
                    .get(0);
        } catch (JSONException je) {
            logger.warn("Error while parsing summary :", je);
        }
        return summary;
    }
    @Override
    public LinkedHashMultimap<String, String> getTestList() throws IOException, MessagingException {

        LinkedHashMultimap<String, String> testListOrdered = null;
        if(StringUtils.isBlank(apiKey)){ return null;}
         else {
            String url = this.urlManager.getTests(APP_KEY, apiKey);
            logger.info("Getting testList with URL=" + url);
            try {
                JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
                JSONArray result = (JSONArray) jo.get(JsonConstants.RESULT);
                if (result != null && result.length() > 0) {
                    testListOrdered = LinkedHashMultimap.create(result.length(),result.length());
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
                                name = en.has(JsonConstants.NAME)?en.getString(JsonConstants.NAME).replaceAll("&", "&amp;"):"";
                                testListOrdered.put(name, id);

                            }
                        } catch (JSONException ie) {
                            logger.warn("Error with the JSON while populating test list, ", ie);
                        }
                    }
                }
            } catch (NullPointerException npe) {
                logger.warn("Error while receiving answer from server - check connection ", npe);
            }catch (Exception e) {
                logger.warn("Error while populating test list, ", e);
            }

    }
        return testListOrdered;
    }

    @Override
    public JSONObject getUser() {
        if(StringUtils.isBlank(apiKey)) return null;
        String url = this.urlManager.getUser(APP_KEY, apiKey);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
        return jo;
    }

    @Override
    public JSONObject getTestConfig(String testId){
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.getTestConfig(APP_KEY, apiKey, testId);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
        return jo;
    }

    @Override
    public JSONObject postJsonConfig(String testId, JSONObject data){
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.postJsonConfig(APP_KEY, apiKey, testId);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, data, BzmHttpWrapper.Method.POST);
        return jo;
    }

    @Override
    public JSONObject createTest(JSONObject data) {
        if(StringUtils.isBlank(apiKey)) return null;
        String url = this.urlManager.createTest(APP_KEY, apiKey);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, data, BzmHttpWrapper.Method.POST);
        return jo;
      }

    @Override
    public JSONObject getTresholds(String sessionId){
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(sessionId)) return null;
        String url = this.urlManager.getTresholds(APP_KEY, apiKey, sessionId);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
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
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(sessionId)) return null;
        String url = this.urlManager.retrieveJUNITXML(APP_KEY, apiKey, sessionId);
        String xmlJunit = this.bzmhc.getResponseAsString(url, null, BzmHttpWrapper.Method.GET);

        return xmlJunit;
    }

    @Override
    public JSONObject retrieveJTLZIP(String sessionId) {
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(sessionId)) return null;
        logger.info("Trying to get JTLZIP url with the following parameters: APP_KEY="+APP_KEY+" apiKey="
                +apiKey+" sessionId="+sessionId);
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, apiKey, sessionId);
        logger.info("Trying to retrieve JTLZIP json with the following url: "+url);
        JSONObject jtlzip = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
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
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.getTestConfig(APP_KEY, apiKey, testId);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, data, BzmHttpWrapper.Method.PUT);
        return jo;
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) {
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(sessionId)) return null;

        String url = this.urlManager.generatePublicToken(APP_KEY, apiKey, sessionId);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.POST);


        return jo;
    }

    @Override
    public BzmHttpWrapper getBzmHttpWr() {
        return this.bzmhc;
    }
}
