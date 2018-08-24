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

import com.blazemeter.api.explorer.Master;
import com.blazemeter.ciworkflow.BuildResult;
import com.blazemeter.ciworkflow.CiBuild;
import com.blazemeter.ciworkflow.CiPostProcess;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.plugins.blazemeter.utils.JenkinsBlazeMeterUtils;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JenkinsCiBuild;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.plugins.blazemeter.utils.logger.BzmJobLogger;
import hudson.plugins.blazemeter.utils.notifier.BzmJobNotifier;
import hudson.remoting.Callable;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BzmBuild implements Callable<Result, Exception> {

    private static final Logger LOGGER = Logger.getLogger(BzmBuild.class.getName());

    private PerformanceBuilder builder;

    private String jobName;
    private String buildId;

    private String apiId;
    private String apiSecret;
    private String serverURL;


    private EnvVars envVars;
    private FilePath workspace;
    private TaskListener listener;

    private Master master;
    private JenkinsCiBuild build;

    private String mainTestFile;
    private String additionalTestFiles;

    private boolean isSlave;
    private ProxyConfiguration proxyConfiguration;
    private long reportLinkId;
    private String reportLinkName;
    private boolean isUnstableIfNotStarted;

    public BzmBuild(PerformanceBuilder builder, String apiId, String apiSecret,
                    String jobName, String buildId, String serverURL,
                    EnvVars envVars, FilePath workspace, TaskListener listener,
                    ProxyConfiguration proxyConfiguration, boolean isSlave,
                    String reportLinkName, long reportLinkId,
                    String mainTestFile, String additionalTestFiles,
                    boolean isUnstableIfNotStarted) {
        this.builder = builder;
        this.apiId = apiId;
        this.apiSecret = apiSecret;
        this.jobName = jobName;
        this.buildId = buildId;
        this.serverURL = serverURL;
        this.envVars = envVars;
        this.workspace = workspace;
        this.listener = listener;

        this.proxyConfiguration = proxyConfiguration;
        this.isSlave = isSlave;

        this.reportLinkName = reportLinkName;
        this.reportLinkId = reportLinkId;

        this.mainTestFile = mainTestFile;
        this.additionalTestFiles = additionalTestFiles;
        this.isUnstableIfNotStarted = isUnstableIfNotStarted;
    }

    @Override
    public Result call() throws Exception {
        ProxyConfigurator.updateProxySettings(proxyConfiguration, isSlave);
        PrintStream logger = listener.getLogger();
        FilePath wsp = createWorkspaceDir(workspace);
        logger.println(BzmJobNotifier.formatMessage("BlazemeterJenkins plugin v." + Utils.version()));
        JenkinsBlazeMeterUtils utils = createBzmUtils(createLogFile(wsp));
        try {
            build = createCiBuild(utils, wsp);
            try {
                master = build.start();
                if (master != null) {
                    build.uploadPropertiesAndNotes(master);
                    String runId = jobName + "-" + buildId + "-" + reportLinkId;
                    EnvVars.masterEnvVars.put(runId, master.getId());
                    EnvVars.masterEnvVars.put(runId + "-" + master.getId(), build.getPublicReport());
                    putLinkName(runId);
                    build.waitForFinish(master);
                } else {
                    listener.error(BzmJobNotifier.formatMessage("Failed to start test"));
                    return isUnstableIfNotStarted ? Result.UNSTABLE : Result.FAILURE;
                }
            } catch (InterruptedException e) {
                EnvVars.masterEnvVars.put("isInterrupted-" + jobName + "-" + buildId, "false");
                utils.getLogger().warn("Wait for finish has been interrupted", e);
                interrupt(build, master, logger);
                EnvVars.masterEnvVars.put("isInterrupted-" + jobName + "-" + buildId, "true");
                return Result.ABORTED;
            } catch (Exception e) {
                if (master == null) {
                    utils.getLogger().warn("Failed to start BlazeMeter test", e);
                    logger.println(BzmJobNotifier.formatMessage("Failed to start BlazeMeter test: " + e.getMessage()));
                    return  isUnstableIfNotStarted ? Result.UNSTABLE : Result.FAILURE;
                } else {
                    utils.getLogger().warn("Caught exception while waiting for build", e);
                    logger.println(BzmJobNotifier.formatMessage("Caught exception: " + e.getMessage()));
                    return e.getMessage().contains("Not enough available resources") ? Result.UNSTABLE : Result.FAILURE;
                }
            }

            BuildResult buildResult = build.doPostProcess(master);
            return mappedBuildResult(buildResult);
        } finally {
            utils.closeLogger();
        }
    }

    private void putLinkName(String runId) {
        String linkName = (StringUtils.isBlank(reportLinkName)) ?
                "BlazeMeter report: " + build.getCurrentTest().getName() :
                reportLinkName;

        EnvVars.masterEnvVars.put(runId + "-link-name",
                prepareReportLinkName(linkName, getReportLinkNameLength())
        );
    }

    private int getReportLinkNameLength() {
        try {
            String len = this.envVars.get("bzm.reportLinkName.length");
            if (StringUtils.isBlank(len)) {
                LOGGER.fine("Property bzm.reportLinkName.length did not find in Jenkins envVars");
                len = System.getProperty("bzm.reportLinkName.length");
                if (StringUtils.isBlank(len)) {
                    LOGGER.fine("Property bzm.reportLinkName.length did not find in System.properties");
                    len = "35";
                }
            }
            LOGGER.info("Get report link name length = " + len);
            return Integer.parseInt(len);
        } catch (NumberFormatException ex) {
            LOGGER.warning("Cannot parse report link name length = " + ex.getMessage());
            return 35;
        }
    }

    private String prepareReportLinkName(String name, int lengthLimit) {
        return name.length() > lengthLimit ? name.substring(0, lengthLimit) + ".." : name;
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

    public void interrupt(CiBuild build, Master master, PrintStream logger) {
        if (build != null && master != null) {
            try {
                boolean hasReport = build.interrupt(master);
                if (hasReport) {
                    logger.println(BzmJobNotifier.formatMessage("Get reports after interrupt"));
                    build.doPostProcess(master);
                }
            } catch (IOException e) {
                logger.println(BzmJobNotifier.formatMessage("Failed to interrupt build " + e.getMessage()));
            }
        }
    }

    private String createLogFile(FilePath workspace) throws IOException, InterruptedException {
        FilePath logFile = workspace.child(Constants.BZM_LOG + "-" + System.currentTimeMillis());
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
                getMainTestFile(workspace),
                getAdditionalTestFiles(workspace),
                envVars.expand(builder.getSessionProperties()),
                envVars.expand(builder.getNotes()),
                createCiPostProcess(utils, workspace));
    }

    private List<File> getAdditionalTestFiles(FilePath workspace) {
        final String additionalFiles = envVars.expand(additionalTestFiles);
        if (StringUtils.isBlank(additionalFiles)) {
            return null;
        }

        String[] paths = additionalFiles.split("\\n");
        List<File> result = new ArrayList<>();

        for (String path : paths) {
            if (StringUtils.isNotBlank(path)) {
                FilePath child = workspace.child(path);
                String remote = child.getRemote();
                File file = new File(remote);
                if (file.exists()) {
                    result.add(file);
                } else {
                    listener.error("Additional test file does not exist: " + remote);
                    throw new RuntimeException("Additional test file does not exist: " + remote);
                }
            }
        }
        return result;
    }

    private File getMainTestFile(FilePath workspace) {
        final String path = envVars.expand(mainTestFile);
        if (StringUtils.isBlank(path)) {
            return null;
        }

        FilePath child = workspace.child(path);
        String remote = child.getRemote();
        File file = new File(remote);
        if (!file.exists()) {
            listener.error("Main test file does not exist: " + remote);
            throw new RuntimeException("Main test file does not exist: " + remote);
        }

        return file;
    }

    private CiPostProcess createCiPostProcess(JenkinsBlazeMeterUtils utils, FilePath workspace) {
        return new CiPostProcess(builder.isGetJtl(), builder.isGetJunit(),
                envVars.expand(builder.getJtlPath()), envVars.expand(builder.getJunitPath()),
                workspace.getRemote(), utils);
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
        // NOOP
    }

    public CiBuild getBuild() {
        return build;
    }
}