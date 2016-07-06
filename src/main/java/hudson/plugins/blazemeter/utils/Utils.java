/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package hudson.plugins.blazemeter.utils;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.plugins.blazemeter.api.TestType;
import org.apache.commons.lang3.text.StrSubstitutor;


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
