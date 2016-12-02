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

import hudson.plugins.blazemeter.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

public class TestUtils {

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
