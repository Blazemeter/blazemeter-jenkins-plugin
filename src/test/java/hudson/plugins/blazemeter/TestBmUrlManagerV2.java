package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.ApiVersion;
import hudson.plugins.blazemeter.api.urlmanager.BmUrlManager;
import hudson.plugins.blazemeter.api.urlmanager.UrlManagerFactory;
import hudson.plugins.blazemeter.utils.Constants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 9/01/15.
 */

public class TestBmUrlManagerV2 {
    private String QA_BLAZEMETER_URL="http://qa.blazemeter.com";
    private String userKey="881a84b35e97c4342bf11";
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="123456789";
    private String sessionId="987654321";
    private String reportId="1212121212";
    private String fileName="111111111";
    private BmUrlManager bmUrlManager=
            UrlManagerFactory.getURLManager(ApiVersion.v2,
            Constants.DEFAULT_BLAZEMETER_URL);

    @Test
    public void getServerUrl(){
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(Constants.DEFAULT_BLAZEMETER_URL));
    }

    @Test
    public void setServerUrl(){
        bmUrlManager.setServerUrl(QA_BLAZEMETER_URL);
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(QA_BLAZEMETER_URL));
    }

    @Test
    public void testStatus(){
        String expTestGetStatus=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/testGetStatus.json/?app_key="
                +appKey+"&user_key="+userKey+"&test_id="+testId+"&"+BmUrlManager.CLIENT_IDENTIFICATION;
        String actTestGetStatus=bmUrlManager.testMasterStatus(appKey, userKey, testId);
        Assert.assertEquals(expTestGetStatus, actTestGetStatus);
    }

    @Test
    public void getTests(){
    String expGetTestsUrl=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/getTests.json/?app_key="+appKey+
            "&user_key="+userKey+"&test_id=all"+BmUrlManager.CLIENT_IDENTIFICATION;
    String actGetTestsUrl=bmUrlManager.getTests(appKey,userKey);
        Assert.assertEquals(expGetTestsUrl, actGetTestsUrl);
    }

    @Test
    public void scriptUpload(){
        String expScriptUpload=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/testScriptUpload.json/?app_key="+appKey+"&user_key="+userKey+"&test_id="+testId+"&file_name="+fileName+"&"+BmUrlManager.CLIENT_IDENTIFICATION;
        String actScriptUpload=bmUrlManager.scriptUpload(appKey, userKey, testId, fileName);
        Assert.assertEquals(expScriptUpload, actScriptUpload);
    }

    @Test
    public void fileUpload(){
        String expFileUpload=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/testArtifactUpload.json/?app_key="+appKey+"&user_key="+userKey+"&test_id="+testId+"&file_name="+fileName+"&"+BmUrlManager.CLIENT_IDENTIFICATION;
        String actFileLoad=bmUrlManager.fileUpload(appKey, userKey, testId, fileName);
        Assert.assertEquals(expFileUpload, actFileLoad);
    }

    @Test
    public void testStart(){
        String expTestStart=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/testStart.json/?app_key="
                +appKey+"&user_key="+userKey+"&test_id="+testId+"&"+BmUrlManager.CLIENT_IDENTIFICATION;
        String actTestStart=bmUrlManager.testStart(appKey, userKey, testId);
        Assert.assertEquals(expTestStart,actTestStart);
    }

    @Test
    public void testStop(){
        String expTestStop=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/testStop.json/?app_key="
                +appKey+"&user_key="+userKey+"&test_id="+testId+"&"+BmUrlManager.CLIENT_IDENTIFICATION;

        String actTestStop=bmUrlManager.testStop(appKey, userKey, testId);
        Assert.assertEquals(expTestStop,actTestStop);
    }


    @Test
    public void testReport(){
        String expTestReport=bmUrlManager.getServerUrl()+"/api/latest/sessions/"+reportId+"/reports/main/summary?api_key="
                +userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actTestReport=bmUrlManager.testReport(appKey, userKey, reportId);
        Assert.assertEquals(expTestReport,actTestReport);
    }

    @Test
    public void getUser(){
        String expGetUser=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/getUserInfo/?app_key="+appKey+
                "&user_key="+userKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actGetUser=bmUrlManager.getUser(appKey, userKey);
        Assert.assertEquals(expGetUser,actGetUser);
    }


    @Test
    public void getTresholds(){
        String expGetTresholds=Constants.NOT_IMPLEMENTED;
        String actGetTresholds=bmUrlManager.getTresholds(appKey, userKey, sessionId);
        Assert.assertEquals(expGetTresholds,actGetTresholds);
    }

    @Test
    public void getTestInfo(){
        String expGetTestInfo=Constants.NOT_IMPLEMENTED;
        String actGetTestInfo=bmUrlManager.getTestConfig(appKey, userKey, testId);
        Assert.assertEquals(expGetTestInfo,actGetTestInfo);
    }

    @Test
    public void putTestInfo(){
        String expPutTestInfo=Constants.NOT_IMPLEMENTED;
        String actPutTestInfo=bmUrlManager.postJsonConfig(appKey, userKey, testId);
        Assert.assertEquals(expPutTestInfo,actPutTestInfo);
    }

    @Test
    public void createTest(){
        String expCreateTest=Constants.NOT_IMPLEMENTED;
        String actCreateTest=bmUrlManager.createTest(appKey, userKey);
        Assert.assertEquals(expCreateTest,actCreateTest);
    }

    @Test
    public void retrieveJUNITXML(){
        String expRetrieveJUNITXML=Constants.NOT_IMPLEMENTED;
        String actRetrieveJUNITXML=bmUrlManager.retrieveJUNITXML(appKey, userKey, sessionId);
        Assert.assertEquals(expRetrieveJUNITXML,actRetrieveJUNITXML);
    }

}
