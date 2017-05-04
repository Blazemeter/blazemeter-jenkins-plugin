/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.utils.Constants;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpLogger implements HttpLoggingInterceptor.Logger {

    private Logger httpLog = Logger.getLogger(Constants.HTTP_LOG);
    private FileHandler http_lfh;

    public HttpLogger(String httpLog_f) throws IOException {
        http_lfh = new FileHandler(httpLog_f);
        http_lfh.setFormatter(new CutUserKeyFormatter());
        httpLog.addHandler(http_lfh);
        httpLog.setUseParentHandlers(false);
    }

    @Override
    public void log(String message) {
        httpLog.info(message);
    }

    public void close() {
        http_lfh.close();
    }
}
