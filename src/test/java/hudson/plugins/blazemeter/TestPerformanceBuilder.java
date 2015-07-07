package hudson.plugins.blazemeter;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import hudson.model.FreeStyleProject;
import hudson.plugins.blazemeter.utils.Constants;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;


/**
 * Created by dzmitrykashlach on 20/01/15.
 */
public class TestPerformanceBuilder {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void configure_save() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        PerformanceBuilder pbBefore=new PerformanceBuilder("1234567890","60","mainJmx","dataFolder", Constants.CREATE_BZM_TEST_NOTE,
                                                                      "v3",Constants.USE_TEST_LOCATION,
                                                                      "pathToJsonConfiguration",false,"1","2","3","4");
        project.getBuildersList().add(pbBefore);
        List<HtmlForm> htmlForms= j.createWebClient().getPage(project,"configure").getForms();
        j.submit(htmlForms.get(1));
        PerformanceBuilder pbAfter=project.getBuildersList().get(PerformanceBuilder.class);
        j.assertEqualBeans(pbBefore,pbAfter,"jobApiKey,testDuration,testId,location,jsonConfig,useServerTresholds,errorFailedThreshold," +
                                             "errorUnstableThreshold,responseTimeFailedThreshold,responseTimeUnstableThreshold");
    }
}
