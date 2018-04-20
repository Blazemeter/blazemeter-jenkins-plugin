package hudson.plugins.blazemeter.utils;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.TestDetector;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.ciworkflow.CiBuild;
import com.blazemeter.ciworkflow.CiPostProcess;

import java.io.IOException;

public class JenkinsCiBuild extends CiBuild {

    protected AbstractTest currentTest;

    public JenkinsCiBuild(BlazeMeterUtils utils, String testId, String properties, String notes, CiPostProcess ciPostProcess) {
        super(utils, testId, properties, notes, ciPostProcess);
    }



    @Override
    public Master start() throws IOException, InterruptedException {
        this.notifier.notifyInfo("CiBuild is started.");
        AbstractTest test = TestDetector.detectTest(this.utils, this.testId);
        currentTest = test;
        if(test == null) {
            this.logger.error("Failed to detect test type. Test with id=" + this.testId + " not found.");
            this.notifier.notifyError("Failed to detect test type. Test with id = " + this.testId + " not found.");
            return null;
        } else {
            notifier.notifyInfo(String.format("Start test id : %s, name : %s", test.getId(), test.getName()));
            return this.startTest(test);
        }
    }

    public AbstractTest getCurrentTest() {
        return currentTest;
    }
}
