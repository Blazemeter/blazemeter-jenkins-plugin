package hudson.plugins.blazemeter;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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

    @Test
    public void test_proxyHost() throws SAXException,IOException,Exception {
        String proxyHost="proxyHost";
        HtmlElement blazeMeterUrl = settings.getElementByName("_.proxyHost");
        blazeMeterUrl.setAttribute("value", proxyHost);
        HtmlForm submit=settings.getFormByName("config");
        j.submit(submit);
        String actBlazeMeterUrl=settings.getElementByName("_.proxyHost").getAttribute("value");
        Assert.assertEquals(actBlazeMeterUrl,proxyHost);
    }

    @Test
    public void test_proxyPort() throws SAXException,IOException,Exception {
        String proxyPort="proxyPort";
        HtmlElement blazeMeterUrl = settings.getElementByName("_.proxyPort");
        blazeMeterUrl.setAttribute("value", proxyPort);
        HtmlForm submit=settings.getFormByName("config");
        j.submit(submit);
        String actBlazeMeterUrl=settings.getElementByName("_.proxyPort").getAttribute("value");
        Assert.assertEquals(actBlazeMeterUrl,proxyPort);
    }

    @Test
    public void test_proxyUser() throws SAXException,IOException,Exception {
        String proxyUser="proxyUser";
        HtmlElement blazeMeterUrl = settings.getElementByName("_.proxyUser");
        blazeMeterUrl.setAttribute("value", proxyUser);
        HtmlForm submit=settings.getFormByName("config");
        j.submit(submit);
        String actBlazeMeterUrl=settings.getElementByName("_.proxyUser").getAttribute("value");
        Assert.assertEquals(actBlazeMeterUrl,proxyUser);
    }

    @Test
    public void test_proxyPass() throws SAXException,IOException,Exception {
        String proxyPass="proxyPass";
        HtmlElement blazeMeterUrl = settings.getElementByName("_.proxyPass");
        blazeMeterUrl.setAttribute("value", proxyPass);
        HtmlForm submit=settings.getFormByName("config");
        j.submit(submit);
        String actBlazeMeterUrl=settings.getElementByName("_.proxyPass").getAttribute("value");
        Assert.assertEquals(actBlazeMeterUrl,proxyPass);
    }
}
