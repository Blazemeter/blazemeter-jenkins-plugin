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

public class BlazemeterApiV2Impl implements BlazemeterApi {
    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);
    public static JSONObject not_implemented;
    private final String apiKey;
    BmUrlManager urlManager;
    private BzmHttpWrapper bzmhc = null;

    BlazemeterApiV2Impl(String apiKey) {
        this.apiKey = apiKey;
        urlManager = UrlManagerFactory.getURLFactory().
                getURLManager(UrlManagerFactory.ApiVersion.v2, Constants.DEFAULT_BLAZEMETER_URL);
        not_implemented=new JSONObject();
        try {
            not_implemented.put(Constants.NOT_IMPLEMENTED,Constants.NOT_IMPLEMENTED);
            bzmhc = new BzmHttpWrapper();
            bzmhc.configureProxy();
        } catch (JSONException je) {
            logger.warn("Error NOT_IMPLEMENTED Object: ", je);
        } catch (Exception ex) {
            logger.warn("Error Instantiating HTTPClient. Exception received: ", ex);
        }
    }


    /**
     * @param testId  - test id
     * @param file    - jmx file
     *                //     * @return test id
     *                //     * @throws java.io.IOException
     *                //     * @throws org.json.JSONException
     */
    @Override
    public synchronized void uploadJmx(String testId, File file) {

        if (StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) {
            return;
        }

        String url = this.urlManager.scriptUpload(APP_KEY, apiKey, testId, file.getName());
        JSONObject json = this.bzmhc.getFileUploadJsonResponse(url, file);
        try {
            if (!json.get("response_code").equals(200)) {
                logger.warn("Could not upload file " + file.getName() + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            logger.warn("Could not upload file " + file.getName() + " ", e);
            e.printStackTrace();
        }
    }

    /**
     * @param testId  - test id
     * @param file    - the file (Java class) you like to upload
     * @return test id
     * //     * @throws java.io.IOException
     * //     * @throws org.json.JSONException
     */

    @Override
    public synchronized JSONObject uploadBinaryFile(String testId, File file) {

        if (StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) {
            return null;
        }

        String url = this.urlManager.fileUpload(APP_KEY, apiKey, testId, file.getName());

        return this.bzmhc.getFileUploadJsonResponse(url, file);
    }


    @Override
    public TestInfo getTestRunStatus(String testId) {
        TestInfo ti = new TestInfo();

        if (StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) {
            ti.setStatus(TestStatus.NotFound);
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, apiKey, testId);
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.POST);

            if ("Test not found".equals(jo.get("error"))) {
                ti.setStatus(TestStatus.NotFound);
            } else {
                ti.setId(jo.getString("test_id"));
                ti.setName(jo.getString("test_name"));
                ti.setStatus(jo.getString("status"));
            }
        } catch (Exception e) {
            logger.warn("ERROR getting status " + e);
            ti.setStatus(TestStatus.Error);
        }
        return ti;
    }

    @Override
    public synchronized JSONObject startTest(String testId) {
        if (StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) {
            return null;
        }

        String url = this.urlManager.testStart(APP_KEY, apiKey, testId);
        return this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.POST);
    }

    @Override
    public int getTestCount() throws JSONException, IOException, ServletException {
        if (StringUtils.isBlank(apiKey)) {
            return 0;
        }

        String url = this.urlManager.getTests(APP_KEY, apiKey);

        try {
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.POST);
            if (jo == null) {
                return -1;
            } else {
                String r = jo.get("response_code").toString();
                if (!r.equals("200")) {
                    return 0;
                }
                JSONArray arr = (JSONArray) jo.get("tests");
                return arr.length();
            }
        } catch (JSONException e) {
            logger.warn("Error getting response from server: ", e);
            return -1;
        }
    }

    /**
     * @param testId  - test id
     *                //     * @throws IOException
     *                //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject stopTest(String testId) {
        if (StringUtils.isBlank(apiKey)&StringUtils.isBlank(testId)) {
            return null;
        }

        String url = this.urlManager.testStop(APP_KEY, apiKey, testId);
        return this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.POST);
    }

    /**
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject testReport(String reportId) {
        if (StringUtils.isBlank(apiKey)&StringUtils.isBlank(reportId)) {
            return null;
        }
        String url = this.urlManager.testReport(APP_KEY, apiKey, reportId);
        JSONObject response = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
        JSONObject aggregate = null;
        try {
            aggregate = response.getJSONObject("report").getJSONObject("aggregate");
        } catch (JSONException e) {
            logger.warn("Error while parsing aggregate report V2: " + e);
        }
        return aggregate;

    }

    @Override
    public HashMap<String, String> getTestList() throws IOException, MessagingException {

        LinkedHashMap<String, String> testListOrdered = null;

        if (StringUtils.isBlank(apiKey)) {
            return null;
        } else {
            String url = this.urlManager.getTests(APP_KEY, apiKey);
            logger.warn("Getting testLists via URL=" + url);
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
            try {
                String r = jo.get("response_code").toString();
                if (r.equals("200")) {
                    JSONArray arr = (JSONArray) jo.get("tests");
                    testListOrdered = new LinkedHashMap<String, String>(arr.length());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject en = null;
                        try {
                            en = arr.getJSONObject(i);
                        } catch (JSONException e) {
                            logger.warn("Error with the JSON while populating test list, ", e);
                        }
                        String id;
                        String name;
                        try {
                            if (en != null) {
                                id = en.getString("test_id");
                                name = en.getString("test_name").replaceAll("&", "&amp;");
                                testListOrdered.put(name, id);

                            }
                        } catch (JSONException ie) {
                            logger.warn("Error with the JSON while populating test list, ", ie);
                        }
                    }
                }
            } catch (Exception e) {
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

    void setBlazeMeterURL(String blazeMeterURL) {
        this.urlManager.setServerUrl(blazeMeterURL);
    }

    @Override
    public String getBlazeMeterURL() {
        return this.urlManager.getServerUrl();
    }

    @Override
    public JSONObject getTresholds(String sessionId) {
        return not_implemented;
    }

    @Override
    public JSONObject getTestInfo(String testId) {
        return not_implemented;
    }

    @Override
    public JSONObject updateTestInfo(String testId, JSONObject data) {
        return not_implemented;
    }

    @Override
    public String retrieveJUNITXML(String sessionId) {
        return Constants.NOT_IMPLEMENTED;
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
    public JSONObject createTest(JSONObject data) {
        return not_implemented;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public BmUrlManager getUrlManager() {
        return null;
    }

    @Override
    public JSONObject retrieveJTLZIP(String sessionId) {
        return null;
    }
}
