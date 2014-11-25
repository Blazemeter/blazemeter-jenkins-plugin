package hudson.plugins.blazemeter.utils;

import hudson.FilePath;
import hudson.model.Result;
import hudson.plugins.blazemeter.PerformanceBuilder;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.entities.TestInfo;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.testresult.TestResult;
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

    private Utils(){}


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
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
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
            je.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
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
            je.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        return duration;
    }

    public static void uploadDataFolderFiles(String dataFolder, String mainJMX,String testId,
                                             BlazemeterApi bmAPI, PrintStream logger) {

        if (dataFolder == null || dataFolder.isEmpty())
            return;

        File folder = new File(dataFolder);
        if (!folder.exists() || !folder.isDirectory()) {
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
                        bmAPI.uploadJmx(testId, file);
                    else
                        uploadFile(testId, bmAPI, file, logger);
                }
            }
        }
    }

    public static void wait_for_finish(BlazemeterApi api,String apiVersion,String testId,PrintStream logger,
                                       String session, int runDurationSeconds) throws InterruptedException {
        Date start = null;

        long lastPrint = 0;
        while (true) {
            Thread.sleep(5000);
            TestInfo info = api.getTestRunStatus(apiVersion.equals("v2") ? testId : session);

            if (!info.getStatus().equals(TestStatus.Running)) {
                break;
            }

            if (start == null)
                start = Calendar.getInstance().getTime();
            long now = Calendar.getInstance().getTime().getTime();
            long diffInSec = (now - start.getTime()) / 1000;
            if (now - lastPrint > 10000) { //print every 10 sec.
                logger.println("BlazeMeter test running from " + start + " - for " + diffInSec + " seconds");
                lastPrint = now;
            }

            if (diffInSec >= runDurationSeconds) {
                api.stopTest(testId);
                logger.println("BlazeMeter test stopped due to user test duration setup reached");
                break;
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }

    public static String createTestFromJSON(BlazemeterApi api, String jsonConfig, FilePath workspace,PrintStream logger){
        FilePath newTestPath=workspace.child(jsonConfig);
        String testId=null;
        try {
            String jsonConfigStr=newTestPath.readToString();
            JSONObject configNode = new JSONObject(jsonConfigStr);
            JSONObject jo=api.createYahooTest(configNode);
            //get created testId;
        testId=jo.getJSONObject("result").getString("id");
        } catch (IOException e) {
            logger.println("Failed to read JSON configuration from file"+newTestPath.getName()+": "+e.getMessage());
        } catch (JSONException je) {
            logger.println("Failed to read JSON configuration from file"+newTestPath.getName()+": "+je.getMessage());
        }finally{
            return testId;
        }
    }

    public static void uploadFile(String testId, BlazemeterApi bmAPI, File file, PrintStream logger) {
        String fileName = file.getName();
        org.json.JSONObject json = bmAPI.uploadBinaryFile(testId, file);
        try {
            if (!json.get("response_code").equals(200)) {
                logger.println("Could not upload file " + fileName + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            logger.println("Could not upload file " + fileName + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveReport(String filename,
                                  String report,
                                  FilePath filePath,
                                  PrintStream logger) {
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
            logger.println("ERROR: Failed to save XML report to workspace "+ fnfe.getMessage());
        }
        catch (IOException e)
        {
            logger.println("ERROR: Failed to save XML report to workspace "+ e.getMessage());
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
        logger.println("Average response time is higher than responseTimeUnstable treshold\n");
        logger.println("Marking build as unstable");
            result=Result.UNSTABLE;
        }

        if(responseTimeFailed>=0&testResult.getAverage()>=responseTimeFailed){
            logger.println("Validating reponseTimeFailed...\n");
            logger.println("Average response time is higher than responseTimeFailure treshold\n");
            logger.println("Marking build as failed");
            result=Result.FAILURE;
        }

        if(errorUnstable<0){
            logger.println("ErrorUnstable percentage validation was skipped: value was not set in configuration");
        }

        if(errorFailed<0){
            logger.println("ErrorFailed percentage validation was skipped: value was not set in configuration");
        }

        if(responseTimeUnstable<0){
            logger.println("ResponseTimeUnstable validation was skipped: value was not set in configuration");
        }

        if(responseTimeFailed<0){
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

/*    public static  boolean updateTresholds(String testDuration,
                                    int errorUnstableThreshold,
                                    int errorFailedThreshold,
                                    int responseTimeFailedThreshold,
                                    int responseTimeUnstableThreshold,
                                    PrintStream logger) {

        boolean updateTresholds=true;
        Result result = Result.SUCCESS;
        if (testDuration == null || testDuration.isEmpty() || testDuration.equals("0")) {
            logger.println("BlazeMeter: Test duration should be more than ZERO, build is considered as "
                    + Result.NOT_BUILT.toString().toLowerCase());
            updateTresholds=false;
            return updateTresholds;
        }
        if (errorUnstableThreshold >= 0 && errorUnstableThreshold <= 100) {
            logger.println("BlazeMeter: Errors percentage greater or equal than "
                    + errorUnstableThreshold + " % will be considered as "
                    + Result.UNSTABLE.toString().toLowerCase());
        } else {
            logger.println("BlazeMeter: ErrorUnstableThreshold percentage should be between 0 to 100");
            updateTresholds=false;
            return updateTresholds;
        }

        if (errorFailedThreshold >= 0 && errorFailedThreshold <= 100) {
            logger.println("BlazeMeter: ErrorFailedThreshold percentage greater or equal than "
                    + errorFailedThreshold + " % will be considered as "
                    + Result.FAILURE.toString().toLowerCase());
        } else {
            logger.println("BlazeMeter: ErrorFailedThreshold percentage should be between 0 to 100");
            updateTresholds=false;
            return updateTresholds;
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
        updateTresholds=false;
        return updateTresholds;
    }*/

}
