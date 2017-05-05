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

import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.util.Secret;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TestBlazeterCredentialImpl {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void constructor() {
        CredentialsScope scope = CredentialsScope.GLOBAL;
        String id = "12345";
        String description="vbfgthryfhds";
        String username="vnfhryegdvsfx";
        String password="qwertyuijkmn";
        BlazemeterCredentialImpl c = new BlazemeterCredentialImpl(scope, id,description,username,password);
        Assert.assertEquals(scope.getDisplayName(),c.getScope().getDisplayName());
        Assert.assertEquals(id,c.getId());
        Assert.assertEquals(description,c.getDescription());
        Assert.assertEquals(username,c.getUsername());
        Assert.assertEquals(Secret.fromString(password),c.getPassword());
    }
}
