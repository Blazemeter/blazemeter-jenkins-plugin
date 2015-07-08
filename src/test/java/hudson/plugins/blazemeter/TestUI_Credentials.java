package hudson.plugins.blazemeter;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import hudson.model.FreeStyleProject;
import hudson.plugins.blazemeter.utils.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.List;


/**
 * Created by dzmitrykashlach on 20/01/15.
 */
public class TestUI_Credentials {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    public FreeStyleProject project= null;

    @Before
    public void setUp() throws IOException {
        project = j.createFreeStyleProject();

    }
}
