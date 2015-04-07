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
    public String testSessionStatus(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%s/api/rest/blazemeter/testGetStatus.json/?app_key=%s&user_key=%s&test_id=%s&",
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
        return String.format("%s/api/rest/blazemeter/getTests.json/?app_key=%s&user_key=%s&test_id=all",
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
        return String.format("%s/api/rest/blazemeter/testScriptUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&",
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
        return String.format("%s/api/rest/blazemeter/testArtifactUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&",
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
        return String.format("%s/api/rest/blazemeter/testStart.json/?app_key=%s&user_key=%s&test_id=%s&",
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
        return String.format("%s/api/rest/blazemeter/testStop.json/?app_key=%s&user_key=%s&test_id=%s&",
                SERVER_URL,appKey, userKey, testId)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String testReport(String appKey, String userKey, String sessionId) {
        String testAggregateReport=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            sessionId = URLEncoder.encode(sessionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testAggregateReport=SERVER_URL+"/api/latest/sessions/"
                +sessionId+"/reports/main/summary?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testAggregateReport;
    }

    @Override
    public String getUser(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%s/api/rest/blazemeter/getUserInfo/?app_key=%s&user_key=%s",
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
    public String getTestConfig(String appKey, String userKey, String testId) {
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

    @Override
    public String postJsonConfig(String appKey, String userKey, String testId) {
        return Constants.NOT_IMPLEMENTED;
    }


    @Override
    public String retrieveJTLZIP(String appKey, String userKey, String sessionId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String generatePublicToken(String appKey, String userKey, String sessionId) {
        String generatePublicToken=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        generatePublicToken=SERVER_URL+"/api/latest/sessions/"+sessionId+
                "/publicToken?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return generatePublicToken;
    }

    @Override
    public String testTerminate(String appKey, String userKey, String testId) {
        return Constants.NOT_IMPLEMENTED;
    }
}

