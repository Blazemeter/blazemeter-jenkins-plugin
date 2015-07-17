package hudson.plugins.blazemeter.utils;

import hudson.plugins.blazemeter.api.TestType;

/**
 * Created by zmicer on 17.7.15.
 */
public class Utils {

    private Utils(){}

    public static TestType getTestType(String testId,int dotPos){
        TestType testType=null;
        try{
            testType=TestType.valueOf(testId.substring(dotPos+1));
        }catch (Exception e){
            testType = TestType.unknown_type;
        }
        return testType;
    }
}
