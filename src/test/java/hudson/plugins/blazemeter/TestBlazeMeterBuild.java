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
import hudson.model.BuildListener;
import hudson.model.Result;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class TestBlazeMeterBuild {

    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.startTest();
        MockedAPI.stopTestSession();
        MockedAPI.getReportUrl();
        MockedAPI.publicToken();
        MockedAPI.notes();
        MockedAPI.getMasterStatus();
        MockedAPI.getTestReport();
    }

    @AfterClass
    public static void tearDown(){
        MockedAPI.stopAPI();
    }


    @Test
    public void call() {
        try {
            BuildListener l = Mockito.mock(BuildListener.class);
            BlazeMeterBuild bb = new BlazeMeterBuild();
            bb.setJobApiKey(TestConstants.MOCKED_USER_KEY_VALID);
            bb.setServerUrl(TestConstants.mockedApiUrl);
            bb.setTestId(TestConstants.TEST_MASTER_ID);
            bb.setNotes("");
            bb.setSessionProperties("");
            bb.setJtlPath("");
            bb.setJunitPath("");
            bb.setGetJtl(false);
            bb.setGetJunit(false);
            FilePath ws = new FilePath(new File(System.getProperty("user.dir")+"/ws"));
            bb.setWs(ws);
            bb.setBuildId("1");
            bb.setJobName("job");
            EnvVars ev = new EnvVars();
            bb.setEv(ev);
            bb.setListener(l);
            Result r = bb.call();
            Assert.assertEquals(Result.FAILURE,r);
            ws.deleteRecursive();
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
