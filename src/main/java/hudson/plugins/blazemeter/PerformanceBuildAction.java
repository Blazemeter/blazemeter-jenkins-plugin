package hudson.plugins.blazemeter;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.util.StreamTaskListener;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerProxy;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class PerformanceBuildAction implements Action, StaplerProxy {
    private final AbstractBuild<?, ?> build;

    /**
     * Configured parsers used to parse reports in this build.
     * For compatibility reasons, this can be null.
     */
//    private final List<PerformanceReportParser> parsers;

//    private transient final PrintStream hudsonConsoleWriter;

    private transient WeakReference<PerformanceReportMap> performanceReportMap;

    private static final Logger logger = Logger.getLogger(PerformanceBuildAction.class.getName());
    private String session;
    private String blazeMeterURL;
    private String reportUrl;

    public PerformanceBuildAction(AbstractBuild<?, ?> pBuild) {
        build = pBuild;
//        hudsonConsoleWriter = logger;
//        this.parsers = parsers;
    }



    public String getDisplayName() {
        return Messages.BuildAction_DisplayName();
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    public String getUrlName() {
        return "BlazeMeter";
    }

    public PerformanceReportMap getTarget() {
        return getPerformanceReportMap();
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public PerformanceReportMap getPerformanceReportMap() {
        PerformanceReportMap reportMap = null;
        WeakReference<PerformanceReportMap> wr = this.performanceReportMap;
        if (wr != null) {
            reportMap = wr.get();
            if (reportMap != null)
                return reportMap;
        }

        try {
            reportMap = new PerformanceReportMap(this, new StreamTaskListener(
                    System.err));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating new PerformanceReportMap()", e);
        }
        this.performanceReportMap = new WeakReference<PerformanceReportMap>(
                reportMap);
        return reportMap;
    }

    public void setPerformanceReportMap(
            WeakReference<PerformanceReportMap> performanceReportMap) {
        this.performanceReportMap = performanceReportMap;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getBlazeMeterURL() {
        return blazeMeterURL;
    }

    public void setBlazeMeterURL(String blazeMeterURL) {
        this.blazeMeterURL = blazeMeterURL;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }
}
