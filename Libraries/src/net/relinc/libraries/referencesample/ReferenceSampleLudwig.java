package net.relinc.libraries.referencesample;

import net.relinc.libraries.staticClasses.Converter;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReferenceSampleLudwig extends ReferenceSample {

    public Double materialYoungsModulus;
    public Double referenceYieldStress;



    public Double intensityCoefficient;
    public Double strainHardeningCoefficient;

    private StressStrain stressStrain;


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

        this.stressStrain = new StressStrain(
                this.computeTrueStress(),
                this.computeTrueStrain(),
                StressStrainMode.TRUE
        );
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

    private List<Double> computeTrueStress() {
        return computeTrueStrain().stream().map(s -> getStressAtStrain(s)).collect(Collectors.toList());
    }

    @Override
    public List<Double> getStress(StressStrainMode mode, StressUnit unit) {

        List<Double> converted = mode == StressStrainMode.TRUE ? StressStrain.toTrue(this.stressStrain).getStress() : StressStrain.toEngineering(this.stressStrain).getStress();

        if(unit == StressUnit.MPA) {
            return converted.stream().map(s -> Converter.MpaFromPa(s)).collect(Collectors.toList());
        } else if(unit == StressUnit.KSI) {
            return converted.stream().map(s -> Converter.ksiFromPa(s)).collect(Collectors.toList());
        } else if(unit == StressUnit.PA) {
            return converted.stream().collect(Collectors.toList());
        }
        else {
            throw new RuntimeException("getStress not implemented for this unit: " + mode.toString());
        }

    }

    private List<Double> computeTrueStrain() {
        return IntStream.range(0, 1001).mapToDouble(i -> i / 5000.0).boxed().collect(Collectors.toList()); // 0 .. .2
    }

    @Override
    public List<Double> getStrain(StressStrainMode mode) {
        return mode == StressStrainMode.TRUE ? StressStrain.toTrue(this.stressStrain).getStrain() : StressStrain.toEngineering(this.stressStrain).getStrain();
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
