package hudson.plugins.blazemeter;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.Computer;
import hudson.model.Result;
import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.ApiV3Impl;
import hudson.plugins.blazemeter.api.TestType;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JobUtility;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.remoting.Callable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.PrintStream;
import java.util.Calendar;

/**
 * Created by zmicer on 15.6.16.
 */
public class BlazeMeterBuild implements Callable<Result, Exception> {
    private String jobApiKey = "";

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

    private String logDir = null;

    @Override
    public Result call() throws Exception {
        Result result = Result.SUCCESS;
        Computer c = Computer.currentComputer();
        if (StringUtils.isBlank(this.logDir)) {
            this.logDir = System.getProperty("user.dir") +
                    File.separator + Constants.BZM_SLAVE_LOGS +
                    File.separator + this.jobName +
                    File.separator + this.buildId;
        }
        File ld = JobUtility.mkLogDir(this.logDir);
        File httpLog_f = new File(ld, Constants.HTTP_LOG);
        File bzmLog_f = new File(ld, Constants.BZM_LOG);
        FileUtils.touch(httpLog_f);
        FileUtils.touch(bzmLog_f);

        PrintStream bzmLog_str = new PrintStream(bzmLog_f);
        StdErrLog bzmLog = new StdErrLog(Constants.BZM_JEN);
        bzmLog.setStdErrStream(bzmLog_str);
        bzmLog.setDebugEnabled(true);

        PrintStream httpLog_str = new PrintStream(httpLog_f);
        StdErrLog httpLog = new StdErrLog(Constants.BZM_JEN);
        httpLog.setStdErrStream(httpLog_str);
        httpLog.setDebugEnabled(true);

        Api api = new ApiV3Impl(this.jobApiKey, this.serverUrl);
        api.setLogger(bzmLog);
        api.getHttp().setLogger(httpLog);

        String userEmail = JobUtility.getUserEmail(this.jobApiKey, this.serverUrl);
        String apiKeyTrimmed = this.jobApiKey.substring(0, 4);
        if (userEmail.isEmpty()) {
            ProxyConfiguration proxy = ProxyConfiguration.load();
            bzmLog.warn("Please, check that settings are valid.");
            bzmLog.warn("UserKey=" + apiKeyTrimmed + ", serverUrl=" + this.serverUrl);
            bzmLog.warn("ProxyHost=" + proxy.name);
            bzmLog.warn("ProxyPort=" + proxy.port);
            bzmLog.warn("ProxyUser=" + proxy.getUserName());
            String proxyPass = proxy.getPassword();
            bzmLog.warn("ProxyPass=" + (StringUtils.isBlank(proxyPass) ? "" : proxyPass.substring(0, 3)) + "...");
            return Result.FAILURE;
        }
        bzmLog.warn("BlazeMeter plugin version =" + JobUtility.getVersion());
        bzmLog.warn("User key =" + apiKeyTrimmed + " is valid with " + this.serverUrl);
        bzmLog.warn("User's e-mail=" + userEmail);
        TestType testType = null;
        try {
            testType = Utils.getTestType(this.testId);
        } catch (Exception e) {
            bzmLog.warn("Failed to detect testType for starting test=" + e);
        }
        String testId_num = Utils.getTestId(this.testId);
        bzmLog.info("TestId=" + this.testId);
        bzmLog.info("Test type=" + testType.toString());
        String masterId = "";
        bzmLog.info("### About to start BlazeMeter test # " + testId_num);
        bzmLog.info("Timestamp: " + Calendar.getInstance().getTime());
        EnvVars envVars = EnvVars.getRemote(ws.getChannel());
        try {
            masterId = api.startTest(testId_num, testType);
            if (masterId.isEmpty()) {
                return Result.FAILURE;
            }
        } catch (JSONException e) {
            bzmLog.warn("Unable to start test: check userKey, testId, server url.");
            bzmLog.warn("Exception while starting BlazeMeter Test ", e);
            return Result.FAILURE;
        } catch (Exception e) {
            bzmLog.warn("Unable to start test: check userKey, testId, server url.");
            bzmLog.warn("Exception while starting BlazeMeter Test ", e);
            return Result.FAILURE;
        }
        /*
        TODO
        JobUtility.publishReport(api,masterId,build,jenBuildLog,bzmBuildLog);
         */

        JobUtility.notes(api, masterId, this.notes, bzmLog);
        try {
            if (!StringUtils.isBlank(this.sessionProperties)) {
                JSONArray props = JobUtility.prepareSessionProperties(this.sessionProperties, envVars, bzmLog);
                JobUtility.properties(api, props, masterId, bzmLog);
            }
            JobUtility.waitForFinish(api, testId_num, bzmLog, masterId);

            bzmLog.info("BlazeMeter test# " + testId_num + " was terminated at " + Calendar.getInstance().getTime());
            result = JobUtility.postProcess(this.ws,
                    buildId,
                    api,
                    masterId,
                    envVars,
                    this.isGetJunit(),
                    this.junitPath,
                    this.isGetJtl(),
                    this.getJtlPath(),
                    bzmLog);
            return result;
        } catch (InterruptedException e) {
            bzmLog.warn("Job was stopped by user");
            return Result.SUCCESS;
        } catch (Exception e) {
            bzmLog.warn("Job was stopped due to unknown reason", e);
            return Result.FAILURE;
        } finally {
            TestStatus testStatus = api.getTestStatus(masterId);

            if (testStatus.equals(TestStatus.Running)) {
                bzmLog.info("Shutting down test");
                JobUtility.stopTestSession(api, masterId, bzmLog);
                return Result.ABORTED;
            } else if (testStatus.equals(TestStatus.NotFound)) {
                bzmLog.warn("Test not found error");
                return Result.FAILURE;
            } else if (testStatus.equals(TestStatus.Error)) {
                bzmLog.warn("Test is not running on server. Check logs for detailed errors");
                return Result.FAILURE;
            }
            /*
            TODO
            - move log file to workspace
            */
        }
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSessionProperties() {
        return sessionProperties;
    }

    public void setSessionProperties(String sessionProperties) {
        this.sessionProperties = sessionProperties;
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

    public boolean isGetJtl() {
        return getJtl;
    }

    public void setGetJtl(boolean getJtl) {
        this.getJtl = getJtl;
    }

    public boolean isGetJunit() {
        return getJunit;
    }

    public void setGetJunit(boolean getJunit) {
        this.getJunit = getJunit;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public FilePath getWs() {
        return ws;
    }

    public void setWs(FilePath ws) {
        this.ws = ws;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }
}
