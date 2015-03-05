package hudson.plugins.blazemeter.testresult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * BlazeMeter
 * User: moshe
 * Date: 5/28/12
 * Time: 12:51 PM
 */
public class TestResultV3Impl extends TestResult {

    TestResultV3Impl(JSONObject json) throws IOException, JSONException {
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
        return "######### AggregateTestResult ####################" +
               "\nhits=" + hits +
               "\nerrors percentage=" + errorPercentage+
               "\naverage=" + average +
               "\naverage=" + average +
               "\nmin=" + min +
               "\nmax=" + max+"\n"+
               "#################################################";
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
