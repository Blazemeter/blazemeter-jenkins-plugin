package hudson.plugins.blazemeter;
/**
 Copyright 2017 BlazeMeter Inc.

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

import hudson.plugins.blazemeter.testresult.TestReport;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestAgrReport {

    @Test
    public void agrReport() throws IOException, JSONException {
        File jf = new File(TestConstants.RESOURCES + "/agreport.json");
        String jo = FileUtils.readFileToString(jf);
        TestReport r = new TestReport(new JSONObject(jo));
        Assert.assertTrue(r.average==6.0520833333333);
        Assert.assertTrue(r.min==0);
        Assert.assertTrue(r.max==172);
        Assert.assertTrue(r.hits==96);
        Assert.assertTrue(r.errorPercentage==100);
        String arStr="AggregateTestResult -> hits=96.0, errors percentage=100.0, average=6.0520833333333, min=0.0, max=172.0";
        Assert.assertEquals(arStr,r.toString());
    }
}
