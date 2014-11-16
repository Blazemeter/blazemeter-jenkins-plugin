package hudson.plugins.blazemeter.api.urlmanager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by dzmitrykashlach on 10/11/14.
 */
public class BmUrlManagerV3Impl implements BmUrlManager {

    private String SERVER_URL = "https://a.blazemeter.com/";
    private static String CLIENT_IDENTIFICATION = "_clientId=CI_JENKINS&_clientVersion=1.1.0&​";

    static{
        try{
            CLIENT_IDENTIFICATION= URLEncoder.encode(CLIENT_IDENTIFICATION, "UTF-8");
            CLIENT_IDENTIFICATION=CLIENT_IDENTIFICATION.substring(0,47);
            CLIENT_IDENTIFICATION= URLDecoder.decode(CLIENT_IDENTIFICATION, "UTF-8");

        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    BmUrlManagerV3Impl(String blazeMeterUrl) {
        SERVER_URL = blazeMeterUrl;
    }

    public String getServerUrl() {
        return SERVER_URL;
    }

    public String testStatus(String appKey, String userKey, String sessionId) {
        String testStatus=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            sessionId = URLEncoder.encode(sessionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStatus=SERVER_URL+"/api/latest/sessions/"+sessionId+"?api_key="+userKey+"&app_key="+appKey+CLIENT_IDENTIFICATION;
         return testStatus;
    }

    public String getTests(String appKey, String userKey) {
        String getTests=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTests=SERVER_URL+"/api/latest/tests?api_key="+userKey+"&app_key="+appKey+CLIENT_IDENTIFICATION;

        return getTests;
    }

    public String scriptUpload(String appKey, String userKey, String testId, String fileName) {
        String scriptUpload=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return scriptUpload;
    }

    public String fileUpload(String appKey, String userKey, String testId, String fileName) {
        String fileUpload=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fileUpload;
    }

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
                +testId+"/start?api_key="+userKey+"&app_key="+appKey+CLIENT_IDENTIFICATION;

        return testStart;
    }

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
                +testId+"/stop?api_key="+userKey+"&app_key="+appKey+CLIENT_IDENTIFICATION;

        return testStop;
    }


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
                +sessionId+"/reports/main/summary?api_key="+userKey+"&app_key="+appKey+CLIENT_IDENTIFICATION;

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
        getUser=SERVER_URL+"/api/latest/user?api_key="+userKey+"&app_key="+appKey+CLIENT_IDENTIFICATION;

        return getUser;
    }
}

