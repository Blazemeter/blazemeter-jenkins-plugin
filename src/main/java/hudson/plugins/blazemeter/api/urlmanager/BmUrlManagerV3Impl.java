package hudson.plugins.blazemeter.api.urlmanager;

import hudson.plugins.blazemeter.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dzmitrykashlach on 10/11/14.
 */
public class BmUrlManagerV3Impl implements BmUrlManager {

    private String SERVER_URL = Constants.DEFAULT_BLAZEMETER_URL+"/";

    BmUrlManagerV3Impl(String blazeMeterUrl) {
        SERVER_URL = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return SERVER_URL;
    }

    @Override
    public String testStatus(String appKey, String userKey, String sessionId) {
        String testStatus=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            sessionId = URLEncoder.encode(sessionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStatus=SERVER_URL+"/api/latest/sessions/"+sessionId+"?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
         return testStatus;
    }

    @Override
    public String getTests(String appKey, String userKey) {
        String getTests=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTests=SERVER_URL+"/api/latest/tests?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTests;
    }

    @Override
    public String scriptUpload(String appKey, String userKey, String testId, String fileName) {
    return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String fileUpload(String appKey, String userKey, String testId, String fileName) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String testStart(String appKey, String userKey, String testId) {
        String testStart=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        testStart=SERVER_URL+"/api/latest/tests/"
                +testId+"/start?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testStart;
    }

    @Override
    public String testStop(String appKey, String userKey, String testId) {
         String testStop=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStop=SERVER_URL+"/api/latest/tests/"
                +testId+"/stop?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testStop;
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
        String getUser=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getUser=SERVER_URL+"/api/latest/user?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getUser;
    }

    @Override
    public String getTestInfo(String appKey, String userKey, String testId){
        String getTestInfo=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTestInfo=SERVER_URL+"/api/latest/tests/"+testId+"?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTestInfo;
    }

    @Override
    public String createTest(String appKey, String userKey) {
        String createTest=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        createTest=SERVER_URL+"/api/latest/tests/custom?custom_test_type=yahoo&api_key="
                +userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return createTest;
    }

    @Override
    public String getTresholds(String appKey, String userKey, String sessionId){
        String getTresholds=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTresholds=SERVER_URL+"/api/latest/sessions/"+sessionId+"/reports/thresholds?api_key="
                +userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTresholds;

    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.SERVER_URL=serverUrl;
    }

    @Override
    public String retrieveJUNITXML(String appKey, String userKey, String sessionId) {
        String retrieveJUNITXML=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        retrieveJUNITXML=SERVER_URL+"/api/latest/sessions/"+sessionId+
                "/reports/thresholds/data?format=junit&api_key="
                +userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return retrieveJUNITXML;
    }



    @Override
    public String postJsonConfig(String appKey, String userKey, String testId) {
        String getTestInfo=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTestInfo=SERVER_URL+"/api/latest/tests/"+testId+"/custom?custom_test_type=yahoo&api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTestInfo;
    }

    @Override
    public String retrieveJTLZIP(String appKey, String userKey, String sessionId) {
        String retrieveJTLZIP=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        retrieveJTLZIP=SERVER_URL+"/api/latest/sessions/"+sessionId+
                "/reports/logs?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return retrieveJTLZIP;
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
}

