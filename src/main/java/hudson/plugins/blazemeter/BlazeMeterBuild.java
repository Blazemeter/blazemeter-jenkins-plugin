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

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import org.jenkinsci.remoting.RoleChecker;


public class BlazeMeterBuild implements Callable<Result, Exception> {

    private boolean credLegacy = false;

    private String credential = null;

    private String workspaceId = null;

    private String serverUrl = "";

    private String testId = "";

    private String notes = "";

    private String sessionProperties = "";

    private String jtlPath = "";

    private String junitPath = "";

    private boolean getJtl = false;

    private boolean getJunit = false;

    private String buildId = null;

    private String jobName = null;

    private FilePath ws = null;

    private EnvVars ev = null;

    private TaskListener listener = null;

    @Override
    public Result call() throws Exception {
        return null;
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {

    }

    public void setEv(EnvVars ev) {
        this.ev = ev;
    }


    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }


    public void setNotes(String notes) {
        this.notes = notes;
    }


    public void setSessionProperties(String sessionProperties) {
        this.sessionProperties = sessionProperties;
    }


    public void setJtlPath(String jtlPath) {
        this.jtlPath = jtlPath;
    }


    public void setJunitPath(String junitPath) {
        this.junitPath = junitPath;
    }


    public void setGetJtl(boolean getJtl) {
        this.getJtl = getJtl;
    }


    public void setGetJunit(boolean getJunit) {
        this.getJunit = getJunit;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setWs(FilePath ws) {
        this.ws = ws;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    public void setCredLegacy(final boolean credLegacy) {
        this.credLegacy = credLegacy;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
}
