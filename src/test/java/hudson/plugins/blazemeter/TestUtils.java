package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.TestType;
import hudson.plugins.blazemeter.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zmicer on 17.7.15.
 */
public class TestUtils {
    public String testId_http="429381.http";
    public String testId_jmeter="429381.jmeter";
    public String testId_followme="429381.followme";
    public String testId_multi="429381.multi";
    public String testId_webdriver="429381.webdriver";
    public String testId_taurus="429381.taurus";

    @Test
    public void getTestType() throws Exception{
        Assert.assertEquals(TestType.http, Utils.getTestType(testId_http));
        Assert.assertEquals(TestType.jmeter, Utils.getTestType(testId_jmeter));
        Assert.assertEquals(TestType.followme, Utils.getTestType(testId_followme));
        Assert.assertEquals(TestType.multi, Utils.getTestType(testId_multi));
        Assert.assertEquals(TestType.webdriver, Utils.getTestType(testId_webdriver));
        Assert.assertEquals(TestType.taurus, Utils.getTestType(testId_taurus));

    }

    @Test
    public void getTestId(){
        Assert.assertEquals("12345", Utils.getTestId("12345.2345"));
        Assert.assertEquals("123452345", Utils.getTestId("123452345"));

    }
}
