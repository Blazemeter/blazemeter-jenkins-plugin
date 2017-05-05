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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TestPerformanceBuilder {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void perform_failure() {
        String credentialsId="1";
        String serverUrl="1";
        String testId="1";
        String jtlPath="1";
        String junitPath="1";
        boolean getJunit=false;
        boolean getJtl=false;
        String notes="a";
        String sessionProperties = "f";
        PerformanceBuilder pb = new PerformanceBuilder(credentialsId, serverUrl, testId, notes, sessionProperties, jtlPath, junitPath, getJtl, getJunit);
        try {
            FreeStyleProject project = j.createFreeStyleProject();
            project.getBuildersList().add(pb);
            FreeStyleBuild b = project.scheduleBuild2(0).get();
            Assert.assertEquals(Result.FAILURE, b.getResult());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
