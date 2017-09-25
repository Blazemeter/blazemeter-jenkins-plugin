/**
 Copyright 2017 BlazeMeter Inc.

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

import com.google.common.base.Charsets;

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
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
            masterId = URLEncoder.encode(masterId, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
         return serverUrl + V4 +UrlManager.MASTERS+"/"+masterId+"/status?events=false&app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String tests(String appKey) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 + "/web/tests?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStart(String appKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
            testId = URLEncoder.encode(testId, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +"/tests/"+testId+"/start?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String collectionStart(String appKey, String collectionId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
            collectionId = URLEncoder.encode(collectionId, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +"/collections/"+collectionId+"/start?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStop(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
            masterId = URLEncoder.encode(masterId, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +UrlManager.MASTERS+"/"+masterId+"/stop?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String testTerminate(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
            masterId = URLEncoder.encode(masterId, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +UrlManager.MASTERS+"/"+masterId+"/terminate?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String testReport(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
            masterId = URLEncoder.encode(masterId, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +UrlManager.MASTERS+"/"+masterId+"/reports/main/summary?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String getUser(String appKey) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +"/user?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String getCIStatus(String appKey, String masterId){
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +MASTERS+"/"+masterId+UrlManager.CI_STATUS+"?app_key="+appKey+ CLIENT_IDENTIFICATION;

    }

    @Override
    public String retrieveJUNITXML(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +MASTERS+"/"+masterId+"/reports/thresholds?format=junit&app_key="+appKey+ CLIENT_IDENTIFICATION;
    }


    @Override
    public String retrieveJTLZIP(String appKey, String sessionId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +UrlManager.SESSIONS+"/"+sessionId+"/reports/logs?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String generatePublicToken(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 + MASTERS + "/" + masterId + "/public-token?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String listOfSessionIds(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 + MASTERS + "/" + masterId + UrlManager.SESSIONS + "?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String masterId(String appKey,String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +UrlManager.MASTERS+"/"+masterId+"?app_key="+appKey+ CLIENT_IDENTIFICATION;
    }

    @Override
    public String properties(String appKey, String sessionId) {
        try {
            appKey = URLEncoder.encode(appKey, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return NO_URL;
        }
        return serverUrl + V4 +UrlManager.SESSIONS+"/"+sessionId+"/properties?target=all&app_key="+appKey+ CLIENT_IDENTIFICATION;
    }
}

