package hudson.plugins.blazemeter;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.entities.TestInfo;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.BzmServiceManager;
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

    private String testDuration = "";

/*  TODO
    This calls are not implemented in v.2.0
    Therefor they will be hidden from GUI
    Should be implemented in v.2.1

    private String mainJMX = "";

    private String dataFolder = "";
*/

    private String location = "";

    private String jsonConfig = "";

    private String errorFailedThreshold = "";

    private String errorUnstableThreshold = "";

    private String responseTimeFailedThreshold = "";

    private String responseTimeUnstableThreshold = "";

    private BlazemeterApi api = null;

    private AbstractBuild<?, ?> build=null;
    /**
     * @deprecated as of 1.3. for compatibility
     */
    private transient String filename;

    private boolean useServerTresholds;
    /**
     * Configured report parsers.
     */

    @DataBoundConstructor
    public PerformanceBuilder(String jobApiKey,
                              String testDuration,
                              String mainJMX,
                              String dataFolder,
                              String testId,
                              String apiVersion,
                              String location,
                              String jsonConfig,
                              boolean useServerTresholds,
                              String errorFailedThreshold,
                              String errorUnstableThreshold,
                              String responseTimeFailedThreshold,
                              String responseTimeUnstableThreshold
    ) {
        this.jobApiKey = BzmServiceManager.selectUserKeyOnId(DESCRIPTOR, jobApiKey);
        this.errorFailedThreshold = errorFailedThreshold;
        this.errorUnstableThreshold = errorUnstableThreshold;
        this.testId = testId;
        this.apiVersion = apiVersion.equals("autoDetect")?
                BzmServiceManager.autoDetectApiVersion(this.jobApiKey):apiVersion;
    /*  TODO
    This calls are not implemented in v.2.0
    Therefor they will be hidden from GUI
    Should be implemented in v.2.1

        this.mainJMX = mainJMX;
        this.dataFolder = dataFolder;
    */
        this.location = location;
        this.jsonConfig = jsonConfig;
        this.useServerTresholds=useServerTresholds;
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
        this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
        APIFactory apiFactory = APIFactory.getApiFactory();
        this.api = apiFactory.getAPI(jobApiKey, APIFactory.ApiVersion.valueOf(this.apiVersion));
        this.testDuration=testDuration;
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
        this.api = APIFactory.getApiFactory().getAPI(jobApiKey, APIFactory.ApiVersion.valueOf(this.apiVersion));
        this.api.setLogger(bzmBuildLog);
        bzmBuildLog.setDebugEnabled(true);
        this.api.getBzmHttpWr().setLogger(bzmBuildLog);
        this.api.getBzmHttpWr().setLogger(bzmBuildLog);

        String userEmail=BzmServiceManager.getUserEmail(this.jobApiKey);
        String userKeyId=BzmServiceManager.selectUserKeyId(DESCRIPTOR,this.jobApiKey);
        if(userEmail.isEmpty()){
            jenBuildLog.warn("Invalid user key. UserKey="+userKeyId+", serverUrl="+APIFactory.getApiFactory().getBlazeMeterUrl());
            return false;
        }
        jenBuildLog.warn("User key ="+userKeyId+" is valid with "+APIFactory.getApiFactory().getBlazeMeterUrl());
        jenBuildLog.warn("User's e-mail="+userEmail);

        this.testId= BzmServiceManager.prepareTestRun(this);
        if(this.testId.isEmpty()){
            jenBuildLog.warn("Failed to start test on server: check that JSON configuration is valid.");
            return false;
        }

        bzmBuildLog.info("Expected test duration=" + this.testDuration);

        org.json.JSONObject json;
        int countStartRequests = 0;
        do {
            bzmBuildLog.info("### About to start Blazemeter test # " + this.testId);
            bzmBuildLog.info("Attempt# " + (countStartRequests + 1));
            bzmBuildLog.info("Timestamp: " + Calendar.getInstance().getTime());
            json = this.api.startTest(testId);
            countStartRequests++;
            if (json == null && countStartRequests > 5) {
                bzmBuildLog.info("Could not start BlazeMeter Test with 5 attempts");
                build.setResult(Result.FAILURE);
                return false;
            }
        } while (json == null);

        String session;
        try {
             session=BzmServiceManager.getSessionId(json, build,APIFactory.ApiVersion.valueOf(this.apiVersion),
                     this.api,bzmBuildLog,jenBuildLog);
            if(session.isEmpty()){
                build.setResult(Result.FAILURE);
                return false;
            }
        } catch (JSONException e) {
            jenBuildLog.warn("Unable to start test: check userKey, testId, server url.");
            bzmBuildLog.warn("Exception while starting BlazeMeter Test ", e);
            return false;
        }

        // add the report to the build object.

        try {
            BzmServiceManager.waitForFinish(this.api, this.apiVersion, this.testId,
                    bzmBuildLog, session);

            bzmBuildLog.info("BlazeMeter test# " + this.testId + " was terminated at " + Calendar.getInstance().getTime());

            Result result = BzmServiceManager.postProcess(this,session);

            build.setResult(result);

            return true;
        } catch (Exception e){
            jenCommonLog.warn("Test execution was interrupted or network connection is broken: ", e);
            jenBuildLog.warn("Test execution was interrupted or network connection is broken: check test state on server");
            return true;

        }

        finally {
            TestInfo info = this.api.getTestInfo(apiVersion.equals("v2") ? testId : session);

            TestStatus status = info.getStatus();
            if (status.equals(TestStatus.Running)) {
                bzmBuildLog.info("Shutting down test");
                BzmServiceManager.stopTestSession(this.api, testId, session, jenBuildLog);
                build.setResult(Result.ABORTED);
            } else if (status.equals(TestStatus.NotFound)) {
                build.setResult(Result.FAILURE);
                bzmBuildLog.warn("Test not found error");
            } else if (status.equals(TestStatus.Error)) {
                build.setResult(Result.FAILURE);
                jenBuildLog.warn("Test is not running on server. Check logs for detailed errors");
            }
        }
    }



    public String getResponseTimeFailedThreshold() {
        return responseTimeFailedThreshold;
    }

    public void setResponseTimeFailedThreshold(String responseTimeFailedThreshold) {
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
    }

    public String getResponseTimeUnstableThreshold() {
        return responseTimeUnstableThreshold;
    }

    public void setResponseTimeUnstableThreshold(String responseTimeUnstableThreshold) {
        this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
    }

    public String getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(String testDuration) {
        this.testDuration = testDuration;
    }

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

    /* TODO
        This calls are not implemented in v.2.0
        Therefor they will be hidden from GUI
        Should be implemented in v.2.1

        public String getMainJMX() {
            return mainJMX;
        }

        public void setMainJMX(String mainJMX) {
            this.mainJMX = mainJMX;
        }

        public String getDataFolder() {
            return dataFolder;
        }

        public void setDataFolder(String dataFolder) {
            this.dataFolder = dataFolder;
        }
    */
    public String getErrorFailedThreshold() {
        return errorFailedThreshold;
    }

    public void setErrorFailedThreshold(String errorFailedThreshold) {
        this.errorFailedThreshold = errorFailedThreshold;
    }

    public String getErrorUnstableThreshold() {
        return errorUnstableThreshold;
    }

    public void setErrorUnstableThreshold(String errorUnstableThreshold) {
        this.errorUnstableThreshold = errorUnstableThreshold;
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

    public String getJsonConfig() {
        return jsonConfig;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isUseServerTresholds() {
        return useServerTresholds;
    }

    public void setUseServerTresholds(boolean useServerTresholds) {
        this.useServerTresholds = useServerTresholds;
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
