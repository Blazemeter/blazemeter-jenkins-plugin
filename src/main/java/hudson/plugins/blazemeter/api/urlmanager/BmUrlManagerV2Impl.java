package hudson.plugins.blazemeter.api.urlmanager;

import hudson.plugins.blazemeter.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dzmitrykashlach on 10/11/14.
 */
public class BmUrlManagerV2Impl implements BmUrlManager {

    private String SERVER_URL = Constants.DEFAULT_BLAZEMETER_URL+"/";

    BmUrlManagerV2Impl(String blazeMeterUrl) {
        SERVER_URL = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return SERVER_URL;
    }

    @Override
    public String testStatus(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%sapi/rest/blazemeter/testGetStatus.json/?app_key=%s&user_key=%s&test_id=%s&",
                SERVER_URL,appKey, userKey, testId)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String getTests(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%sapi/rest/blazemeter/getTests.json/?app_key=%s&user_key=%s&test_id=all",
                SERVER_URL, appKey, userKey)+ CLIENT_IDENTIFICATION;

    }

    @Override
    public String scriptUpload(String appKey, String userKey, String testId, String fileName) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%sapi/rest/blazemeter/testScriptUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&",
                SERVER_URL,appKey, userKey, testId, fileName)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String fileUpload(String appKey, String userKey, String testId, String fileName) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%sapi/rest/blazemeter/testArtifactUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&",
                SERVER_URL,appKey, userKey, testId, fileName)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStart(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%sapi/rest/blazemeter/testStart.json/?app_key=%s&user_key=%s&test_id=%s&",
                SERVER_URL,appKey, userKey, testId)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStop(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%sapi/rest/blazemeter/testStop.json/?app_key=%s&user_key=%s&test_id=%s&",
                SERVER_URL,appKey, userKey, testId)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String testReport(String appKey, String userKey, String reportId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            reportId = URLEncoder.encode(reportId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%sapi/rest/blazemeter/testGetReport.json/?app_key=%s&user_key=%s&report_id=%s&get_aggregate=true&",
                SERVER_URL,appKey, userKey, reportId)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String getUser(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%sapi/rest/blazemeter/getUserInfo/?app_key=%s&user_key=%s",
                SERVER_URL, appKey, userKey)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.SERVER_URL=serverUrl;
    }

    @Override
    public String getTresholds(String appKey, String userKey, String sessionId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String getTestInfo(String appKey, String userKey, String testId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String createYahooTest(String appKey, String userKey) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String retrieveJUNITXML(String appKey, String userKey, String sessionId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String createTest(String appKey, String userKey) {
        return Constants.NOT_IMPLEMENTED;
    }
}

