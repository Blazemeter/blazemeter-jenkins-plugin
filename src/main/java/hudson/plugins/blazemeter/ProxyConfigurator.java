package hudson.plugins.blazemeter;

import hudson.ProxyConfiguration;

public class ProxyConfigurator {
    static String PROXY_HOST = "http.proxyHost";
    static String PROXY_PORT = "http.proxyPort";
    static String PROXY_USER = "http.proxyUser";
    static String PROXY_PASS = "http.proxyPass";

    public static void updateProxySettings(boolean updateProxySettings) throws Exception {
        if (updateProxySettings) {
            ProxyConfiguration proxyConfiguration = ProxyConfiguration.load();
            if (proxyConfiguration != null) {
                System.setProperty(PROXY_HOST, proxyConfiguration.name);
                System.setProperty(PROXY_PORT, String.valueOf(proxyConfiguration.port));
                System.setProperty(PROXY_USER, proxyConfiguration.getUserName());
                System.setProperty(PROXY_PASS, proxyConfiguration.getPassword());
            } else {
                System.clearProperty(PROXY_HOST);
                System.clearProperty(PROXY_PORT);
                System.clearProperty(PROXY_USER);
                System.clearProperty(PROXY_PASS);
            }
        }
    }
}
