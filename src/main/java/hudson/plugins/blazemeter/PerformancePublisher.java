package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Result;
import hudson.plugins.blazemeter.api.AggregateTestResult;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.api.TestInfo;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerformancePublisher extends Notifier {
    DateFormat df = new SimpleDateFormat("dd/MM/yy");

/*
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public String getDisplayName() {
            return Messages.Publisher_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/BlazeMeterJenkinsPlugin/help.html";
        }

        public List<PerformanceReportParserDescriptor> getParserDescriptors() {
            return PerformanceReportParserDescriptor.all();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
*/

    private String apiKey;

    private String testId = "";

    private String testDuration = "180";

    private String mainJMX = "";

    private String dataFolder = "";

    private int errorFailedThreshold = 0;

    private int errorUnstableThreshold = 0;

    private int responseTimeFailedThreshold = 0;

    private int responseTimeUnstableThreshold = 0;

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
                                int errorFailedThreshold,
                                int errorUnstableThreshold,
                                int responseTimeFailedThreshold,
                                int responseTimeUnstableThreshold) {
        this.apiKey = apiKey;
        this.errorFailedThreshold = errorFailedThreshold;
        this.errorUnstableThreshold = errorUnstableThreshold;
        this.testId = testId;
        this.testDuration = testDuration;
        this.mainJMX = mainJMX;
        this.dataFolder = dataFolder;
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
        this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
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

//    @Override
//    public Action getProjectAction(AbstractProject<?, ?> project) {
//        PerformanceProjectAction ret = new PerformanceProjectAction(project);
//        performanceProjectActions.add(ret);
//        return ret;
//    }

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

        int runDurationSeconds = Integer.parseInt(testDuration) * 60;

        if ((result = validateThresholds(logger)) != Result.SUCCESS) // input parameters error.
            return true;

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
        BlazemeterApi bmAPI = new BlazemeterApi();

        uploadDataFolderFiles(apiKey, testId, bmAPI, logger);

        org.json.JSONObject json;
        int countStartRequests = 0;
        do {
            logger.print(".");
            json = bmAPI.startTest(apiKey, testId);
            countStartRequests++;
            if (countStartRequests > 5) {
                logger.println("Could not start BlazeMeter Test");
                result = Result.NOT_BUILT;
                return false;
            }
        } while (json == null);

        try {
            if (!json.get("response_code").equals(200)) {
                if (json.get("response_code").equals(500) && json.get("error").toString().startsWith("Test already running")) {
//                    bmAPI.stopTest(apiKey, testId);
                    logger.println("Test already running, please stop it first");
                    result = Result.NOT_BUILT;
                    return false;
                }
                //Try again.
                logger.print(".");
                json = bmAPI.startTest(apiKey, testId);
                if (json == null) {
                    logger.println("Could not start BlazeMeter Test");
                    result = Result.NOT_BUILT;
                    return false;
                }
                if (!json.get("response_code").equals(200)) {
                    logger.println("Could not start BlazeMeter Test -" + json.get("error").toString());
                    result = Result.NOT_BUILT;
                    return false;
                }
            }
            session = json.get("session_id").toString();

        } catch (JSONException e) {
            e.printStackTrace();
            logger.println("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
            return false;
        }
        // add the report to the build object.
        PerformanceBuildAction a = new PerformanceBuildAction(build, logger, parsers);
        a.setSession(session);
        a.setBlazeMeterURL(DESCRIPTOR.blazeMeterURL);
        build.addAction(a);

        Date start = null;

        long lastPrint = 0;
        while (true) {
            TestInfo info = bmAPI.getTestRunStatus(apiKey, testId);

            //if drupal works hard
            //Thread.sleep(1000);

            if (info.getStatus().equals(BlazemeterApi.TestStatus.Error)) {
                build.setResult(Result.NOT_BUILT);
                logger.println("Error while running a test - please try to run the same test on BlazeMeter");
                return true;
            }

            if (info.getStatus().equals(BlazemeterApi.TestStatus.NotFound)) {
                build.setResult(Result.NOT_BUILT);
                logger.println("Test not found error");
                return true;
            }

            if (info.getStatus().equals(BlazemeterApi.TestStatus.Running)) {
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
                    bmAPI.stopTest(apiKey, testId);
                    logger.println("BlazeMeter test stopped due to user test duration setup reached");
                    break;
                }
                continue;
            }

            if (info.getStatus().equals(BlazemeterApi.TestStatus.NotRunning))
                break;
        }

        logger.println("BlazeMeter test running terminated at " + Calendar.getInstance().getTime());

        //TODO: loop probe with special response code. or loop for certain time on 404 error code.
        Thread.sleep(10 * 1000); // Wait for the report to generate.

        //get testGetArchive information
        json = bmAPI.aggregateReport(apiKey, session);
        for (int i = 0; i < 200; i++) {
            try {
                if (json.get("response_code").equals(404))
                    json = bmAPI.aggregateReport(apiKey, session);
                else
                    break;
            } catch (JSONException e) {
            } finally {
                Thread.sleep(5 * 1000);
            }
        }
        String aggregate = "null";

        for (int i = 0; i < 30; i++) {
            try {
                if (!json.get("response_code").equals(200))
                    logger.println("Error: Requesting aggregate report response code:" + json.get("response_code"));

                aggregate = json.getJSONObject("report").get("aggregate").toString();
            } catch (JSONException e) {
                logger.println("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
                e.printStackTrace();
            }

            if (!aggregate.equals("null"))
                break;

            Thread.sleep(2 * 1000);
            json = bmAPI.aggregateReport(apiKey, session);
        }

        if (aggregate == null) {
            logger.println("Error: Requesting aggregate is not available");
            build.setResult(Result.NOT_BUILT);
            return false;
        }

        AggregateTestResult aggregateTestResult = AggregateTestResult.generate(aggregate);

        if (aggregateTestResult == null) {
            logger.println("Error: Requesting aggregate Test Result is not available");
            build.setResult(Result.NOT_BUILT);
            return false;
        }

        if (performanceProjectActions.size() > 0) {
            performanceProjectActions.get(performanceProjectActions.size() - 1).lastReportSession = session;
            performanceProjectActions.get(performanceProjectActions.size() - 1).lastBlazeMeterURL = DESCRIPTOR.blazeMeterURL;
        }

        double thresholdTolerance = 0.00005; //null hypothesis
        double errorPercent = aggregateTestResult.getErrorPercentage();
        double AverageResponseTime = aggregateTestResult.getAverage();

        if (errorFailedThreshold >= 0 && errorPercent - errorFailedThreshold > thresholdTolerance) {
            result = Result.FAILURE;
            logger.println("Test ended with " + Result.FAILURE + " on error percentage threshold");
        } else if (errorUnstableThreshold >= 0
                && errorPercent - errorUnstableThreshold > thresholdTolerance) {
            logger.println("Test ended with " + Result.UNSTABLE + " on error percentage threshold");
            result = Result.UNSTABLE;
        }

        if (responseTimeFailedThreshold >= 0 && AverageResponseTime - responseTimeFailedThreshold > thresholdTolerance) {
            result = Result.FAILURE;
            build.setResult(Result.FAILURE);
            logger.println("Test ended with " + Result.FAILURE + " on response time threshold");

        } else if (responseTimeUnstableThreshold >= 0
                && AverageResponseTime - responseTimeUnstableThreshold > thresholdTolerance) {
            result = Result.UNSTABLE;
            logger.println("Test ended with " + Result.UNSTABLE + " on response time threshold");
        }

        build.setResult(result);

/*
        for (PerformanceReportParser parser : parsers) {
            String glob = parser.glob;
            logger.println("Performance: Recording " + parser.getReportName()
                    + " reports '" + glob + "'");

            List<FilePath> files = locatePerformanceReports(build.getWorkspace(),
                    glob);

            if (files.isEmpty()) {
                if (build.getResult().isWorseThan(Result.UNSTABLE)) {
                    return true;
                }
                build.setResult(Result.FAILURE);
                logger.println("Performance: no " + parser.getReportName()
                        + " files matching '" + glob
                        + "' have been found. Has the report generated?. Setting Build to "
                        + build.getResult());
                return true;
            }

            List<File> localReports = copyReportsToMaster(build, logger, files,
                    parser.getDescriptor().getDisplayName());
            Collection<PerformanceReport> parsedReports = parser.parse(build,
                    localReports, listener);

            // mark the build as unstable or failure depending on the outcome.
            for (PerformanceReport r : parsedReports) {
                r.setBuildAction(a);
                double errorPercent = r.errorPercent();
                Result result = Result.SUCCESS;
                if (errorFailedThreshold >= 0 && errorPercent - errorFailedThreshold > thresholdTolerance) {
                    result = Result.FAILURE;
                    build.setResult(Result.FAILURE);
                } else if (errorUnstableThreshold >= 0
                        && errorPercent - errorUnstableThreshold > thresholdTolerance) {
                    result = Result.UNSTABLE;
                }
                if (result.isWorseThan(build.getResult())) {
                    build.setResult(result);
                }
                logger.println("Performance: File " + r.getReportFileName()
                        + " reported " + errorPercent
                        + "% of errors [" + result + "]. Build status is: "
                        + build.getResult());
            }
        }
*/

        return true;
    }

//    public void getAllTestsForUser(String   userKey)  {
//        try {
//            BlazemeterApi   bzm  = new  BlazemeterApi();
//            bzm.getTests(userKey);
//        } catch (Exception e) {
//          // Do nothing!
//        }
//    }


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
        if (errorUnstableThreshold >= 0 && errorUnstableThreshold <= 100) {
            logger.println("BlazeMeter: Errors percentage greater or equal than "
                    + errorUnstableThreshold + "% will be considered as "
                    + Result.UNSTABLE.toString().toLowerCase());
        } else {
            logger.println("BlazeMeter: percentage should be between 0 to 100");
            result = Result.NOT_BUILT;
        }

        if (errorFailedThreshold >= 0 && errorFailedThreshold <= 100) {
            logger.println("BlazeMeter: Errors percentage greater or equal than "
                    + errorFailedThreshold + "% will be considered as "
                    + Result.FAILURE.toString().toLowerCase());
        } else {
            logger.println("BlazeMeter: percentage should be between 0 to 100");
            result = Result.NOT_BUILT;
        }

        if (responseTimeUnstableThreshold >= 0) {
            logger.println("BlazeMeter: Response time greater or equal than "
                    + responseTimeUnstableThreshold + "millis will be considered as "
                    + Result.UNSTABLE.toString().toLowerCase());
        } else {
            logger.println("BlazeMeter: percentage should be greater or equal than 0");
            result = Result.NOT_BUILT;
        }

        if (responseTimeFailedThreshold >= 0) {
            logger.println("BlazeMeter: Response time greater or equal than "
                    + responseTimeFailedThreshold + "millis will be considered as "
                    + Result.FAILURE.toString().toLowerCase());
        } else {
            logger.println("BlazeMeter: percentage should be greater or equal than 0");
            result = Result.NOT_BUILT;
        }
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
        // data format migration
/*
        if (parsers == null)
            parsers = new ArrayList<PerformanceReportParser>();
        if (filename != null) {
            parsers.add(new LoadReportParser(filename));
            filename = null;
        }
*/
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


    public static class BlazeMeterPerformancePublisherDescriptor extends BuildStepDescriptor<Publisher> {

        private String blazeMeterURL = "https://a.blazemeter.com";
        private String name = "My BlazeMeter Account";
        private String apiKey;

        public BlazeMeterPerformancePublisherDescriptor() {
            super(PerformancePublisher.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "BlazeMeter";
        }

        // Used by config.jelly to display the test list.
        public ListBoxModel doFillTestIdItems(@QueryParameter String apiKey) throws FormValidation {
            if (StringUtils.isBlank(apiKey)) {
                apiKey = getApiKey();
            }

            Secret apiSecret = null;
            Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
            for (BlazemeterCredential c : CredentialsProvider
                    .lookupCredentials(BlazemeterCredential.class, item, ACL.SYSTEM)) {
                if (StringUtils.equals(apiKey, c.getId())) {
                    apiSecret = c.getApiKey();
                    break;
                }
            }
            ListBoxModel items = new ListBoxModel();
            if (apiSecret == null) {
                items.add("No API Key", "-1");
            } else {
            BlazemeterApi bzm = new BlazemeterApi();

            try {
                HashMap<String, String> testList = bzm.getTestList(apiSecret.getPlainText());
                if (testList == null){
                    items.add("Invalid API key ", "-1");
                } else if (testList.isEmpty()){
                    items.add("No tests", "-1");
                } else {
                    Set set = testList.entrySet();
                    for (Object test : set) {
                        Map.Entry me = (Map.Entry) test;
                        items.add((String) me.getKey(), String.valueOf(me.getValue()));
                    }
                }
            } catch (Exception e) {
                throw FormValidation.error(e.getMessage(), e);
            }
            }
            return items;
        }

        public ListBoxModel doFillApiKeyItems() {
            ListBoxModel items = new ListBoxModel();
            Set<String> apiKeys = new HashSet<String>();

            Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
            if (item instanceof Job) {
                List<BlazemeterCredential> global = CredentialsProvider
                        .lookupCredentials(BlazemeterCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
                if (!global.isEmpty() && !StringUtils.isEmpty(getApiKey())) {
                    items.add("Default API Key", "");
                }
            }
            for (BlazemeterCredential c : CredentialsProvider
                    .lookupCredentials(BlazemeterCredential.class, item, ACL.SYSTEM)) {
                String id = c.getId();
                if (!apiKeys.contains(id)) {
                    items.add(StringUtils.defaultIfEmpty(c.getDescription(), id), id);
                    apiKeys.add(id);
                }
            }
            return items;
        }

        public List<BlazemeterCredential> getCredentials(Object scope) {
            List<BlazemeterCredential> result = new ArrayList<BlazemeterCredential>();
            Set<String> apiKeys = new HashSet<String>();

            Item item = scope instanceof Item ? (Item) scope : null;
            for (BlazemeterCredential c : CredentialsProvider
                    .lookupCredentials(BlazemeterCredential.class, item, ACL.SYSTEM)) {
                String id = c.getId();
                if (!apiKeys.contains(id)) {
                    result.add(c);
                    apiKeys.add(id);
                }
            }
            return result;
        }

        // Used by global.jelly to authenticate User key
        public FormValidation doTestConnection(@QueryParameter("apiKey") final String userKey)
                throws MessagingException, IOException, JSONException, ServletException {
            BlazemeterApi bzm = new BlazemeterApi();
            int testCount = bzm.getTestCount(userKey);
            if (testCount <= 0) {
                return FormValidation.errorWithMarkup("User Key Invalid Or No Available Tests");
            } else {
                return FormValidation.ok("User Key Valid. " + testCount + " Available Tests");
            }
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            apiKey = formData.optString("apiKey");
            save();
            return true;
        }

        public String getBlazeMeterURL() {
            return blazeMeterURL;
        }

        public void setBlazeMeterURL(String blazeMeterURL) {
            this.blazeMeterURL = blazeMeterURL;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getApiKey() {
            List<BlazemeterCredential> credentials = CredentialsProvider
                    .lookupCredentials(BlazemeterCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
            if (StringUtils.isBlank(apiKey) && !credentials.isEmpty()) {
                return credentials.get(0).getId();
            }
            if (credentials.size() == 1) {
                return credentials.get(0).getId();
            }
            for (BlazemeterCredential c: credentials) {
                if (StringUtils.equals(c.getId(), apiKey)) {
                    return apiKey;
                }
            }
            // API key is not valid any more
            return "";
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

    }
}
