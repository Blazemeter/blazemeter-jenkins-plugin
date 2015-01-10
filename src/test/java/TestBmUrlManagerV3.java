import hudson.plugins.blazemeter.api.urlmanager.BmUrlManager;
import hudson.plugins.blazemeter.api.urlmanager.UrlManagerFactory;
import hudson.plugins.blazemeter.utils.Constants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 9/01/15.
 */

public class TestBmUrlManagerV3 {
    private String QA_BLAZEMETER_URL="http://qa.blazemeter.com";
    private String userKey="881a84b35e97c4342bf11";
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="123456789";
    private String sessionId="987654321";
    private BmUrlManager bmUrlManager=
            UrlManagerFactory.getURLFactory().getURLManager(UrlManagerFactory.ApiVersion.v3,
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

    public void testStatus(String appKey, String userKey, String testId){
    }

    @Test
    public void getTests(){
    String expGetTestsUrl=bmUrlManager.getServerUrl()+"/api/latest/tests?api_key="+userKey+
            "&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
    String actGetTestsUrl=bmUrlManager.getTests(appKey,userKey);
        Assert.assertEquals(expGetTestsUrl, actGetTestsUrl);
    }

    public String scriptUpload(String appKey, String userKey, String testId, String fileName){
        return appKey;
    }

    public String fileUpload(String appKey, String userKey, String testId, String fileName){
        return appKey;
    }

    @Test
    public void testStart(){
        String expTestStart=bmUrlManager.getServerUrl()+"/api/latest/tests/"
                +testId+"/start?api_key="+userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;

        String actTestStart=bmUrlManager.testStart(appKey, userKey, testId);
        Assert.assertEquals(expTestStart,actTestStart);
    }

    @Test
    public void testStop(){
        String expTestStop=bmUrlManager.getServerUrl()+"/api/latest/tests/"
                +testId+"/stop?api_key="+userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;

        String actTestStop=bmUrlManager.testStop(appKey, userKey, testId);
        Assert.assertEquals(expTestStop,actTestStop);
    }


    @Test
    public void testReport(){
        String expTestReport=bmUrlManager.getServerUrl()+"/api/latest/sessions/"
                +sessionId+"/reports/main/summary?api_key="+userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actTestReport=bmUrlManager.testReport(appKey, userKey, sessionId);
        Assert.assertEquals(expTestReport,actTestReport);

    }

    @Test
    public void getUser(){
        String expGetUser=bmUrlManager.getServerUrl()+"/api/latest/user?api_key="+userKey+
                "&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actGetUser=bmUrlManager.getUser(appKey, userKey);
        Assert.assertEquals(expGetUser,actGetUser);
    }

    public String getTresholds(String appKey, String userKey, String sessionId){
        return appKey;
    }

    public String getTestInfo(String appKey, String userKey, String testId){
        return appKey;
    }

    public String putTestInfo(String appKey, String userKey, String testId){
        return appKey;
    }

    public String createYahooTest(String appKey, String userKey){
        return appKey;
    }

    public String createTest(String appKey, String userKey){
        return appKey;
    }

    public String retrieveJUNITXML(String appKey, String userKey, String sessionId){
        return appKey;
    }

}
