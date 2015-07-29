package hudson.plugins.blazemeter;

import hudson.Extension;
import hudson.Launcher;
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
import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PerformanceBuilder extends Builder {
    DateFormat df = new SimpleDateFormat("dd/MM/yy");
    private static AbstractLogger jenCommonLog =new JavaUtilLog(Constants.BZM_JEN);
    private static StdErrLog bzmBuildLog =new StdErrLog(Constants.BZM_JEN);
    private StdErrLog jenBuildLog;

    private String jobApiKey = "";

    private String testId = "";

    private String apiVersion = "v3";

//    private String testDuration = "";

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
//                              String testDuration,
                              String testId,
                              String apiVersion,
                              boolean getJtl,
                              boolean getJunit
    ) {
        this.jobApiKey = BzmServiceManager.selectUserKeyOnId(DESCRIPTOR, jobApiKey);
        this.testId = testId;
        this.apiVersion = apiVersion.equals("autoDetect")?
                BzmServiceManager.autoDetectApiVersion(this.jobApiKey,DESCRIPTOR.getBlazeMeterURL()):apiVersion;
        this.api = APIFactory.getAPI(jobApiKey, ApiVersion.valueOf(this.apiVersion),DESCRIPTOR.getBlazeMeterURL());
//        this.testDuration=testDuration;
        this.getJtl=getJtl;
        this.getJunit=getJunit;
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
        File bzmLogFile = new File(build.getLogFile().getParentFile()+"/"+Constants.BZM_JEN_LOG);
        if(!bzmLogFile.exists()){
            bzmLogFile.createNewFile();
        }
        PrintStream bzmBuildLogStream = new PrintStream(bzmLogFile);
        bzmBuildLog.setStdErrStream(bzmBuildLogStream);
        this.api = APIFactory.getAPI(jobApiKey, ApiVersion.valueOf(this.apiVersion), DESCRIPTOR.getBlazeMeterURL());
        this.api.setLogger(bzmBuildLog);
        bzmBuildLog.setDebugEnabled(true);
        this.api.getBzmHttpWr().setLogger(bzmBuildLog);
        this.api.getBzmHttpWr().setLogger(bzmBuildLog);

        String userEmail=BzmServiceManager.getUserEmail(this.jobApiKey,DESCRIPTOR.getBlazeMeterURL());
        String userKeyId=BzmServiceManager.selectUserKeyId(DESCRIPTOR,this.jobApiKey);
        if(userEmail.isEmpty()){
            jenBuildLog.warn("Invalid user key. UserKey="+userKeyId+", serverUrl="+DESCRIPTOR.getBlazeMeterURL());
            return false;
        }
        jenBuildLog.warn("User key ="+userKeyId+" is valid with "+DESCRIPTOR.getBlazeMeterURL());
        jenBuildLog.warn("User's e-mail="+userEmail);
        TestType testType= Utils.getTestType(this.testId);
        this.testId=Utils.getTestId(this.testId);
        // implemented only with V3
        /*if(this.api instanceof BlazemeterApiV3Impl){
            this.testId= BzmServiceManager.prepareTestRun(this);
            if(this.testId.isEmpty()){
                jenBuildLog.warn("Failed to start test on server: check that JSON configuration is valid.");
                return false;
            }
        }*/


//        bzmBuildLog.info("Expected test duration=" + this.testDuration);
        String masterId="";
        bzmBuildLog.info("### About to start Blazemeter test # " + this.testId);
        bzmBuildLog.info("Timestamp: " + Calendar.getInstance().getTime());

        try {
            masterId = api.startTest(testId,testType);
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

        // add the report to the build object.

        BzmServiceManager.publishReport(this.api,masterId,build,jenBuildLog,bzmBuildLog);

        try {
            BzmServiceManager.waitForFinish(this.api, this.apiVersion, this.testId,
                    bzmBuildLog, masterId);

            bzmBuildLog.info("BlazeMeter test# " + this.testId + " was terminated at " + Calendar.getInstance().getTime());

            Result result = BzmServiceManager.postProcess(this,masterId);

            build.setResult(result);

            return true;
        } catch (Exception e){
            jenCommonLog.warn("Test execution was interrupted or network connection is broken: ", e);
            jenBuildLog.warn("Test execution was interrupted or network connection is broken: check test state on server");
            return true;

        }

        finally {
            TestStatus testStatus = this.api.getTestStatus(apiVersion.equals("v2") ? testId : masterId);

            if (testStatus.equals(TestStatus.Running)) {
                bzmBuildLog.info("Shutting down test");
                BzmServiceManager.stopTestSession(this.api, masterId, jenBuildLog);
                build.setResult(Result.ABORTED);
            } else if (testStatus.equals(TestStatus.NotFound)) {
                build.setResult(Result.FAILURE);
                bzmBuildLog.warn("Test not found error");
            } else if (testStatus.equals(TestStatus.Error)) {
                build.setResult(Result.FAILURE);
                jenBuildLog.warn("Test is not running on server. Check logs for detailed errors");
            }
        }
    }



/*
    public String getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(String testDuration) {
        this.testDuration = testDuration;
    }
*/

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
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
