package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.ApiVersion;
import hudson.plugins.blazemeter.api.BlazemeterApiV2Impl;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.Constants;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestBlazemeterApiV2Impl {
    BlazemeterApiV2Impl blazemeterApiV2 =null;

    @Before
    public void setUp(){
    blazemeterApiV2=(BlazemeterApiV2Impl)APIFactory.getAPI(null, ApiVersion.v2,Constants.DEFAULT_BLAZEMETER_URL);
    }

    @Test
    public void createTest(){
        Assert.assertEquals(blazemeterApiV2.createTest(null), BlazemeterApiV2Impl.not_implemented);
    }

    @Test
    public void retrieveJUNITXML(){
        Assert.assertEquals(blazemeterApiV2.retrieveJUNITXML(null), Constants.NOT_IMPLEMENTED);
    }


    @Test
    public void getTresholds(){
        Assert.assertEquals(blazemeterApiV2.getTresholds(null), BlazemeterApiV2Impl.not_implemented);
    }

    @Test
    public void updateTestInfo(){
        Assert.assertEquals(blazemeterApiV2.postJsonConfig(null, null), BlazemeterApiV2Impl.not_implemented);
    }

    @Test
    public void getTestInfo(){
        Assert.assertEquals(blazemeterApiV2.getTestConfig(null), BlazemeterApiV2Impl.not_implemented);
    }

    @Test
    public void getUser(){
        Assert.assertEquals(blazemeterApiV2.getUser(), null);
    }
 
    @Test
    public void getTestList(){
        try {
            Assert.assertEquals(blazemeterApiV2.getTestsMultiMap(), null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getTestCount(){
        try {
            Assert.assertEquals(blazemeterApiV2.getTestCount(), 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testReport(){
        Assert.assertEquals(blazemeterApiV2.testReport(null), null);
    }

   @Test
    public void stopTest(){
        Assert.assertEquals(blazemeterApiV2.stopTest(null), null);
    }

   @Test
    public void startTest(){
       try {
           Assert.assertEquals(blazemeterApiV2.startTest(null), null);
       } catch (JSONException e) {
           e.printStackTrace();
       }
   }


   @Test
    public void getTestRunStatus(){
        Assert.assertEquals(blazemeterApiV2.getTestStatus(null), TestStatus.NotFound);
    }

    @Test
    public void uploadBinaryFile(){
        Assert.assertEquals(blazemeterApiV2.uploadBinaryFile(null, null), null);
    }

}