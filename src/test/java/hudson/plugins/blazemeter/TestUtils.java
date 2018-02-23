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

import hudson.plugins.blazemeter.utils.Utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class TestUtils {


    @Test
    public void resolveId_positive() {
        String testId = "dddddddddd(1.d)";
        assertEquals("1.d", Utils.resolveTestId(testId));
    }

    @Test
    public void resolveId_not_changes() {
        String testId = "1.d";
        assertEquals("1.d", Utils.resolveTestId(testId));
    }

    @Test
    public void resolveId_negative() {
        String testId = "no-test-id";
        assertEquals("no-test-id", Utils.resolveTestId(testId));
    }

    @Test
    public void getTestId() {
        String testId = "1.d";
        assertEquals("1", Utils.getTestId(testId));
    }

    @Test
    public void version() {
        assertNotEquals("N/A", Utils.version());
    }
}
