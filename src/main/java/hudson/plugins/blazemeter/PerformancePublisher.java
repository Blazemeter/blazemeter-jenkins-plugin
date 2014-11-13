package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.blazemeter.aggregatetestresult.AggregateTestResultFactory;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.aggregatetestresult.AggregateTestResult;
import hudson.plugins.blazemeter.entities.TestInfo;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.security.ACL;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import org.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerformancePublisher extends Notifier {
    DateFormat df = new SimpleDateFormat("dd/MM/yy");


    private String apiKey;

    private String testId = "";

    private String apiVersion = "";

    private String testDuration = "180";

    private String mainJMX = "";

    private String dataFolder = "";

    private int errorFailedThreshold = 0;

    private int errorUnstableThreshold = 0;

    private int responseTimeFailedThreshold = 0;

    private int responseTimeUnstableThreshold = 0;

    private BlazemeterApi api=null;
    /**
     * @deprecated as of 1.3. for compatibility
     */
    private transient String filename;

    /**
     * Configured report parsers.
     */
    private List<PerformanceReportParser> parsers = null;

    @DataBoundConstructor
    public PerformancePublisher(String apiKey,
                                String testDuration,
                                String mainJMX,
                                String dataFolder,
                                String testId,
                                String apiVersion,
                                int errorFailedThreshold,
                                int errorUnstableThreshold,
                                int responseTimeFailedThreshold,
                                int responseTimeUnstableThreshold) {
        this.apiKey = apiKey;
        this.errorFailedThreshold = errorFailedThreshold;
        this.errorUnstableThreshold = errorUnstableThreshold;
        this.testId = testId;
        this.apiVersion = apiVersion;
        this.testDuration = testDuration;
        this.mainJMX = mainJMX;
        this.dataFolder = dataFolder;
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
        this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
        APIFactory apiFactory = APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.valueOf(apiVersion));
        this.api=apiFactory.getAPI();
    }


    public static File getPerformanceReport(AbstractBuild<?, ?> build,
                                            String parserDisplayName, String performanceReportName) {
        return new File(build.getRootDir(),
                PerformanceReportMap.getPerformanceReportFileRelativePath(
                        parserDisplayName,
                        getPerformanceReportBuildFileName(performanceReportName)));
    }

    List<PerformanceProjectAction> performanceProjectActions = new ArrayList<PerformanceProjectAction>();

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
        Result result; // Result.SUCCESS;
        String session;

        if(testDuration==null||testDuration.isEmpty()){
            build.setResult(Result.ABORTED);
            logger.println("Build was aborted due to incorrect values of test duration: null or empty");
            return true;
        }

        if ((result = validateThresholds(logger)) != Result.SUCCESS){
            // input parameters error.
            build.setResult(Result.ABORTED);
            logger.println("Build was aborted due to incorrect values of tresholds");
            return true;
        }
        logger.println("Expected test duration="+testDuration);

        int runDurationSeconds = Integer.parseInt(testDuration) * 60;

        String apiKeyId = StringUtils.defaultIfEmpty(getApiKey(), getDescriptor().getApiKey());
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
        this.api = APIFactory.getApiFactory().getAPI();

        uploadDataFolderFiles(apiKey, testId, this.api, logger);

        org.json.JSONObject json;
        int countStartRequests = 0;
        do {
            logger.print(".");
            json = this.api.startTest(apiKey, testId);
            countStartRequests++;
            if (json == null && countStartRequests > 5) {
                logger.println("Could not start BlazeMeter Test");
                build.setResult(Result.NOT_BUILT);
                return false;
            }
        } while (json == null);

        try {
            if (apiVersion.equals(APIFactory.ApiVersion.v2.name())&&!json.get("response_code").equals(200)) {
                if (json.get("response_code").equals(500) && json.get("error").toString()
                        .startsWith("Test already running")) {
                    logger.println("Test already running, please stop it first");
                    build.setResult(Result.NOT_BUILT);
                    return false;
                }

            }
            if(apiVersion.equals(APIFactory.ApiVersion.v2.name())){
                session = json.get("session_id").toString();

            }else{
                JSONObject startJO = (JSONObject)json.get("result");
                session = ((JSONArray)startJO.get("sessionsId")).get(0).toString();
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

        Date start = null;

        long lastPrint = 0;
        while (true) {
            TestInfo info = this.api.getTestRunStatus(apiKey,
                    apiVersion.equals("v2")?testId:session);

            //if drupal works hard
            //Thread.sleep(1000);

            if (info.getStatus().equals(TestStatus.Error)) {
                build.setResult(Result.NOT_BUILT);
                logger.println("Error while running a test - please try to run the same test on BlazeMeter");
                return true;
            }

            if (info.getStatus().equals(TestStatus.NotFound)) {
                build.setResult(Result.NOT_BUILT);
                logger.println("Test not found error");
                return true;
            }

            if (info.getStatus().equals(TestStatus.Running)) {
                if (start == null)
                    start = Calendar.getInstance().getTime();
                build.setResult(Result.SUCCESS);
                long now = Calendar.getInstance().getTime().getTime();
                long diffInSec = (now - start.getTime()) / 1000;
                if (now - lastPrint > 10000) { //print every 10 sec.
                    logger.println("BlazeMeter test running from " + start + " - for " + diffInSec + " seconds");
                    lastPrint = now;
                }

                if (diffInSec >= runDurationSeconds) {
                    this.api.stopTest(apiKey, testId);
                    logger.println("BlazeMeter test stopped due to user test duration setup reached");
                    break;
                }
                continue;
            }

            if (info.getStatus().equals(TestStatus.NotRunning))
                break;
        }

        logger.println("BlazeMeter test running terminated at " + Calendar.getInstance().getTime());

        //TODO: loop probe with special response code. or loop for certain time on 404 error code.
        Thread.sleep(10 * 1000); // Wait for the report to generate.

        //get testGetArchive information
        JSONObject aggregate = this.api.aggregateReport(apiKey, session);


        if (aggregate == null || aggregate.equals("null")) {
            logger.println("Error: Requesting aggregate is not available");
            build.setResult(Result.NOT_BUILT);
            return false;
        }

        AggregateTestResultFactory testResultFactory = AggregateTestResultFactory.getAggregateTestResultFactory();
        testResultFactory.setVersion(APIFactory.ApiVersion.valueOf(apiVersion));
        AggregateTestResult aggregateTestResult=null;
        try{
            aggregateTestResult = testResultFactory.getAggregateTestResult(aggregate);

        }catch (IOException ioe){
            logger.println("Error: Failed to generate AggregateTestResult: "+ioe);
        }catch (JSONException je){
            logger.println("Error: Failed to generate AggregateTestResult: "+je);
        }

        if (aggregateTestResult == null) {
            logger.println("Error: Requesting aggregate Test Result is not available");
            build.setResult(Result.NOT_BUILT);
            return false;
        }

        if (performanceProjectActions.size() > 0) {
            performanceProjectActions.get(performanceProjectActions.size() - 1).lastReportSession = session;
            performanceProjectActions.get(performanceProjectActions.size() - 1).lastBlazeMeterURL = DESCRIPTOR.getBlazeMeterURL();
        }

        double thresholdTolerance = 0.00005; //null hypothesis
        double errorPercent = aggregateTestResult.getErrorPercentage();
        double AverageResponseTime = aggregateTestResult.getAverage();

        if (errorFailedThreshold > 0 && errorPercent - errorFailedThreshold > thresholdTolerance) {
            result = Result.FAILURE;
            logger.println("Test ended with " + Result.FAILURE + " on error percentage threshold");
        } else if (errorUnstableThreshold > 0
                && errorPercent - errorUnstableThreshold > thresholdTolerance) {
            logger.println("Test ended with " + Result.UNSTABLE + " on error percentage threshold");
            result = Result.UNSTABLE;
        }

        if (responseTimeFailedThreshold > 0 && AverageResponseTime - responseTimeFailedThreshold > thresholdTolerance) {
            result = Result.FAILURE;
            build.setResult(Result.FAILURE);
            logger.println("Test ended with " + Result.FAILURE + " on response time threshold");

        } else if (responseTimeUnstableThreshold > 0
                && AverageResponseTime - responseTimeUnstableThreshold > thresholdTolerance) {
            result = Result.UNSTABLE;
            logger.println("Test ended with " + Result.UNSTABLE + " on response time threshold");
        }

        build.setResult(result);


        return true;
    }

    private void uploadDataFolderFiles(String apiKey, String testId, BlazemeterApi bmAPI, PrintStream logger) {

        if (dataFolder == null || dataFolder.isEmpty())
            return;

        File folder = new File(dataFolder);
        if (!folder.exists() || !folder.isDirectory()){
            logger.println("dataFolder " + dataFolder + " could not be found on local file system, please check that the folder exists.");
            return;
        }

        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                String fileName;
                if (file.isFile()) {
                    fileName = file.getName();

                    if (fileName.endsWith(mainJMX))
                        bmAPI.uploadJmx(apiKey, testId, file);
                    else
                        uploadFile(apiKey, testId, bmAPI, file, logger);
                }
            }
        }
    }

    private void uploadFile(String apiKey, String testId, BlazemeterApi bmAPI, File file, PrintStream logger) {
        String fileName = file.getName();
        org.json.JSONObject json = bmAPI.uploadBinaryFile(apiKey, testId, file);
        try {
            if (!json.get("response_code").equals(200)) {
                logger.println("Could not upload file " + fileName + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            logger.println("Could not upload file " + fileName + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Result validateThresholds(PrintStream logger) {
        Result result = Result.SUCCESS;
        if(testDuration.equals("0")){
            logger.println("BlazeMeter: Test duration should be more than ZERO, build is considered as "
                    +Result.NOT_BUILT.toString().toLowerCase());
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
        } /*else {
            logger.println("BlazeMeter: ResponseTimeUnstable should be greater than 0");
            return Result.ABORTED;
        }*/

        if (responseTimeFailedThreshold > 0) {
            logger.println("BlazeMeter: ResponseTimeFailed greater than "
                    + responseTimeFailedThreshold + " millis will be considered as "
                    + Result.FAILURE.toString().toLowerCase());
        } /*else {
            logger.println("BlazeMeter: ResponseTimeFailed should be greater than 0");
            return Result.ABORTED;
        }*/
        return result;
    }

    private List<File> copyReportsToMaster(AbstractBuild<?, ?> build,
                                           PrintStream logger, List<FilePath> files, String parserDisplayName)
            throws IOException, InterruptedException {
        List<File> localReports = new ArrayList<File>();
        for (FilePath src : files) {
            final File localReport = getPerformanceReport(build, parserDisplayName,
                    src.getName());
            if (src.isDirectory()) {
                logger.println("Performance: File '" + src.getName()
                        + "' is a directory, not a Performance Report");
                continue;
            }
            src.copyTo(new FilePath(localReport));
            localReports.add(localReport);
        }
        return localReports;
    }

    public Object readResolve() {
        return this;
    }

    public String getApiKey() {
        return apiKey;
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
    public BlazeMeterPerformancePublisherDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final BlazeMeterPerformancePublisherDescriptor DESCRIPTOR = new BlazeMeterPerformancePublisherDescriptor();

    // The descriptor has been moved but we need to maintain the old descriptor for backwards compatibility reasons.
    @SuppressWarnings({"UnusedDeclaration"})
    public static final class DescriptorImpl
            extends BlazeMeterPerformancePublisherDescriptor {
    }
}
