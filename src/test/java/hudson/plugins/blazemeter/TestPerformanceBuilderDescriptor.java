package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.utils.Constants;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Created by zmicer on 20.12.16.
 */
public class TestPerformanceBuilderDescriptor {
    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.getTests();
    }

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void descriptor(){
        BlazeMeterPerformanceBuilderDescriptor d = new BlazeMeterPerformanceBuilderDescriptor(TestConstants.mockedApiUrl);
        try {
            ListBoxModel tests=d.doFillTestIdItems(TestConstants.MOCKED_USER_KEY_VALID,"5270902");
            Assert.assertEquals(1,tests.size());
            Assert.assertEquals(Constants.NO_API_KEY,tests.get(0).name);
        } catch (FormValidation formValidation) {
            formValidation.printStackTrace();
            Assert.fail();
        }
    }

    @AfterClass
    public static void tearDown(){
        MockedAPI.stopAPI();
    }

}
