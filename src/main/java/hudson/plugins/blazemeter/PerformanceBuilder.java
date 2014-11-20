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
import hudson.plugins.blazemeter.utils.Utils;
import hudson.security.ACL;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerformanceBuilder extends Builder {
    DateFormat df = new SimpleDateFormat("dd/MM/yy");


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

    private BlazemeterApi api = null;
    /**
     * @deprecated as of 1.3. for compatibility
     */
    private transient String filename;

    /**
     * Configured report parsers.
     */
    private List<PerformanceReportParser> parsers = null;

    @DataBoundConstructor
    public PerformanceBuilder(String apiKey,
                              String testDuration,
                              String mainJMX,
                              String dataFolder,
                              String testId,
                              String apiVersion,
                              String jsonConfig,
                              int errorFailedThreshold,
                              int errorUnstableThreshold,
                              int responseTimeFailedThreshold,
                              int responseTimeUnstableThreshold) {
        this.errorFailedThreshold = errorFailedThreshold;
        this.errorUnstableThreshold = errorUnstableThreshold;
        this.testId = testId;
        this.apiVersion = apiVersion.equals("autoDetect")?
                Utils.autoDetectApiVersion(apiVersion, apiKey):apiVersion;
        this.mainJMX = mainJMX;
        this.dataFolder = dataFolder;
        this.jsonConfig = jsonConfig;
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
        this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
        APIFactory apiFactory = APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.valueOf(this.apiVersion));
        this.api = apiFactory.getAPI(apiKey);
        this.testDuration = (testDuration != null && !testDuration.isEmpty()) ?
                testDuration : Utils.requestTestDuration(this.api, this.testId);
}





    private String blazeMeterURL;


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
        PrintStream logger = listener.getLogger();
        if (validateThresholds(logger) != Result.SUCCESS) {
            // input parameters error.
            build.setResult(Result.FAILURE);
            logger.println("Build was failed due to incorrect values of parameters");
            return true;
        }


        BlazeMeterPerformanceBuilderDescriptor descriptor=getDescriptor();
        //update testDuration on server
        Utils.updateBZMUrl(descriptor,this.api,logger);
        this.api = getAPIClient(build);
        if(!this.jsonConfig.isEmpty()){
            FilePath workspace=build.getWorkspace();
            this.testId=Utils.createTestFromJSON(this.api,this.jsonConfig,workspace,logger);

        }else{
            Utils.saveTestDuration(this.api, this.testId, testDuration);
        }
        logger.println("Expected test duration=" + testDuration);
        int runDurationSeconds = Integer.parseInt(testDuration) * 60;


        Utils.uploadDataFolderFiles(this.dataFolder,this.mainJMX,testId, this.api, logger);

        org.json.JSONObject json;
        int countStartRequests = 0;
        do {
            logger.print("### About to start Blazemeter Test.....  ");
            json = this.api.startTest(testId);
            countStartRequests++;
            if (json == null && countStartRequests > 5) {
                logger.println("Could not start BlazeMeter Test");
                build.setResult(Result.FAILURE);
                return false;
            }
        } while (json == null);

        String session;
        try {
             session=this.getTestSession(json, logger, build);
            if(session.isEmpty()){
                build.setResult(Result.FAILURE);
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            logger.println("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
            return false;
        }

        // add the report to the build object.
        PerformanceBuildAction a = new PerformanceBuildAction(build, logger, parsers);
        a.setSession(session);
        a.setBlazeMeterURL(DESCRIPTOR.getBlazeMeterURL());
        build.addAction(a);

        try {
            Utils.wait_for_finish(this.api, this.apiVersion, this.testId,
                    logger, session, runDurationSeconds);

            logger.println("BlazeMeter test running terminated at " + Calendar.getInstance().getTime());

            Result result = this.postProcess(session, logger);

            build.setResult(result);

            return true;
        } finally {
            TestInfo info = this.api.getTestRunStatus(apiVersion.equals("v2") ? testId : session);

            String status = info.getStatus();
            if (status.equals(TestStatus.Running)) {
                logger.println("Shutting down test");
                this.api.stopTest(testId);
            } else if (status.equals(TestStatus.Error)) {
                build.setResult(Result.FAILURE);
                logger.println("Error while running a test - please try to run the same test on BlazeMeter");
            } else if (status.equals(TestStatus.NotFound)) {
                build.setResult(Result.FAILURE);
                logger.println("Test not found error");
            }
        }
    }
    private BlazemeterApi getAPIClient(AbstractBuild<?, ?> build) {
        String apiKeyId = getDescriptor().getApiKey();
        String apiKey = null;
        for (BlazemeterCredential c : CredentialsProvider
                .lookupCredentials(BlazemeterCredential.class, build.getProject(), ACL.SYSTEM)) {
            if (StringUtils.equals(apiKeyId, c.getId())) {
                apiKey = c.getApiKey().getPlainText();
                break;
            }
        }

        // ideally, at this point we'd look up the credential based on the API key to find the secret
        // but there are no secrets, so no need to!
        return APIFactory.getApiFactory().getAPI(apiKey);
    }


   private String getTestSession(JSONObject json, PrintStream logger, AbstractBuild<?, ?> build) throws JSONException{
       String session="";
       if (apiVersion.equals(APIFactory.ApiVersion.v2.name()) && !json.get("response_code").equals(200)) {
           if (json.get("response_code").equals(500) && json.get("error").toString()
                   .startsWith("Test already running")) {
               logger.println("Test already running, please stop it first");
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
           logger.println("Blazemeter test report will be available at " +  this.api.getBlazeMeterURL()+"/app/#report/"+session+"/loadreport");
       }
   return session;
   }

    private Result postProcess(String session, PrintStream logger) throws InterruptedException {
        //TODO: loop probe with special response code. or loop for certain time on 404 error code.
        Thread.sleep(10 * 1000); // Wait for the report to generate.

        //get testGetArchive information
        JSONObject testReport = this.api.testReport(session);


        if (testReport == null || testReport.equals("null")) {
            logger.println("Error: Requesting aggregate is not available");
            return Result.FAILURE;
        }

        TestResultFactory testResultFactory = TestResultFactory.getAggregateTestResultFactory();
        testResultFactory.setVersion(APIFactory.ApiVersion.valueOf(apiVersion));
        TestResult testResult = null;
        try {
            testResult = testResultFactory.getAggregateTestResult(testReport);

        } catch (IOException ioe) {
            logger.println("Error: Failed to generate AggregateTestResult: " + ioe);
        } catch (JSONException je) {
            logger.println("Error: Failed to generate AggregateTestResult: " + je);
        }

        if (testResult == null) {
            logger.println("Error: Requesting aggregate Test Result is not available");
            return Result.FAILURE;
        }

        double thresholdTolerance = 0.00005; //null hypothesis
        double errorPercent = testResult.getErrorPercentage();
        double AverageResponseTime = testResult.getAverage();


        JSONObject jo = this.api.getTresholds(session);
        boolean success=false;
        try {
            success=jo.getJSONObject("result").getJSONObject("data").getBoolean("success");
        } catch (JSONException je) {
            logger.println("Error: Failed to get tresholds: " + je);
        }
        logger.println("Validating tresholds from server...");

        Result result = success?Result.SUCCESS:Result.FAILURE;

        /*if (errorFailedThreshold > 0 && errorPercent - errorFailedThreshold > thresholdTolerance) {
            result = Result.FAILURE;
            logger.println("Test ended with " + Result.FAILURE + " on error percentage threshold");
        } else if (errorUnstableThreshold > 0
                && errorPercent - errorUnstableThreshold > thresholdTolerance) {
            logger.println("Test ended with " + Result.UNSTABLE + " on error percentage threshold");
            result = Result.UNSTABLE;
        }

        if (responseTimeFailedThreshold > 0 && AverageResponseTime - responseTimeFailedThreshold > thresholdTolerance) {
            result = Result.FAILURE;
            logger.println("Test ended with " + Result.FAILURE + " on response time threshold");

        } else if (responseTimeUnstableThreshold > 0
                && AverageResponseTime - responseTimeUnstableThreshold > thresholdTolerance) {
            result = Result.UNSTABLE;
            logger.println("Test ended with " + Result.UNSTABLE + " on response time threshold");
        }*/
        return result;
    }


    private Result validateThresholds(PrintStream logger) {

        Result result = Result.SUCCESS;
        if (testDuration == null || testDuration.isEmpty() || testDuration.equals("0")) {
            logger.println("BlazeMeter: Test duration should be more than ZERO, build is considered as "
                    + Result.NOT_BUILT.toString().toLowerCase());
            return Result.ABORTED;
        }
        if (errorUnstableThreshold >= 0 && errorUnstableThreshold <= 100) {
            logger.println("BlazeMeter: Errors percentage greater or equal than "
                    + errorUnstableThreshold + " % will be considered as "
                    + Result.UNSTABLE.toString().toLowerCase());
        } else {
            logger.println("BlazeMeter: ErrorUnstableThreshold percentage should be between 0 to 100");
            return Result.ABORTED;
        }

        if (errorFailedThreshold >= 0 && errorFailedThreshold <= 100) {
            logger.println("BlazeMeter: ErrorFailedThreshold percentage greater or equal than "
                    + errorFailedThreshold + " % will be considered as "
                    + Result.FAILURE.toString().toLowerCase());
        } else {
            logger.println("BlazeMeter: ErrorFailedThreshold percentage should be between 0 to 100");
            return Result.ABORTED;
        }

        if (responseTimeUnstableThreshold > 0) {
            logger.println("BlazeMeter: ResponseTimeUnstable greater or equal than "
                    + responseTimeUnstableThreshold + " millis will be considered as "
                    + Result.UNSTABLE.toString().toLowerCase());
        }
        if (responseTimeFailedThreshold > 0) {
            logger.println("BlazeMeter: ResponseTimeFailed greater than "
                    + responseTimeFailedThreshold + " millis will be considered as "
                    + Result.FAILURE.toString().toLowerCase());
        }
        return result;
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
