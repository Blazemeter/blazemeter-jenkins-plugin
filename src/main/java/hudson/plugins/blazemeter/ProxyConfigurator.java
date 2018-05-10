/**
 * Copyright 2018 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hudson.plugins.blazemeter;

import hudson.ProxyConfiguration;
import org.apache.commons.lang.StringUtils;

import java.util.logging.Logger;

public class ProxyConfigurator {
    private static final Logger LOGGER = Logger.getLogger(ProxyConfigurator.class.getName());

    static String PROXY_HOST = "http.proxyHost";
    static String PROXY_PORT = "http.proxyPort";
    static String PROXY_USER = "http.proxyUser";
    static String PROXY_PASS = "http.proxyPass";

    static String PROXY_OVERRIDE = "proxy.override";

    public static void updateProxySettings(ProxyConfiguration proxyConfiguration, boolean isSlave) throws Exception {
        if (isSlave && "true".equals(System.getProperty(PROXY_OVERRIDE))) {
            LOGGER.info("Override proxy setting for slave");
            LOGGER.info("Use proxy host: " + System.getProperty(PROXY_HOST));
            LOGGER.info("Use proxy port: " + System.getProperty(PROXY_PORT));
            return;
        }

        if (proxyConfiguration != null) {
            if (StringUtils.isNotBlank(proxyConfiguration.name)) {
                LOGGER.info("Use proxy host: " + proxyConfiguration.name);
                System.setProperty(PROXY_HOST, proxyConfiguration.name);
            }

            if (StringUtils.isNotBlank(String.valueOf(proxyConfiguration.port))) {
                LOGGER.info("Use proxy port: " + proxyConfiguration.port);
                System.setProperty(PROXY_PORT, String.valueOf(proxyConfiguration.port));
            }

            if (StringUtils.isNotBlank(proxyConfiguration.getUserName())) {
                System.setProperty(PROXY_USER, proxyConfiguration.getUserName());
            }

            if (StringUtils.isNotBlank(proxyConfiguration.getPassword())) {
                System.setProperty(PROXY_PASS, proxyConfiguration.getPassword());
            }
        } else {
            LOGGER.info("Clear proxy configs");
            System.clearProperty(PROXY_HOST);
            System.clearProperty(PROXY_PORT);
            System.clearProperty(PROXY_USER);
            System.clearProperty(PROXY_PASS);
        }
    }
}