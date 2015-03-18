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
import hudson.plugins.blazemeter.testresult.TestResult;
import hudson.plugins.blazemeter.testresult.TestResultFactory;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.BzmServiceManager;
import hudson.plugins.blazemeter.utils.JsonConstants;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    private String apiKey = "";

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
    public PerformanceBuilder(String apiKey,
                              String testDuration,
                              String mainJMX,
                              String dataFolder,
                              String testId,
                              String apiVersion,
                              String jsonConfig,
                              boolean useServerTresholds,
                              String errorFailedThreshold,
                              String errorUnstableThreshold,
                              String responseTimeFailedThreshold,
                              String responseTimeUnstableThreshold
    ) {
        this.apiKey= BzmServiceManager.selectUserKeyOnId(DESCRIPTOR, apiKey);
        this.errorFailedThreshold = errorFailedThreshold;
        this.errorUnstableThreshold = errorUnstableThreshold;
        this.testId = testId;
        this.apiVersion = apiVersion.equals("autoDetect")?
                BzmServiceManager.autoDetectApiVersion(this.apiKey, jenCommonLog):apiVersion;
    /*  TODO
    This calls are not implemented in v.2.0
    Therefor they will be hidden from GUI
    Should be implemented in v.2.1

        this.mainJMX = mainJMX;
        this.dataFolder = dataFolder;
    */
        this.jsonConfig = jsonConfig;
        this.useServerTresholds=useServerTresholds;
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
        this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
        APIFactory apiFactory = APIFactory.getApiFactory();
        this.api = apiFactory.getAPI(apiKey, APIFactory.ApiVersion.valueOf(this.apiVersion));
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
        this.api = getAPIClient();
        this.api.setLogger(bzmBuildLog);
        bzmBuildLog.setDebugEnabled(true);
        this.api.getBzmHttpWr().setLogger(bzmBuildLog);
        this.api.getBzmHttpWr().setLogger(bzmBuildLog);
        /*
        TODO
        1. Get BzmHttpClient
        2. Set logger
         */
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
             session=this.getTestSession(json, build);
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

            Result result = this.postProcess(session, build);

            build.setResult(result);

            return true;
        } catch (Exception e){
            jenCommonLog.warn("Test execution was interrupted or network connection is broken: ", e);
            jenBuildLog.warn("Test execution was interrupted or network connection is broken: check test state on server");
            return true;

        }

        finally {
            TestInfo info = this.api.getTestRunStatus(apiVersion.equals("v2") ? testId : session);

            TestStatus status = info.getStatus();
            if (status.equals(TestStatus.Running)) {
                bzmBuildLog.info("Shutting down test");
                this.api.stopTest(testId);
            } else if (status.equals(TestStatus.NotFound)) {
                build.setResult(Result.FAILURE);
                bzmBuildLog.warn("Test not found error");
            } else if (status.equals(TestStatus.Error)) {
                build.setResult(Result.FAILURE);
                jenBuildLog.warn("Test is not running on server. Check logs for detailed errors");
            }
        }
    }


    /** TODO
     * 1. Get rid of this method, it's legacy code
     * 2. Make Utils.getAPIKey() from CredentialsProvider;
     */

    private BlazemeterApi getAPIClient() {

        // ideally, at this point we'd look up the credential based on the API key to find the secret
        // but there are no secrets, so no need to!
        return APIFactory.getApiFactory().getAPI(apiKey,APIFactory.ApiVersion.valueOf(this.apiVersion));
    }


    /**
     * TODO
     * 1. Split this method into two parts depending on
     * API version and place code to appropriate BlazeMeterAPIImpl
     * 2. Remove this method, it's legacy
     *
     * @param json
     * @param build
     * @return
     * @throws JSONException
     */
   private String getTestSession(JSONObject json, AbstractBuild<?, ?> build) throws JSONException{
       String session="";
       try {
           if (apiVersion.equals(APIFactory.ApiVersion.v2.name()) && !json.get(JsonConstants.RESPONSE_CODE).equals(200)) {
           if (json.get(JsonConstants.RESPONSE_CODE).equals(500) && json.get(JsonConstants.ERROR).toString()
                   .startsWith("Test already running")) {
               bzmBuildLog.warn("Test already running, please stop it first");
               build.setResult(Result.FAILURE);
               return session;
           }

       }
       // get sessionId add to interface
           if (apiVersion.equals(APIFactory.ApiVersion.v2.name())) {
           session = json.get("session_id").toString();

       } else {

               JSONObject startJO = (JSONObject) json.get(JsonConstants.RESULT);
               session = ((JSONArray) startJO.get("sessionsId")).get(0).toString();
               String reportUrl= BzmServiceManager.getReportUrl(this.api, session, jenBuildLog, bzmBuildLog);
               jenBuildLog.info("Blazemeter test report will be available at " + reportUrl);
               jenBuildLog.info("Blazemeter test log will be available at " + build.getLogFile().getParent() + "/" + Constants.BZM_JEN_LOG);

               PerformanceBuildAction a = new PerformanceBuildAction(build);
               a.setReportUrl(reportUrl);
               build.addAction(a);
           }

       }catch (Exception e) {
           jenBuildLog.info("Failed to get session_id: "+e.getMessage());
           bzmBuildLog.info("Failed to get session_id. ",e);
       }
   return session;
   }

    /*
    TODO
    1. Split into several small logical methods.
    2. Move these parts to Utils
    3. Remove this method: it's legacy
     */
    private Result postProcess(String session, AbstractBuild<?, ?> build) throws InterruptedException {
        Thread.sleep(10000); // Wait for the report to generate.
        //get tresholds from server and check if test is success
        String junitReport="";
        Result result = Result.SUCCESS;
        try{
            junitReport = this.api.retrieveJUNITXML(session);
        }catch (Exception e){
            jenBuildLog.warn("Problems with receiving JUNIT report from server: "+e.getMessage());
        }
        bzmBuildLog.info("Received Junit report from server.... Saving it to the disc...");
        BzmServiceManager.saveReport(Constants.BM_TRESHOLDS, junitReport, build.getWorkspace(), bzmBuildLog, jenBuildLog);
        BzmServiceManager.getJTL(this.api, session, build.getWorkspace(), jenBuildLog, bzmBuildLog);
        if(this.useServerTresholds){
         jenBuildLog.info("UseServerTresholds flag is set to TRUE, Server tresholds will be validated.");
         result= BzmServiceManager.validateServerTresholds(this.api,session,jenBuildLog);
        }
        //get testGetArchive information
        JSONObject testReport=null;
        try{

        testReport = this.api.testReport(session);
        }catch (Exception e){
           bzmBuildLog.info("Failed to get test report from server.");
        }


        if (testReport == null || testReport.equals("null")) {
            bzmBuildLog.warn("Requesting aggregate is not available. " +
                    "Build won't be validated against local tresholds");
            return result;
        }

        TestResultFactory testResultFactory = TestResultFactory.getTestResultFactory();
        testResultFactory.setVersion(APIFactory.ApiVersion.valueOf(apiVersion));
        TestResult testResult = null;
        Result localTresholdsResult=null;
        try {
            testResult = testResultFactory.getTestResult(testReport);
            bzmBuildLog.info(testResult.toString());
            bzmBuildLog.info("Validating local tresholds...\n");
            localTresholdsResult= BzmServiceManager.validateLocalTresholds(testResult, this, jenBuildLog);
        } catch (IOException ioe) {
            jenBuildLog.info("Failed to get test result. Try to check server for it");
            bzmBuildLog.info("ERROR: Failed to generate TestResult: " + ioe);
        } catch (JSONException je) {
            jenBuildLog.info("Failed to get test result. Try to check server for it");
            bzmBuildLog.info("ERROR: Failed to generate TestResult: " + je);
        }finally{
            return localTresholdsResult!=null?localTresholdsResult:result;
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
