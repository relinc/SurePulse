package net.relinc.libraries.referencesample;

import net.relinc.libraries.staticClasses.Converter;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReferenceSampleLudwig extends ReferenceSample {

    private Double materialYoungsModulus;
    private Double referenceYieldStress;



    private Double intensityCoefficient;
    private Double strainHardeningCoefficient;


    public ReferenceSampleLudwig(
            String name,
            String loadedPath,
            Double materialYoungsModulus,
            Double referenceYieldStress,
            Double intensityCoefficient,
            Double strainHardeningCoefficient
    ) {
        super(name, loadedPath);
        // this.stressStrain = stressStrain;
        this.materialYoungsModulus = materialYoungsModulus;
        this.referenceYieldStress = referenceYieldStress;

        this.intensityCoefficient = intensityCoefficient;
        this.strainHardeningCoefficient = strainHardeningCoefficient;
    }

    @Override
    public String getJson() {

        JSONObject rootObject = new JSONObject();

        rootObject.put("type", "ludwig");
        rootObject.put("name", this.getName());

        rootObject.put("materialYoungsModulus", this.materialYoungsModulus);
        rootObject.put("referenceYieldStress", this.referenceYieldStress);

        rootObject.put("intensityCoefficient", this.intensityCoefficient);
        rootObject.put("strainHardeningCoefficient", this.strainHardeningCoefficient);

        return rootObject.toJSONString();
    }


    private Double getStressAtStrain(Double strain) {
        if(strain < this.referenceYieldStress / this.materialYoungsModulus) {
            return strain * this.materialYoungsModulus;
        } else {
            Double plasticStrain = strain - this.referenceYieldStress / this.materialYoungsModulus;

            return this.referenceYieldStress + this.intensityCoefficient * Math.pow(plasticStrain, this.strainHardeningCoefficient);
        }
    }

    @Override
    public List<Double> getStress(StressStrainMode mode, StressUnit unit) {

        if(unit == StressUnit.MPA) {
            return getStrain(mode).stream().map(s -> getStressAtStrain(s)).map(s -> Converter.MpaFromPa(s)).collect(Collectors.toList());
        } else if(unit == StressUnit.KSI) {
            return getStrain(mode).stream().map(s -> getStressAtStrain(s)).map(s -> Converter.ksiFromPa(s)).collect(Collectors.toList());
        } else if(unit == StressUnit.PA) {
            return getStrain(mode).stream().map(s -> getStressAtStrain(s)).collect(Collectors.toList());
        }
        else {
            throw new RuntimeException("getStress not implemented for this unit: " + mode.toString());
        }

    }

    @Override
    public List<Double> getStrain(StressStrainMode mode) {
        return IntStream.range(0, 1001).mapToDouble(i -> i / 5000.0).boxed().collect(Collectors.toList()); // 0 .. .2
    }

    public static ReferenceSample fromJson(JSONObject object, String loadedPath) {
        try {

            if (!object.get("type").equals("ludwig")) {
                return null;
            }

            Double youngsMod = (Double) object.get("materialYoungsModulus");
            Double referenceYieldStress = (Double) object.get("referenceYieldStress");

            Double intensityCoefficient = (Double) object.get("intensityCoefficient");
            Double strainHardeningCoefficient = (Double) object.get("strainHardeningCoefficient");

            return new ReferenceSampleLudwig(
                    (String) object.get("name"),
                    loadedPath,

                    youngsMod,
                    referenceYieldStress,

                    intensityCoefficient,
                    strainHardeningCoefficient
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
