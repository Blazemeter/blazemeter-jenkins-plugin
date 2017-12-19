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
    public int tp90;
    public double avgthrpt;
    public double errorPercentage;
    public int hits;

    public TestReport(JSONObject json) throws JSONException {
        this.average = Math.round(json.getDouble("avg")*100.0)/100.0;
        this.min = json.getInt("min");
        this.max = json.getInt("max");
        this.tp90 = json.getInt("tp90");
        this.errorPercentage = Math.round((json.getDouble("failed") / json.getDouble("hits") * 100)*100)/100;
        this.hits = json.getInt("hits");
        this.avgthrpt = Math.round(this.hits/(json.getDouble("last")-json.getDouble("first"))*100.0)/100.0;
    }

    @Override
    public String toString() {
        String hits = Integer.toString(this.hits);
        String errorPercentage = Double.toString(this.errorPercentage);
        String average = Double.toString(this.average);
        String min = Integer.toString(this.min);
        String max = Integer.toString(this.max);
        return "AggregateTestResult ->" +
            " hits = " + hits +
            " hits, errors = " + errorPercentage +
            " %, average = " + average +
            " ms, min = " + min +
            " ms, max = " + max +
            " ms, average throughput = "+this.avgthrpt+
            " hits/s, 90% Response Time = "+this.tp90+" s.";
    }
}
