/**
 * Copyright 2018 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hudson.plugins.blazemeter.utils;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Item;
import hudson.plugins.blazemeter.BlazemeterCredentialsBAImpl;
import hudson.security.ACL;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;


public class Utils {

    private Utils() {
    }

    public static String getTestId(String testId) {
        try {
            return testId.substring(testId.lastIndexOf("(") + 1, testId.lastIndexOf("."));
        } catch (Exception e) {
            return testId;
        }
    }


    public static List<BlazemeterCredentialsBAImpl> getCredentials(Object scope) {
        List<BlazemeterCredentialsBAImpl> result = new ArrayList<>();
        Set<String> apiKeys = new HashSet<String>();
        Item item = scope instanceof Item ? (Item) scope : null;
        for (BlazemeterCredentialsBAImpl c : CredentialsProvider
                .lookupCredentials(BlazemeterCredentialsBAImpl.class, item, ACL.SYSTEM)) {
            String id = c.getId();
            if (!apiKeys.contains(id)) {
                result.add(c);
                apiKeys.add(id);
            }
        }

        return result;
    }

    public static BlazemeterCredentialsBAImpl findCredentials(String credentialsId, Object scope) {
        List<BlazemeterCredentialsBAImpl> creds = getCredentials(scope);
        BlazemeterCredentialsBAImpl cred = BlazemeterCredentialsBAImpl.EMPTY;
        for (BlazemeterCredentialsBAImpl c : creds) {
            if (c.getId().equals(credentialsId)) {
                cred = c;
            }
        }
        return cred;
    }


    public static String version() {
        Properties props = new Properties();
        ClassLoader classLoader = Utils.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("version.properties");
        if (resourceAsStream != null) {
            try {
                props.load(resourceAsStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return props.getProperty(Constants.VERSION);
    }

    public static String resolveTestId(String savedTestId) {
        try {
            int startIndex = savedTestId.lastIndexOf("(") + 1;
            int endIndex = savedTestId.lastIndexOf(")");
            return savedTestId.substring(startIndex, endIndex);
        } catch (Exception e) {
            return savedTestId;
        }
    }

}
