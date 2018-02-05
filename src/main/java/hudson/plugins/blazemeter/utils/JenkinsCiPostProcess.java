package hudson.plugins.blazemeter.utils;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.ciworkflow.CiPostProcess;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public class JenkinsCiPostProcess extends CiPostProcess{


    // TODO to api-client 1.3
    public JenkinsCiPostProcess(boolean isDownloadJtl, boolean isDownloadJunit, String jtlPath, String junitPath, String workspaceDir, UserNotifier notifier, Logger logger) {
        super(isDownloadJtl, isDownloadJunit, jtlPath, junitPath, workspaceDir, notifier, logger);
    }

    @Override
    protected File getParentDirWithPermissionsCheck(File dir, String workspaceDir) throws IOException {
        return new File(FilenameUtils.normalize(super.getParentDirWithPermissionsCheck(dir, workspaceDir).getAbsolutePath()));
    }
}
