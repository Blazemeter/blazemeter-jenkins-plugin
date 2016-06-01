package hudson.plugins.blazemeter.utils;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.plugins.blazemeter.api.TestType;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Created by zmicer on 17.7.15.
 */
public class Utils {

    private Utils(){}

    public static TestType getTestType(String testId) throws Exception{
        int dotPos=testId.indexOf(".");
        TestType testType=null;
        try{
            testType=TestType.valueOf(testId.substring(dotPos+1));
        }catch (Exception e){
            throw e;
        }
        return testType;
    }

    public static String getTestId(String testId){
        try{
            return testId.substring(0,testId.indexOf("."));
        }catch (Exception e){
            return testId;
        }
     }

    public static FilePath resolvePath(FilePath workspace, String path, EnvVars vars) throws Exception {
        FilePath fp = null;
        StrSubstitutor strSubstr=new StrSubstitutor(vars);
        String resolvedPath=strSubstr.replace(path);
        if (resolvedPath.startsWith("/")|resolvedPath.matches("(^[a-zA-Z][:][\\\\].+)")) {
            fp = new FilePath(workspace.getChannel(), resolvedPath);
        } else {
            fp = new FilePath(workspace, resolvedPath);
        }
        if (!fp.exists()) {
            try {
                fp.mkdirs();
            } catch (Exception e) {
                throw new Exception("Failed to find filepath = " + fp.getName());
            }
        }
        return fp;
    }


}
