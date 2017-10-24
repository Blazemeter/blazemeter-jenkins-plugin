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

package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.urlmanager.UrlManager;
import hudson.plugins.blazemeter.api.urlmanager.UrlManagerV3Impl;
import org.junit.Assert;
import org.junit.Test;

public class TestUrlManagerV3 {
    private final String appKey = "jnk100x987c06f4e10c4";
    private final String testId = "123456789";
    private final String masterId = "987654321";
    private final String sessionId = "r-v3-57230c5251da9";
    private final UrlManager bmUrlManager = new UrlManagerV3Impl(TestConstants.mockedApiUrl);

    @Test
    public void getServerUrl(){
        Assert.assertTrue(this.bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void testStatus(){
        String expTestGetStatus = this.bmUrlManager.getServerUrl() + UrlManager.V4 + UrlManager.MASTERS + "/"
                + this.masterId + "/status?events=false&app_key=" + this.appKey + UrlManager.CLIENT_IDENTIFICATION;
        String actTestGetStatus = this.bmUrlManager.masterStatus(this.appKey, this.masterId);
        Assert.assertEquals(expTestGetStatus, actTestGetStatus);
    }

    @Test
    public void testStop_masters(){
        String expTestStop = this.bmUrlManager.getServerUrl() + UrlManager.V4 + UrlManager.MASTERS + "/"
                + this.testId + "/stop?app_key=" + this.appKey + UrlManager.CLIENT_IDENTIFICATION;

        String actTestStop = this.bmUrlManager.testStop(this.appKey, this.testId);
        Assert.assertEquals(expTestStop,actTestStop);
    }

    @Test
    public void testTerminate_masters(){
        String expTestTerminate = this.bmUrlManager.getServerUrl() + UrlManager.V4 + UrlManager.MASTERS + "/"
                + this.testId + "/terminate?app_key=" + this.appKey + UrlManager.CLIENT_IDENTIFICATION;

        String actTestTerminate = this.bmUrlManager.testTerminate(this.appKey, this.testId);
        Assert.assertEquals(expTestTerminate, actTestTerminate);
    }

    @Test
    public void testReport(){
        String expTestReport = this.bmUrlManager.getServerUrl() + UrlManager.V4 + UrlManager.MASTERS + "/"
                + this.masterId + "/reports/main/summary?app_key=" + this.appKey + UrlManager.CLIENT_IDENTIFICATION;
        String actTestReport = this.bmUrlManager.testReport(this.appKey, this.masterId);
        Assert.assertEquals(expTestReport, actTestReport);

    }

    @Test
    public void getUser(){
        String expGetUser = this.bmUrlManager.getServerUrl() + UrlManager.V4 + "/user?app_key=" + this.appKey + UrlManager.CLIENT_IDENTIFICATION;
        String actGetUser = this.bmUrlManager.getUser(this.appKey);
        Assert.assertEquals(expGetUser,actGetUser);
    }


    @Test
    public void getCIStatus(){
        String expCIStatus = this.bmUrlManager.getServerUrl() + UrlManager.V4 + UrlManager.MASTERS + "/" + this.masterId + UrlManager.CI_STATUS
                + "?app_key=" + this.appKey + UrlManager.CLIENT_IDENTIFICATION;
        String actCIStatus = this.bmUrlManager.getCIStatus(this.appKey, this.masterId);
        Assert.assertEquals(expCIStatus,actCIStatus);
    }

    @Test
    public void retrieveJUNITXML(){
        String expRetrieveJUNITXML = this.bmUrlManager.getServerUrl() + UrlManager.V4 + UrlManager.MASTERS + "/" + this.masterId +
                "/reports/thresholds?format=junit&app_key=" + this.appKey + UrlManager.CLIENT_IDENTIFICATION;
        String actRetrieveJUNITXML = this.bmUrlManager.retrieveJUNITXML(this.appKey, this.masterId);
        Assert.assertEquals(expRetrieveJUNITXML,actRetrieveJUNITXML);
    }

    @Test
    public void generatePublicToken_masters(){
        String expGenPublicToken = this.bmUrlManager.getServerUrl() + UrlManager.V4 + UrlManager.MASTERS + "/" + this.masterId +
                "/public-token?app_key=" + this.appKey + UrlManager.CLIENT_IDENTIFICATION;
        String actGenPublicToken = this.bmUrlManager.generatePublicToken(this.appKey, this.masterId);
        Assert.assertEquals(expGenPublicToken,actGenPublicToken);
    }

    @Test
    public void listOfSessions(){
        String expListOfSessionIds = this.bmUrlManager.getServerUrl() + UrlManager.V4 + UrlManager.MASTERS + "/" + this.masterId +
                "/sessions?app_key=" + this.appKey + UrlManager.CLIENT_IDENTIFICATION;
        String actListOfSessionsIds = this.bmUrlManager.listOfSessionIds(this.appKey, this.masterId);
        Assert.assertEquals(expListOfSessionIds,actListOfSessionsIds);
    }

    @Test
    public void masterId(){
        String expMasterId = this.bmUrlManager.getServerUrl() + UrlManager.V4 + UrlManager.MASTERS + "/" + this.masterId + "?app_key=" + this.appKey +
                UrlManager.CLIENT_IDENTIFICATION;
        String actMasterId = this.bmUrlManager.masterId(this.appKey, this.masterId);
        Assert.assertEquals(expMasterId,actMasterId);
    }

    @Test
    public void properties() {
        String expProperties = this.bmUrlManager.getServerUrl() + UrlManager.V4 + "/sessions/" + this.sessionId + "/properties?target=all&app_key=" + this.appKey +
                UrlManager.CLIENT_IDENTIFICATION;
        String actProperties = this.bmUrlManager.properties(this.appKey, this.sessionId);
        Assert.assertEquals(expProperties, actProperties);
    }

    @Test
    public void projectId() {
        Assert.fail();
    }

    @Test
    public void workspaceId() {
        Assert.fail();
    }

}
