package hudson.plugins.blazemeter;

import hudson.model.FreeStyleProject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;


/**
 * Created by dzmitrykashlach on 20/01/15.
 */
public class TestPerformanceBuilder {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Ignore
    @Test
    public void first() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        PerformanceBuilder pbBefore=new PerformanceBuilder("1234567890","60","","","123","v3",
                                                                      "","","","","");
//        j.jenkins
        project.getBuildersList().add(pbBefore);
//        j.submit(j.createWebClient().getPage(project,"configure").getForms().get(0));
//        PerformanceBuilder pbAfter=project.getBuildersList().get(PerformanceBuilder.class);

//        j.assertEqualBeans(pbBefore,pbAfter,"prop1,prop2,prop3,...");
    }
}
