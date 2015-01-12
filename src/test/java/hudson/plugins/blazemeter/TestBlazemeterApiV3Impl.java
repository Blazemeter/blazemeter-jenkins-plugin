package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApiV3Impl;
import hudson.plugins.blazemeter.entities.TestStatus;
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
public class TestBlazemeterApiV3Impl {
    BlazemeterApiV3Impl blazemeterApiV3 =null;

    @Before
    public void setUp(){
    blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getApiFactory().getAPI(null);
    }

    @Test
    public void createTest(){
        Assert.assertEquals(blazemeterApiV3.createTest(null, null), null);
    }

    @Test
    public void retrieveJUNITXML(){
        Assert.assertEquals(blazemeterApiV3.retrieveJUNITXML(null), null);
    }


    @Test
    public void getTresholds(){
        Assert.assertEquals(blazemeterApiV3.getTresholds(null), null);
    }

    @Test
    public void createYahooTest(){
        Assert.assertEquals(blazemeterApiV3.createYahooTest(null), null);
    }

    @Test
    public void updateTestInfo(){
        Assert.assertEquals(blazemeterApiV3.updateTestInfo(null, null), null);
    }

    @Test
    public void getTestInfo(){
        Assert.assertEquals(blazemeterApiV3.getTestInfo(null), null);
    }

    @Test
    public void getUser(){
        Assert.assertEquals(blazemeterApiV3.getUser(), null);
    }
 
    @Test
    public void getTestList(){
        try {
            Assert.assertEquals(blazemeterApiV3.getTestList(), null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getTestCount(){
        try {
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
    public void testReport(){
        Assert.assertEquals(blazemeterApiV3.testReport(null), null);
    }

   @Test
    public void stopTest(){
        Assert.assertEquals(blazemeterApiV3.stopTest(null), null);
    }

   @Test
    public void startTest(){
        Assert.assertEquals(blazemeterApiV3.startTest(null), null);
    }


   @Test
    public void getTestRunStatus(){
        Assert.assertEquals(blazemeterApiV3.getTestRunStatus(null).getStatus(), TestStatus.NotFound);
    }

    @Test
    public void uploadBinaryFile(){
        Assert.assertEquals(blazemeterApiV3.uploadBinaryFile(null, null), null);
    }

}