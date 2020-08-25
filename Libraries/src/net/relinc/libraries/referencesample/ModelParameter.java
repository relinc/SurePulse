package net.relinc.libraries.referencesample;

public class ModelParameter {
    public Double value;
    public String metricLabel;
    public String englishLabel;

    public ModelParameter(Double value) {
        this.value = value;
    }

    public ModelParameter(Double value, String label) {
        this.value = value;
        this.metricLabel = label;
        this.englishLabel = label;
    }
}
