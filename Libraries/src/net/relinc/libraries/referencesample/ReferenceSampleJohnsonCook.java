package net.relinc.libraries.referencesample;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mark on 3/31/20.
 */
public class ReferenceSampleJohnsonCook extends ReferenceSample {

    private Double materialYoungsModulus;
    private Double referenceYieldStress;
    Double strainRate;


    Double referenceStrainRate;
    Double roomTemperature;
    Double meltingTemperature;
    Double sampleTemperature;
    Double yieldStress;
    Double intensityCoefficient;
    Double strainRateCoefficient;
    Double strainHardeningCoefficient;
    Double thermalSofteningCoefficient;


    public ReferenceSampleJohnsonCook(
            String name,
            String loadedPath,
            Double materialYoungsModulus,
            Double referenceYieldStress,
            Double strainRate,

            Double referenceStrainRate,
            Double roomTemperature,
            Double meltingTemperature,
            Double sampleTemperature,
            Double yieldStress,
            Double intensityCoefficient,
            Double strainRateCoefficient,
            Double strainHardeningCoefficient,
            Double thermalSofteningCoefficient

    ) {
        super(name, loadedPath);
        // this.stressStrain = stressStrain;

        this.materialYoungsModulus = materialYoungsModulus;
        this.referenceYieldStress = referenceYieldStress;
        this.referenceStrainRate = referenceStrainRate;
        this.strainRate = strainRate;
        this.roomTemperature = roomTemperature;
        this.meltingTemperature = meltingTemperature;
        this.sampleTemperature = sampleTemperature;
        this.yieldStress = yieldStress;
        this.intensityCoefficient = intensityCoefficient;
        this.strainRateCoefficient = strainRateCoefficient;
        this.strainHardeningCoefficient = strainHardeningCoefficient;
        this.thermalSofteningCoefficient = thermalSofteningCoefficient;
    }

    @Override
    public String getJson() {
        return null;
    }

    @Override
    public List<Double> getStress(StressStrainMode mode, StressUnit unit) {
        return getStrain(StressStrainMode.TRUE).stream().map(s -> getStressAtStrain(s)).collect(Collectors.toList());
    }

    @Override
    public List<Double> getStrain(StressStrainMode mode) {
        return IntStream.range(0, 1001).mapToDouble(i -> i / 5000.0).boxed().collect(Collectors.toList()); // 0 .. .2
    }

    private Double getStressAtStrain(Double strain) {
        Double yieldStress = this.referenceYieldStress;
        if (strain < yieldStress / this.materialYoungsModulus) {
            return strain * this.materialYoungsModulus;
        } else {
            Double plasticStrain = strain - yieldStress / this.materialYoungsModulus;
            return  (yieldStress + this.intensityCoefficient * Math.pow(plasticStrain, this.strainHardeningCoefficient)) *
                            (1 + this.strainRateCoefficient * Math.log(this.strainRate / this.referenceStrainRate)) *
                            (1 - Math.pow((this.sampleTemperature - this.roomTemperature) / (this.meltingTemperature - this.roomTemperature), this.thermalSofteningCoefficient));

        }
    }
}
