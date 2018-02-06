/**
 * Copyright 2017 BlazeMeter Inc.
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

import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.Extension;
import hudson.plugins.blazemeter.utils.JenkinsBlazeMeterUtils;
import hudson.plugins.blazemeter.utils.JenkinsTestListFlow;
import hudson.plugins.blazemeter.utils.Utils;

import hudson.plugins.blazemeter.utils.logger.BzmServerLogger;
import hudson.plugins.blazemeter.utils.notifier.BzmServerNotifier;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


@Extension(optional = true)
public class PerformanceBuilderDSLExtension extends ContextExtensionPoint {
    @DslExtensionMethod(context = StepContext.class)
    public Object blazeMeterTest(Runnable closure) {
        BzmServerLogger logger = new BzmServerLogger();
        BzmServerNotifier notifier = new BzmServerNotifier();
        logger.info("Running 'blazeMeterTest' method from JOB DSL plugin...");
        PerformanceBuilderDSLContext c = new PerformanceBuilderDSLContext();
        executeInContext(closure, c);
        PerformanceBuilder pb = null;
        BlazeMeterPerformanceBuilderDescriptor desc = BlazeMeterPerformanceBuilderDescriptor.getDescriptor();
        String serverUrl = desc.getBlazeMeterURL();
        try {
            BlazemeterCredentialsBAImpl credential = Utils.findCredentials(c.credentialsId, CredentialsScope.GLOBAL);
            JenkinsBlazeMeterUtils bzmUtils = null;
            if (!StringUtils.isBlank(credential.getId())) {
                logger.info("Credentials with id = " + c.credentialsId + " are present in credentials.");
                bzmUtils = new JenkinsBlazeMeterUtils(credential.getUsername(), credential.getPassword().getPlainText(), serverUrl, notifier, logger);
                User user = null;
                try {
                    user = User.getUser(bzmUtils);
                    logger.info("Credentials with id = " + c.credentialsId + " are valid.");
                } catch (Exception e) {
                    logger.error("Credentials with credentialsId = " + c.credentialsId + " are invalid.");
                    return pb;
                }
                String limit = System.getProperty("bzm.limit", "10000");
                JenkinsTestListFlow jenkinsTestListFlow = new JenkinsTestListFlow(bzmUtils, limit);
                List<Workspace> workspaces = jenkinsTestListFlow.getWorkspacesForUser(user);
                List<AbstractTest> tests = null;
                for (Workspace workspace : workspaces) {
                    tests = jenkinsTestListFlow.getAllTestsForWorkspace(workspace);
                    for (AbstractTest t : tests) {
                        if (t.getId().equals(c.testId)) {
                            pb = new PerformanceBuilder(c.credentialsId, workspace.getId(), serverUrl,
                                    c.testId, c.notes, c.sessionProperties,
                                    c.jtlPath, c.junitPath, c.getJtl, c.getJunit);
                            logger.info("PerformanceBuilder was successfully created for test = " + c.testId + " in workspace = " + workspace.getId());

                        }
                    }
                }
            } else {
                logger.info(c.credentialsId + " is not present in credentials");
                return pb;
            }

        } catch (Exception e) {
            logger.warn("Failed to create PerformanceBuilder object from Job DSL description: credentialsId=" + c.credentialsId +
                    ", testId =" + c.testId + ", serverUrl=" + serverUrl);
        } finally {
            return pb;
        }
    }
}
