package hudson.plugins.blazemeter.utils;
import java.io.File;
import hudson.FilePath;
import hudson.plugins.blazemeter.api.TestType;

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

    public static FilePath resolvePath(FilePath workspace,String path) throws Exception{
        FilePath fp=null;
        FilePath root=new FilePath(new File("/"));
        if(path.startsWith("/")){
            fp=new FilePath(root,path);
        }else{
            fp=new FilePath(workspace,path);
        }
        if(!fp.exists()){
            /*
            TODO
            - try to create absent foldeer before throwing exception
             */
            throw new Exception("Failed to find filepath = "+fp.getName());
        }

        /*
          TODO
        - resolve jenkins variable;
         */
        return fp;
    }
}
