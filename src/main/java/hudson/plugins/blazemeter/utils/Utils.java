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
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by dzmitrykashlach on 18/11/14.
 */
public class Utils {

    private Utils(){}

    private static JavaUtilLog javaUtillogger=new JavaUtilLog(Constants.BZM_JEN);

    public static String autoDetectApiVersion(String apiVersion, String apiKey){
        BlazemeterApi api=null;
        APIFactory apiFactory = APIFactory.getApiFactory();
        String detectedApiVersion = !apiVersion.equals("autoDetect")?apiVersion:"";
        if(apiVersion.equals("autoDetect")){
            apiFactory.setVersion(APIFactory.ApiVersion.v3);
            api=apiFactory.getApiFactory().getAPI(apiKey);
            boolean isV3=false;
            try {
                isV3=api.getUser().getJSONObject("features").getBoolean("v3");
                if(isV3){

                    return "v3";
                }else{
                    return "v2";
                }
            } catch (JSONException je) {
                javaUtillogger.warn("Received JSONException while auto-detecting version: ", je);
            } catch (NullPointerException npe) {
                javaUtillogger.warn("Received JSONException while auto-detecting version: ", npe);
                return "v3";
            }
        }
        return detectedApiVersion;
    }


    public static void saveTestDuration(BlazemeterApi api, String testId, String updDuration){
        try{
            JSONObject jo =api.getTestInfo(testId);
            JSONObject result = jo.getJSONObject("result");
            JSONObject configuration = result.getJSONObject("configuration");
            JSONObject plugins = configuration.getJSONObject("plugins");
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject("override");
            override.put("duration", updDuration);
            override.put("threads", JSONObject.NULL);
            configuration.put("serversCount",JSONObject.NULL);
            api.putTestInfo(testId,result);

        }catch(JSONException je){
            javaUtillogger.warn("Received JSONException while saving testDuration: ", je);
        }catch(Exception e){
            javaUtillogger.warn("Received JSONException while saving testDuration: ", e);
        }
    }

    public static String requestTestDuration(BlazemeterApi api, String testId){
        String duration=null;
        try{
            JSONObject jo = api.getTestInfo(testId);
            JSONObject result = jo.getJSONObject("result");
            JSONObject configuration = result.getJSONObject("configuration");
            JSONObject plugins = configuration.getJSONObject("plugins");
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject("override");
            duration = override.getString("duration");

        }catch(JSONException je){
            javaUtillogger.warn("Received JSONException while requesting testDuration: ", je);
        }catch(Exception e){
            javaUtillogger.warn("Received Exception while requesting testDuration: ", e);
        }
        return duration;
    }

    public static void uploadDataFolderFiles(String dataFolder, String mainJMX,String testId,
                                             BlazemeterApi bmAPI) {

        if (dataFolder == null || dataFolder.isEmpty())
            return;

        File folder = new File(dataFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            javaUtillogger.info("dataFolder " + dataFolder + " could not be found on local file system, please check that the folder exists.");
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
                        uploadFile(testId, bmAPI, file);
                }
            }
        }
    }

    public static void waitForFinish(BlazemeterApi api, String apiVersion, String testId, AbstractLogger logger,
                                     String session, int runDurationSeconds) throws InterruptedException {
        Date start = null;
        long lastPrint = 0;
        while (true) {
            Thread.sleep(5000);
            TestInfo info = api.getTestRunStatus(apiVersion.equals("v2") ? testId : session);

            if (!info.getStatus().equals(TestStatus.Running)) {
                javaUtillogger.info("TestStatus for session " + (apiVersion.equals("v2") ? testId : session)
                        + info.getStatus());
                javaUtillogger.info("BlazeMeter TestStatus for session" +
                        (apiVersion.equals("v2") ? testId : session)
                        + " is not 'Running': finishing build.... ");
                javaUtillogger.info("Timestamp: " + Calendar.getInstance().getTime());
                break;
            }

            if (start == null)
                start = Calendar.getInstance().getTime();
            long now = Calendar.getInstance().getTime().getTime();
            long diffInSec = (now - start.getTime()) / 1000;
            if (now - lastPrint > 10000) { //print every 10 sec.
                javaUtillogger.info("BlazeMeter test# "+testId+", session # "+session+" running from " + start + " - for " + diffInSec + " seconds");
                lastPrint = now;
            }

            if (diffInSec >= runDurationSeconds) {
                javaUtillogger.info("About to stop Blazemeter test...");
                javaUtillogger.info("Timestamp: " + Calendar.getInstance().getTime());
                api.stopTest(testId);
                javaUtillogger.info("BlazeMeter test stopped due to user test duration setup reached");
                logger.info("BlazeMeter test stopped due to user test duration setup reached");
                break;
            }
            if (Thread.interrupted()) {
                javaUtillogger.info("Test was interrupted: throwing Interrupted Exception");
                throw new InterruptedException();
            }
        }
    }

    public static String createTestFromJSON(BlazemeterApi api, String jsonConfig, FilePath workspace){
        FilePath newTestPath=workspace.child(jsonConfig);
        String testId=null;
        try {
            String jsonConfigStr=newTestPath.readToString();
            JSONObject configNode = new JSONObject(jsonConfigStr);
            JSONObject jo=api.createYahooTest(configNode);
            //get created testId;
        testId=jo.getJSONObject("result").getString("id");
        } catch (IOException e) {
            javaUtillogger.info("Failed to read JSON configuration from file" + newTestPath.getName() + ": " + e.getMessage());
        } catch (JSONException je) {
            javaUtillogger.info("Failed to read JSON configuration from file" + newTestPath.getName() + ": " + je.getMessage());
        }finally{
            return testId;
        }
    }

    public static void uploadFile(String testId, BlazemeterApi bmAPI, File file) {
        String fileName = file.getName();
        org.json.JSONObject json = bmAPI.uploadBinaryFile(testId, file);
        try {
            if (!json.get("response_code").equals(200)) {
                javaUtillogger.info("Could not upload file " + fileName + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            javaUtillogger.info("Could not upload file " + fileName + " " + e.getMessage());
        }
    }

    public static void saveReport(String filename,
                                  String report,
                                  FilePath filePath) {
        File reportFile=new File(filePath.getParent()
                +"/"+filePath.getName()+"/"+filename+".xml");
        try {
            if(!reportFile.exists()){
                reportFile.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(reportFile));
            out.write(report);
            out.close();

        }catch (FileNotFoundException fnfe)
        {
            javaUtillogger.info("ERROR: Failed to save XML report to workspace " + fnfe.getMessage());
        }
        catch (IOException e)
        {
            javaUtillogger.info("ERROR: Failed to save XML report to workspace " + e.getMessage());
        }
    }


    public static Result validateLocalTresholds(TestResult testResult,
                                              PrintStream logger,
                                              PerformanceBuilder builder){
        int responseTimeUnstable=builder.getResponseTimeUnstableThreshold();
        int responseTimeFailed=builder.getResponseTimeFailedThreshold();
        int errorUnstable=builder.getErrorUnstableThreshold();
        int errorFailed=builder.getErrorFailedThreshold();


        Result result=Result.SUCCESS;
        if(responseTimeUnstable>=0&testResult.getAverage()>responseTimeUnstable&
                testResult.getAverage()<responseTimeFailed){
        logger.println("Validating reponseTimeUnstable...\n");
        javaUtillogger.info("Validating reponseTimeUnstable...\n");
        logger.println("Average response time is higher than responseTimeUnstable treshold\n");
        javaUtillogger.info("Average response time is higher than responseTimeUnstable treshold\n");
        logger.println("Marking build as unstable");
        javaUtillogger.info("Marking build as unstable");
            result=Result.UNSTABLE;
        }

        if(errorUnstable>=0&testResult.getErrorPercentage()>errorUnstable&
                testResult.getAverage()<errorFailed){
            logger.println("Validating errorPercentageUnstable...\n");
            javaUtillogger.info("Validating errorPercentageUnstable...\n");
            logger.println("Error percentage is higher than errorPercentageUnstable treshold\n");
            javaUtillogger.info("Error percentage is higher than errorPercentageUnstable treshold\n");
            logger.println("Marking build as unstable");
            javaUtillogger.info("Marking build as unstable");
            result=Result.UNSTABLE;
        }


        if(responseTimeFailed>=0&testResult.getAverage()>=responseTimeFailed){
            logger.println("Validating reponseTimeFailed...\n");
            javaUtillogger.info("Validating reponseTimeFailed...\n");
            logger.println("Average response time is higher than responseTimeFailure treshold\n");
            javaUtillogger.info("Average response time is higher than responseTimeFailure treshold\n");
            logger.println("Marking build as failed");
            javaUtillogger.info("Marking build as failed");
            result=Result.FAILURE;
        }

        if(errorFailed>=0&testResult.getAverage()>=errorFailed){
            logger.println("Validating errorPercentageUnstable...\n");
            javaUtillogger.info("Validating errorPercentageUnstable...\n");
            logger.println("Error percentage is higher than errorPercentageUnstable treshold\n");
            javaUtillogger.info("Error percentage is higher than errorPercentageUnstable treshold\n");
            logger.println("Marking build as failed");
            javaUtillogger.info("Marking build as failed");
            result=Result.FAILURE;
        }

        if(errorUnstable<0){
            javaUtillogger.info("ErrorUnstable percentage validation was skipped: value was not set in configuration");
            logger.println("ErrorUnstable percentage validation was skipped: value was not set in configuration");
        }

        if(errorFailed<0){
            javaUtillogger.info("ErrorFailed percentage validation was skipped: value was not set in configuration");
            logger.println("ErrorFailed percentage validation was skipped: value was not set in configuration");
        }

        if(responseTimeUnstable<0){
            javaUtillogger.info("ResponseTimeUnstable validation was skipped: value was not set in configuration");
            logger.println("ResponseTimeUnstable validation was skipped: value was not set in configuration");
        }

        if(responseTimeFailed<0){
            javaUtillogger.info("ResponseTimeFailed validation was skipped: value was not set in configuration");
            logger.println("ResponseTimeFailed validation was skipped: value was not set in configuration");
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
