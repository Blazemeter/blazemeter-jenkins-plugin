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
public class TestUI {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    public FreeStyleProject project= null;
    public String responseTimeFailedThreshold="responseTimeFailedThreshold";
    public String responseTimeUnstableThreshold="responseTimeUnstableThreshold";
    public String errorUnstableThreshold="errorUnstableThreshold";
    public String errorFailedThreshold="errorFailedThreshold";
    public String jobApiKey="jobApiKey";
    public String testDuration="testDuration";
    public String testId="testId";
    public String location="location";
    public String jsonConfig="jsonConfig";
    public String useServerTresholds="useServerTresholds";

    @Before
    public void setUp() throws IOException {
        project = j.createFreeStyleProject();

    }

    @Test
    public void configure_save_check() throws Exception {
        // Create performance builder with initial settings
        PerformanceBuilder pbBefore=new PerformanceBuilder("1234567890","60","mainJmx","dataFolder", Constants.CREATE_BZM_TEST_NOTE,
                                                                      "v3",Constants.USE_TEST_LOCATION,
                                                                      "pathToJsonConfiguration",false,"1","2","3","4");
        project.getBuildersList().add(pbBefore);
        //Go to "congigure" page
        List<HtmlForm> htmlForms= j.createWebClient().getPage(project,"configure").getForms();
        //submit form
        j.submit(htmlForms.get(1));
        //make sure that builder settings did not change
        PerformanceBuilder pbAfter=project.getBuildersList().get(PerformanceBuilder.class);
        j.assertEqualBeans(pbBefore,pbAfter,jobApiKey+","+testDuration+","+testId+","+location+","+jsonConfig+","+useServerTresholds+","+errorFailedThreshold+"," +
                                             errorUnstableThreshold+","+responseTimeFailedThreshold+","+responseTimeUnstableThreshold);
    }

    @Test
    public void configure_respTimeF_check() throws Exception {
        // Create performance builder with initial settings
        PerformanceBuilder pbBefore=new PerformanceBuilder("1234567890","60","mainJmx","dataFolder", Constants.CREATE_BZM_TEST_NOTE,
                                                                      "v3",Constants.USE_TEST_LOCATION,
                                                                      "pathToJsonConfiguration",false,"1","2","3","4");
        project.getBuildersList().add(pbBefore);
        //Go to "congigure" page
        List<HtmlForm> htmlForms= j.createWebClient().getPage(project,"configure").getForms();
        HtmlForm builder=htmlForms.get(1);
        //get responseTimeTresholds
        HtmlInput respTimeF=builder.getInputByName("_."+responseTimeFailedThreshold);
        // set new value
        respTimeF.setValueAttribute("10");
        j.submit(builder);
        PerformanceBuilder pbAfter=project.getBuildersList().get(PerformanceBuilder.class);
        //make sure that other values did not change.
        j.assertEqualBeans(pbBefore,pbAfter,jobApiKey+","+testDuration+","+testId+","+location+","+jsonConfig+","+useServerTresholds+","+errorFailedThreshold+","+
                                             errorUnstableThreshold+","+responseTimeUnstableThreshold);
        builder=htmlForms.get(1);
        respTimeF=builder.getInputByName("_."+responseTimeFailedThreshold);
        //check that value was changed
        Assert.assertEquals("10", respTimeF.getValueAttribute());
    }

    @Test
    public void configure_respTimeUns_check() throws Exception {
        // Create performance builder with initial settings
        PerformanceBuilder pbBefore=new PerformanceBuilder("1234567890","60","mainJmx","dataFolder", Constants.CREATE_BZM_TEST_NOTE,
                "v3",Constants.USE_TEST_LOCATION,
                "pathToJsonConfiguration",false,"1","2","3","4");
        project.getBuildersList().add(pbBefore);
        //Go to "congigure" page
        List<HtmlForm> htmlForms= j.createWebClient().getPage(project,"configure").getForms();
        HtmlForm builder=htmlForms.get(1);
        //get responseTimeTresholds
        HtmlInput respTimeU=builder.getInputByName("_."+responseTimeUnstableThreshold);
        // set new value
        respTimeU.setValueAttribute("10");
        j.submit(builder);
        PerformanceBuilder pbAfter=project.getBuildersList().get(PerformanceBuilder.class);
        //make sure that other values did not change.
        j.assertEqualBeans(pbBefore,pbAfter,jobApiKey+","+testDuration+","+testId+","+location+","+jsonConfig+","+useServerTresholds+","
                +errorFailedThreshold+","+errorUnstableThreshold+","+responseTimeFailedThreshold);
        builder=htmlForms.get(1);
        respTimeU=builder.getInputByName("_."+responseTimeUnstableThreshold);
        //check that value was changed
        Assert.assertEquals("10", respTimeU.getValueAttribute());
    }

    @Test
    public void configure_error_F_check() throws Exception {
        // Create performance builder with initial settings
        PerformanceBuilder pbBefore=new PerformanceBuilder("1234567890","60","mainJmx","dataFolder", Constants.CREATE_BZM_TEST_NOTE,
                "v3",Constants.USE_TEST_LOCATION,
                "pathToJsonConfiguration",false,"1","2","3","4");
        project.getBuildersList().add(pbBefore);
        //Go to "congigure" page
        List<HtmlForm> htmlForms= j.createWebClient().getPage(project,"configure").getForms();
        HtmlForm builder=htmlForms.get(1);
        //get responseTimeTresholds
        HtmlInput errF=builder.getInputByName("_."+errorFailedThreshold);
        // set new value
        errF.setValueAttribute("10");
        j.submit(builder);
        PerformanceBuilder pbAfter=project.getBuildersList().get(PerformanceBuilder.class);
        //make sure that other values did not change.
        j.assertEqualBeans(pbBefore,pbAfter,jobApiKey+","+testDuration+","+testId+","+location+","+jsonConfig+","+useServerTresholds+","
                                            +errorUnstableThreshold+","+responseTimeFailedThreshold+","+responseTimeUnstableThreshold);
        builder=htmlForms.get(1);
        errF=builder.getInputByName("_."+errorFailedThreshold);
        //check that value was changed
        Assert.assertEquals("10", errF.getValueAttribute());
    }

    @Test
    public void configure_error_U_check() throws Exception {
        // Create performance builder with initial settings
        PerformanceBuilder pbBefore=new PerformanceBuilder("1234567890","60","mainJmx","dataFolder", Constants.CREATE_BZM_TEST_NOTE,
                "v3",Constants.USE_TEST_LOCATION,
                "pathToJsonConfiguration",false,"1","2","3","4");
        project.getBuildersList().add(pbBefore);
        //Go to "congigure" page
        List<HtmlForm> htmlForms= j.createWebClient().getPage(project,"configure").getForms();
        HtmlForm builder=htmlForms.get(1);
        //get responseTimeTresholds
        HtmlInput errU=builder.getInputByName("_."+errorUnstableThreshold);
        // set new value
        errU.setValueAttribute("10");
        j.submit(builder);
        PerformanceBuilder pbAfter=project.getBuildersList().get(PerformanceBuilder.class);
        //make sure that other values did not change.
        j.assertEqualBeans(pbBefore,pbAfter,jobApiKey+","+testDuration+","+testId+","+location+","+jsonConfig+","+useServerTresholds+","
                +errorFailedThreshold+","+responseTimeFailedThreshold+","+responseTimeUnstableThreshold);
        builder=htmlForms.get(1);
        errU=builder.getInputByName("_."+errorUnstableThreshold);
        //check that value was changed
        Assert.assertEquals("10", errU.getValueAttribute());
    }

}
