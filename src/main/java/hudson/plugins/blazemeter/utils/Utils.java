package hudson.plugins.blazemeter.utils;

import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.api.BlazemeterApiV2Impl;
import hudson.plugins.blazemeter.api.BlazemeterApiV3Impl;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintStream;

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


}
