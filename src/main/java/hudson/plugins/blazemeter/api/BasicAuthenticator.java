/**
 Copyright 2017 BlazeMeter Inc.

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

import hudson.plugins.blazemeter.BlazemeterCredentialImpl;
import java.io.IOException;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class BasicAuthenticator implements Authenticator {
    private BlazemeterCredentialImpl c;
    public BasicAuthenticator(BlazemeterCredentialImpl c) {
        this.c = c;
    }

    @Override
    public Request authenticate(final Route route, final Response response) throws IOException {
        String credential = Credentials.basic(this.c.getUsername(), this.c.getPassword().getPlainText());
        if (credential.equals(response.request().header("Authorization"))) {
            return null;
        }
        return response.request().newBuilder()
            .header("Authorization", credential)
            .build();
    }
}

