/**
 * Copyright 2016 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.Extension;
import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.ApiImpl;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.Utils;

import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import okhttp3.Credentials;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.log.StdErrLog;


@Extension(optional = true)
public class PerformanceBuilderDSLExtension extends ContextExtensionPoint {
    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);

    @DslExtensionMethod(context = StepContext.class)
    public Object blazeMeterTest(Runnable closure) {
        logger.info("Running 'blazeMeterTest' method from JOB DSL plugin...");
        PerformanceBuilderDSLContext c = new PerformanceBuilderDSLContext();
        executeInContext(closure, c);
        boolean credentialsPresent = false;
        PerformanceBuilder pb = null;
        BlazeMeterPerformanceBuilderDescriptor desc = BlazeMeterPerformanceBuilderDescriptor.getDescriptor();
        String serverUrl = desc.getBlazeMeterURL();
        try {
            BlazemeterCredentials credential = Utils.findCredentials(c.credentialsId, CredentialsScope.GLOBAL);
            credentialsPresent = !StringUtils.isBlank(credential.getId());
            logger.info(c.credentialsId + " is " + (credentialsPresent ? "" : "not") + " present in credentials");
            String buildCr = "";
            Api api = null;
            if (credentialsPresent) {
                if (credential instanceof BlazemeterCredentialsBAImpl) {
                    buildCr = Credentials.basic(((BlazemeterCredentialsBAImpl) credential).getUsername(),
                            ((BlazemeterCredentialsBAImpl) credential).getPassword().getPlainText());
                    api = new ApiImpl(buildCr, serverUrl, false);
                } else {
                    buildCr = ((BlazemeterCredentialImpl) credential).getApiKey();
                    api = new ApiImpl(buildCr, serverUrl, true);
                }
                int pid=api.projectId(c.testId);
                if (pid>0) {
                    try {
                        c.workspaceId = String.valueOf(api.workspaceId(String.valueOf(pid)));
                    } catch (Exception e) {
                        logger.info("Failed to find workspace for testId = " + c.testId);
                    }
                    pb = new PerformanceBuilder(c.credentialsId, c.workspaceId, serverUrl,
                            c.testId, c.notes, c.sessionProperties,
                            c.jtlPath, c.junitPath, c.getJtl, c.getJunit);
                }
            }

        } catch (Exception e) {
            logger.warn("Failed to create PerformanceBuilder object from Job DSL description: credentialsId=" + c.credentialsId +
                    ", testId =" + c.testId + ", serverUrl=" + serverUrl);
        } finally {
            return pb;
        }
    }
}
