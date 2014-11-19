package hudson.plugins.blazemeter.utils;

import hudson.FilePath;
import hudson.plugins.blazemeter.BlazeMeterPerformanceBuilderDescriptor;
import hudson.plugins.blazemeter.PerformanceBuilder;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.api.BlazemeterApiV2Impl;
import hudson.plugins.blazemeter.api.BlazemeterApiV3Impl;
import hudson.plugins.blazemeter.entities.TestInfo;
import hudson.plugins.blazemeter.entities.TestStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;

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

    public static void checkAPIisV3version(BlazemeterApi api) throws Exception{
        if(api instanceof BlazemeterApiV2Impl){
            throw new Exception("Can't fetch test duration from server: select API V3");
        }
    }


    public static void saveTestDuration(BlazemeterApi api, String testId, String updDuration){
        try{
            checkAPIisV3version(api);
            JSONObject jo = ((BlazemeterApiV3Impl)api).getTestInfo(testId);
            JSONObject result = jo.getJSONObject("result");
            JSONObject configuration = result.getJSONObject("configuration");
            JSONObject plugins = configuration.getJSONObject("plugins");
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject("override");
            override.put("duration", updDuration);
            override.put("threads", JSONObject.NULL);
            configuration.put("serversCount",JSONObject.NULL);
            ((BlazemeterApiV3Impl)api).putTestInfo(testId,result);

        }catch(JSONException je){
            je.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String requestTestDuration(BlazemeterApi api, String testId){
        String duration=null;
        try{
            checkAPIisV3version(api);
            JSONObject jo = ((BlazemeterApiV3Impl)api).getTestInfo(testId);
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
            JSONObject jo=((BlazemeterApiV3Impl)api).createYahooTest(configNode);
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

    public static void updateBZMUrl(BlazeMeterPerformanceBuilderDescriptor descriptor,
                                    BlazemeterApi api, PrintStream logger){
        String url=descriptor.getBlazeMeterURL();
        api.setBlazeMeterURL(url);
        logger.println("BlazeMeterURL=" + url+" will be used for test");
    }

}
