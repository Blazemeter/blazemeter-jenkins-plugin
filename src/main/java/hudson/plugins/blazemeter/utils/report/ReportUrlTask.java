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
package hudson.plugins.blazemeter.utils.report;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.plugins.blazemeter.PerformanceBuildAction;
import hudson.remoting.VirtualChannel;
import java.io.IOException;



public class ReportUrlTask implements Runnable {
    private VirtualChannel c = null;
    private AbstractBuild build = null;
    public boolean reportUrl = false;
    public String jobName = null;


    public ReportUrlTask(AbstractBuild build, String jobName, VirtualChannel c) {
        this.build = build;
        this.jobName=jobName;
        this.c = c;
    }

    @Override
    public void run() {
        try {
            if(this.reportUrl){
                return;
            }
            EnvVars ev = EnvVars.getRemote(c);
            String ruId = this.jobName + "-" + build.getId();
            if (ev.containsKey(ruId)) {
                PerformanceBuildAction a = new PerformanceBuildAction(build);
                a.setReportUrl(ev.get(ruId, ""));
                build.addAction(a);
                this.reportUrl = true;
            }
        } catch (InterruptedException e) {
            return;
        } catch (IOException e) {
            return;
        }
    }
}

