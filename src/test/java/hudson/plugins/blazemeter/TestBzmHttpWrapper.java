package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.BzmHttpWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestBzmHttpWrapper {
    private DefaultHttpClient httpClient = Mockito.mock(DefaultHttpClient.class);
    private BzmHttpWrapper bzmHttpWrapper=new BzmHttpWrapper(httpClient);


    @Test
    public void testMockito(){

    }
}
