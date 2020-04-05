package net.relinc.libraries.referencesample;

import net.relinc.libraries.staticClasses.Converter;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReferenceSampleCowperSymonds extends ReferenceSample {

    private Double materialYoungsModulus;
    private Double referenceYieldStress;
    private Double strainRate;


    private Double yieldStress;
    private Double intensityCoefficient;
    private Double strainRateCoefficient;
    private Double strainHardeningCoefficient;
    private Double strainRateSensitivityCoefficient;


    public ReferenceSampleCowperSymonds(
            String name,
            String loadedPath,
            Double materialYoungsModulus,
            Double referenceYieldStress,
            Double strainRate,

            Double yieldStress,
            Double intensityCoefficient,
            Double strainRateCoefficient,
            Double strainHardeningCoefficient,
            Double strainRateSensitivityCoefficient
    ) {
        super(name, loadedPath);
        // this.stressStrain = stressStrain;
        this.materialYoungsModulus = materialYoungsModulus;
        this.referenceYieldStress = referenceYieldStress;
        this.strainRate = strainRate;

        this.yieldStress = yieldStress;
        this.intensityCoefficient = intensityCoefficient;
        this.strainRateCoefficient = strainRateCoefficient;
        this.strainHardeningCoefficient = strainHardeningCoefficient;
        this.strainRateSensitivityCoefficient = strainRateSensitivityCoefficient;
    }

    @Override
    public String getJson() {

        JSONObject rootObject = new JSONObject();

        rootObject.put("type", "cowperSymonds");
        rootObject.put("name", this.getName());

        rootObject.put("materialYoungsModulus", this.materialYoungsModulus);
        rootObject.put("referenceYieldStress", this.referenceYieldStress);
        rootObject.put("strainRate", this.strainRate);

        rootObject.put("yieldStress", this.yieldStress);
        rootObject.put("intensityCoefficient", this.intensityCoefficient);
        rootObject.put("strainRateCoefficient", this.strainRateCoefficient);
        rootObject.put("strainHardeningCoefficient", this.strainHardeningCoefficient);
        rootObject.put("strainRateSensitivityCoefficient", this.strainRateSensitivityCoefficient);

        return rootObject.toJSONString();
    }


    private Double getStressAtStrain(Double strain) {
        Double yieldStress = this.referenceYieldStress * (1 + Math.pow(this.strainRate / this.strainRateCoefficient, 1 / this.strainRateSensitivityCoefficient));

        if (strain < yieldStress / this.materialYoungsModulus) {
            return strain * this.materialYoungsModulus;
        } else {
            Double plasticStrain = strain - this.referenceYieldStress / this.materialYoungsModulus;

            // σ_y+csp.B*ϵ_plastic^csp.p*(1+(∂ϵ_∂t/csp.C)^(1/csp.P))
            return yieldStress +
                    this.intensityCoefficient * Math.pow(plasticStrain, this.strainHardeningCoefficient) *
                            (1 + Math.pow(this.strainRate / this.strainRateCoefficient, 1 / this.strainRateSensitivityCoefficient));
            // return this.referenceYieldStress + this.intensityCoefficient * Math.pow(plasticStrain, this.strainHardeningCoefficient);
        }
    }

    @Override
    public List<Double> getStress(StressStrainMode mode, StressUnit unit) {

        if (unit == StressUnit.MPA) {
            return getStrain(mode).stream().map(s -> getStressAtStrain(s)).map(s -> Converter.MpaFromPa(s)).collect(Collectors.toList());
        } else if (unit == StressUnit.KSI) {
            return getStrain(mode).stream().map(s -> getStressAtStrain(s)).map(s -> Converter.ksiFromPa(s)).collect(Collectors.toList());
        } else if (unit == StressUnit.PA) {
            return getStrain(mode).stream().map(s -> getStressAtStrain(s)).collect(Collectors.toList());
        } else {
            throw new RuntimeException("getStress not implemented for this unit: " + mode.toString());
        }

    }

    @Override
    public List<Double> getStrain(StressStrainMode mode) {
        return IntStream.range(0, 1001).mapToDouble(i -> i / 5000.0).boxed().collect(Collectors.toList()); // 0 .. .2
    }

    public static ReferenceSample fromJson(JSONObject object, String loadedPath) {
        try {

            if (!object.get("type").equals("cowperSymonds")) {
                return null;
            }

            Double youngsMod = (Double) object.get("materialYoungsModulus");
            Double referenceYieldStress = (Double) object.get("referenceYieldStress");
            Double strainRate = (Double) object.get("strainRate");


            Double yieldStress = (Double) object.get("yieldStress");
            Double intensityCoefficient = (Double) object.get("intensityCoefficient");
            Double strainRateCoefficient = (Double) object.get("strainRateCoefficient");
            Double strainHardeningCoefficient = (Double) object.get("strainHardeningCoefficient");
            Double strainRateSensitivityCoefficient = (Double) object.get("strainRateSensitivityCoefficient");

            return new ReferenceSampleCowperSymonds(
                    (String) object.get("name"),
                    loadedPath,

                    youngsMod,
                    referenceYieldStress,
                    strainRate,

                    yieldStress,
                    intensityCoefficient,
                    strainRateCoefficient,
                    strainHardeningCoefficient,
                    strainRateSensitivityCoefficient
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
