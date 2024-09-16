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
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JenkinsBlazeMeterUtils;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.plugins.blazemeter.utils.logger.BzmServerLogger;
import hudson.plugins.blazemeter.utils.notifier.BzmJobNotifier;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;

public class BzmBuild extends MasterToSlaveCallable<Result, Exception> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(BzmBuild.class.getName());

    private PerformanceBuilder builder;
    private CiPostProcess ciPostProcess;

    private String jobName;
    private String buildId;

    private String apiId;
    private String apiSecret;
    private String serverURL;


    private EnvVars envVars;
    private FilePath workspace;
    private TaskListener listener;

    private Master master;
    private CiBuild build;

    private String mainTestFile;
    private String additionalTestFiles;

    private boolean isSlave;
    private ProxyConfiguration proxyConfiguration;
    private long reportLinkId;
    private String reportLinkName;
    private boolean isUnstableIfHasFails;
    private String SLACK ="slack";
    private String TEAMS ="teams";


    public BzmBuild(PerformanceBuilder builder,String apiId, String apiSecret,
                    String jobName, String buildId, String serverURL,
                    EnvVars envVars, FilePath workspace, TaskListener listener,
                    ProxyConfiguration proxyConfiguration, boolean isSlave,
                    String reportLinkName, long reportLinkId,
                    String mainTestFile, String additionalTestFiles,
                    boolean isUnstableIfHasFails) {
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
        this.isUnstableIfHasFails = isUnstableIfHasFails;
    }

    @Override
    public Result call() throws Exception {
        ProxyConfigurator.updateProxySettings(proxyConfiguration, isSlave);
        PrintStream logger = listener.getLogger();
        FilePath wsp = createWorkspaceDir(workspace);
        logger.println(BzmJobNotifier.formatMessage("BlazemeterJenkins plugin v." + Utils.version()));
        JenkinsBlazeMeterUtils utils = createBzmUtils();
        try {
            build = createCiBuild(utils, wsp);
            try {
                build.setWorkspaceId(builder.getWorkspaceId());
                master = build.start();
                if (master != null) {
                    String runId = jobName + "-" + buildId + "-" + reportLinkId;
                    EnvVars.masterEnvVars.put(runId, master.getId());

                    // set master id as a Environment variable
                    createGlobalEnvironmentVariables("masterId",master.getId());

                    EnvVars.masterEnvVars.put(runId + "-" + master.getId(), build.getPublicReport());
                    putLinkName(runId);

                    build.waitForFinish(master);

                    if (SLACK.equals(builder.getSelectWebhook())) {
                        Optional.ofNullable(builder.getWebhookUrl())
                                .filter(webhookUrl -> !webhookUrl.isEmpty())
                                .ifPresent(webhookUrl -> sendWebhookNotificationSlack(master.getId(), build.getCurrentTest().getName(), build.getPublicReport(), webhookUrl));
                    } else if (TEAMS.equals(builder.getSelectWebhook())) {
                        Optional.ofNullable(builder.getWebhookUrl())
                                .filter(webhookUrl -> !webhookUrl.isEmpty())
                                .ifPresent(webhookUrl -> sendWebhookNotificationTeams(master.getId(), build.getCurrentTest().getName(), build.getPublicReport(), webhookUrl));
                    }
                } else {
                    listener.error(BzmJobNotifier.formatMessage("Failed to start test"));
                    return isUnstableIfHasFails ? Result.UNSTABLE : Result.FAILURE;
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
                } else {
                    utils.getLogger().warn("Caught exception while waiting for build", e);
                    logger.println(BzmJobNotifier.formatMessage("Caught exception: " + e.getMessage()));
                }
                return  isUnstableIfHasFails ? Result.UNSTABLE : Result.FAILURE;
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

    private JenkinsBlazeMeterUtils createBzmUtils() {
        return new JenkinsBlazeMeterUtils(apiId, apiSecret, serverURL,
                new BzmJobNotifier(listener),
                new BzmServerLogger());
    }

    private CiBuild createCiBuild(JenkinsBlazeMeterUtils utils, FilePath workspace) {
        return new CiBuild(utils,
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
                try {
                    if (child.exists()) {
                        // Copy the file from remote agent to master for further processing
                        // /var/tmp is a safe path as it's accessible to all the users
                        File localFile = new File("/var/tmp/" + file.getName());
                        child.copyTo(new FilePath(localFile));
                        result.add(localFile);
                    } else {
                        listener.error("Additional test file does not exist: " + remote);
                        throw new RuntimeException("Additional test file does not exist: " + remote);
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Failed to check Additional file existence for: " + remote, e);
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
        File localFile = null;
        try {
            if (child.exists()) {
                // Copy the file from remote agent to master for further processing
                // /var/tmp is a safe path as it's accessible to all the users
                localFile = new File("/var/tmp/" + file.getName());
                child.copyTo(new FilePath(localFile));
            } else {
                listener.error("Main test file does not exist: " + remote);
                throw new RuntimeException("Main test file does not exist: " + remote);
            }

            return localFile;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to check Main file existence for: " + remote, e);
        }
    }

    private CiPostProcess createCiPostProcess(JenkinsBlazeMeterUtils utils, FilePath workspace) {
        return new CiPostProcess(builder.isGetJtl(), builder.isGetJunit(),
                envVars.expand(builder.getJtlPath()), envVars.expand(builder.getJunitPath()),
                workspace.getRemote(), utils) {

            public boolean isErrorsFailed(JSONArray errors) {
                if (isUnstableIfHasFails) {
                    return false;
                }
                return super.isErrorsFailed(errors);
            }
        };
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
        // NOOP
    }

    public CiBuild getBuild() {
        return build;
    }
    public void createGlobalEnvironmentVariables(String key, String value){
        PrintStream logger = listener.getLogger();
        try {
            Jenkins instance = Jenkins.getInstance();
            DescribableList<NodeProperty<?>, NodePropertyDescriptor> globalNodeProperties = instance.getGlobalNodeProperties();
            List<EnvironmentVariablesNodeProperty> envVarsNodePropertyList = globalNodeProperties.getAll(EnvironmentVariablesNodeProperty.class);

            EnvironmentVariablesNodeProperty newEnvVarsNodeProperty = null;
            EnvVars envVars = null;

            if (envVarsNodePropertyList == null || envVarsNodePropertyList.size() == 0) {
                newEnvVarsNodeProperty = new EnvironmentVariablesNodeProperty();
                globalNodeProperties.add(newEnvVarsNodeProperty);
                envVars = newEnvVarsNodeProperty.getEnvVars();
            } else {
                envVars = envVarsNodePropertyList.get(0).getEnvVars();
            }
            envVars.put(key, value);
            instance.save();
        }
        catch (Exception e)
        {
            logger.println(BzmJobNotifier.formatMessage("Error generated by creating global env"));
        }
    }
    private String getColor(BuildResult buildResult) {
        return (buildResult == BuildResult.FAILED) ? "#FF0000" : "#008000";
    }
    public void sendWebhookNotificationTeams(String masterId, String testName, String publicReportUrl, String webhookUrl){
        BuildResult buildResult = build.doPostProcess(master);
        String color = getColor(buildResult);
        String json = "{\n"
                + "    \"@type\": \"MessageCard\",\n"
                + "    \"@context\": \"http://schema.org/extensions\",\n"
                + "    \"themeColor\": \"" + color + "\",\n"
                + "    \"summary\": \"Blazemeter Test Execution\",\n"
                + "    \"sections\": [\n"
                + "        {\n"
                + "            \"activityTitle\": \"Blazemeter Test Execution\",\n"
                + "            \"facts\": [\n"
                + "                {\n"
                + "                    \"name\": \"Test Name : \",\n"
                + "                    \"value\": \"" + testName + "\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"name\": \"Master ID : \",\n"
                + "                    \"value\": \"" + masterId + "\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"name\": \"Test Status : \",\n"
                + "                    \"value\": \"" + buildResult + "\"\n"
                + "                }\n"
                + "            ],\n"
                + "            \"markdown\": true\n"
                + "        }\n"
                + "    ],\n"
                + "    \"potentialAction\": [\n"
                + "        {\n"
                + "            \"@type\": \"OpenUri\",\n"
                + "            \"name\": \"View Report\",\n"
                + "            \"targets\": [\n"
                + "                {\n"
                + "                    \"os\": \"default\",\n"
                + "                    \"uri\": \"" + publicReportUrl + "\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    ]\n"
                + "}\n";
        sendWebhookNotification(webhookUrl, json);
    }
    public void sendWebhookNotificationSlack(String masterId,String testName,String publicReportUrl,String webhookUrl){
        BuildResult buildResult = build.doPostProcess(master);
        String color = getColor(buildResult);
        String jsonPayload = String.format(
                "{" +
                        "\"attachments\":[" +
                        "{" +
                        "\"title\":\"Blazemeter Test Execution\"," +
                        "\"pretext\":\"Blazemeter Test Execution\"," +
                        "\"fallback\":\"Blazemeter Test Execution\"," +
                        "\"color\":\"%s\"," +
                        "\"fields\":[" +
                        "{" +
                        "\"title\":\"Test Name\"," +
                        "\"value\":\"%s\"," +
                        "\"short\":true" +
                        "}," +
                        "{" +
                        "\"title\":\"Master ID\"," +
                        "\"value\":\"%s\"," +
                        "\"short\":true" +
                        "}," +
                        "{" +
                        "\"title\":\"Test Status\"," +
                        "\"value\":\"%s\"," +
                        "\"short\":true" +
                        "}," +
                        "{" +
                        "\"title\":\"View Report\"," +
                        "\"value\":\"%s\"," +
                        "\"short\":true" +
                        "}" +
                        "]" +
                        "}" +
                        "]" +
                        "}",
                color, testName, masterId, buildResult, publicReportUrl);
               sendWebhookNotification(webhookUrl, jsonPayload);

    }
    private void sendWebhookNotification(String webhookUrl, String jsonPayload) {
        PrintStream logger = listener.getLogger();
        try{
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            byte[] postData = jsonPayload.getBytes(StandardCharsets.UTF_8);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(postData);


            int responseCode = connection.getResponseCode();
            logger.println(BzmJobNotifier.formatMessage("Response Code: " + responseCode));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
        private void writeObject(java.io.ObjectOutputStream out)
            throws IOException{

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException{

    }

}