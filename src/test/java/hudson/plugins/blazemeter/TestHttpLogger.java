package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.HttpLogger;
import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zmicer on 20.12.16.
 */
public class TestHttpLogger {

    @Test
    public void constructor() {
        try {
            String lfn="logger";
            HttpLogger l = new HttpLogger(lfn);
            File lf=new File(lfn);
            if(lf.exists()){
                lf.delete();
            }
        } catch (IOException e) {
            Assert.fail();
        }
    }
}
