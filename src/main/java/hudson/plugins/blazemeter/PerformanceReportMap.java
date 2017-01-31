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

import hudson.model.ModelObject;
import hudson.model.Run;

public class PerformanceReportMap implements ModelObject {

    private transient PerformanceBuildAction buildAction;

    PerformanceReportMap(final PerformanceBuildAction buildAction){
        this.buildAction = buildAction;
    }


    public Run getRun() {
        return buildAction.getRun();
    }

    public String getDisplayName() {
       return Messages.Report_DisplayName();
    }

    public String getReportUrl(){
        return this.buildAction.getReportUrl();
    }

    public void setReportUrl(String reportUrl){
        this.buildAction.setReportUrl(reportUrl);
    }
}
