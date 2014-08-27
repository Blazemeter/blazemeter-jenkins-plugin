package hudson.plugins.blazemeter.api;

import hudson.ProxyConfiguration;
import hudson.model.Hudson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.mail.MessagingException;
import javax.servlet.ServletException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: Vitali
 * Date: 4/2/12
 * Time: 14:05
 * <p/>
 * Updated
 * User: Doron
 * Date: 8/7/12
 *
 * Updated (proxy)
 * User: Marcel
 * Date: 9/23/13

 */

public class BlazemeterApi {
    PrintStream logger = new PrintStream(System.out);

    public class TestStatus {
        public static final String Running = "Running";
        public static final String NotRunning = "Not Running";
        public static final String NotFound = "NotFound";
        public static final String Error = "error";
    }

    public static final String APP_KEY = "jnk100x987c06f4e10c4";
    DefaultHttpClient httpClient;
    BmUrlManager urlManager;

    public BlazemeterApi() {
        urlManager = new BmUrlManager("https://a.blazemeter.com");
        try {
            httpClient = new DefaultHttpClient();
            configureProxy();
        } catch (Exception ex) {
            logger.format("error Instantiating HTTPClient. Exception received: %s", ex);
        }
    }

    private void configureProxy(){
    	if (Hudson.getInstance() != null && Hudson.getInstance().proxy != null) {
    		ProxyConfiguration proxy = Hudson.getInstance().proxy;
        	// Configure the proxy if necessary
            if (proxy.name != null && !proxy.name.isEmpty() && proxy.port > 0) {
                if (proxy.getUserName() != null && !proxy.getUserName().isEmpty()){
                	Credentials cred = new UsernamePasswordCredentials(proxy.getUserName(), proxy.getPassword());
                    httpClient.getCredentialsProvider().setCredentials(new AuthScope(proxy.name, proxy.port), cred);
                }
                HttpHost proxyHost = new HttpHost(proxy.name, proxy.port);
                httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
            }    	
        } 
    }
    
    private HttpResponse getResponse(String url, JSONObject data) throws IOException {

        logger.println("Requesting : " + url);
        HttpResponse response = null;

        try {
            HttpPost postRequest = new HttpPost(url);
            postRequest.setHeader("Accept", "application/json");
            postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

            if (data != null) {
                postRequest.setEntity(new StringEntity(data.toString()));
            }

            response = this.httpClient.execute(postRequest);


            if (response == null || response.getStatusLine() == null) {
                logger.format("Erroneous response (Probably null) for url: %s", url);
                response = null;
            }
        } catch (Exception e) {
            logger.format("Wrong response: %s\n", e);
        }
        return response;
    }

    private HttpResponse getResponseForFileUpload(String url, File file) throws IOException {

        logger.println("Requesting : " + url);
        HttpResponse response = null;

        try {
            HttpPost postRequest = new HttpPost(url);
            postRequest.setHeader("Accept", "application/json");
            postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

            if (file != null) {
                postRequest.setEntity(new FileEntity(file, "text/plain; charset=\"UTF-8\""));
            }

            response = this.httpClient.execute(postRequest);

            if (response != null && response.getStatusLine() != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                String error = response.getStatusLine().getReasonPhrase();
                if ((statusCode >= 300) || (statusCode < 200)) {
                    throw new RuntimeException(String.format("Failed : %d %s", statusCode, error));
                }
            } else {
                logger.format("Erroneous response (Probably null) for url: %s", url);
                response = null;
            }
        } catch (Exception e) {
            logger.format("Wrong response: %s\n", e);
        }
        return response;
    }


    private JSONObject getJsonForFileUpload(String url, File file) {
        JSONObject jo = null;
        try {
            HttpResponse response = getResponseForFileUpload(url, file);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                logger.println(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            logger.println("error decoding Json " + e);
        } catch (JSONException e) {
            logger.println("error decoding Json " + e);
        }
        return jo;
    }

    private JSONObject getJson(String url, JSONObject data) {
        JSONObject jo = null;
        try {
            HttpResponse response = getResponse(url, data);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                logger.println(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            logger.println("error decoding Json " + e);
        } catch (JSONException e) {
            logger.println("error decoding Json " + e);
        }
        return jo;
    }


    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @param file     - jmx file
     *                 //     * @return test id
     *                 //     * @throws java.io.IOException
     *                 //     * @throws org.json.JSONException
     */
    public synchronized void uploadJmx(String userKey, String testId, File file) {

        if (!validate(userKey, testId)) return;

        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, file.getName());
        JSONObject json = getJsonForFileUpload(url, file);
        try {
            if (!json.get("response_code").equals(200)) {
                logger.println("Could not upload file " + file.getName() + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            logger.println("Could not upload file " + file.getName() + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @param file     - the file (Java class) you like to upload
     * @return test id
     *         //     * @throws java.io.IOException
     *         //     * @throws org.json.JSONException
     */

    public synchronized JSONObject uploadBinaryFile(String userKey, String testId, File file) {

        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, file.getName());

        return getJsonForFileUpload(url, file);
    }



    private String getFileContents(String fn) {

        // ...checks on aFile are elided
        StringBuilder contents = new StringBuilder();
        File aFile = new File(fn);

        try {

            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile));

            try {
                String line;    // not declared within while loop

                /*
                 *         readLine is a bit quirky : it returns the content of a line
                 *         MINUS the newline. it returns null only for the END of the
                 *         stream. it returns an empty String if two newlines appear in
                 *         a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ignored) {
        }

        return contents.toString();
    }



    public TestInfo getTestRunStatus(String userKey, String testId) {
        TestInfo ti = new TestInfo();

        if (!validate(userKey, testId)) {
            ti.status = TestStatus.NotFound;
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId);
            JSONObject jo = getJson(url, null);

            if ("Test not found".equals(jo.get("error"))) {
                ti.status = TestStatus.NotFound;
            } else {
                ti.id = jo.getString("test_id");
                ti.name = jo.getString("test_name");
                ti.status = jo.getString("status");
            }
        } catch (Exception e) {
            logger.println("error getting status " + e);
            ti.status = TestStatus.Error;
        }
        return ti;
    }

    public synchronized JSONObject startTest(String userKey, String testId) {

        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        return getJson(url, null);
    }

    public int getTestCount(String userKey) throws JSONException, IOException, ServletException {
        if (userKey == null || userKey.trim().isEmpty()) {
            logger.println("getTests userKey is empty");
            return 0;
        }

        String url = getUrlForTestList(APP_KEY, userKey);

        try {
            JSONObject jo = getJson(url, null);
            if (jo == null){
                return -1;
            }
            else {
                String r = jo.get("response_code").toString();
                if (!r.equals("200")) {
                    return 0;
                }
                JSONArray arr = (JSONArray) jo.get("tests");
                return arr.length();
            }
        } catch (JSONException e) {
            logger.println("Error getting response from server: ");
            e.printStackTrace();
            return -1;
        }
    }


    private String getUrlForTestList(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("https://a.blazemeter.com/api/rest/blazemeter/getTests.json/?app_key=%s&user_key=%s&test_id=all", appKey, userKey);

    }

    private boolean validate(String userKey, String testId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            logger.println("startTest userKey is empty");
            return false;
        }

        if (testId == null || testId.trim().isEmpty()) {
            logger.println("testId is empty");
            return false;
        }
        return true;
    }

    /**
     * @param userKey - user key
     * @param testId  - test id
     *                //     * @throws IOException
     *                //     * @throws ClientProtocolException
     */
    public JSONObject stopTest(String userKey, String testId) {
        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        return getJson(url, null);
    }

    /**
     * @param userKey  - user key
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    public JSONObject aggregateReport(String userKey, String reportId) {
        if (!validate(userKey, reportId)) return null;

        String url = this.urlManager.testAggregateReport(APP_KEY, userKey, reportId);
        return getJson(url, null);
    }

    public HashMap<String, String> getTestList(String userKey) throws IOException, MessagingException {

        LinkedHashMap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
            logger.println("getTests userKey is empty");
        } else {
            String url = getUrlForTestList(APP_KEY, userKey);
            logger.println(url);
            JSONObject jo = getJson(url, null);
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
                            logger.println("Error with the JSON while populating test list, " + e);
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
                            logger.println("Error with the JSON while populating test list, " + ie);
                        }
                    }
                }
            }
            catch (Exception e) {
                logger.println("Error while populating test list, " + e);
            }
        }

        return testListOrdered;
    }

    public static class BmUrlManager {

        private String SERVER_URL = "https://a.blazemeter.com/";
        private String CLIENT_IDENTIFICATION="_clientId=CI_JENKINS&_clientVersion=1.08-1-SNAPSHOT​";

        public BmUrlManager(String blazeMeterUrl) {
            SERVER_URL = blazeMeterUrl;
        }

        public String getServerUrl() {
            return SERVER_URL;
        }

        public String testStatus(String appKey, String userKey, String testId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("https://a.blazemeter.com/api/rest/blazemeter/testGetStatus.json/?app_key=%s&user_key=%s&test_id=%s&"+CLIENT_IDENTIFICATION, appKey, userKey, testId);
        }


        public String scriptUpload(String appKey, String userKey, String testId, String fileName) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("https://a.blazemeter.com/api/rest/blazemeter/testScriptUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&"+CLIENT_IDENTIFICATION, appKey, userKey, testId, fileName);
        }

        public String fileUpload(String appKey, String userKey, String testId, String fileName) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("https://a.blazemeter.com/api/rest/blazemeter/testArtifactUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&"+CLIENT_IDENTIFICATION, appKey, userKey, testId, fileName);
        }

        public String testStart(String appKey, String userKey, String testId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("https://a.blazemeter.com/api/rest/blazemeter/testStart.json/?app_key=%s&user_key=%s&test_id=%s&"+CLIENT_IDENTIFICATION, appKey, userKey, testId);
        }

        public String testStop(String appKey, String userKey, String testId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("https://a.blazemeter.com/api/rest/blazemeter/testStop.json/?app_key=%s&user_key=%s&test_id=%s&"+CLIENT_IDENTIFICATION, appKey, userKey, testId);
        }


        public String testAggregateReport(String appKey, String userKey, String reportId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                reportId = URLEncoder.encode(reportId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("https://a.blazemeter.com/api/rest/blazemeter/testGetReport.json/?app_key=%s&user_key=%s&report_id=%s&get_aggregate=true&"+CLIENT_IDENTIFICATION, appKey, userKey, reportId);
        }
    }
}
