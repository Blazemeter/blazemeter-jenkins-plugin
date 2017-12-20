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
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.ApiImpl;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JobUtility;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.plugins.blazemeter.utils.report.BuildReporter;
import hudson.plugins.blazemeter.utils.report.ReportUrlTask;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;

import jenkins.tasks.SimpleBuildStep;
import okhttp3.Credentials;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;


public class PerformanceBuilder extends Builder implements SimpleBuildStep {

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
        return StringUtils.isBlank(this.credentialsId) ? this.jobApiKey : this.credentialsId;
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

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        Result result = null;
        if (StringUtils.isBlank(testId)) {
            listener.error("Please, reconfigure job and select valid credentials and test");
            listener.error("Refer to https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys- for getting new credentials.");
            run.setResult(Result.FAILURE);
            return;
        }

        BuildReporter reporter = new BuildReporter();
        boolean credentialsPresent = false;
        String buildCr = "";
        boolean legacy=false;
        try {
            String credId = (StringUtils.isBlank(this.credentialsId) && !StringUtils.isBlank(this.jobApiKey)) ?
                    Utils.calcLegacyId(this.jobApiKey) : this.credentialsId;

            BlazemeterCredentials credential = Utils.findCredentials(credId, CredentialsScope.GLOBAL);
            credentialsPresent = !StringUtils.isBlank(credential.getId());

            if (!credentialsPresent) {
                listener.error("Can not start build: userKey=" + this.credentialsId + "... is absent in credentials store.");
                result = Result.NOT_BUILT;
                run.setResult(result);
                return;
            }
            BlazeMeterBuild bzmBuild = new BlazeMeterBuild();
            if (credential instanceof BlazemeterCredentialsBAImpl) {
                buildCr = Credentials.basic(((BlazemeterCredentialsBAImpl) credential).getUsername(),
                        ((BlazemeterCredentialsBAImpl) credential).getPassword().getPlainText());
                legacy = false;
            } else {
                buildCr = ((BlazemeterCredentialImpl) credential).getApiKey();
                bzmBuild.setCredLegacy(true);
                legacy = true;
            }
            bzmBuild.setCredential(buildCr);
            String serverUrlConfig = BlazeMeterPerformanceBuilderDescriptor.getDescriptor().getBlazeMeterURL();
            bzmBuild.setServerUrl(serverUrlConfig!=null ? serverUrlConfig : Constants.A_BLAZEMETER_COM);
            bzmBuild.setTestId(this.testId);
            bzmBuild.setNotes(this.notes);
            bzmBuild.setSessionProperties(this.sessionProperties);
            bzmBuild.setJtlPath(this.jtlPath);
            bzmBuild.setJunitPath(this.junitPath);
            bzmBuild.setGetJtl(this.getJtl);
            bzmBuild.setGetJunit(this.getJunit);
            bzmBuild.setListener(listener);
            bzmBuild.setWs(workspace);
            bzmBuild.setWorkspaceId(this.workspaceId);
            String buildId = run.getId();
            bzmBuild.setBuildId(buildId);
            String jobName = run.getLogFile().getParentFile().getParentFile().getParentFile().getName();
            bzmBuild.setJobName(jobName);
            VirtualChannel channel = launcher.getChannel();
            EnvVars ev = run.getEnvironment(listener);
            bzmBuild.setEv(ev);
            ReportUrlTask rugt = new ReportUrlTask(run, jobName, channel);
            reporter = new BuildReporter();
            reporter.run(rugt);
            result = channel.call(bzmBuild);
        } catch (InterruptedException e) {
            result = Result.ABORTED;
            Api api = new ApiImpl(buildCr, BlazeMeterPerformanceBuilderDescriptor.getDescriptor().getBlazeMeterURL() , legacy);
            String masterId = null;
            String buildId = run.getId();
            FilePath ld = new FilePath(workspace, buildId);
            List<FilePath> ldfp = ld.list();
            for (FilePath p : ldfp) {
                if (p.getBaseName().matches("\\d+")) {
                    masterId = p.getBaseName();
                    p.delete();
                    break;
                }
            }
            if (!StringUtils.isBlank(masterId)) {
                try {
                    JobUtility.stopMaster(api, masterId);
                } catch (Exception e1) {
                    listener.error("Failure while stopping master session = " + e1);
                }
            }
        } catch (Exception e) {
            result = Result.FAILURE;
        } finally {
            reporter.stop();
            run.setResult(result);
        }
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
}
