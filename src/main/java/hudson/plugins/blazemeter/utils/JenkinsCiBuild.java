package hudson.plugins.blazemeter.utils;

import com.blazemeter.api.exception.InterruptRuntimeException;
import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.ciworkflow.CiBuild;
import com.blazemeter.ciworkflow.CiPostProcess;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class JenkinsCiBuild extends CiBuild {
    public JenkinsCiBuild(BlazeMeterUtils utils, String testId, File mainTestFile, List<File> additionalTestFiles, String properties, String notes, CiPostProcess ciPostProcess) {
        super(utils, testId, mainTestFile, additionalTestFiles, properties, notes, ciPostProcess);
    }

    @Override
    protected Master startTest(AbstractTest test) throws IOException {
        if (!StringUtils.isBlank(properties) && test instanceof SingleTest) {
            notifier.notifyInfo("Sent properties: " + properties);
            return test.startWithProperties(properties);
        } else {
            return test.start();
        }
    }

    public void uploadPropertiesAndNotes(Master master) throws IOException, InterruptedException {
        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(System.currentTimeMillis());
        notifier.notifyInfo("Test has been started successfully at " + startTime.getTime().toString() + ". Master id=" + master.getId());

        try {
            generatePublicReport(master);

            skipInitState(master);

            if (!StringUtils.isBlank(properties) && currentTest instanceof MultiTest) {
                notifier.notifyInfo("Sent properties: " + properties);
                master.postProperties(properties);
            }

            postNotes(master);
        } catch (InterruptedException | InterruptRuntimeException ex) {
            logger.warn("Interrupt master", ex);
            boolean hasReports = interrupt(master);
            if (hasReports) {
                doPostProcess(master);
            }
            throw new InterruptedException("Interrupt master");
        }
    }
}
