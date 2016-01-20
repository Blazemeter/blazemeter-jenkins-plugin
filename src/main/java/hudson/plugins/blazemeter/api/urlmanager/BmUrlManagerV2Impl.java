package hudson.plugins.blazemeter.api.urlmanager;

import hudson.plugins.blazemeter.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dzmitrykashlach on 10/11/14.
 */
public class BmUrlManagerV2Impl implements BmUrlManager {

    private String serverUrl = "";

    BmUrlManagerV2Impl(String blazeMeterUrl) {
        this.serverUrl = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String masterStatus(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%s/api/rest/blazemeter/testGetStatus.json/?app_key=%s&user_key=%s&test_id=%s&",
                serverUrl,appKey, userKey, testId)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String tests(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%s/api/rest/blazemeter/tests.json/?app_key=%s&user_key=%s&test_id=all",
                serverUrl, appKey, userKey)+ CLIENT_IDENTIFICATION;

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
                serverUrl,appKey, userKey, testId, fileName)+ CLIENT_IDENTIFICATION;
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
                serverUrl,appKey, userKey, testId, fileName)+ CLIENT_IDENTIFICATION;
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
                serverUrl,appKey, userKey, testId)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String collectionStart(String appKey, String userKey, String collectionId) {
        return Constants.NOT_IMPLEMENTED;
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
                serverUrl,appKey, userKey, testId)+ CLIENT_IDENTIFICATION;
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
        testAggregateReport= serverUrl +"/api/latest/sessions/"
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
                serverUrl, appKey, userKey)+ CLIENT_IDENTIFICATION;
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.serverUrl =serverUrl;
    }

    @Override
    public String getCIStatus(String appKey, String userKey, String sessionId) {
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
        generatePublicToken= serverUrl +"/api/latest/sessions/"+sessionId+
                "/publicToken?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return generatePublicToken;
    }

    @Override
    public String testTerminate(String appKey, String userKey, String testId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String listOfSessionIds(String appKey, String userKey, String masterId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String activeTests(String appKey, String userKey) {
        return Constants.NOT_IMPLEMENTED;
    }
}

