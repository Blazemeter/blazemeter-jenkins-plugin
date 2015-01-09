package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.Extension;
import hudson.FilePath;
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
import hudson.plugins.blazemeter.utils.Utils;
import hudson.security.ACL;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerformanceBuilder extends Builder {
    DateFormat df = new SimpleDateFormat("dd/MM/yy");
    private static AbstractLogger jenCommonLog =new JavaUtilLog(Constants.BZM_JEN);
    private static StdErrLog bzmBuildLog =new StdErrLog(Constants.BZM_JEN);
    private static StdErrLog jenBuildLog =new StdErrLog(Constants.BUILD_JEN);

    private String testId = "";

    private String apiVersion = "v3";

    private String testDuration = "";

    private String mainJMX = "";

    private String dataFolder = "";

    private String jsonConfig = "";

    private int errorFailedThreshold = 0;

    private int errorUnstableThreshold = 0;

    private int responseTimeFailedThreshold = 0;

    private int responseTimeUnstableThreshold = 0;

    private String testName="";

    private BlazemeterApi api = null;

    private AbstractBuild<?, ?> build=null;
    /**
     * @deprecated as of 1.3. for compatibility
     */
    private transient String filename;

    /**
     * Configured report parsers.
     */
    private List<PerformanceReportParser> parsers = null;

    @DataBoundConstructor
    public PerformanceBuilder(String testDuration,
                              String mainJMX,
                              String dataFolder,
                              String testId,
                              String apiVersion,
                              String jsonConfig,
                              String errorFailedThreshold,
                              String errorUnstableThreshold,
                              String responseTimeFailedThreshold,
                              String responseTimeUnstableThreshold,
                              String testName
    ) {
        this.errorFailedThreshold = Integer.valueOf(errorFailedThreshold.isEmpty()
                ?"-1":errorFailedThreshold);
        this.errorUnstableThreshold = Integer.valueOf(errorUnstableThreshold.isEmpty()
                ?"-1":errorUnstableThreshold);
        this.testId = testId;
        String apiKey=getDescriptor().getCredentials("Global").get(0).getApiKey();
        this.apiVersion = apiVersion.equals("autoDetect")?
                Utils.autoDetectApiVersion(apiVersion,apiKey,jenCommonLog):apiVersion;
        this.mainJMX = mainJMX;
        this.dataFolder = dataFolder;
        this.jsonConfig = jsonConfig;
        this.responseTimeFailedThreshold = Integer.valueOf(responseTimeFailedThreshold.isEmpty()
                ?"-1":responseTimeFailedThreshold);
        this.responseTimeUnstableThreshold = Integer.valueOf(responseTimeUnstableThreshold.isEmpty()
                ?"-1":responseTimeUnstableThreshold);
        APIFactory apiFactory = APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.valueOf(this.apiVersion));
        this.testName=testName;
        this.api = apiFactory.getAPI(apiKey);
        this.testDuration=testDuration;
    }


    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public List<PerformanceReportParser> getParsers() {
        return parsers;
    }

    /**
     * <p>
     * Delete the date suffix appended to the Performance result files by the
     * Maven Performance plugin
     * </p>
     *
     * @param performanceReportWorkspaceName self explanatory.
     * @return the name of the PerformanceReport in the Build
     */
    public static String getPerformanceReportBuildFileName(
            String performanceReportWorkspaceName) {
        String result = performanceReportWorkspaceName;
        if (performanceReportWorkspaceName != null) {
            Pattern p = Pattern.compile("-[0-9]*\\.xml");
            Matcher matcher = p.matcher(performanceReportWorkspaceName);
            if (matcher.find()) {
                result = matcher.replaceAll(".xml");
            }
        }
        return result;
    }

    /**
     * look for blazemeter reports based in the configured parameter includes.
     * 'includes' is - an Ant-style pattern - a list of files and folders
     * separated by the characters ;:,
     */
    protected static List<FilePath> locatePerformanceReports(FilePath workspace,
                                                             String includes) throws IOException, InterruptedException {

        // First use ant-style pattern
        try {
            FilePath[] ret = workspace.list(includes);
            if (ret.length > 0) {
                return Arrays.asList(ret);
            }
        } catch (IOException e) {
            // Do nothing.
        }

        // If it fails, do a legacy search
        ArrayList<FilePath> files = new ArrayList<FilePath>();
        String parts[] = includes.split("\\s*[;:,]+\\s*");
        for (String path : parts) {
            FilePath src = workspace.child(path);
            if (src.exists()) {
                if (src.isDirectory()) {
                    files.addAll(Arrays.asList(src.list("**/*")));
                } else {
                    files.add(src);
                }
            }
        }
        return files;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener) throws InterruptedException, IOException {
        this.build=build;
        jenBuildLog.setStdErrStream(listener.getLogger());
        File bzmLogFile = new File(build.getLogFile().getParentFile()+"/"+Constants.BZM_JEN_LOG);
        if(!bzmLogFile.exists()){
            bzmLogFile.createNewFile();
        }
        PrintStream bzmBuildLogStream = new PrintStream(bzmLogFile);
        bzmBuildLog.setStdErrStream(bzmBuildLogStream);
        this.api.setLogger(bzmBuildLog);

        this.api = getAPIClient(build);
        this.testId=Utils.prepareTestRun(this);
        if(this.testId.isEmpty()){
            jenBuildLog.warn("Failed to start test on server: check JSON configuration or url format");
            return false;
        }
        bzmBuildLog.info("Expected test duration=" + this.testDuration);

        int runDurationSeconds = Integer.parseInt(testDuration) * 60;


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
            bzmBuildLog.warn("Exception while starting BlazeMeter Test ", e);
            return false;
        }

        // add the report to the build object.
        PerformanceBuildAction a = new PerformanceBuildAction(build, bzmBuildLogStream, parsers);
        a.setSession(session);
        APIFactory apiFactory=APIFactory.getApiFactory();
        a.setBlazeMeterURL(apiFactory.getBlazeMeterUrl());
        build.addAction(a);

        try {
            Utils.waitForFinish(this.api, this.apiVersion, this.testId,
                    bzmBuildLog, session, runDurationSeconds);

            bzmBuildLog.info("BlazeMeter test# " + this.testId + " was terminated at " + Calendar.getInstance().getTime());

            Result result = this.postProcess(session, bzmBuildLog,build);

            build.setResult(result);

            return true;
        } catch (Exception e){
            jenCommonLog.warn("Test execution was interrupted or network connection is broken: ", e);
            jenBuildLog.warn("Test execution was interrupted or network connection is broken: ", e);
            return true;

        }

        finally {
            TestInfo info = this.api.getTestRunStatus(apiVersion.equals("v2") ? testId : session);

            String status = info.getStatus();
            if (status.equals(TestStatus.Running)) {
                bzmBuildLog.info("Shutting down test");
                this.api.stopTest(testId);
            } else if (status.equals(TestStatus.Error)) {
                build.setResult(Result.FAILURE);
                bzmBuildLog.warn("Error while running a test - please try to run the same test on BlazeMeter");
            } else if (status.equals(TestStatus.NotFound)) {
                build.setResult(Result.FAILURE);
                bzmBuildLog.warn("Test not found error");
            }
        }
    }

    private BlazemeterApi getAPIClient(AbstractBuild<?, ?> build) {
        String apiKeyId = getDescriptor().getApiKey();
        String apiKey = null;
        for (BlazemeterCredential c : CredentialsProvider
                .lookupCredentials(BlazemeterCredential.class, build.getProject(), ACL.SYSTEM)) {
            if (StringUtils.equals(apiKeyId, c.getId())) {
             apiKey = c.getApiKey();
                break;
            }
        }

        // ideally, at this point we'd look up the credential based on the API key to find the secret
        // but there are no secrets, so no need to!
        return APIFactory.getApiFactory().getAPI(apiKey);
    }


   private String getTestSession(JSONObject json, AbstractBuild<?, ?> build) throws JSONException{
       String session="";
       if (apiVersion.equals(APIFactory.ApiVersion.v2.name()) && !json.get("response_code").equals(200)) {
           if (json.get("response_code").equals(500) && json.get("error").toString()
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
           JSONObject startJO = (JSONObject) json.get("result");
           session = ((JSONArray) startJO.get("sessionsId")).get(0).toString();
           APIFactory apiFactory=APIFactory.getApiFactory();
           jenBuildLog.info("Blazemeter test report will be available at " + apiFactory.getBlazeMeterUrl() + "/app/#report/" + session + "/loadreport");
           jenBuildLog.info("Blazemeter test log will be available at " + build.getLogFile().getParent()+"/"+Constants.BZM_JEN_LOG);
       }
   return session;
   }

    private Result postProcess(String session, StdErrLog bzmBuildLog,AbstractBuild<?, ?> build) throws InterruptedException {
        Thread.sleep(10000); // Wait for the report to generate.
        //get tresholds from server and check if test is success
        JSONObject jo = this.api.getTresholds(session);
        boolean success=false;
        try {
            bzmBuildLog.info("Treshold object = " + jo.toString());
            success=jo.getJSONObject("result").getJSONObject("data").getBoolean("success");
        } catch (JSONException je) {
            bzmBuildLog.warn("Error: Failed to get tresholds: " + je + "\n" + jo.toString());
        }
        String junitReport = this.api.retrieveJUNITXML(session);
        bzmBuildLog.info("Received Junit report from server.... Saving it to the disc...");
        Utils.saveReport(session, junitReport, build.getWorkspace(),bzmBuildLog);

        bzmBuildLog.info("Validating server tresholds: " + (success ? "PASSED" : "FAILED") + "\n");

        Result result = success?Result.SUCCESS:Result.FAILURE;
        if(result.equals(Result.FAILURE)){
            return result;
        }

        //get testGetArchive information
        JSONObject testReport = this.api.testReport(session);


        if (testReport == null || testReport.equals("null")) {
            bzmBuildLog.warn("Requesting aggregate is not available. " +
                    "Build won't be validated against local tresholds");
            return result;
        }

        TestResultFactory testResultFactory = TestResultFactory.getTestResultFactory();
        testResultFactory.setVersion(APIFactory.ApiVersion.valueOf(apiVersion));
        TestResult testResult = null;
        try {
            testResult = testResultFactory.getTestResult(testReport);
            bzmBuildLog.info(testResult.toString());
            bzmBuildLog.info("Validating local tresholds...\n");
            result=Utils.validateLocalTresholds(testResult,this,bzmBuildLog);

        } catch (IOException ioe) {
            bzmBuildLog.info("ERROR: Failed to generate TestResult: " + ioe);
        } catch (JSONException je) {
            bzmBuildLog.info("ERROR: Failed to generate TestResult: " + je);
        }finally{
            return result;
        }
    }


    public int getResponseTimeFailedThreshold() {
        return responseTimeFailedThreshold;
    }

    public void setResponseTimeFailedThreshold(int responseTimeFailedThreshold) {
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
    }

    public int getResponseTimeUnstableThreshold() {
        return responseTimeUnstableThreshold;
    }

    public void setResponseTimeUnstableThreshold(int responseTimeUnstableThreshold) {
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

    public int getErrorFailedThreshold() {
        return errorFailedThreshold;
    }

    public void setErrorFailedThreshold(int errorFailedThreshold) {
        this.errorFailedThreshold = Math.max(0, Math.min(errorFailedThreshold, 100));
    }

    public int getErrorUnstableThreshold() {
        return errorUnstableThreshold;
    }

    public void setErrorUnstableThreshold(int errorUnstableThreshold) {
        this.errorUnstableThreshold = Math.max(0, Math.min(errorUnstableThreshold,
                100));
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

    public static StdErrLog getJenBuildLog() {
        return jenBuildLog;
    }

    public BlazemeterApi getApi() {
        return api;
    }

    public String getTestName() {
        return testName;
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
    }
}
