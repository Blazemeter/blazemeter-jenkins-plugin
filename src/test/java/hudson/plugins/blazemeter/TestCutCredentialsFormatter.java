/**
 * Copyright 2016 BlazeMeter Inc.
 *
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

import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.CutCredentialsFormatter;
import hudson.plugins.blazemeter.utils.Constants;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.Assert;
import org.junit.Test;

public class TestCutCredentialsFormatter {

    @Test
    public void format_true() {
        CutCredentialsFormatter cuk = new CutCredentialsFormatter();
        TestLogRecord lr = new TestLogRecord(Level.INFO,
            Api.AUTHORIZATION + ": 12345678901234566567sssssssssssssssssssssssssssssssssssssssssssssssssss");
        String formatted = cuk.format(lr);
        int dots = formatted.length() - 14;
        Assert.assertEquals(cuk.format(lr).indexOf(Constants.SPACE_THREE_DOTS), dots);
    }

    @Test
    public void format_false() {
        CutCredentialsFormatter cuk = new CutCredentialsFormatter();
        TestLogRecord lr = new TestLogRecord(Level.INFO,"");
        Assert.assertFalse(cuk.format(lr).contains(Constants.SPACE_THREE_DOTS));
    }

    private class TestLogRecord extends LogRecord {
        public TestLogRecord(final Level level, final String msg) {
            super(level, msg);
        }
    }
}
