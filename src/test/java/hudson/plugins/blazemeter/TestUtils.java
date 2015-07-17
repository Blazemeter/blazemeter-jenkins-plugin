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
    public String testId_unkown_type="429381.cvbhgy";
    public String testId_multi="429381.multi";

    @Test
    public void getTestType(){
        int dotPos=testId_http.indexOf(".");
        Assert.assertEquals(TestType.http, Utils.getTestType(testId_http,dotPos));
        dotPos=testId_jmeter.indexOf(".");
        Assert.assertEquals(TestType.jmeter, Utils.getTestType(testId_jmeter,dotPos));
        dotPos=testId_followme.indexOf(".");
        Assert.assertEquals(TestType.followme, Utils.getTestType(testId_followme,dotPos));
        dotPos=testId_unkown_type.indexOf(".");
        Assert.assertEquals(TestType.unknown_type, Utils.getTestType(testId_unkown_type,dotPos));
        dotPos=testId_multi.indexOf(".");
        Assert.assertEquals(TestType.multi, Utils.getTestType(testId_multi,dotPos));

    }
}
