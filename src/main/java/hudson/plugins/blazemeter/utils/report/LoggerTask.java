package hudson.plugins.blazemeter.utils.report;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.plugins.blazemeter.PerformanceBuildAction;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.remoting.VirtualChannel;
import org.eclipse.jetty.util.log.StdErrLog;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zmicer on 20.6.16.
 */
public class LoggerTask implements Runnable {
    private FilePath l = null;
    private PrintStream ps = null;
    private int logged=0;

    public LoggerTask(PrintStream ps,FilePath l) {
        this.ps = ps;
        this.l = l;
    }

    @Override
    public void run() {
        try {
            StdErrLog errLog = new StdErrLog(Constants.BZM_JEN);
            errLog.setPrintLongNames(false);
            errLog.setStdErrStream(this.ps);
            String s=this.l.readToString();
            if (s != null) {
                int currentLogged=0;
                List<String> ls= Arrays.asList(s.split("\\n"));
                int lss=ls.size();
                for(int i=0+this.logged;i<lss;i++){
                    String lstr=ls.get(i);
                    errLog.info(lstr.substring(lstr.lastIndexOf(": ")+2));
                    currentLogged++;
                }
                this.logged=this.logged+currentLogged;

            }
        } catch (Exception e) {
            this.logged=0;
            return;
        }
    }
}

