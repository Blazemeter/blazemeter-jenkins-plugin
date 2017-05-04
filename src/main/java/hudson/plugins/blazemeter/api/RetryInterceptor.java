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

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.jetty.util.log.StdErrLog;

public class RetryInterceptor implements Interceptor {

    private StdErrLog bzmLog =null;
    public RetryInterceptor(StdErrLog bzmLog){
        this.bzmLog = bzmLog;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        int retry = 1;
        int maxRetries=3;
        while (retry(response) && retry < maxRetries+1) {
            try{
                Thread.sleep(1000*retry);
            }catch (InterruptedException e){
                throw new IOException("Retry bzmLog was interrupted on sleep at retry # "+retry);
            }
            response = chain.proceed(request);
            bzmLog.info("Child request: code = "+response.code()+" -> "+retry+" retry");
            retry++;
        }
        return response;
    }

    private boolean retry(Response response) {
        boolean retry = !(response.isSuccessful() || response.code() <= 406 || response.code()==500);
        return retry;
    }
}
