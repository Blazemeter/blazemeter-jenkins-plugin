package hudson.plugins.blazemeter.testresult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

public class TestResult {
    protected double std              ;
    protected double average          ;
    protected double min              ;
    protected double max              ;
    protected double samples          ;
    protected double median           ;
    protected double percentile90     ;
    protected double percentile99     ;
    protected double errorPercentage  ;
    protected double hits             ;
    protected double kbs              ;
    protected long   n                ;

    public TestResult(JSONObject json) throws IOException, JSONException {
        this.std = json.getDouble("std");
        this.average = json.getDouble("avg");
        this.min = json.getDouble("min");
        this.max = json.getDouble("max");

        // not implemented because such field is absent in JSON
//        this.samples = json.getDouble("samples");
        this.samples=-1;
        // not implemented because such field is absent in JSON
//        this.median = json.getDouble("median");
        this.median=-1;
        this.percentile90 = json.getDouble("tp90");

        // not implemented because such field is absent in JSON
//        this.errorPercentage = json.getDouble("errorPercentage");
        this.errorPercentage=json.getDouble("failed")/json.getDouble("hits")*100;
        this.hits = json.getDouble("hits");
        this.kbs = json.getDouble("bytes")/1024;

        // not implemented because such field is absent in JSON
//        this.n = json.getLong("n");
          this.n=-1;
    }

    @Override
    public String toString() {
        String hits=String.valueOf(this.hits);
        String errorPercentage=String.valueOf(this.errorPercentage);
        String average=String.valueOf(this.average);
        String min=String.valueOf(this.min);
        String max=String.valueOf(this.max);
        return "AggregateTestResult ->" +
               " hits=" + hits +
               ", errors percentage=" + errorPercentage +
               ", average=" + average +
               ", min=" + min +
               ", max=" + max;
    }

    public double getStd() {
        return std;
    }

    public void setStd(double std) {
        this.std = std;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getSamples() {
        return samples;
    }

    public void setSamples(double samples) {
        this.samples = samples;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getPercentile90() {
        return percentile90;
    }

    public void setPercentile90(double percentile90) {
        this.percentile90 = percentile90;
    }

    public double getPercentile99() {
        return percentile99;
    }

    public void setPercentile99(double percentile99) {
        this.percentile99 = percentile99;
    }

    public double getErrorPercentage() {
        return errorPercentage;
    }

    public void setErrorPercentage(double errorPercentage) {
        this.errorPercentage = errorPercentage;
    }

    public double getHits() {
        return hits;
    }

    public void setHits(double hits) {
        this.hits = hits;
    }

    public double getKbs() {
        return kbs;
    }

    public void setKbs(double kbs) {
        this.kbs = kbs;
    }

    public long getN() {
        return n;
    }

    public void setN(long n) {
        this.n = n;
    }

}
