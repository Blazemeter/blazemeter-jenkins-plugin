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
    public UrlManagerV3Impl(String blazeMeterUrl) {
        this.serverUrl = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String masterStatus(String appKey, String userKey, String masterId) {
        String testStatus=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStatus= serverUrl +LATEST+"/masters/"+masterId+"/status?events=false&api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
         return testStatus;
    }

    @Override
    public String tests(String appKey, String userKey) {
        String getTests=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTests= serverUrl +"/api/web/tests?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTests;
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
        testStart= serverUrl +LATEST+"/tests/"
                +testId+"/start?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testStart;
    }

    @Override
    public String collectionStart(String appKey, String userKey, String collectionId) {
        String testStart=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            collectionId = URLEncoder.encode(collectionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStart= serverUrl +LATEST+"/collections/"
                +collectionId+"/start?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testStart;
    }

    @Override
    public String testStop(String appKey, String userKey, String masterId) {
         String testStop=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStop= serverUrl +LATEST+"/masters/"
                +masterId+"/stop?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testStop;
    }

    @Override
    public String testTerminate(String appKey, String userKey, String masterId) {
        String testTerminate=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testTerminate= serverUrl +LATEST+"/masters/"
                +masterId+"/terminate?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testTerminate;
    }

    @Override
    public String testReport(String appKey, String userKey, String masterId) {
        String testAggregateReport=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testAggregateReport= serverUrl +LATEST+"/masters/"
                +masterId+"/reports/main/summary?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

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
        getUser= serverUrl +LATEST+"/user?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getUser;
    }

    @Override
    public String getTestConfig(String appKey, String userKey, String testId){
        String getTestInfo=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTestInfo= serverUrl +LATEST+"/tests/"+testId+"?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

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
        createTest= serverUrl +LATEST+TESTS+"/custom?custom_test_type=yahoo&api_key="
                +userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return createTest;
    }

    @Override
    public String getCIStatus(String appKey, String userKey, String masterId){
        String getTresholds=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTresholds= serverUrl +LATEST+MASTERS+"/"+masterId+"/ci-status?api_key="
                +userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTresholds;

    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.serverUrl =serverUrl;
    }

    @Override
    public String retrieveJUNITXML(String appKey, String userKey, String masterId) {
        String retrieveJUNITXML=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        retrieveJUNITXML= serverUrl +LATEST+MASTERS+"/"+masterId+
                "/reports/thresholds?format=junit&api_key="
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
        getTestInfo= serverUrl +LATEST+TESTS+"/"+testId+"/custom?custom_test_type=yahoo&api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

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
        retrieveJTLZIP= serverUrl +LATEST+"/sessions/"+sessionId+
                "/reports/logs?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return retrieveJTLZIP;
    }

    @Override
    public String generatePublicToken(String appKey, String userKey, String masterId) {
        String generatePublicToken=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        generatePublicToken= serverUrl +LATEST+MASTERS+"/"+masterId+
                "/publicToken?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return generatePublicToken;
    }


    @Override
    public String listOfSessionIds(String appKey, String userKey, String masterId) {
        String listOfSessionIds=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        listOfSessionIds= serverUrl +LATEST+MASTERS+"/"+masterId+
                "/sessions?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return listOfSessionIds;
    }

    @Override
    public String activeTests(String appKey, String userKey) {
        String activeTests=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return activeTests= serverUrl +LATEST+WEB+"/active?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

    }

    @Override
    public String version(String appKey) {
        String version=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return version= serverUrl +LATEST+WEB+"/version?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String masterId(String appKey,String userKey,String masterId) {
        String masterIdUrl=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return masterIdUrl= serverUrl +LATEST+"/masters/"+masterId+"?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String properties(String appKey, String userKey, String sessionId) {
        String properties=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return properties= serverUrl +LATEST+"/sessions/"+sessionId+"/properties?target=all&api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
    }
}

