package hudson.plugins.blazemeter.aggregatetestresult;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;

/**
 * BlazeMeter
 * User: moshe
 * Date: 5/28/12
 * Time: 12:51 PM
 */
public class AggregateTestResultV2Impl extends AggregateTestResult{

    AggregateTestResultV2Impl(String json) throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        TypeReference<AggregateTestResultV2Impl> typeRef
                = new TypeReference<AggregateTestResultV2Impl>() {
        };
        mapper.readValue(json, typeRef);
    }

    @Override
    public String toString() {
        return "AggregateTestResult{" +
                "average=" + average +
                ", min=" + min +
                ", max=" + max +
                ", samples=" + samples +
                ", median=" + median +
                ", percentile90=" + percentile90 +
                ", percentile99=" + percentile99 +
                ", errorPercentage=" + errorPercentage +
                ", hits=" + hits +
                ", kbs=" + kbs +
                '}';
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
