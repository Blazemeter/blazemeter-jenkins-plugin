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
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="123456789";
    private String masterId ="987654321";
    private String sessionId ="r-v3-57230c5251da9";
    private UrlManager bmUrlManager=new UrlManagerV3Impl(TestConstants.mockedApiUrl);

    @Test
    public void getServerUrl(){
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void testStatus(){
        String expTestGetStatus=bmUrlManager.getServerUrl()+UrlManager.V4 +UrlManager.MASTERS+"/"
                + masterId +"/status?events=false&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actTestGetStatus=bmUrlManager.masterStatus(appKey, masterId);
        Assert.assertEquals(expTestGetStatus, actTestGetStatus);
    }

    @Test
    public void getTests(){
    String expGetTestsUrl=bmUrlManager.getServerUrl()+UrlManager.V4+"/web/tests?app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
    String actGetTestsUrl=bmUrlManager.tests(appKey);
        Assert.assertEquals(expGetTestsUrl, actGetTestsUrl);
    }


    @Test
    public void testStop_masters(){
        String expTestStop=bmUrlManager.getServerUrl()+UrlManager.V4 +UrlManager.MASTERS+"/"
                +testId+"/stop?app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;

        String actTestStop=bmUrlManager.testStop(appKey, testId);
        Assert.assertEquals(expTestStop,actTestStop);
    }

    @Test
    public void testTerminate_masters(){
        String expTestTerminate=bmUrlManager.getServerUrl()+UrlManager.V4 +UrlManager.MASTERS+"/"
                +testId+"/terminate?app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;

        String actTestTerminate=bmUrlManager.testTerminate(appKey, testId);
        Assert.assertEquals(expTestTerminate, actTestTerminate);
    }

    @Test
    public void testReport(){
        String expTestReport=bmUrlManager.getServerUrl()+UrlManager.V4 +UrlManager.MASTERS+"/"
                + masterId +"/reports/main/summary?app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actTestReport=bmUrlManager.testReport(appKey, masterId);
        Assert.assertEquals(expTestReport, actTestReport);

    }

    @Test
    public void getUser(){
        String expGetUser=bmUrlManager.getServerUrl()+UrlManager.V4 +"/user?app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actGetUser=bmUrlManager.getUser(appKey);
        Assert.assertEquals(expGetUser,actGetUser);
    }


    @Test
    public void getCIStatus(){
        String expCIStatus=bmUrlManager.getServerUrl()+UrlManager.V4 +UrlManager.MASTERS+"/"+ masterId +UrlManager.CI_STATUS
                +"?app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actCIStatus=bmUrlManager.getCIStatus(appKey, masterId);
        Assert.assertEquals(expCIStatus,actCIStatus);
    }

    @Test
    public void retrieveJUNITXML(){
        String expRetrieveJUNITXML=bmUrlManager.getServerUrl()+UrlManager.V4 +UrlManager.MASTERS+"/"+ masterId +
                "/reports/thresholds?format=junit&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actRetrieveJUNITXML=bmUrlManager.retrieveJUNITXML(appKey, masterId);
        Assert.assertEquals(expRetrieveJUNITXML,actRetrieveJUNITXML);
    }

    @Test
    public void generatePublicToken_masters(){
        String expGenPublicToken=bmUrlManager.getServerUrl()+UrlManager.V4 +UrlManager.MASTERS+"/"+ masterId +
                "/public-token?app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actGenPublicToken=bmUrlManager.generatePublicToken(appKey, masterId);
        Assert.assertEquals(expGenPublicToken,actGenPublicToken);
    }

    @Test
    public void listOfSessions(){
        String expListOfSessionIds=bmUrlManager.getServerUrl()+UrlManager.V4 +UrlManager.MASTERS+"/"+ masterId +
                "/sessions?app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actListOfSessionsIds=bmUrlManager.listOfSessionIds(appKey, masterId);
        Assert.assertEquals(expListOfSessionIds,actListOfSessionsIds);
    }

    @Test
    public void masterId(){
        String expMasterId=bmUrlManager.getServerUrl()+ UrlManager.V4 +UrlManager.MASTERS+"/"+masterId+"?app_key="+appKey+
                UrlManager.CLIENT_IDENTIFICATION;;
        String actMasterId=bmUrlManager.masterId(appKey,masterId);
        Assert.assertEquals(expMasterId,actMasterId);
    }

    @Test
    public void properties(){
        String expProperties=bmUrlManager.getServerUrl()+ UrlManager.V4 +"/sessions/"+sessionId+"/properties?target=all&app_key="+appKey+
                UrlManager.CLIENT_IDENTIFICATION;;
        String actProperties=bmUrlManager.properties(appKey,sessionId);
        Assert.assertEquals(expProperties,actProperties);
    }

}
