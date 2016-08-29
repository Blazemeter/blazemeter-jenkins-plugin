/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.TestType;
import hudson.plugins.blazemeter.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

public class TestUtils {
    public String testId_http_new ="a - tut.gyt - positive(429381.http)";
    public String testId_jmeter_new ="a - tut.gyt - positive(429381.jmeter)";
    public String testId_followme_new ="a - tut.gyt - positive(429381.followme)";
    public String testId_multi_new ="a - tut.gyt - positive(429381.multi)";
    public String testId_webdriver_new ="a - tut.gyt - positive(429381.webdriver)";
    public String testId_taurus_new ="a - tut.gyt - positive(429381.taurus)";
    public String testId_http_old ="429381.http";
    public String testId_jmeter_old ="429381.jmeter";
    public String testId_followme_old ="429381.followme";
    public String testId_multi_old ="429381.multi";
    public String testId_webdriver_old ="429381.webdriver";
    public String testId_taurus_old ="429381.taurus";

    @Test
    public void getTestType_new() throws Exception{
        Assert.assertEquals(TestType.http, Utils.getTestType(testId_http_new));
        Assert.assertEquals(TestType.jmeter, Utils.getTestType(testId_jmeter_new));
        Assert.assertEquals(TestType.followme, Utils.getTestType(testId_followme_new));
        Assert.assertEquals(TestType.multi, Utils.getTestType(testId_multi_new));
        Assert.assertEquals(TestType.webdriver, Utils.getTestType(testId_webdriver_new));
        Assert.assertEquals(TestType.taurus, Utils.getTestType(testId_taurus_new));

    }

    @Test
    public void getTestType_old() throws Exception{
        Assert.assertEquals(TestType.http, Utils.getTestType(testId_http_old));
        Assert.assertEquals(TestType.jmeter, Utils.getTestType(testId_jmeter_old));
        Assert.assertEquals(TestType.followme, Utils.getTestType(testId_followme_old));
        Assert.assertEquals(TestType.multi, Utils.getTestType(testId_multi_old));
        Assert.assertEquals(TestType.webdriver, Utils.getTestType(testId_webdriver_old));
        Assert.assertEquals(TestType.taurus, Utils.getTestType(testId_taurus_old));

    }

    @Test
    public void getTestId_new(){
        Assert.assertEquals("12345", Utils.getTestId("asdfg(12345.2345)"));
        Assert.assertEquals("5166480", Utils.getTestId("a - tut.gyt - positive(5166480.http)"));
        Assert.assertEquals("123452345", Utils.getTestId("123452345"));

    }

    @Test
    public void getTestId_old(){
        Assert.assertEquals("12345", Utils.getTestId("12345.2345"));
        Assert.assertEquals("5166480", Utils.getTestId("5166480.http"));
        Assert.assertEquals("123452345", Utils.getTestId("123452345"));

    }
}
