package hudson.plugins.blazemeter;

import java.io.*;
import hudson.plugins.blazemeter.api.BzmHttpWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by dzmitrykashlach on 13/01/15.
 */
public class TestBzmHttpWrapper {
    private Logger log = LogManager.getLogManager().getLogger("TEST");
    private BzmHttpWrapper bzmHttpWrapper= new BzmHttpWrapper();
    private DefaultHttpClient httpClient = Mockito.mock(DefaultHttpClient.class);
    private String url = "http://tut.by";
    private JSONObject data = null;
    private BzmHttpWrapper.Method method = BzmHttpWrapper.Method.GET;
    private File file = new File("TestBzmHttpWrapper.class");

    @Test
    public void getResponseAsJson(){
        BzmHttpWrapper spyBzmHttpWrapper=Mockito.spy(bzmHttpWrapper);
        spyBzmHttpWrapper.getResponseAsJson(url, data, method);
        try {
            Mockito.verify(spyBzmHttpWrapper).getHttpResponse(url, data, method);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void getResponseAsString(){
        BzmHttpWrapper spyBzmHttpWrapper=Mockito.spy(bzmHttpWrapper);
        spyBzmHttpWrapper.getResponseAsString(url, data, method);
        try {
            Mockito.verify(spyBzmHttpWrapper).getHttpResponse(url, data, method);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void getFileUploadHttpResponse(){
        BzmHttpWrapper spyBzmHttpWrapper=Mockito.spy(bzmHttpWrapper);
        spyBzmHttpWrapper.setHttpClient(httpClient);
        try {
            spyBzmHttpWrapper.getFileUploadHttpResponse(url,file );
            Mockito.verify(spyBzmHttpWrapper,Mockito.never()).getHttpResponse(url, data, method);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void getHttpResponse(){
        BzmHttpWrapper spyBzmHttpWrapper=Mockito.spy(bzmHttpWrapper);
        try {
            Assert.assertEquals(spyBzmHttpWrapper.getHttpResponse(null, data, method),null);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void getFileUploadResponse(){
        BzmHttpWrapper spyBzmHttpWrapper=Mockito.spy(bzmHttpWrapper);
        try {
            Assert.assertEquals(spyBzmHttpWrapper.getFileUploadHttpResponse(url, null), null);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }
}
