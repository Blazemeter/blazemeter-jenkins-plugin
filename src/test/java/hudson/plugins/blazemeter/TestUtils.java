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
    public String testId_http="a - tut.gyt - positive(429381.http)";
    public String testId_jmeter="a - tut.gyt - positive(429381.jmeter)";
    public String testId_followme="a - tut.gyt - positive(429381.followme)";
    public String testId_multi="a - tut.gyt - positive(429381.multi)";
    public String testId_webdriver="a - tut.gyt - positive(429381.webdriver)";
    public String testId_taurus="a - tut.gyt - positive(429381.taurus)";

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
        Assert.assertEquals("12345", Utils.getTestId("asdfg(12345.2345)"));
        Assert.assertEquals("5166480", Utils.getTestId("a - tut.gyt - positive(5166480.http)"));
        Assert.assertEquals("123452345", Utils.getTestId("123452345"));

    }
}
