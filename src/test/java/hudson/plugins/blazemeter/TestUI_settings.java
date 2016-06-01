package hudson.plugins.blazemeter;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;


/**
 * Created by dzmitrykashlach on 20/01/15.
 */
public class TestUI_settings {

    @Rule
    public  JenkinsRule j = new JenkinsRule();
    public  HtmlPage settings=null;
    public  JenkinsRule.WebClient webClient=null;

    @Before
    public void setUp() throws SAXException,IOException {
        webClient=j.createWebClient();
        settings=webClient.goTo("configure");
    }

    @Test
    public void test_blazemeterUrl() throws SAXException,IOException,Exception {
        String expBlazemeterUrl="https://a.blazemeter.com";
        HtmlElement blazeMeterUrl = settings.getElementByName("_.blazeMeterURL");
        blazeMeterUrl.setAttribute("value", expBlazemeterUrl);
        HtmlForm submit=settings.getFormByName("config");
        j.submit(submit);
        String actBlazeMeterUrl=settings.getElementByName("_.blazeMeterURL").getAttribute("value");
        Assert.assertEquals(actBlazeMeterUrl,expBlazemeterUrl);
    }
}
