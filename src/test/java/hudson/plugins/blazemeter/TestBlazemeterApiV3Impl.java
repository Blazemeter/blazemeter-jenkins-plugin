package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApiV3Impl;
import hudson.plugins.blazemeter.api.BzmHttpWrapper;
import hudson.plugins.blazemeter.entities.TestStatus;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestBlazemeterApiV3Impl {
    BlazemeterApiV3Impl blazemeterApiV3 =null;
    private BzmHttpWrapper bzmHttpWrapper= Mockito.mock(BzmHttpWrapper.class);

    @Before
    public void setUp_null_apiKey(){
    blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getApiFactory().getAPI(null);
    }

    @Test
    public void createTest_null(){
        Assert.assertEquals(blazemeterApiV3.createTest(null, null), null);
    }

    @Test
    public void retrieveJUNITXML_null(){
        Assert.assertEquals(blazemeterApiV3.retrieveJUNITXML(null), null);
    }


    @Test
    public void getTresholds_null(){
        Assert.assertEquals(blazemeterApiV3.getTresholds(null), null);
    }

    @Test
    public void createYahooTest_null(){
        Assert.assertEquals(blazemeterApiV3.createYahooTest(null), null);
    }

    @Test
    public void updateTestInfo_null(){
        Assert.assertEquals(blazemeterApiV3.updateTestInfo(null, null), null);
    }

    @Test
    public void getTestInfo_null(){
        Assert.assertEquals(blazemeterApiV3.getTestInfo(null), null);
    }

    @Test
    public void getUser_null(){
        Assert.assertEquals(blazemeterApiV3.getUser(), null);
    }
 
    @Test
    public void getTestList_null(){
        try {
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
        Assert.assertEquals(blazemeterApiV3.testReport(null), null);
    }

   @Test
    public void stopTest_null(){
        Assert.assertEquals(blazemeterApiV3.stopTest(null), null);
    }

   @Test
    public void startTest_null(){
        Assert.assertEquals(blazemeterApiV3.startTest(null), null);
    }


   @Test
    public void getTestRunStatus_notFound(){
        Assert.assertEquals(blazemeterApiV3.getTestRunStatus(null).getStatus(), TestStatus.NotFound);
    }

    @Test
    public void uploadBinaryFile_null(){
        Assert.assertEquals(blazemeterApiV3.uploadBinaryFile(null, null), null);
    }

}