/**
 * Copyright 2018 BlazeMeter Inc.
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
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.plugins.blazemeter.utils.interrupt.InterruptListenerTask;
import hudson.plugins.blazemeter.utils.notifier.BzmJobNotifier;
import hudson.plugins.blazemeter.utils.report.ReportUrlTask;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.Timer;
import java.util.logging.Logger;


public class PerformanceBuilder extends Builder implements SimpleBuildStep, Serializable {

    private static final Logger LOGGER = Logger.getLogger(PerformanceBuilder.class.getName());

    @Deprecated
    private String jobApiKey = "";

    private String credentialsId = "";

    private String workspaceId = "";

    @Deprecated
    private String serverUrl = "";

    private String testId = "";

    private String notes = "";

    private String sessionProperties = "";

    private String jtlPath = "";

    private String junitPath = "";

    private boolean getJtl = false;

    private boolean getJunit = false;


    @DataBoundConstructor
    public PerformanceBuilder(String credentialsId, String workspaceId, String testId) {
        this.credentialsId = credentialsId;
        this.workspaceId = workspaceId;
        this.testId = testId;
    }


    @Restricted(NoExternalUse.class)
    public PerformanceBuilder(String credentialsId,
                              String workspaceId,
                              String serverUrl,
                              String testId,
                              String notes,
                              String sessionProperties,
                              String jtlPath,
                              String junitPath,
                              boolean getJtl,
                              boolean getJunit
    ) {
        this.credentialsId = credentialsId;
        this.workspaceId = workspaceId;
        this.serverUrl = serverUrl;
        this.testId = testId;
        this.jtlPath = jtlPath;
        this.junitPath = junitPath;
        this.getJtl = getJtl;
        this.getJunit = getJunit;
        this.notes = notes;
        this.sessionProperties = sessionProperties;
    }


    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getTestId() {
        return testId;
    }

    @DataBoundSetter
    public void setTestId(String testId) {
        this.testId = testId;
    }

    public boolean isGetJtl() {
        return getJtl;
    }

    public boolean isGetJunit() {
        return getJunit;
    }

    public String getNotes() {
        return notes;
    }

    @DataBoundSetter
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSessionProperties() {
        return sessionProperties;
    }

    public String getJtlPath() {
        return jtlPath;
    }

    @DataBoundSetter
    public void setJtlPath(String jtlPath) {
        this.jtlPath = jtlPath;
    }

    public String getJunitPath() {
        return junitPath;
    }

    @DataBoundSetter
    public void setJunitPath(String junitPath) {
        this.junitPath = junitPath;
    }

    @DataBoundSetter
    public void setSessionProperties(String sessionProperties) {
        this.sessionProperties = sessionProperties;
    }

    @Deprecated
    public String getJobApiKey() {
        return this.jobApiKey;
    }

    @Deprecated
    @DataBoundSetter
    public void setJobApiKey(final String jobApiKey) {
        this.jobApiKey = jobApiKey;
    }


    public String getWorkspaceId() {
        return this.workspaceId;
    }

    @DataBoundSetter
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    @Deprecated
    public String getServerUrl() {
        return serverUrl;
    }

    @Deprecated
    @DataBoundSetter
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @DataBoundSetter
    public void setGetJtl(boolean getJtl) {
        this.getJtl = getJtl;
    }

    @DataBoundSetter
    public void setGetJunit(boolean getJunit) {
        this.getJunit = getJunit;
    }

    private boolean validateTestId(TaskListener listener) {
        if (StringUtils.isBlank(testId)) {
            listener.error(BzmJobNotifier.formatMessage("Please, reconfigure job and select valid credentials and test"));
            listener.error(BzmJobNotifier.formatMessage("Refer to https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys- for getting new credentials."));
            return false;
        }
        return true;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {

        if (!validateTestId(listener)) {
            run.setResult(Result.FAILURE);
            return;
        }

        BlazemeterCredentialsBAImpl credentials = Utils.findCredentials(credentialsId, CredentialsScope.GLOBAL);
        boolean isValidCredentials = !StringUtils.isBlank(credentialsId) && validateCredentials(credentials);
        if (!isValidCredentials) {
            listener.error(BzmJobNotifier.formatMessage("Can not start build: Invalid credentials=" + credentialsId + "... is deprecated or absent in credentials store."));
            run.setResult(Result.NOT_BUILT);
            return;
        }

        String serverUrlConfig = BlazeMeterPerformanceBuilderDescriptor.getDescriptor().getBlazeMeterURL();
        String jobName = run.getFullDisplayName();
        VirtualChannel channel = launcher.getChannel();

        final long reportLinkId = System.currentTimeMillis();

        BzmBuild bzmBuild = new BzmBuild(this, credentials.getUsername(), credentials.getPassword().getPlainText(),
                jobName, run.getId(), StringUtils.isBlank(serverUrlConfig) ? Constants.A_BLAZEMETER_COM : serverUrlConfig,
                run.getEnvironment(listener), workspace, listener, channel instanceof LocalChannel, reportLinkId);


        ReportUrlTask reportUrlTask = new ReportUrlTask(run, jobName, channel, reportLinkId);

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(reportUrlTask, 20 * 1000, 10 * 1000);
        try {
            Result result = channel.call(bzmBuild);
            run.setResult(result);
        } catch (InterruptedException e) {
            LOGGER.warning("Build has been aborted");
            // start new task for wait Slave
            InterruptListenerTask interrupt = new InterruptListenerTask(run, jobName, channel);
            interrupt.start();
            interrupt.join();
            run.setResult(Result.ABORTED);
        } catch (Exception e) {
            listener.getLogger().println(BzmJobNotifier.formatMessage("Failure with exception: " + e.getMessage()));
            e.printStackTrace(listener.getLogger());
            run.setResult(Result.FAILURE);
        } finally {
            reportUrlTask.cancel();
            timer.cancel();
            timer.purge();
        }
    }

    private boolean validateCredentials(BlazemeterCredentials credential) {
        return !StringUtils.isBlank(credential.getId()) && credential instanceof BlazemeterCredentialsBAImpl;
    }

    // The descriptor has been moved but we need to maintain the old descriptor for backwards compatibility reasons.
    @SuppressWarnings({"UnusedDeclaration"})
    public static final class DescriptorImpl
            extends BlazeMeterPerformanceBuilderDescriptor {

        @Override
        public boolean configure(StaplerRequest req, net.sf.json.JSONObject formData) throws FormException {
            return super.configure(req, formData);
        }
    }

    /**
     * This method, invoked after object is resurrected from persistence
     */
    public Object readResolve() {
        // resolve old testId format:  "testLabel(testId.testType)" to new "testId.testType"
        if (!StringUtils.isBlank(testId) && testId.contains("(")) {
            testId = Utils.resolveTestId(testId);
        }
        return this;
    }
}
