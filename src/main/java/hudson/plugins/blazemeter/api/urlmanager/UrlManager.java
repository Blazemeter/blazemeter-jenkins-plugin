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

import hudson.plugins.blazemeter.utils.JobUtility;


public interface UrlManager {

    String CLIENT_IDENTIFICATION = "&_clientId=CI_JENKINS&_clientVersion="
            + JobUtility.version();

    String LATEST="/api/latest";
    String TESTS="/tests";
    String MASTERS="/masters";
    String WEB="/web";
    String CI_STATUS="/ci-status";
    String getServerUrl();

    void setServerUrl(String serverUrl);

    String masterStatus(String appKey, String userKey, String testId);

    String tests(String appKey, String userKey);

    String activeTests(String appKey, String userKey);

    String masterId(String appKey,String userKey, String masterId);

    String testStart(String appKey, String userKey, String testId);

    String collectionStart(String appKey, String userKey, String collectionId);

    String testStop(String appKey, String userKey, String testId);

    String testTerminate(String appKey, String userKey, String testId);

    String testReport(String appKey, String userKey, String reportId);

    String getUser(String appKey, String userKey);

    String getCIStatus(String appKey, String userKey, String sessionId);

    String getTestConfig(String appKey, String userKey, String testId);

    String postJsonConfig(String appKey, String userKey, String testId);

    String createTest(String appKey, String userKey);

    String retrieveJUNITXML(String appKey, String userKey, String sessionId);

    String retrieveJTLZIP(String appKey, String userKey, String sessionId);

    String generatePublicToken(String appKey, String userKey, String sessionId);

    String listOfSessionIds(String appKey, String userKey, String masterId);

    String version(String appKey);

    String properties(String appKey, String userKey, String sessionId);
}

