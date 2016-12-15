package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.testresult.AgrReport;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zmicer on 15.12.16.
 */
public class TestAgrReport {

    @Test
    public void agrReport() throws IOException, JSONException {
        File jf = new File(TestConstants.RESOURCES + "/agreport.json");
        String jo = FileUtils.readFileToString(jf);
        AgrReport r = new AgrReport(new JSONObject(jo));
        Assert.assertTrue(r.average==6.0520833333333);
        Assert.assertTrue(r.min==0);
        Assert.assertTrue(r.max==172);
        Assert.assertTrue(r.hits==96);
        Assert.assertTrue(r.errorPercentage==100);
    }
}
