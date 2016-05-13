package hudson.plugins.blazemeter;

import hudson.*;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.blazemeter.api.*;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.BzmServiceManager;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class PerformanceBuilder extends Builder {
    private static StdErrLog bzmBuildLog =new StdErrLog(Constants.BZM_JEN);
    private StdErrLog jenBuildLog;

    private String jobApiKey = "";

    private String testId = "";

    private String notes = "";

    private String sessionProperties = "";

    private String jtlPath = "";

    private String junitPath = "";

    private boolean getJtl = false;

    private boolean getJunit = false;

    private BlazemeterApi api = null;

    private AbstractBuild<?, ?> build=null;
    /**
     * @deprecated as of 1.3. for compatibility
     */
    private transient String filename;

    /**
     * Configured report parsers.
     */

    @DataBoundConstructor
    public PerformanceBuilder(String jobApiKey,
                              String testId,
                              String notes,
                              String sessionProperties,
                              String jtlPath,
                              String junitPath,
                              boolean getJtl,
                              boolean getJunit
    ) {
        this.jobApiKey = BzmServiceManager.selectUserKeyOnId(DESCRIPTOR, jobApiKey);
        this.testId = testId;
        this.api = new BlazemeterApiV3Impl(jobApiKey,DESCRIPTOR.getBlazeMeterURL());
        this.jtlPath = jtlPath;
        this.junitPath = junitPath;
        this.getJtl=getJtl;
        this.getJunit=getJunit;
        this.notes=notes;
        this.sessionProperties = sessionProperties;
    }


    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }




    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener) throws InterruptedException, IOException {
        this.build=build;
        jenBuildLog=new StdErrLog(Constants.BUILD_JEN);
        jenBuildLog.setStdErrStream(listener.getLogger());
        File bzmLogFile = new File(build.getLogFile().getParentFile(),Constants.BZM_JEN_LOG);
        if(!bzmLogFile.exists()){
            FileUtils.forceMkdir(bzmLogFile.getParentFile());
            bzmLogFile.createNewFile();
        }
        PrintStream bzmBuildLogStream = new PrintStream(bzmLogFile);
        bzmBuildLog.setStdErrStream(bzmBuildLogStream);
        this.api = new BlazemeterApiV3Impl(jobApiKey, DESCRIPTOR.getBlazeMeterURL());
        this.api.setLogger(jenBuildLog);
        bzmBuildLog.setDebugEnabled(true);
        this.api.getBzmHttpWr().setLogger(bzmBuildLog);
        this.api.getBzmHttpWr().setLogger(bzmBuildLog);

        String userEmail=BzmServiceManager.getUserEmail(this.jobApiKey,DESCRIPTOR.getBlazeMeterURL());
        String userKeyId=BzmServiceManager.selectUserKeyId(DESCRIPTOR,this.jobApiKey);
        if(userEmail.isEmpty()){
            ProxyConfiguration proxy=ProxyConfiguration.load();
            jenBuildLog.warn("Please, check that settings are valid.");
            jenBuildLog.warn("UserKey=" + userKeyId + ", serverUrl=" + DESCRIPTOR.getBlazeMeterURL());
            jenBuildLog.warn("ProxyHost=" + proxy.name);
            jenBuildLog.warn("ProxyPort=" + proxy.port);
            jenBuildLog.warn("ProxyUser=" + proxy.getUserName());
            String proxyPass=proxy.getPassword();
            jenBuildLog.warn("ProxyPass=" + (StringUtils.isBlank(proxyPass)?"":proxyPass.substring(0,3))+"...");
            return false;
        }
        jenBuildLog.warn("BlazeMeter plugin version ="+BzmServiceManager.getVersion());
        jenBuildLog.warn("User key ="+userKeyId+" is valid with "+DESCRIPTOR.getBlazeMeterURL());
        jenBuildLog.warn("User's e-mail="+userEmail);
        TestType testType= null;
        try {
            testType = Utils.getTestType(this.testId);
        } catch (Exception e) {
            jenBuildLog.warn("Failed to detect testType for starting test=" + e);
        }
        String testId_num=Utils.getTestId(this.testId);
        jenBuildLog.info("TestId="+this.testId);
        jenBuildLog.info("Test type="+testType.toString());
        String masterId="";
        bzmBuildLog.info("### About to start BlazeMeter test # " + testId_num);
        bzmBuildLog.info("Timestamp: " + Calendar.getInstance().getTime());
        EnvVars envVars = build.getEnvironment(listener);
        try {
            masterId = api.startTest(testId_num,testType);
            if(masterId.isEmpty()){
                build.setResult(Result.FAILURE);
                return false;
            }
        } catch (JSONException e) {
            jenBuildLog.warn("Unable to start test: check userKey, testId, server url.");
            bzmBuildLog.warn("Exception while starting BlazeMeter Test ", e);
            return false;
        } catch (Exception e) {
            jenBuildLog.warn("Unable to start test: check userKey, testId, server url.");
            bzmBuildLog.warn("Exception while starting BlazeMeter Test ", e);
            return false;
        }

        BzmServiceManager.publishReport(this.api,masterId,build,jenBuildLog,bzmBuildLog);
        jenBuildLog.info("BlazeMeter test log will be available at " + bzmLogFile.getAbsolutePath());

        BzmServiceManager.notes(this.api,masterId,this.notes,jenBuildLog);
        try {
            if(!StringUtils.isBlank(this.sessionProperties)){
                JSONArray props=BzmServiceManager.prepareSessionProperties(this.sessionProperties,envVars,jenBuildLog);
                BzmServiceManager.properties(this.api,props,masterId,jenBuildLog);
            }
            BzmServiceManager.waitForFinish(this.api,testId_num,bzmBuildLog, masterId);

            bzmBuildLog.info("BlazeMeter test# " + testId_num + " was terminated at " + Calendar.getInstance().getTime());

            Result result = BzmServiceManager.postProcess(this,masterId,envVars);

            build.setResult(result);

            return true;
        } catch (InterruptedException e){
            jenBuildLog.warn("Job was stopped by user");
            return true;
        }
            catch (Exception e){
            jenBuildLog.warn("Job was stopped due to unknown reason", e);
            return false;
        }

        finally {
            TestStatus testStatus = this.api.getTestStatus(masterId);

            if (testStatus.equals(TestStatus.Running)) {
                jenBuildLog.info("Shutting down test");
                BzmServiceManager.stopTestSession(this.api, masterId, jenBuildLog);
                build.setResult(Result.ABORTED);
            } else if (testStatus.equals(TestStatus.NotFound)) {
                build.setResult(Result.FAILURE);
                jenBuildLog.warn("Test not found error");
            } else if (testStatus.equals(TestStatus.Error)) {
                build.setResult(Result.FAILURE);
                jenBuildLog.warn("Test is not running on server. Check logs for detailed errors");
            }
            FilePath bzmLogPath=new FilePath(bzmLogFile.getParentFile());
            FilePath bzmLogPathWS=new FilePath(build.getWorkspace(),build.getId());
            jenBuildLog.warn("Copying bzm log files to build workspace: "+bzmLogPathWS.getRemote());
            bzmLogPath.copyRecursiveTo(bzmLogPathWS);
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public static StdErrLog getBzmBuildLog() {
        return bzmBuildLog;
    }

    public StdErrLog getJenBuildLog() {
        return jenBuildLog;
    }

    public BlazemeterApi getApi() {
        return api;
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

    @Override
    public BlazeMeterPerformanceBuilderDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final BlazeMeterPerformanceBuilderDescriptor DESCRIPTOR = new BlazeMeterPerformanceBuilderDescriptor();

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
