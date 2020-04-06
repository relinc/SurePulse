package net.relinc.libraries.referencesample;

import net.relinc.libraries.staticClasses.Converter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReferenceSampleKN extends ReferenceSample {

    private Double K;
    private Double N;
    private Double materialYoungsModulus;
    private Double referenceYieldStress;

    private StressStrain stressStrain;

    public ReferenceSampleKN(String name, String loadedPath, Double K, Double N, Double materialYoungsModulus, Double referenceYieldStress) {
        super(name, loadedPath);
        // this.stressStrain = stressStrain;
        this.K = K;
        this.N = N;
        this.materialYoungsModulus = materialYoungsModulus;
        this.referenceYieldStress = referenceYieldStress;

        this.stressStrain = new StressStrain(
                this.computeTrueStress(),
                this.computeTrueStrain(),
                StressStrainMode.TRUE
        );
    }

    @Override
    public String getJson() {

        JSONObject rootObject = new JSONObject();

        rootObject.put("type", "kn");
        rootObject.put("name", this.getName());

        rootObject.put("K", this.K);
        rootObject.put("N", this.N);
        rootObject.put("materialYoungsModulus", this.materialYoungsModulus);
        rootObject.put("referenceYieldStress", this.referenceYieldStress);

        return rootObject.toJSONString();
    }


    private Double getStressAtStrain(Double strain) {
        if(strain < this.referenceYieldStress / this.materialYoungsModulus) {
            return strain * this.materialYoungsModulus;
        } else {
            Double plasticStrain = strain - this.referenceYieldStress / this.materialYoungsModulus;
            return this.K * Math.pow(plasticStrain, this.N);
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

    // Gets True Strain
    public List<Double> computeTrueStrain() {
        return IntStream.range(0, 1001).mapToDouble(i -> i / 5000.0).boxed().collect(Collectors.toList()); // 0 .. .2
    }

    @Override
    public List<Double> getStrain(StressStrainMode mode) {
        return mode == StressStrainMode.TRUE ? StressStrain.toTrue(this.stressStrain).getStrain() : StressStrain.toEngineering(this.stressStrain).getStrain();
    }

    public static ReferenceSample fromJson(JSONObject object, String loadedPath) {
        try {

            if (!object.get("type").equals("kn")) {
                return null;
            }

            Double youngsMod = (Double) object.get("materialYoungsModulus");
            Double yieldStress = (Double) object.get("referenceYieldStress");
            Double K = (Double) object.get("K");
            Double N = (Double) object.get("N");

            return new ReferenceSampleKN((String) object.get("name"), loadedPath, K, N, youngsMod, yieldStress);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
