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
package hudson.plugins.blazemeter;

import javaposse.jobdsl.dsl.Context;

public class PerformanceBuilderDSLContext implements Context {
    String jobApiKey = "";

    String testId = "";

    String notes = "";

    String sessionProperties = "";

    String jtlPath = "";

    String junitPath = "";

    boolean getJtl = false;

    boolean getJunit = false;


    public void jobApiKey(String jobApiKey) {
        this.jobApiKey = jobApiKey;
    }

    public void testId(String testId) {
        this.testId = testId;
    }

    public void notes(String notes) {
        this.notes = notes;
    }

    public void sessionProperties(String sessionProperties) {
        this.sessionProperties = sessionProperties;
    }

    public void jtlPath(String jtlPath) {
        this.jtlPath = jtlPath;
    }

    public void junitPath(String junitPath) {
        this.junitPath = junitPath;
    }

    public void getJtl(boolean getJtl) {
        this.getJtl = getJtl;
    }

    public void getJunit(boolean getJunit) {
        this.getJunit = getJunit;
    }

}
