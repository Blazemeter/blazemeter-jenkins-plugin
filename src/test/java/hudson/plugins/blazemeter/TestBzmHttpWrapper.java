package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.BzmHttpWrapper;
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
    private String url = "http://tut.by";
    private JSONObject data = null;
    private BzmHttpWrapper.Method method = BzmHttpWrapper.Method.GET;

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
            Assert.assertEquals(spyBzmHttpWrapper.getFileUploadResponse(url, null), null);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }
}
