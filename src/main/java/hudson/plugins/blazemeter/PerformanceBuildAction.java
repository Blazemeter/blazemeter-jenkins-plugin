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

package hudson.plugins.blazemeter;

import hudson.model.Action;
import hudson.model.Run;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerProxy;


public class PerformanceBuildAction implements Action, StaplerProxy {

    private final Run run;
    private String reportUrl;
    private String linkName;
    private final String masterId;
    private PerformanceReportMap m = null;

    public PerformanceBuildAction(Run run, String masterId) {
        this.masterId = masterId;
        this.run = run;
    }

    public String getDisplayName() {
        return (!StringUtils.isBlank(linkName)) ?
                linkName :
                "BlazeMeter Report";
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    public String getUrlName() {
        return (!StringUtils.isBlank(masterId)) ?
                "BlazeMeter_" + masterId :
                "BlazeMeter";
    }

    public PerformanceReportMap getTarget() {
        if (this.m == null) {
            this.m = new PerformanceReportMap(this);
        }
        return m;
    }

    public Run getRun() {
        return run;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }
}
