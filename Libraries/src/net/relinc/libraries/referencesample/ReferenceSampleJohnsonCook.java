package net.relinc.libraries.referencesample;

import net.relinc.libraries.sample.Sample;

import java.util.List;

/**
 * Created by mark on 3/31/20.
 */
public class ReferenceSampleJohnsonCook extends ReferenceSample {


    public ReferenceSampleJohnsonCook(String name, String loadedPath, Double referenceStrainRate, Double roomTemperature) {
        super(name, loadedPath);
        // this.stressStrain = stressStrain;

    }

    @Override
    public String getJson() {
        return null;
    }

    @Override
    public List<Double> getStress(StressStrainMode mode, StressUnit unit) {
        return null;
    }

    @Override
    public List<Double> getStrain(StressStrainMode mode) {
        return null;
    }
}
