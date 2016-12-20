package hudson.plugins.blazemeter;

import hudson.model.AbstractBuild;
import hudson.plugins.blazemeter.utils.report.ReportUrlTask;
import hudson.remoting.VirtualChannel;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by zmicer on 20.12.16.
 */
public class TestReportUrlTask {

    @Test
    public void run() {
        try {

            AbstractBuild b = Mockito.mock(AbstractBuild.class);
            VirtualChannel c = Mockito.mock(VirtualChannel.class);
            ReportUrlTask t = new ReportUrlTask(b, "name", c);
            t.run();
        } catch (Exception e) {
            Assert.fail();
        }
    }
}
