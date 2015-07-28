package hudson.plugins.blazemeter;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import hudson.model.FreeStyleProject;
import hudson.plugins.blazemeter.utils.Constants;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.List;


/**
 * Created by dzmitrykashlach on 20/01/15.
 */
public class TestUI_JobConfiguration {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    public FreeStyleProject project= null;
    public String jobApiKey="jobApiKey";
    public String testDuration="testDuration";
    public String testId="testId";
    public String getJtl="getJtl";
    public String getJunit="getJunit";

    @Before
    public void setUp() throws IOException {
        project = j.createFreeStyleProject();

    }

    @Test
    public void configure_save_check() throws Exception {
        // Create performance builder with initial settings
        PerformanceBuilder pbBefore=new PerformanceBuilder("1234567890","60", testId,
                                                                      "v3",false,true);
        project.getBuildersList().add(pbBefore);
        //Go to "congigure" page
        List<HtmlForm> htmlForms= j.createWebClient().getPage(project,"configure").getForms();
        //submit form
        j.submit(htmlForms.get(1));
        //make sure that builder settings did not change
        PerformanceBuilder pbAfter=project.getBuildersList().get(PerformanceBuilder.class);
        j.assertEqualBeans(pbBefore,pbAfter,jobApiKey+","+testDuration+","+getJtl+","+getJunit);
    }
}
