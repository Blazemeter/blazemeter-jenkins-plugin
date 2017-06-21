/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package hudson.plugins.blazemeter.api.urlmanager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class UrlManagerV3Impl implements UrlManager {

    private String serverUrl = "";
    private String NO_URL="";
    public UrlManagerV3Impl(String blazeMeterUrl) {
        this.serverUrl = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String masterStatus(String appKey, String masterId) {
        String testStatus=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        testStatus= serverUrl + V4 +UrlManager.MASTERS+"/"+masterId+"/status?events=false&app_key="+appKey+ CLIENT_IDENTIFICATION;
         return testStatus;
    }

    @Override
    public String tests(String appKey) {
        String getTests = null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        getTests = serverUrl + V4 + "/web/tests?app_key=" + appKey + CLIENT_IDENTIFICATION;

        return getTests;
    }

    @Override
    public String testStart(String appKey, String testId) {
        String testStart=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        testStart= serverUrl + V4 +"/tests/"
                +testId+"/start?app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testStart;
    }

    @Override
    public String collectionStart(String appKey, String collectionId) {
        String testStart=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            collectionId = URLEncoder.encode(collectionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        testStart= serverUrl + V4 +"/collections/"
                +collectionId+"/start?app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testStart;
    }

    @Override
    public String testStop(String appKey, String masterId) {
         String testStop=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        testStop= serverUrl + V4 +UrlManager.MASTERS+"/"
                +masterId+"/stop?app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testStop;
    }

    @Override
    public String testTerminate(String appKey, String masterId) {
        String testTerminate=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        testTerminate= serverUrl + V4 +UrlManager.MASTERS+"/"
                +masterId+"/terminate?app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testTerminate;
    }

    @Override
    public String testReport(String appKey, String masterId) {
        String testAggregateReport=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        testAggregateReport= serverUrl + V4 +UrlManager.MASTERS+"/"
                +masterId+"/reports/main/summary?app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testAggregateReport;
    }

    @Override
    public String getUser(String appKey) {
        String getUser=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        getUser= serverUrl + V4 +"/user?app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getUser;
    }

    @Override
    public String getCIStatus(String appKey, String masterId){
        String getTresholds=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        getTresholds= serverUrl + V4 +MASTERS+"/"+masterId+UrlManager.CI_STATUS+"?app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTresholds;

    }

    @Override
    public String retrieveJUNITXML(String appKey, String masterId) {
        String retrieveJUNITXML=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }

        retrieveJUNITXML= serverUrl + V4 +MASTERS+"/"+masterId+
                "/reports/thresholds?format=junit&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return retrieveJUNITXML;
    }


    @Override
    public String retrieveJTLZIP(String appKey, String sessionId) {
        String retrieveJTLZIP=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        retrieveJTLZIP= serverUrl + V4 +UrlManager.SESSIONS+"/"+sessionId+
                "/reports/logs?app_key="+appKey+ CLIENT_IDENTIFICATION;

        return retrieveJTLZIP;
    }

    @Override
    public String generatePublicToken(String appKey, String masterId) {
        String generatePublicToken = null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        generatePublicToken = serverUrl + V4 + MASTERS + "/" + masterId +
            "/publicToken?app_key=" + appKey + CLIENT_IDENTIFICATION;

        return generatePublicToken;
    }

    @Override
    public String listOfSessionIds(String appKey, String masterId) {
        String listOfSessionIds = null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        listOfSessionIds = serverUrl + V4 + MASTERS + "/" + masterId +
            UrlManager.SESSIONS + "?app_key=" + appKey + CLIENT_IDENTIFICATION;

        return listOfSessionIds;
    }

    @Override
    public String activeTests(String appKey) {
        String activeTests=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return activeTests= serverUrl + V4 +WEB+"/active?app_key="+appKey+ CLIENT_IDENTIFICATION;

    }

    @Override
    public String version(String appKey) {
        String version=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return version= serverUrl + V4 +WEB+"/version?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String masterId(String appKey,String masterId) {
        String masterIdUrl=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return masterIdUrl= serverUrl + V4 +UrlManager.MASTERS+"/"+masterId+"?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String properties(String appKey, String sessionId) {
        String properties=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return properties= serverUrl + V4 +UrlManager.SESSIONS+"/"+sessionId+"/properties?target=all&app_key="+appKey+ CLIENT_IDENTIFICATION;
    }
}

