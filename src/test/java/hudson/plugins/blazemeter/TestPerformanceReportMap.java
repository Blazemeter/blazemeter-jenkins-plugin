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

import hudson.model.AbstractBuild;
import java.io.IOException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

public class TestPerformanceReportMap {

    @Test
    public void reportMap() {
        String url = "http://url.com";
        String url1 = "http://url1.com";
        AbstractBuild ab = Mockito.mock(AbstractBuild.class);
        PerformanceBuildAction ba = new PerformanceBuildAction(ab);
        PerformanceReportMap map = new PerformanceReportMap(ba);
        ba.setReportUrl(url);
        Assert.assertEquals(url, map.getReportUrl());
        map.setReportUrl(url1);
        Assert.assertEquals(url1, ba.getReportUrl());

    }


}
