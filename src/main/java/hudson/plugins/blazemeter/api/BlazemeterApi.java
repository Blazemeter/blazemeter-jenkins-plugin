package hudson.plugins.blazemeter.api;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.mail.util.LineOutputStream;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.mail.MessagingException;

/**
 * User: Vitali
 * Date: 4/2/12
 * Time: 14:05
 * <p/>
 * Updated - Platform independent
 * User: moshe
 * Date: 5/12/12
 * Time: 1:05 PM
 * <p/>
 * Updated - Minor fixes
 * User: Doron
 * Date: 8/7/12
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


    public BlazemeterApi(String blazeMeterUrl) {
        urlManager = new BmUrlManager(blazeMeterUrl);
        try {
            //logger = new PrintStream(new FileOutputStream("/Users/moshe/tests/jenkins.log"));
            httpClient = new DefaultHttpClient();
        } catch (Exception ex) {
            logger.format("error Instantiating HTTPClient. Exception received: %s", ex);
        }
    }

    private HttpResponse getResponse(String url, JSONObject data) throws IOException {

        logger.println("Requesting : " + url);
        HttpPost postRequest = new HttpPost(url);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

        if (data != null) {
            postRequest.setEntity(new StringEntity(data.toString()));
        }

        HttpResponse response = null;
        try {
            response = this.httpClient.execute(postRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if ((statusCode >= 300) || (statusCode < 200)) {
                throw new RuntimeException(String.format("Failed : %d %s", statusCode, error));
            }
        } catch (Exception e) {
            System.err.format("Wrong response: %s", e);
        }

        return response;
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

    //    public synchronized void getReports(String userKey, String id) throws IOException, JSONException {
//        String url = this.urlManager.testReport(APP_KEY, userKey, id);
//
//        JSONObject jo = getJson(url, null);
//        ArrayList<JSONObject> arr = (ArrayList<JSONObject>) jo.get("reports");
//        HashMap<String, String> rpt = new HashMap<String, String>();
//
//        for (JSONObject en : arr) {
//            String zipurl = (String) en.get("zip_url");
//            String date = (String) en.get("date");
//            String test_url = (String) en.get("url");
//            String title = (String) en.get("title");
//
//            if (rpt.containsKey(id)) {
//                rpt.put("title", title);
//                rpt.put("date", date);
//                rpt.put("url", url);
//                rpt.put("zipurl", zipurl);
//            }
//
//            System.out.format("zip URL " + zipurl);
//            System.out.format("Date of  Test Run " + date);
//            System.out.format("URL For  Test" + test_url);
//            System.out.format("Title" + title);
//        }
//    }

//    public interface TestContainerNotifier {
//        public void testReceived(ArrayList<TestInfo> tests);
//    }


//    public synchronized void getTestsAsync(String userKey, TestContainerNotifier notifier) {
//        class TestsFetcher implements Runnable {
//            String userKey;
//            TestContainerNotifier notifier;
//
//            TestsFetcher(String userKey, TestContainerNotifier notifier) {
//                this.userKey = userKey;
//                this.notifier = notifier;
//            }
//
//            public void run() {
//                ArrayList<TestInfo> tests = getTests(userKey);
//                notifier.testReceived(tests);
//            }
//        }
//        new Thread(new TestsFetcher(userKey, notifier)).start();
//    }

//    public synchronized ArrayList<TestInfo> getTests(String userKey) {
//        if (userKey.trim().isEmpty()) {
//            logger.println("getTests userKey is empty");
//            return null;
//        }
//
//        String url = this.urlManager.getTests(APP_KEY, userKey, "all");
//
//        JSONObject jo = getJson(url, null);
//        JSONArray arr;
//        try {
//            String r = jo.get("response_code").toString();
//            if (!r.equals("200"))
//                return null;
//            arr = (JSONArray) jo.get("tests");
//        } catch (JSONException e) {
//            return null;
//        }
//
//
//        ArrayList<TestInfo> tests = new ArrayList<TestInfo>();
//        for (int i = 0; i < arr.length(); i++) {
//            JSONObject en;
//            try {
//                en = arr.getJSONObject(i);
//            } catch (JSONException e) {
//                System.err.format(e.getMessage());
//                continue;
//            }
//            String id = null;
//            String name = null;
//            try {
//                id = en.getString("test_id");
//                name = en.getString("test_name");
//            } catch (JSONException ignored) {
//            }
//            TestInfo testInfo = new TestInfo();
//            testInfo.name = name;
//            testInfo.id = id;
//            tests.add(testInfo);
//        }
//        return tests;
//    }

//    public synchronized TestInfo createTest(String userKey, String testName) {
//        if (userKey == null || userKey.trim().isEmpty()) {
//            logger.println("createTest userKey is empty");
//            return null;
//        }
//
//        String url = this.urlManager.scriptCreation(APP_KEY, userKey, testName);
//        JSONObject jo = getJson(url, null);
//        TestInfo ti = new TestInfo();
//        try {
//            if (jo.isNull("error")) {
//                ti.id = jo.getString("test_id");
//                ti.name = jo.getString("test_name");
//            } else {
//                ti.status = jo.getString("error");
//            }
//        } catch (JSONException e) {
//            ti.status = "error";
//        }
//        return ti;
//    }

//    public synchronized JSONObject getTestReport(String userKey, String session) {
//        if (userKey == null || userKey.trim().isEmpty()) {
//            logger.println("createTest userKey is empty");
//            return null;
//        }
//
//        String url = this.urlManager.testReport(APP_KEY, userKey, session);
//        JSONObject jo = getJson(url, null);
//        return jo;
//    }

    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @param fileName - test name
     * @param pathName - jmx file path
//     * @return test id
//     * @throws java.io.IOException
//     * @throws org.json.JSONException
     */
    public synchronized void uploadJmx(String userKey, String testId, String fileName, String pathName) {

        if (!validate(userKey, testId)) return;

        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = getFileContents(pathName);

        try {
            jmxData.put("data", fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
        }

        getJson(url, jmxData);
    }

    /**
     *
     * @param userKey  - user key
     * @param testId   - test id
     * @param fileName - name for file you like to upload
     * @param pathName - to the file you like to upload
     * @return test id
//     * @throws java.io.IOException
//     * @throws org.json.JSONException
     */
    public synchronized JSONObject uploadFile(String userKey, String testId, String fileName, String pathName) {

        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = getFileContents(pathName);

        try {
            jmxData.put("data", fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
        }

        return getJson(url, jmxData);
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


//    public synchronized int dataUpload(String userKey, String testId, String reportName, String buff, String dataType) {
//
//        if (!validate(userKey, testId)) return -1;
//
//        Integer fileSize = -1;
//
//        reportName = reportName.trim().isEmpty() ? "sample" : reportName;
//        if (dataType.equals("jtl"))
//            reportName = reportName.toLowerCase().endsWith(".jtl") ? reportName : reportName + ".jtl";
//
//        String url = this.urlManager.testResultsJTLUpload(APP_KEY, userKey, testId, reportName, dataType);
//
//        JSONObject obj = new JSONObject();
//        try {
//            obj.put("data", buff);
//            JSONObject jo = getJson(url, obj);
//            fileSize = (Integer) jo.get("file_size");
//
//        } catch (JSONException e) {
//            System.err.format(e.getMessage());
//        }
//        return fileSize;
//    }

    public TestInfo getTestRunStatus(String userKey, String testId) {
        TestInfo ti = new TestInfo();

        if (!validate(userKey, testId)) {
            ti.status = TestStatus.NotFound;
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId);
            JSONObject jo = getJson(url, null);

            if (jo.get("status") == "Test not found")
                ti.status = TestStatus.NotFound;
            else {
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

public synchronized ArrayList<TestInfo> getTests(String userKey) throws 	JSONException, IOException {
        if (userKey.trim().isEmpty()) {
            logger.println("getTests userKey is empty");
            return null;
        }

        String url =getUrlForTestList(APP_KEY, userKey);

        JSONObject jo = getJson(url, null);
        JSONArray arr;
        try {
            String r = jo.get("response_code").toString();
            if (!r.equals("200"))
                return null;
            arr = (JSONArray) jo.get("tests");
        } catch (JSONException e) {
            return null;
        }

        FileOutputStream fc = new FileOutputStream("src/main/resources/hudson/plugins/blazemeter/PerformancePublisher/testList.jelly");
        LineOutputStream li = new LineOutputStream(fc);
        try {
			li.writeln("<j:jelly xmlns:j=\"jelly:core\" xmlns:st=\"jelly:stapler\"   xmlns:d=\"jelly:define\"    " +  
						"xmlns:l=\"/lib/layout\" xmlns:t=\"/lib/hudson\"   xmlns:f=\"/lib/form\"   xmlns:x=\"jelly:xml\"   xmlns:html=\"jelly:html\">"+
							"<f:entry name=\"testList\" title=\"Choose Test from List\" field=\"tests\">" +
								"<select name=\"testId\">" );
		} catch (MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        ArrayList<TestInfo>  testList = new ArrayList<TestInfo>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject en;
            try {
                en = arr.getJSONObject(i);
            } catch (JSONException e) {
                System.err.format(e.getMessage());
                continue;
            }
            String id = null;
            String name = null;
            try {
                id = en.getString("test_id");
                name = en.getString("test_name");
              
            } catch (JSONException ignored) {
            }
            TestInfo testInfo = new TestInfo();
            testInfo.name = name;
            testInfo.id = id;
            try {
				li.writeln("<option value=\"" + id + "\">" + name +  "</option>");
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            testList.add(testInfo);
        }
        try {
			li.writeln("</select></f:entry></j:jelly>");
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        li.close();
        
        return testList;
    }
 

    private String getUrlForTestList(String appKey, String userKey) {
		// TODO Auto-generated method stub
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
     *
     * @param userKey - user key
     * @param testId  - test id
//     * @throws IOException
//     * @throws ClientProtocolException
     */
    public JSONObject stopTest(String userKey, String testId) {
        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        return getJson(url, null);
    }

    /**
     *
     * @param userKey - user key
     * @param reportId  - report Id same as Session Id, can be obtained from start stop status.
//     * @throws IOException
//     * @throws ClientProtocolException
     */
    public JSONObject aggregateReport(String userKey, String reportId) {
        if (!validate(userKey, reportId)) return null;

        String url = this.urlManager.testAggregateReport(APP_KEY, userKey, reportId);
        return getJson(url, null);
    }

    
    
    public static class BmUrlManager {

        private String SERVER_URL;

//        public BmUrlManager() {
//            this("https://a.blazemeter.com");
//        }

        public BmUrlManager(String blazeMeterUrl) {
            SERVER_URL = blazeMeterUrl;
            //logger.println("Server url is :" + SERVER_URL);

        }

//        public String getServerUrl() {
//            return SERVER_URL;
//        }

        public String testStatus(String appKey, String userKey, String testId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("%s/api/rest/blazemeter/testGetStatus.json/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
        }

//        public String scriptCreation(String appKey, String userKey, String testName) {
//            try {
//                appKey = URLEncoder.encode(appKey, "UTF-8");
//                userKey = URLEncoder.encode(userKey, "UTF-8");
//                testName = URLEncoder.encode(testName, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            return String.format("%s/api/rest/blazemeter/testCreate.json/?app_key=%s&user_key=%s&test_name=%s", SERVER_URL, appKey, userKey, testName);
//        }

        public String scriptUpload(String appKey, String userKey, String testId, String fileName) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("%s/api/rest/blazemeter/testScriptUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s", SERVER_URL, appKey, userKey, testId, fileName);
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
            return String.format("%s/api/rest/blazemeter/testArtifactUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s", SERVER_URL, appKey, userKey, testId, fileName);
        }

        public String testStart(String appKey, String userKey, String testId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("%s/api/rest/blazemeter/testStart.json/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
        }

        public String testStop(String appKey, String userKey, String testId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                testId = URLEncoder.encode(testId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("%s/api/rest/blazemeter/testStop.json/?app_key=%s&user_key=%s&test_id=%s", SERVER_URL, appKey, userKey, testId);
        }

//        public String testReport(String appKey, String userKey, String reportId) {
//            try {
//                appKey = URLEncoder.encode(appKey, "UTF-8");
//                userKey = URLEncoder.encode(userKey, "UTF-8");
//                reportId = URLEncoder.encode(reportId, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            return String.format("%s/api/rest/blazemeter/testGetArchive.json/?app_key=%s&user_key=%s&report_id=%s", SERVER_URL, appKey, userKey, reportId);
//        }

//        public String testResultsJTLUpload(String appKey, String userKey, String testId, String fileName, String dataType) {
//            try {
//                appKey = URLEncoder.encode(appKey, "UTF-8");
//                userKey = URLEncoder.encode(userKey, "UTF-8");
//                testId = URLEncoder.encode(testId, "UTF-8");
//                fileName = URLEncoder.encode(fileName, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            return String.format("%s/api/rest/blazemeter/testDataUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&data_type=%s", SERVER_URL, appKey, userKey, testId, fileName, dataType);
//        }

//        public String getTests(String appKey, String userKey, String type) {
//            try {
//                appKey = URLEncoder.encode(appKey, "UTF-8");
//                userKey = URLEncoder.encode(userKey, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//
//            return String.format("%s/api/rest/blazemeter/getTests.json/?app_key=%s&user_key=%s&type=%s", SERVER_URL, appKey, userKey, type);
//        }

        public String testAggregateReport(String appKey, String userKey, String reportId) {
            try {
                appKey = URLEncoder.encode(appKey, "UTF-8");
                userKey = URLEncoder.encode(userKey, "UTF-8");
                reportId = URLEncoder.encode(reportId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("%s/api/rest/blazemeter/testGetReport.json/?app_key=%s&user_key=%s&report_id=%s&get_aggregate=true", SERVER_URL, appKey, userKey, reportId);
        }
    }
}
