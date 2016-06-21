package hudson.plugins.blazemeter;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.ProxyConfiguration;
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

    private EnvVars ev = null;

    @Override
    public Result call() throws Exception {
        Result result;
        StringBuilder lentry=new StringBuilder();
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

        StdErrLog bls=new StdErrLog(Constants.BZM_JEN);

        Api api = new ApiV3Impl(this.jobApiKey, this.serverUrl);
        api.setLogger(bzmLog);
        api.getHttp().setLogger(httpLog);

        String userEmail = JobUtility.getUserEmail(this.jobApiKey, this.serverUrl);
        String apiKeyTrimmed = this.jobApiKey.substring(0, 4)+"...";
        if (userEmail.isEmpty()) {
            ProxyConfiguration proxy = ProxyConfiguration.load();
            lentry.append("Please, check that settings are valid.");
            bzmLog.warn(lentry.toString());
            bls.warn(lentry.toString());
            lentry.setLength(0);

            lentry.append("UserKey=" + apiKeyTrimmed + ", serverUrl=" + this.serverUrl);
            bzmLog.warn(lentry.toString());
            bls.warn(lentry.toString());
            lentry.setLength(0);

            lentry.append("ProxyHost=" + proxy.name);
            bzmLog.warn(lentry.toString());
            bls.warn(lentry.toString());
            lentry.setLength(0);

            lentry.append("ProxyPort=" + proxy.port);
            bzmLog.warn(lentry.toString());
            bls.warn(lentry.toString());
            lentry.setLength(0);

            lentry.append("ProxyUser=" + proxy.getUserName());
            bzmLog.warn(lentry.toString());
            bls.warn(lentry.toString());
            lentry.setLength(0);

            String proxyPass = proxy.getPassword();

            lentry.append("ProxyPass=" + (StringUtils.isBlank(proxyPass) ? "" : proxyPass.substring(0, 3)) + "...");
            bzmLog.warn(lentry.toString());
            bls.warn(lentry.toString());
            lentry.setLength(0);

            return Result.FAILURE;
        }

        lentry.append("BlazeMeter plugin version =" + JobUtility.getVersion());
        bzmLog.warn(lentry.toString());
        bls.warn(lentry.toString());
        lentry.setLength(0);

        lentry.append("User key =" + apiKeyTrimmed + " is valid with " + this.serverUrl);
        bzmLog.warn(lentry.toString());
        bls.warn(lentry.toString());
        lentry.setLength(0);

        lentry.append("User's e-mail=" + userEmail);
        bzmLog.warn(lentry.toString());
        bls.warn(lentry.toString());
        lentry.setLength(0);

        TestType testType = null;
        try {
            testType = Utils.getTestType(this.testId);
        } catch (Exception e) {
            lentry.append("Failed to detect testType for starting test=" + e);
            bzmLog.warn(lentry.toString());
            bls.warn(lentry.toString());
            lentry.setLength(0);
        }

        String testId_num = Utils.getTestId(this.testId);

        lentry.append("TestId=" + this.testId);
        bzmLog.warn(lentry.toString());
        bls.warn(lentry.toString());
        lentry.setLength(0);

        lentry.append("Test type=" + testType.toString());
        bzmLog.warn(lentry.toString());
        bls.warn(lentry.toString());
        lentry.setLength(0);

        String masterId = "";

        lentry.append("### About to start BlazeMeter test # " + testId_num);
        bzmLog.warn(lentry.toString());
        bls.warn(lentry.toString());
        lentry.setLength(0);

        lentry.append("Timestamp: " + Calendar.getInstance().getTime());
        bzmLog.warn(lentry.toString());
        bls.warn(lentry.toString());
        lentry.setLength(0);

        try {
            masterId = api.startTest(testId_num, testType);
            if (masterId.isEmpty()) {
                return Result.FAILURE;
            }
        } catch (JSONException e) {
            lentry.append("Unable to start test: check userKey, testId, server url.");
            bls.warn(lentry.toString()+e.getMessage());
            bzmLog.warn(lentry.toString(), e);
            lentry.setLength(0);
            return Result.FAILURE;
        } catch (Exception e) {
            lentry.append("Unable to start test: check userKey, testId, server url.");
            bls.warn(lentry.toString()+e.getMessage());
            bzmLog.warn(lentry.toString(), e);
            lentry.setLength(0);
            return Result.FAILURE;
        }

        String reportUrl= JobUtility.getReportUrl(api, masterId, bzmLog);
        lentry.append("BlazeMeter test report will be available at " + reportUrl);
        bls.warn(lentry.toString());
        bzmLog.warn(lentry.toString());
        lentry.setLength(0);

        bls.warn("For more detailed logs, please, refer to " + bzmLog_f.getCanonicalPath());
        bls.warn("Communication with BZM server is logged at " + httpLog_f.getCanonicalPath());

        ((EnvVars) EnvVars.masterEnvVars).put(this.jobName+"-"+this.buildId,reportUrl);
        JobUtility.notes(api, masterId, this.notes, bzmLog);
        try {
            if (!StringUtils.isBlank(this.sessionProperties)) {
                JSONArray props = JobUtility.prepareSessionProperties(this.sessionProperties, this.ev, bzmLog);
                JobUtility.properties(api, props, masterId, bzmLog);
            }
            JobUtility.waitForFinish(api, testId_num, bzmLog, masterId);

            lentry.append("BlazeMeter test# " + testId_num + " ended at " + Calendar.getInstance().getTime());
            bls.warn(lentry.toString());
            bzmLog.warn(lentry.toString());
            lentry.setLength(0);

            result = JobUtility.postProcess(this.ws,
                    buildId,
                    api,
                    masterId,
                    this.ev,
                    this.getJunit,
                    this.junitPath,
                    this.getJtl,
                    this.jtlPath,
                    bzmLog);
            return result;
        } catch (InterruptedException e) {
            lentry.append("Job was stopped by user");
            bls.warn(lentry.toString());
            bzmLog.warn(lentry.toString());
            lentry.setLength(0);
            return Result.SUCCESS;
        } catch (Exception e) {
            lentry.append("Job was stopped due to unknown reason");
            bls.warn(lentry.toString());
            bzmLog.warn(lentry.toString());
            lentry.setLength(0);
            return Result.FAILURE;
        } finally {
            TestStatus testStatus = api.getTestStatus(masterId);

            if (testStatus.equals(TestStatus.Running)) {
                lentry.append("Shutting down test");
                bls.warn(lentry.toString());
                bzmLog.warn(lentry.toString());
                lentry.setLength(0);
                JobUtility.stopTestSession(api, masterId, bzmLog);
                return Result.ABORTED;
            } else if (testStatus.equals(TestStatus.NotFound)) {
                lentry.append("Test not found error");
                bls.warn(lentry.toString());
                bzmLog.warn(lentry.toString());
                lentry.setLength(0);
                return Result.FAILURE;
            } else if (testStatus.equals(TestStatus.Error)) {
                lentry.append("Test is not running on server. Check http-log & bzm-log for detailed errors");
                bls.warn(lentry.toString());
                bzmLog.warn(lentry.toString());
                lentry.setLength(0);
                return Result.FAILURE;
            }
            lentry.append("Copying bzm log files to build workspace folder...");
            bls.warn(lentry.toString());
            bzmLog.warn(lentry.toString());
            lentry.setLength(0);

            FilePath log_p = new FilePath(ws, buildId);
            FilePath bzmLog_p=new FilePath(bzmLog_f);
            FilePath httpLog_p=new FilePath(httpLog_f);
            FilePath bzmLog_p_ws=new FilePath(log_p,bzmLog_f.getName());
            FilePath httpLog_p_ws=new FilePath(log_p,httpLog_f.getName());
            bzmLog_p_ws.copyFrom(bzmLog_p);
            httpLog_p_ws.copyFrom(httpLog_p);

            lentry.append("Deleting "+bzmLog_f.getCanonicalPath());
            bls.warn(lentry.toString());
            bzmLog.warn(lentry.toString());
            lentry.setLength(0);
            bzmLog_p.delete();

            lentry.append("Deleting "+httpLog_f.getCanonicalPath());
            bls.warn(lentry.toString());
            bzmLog.warn(lentry.toString());
            lentry.setLength(0);
            httpLog_p.delete();
        }
    }


    public void setEv(EnvVars ev) {
        this.ev = ev;
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

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }
}
