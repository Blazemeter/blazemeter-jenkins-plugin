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
import hudson.Extension;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JobUtility;
import hudson.util.FormValidation;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import org.eclipse.jetty.util.log.StdErrLog;


@Extension(optional = true)
public class PerformanceBuilderDSLExtension extends ContextExtensionPoint {
    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);

    @DslExtensionMethod(context = StepContext.class)
    public Object blazeMeterTest(Runnable closure) {
        logger.info("Running 'blazeMeterTest' method from JOB DSL plugin...");
        PerformanceBuilderDSLContext c = new PerformanceBuilderDSLContext();
        executeInContext(closure, c);
        boolean jobApiKeyPresent = false;
        boolean jobApiKey = false;
        boolean testId = false;
        PerformanceBuilder pb = null;
        BlazeMeterPerformanceBuilderDescriptor desc = BlazeMeterPerformanceBuilderDescriptor.getDescriptor();
        String serverUrl = desc.getBlazeMeterURL();
        try {
            logger.info("Checking that " + c.jobApiKey + " is present in credentials");
            jobApiKeyPresent = desc.credPresent(c.jobApiKey, CredentialsScope.GLOBAL);
            logger.info("Checking that " + c.jobApiKey + " valid with " + serverUrl);
            jobApiKey = JobUtility.validateUserKey(c.jobApiKey, serverUrl).kind.equals(FormValidation.Kind.OK);
            logger.info("Checking that " + c.testId + " is valid for " + c.jobApiKey + " and " + serverUrl);
            testId = JobUtility.testIdExists(c.testId, c.jobApiKey, serverUrl);
            if (jobApiKeyPresent && jobApiKey && testId) {
                pb = new PerformanceBuilder(c.jobApiKey,
                        BlazeMeterPerformanceBuilderDescriptor.getDescriptor().getBlazeMeterURL(),
                        c.testId, c.notes, c.sessionProperties,
                        c.jtlPath, c.junitPath, c.getJtl, c.getJunit);

            }
        } catch (Exception e) {
            logger.warn("Failed to create PerformanceBuilder object from Job DSL description: jobApiKey=" + c.jobApiKey +
                    ", testId =" + c.testId + ", serverUrl=" + serverUrl);
        } finally {
            return pb;

        }

    }


}
