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

package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.HttpLogger;
import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class TestHttpLogger {

    @Test
    public void constructor() {
        HttpLogger l=null;
        try {
            String lfn="logger";
            l = new HttpLogger(lfn);
            File lf=new File(lfn);
            if(lf.exists()){
                lf.delete();
            }
        } catch (IOException e) {
            Assert.fail();
        }finally {
            l.close();
        }
    }
}
