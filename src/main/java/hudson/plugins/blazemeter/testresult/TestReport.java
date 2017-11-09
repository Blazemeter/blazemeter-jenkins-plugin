package hudson.plugins.blazemeter.testresult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright 2017 BlazeMeter Inc.
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

public class TestReport {
    public double average;
    public int min;
    public int max;
    public double errorPercentage;
    public int hits;

    public TestReport(JSONObject json) throws JSONException {
        this.average = Math.round(json.getDouble("avg")*100)/100;
        this.min = json.getInt("min");
        this.max = json.getInt("max");
        this.errorPercentage = Math.round((json.getDouble("failed") / json.getDouble("hits") * 100)*100)/100;
        this.hits = json.getInt("hits");
    }

    @Override
    public String toString() {
        String hits = String.valueOf(this.hits);
        String errorPercentage = String.valueOf(this.errorPercentage);
        String average = String.valueOf(this.average);
        String min = String.valueOf(this.min);
        String max = String.valueOf(this.max);
        return "AggregateTestResult ->" +
            " hits=" + hits +
            ", errors percentage=" + errorPercentage +
            ", average=" + average +
            ", min=" + min +
            ", max=" + max;
    }
}
