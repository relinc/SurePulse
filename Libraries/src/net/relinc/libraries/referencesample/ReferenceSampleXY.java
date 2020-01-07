package net.relinc.libraries.referencesample;

import java.util.List;

public class ReferenceSampleXY extends ReferenceSample {

    public List<Double> engineeringStressPa;
    public List<Double> engineeringStrain;

    public ReferenceSampleXY(String name, List<Double> stressPa, List<Double> strain) {
        super(name);
        this.engineeringStressPa = stressPa;
        this.engineeringStrain = strain;
    }

    @Override
    public String getJson() {
        return null;
    }

    @Override
    public ReferenceSample fromJson(String json) {
        return null;
    }
}
