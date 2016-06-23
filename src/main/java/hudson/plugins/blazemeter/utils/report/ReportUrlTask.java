package hudson.plugins.blazemeter.utils.report;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.plugins.blazemeter.PerformanceBuildAction;
import hudson.remoting.VirtualChannel;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zmicer on 20.6.16.
 */
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

