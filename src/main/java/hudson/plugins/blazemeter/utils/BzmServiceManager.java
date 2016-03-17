package hudson.plugins.blazemeter.utils;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.plugins.blazemeter.*;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.api.BlazemeterApiV3Impl;
import hudson.plugins.blazemeter.entities.CIStatus;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.testresult.TestResult;
import hudson.util.FormValidation;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by dzmitrykashlach on 18/11/14.
 */
public class BzmServiceManager {
    private static StdErrLog logger = new StdErrLog(Constants.BZM_JEN);
    private final static int BUFFER_SIZE = 2048;
    private final static String ZIP_EXTENSION = ".zip";
    private BzmServiceManager() {
    }

    public static void waitForFinish(BlazemeterApi api, String testId, AbstractLogger bzmBuildLog,
                                     String session) throws InterruptedException {
        Date start = null;
        long lastPrint = 0;
        while (true) {
            Thread.sleep(15000);
            TestStatus testStatus = api.getTestStatus(session);

            if (!testStatus.equals(TestStatus.Running)) {
                bzmBuildLog.info("TestStatus for session " + session +
                        " " + testStatus);
                bzmBuildLog.info("BlazeMeter TestStatus for session" +
                        session
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

            if (Thread.interrupted()) {
                bzmBuildLog.info("Job was stopped by user");
                throw new InterruptedException("Job was stopped by user");
            }
        }
    }

    public static String getReportUrl(BlazemeterApi api, String masterId,
                                      StdErrLog jenBuildLog, StdErrLog bzmBuildLog) {
        JSONObject jo=null;
        String publicToken="";
        String reportUrl=null;
        try {
            jo = api.generatePublicToken(masterId);
            if(jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)){
                JSONObject result=jo.getJSONObject(JsonConstants.RESULT);
                publicToken=result.getString("publicToken");
                reportUrl=api.getBlazeMeterURL()+"/app/?public-token="+publicToken+"#masters/"+masterId+"/summary";
            }else{
                jenBuildLog.warn("Problems with generating public-token for report URL: "+jo.get(JsonConstants.ERROR).toString());
                bzmBuildLog.warn("Problems with generating public-token for report URL: "+jo.get(JsonConstants.ERROR).toString());
                reportUrl=api.getBlazeMeterURL()+"/app/#masters/"+masterId+"/summary";
            }

        } catch (Exception e){
          jenBuildLog.warn("Problems with generating public-token for report URL");
          bzmBuildLog.warn("Problems with generating public-token for report URL",e);
        }finally {
                return reportUrl;
        }
    }

    public static String getSessionId(JSONObject json,StdErrLog bzmBuildLog,StdErrLog jenBuildLog) throws JSONException {
        String session = "";
        try {

            // get sessionId add to interface
                JSONObject startJO = (JSONObject) json.get(JsonConstants.RESULT);
                session = ((JSONArray) startJO.get("sessionsId")).get(0).toString();
        } catch (Exception e) {
            jenBuildLog.info("Failed to get session_id: " + e.getMessage());
            bzmBuildLog.info("Failed to get session_id. ", e);
        }
        return session;
    }

    public static void publishReport(BlazemeterApi api, String masterId,
                                     AbstractBuild<?, ?> build,
                                     String bzmBuildLogPath,
                                     StdErrLog jenBuildLog,
                                     StdErrLog bzmBuildLog){

        String reportUrl= getReportUrl(api, masterId, jenBuildLog,bzmBuildLog);
        jenBuildLog.info("BlazeMeter test report will be available at " + reportUrl);
        jenBuildLog.info("BlazeMeter test log will be available at " + bzmBuildLogPath);

        PerformanceBuildAction a = new PerformanceBuildAction(build);
        a.setReportUrl(reportUrl);
        build.addAction(a);

    }

    public static void saveReport(String filename,
                                  String report,
                                  FilePath filePath,
                                  StdErrLog jenBuildLog) {
        File reportFile = new File(filePath.getParent()
                + "/" + filePath.getName() + "/" + filename);
        try {
            if (!reportFile.exists()) {
                FileUtils.forceMkdir(reportFile.getParentFile());
                reportFile.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(reportFile));
            out.write(report);
            out.close();

        } catch (FileNotFoundException fnfe) {
            jenBuildLog.info("ERROR: Failed to save XML report to workspace " + fnfe.getMessage());
        } catch (IOException e) {
            jenBuildLog.info("ERROR: Failed to save XML report to workspace " + e.getMessage());
        }
    }

    public static CIStatus validateCIStatus(BlazemeterApi api, String session, StdErrLog jenBuildLog){
        CIStatus ciStatus=CIStatus.success;
        JSONObject jo;
        JSONArray failures=new JSONArray();
        JSONArray errors=new JSONArray();
        try {
            jo=api.getCIStatus(session);
            jenBuildLog.info("Test status object = " + jo.toString());
            failures=jo.getJSONArray(JsonConstants.FAILURES);
            errors=jo.getJSONArray(JsonConstants.ERRORS);
        } catch (JSONException je) {
            jenBuildLog.warn("No thresholds on server: setting 'success' for CIStatus ");
        } catch (Exception e) {
            jenBuildLog.warn("No thresholds on server: setting 'success' for CIStatus ");
        }finally {
            if(errors.length()>0){
                jenBuildLog.info("Having errors while test status validation...");
                jenBuildLog.info("Errors: " + errors.toString());
                ciStatus=CIStatus.errors;
                jenBuildLog.info("Setting CIStatus="+CIStatus.errors.name());
                return ciStatus;
            }
            if(failures.length()>0){
                jenBuildLog.info("Having failures while test status validation...");
                jenBuildLog.info("Failures: " + failures.toString());
                ciStatus=CIStatus.failures;
                jenBuildLog.info("Setting CIStatus="+CIStatus.failures.name());
                return ciStatus;
            }
            jenBuildLog.info("No errors/failures while validating CIStatus: setting "+CIStatus.success.name());
        }
        return ciStatus;
    }

    public static String selectUserKeyOnId(BlazeMeterPerformanceBuilderDescriptor descriptor,
                                           String id){
        String userKey=null;
        List<BlazemeterCredential> credentialList=descriptor.getCredentials("Global");
        if(credentialList.size()==1){
            userKey=credentialList.get(0).getApiKey();
        }else{
            for(BlazemeterCredential c:credentialList){
                if(c.getId().equals(id)){
                    userKey=c.getApiKey();
                    break;
                }
            }
        }
        return userKey;
    }

    public static String selectUserKeyId(BlazeMeterPerformanceBuilderDescriptor descriptor,
                                           String userKey){
        String userKeyId=null;
        List<BlazemeterCredential> credentialList=descriptor.getCredentials("Global");
        if(credentialList.size()==1){
            userKeyId=credentialList.get(0).getId();
        }else{
            for(BlazemeterCredential c:credentialList){
                if(c.getApiKey().equals(userKey)){
                    userKeyId=c.getId();
                    break;
                }
            }
        }
        return userKeyId;
    }

    public static void downloadJtlReport(BlazemeterApi api, String sessionId, FilePath filePath,
                                         String buildNumber,
                                         StdErrLog jenBuildLog,
                                         StdErrLog bzmBuildLog) {

        JSONObject jo=api.retrieveJtlZip(sessionId);
        String dataUrl=null;
        URL url=null;
        try {
            JSONArray data=jo.getJSONObject(JsonConstants.RESULT).getJSONArray(JsonConstants.DATA);
            for(int i=0;i<data.length();i++){
                String title=data.getJSONObject(i).getString("title");
                if(title.equals("Zip")){
                    dataUrl=data.getJSONObject(i).getString(JsonConstants.DATA_URL);
                    break;
                }
            }
            File jtlZip=new File(filePath.getParent()
                    + "/" + buildNumber + "/" +sessionId+"-"+ Constants.BM_ARTEFACTS);

            if(!dataUrl.contains("amazonaws")){
                url=new URL(dataUrl+"?api_key="+api.getApiKey());
            }else{
                url=new URL(dataUrl);
            }

            FileUtils.copyURLToFile(url, jtlZip);
            jenBuildLog.info("Downloading JTLZIP .... ");
            String jtlZipCanonicalPath=jtlZip.getCanonicalPath();
            jenBuildLog.info("Saving ZIP to " + jtlZipCanonicalPath);
            unzip(jtlZip.getAbsolutePath(), jtlZipCanonicalPath.substring(0,jtlZipCanonicalPath.length()-4), jenBuildLog);
            FilePath sample_jtl=new FilePath(filePath,"sample.jtl");
            FilePath bm_kpis_jtl=new FilePath(filePath,Constants.BM_KPIS);
            if(sample_jtl.exists()){
                sample_jtl.renameTo(bm_kpis_jtl);
            }
        } catch (JSONException e) {
            bzmBuildLog.warn("Unable to get  JTLZIP from "+url, e);
            jenBuildLog.warn("Unable to get  JTLZIP from "+url+" "+e.getMessage());
        } catch (MalformedURLException e) {
            bzmBuildLog.warn("Unable to get  JTLZIP from "+url, e);
            jenBuildLog.warn("Unable to get  JTLZIP from "+url+" "+e.getMessage());
        } catch (IOException e) {
            bzmBuildLog.warn("Unable to get JTLZIP from "+url, e);
            jenBuildLog.warn("Unable to get JTLZIP from "+url+" "+e.getMessage());
        } catch (InterruptedException e) {
            bzmBuildLog.warn("Unable to get JTLZIP from "+url, e);
            jenBuildLog.warn("Unable to get JTLZIP from "+url+" "+e.getMessage());
        }
    }

    public static void downloadJtlReports(BlazemeterApi api, String masterId, FilePath filePath,
                                          String buildNumber, StdErrLog jenBuildLog,
                                          StdErrLog bzmBuildLog) {
        List<String> sessionsIds = api.getListOfSessionIds(masterId);
        for (String s : sessionsIds) {
            downloadJtlReport(api, s, filePath,buildNumber, jenBuildLog, bzmBuildLog);
        }
    }



    public static void unzip(String srcZipFileName,
                             String destDirectoryName, StdErrLog jenBuildLog) {
        try {
            BufferedInputStream bufIS = null;
            // create the destination directory structure (if needed)
            File destDirectory = new File(destDirectoryName);
            destDirectory.mkdirs();

            // open archive for reading
            File file = new File(srcZipFileName);
            ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ);

            //for every zip archive entry do
            Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                if(entry.getName().substring((entry.getName().length()-4)).equals(".jtl")&!entry.isDirectory()){
                    jenBuildLog.info("\tExtracting jtl report: " + entry);

                    //create destination file
                    File destFile = new File(destDirectory, entry.getName());

                    //create parent directories if needed
                    File parentDestFile = destFile.getParentFile();
                    parentDestFile.mkdirs();

                        bufIS = new BufferedInputStream(
                                zipFile.getInputStream(entry));
                        int currentByte;

                        // buffer for writing file
                        byte data[] = new byte[BUFFER_SIZE];

                        // write the current file to disk
                        FileOutputStream fOS = new FileOutputStream(destFile);
                        BufferedOutputStream bufOS = new BufferedOutputStream(fOS, BUFFER_SIZE);

                        while ((currentByte = bufIS.read(data, 0, BUFFER_SIZE)) != -1) {
                            bufOS.write(data, 0, currentByte);
                        }

                        // close BufferedOutputStream
                        bufOS.flush();
                        bufOS.close();

                }

            }
            bufIS.close();
        } catch (Exception e) {
            jenBuildLog.warn("Failed to unzip report: check that it is downloaded");
        }
    }

    public static void retrieveJUNITXMLreport(BlazemeterApi api, String masterId, FilePath workspace, String buildNumber, StdErrLog jenBuildLog){
        String junitReport="";
        jenBuildLog.info("Requesting JUNIT report from server, masterId="+masterId);
        try{
            junitReport = api.retrieveJUNITXML(masterId);
            String junitReportName = masterId + "-" + Constants.BM_TRESHOLDS;
            FilePath junitReportFilePath = new FilePath(workspace,buildNumber);
            jenBuildLog.warn("build number="+buildNumber);
            jenBuildLog.warn("masterId=" + masterId);
            String junitReportPath = workspace.getParent()
                    + "/" + workspace.getName() + "/" +buildNumber+"/"+ masterId + "-" + Constants.BM_TRESHOLDS;
            jenBuildLog.info("Received Junit report from server.... masterId=" + masterId);
            jenBuildLog.info("Saving it to " + junitReportPath);
            saveReport(junitReportName, junitReport, junitReportFilePath, jenBuildLog);
        } catch (Exception e) {
            jenBuildLog.warn("Problems with receiving JUNIT report from server, masterId=" + masterId + ": " + e.getMessage());
        }
    }

    public static Result postProcess(PerformanceBuilder builder,String masterId,String buildNumber) throws InterruptedException {
        Thread.sleep(10000); // Wait for the report to generate.
        //get thresholds from server and check if test is success
        Result result;
        BlazemeterApi api=builder.getApi();
        StdErrLog jenBuildLog=builder.getJenBuildLog();
        CIStatus ciStatus = BzmServiceManager.validateCIStatus(api, masterId, jenBuildLog);
        if(ciStatus.equals(CIStatus.errors)){
            result=Result.FAILURE;
            return result;
        }
        result=ciStatus.equals(CIStatus.failures)?Result.FAILURE:Result.SUCCESS;
        FilePath workspace = builder.getBuild().getWorkspace();
        if (builder.isGetJunit()) {
            retrieveJUNITXMLreport(api, masterId, workspace, buildNumber, jenBuildLog);
        } else {
            jenBuildLog.info("JUNIT report won't be requested: check-box is unchecked.");
        }
        Thread.sleep(30000);
        if(builder.isGetJtl()){
            AbstractBuild build=builder.getBuild();
            FilePath jtlPath = new FilePath(build.getWorkspace(), build.getId());
            BzmServiceManager.downloadJtlReports(api, masterId, jtlPath, buildNumber, jenBuildLog, jenBuildLog);
        } else {
            jenBuildLog.info("JTL report won't be requested: check-box is unchecked.");
        }



        //get testGetArchive information
        JSONObject testReport=requestAggregateReport(api,jenBuildLog,masterId);


        if (testReport == null || testReport.equals("null")) {
            jenBuildLog.warn("Aggregate report is not available after 4 attempts.");
            return result;
        }
        TestResult testResult = null;
        try {
            testResult = new TestResult(testReport);
            jenBuildLog.info(testResult.toString());
        } catch (IOException ioe) {
            jenBuildLog.info("Failed to get test result. Try to check server for it");
            jenBuildLog.info("ERROR: Failed to generate TestResult: " + ioe);
        } catch (JSONException je) {
            jenBuildLog.info("Failed to get test result. Try to check server for it");
            jenBuildLog.info("ERROR: Failed to generate TestResult: " + je);
        }finally{
            return result;
        }

    }


    public static JSONObject requestAggregateReport(BlazemeterApi api,StdErrLog jenBuildLog,String masterId){
        JSONObject testReport=null;
        int retries = 1;
        try {
            while (retries < 5 && testReport == null) {
                jenBuildLog.info("Trying to get aggregate test report from server, attempt# "+retries);
                testReport = api.testReport(masterId);
                if (testReport != null) {
                    return testReport;
                }
                Thread.sleep(5000);
                retries++;
            }
        } catch (Exception e) {
            jenBuildLog.info("Failed to get test report from server.");
        }
        return testReport;
    }

    public static boolean stopTestSession(BlazemeterApi api, String masterId, StdErrLog jenBuildLog) {
        boolean terminate = false;
        try {
                int statusCode = api.getTestMasterStatusCode(masterId);
                if (statusCode < 100 & statusCode != 0) {
                    api.terminateTest(masterId);
                    terminate = true;
                }
                if (statusCode >= 100 | statusCode == -1 | statusCode == 0) {
                    api.stopTest(masterId);
                    terminate = false;
                }
        } catch (Exception e) {
            jenBuildLog.warn("Error while trying to stop test with testId=" + masterId + ", " + e.getMessage());
        } finally {
            return terminate;
        }
    }

    public static String getVersion() {
        Properties props = new Properties();
        try {
            props.load(BzmServiceManager.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty(Constants.VERSION, "N/A");
        }
        return props.getProperty(Constants.VERSION);
    }

    public static FormValidation validateUserKey(String userKey, String blazeMeterUrl,
                                                 String proxyHost,
                                                 String proxyPort,
                                                 String proxyUser,
                                                 String proxyPass) {
        if(userKey.isEmpty()){
            logger.warn(Constants.API_KEY_EMPTY);
            return FormValidation.errorWithMarkup(Constants.API_KEY_EMPTY);
        }
        String encryptedKey=userKey.substring(0,4)+"..."+userKey.substring(17);
        try {
            logger.info("Validating API key started: API key=" + encryptedKey);
            BlazemeterApi bzm = new BlazemeterApiV3Impl(userKey, blazeMeterUrl,proxyHost,proxyPort,proxyUser,proxyPass);
            logger.info("Getting user details from server: serverUrl=" + blazeMeterUrl);
            JSONObject u = bzm.getUser();
            net.sf.json.JSONObject user = null;
            if (u!= null) {
                user = net.sf.json.JSONObject.fromObject(u.toString());
                if (user.has("error") && !user.get("error").equals(null)) {
                    logger.warn("API key is not valid: error=" + user.get("error").toString());
                    logger.warn("User profile: "+user.toString());
                    return FormValidation.errorWithMarkup("API key is not valid: error=" + user.get("error").toString());
                } else {
                    logger.warn("API key is valid: user e-mail=" + user.getString("mail"));
                    return FormValidation.ok("API key Valid. Email - " + user.getString("mail"));
                }
            }
        } catch (ClassCastException e) {
            logger.warn("API key is not valid: unexpected exception=" + e.getMessage().toString());
            logger.warn(e);
        }
        catch (Exception e) {
            logger.warn("API key is not valid: unexpected exception=" + e.getMessage().toString());
            logger.warn(e);
            return FormValidation.errorWithMarkup("API key is not valid: unexpected exception=" + e.getMessage().toString());
        }
        logger.warn("API key is not valid: userKey="+encryptedKey+" blazemeterUrl="+blazeMeterUrl+". Please, check manually.");
        return FormValidation.error("API key is not valid: API key="+encryptedKey+" blazemeterUrl="+blazeMeterUrl+". Please, check manually.");
    }

    public static String getUserEmail(String userKey,String blazemeterUrl,
                                      String proxyHost,
                                      String proxyPort,
                                      String proxyUser,
                                      String proxyPass){
        BlazemeterApi bzm = new BlazemeterApiV3Impl(userKey, blazemeterUrl,
                proxyHost,proxyPort,proxyUser,proxyPass);
        try {
            net.sf.json.JSONObject user= net.sf.json.JSONObject.fromObject(bzm.getUser().toString());
            if (user.has("mail")) {
                return user.getString("mail");
            } else {
                return "";
            }
        }catch (Exception e){
            return "";
        }
    }
}
