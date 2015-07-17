package hudson.plugins.blazemeter;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import java.io.IOException;

import hudson.model.TaskListener;
import hudson.plugins.blazemeter.utils.Constants;

/**
 * Root object of a blazemeter report.
 */
public class PerformanceReportMap implements ModelObject {

    /**
     * The {@link PerformanceBuildAction} that this report belongs to.
     */
    private transient PerformanceBuildAction buildAction;

    /**
     * Parses the reports and build a {@link PerformanceReportMap}.
     *
     * @throws IOException If a report fails to parse.
     */
    PerformanceReportMap(final PerformanceBuildAction buildAction, TaskListener listener)
            throws IOException {
        this.buildAction = buildAction;
    }


    public AbstractBuild<?, ?> getBuild() {
        return buildAction.getBuild();
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
