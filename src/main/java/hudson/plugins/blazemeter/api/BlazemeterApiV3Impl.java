package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.api.urlmanager.BmUrlManager;
import hudson.plugins.blazemeter.api.urlmanager.UrlManagerFactory;
import hudson.plugins.blazemeter.entities.TestInfo;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.Constants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
            if (!json.get("response_code").equals(200)) {
                logger.warn("Could not upload file " + file.getName() + " " + json.get("error").toString());
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
    public TestInfo getTestRunStatus(String testId) {
        TestInfo ti = new TestInfo();

        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId))
        {
            ti.setStatus(TestStatus.NotFound);
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, apiKey, testId);
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
            JSONObject result = (JSONObject) jo.get("result");
            if (result.get("dataUrl") == null) {
                ti.setStatus(TestStatus.NotFound);
            } else {
                ti.setId(result.getString("testId"));
                ti.setName(result.getString("name"));
                if (!result.getString("status").equals("ENDED")) {
                    ti.setStatus(TestStatus.Running);
                } else {
                    ti.setStatus(TestStatus.NotRunning);
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
                JSONArray result = (JSONArray) jo.get("result");
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
            summary = (JSONObject) this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET).getJSONObject("result")
                    .getJSONArray("summary")
                    .get(0);
        } catch (JSONException je) {
            logger.warn("Error while parsing summary :", je);
        }
        return summary;
    }
    @Override
    public HashMap<String, String> getTestList() throws IOException, MessagingException {

        LinkedHashMap<String, String> testListOrdered = null;
        if(StringUtils.isBlank(apiKey)){ return null;}
         else {
            String url = this.urlManager.getTests(APP_KEY, apiKey);
            logger.info("Getting testList with URL=" + url);
            try {
                JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
                JSONArray result = (JSONArray) jo.get("result");
                if (result != null && result.length() > 0) {
                    testListOrdered = new LinkedHashMap<String, String>(result.length());
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
                                id = en.getString("id");
                                name = en.getString("name").replaceAll("&", "&amp;");
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
    public JSONObject getTestInfo(String testId){
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.getTestInfo(APP_KEY, apiKey, testId);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
        return jo;
    }

    @Override
    public JSONObject updateTestInfo(String testId, JSONObject data){
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.putTestInfo(APP_KEY, apiKey, testId);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, data, BzmHttpWrapper.Method.POST);
        return jo;
    }

    @Override
    public JSONObject createYahooTest(JSONObject data){
        if(StringUtils.isBlank(apiKey)) return null;
        String url = this.urlManager.createYahooTest(APP_KEY, apiKey);
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
    public StdErrLog getLogger() {
        return logger;
    }

    @Override
    public void setLogger(StdErrLog logger) {
        this.logger = logger;
    }

    @Override
    public JSONObject createTest(JSONObject data,String testName) {
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(testName)) return null;
        String url = this.urlManager.createTest(APP_KEY, apiKey);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, data, BzmHttpWrapper.Method.POST);
        return jo;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }


    public void setBzmHttpWr(BzmHttpWrapper bzmhc) {
        this.bzmhc = bzmhc;
    }

    public BmUrlManager getUrlManager() {
        return urlManager;
    }
}
