package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApiV3Impl;
import hudson.plugins.blazemeter.api.BzmHttpWrapper;
import hudson.plugins.blazemeter.entities.TestStatus;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestBlazemeterApiV3Impl {
    private Logger log = LogManager.getLogManager().getLogger("TEST");
    private BlazemeterApiV3Impl blazemeterApiV3 =null;
    private BzmHttpWrapper bzmHttpWrapper= Mockito.mock(BzmHttpWrapper.class);
    private String userKey="1234567890";
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="12345";

    @Test
    public void createTest_null(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
        Assert.assertEquals(blazemeterApiV3.createTest(null), null);
    }

    @Test
    public void retrieveJUNITXML_null(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
        Assert.assertEquals(blazemeterApiV3.retrieveJUNITXML(null), null);
    }


    @Test
    public void getTresholds_null(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
        Assert.assertEquals(blazemeterApiV3.getTresholds(null), null);
    }


    @Test
    public void updateTestInfo_null(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
        Assert.assertEquals(blazemeterApiV3.updateTestInfo(null, null), null);
    }

    @Test
    public void getTestInfo_null(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
        Assert.assertEquals(blazemeterApiV3.getTestInfo(null), null);
    }

    @Test
    public void getUser_null(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
        Assert.assertEquals(blazemeterApiV3.getUser(), null);
    }
 
    @Test
    public void getTestList_null(){
        try {
            APIFactory apiFactory=APIFactory.getApiFactory();
            apiFactory.setVersion(APIFactory.ApiVersion.v3);
            blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
            Assert.assertEquals(blazemeterApiV3.getTestList(), null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getTestCount_zero(){
        try {
            APIFactory apiFactory=APIFactory.getApiFactory();
            apiFactory.setVersion(APIFactory.ApiVersion.v3);
            blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
            Assert.assertEquals(blazemeterApiV3.getTestCount(), 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testReport_null(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
        Assert.assertEquals(blazemeterApiV3.testReport(null), null);
    }

   @Test
    public void stopTest_null(){
       APIFactory apiFactory=APIFactory.getApiFactory();
       apiFactory.setVersion(APIFactory.ApiVersion.v3);
       blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
       Assert.assertEquals(blazemeterApiV3.stopTest(null), null);
    }

   @Test
    public void startTest_null(){
       APIFactory apiFactory=APIFactory.getApiFactory();
       apiFactory.setVersion(APIFactory.ApiVersion.v3);
       blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
       Assert.assertEquals(blazemeterApiV3.startTest(null), null);
    }


   @Test
    public void getTestRunStatus_notFound(){
       APIFactory apiFactory=APIFactory.getApiFactory();
       apiFactory.setVersion(APIFactory.ApiVersion.v3);
       blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
       Assert.assertEquals(blazemeterApiV3.getTestRunStatus(null).getStatus(), TestStatus.NotFound);
    }

    @Test
    public void uploadBinaryFile_null(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(null);
        Assert.assertEquals(blazemeterApiV3.uploadBinaryFile(null, null), null);
    }

    @Test
    public void getTestsList(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(userKey);
        blazemeterApiV3.setBzmHttpWr(bzmHttpWrapper);
        String url = blazemeterApiV3.getUrlManager().getTests(appKey,userKey);
        try {
            blazemeterApiV3.getTestList();
            Mockito.verify(bzmHttpWrapper).getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
        } catch (IOException e) {
            log.info(e.getMessage());
        } catch (MessagingException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void getTestsCount(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(userKey);
        blazemeterApiV3.setBzmHttpWr(bzmHttpWrapper);
        String url = blazemeterApiV3.getUrlManager().getTests(appKey,userKey);
        try {
            blazemeterApiV3.getTestCount();
            Mockito.verify(bzmHttpWrapper).getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
        } catch (IOException e) {
            log.info(e.getMessage());
        } catch (ServletException se) {
            log.info(se.getMessage());
        } catch (JSONException je) {
            log.info(je.getMessage());
        }
    }

    @Test
    public void getTestRunStatus(){
        APIFactory apiFactory=APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApiV3=(BlazemeterApiV3Impl)apiFactory.getAPI(userKey);
        blazemeterApiV3.setBzmHttpWr(bzmHttpWrapper);
        String url = blazemeterApiV3.getUrlManager().getTestInfo(appKey, userKey, testId);
        try {
            blazemeterApiV3.getTestInfo(testId);
            Mockito.verify(bzmHttpWrapper).getResponseAsJson(url, null, BzmHttpWrapper.Method.GET);
        } catch (Exception e) {
            log.info(e.getMessage());
       } 
    }
}