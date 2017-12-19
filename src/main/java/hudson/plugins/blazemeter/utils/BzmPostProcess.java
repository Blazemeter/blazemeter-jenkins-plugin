package hudson.plugins.blazemeter.utils;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.ciworkflow.CiPostProcess;
import hudson.FilePath;

public class BzmPostProcess extends CiPostProcess {

    private final FilePath workspace;

    public BzmPostProcess(boolean isDownloadJtl, boolean isDownloadJunit,
                          String jtlPath, String junitPath,
                          FilePath workspace,
                          UserNotifier notifier, Logger logger) {
        super(isDownloadJtl, isDownloadJunit, jtlPath, junitPath, workspace.getRemote(), notifier, logger);
        this.workspace = workspace;
    }

    @Override
    public void saveJunit(Master master) {
        super.saveJunit(master);
    }

    @Override
    public void saveJTL(Master master) {
        super.saveJTL(master);
    }
}
