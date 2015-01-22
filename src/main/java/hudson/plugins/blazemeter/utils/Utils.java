package hudson.plugins.blazemeter.utils;

import hudson.FilePath;
import hudson.model.Result;
import hudson.plugins.blazemeter.PerformanceBuilder;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.entities.TestInfo;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.testresult.TestResult;
import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Created by dzmitrykashlach on 18/11/14.
 */
public class Utils {

    private Utils() {
    }

    public static String autoDetectApiVersion(String apiKey, AbstractLogger logger) {
        BlazemeterApi api = null;
        APIFactory apiFactory = APIFactory.getApiFactory();
        String detectedApiVersion = null;
            api = apiFactory.getApiFactory().getAPI(apiKey,APIFactory.ApiVersion.v3);
            boolean isV3 = false;
            try {
                isV3 = api.getUser().getJSONObject("features").getBoolean("v3");
                if (isV3) {
                    detectedApiVersion="v3";
                } else {
                    detectedApiVersion="v2";
                }
            } catch (JSONException je) {
                logger.warn("Received JSONException while auto-detecting version: ", je);
            } catch (NullPointerException npe) {
                logger.warn("Received JSONException while auto-detecting version: ", npe);
                return "v3";
            }
        return detectedApiVersion;
    }


    public static JSONObject updateTest(BlazemeterApi api,
                                  String testId,
                                  String updDuration,
                                  JSONObject configNode,
                                  StdErrLog bzmBuildLog) {
        JSONObject updateResult=null;
        try {
            JSONObject result = null;
            if (configNode != null) {
                result=configNode;
            } else if (updDuration != null && !updDuration.isEmpty()) {
                JSONObject jo = api.getTestInfo(testId);
                result = jo.getJSONObject("result");
                JSONObject configuration = result.getJSONObject("configuration");
                JSONObject plugins = configuration.getJSONObject("plugins");
                String type = configuration.getString("type");
                JSONObject options = plugins.getJSONObject(type);
                JSONObject override = options.getJSONObject("override");
                override.put("duration", updDuration);
                override.put("threads", JSONObject.NULL);
                configuration.put("serversCount", JSONObject.NULL);
            }
            updateResult=api.updateTestInfo(testId, result);

        } catch (JSONException je) {
            bzmBuildLog.warn("Received JSONException while saving testDuration: ", je);
        } catch (Exception e) {
            bzmBuildLog.warn("Received JSONException while saving testDuration: ", e);
        }
        return updateResult;
    }

    public static String requestTestDuration(BlazemeterApi api, String testId, StdErrLog bzmBuildLog) {
        String duration = null;
        try {
            JSONObject jo = api.getTestInfo(testId);
            JSONObject result = jo.getJSONObject("result");
            JSONObject configuration = result.getJSONObject("configuration");
            JSONObject plugins = configuration.getJSONObject("plugins");
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject("override");
            duration = override.getString("duration");

        } catch (JSONException je) {
            bzmBuildLog.warn("Received JSONException while requesting testDuration: ", je);
        } catch (Exception e) {
            bzmBuildLog.warn("Received Exception while requesting testDuration: ", e);
        }
        return duration;
    }

    public static void uploadDataFolderFiles(String dataFolder, String mainJMX, String testId,
                                             BlazemeterApi bmAPI, StdErrLog bzmBuildLog) {

        if (dataFolder == null || dataFolder.isEmpty())
            return;

        File folder = new File(dataFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            bzmBuildLog.info("dataFolder " + dataFolder + " could not be found on local file system, please check that the folder exists.");
            return;
        }

        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                String fileName;
                if (file.isFile()) {
                    fileName = file.getName();

                    if (fileName.endsWith(mainJMX))
                        bmAPI.uploadJmx(testId, file);
                    else
                        uploadFile(testId, bmAPI, file, bzmBuildLog);
                }
            }
        }
    }

    public static void waitForFinish(BlazemeterApi api, String apiVersion, String testId, AbstractLogger bzmBuildLog,
                                     String session, int runDurationSeconds) throws InterruptedException {
        Date start = null;
        long lastPrint = 0;
        while (true) {
            Thread.sleep(15000);
            TestInfo info = api.getTestRunStatus(apiVersion.equals("v2") ? testId : session);

            if (!info.getStatus().equals(TestStatus.Running)) {
                bzmBuildLog.info("TestStatus for session " + (apiVersion.equals("v2") ? testId : session)
                        + info.getStatus());
                bzmBuildLog.info("BlazeMeter TestStatus for session" +
                        (apiVersion.equals("v2") ? testId : session)
                        + " is not 'Running': finishing build.... ");
                bzmBuildLog.info("Timestamp: " + Calendar.getInstance().getTime());
                break;
            }

            if (start == null)
                start = Calendar.getInstance().getTime();
            long now = Calendar.getInstance().getTime().getTime();
            long diffInSec = (now - start.getTime()) / 1000;
            if (now - lastPrint > 10000) { //print every 10 sec.
                bzmBuildLog.info("BlazeMeter test# " + testId + ", session # " + session + " running from " + start + " - for " + diffInSec + " seconds");
                lastPrint = now;
            }

            if (diffInSec >= runDurationSeconds) {
                bzmBuildLog.info("About to stop Blazemeter test...");
                bzmBuildLog.info("Timestamp: " + Calendar.getInstance().getTime());
                api.stopTest(testId);
                bzmBuildLog.info("BlazeMeter test stopped due to user test duration setup reached");
                bzmBuildLog.info("BlazeMeter test stopped due to user test duration setup reached");
                break;
            }
            if (Thread.interrupted()) {
                bzmBuildLog.info("Test was interrupted: throwing Interrupted Exception");
                throw new InterruptedException();
            }
        }
    }

    private static String createTest(BlazemeterApi api, JSONObject configNode,
                                     String testId,StdErrLog jenBuildLog) throws JSONException {
        if(testId.equals(Constants.CREATE_BZM_TEST_NOTE)){
            JSONObject jo = api.createTest(configNode);
            if(jo.has("error")&&!jo.getString("error").equals("null")){
                jenBuildLog.warn("Failed to create test: "+jo.getString("error"));
                testId="";
            }else{
                testId = jo.getJSONObject("result").getString("id");
            }
        }
        return testId;
    }

    public static String prepareTestRun(PerformanceBuilder builder) {
        BlazemeterApi api = builder.getApi();
        FilePath jsonConfigPath = new FilePath(builder.getBuild().getWorkspace(), builder.getJsonConfig());
        StdErrLog bzmBuildLog = PerformanceBuilder.getBzmBuildLog();
        StdErrLog jenBuildLog = PerformanceBuilder.getJenBuildLog();
        String testId = builder.getTestId();
        try {
            String jsonConfigStr = jsonConfigPath.readToString();
            JSONObject configNode = new JSONObject(jsonConfigStr);
            if (testId.contains("create")) {
                testId=createTest(api,configNode,testId,jenBuildLog);
                builder.setTestId(testId);
            }

            if(configNode!=null) {
                JSONObject updateResult=updateTest(api,testId,builder.getTestDuration(), configNode, bzmBuildLog);
                if(updateResult.has("error")&&!updateResult.get("error").equals(null)){
                    jenBuildLog.warn("Failed to update test with JSON configuration");
                    jenBuildLog.warn("Error:"+updateResult.getString("error"));
                    testId="";
                }else{
                    jenBuildLog.info("Test was updated on server");
                    JSONObject result = updateResult.getJSONObject("result");
                    jenBuildLog.info("Test id="+result.getString("id"));
                    jenBuildLog.info("Test name="+result.getString("name"));
                    testId=result.getString("id");
                }
            }

            String testDuration = (builder.getTestDuration() != null && !builder.getTestDuration().isEmpty()) ?
                    builder.getTestDuration() : requestTestDuration(api, builder.getTestId(), bzmBuildLog);
            builder.setTestDuration(testDuration);
        } catch (IOException e) {
            bzmBuildLog.info("Failed to read JSON configuration from file " + jsonConfigPath.getName() + ": " + e.getMessage());
            jenBuildLog.info("Failed to read JSON configuration from file " + jsonConfigPath.getName() + ": " + e.getMessage());
        } catch (JSONException je) {
            bzmBuildLog.info("Failed to read JSON configuration from file " + jsonConfigPath.getName() + ": " + je.getMessage());
            jenBuildLog.info("Failed to read JSON configuration from file " + jsonConfigPath.getName() + ": " + je.getMessage());
        } finally {
            uploadDataFolderFiles(builder.getDataFolder(),builder.getMainJMX(),testId, api,bzmBuildLog);
            return testId;
        }
    }

    public static void uploadFile(String testId, BlazemeterApi bmAPI, File file, StdErrLog bzmBuildLog) {
        String fileName = file.getName();
        org.json.JSONObject json = bmAPI.uploadBinaryFile(testId, file);
        try {
            if (!json.get("response_code").equals(200)) {
                bzmBuildLog.info("Could not upload file " + fileName + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            bzmBuildLog.info("Could not upload file " + fileName + " " + e.getMessage());
        }
    }

    public static void saveReport(String filename,
                                  String report,
                                  FilePath filePath, StdErrLog bzmBuildLog) {
        File reportFile = new File(filePath.getParent()
                + "/" + filePath.getName() + "/" + filename + ".xml");
        try {
            if (!reportFile.exists()) {
                reportFile.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(reportFile));
            out.write(report);
            out.close();

        } catch (FileNotFoundException fnfe) {
            bzmBuildLog.info("ERROR: Failed to save XML report to workspace " + fnfe.getMessage());
        } catch (IOException e) {
            bzmBuildLog.info("ERROR: Failed to save XML report to workspace " + e.getMessage());
        }
    }


    public static Result validateLocalTresholds(TestResult testResult,
                                                PerformanceBuilder builder,
                                                StdErrLog bzmBuildLog) {
        int responseTimeUnstable = Integer.valueOf(builder.getResponseTimeUnstableThreshold().isEmpty()
                ?"-1":builder.getResponseTimeUnstableThreshold());

        int responseTimeFailed = Integer.valueOf(builder.getResponseTimeFailedThreshold().isEmpty()
                ?"-1":builder.getResponseTimeFailedThreshold());
        int errorUnstable = Integer.valueOf(builder.getErrorUnstableThreshold().isEmpty()
                ?"-1":builder.getErrorUnstableThreshold());
        int errorFailed = Integer.valueOf(builder.getErrorFailedThreshold().isEmpty()
                ?"-1":builder.getErrorFailedThreshold());

        Result result = Result.SUCCESS;
        if (responseTimeUnstable >= 0 & testResult.getAverage() > responseTimeUnstable &
                testResult.getAverage() < responseTimeFailed) {
            bzmBuildLog.info("Validating reponseTimeUnstable...\n");
            bzmBuildLog.info("Average response time is higher than responseTimeUnstable treshold\n");
            bzmBuildLog.info("Marking build as unstable");
            result = Result.UNSTABLE;
        }

        if (errorUnstable >= 0 & testResult.getErrorPercentage() > errorUnstable &
                testResult.getAverage() < errorFailed) {
            bzmBuildLog.info("Validating errorPercentageUnstable...\n");
            bzmBuildLog.info("Error percentage is higher than errorPercentageUnstable treshold\n");
            bzmBuildLog.info("Marking build as unstable");
            result = Result.UNSTABLE;
        }


        if (responseTimeFailed >= 0 & testResult.getAverage() >= responseTimeFailed) {
            bzmBuildLog.info("Validating reponseTimeFailed...\n");
            bzmBuildLog.info("Average response time is higher than responseTimeFailure treshold\n");
            bzmBuildLog.info("Marking build as failed");
            result = Result.FAILURE;
        }

        if (errorFailed >= 0 & testResult.getAverage() >= errorFailed) {
            bzmBuildLog.info("Validating errorPercentageUnstable...\n");
            bzmBuildLog.info("Error percentage is higher than errorPercentageUnstable treshold\n");
            bzmBuildLog.info("Marking build as failed");
            result = Result.FAILURE;
        }

        if (errorUnstable < 0) {
            bzmBuildLog.info("ErrorUnstable percentage validation was skipped: value was not set in configuration");
        }

        if (errorFailed < 0) {
            bzmBuildLog.info("ErrorFailed percentage validation was skipped: value was not set in configuration");
        }

        if (responseTimeUnstable < 0) {
            bzmBuildLog.info("ResponseTimeUnstable validation was skipped: value was not set in configuration");
        }

        if (responseTimeFailed < 0) {
            bzmBuildLog.info("ResponseTimeFailed validation was skipped: value was not set in configuration");
        }
        return result;
    }

    public static String getVersion() {
        Properties props = new Properties();
        try {
            props.load(Utils.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty("version", "N/A");
        }
        return props.getProperty("version");
    }
}
