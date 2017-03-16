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
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.plugins.blazemeter.utils.report.BuildReporter;
import hudson.plugins.blazemeter.utils.report.ReportUrlTask;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import java.io.IOException;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;


public class PerformanceBuilder extends Builder implements SimpleBuildStep{

    private String jobApiKey = "";

    private String serverUrl = "";

    private String testId = "";

    private String notes = "";

    private String sessionProperties = "";

    private String jtlPath = "";

    private String junitPath = "";

    private boolean getJtl = false;

    private boolean getJunit = false;


    @DataBoundConstructor
    public PerformanceBuilder(String jobApiKey,
                              String serverUrl,
                              String testId,
                              String notes,
                              String sessionProperties,
                              String jtlPath,
                              String junitPath,
                              boolean getJtl,
                              boolean getJunit
    ) {
        this.jobApiKey = jobApiKey;
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

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace, @Nonnull final Launcher launcher, @Nonnull final TaskListener listener) throws InterruptedException, IOException {
        Result r = null;
        BuildReporter br = new BuildReporter();
        try {
            boolean valid = Utils.credPresent(this.jobApiKey, CredentialsScope.GLOBAL);
            if (!valid) {
                listener.error("Can not start build: userKey=" + this.jobApiKey.substring(0, 3) + "... is absent in credentials store.");
                r = Result.NOT_BUILT;
                run.setResult(r);
                return;
            }
            BlazeMeterBuild b = new BlazeMeterBuild();
            b.setJobApiKey(this.jobApiKey);
            b.setServerUrl(this.serverUrl != null ? this.serverUrl : Constants.A_BLAZEMETER_COM);
            b.setTestId(this.testId);
            b.setNotes(this.notes);
            b.setSessionProperties(this.sessionProperties);
            b.setJtlPath(this.jtlPath);
            b.setJunitPath(this.junitPath);
            b.setGetJtl(this.getJtl);
            b.setGetJunit(this.getJunit);
            b.setListener(listener);
            b.setWs(workspace);
            String buildId = run.getId();
            b.setBuildId(buildId);
            String jobName = run.getLogFile().getParentFile().getParentFile().getParentFile().getName();
            b.setJobName(jobName);
            VirtualChannel c = launcher.getChannel();
            EnvVars ev = run.getEnvironment(listener);
            b.setEv(ev);
            ReportUrlTask rugt = new ReportUrlTask(run, jobName, c);
            br = new BuildReporter();
            br.run(rugt);
            r = c.call(b);
        } catch (InterruptedException e) {
            r = Result.ABORTED;
        } catch (Exception e) {
            r = Result.FAILURE;
        } finally {
            br.stop();
            run.setResult(r);
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
        BuildListener listener) throws InterruptedException, IOException {
        this.perform(build, build.getWorkspace(), launcher, listener);
        return !build.getResult().equals(Result.FAILURE);
    }


    public String getJobApiKey() {
        return jobApiKey;
    }

    public void setJobApiKey(String jobApiKey) {
        this.jobApiKey = jobApiKey;
    }

    public String getTestId() {
        return testId;
    }

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

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSessionProperties() {
        return sessionProperties;
    }

    public String getJtlPath() {
        return jtlPath;
    }

    public void setJtlPath(String jtlPath) {
        this.jtlPath = jtlPath;
    }

    public String getJunitPath() {
        return junitPath;
    }

    public void setJunitPath(String junitPath) {
        this.junitPath = junitPath;
    }

    public void setSessionProperties(String sessionProperties) {
        this.sessionProperties = sessionProperties;
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
