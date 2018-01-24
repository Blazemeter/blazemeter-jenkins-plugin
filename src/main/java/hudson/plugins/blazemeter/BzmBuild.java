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

import com.blazemeter.api.explorer.Master;
import com.blazemeter.ciworkflow.BuildResult;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.plugins.blazemeter.utils.JenkinsBlazeMeterUtils;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JenkinsCiBuild;
import hudson.plugins.blazemeter.utils.JenkinsCiPostProcess;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.plugins.blazemeter.utils.logger.BzmJobLogger;
import hudson.plugins.blazemeter.utils.notifier.BzmJobNotifier;
import hudson.remoting.Callable;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class BzmBuild implements Callable<Result, Exception> {

    private PerformanceBuilder builder;

    private String jobName;
    private String buildId;

    private String apiId;
    private String apiSecret;
    private String serverURL;


    private EnvVars envVars;
    private FilePath workspace;
    private TaskListener listener;

    private boolean applyJenkinsProxy;
    private Master master;
    private JenkinsCiBuild build;

    public BzmBuild(PerformanceBuilder builder, String apiId, String apiSecret,
                    String jobName, String buildId, String serverURL,
                    EnvVars envVars, FilePath workspace, TaskListener listener, boolean applyJenkinsProxy) {
        this.builder = builder;
        this.apiId = apiId;
        this.apiSecret = apiSecret;
        this.jobName = jobName;
        this.buildId = buildId;
        this.serverURL = serverURL;
        this.envVars = envVars;
        this.workspace = workspace;
        this.listener = listener;
        this.applyJenkinsProxy = applyJenkinsProxy;
    }

    @Override
    public Result call() throws Exception {
        ProxyConfigurator.updateProxySettings(applyJenkinsProxy);
        PrintStream logger = listener.getLogger();
        FilePath wsp = createWorkspaceDir(workspace);
        logger.println("BlazemeterJenkins plugin v." + Utils.version());
        JenkinsBlazeMeterUtils utils = createBzmUtils(createLogFile(wsp));
        try {
            build = createCiBuild(utils, wsp);

            try {
                master = build.start();
                if (master != null) {
                    EnvVars.masterEnvVars.put(jobName + "-" + buildId, build.getPublicReport());
                    build.waitForFinish(master);
                } else {
                    listener.error("Failed to start test");
                    return Result.FAILURE;
                }
            } catch (InterruptedException e) {
                EnvVars.masterEnvVars.put("isInterrupted-" + jobName + "-" + buildId, "false");
                utils.getLogger().warn("Wait for finish has been interrupted", e);
                interrupt(build, master, logger);
                EnvVars.masterEnvVars.put("isInterrupted-" + jobName + "-" + buildId, "true");
                return Result.ABORTED;
            } catch (Exception e) {
                utils.getLogger().warn("Caught exception while waiting for build", e);
                logger.println("Caught exception " + e.getMessage());
                return Result.FAILURE;
            }

            BuildResult buildResult = build.doPostProcess(master);
            return mappedBuildResult(buildResult);
        } finally {
//            ProxyConfigurator.clearProxySettings(applyJenkinsProxy);
            utils.closeLogger();
        }
    }

    private Result mappedBuildResult(BuildResult buildResult) {
        switch (buildResult) {
            case SUCCESS:
                return Result.SUCCESS;
            case ABORTED:
                return Result.ABORTED;
            case ERROR:
                return Result.UNSTABLE;
            case FAILED:
                return Result.FAILURE;
            default:
                return Result.NOT_BUILT;
        }
    }

    public void interrupt(JenkinsCiBuild build, Master master, PrintStream logger) {
        if (build != null && master != null) {
            try {
                logger.println("Build has been interrupted");
                boolean hasReport = build.interrupt(master);
                if (hasReport) {
                    logger.println("Get reports after interrupt");
                    build.doPostProcess(master);
                }
            } catch (IOException e) {
                logger.println("Failed to interrupt build " + e.getMessage());
            }
        }
    }

    private String createLogFile(FilePath workspace) throws IOException, InterruptedException {
        FilePath logFile = workspace.child(Constants.BZM_LOG);
        logFile.touch(System.currentTimeMillis());
        return logFile.getRemote();
    }

    private FilePath createWorkspaceDir(FilePath workspace) throws IOException, InterruptedException {
        FilePath wsp = new FilePath(workspace.getChannel(),
                workspace.getRemote() + File.separator + buildId);
        wsp.mkdirs();
        return wsp;
    }

    private JenkinsBlazeMeterUtils createBzmUtils(String logFile) {
        return new JenkinsBlazeMeterUtils(apiId, apiSecret, serverURL,
                new BzmJobNotifier(listener),
                new BzmJobLogger(logFile));
    }

    private JenkinsCiBuild createCiBuild(JenkinsBlazeMeterUtils utils, FilePath workspace) {
        return new JenkinsCiBuild(utils,
                Utils.getTestId(builder.getTestId()),
                envVars.expand(builder.getSessionProperties()),
                envVars.expand(builder.getNotes()),
                createCiPostProcess(utils, workspace));
    }

    private JenkinsCiPostProcess createCiPostProcess(JenkinsBlazeMeterUtils utils, FilePath workspace) {
        return new JenkinsCiPostProcess(builder.isGetJtl(), builder.isGetJunit(),
                envVars.expand(builder.getJtlPath()), envVars.expand(builder.getJunitPath()),
                workspace.getRemote(), utils.getNotifier(), utils.getLogger());
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
        // NOOP
    }

    public JenkinsCiBuild getBuild() {
        return build;
    }


}