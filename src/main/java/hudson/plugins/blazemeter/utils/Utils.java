/**
 * Copyright 2016 BlazeMeter Inc.
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


import hudson.util.FormValidation;

import java.io.IOException;
import java.util.Properties;


public class Utils {

    private Utils() {
    }

    public static String version() {
        Properties props = new Properties();
        try {
            props.load(Utils.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty(Constants.VERSION, "N/A");
        }
        return props.getProperty(Constants.VERSION);
    }

}
