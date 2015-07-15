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

public class BlazemeterApiV2Impl implements BlazemeterApi {
    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);
    public static JSONObject not_implemented;
    private final String apiKey;
    BmUrlManager urlManager;
    private BzmHttpWrapper bzmhc = null;

    BlazemeterApiV2Impl(String apiKey,String blazemeterUrl) {
        this.apiKey = apiKey;
        urlManager = UrlManagerFactory.
                getURLManager(ApiVersion.v2, blazemeterUrl);
        not_implemented=new JSONObject();
        try {
            not_implemented.put(Constants.NOT_IMPLEMENTED,Constants.NOT_IMPLEMENTED);
            bzmhc = new BzmHttpWrapper();
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
            if (!json.get(JsonConstants.RESPONSE_CODE).equals(200)) {
                logger.warn("Could not upload file " + file.getName() + " " + json.get(JsonConstants.ERROR).toString());
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
    public TestInfo getTestInfo(String id) {
        TestInfo ti = new TestInfo();

        if (StringUtils.isBlank(apiKey)&StringUtils.isBlank(id)) {
            ti.setStatus(TestStatus.NotFound);
            return ti;
        }

        try {
            String url = this.urlManager.testSessionStatus(APP_KEY, apiKey, id);
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);

            if ("Test not found".equals(jo.get(JsonConstants.ERROR))) {
                ti.setStatus(TestStatus.NotFound);
            } else {
                ti.setId(jo.getString(JsonConstants.TEST_ID));
                ti.setName(jo.getString("test_name"));
                ti.setStatus(TestStatus.valueOf(jo.getString(JsonConstants.STATUS).equals("Not Running")?"NotRunning":jo.getString(JsonConstants.STATUS)));
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
        return this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
    }

    @Override
    public int getTestCount() throws JSONException, IOException, ServletException {
        if (StringUtils.isBlank(apiKey)) {
            return 0;
        }

        String url = this.urlManager.getTests(APP_KEY, apiKey);

        try {
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
            if (jo == null) {
                return -1;
            } else {
                String r = jo.get(JsonConstants.RESPONSE_CODE).toString();
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
    public LinkedHashMultimap<String, String> getTestsMultiMap() throws IOException, MessagingException {

        LinkedHashMultimap<String, String> testListOrdered = null;

        if (StringUtils.isBlank(apiKey)) {
            return null;
        } else {
            String url = this.urlManager.getTests(APP_KEY, apiKey);
            logger.warn("Getting testLists via URL=" + url);
            JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
            try {
                String r = jo.get(JsonConstants.RESPONSE_CODE).toString();
                if (r.equals("200")) {
                    JSONArray arr = (JSONArray) jo.get("tests");
                    testListOrdered = LinkedHashMultimap.create(arr.length(),arr.length());
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
                                id = en.getString(JsonConstants.TEST_ID);
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
    public JSONObject getTestConfig(String testId) {
        return not_implemented;
    }

    @Override
    public JSONObject postJsonConfig(String testId, JSONObject data) {
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
        return this.urlManager;
    }

    @Override
    public JSONObject retrieveJTLZIP(String sessionId) {
        return null;
    }

    @Override
    public JSONObject putTestInfo(String testId, JSONObject data) {
        return null;
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) {
        if(StringUtils.isBlank(apiKey)&StringUtils.isBlank(sessionId)) return null;

        String url = this.urlManager.generatePublicToken(APP_KEY, apiKey, sessionId);
        JSONObject jo = this.bzmhc.getResponseAsJson(url, null, BzmHttpWrapper.Method.POST);


        return jo;

    }

    @Override
    public void setBzmHttpWr(BzmHttpWrapper bzmhc) {
        this.bzmhc=bzmhc;
    }

    @Override
    public BzmHttpWrapper getBzmHttpWr() {
        return this.bzmhc;
    }

    @Override
    public int getTestSessionStatusCode(String id) {
        return -1;
    }

    @Override
    public JSONObject terminateTest(String testId) {
        return not_implemented;
    }

    @Override
    public JSONObject getTestsJSON() {
        return not_implemented;
    }
}
