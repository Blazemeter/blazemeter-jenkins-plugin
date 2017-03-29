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
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Item;
import hudson.plugins.blazemeter.BlazemeterCredentialImpl;
import hudson.security.ACL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.text.StrSubstitutor;


public class Utils {

    private Utils(){}

    public static String getTestId(String testId){
        try{
            return testId.substring(testId.lastIndexOf("(")+1,testId.lastIndexOf("."));
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

    public static List<BlazemeterCredentialImpl> getCredentials(Object scope) {
        List<BlazemeterCredentialImpl> result = new ArrayList<BlazemeterCredentialImpl>();
        Set<String> apiKeys = new HashSet<String>();

        Item item = scope instanceof Item ? (Item) scope : null;
        for (BlazemeterCredentialImpl c : CredentialsProvider
            .lookupCredentials(BlazemeterCredentialImpl.class, item, ACL.SYSTEM)) {
            String id = c.getId();
            if (!apiKeys.contains(id)) {
                result.add(c);
                apiKeys.add(id);
            }
        }
        return result;
    }

    public static boolean credPresent(String userKey, Object scope) {
        List<BlazemeterCredentialImpl> cred = getCredentials(scope);

        boolean valid = false;
        for (BlazemeterCredentialImpl c : cred) {
            if (c.getUsername().equals(userKey)) {
                valid = true;
            }
        }
        return valid;
    }

}
