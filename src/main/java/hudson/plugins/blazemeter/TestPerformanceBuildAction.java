package hudson.plugins.blazemeter;

import hudson.model.AbstractBuild;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by zmicer on 20.12.16.
 */
public class TestPerformanceBuildAction {

    @Test
    public void getTarget(){
        AbstractBuild b = Mockito.mock(AbstractBuild.class);
        PerformanceBuildAction a = new PerformanceBuildAction(b);
        PerformanceReportMap m = a.getTarget();
        Assert.assertTrue(m.getBuildAction().equals(a));
        Assert.assertTrue(m.getBuildAction().equals(a));
    }
}
