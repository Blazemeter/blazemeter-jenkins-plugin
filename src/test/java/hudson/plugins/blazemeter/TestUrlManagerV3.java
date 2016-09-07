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

package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.urlmanager.UrlManager;
import hudson.plugins.blazemeter.api.urlmanager.UrlManagerV3Impl;
import org.junit.Assert;
import org.junit.Test;

public class TestUrlManagerV3 {
    private String userKey="881a84b35e97c4342bf11";
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="123456789";
    private String masterId ="987654321";
    private String sessionId ="r-v3-57230c5251da9";
    private String fileName="111111111";
    private UrlManager bmUrlManager=new UrlManagerV3Impl(TestConstants.mockedApiUrl);

    @Test
    public void getServerUrl(){
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void setServerUrl(){
        bmUrlManager.setServerUrl(TestConstants.mockedApiUrl);
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void testStatus(){
        String expTestGetStatus=bmUrlManager.getServerUrl()+"/api/latest/masters/"
                + masterId +"/status?events=false&api_key="+userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actTestGetStatus=bmUrlManager.masterStatus(appKey, userKey, masterId);
        Assert.assertEquals(expTestGetStatus, actTestGetStatus);
    }

    @Test
    public void getTests(){
    String expGetTestsUrl=bmUrlManager.getServerUrl()+"/api/web/tests?api_key="+userKey+
            "&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
    String actGetTestsUrl=bmUrlManager.tests(appKey, userKey);
        Assert.assertEquals(expGetTestsUrl, actGetTestsUrl);
    }


    @Test
    public void testStop_masters(){
        String expTestStop=bmUrlManager.getServerUrl()+"/api/latest/masters/"
                +testId+"/stop?api_key="+userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;

        String actTestStop=bmUrlManager.testStop(appKey, userKey, testId);
        Assert.assertEquals(expTestStop,actTestStop);
    }

    @Test
    public void testTerminate_masters(){
        String expTestTerminate=bmUrlManager.getServerUrl()+"/api/latest/masters/"
                +testId+"/terminate?api_key="+userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;

        String actTestTerminate=bmUrlManager.testTerminate(appKey, userKey, testId);
        Assert.assertEquals(expTestTerminate, actTestTerminate);
    }

    @Test
    public void testReport(){
        String expTestReport=bmUrlManager.getServerUrl()+"/api/latest/masters/"
                + masterId +"/reports/main/summary?api_key="+userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actTestReport=bmUrlManager.testReport(appKey, userKey, masterId);
        Assert.assertEquals(expTestReport, actTestReport);

    }

    @Test
    public void getUser(){
        String expGetUser=bmUrlManager.getServerUrl()+"/api/latest/user?api_key="+userKey+
                "&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actGetUser=bmUrlManager.getUser(appKey, userKey);
        Assert.assertEquals(expGetUser,actGetUser);
    }


    @Test
    public void getCIStatus(){
        String expCIStatus=bmUrlManager.getServerUrl()+"/api/latest/masters/"+ masterId +"/ci-status?api_key="
                +userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actCIStatus=bmUrlManager.getCIStatus(appKey, userKey, masterId);
        Assert.assertEquals(expCIStatus,actCIStatus);
    }

    @Test
    public void getTestInfo(){
        String expGetTestInfo=bmUrlManager.getServerUrl()+"/api/latest/tests/"+testId+"?api_key="+userKey+"&app_key="+appKey
                + UrlManager.CLIENT_IDENTIFICATION;
        String actGetTestInfo=bmUrlManager.testConfig(appKey, userKey, testId);
        Assert.assertEquals(expGetTestInfo,actGetTestInfo);
    }

    @Test
    public void postJsonConfig(){
        String expPutTestInfo=bmUrlManager.getServerUrl()+"/api/latest/tests/"+testId+
                "/custom?custom_test_type=yahoo&api_key="+userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actPutTestInfo=bmUrlManager.postJsonConfig(appKey, userKey, testId);
        Assert.assertEquals(expPutTestInfo,actPutTestInfo);
    }

    @Test
    public void createTest(){
        String expCreateTest=bmUrlManager.getServerUrl()+"/api/latest/tests/custom?custom_test_type=yahoo&api_key="
                +userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actCreateTest=bmUrlManager.createTest(appKey, userKey);
        Assert.assertEquals(expCreateTest,actCreateTest);
    }

    @Test
    public void retrieveJUNITXML(){
        String expRetrieveJUNITXML=bmUrlManager.getServerUrl()+"/api/latest/masters/"+ masterId +
                "/reports/thresholds?format=junit&api_key="
                +userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actRetrieveJUNITXML=bmUrlManager.retrieveJUNITXML(appKey, userKey, masterId);
        Assert.assertEquals(expRetrieveJUNITXML,actRetrieveJUNITXML);
    }

    @Test
    public void generatePublicToken_masters(){
        String expGenPublicToken=bmUrlManager.getServerUrl()+"/api/latest/masters/"+ masterId +
                "/publicToken?api_key="
                +userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actGenPublicToken=bmUrlManager.generatePublicToken(appKey, userKey, masterId);
        Assert.assertEquals(expGenPublicToken,actGenPublicToken);
    }

    @Test
    public void listOfSessions(){
        String expListOfSessionIds=bmUrlManager.getServerUrl()+"/api/latest/masters/"+ masterId +
                "/sessions?api_key="+userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actListOfSessionsIds=bmUrlManager.listOfSessionIds(appKey, userKey, masterId);
        Assert.assertEquals(expListOfSessionIds,actListOfSessionsIds);
    }

    @Test
    public void activeTests(){
        String expActiveTests=bmUrlManager.getServerUrl()+"/api/latest/web/active?api_key="
                +userKey+"&app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actActiveTests=bmUrlManager.activeTests(appKey, userKey);
        Assert.assertEquals(expActiveTests,actActiveTests);
    }

    @Test
    public void version(){
        String expVersion=bmUrlManager.getServerUrl()+ UrlManager.LATEST+
                UrlManager.WEB+"/version?app_key="+appKey+ UrlManager.CLIENT_IDENTIFICATION;
        String actVersion=bmUrlManager.version(appKey);
        Assert.assertEquals(expVersion,actVersion);
    }

    @Test
    public void masterId(){
        String expMasterId=bmUrlManager.getServerUrl()+ UrlManager.LATEST+"/masters/"+masterId+"?api_key="+userKey+"&app_key="+appKey+
                UrlManager.CLIENT_IDENTIFICATION;;
        String actMasterId=bmUrlManager.masterId(appKey,userKey,masterId);
        Assert.assertEquals(expMasterId,actMasterId);
    }

    @Test
    public void properties(){
        String expProperties=bmUrlManager.getServerUrl()+ UrlManager.LATEST+"/sessions/"+sessionId+"/properties?target=all&api_key="+userKey+"&app_key="+appKey+
                UrlManager.CLIENT_IDENTIFICATION;;
        String actProperties=bmUrlManager.properties(appKey,userKey,sessionId);
        Assert.assertEquals(expProperties,actProperties);
    }

}
