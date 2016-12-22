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

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

public class TestBlazeMeterBuild {
    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
    }

    @AfterClass
    public static void tearDown(){
        MockedAPI.stopAPI();
    }

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void call() {
        String testId="11234";
        String jtlPath="12345";
        String junitPath="12345";
        boolean getJunit=false;
        boolean getJtl=false;
        String notes="a";
        String sessionProperties="f";
        try {
            FreeStyleProject project = j.createFreeStyleProject();
            AbstractBuild b = project.scheduleBuild2(0).get();
            BuildListener l = Mockito.mock(BuildListener.class);
            BlazeMeterBuild bb = new BlazeMeterBuild();
            bb.setJobApiKey(TestConstants.MOCKED_USER_KEY_VALID);
            bb.setServerUrl(TestConstants.mockedApiUrl);
            bb.setTestId(testId);
            bb.setNotes(notes);
            bb.setSessionProperties(sessionProperties);
            bb.setJtlPath(jtlPath);
            bb.setJunitPath(junitPath);
            bb.setGetJtl(getJtl);
            bb.setGetJunit(getJunit);
            FilePath ws = b.getWorkspace();
            bb.setWs(ws);
            String buildId = b.getId();
            bb.setBuildId(buildId);
            String jobName = b.getLogFile().getParentFile().getParentFile().getParentFile().getName();
            bb.setJobName(jobName);
            VirtualChannel c = j.getInstance().getChannel();
            EnvVars ev = EnvVars.getRemote(c);
            bb.setEv(ev);
            bb.setListener(l);
            Result r = bb.call();
            Assert.assertEquals(Result.FAILURE,r);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Assert.fail();
            e.printStackTrace();
        }
    }
}
