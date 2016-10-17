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
import com.google.common.collect.LinkedHashMultimap;
import hudson.Extension;
import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.ApiV3Impl;
import hudson.plugins.blazemeter.utils.Constants;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import org.eclipse.jetty.util.log.StdErrLog;
import java.util.Collection;


@Extension(optional = true)
public class PerformanceBuilderDSLExtension extends ContextExtensionPoint {
    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);

    @DslExtensionMethod(context = StepContext.class)
    public Object blazeMeterTest(Runnable closure) {
        logger.info("Running 'blazeMeterTest' method from JOB DSL plugin...");
        PerformanceBuilderDSLContext c = new PerformanceBuilderDSLContext();
        executeInContext(closure, c);
        boolean jobApiKeyPresent = false;
        PerformanceBuilder pb = null;
        BlazeMeterPerformanceBuilderDescriptor desc = BlazeMeterPerformanceBuilderDescriptor.getDescriptor();
        String serverUrl = desc.getBlazeMeterURL();
        try {
            jobApiKeyPresent = desc.credPresent(c.jobApiKey, CredentialsScope.GLOBAL);
            logger.info(c.jobApiKey + " is " + (jobApiKeyPresent ? "" : "not") + " present in credentials");
            if (!jobApiKeyPresent) {
                return pb;
            }
            Api api = new ApiV3Impl(c.jobApiKey, serverUrl);
            if (jobApiKeyPresent) {
                LinkedHashMultimap<String, String> tests = api.testsMultiMap();
                Collection<String> values = tests.values();
                logger.info(c.jobApiKey + " is " + (values.size() > 0 ? "" : "not") + " valid for " + BlazeMeterPerformanceBuilderDescriptor.getDescriptor().getBlazeMeterURL());
                if (values.size() > 0) {
                    for (String v : values) {
                        if (v.contains(c.testId)) {
                            logger.info("Test with " + c.testId + " exists on server.");
                            pb = new PerformanceBuilder(c.jobApiKey,
                                    BlazeMeterPerformanceBuilderDescriptor.getDescriptor().getBlazeMeterURL(),
                                    v, c.notes, c.sessionProperties,
                                    c.jtlPath, c.junitPath, c.getJtl, c.getJunit);

                            break;
                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.warn("Failed to create PerformanceBuilder object from Job DSL description: jobApiKey=" + c.jobApiKey +
                    ", testId =" + c.testId + ", serverUrl=" + serverUrl);
        } finally {
            return pb;

        }

    }


}
