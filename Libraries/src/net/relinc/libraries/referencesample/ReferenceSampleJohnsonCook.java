package net.relinc.libraries.referencesample;

import net.relinc.libraries.staticClasses.Converter;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mark on 3/31/20.
 */
public class ReferenceSampleJohnsonCook extends ReferenceSample {

    private Double materialYoungsModulus;
    private Double referenceYieldStress;
    private Double strainRate;


    private Double referenceStrainRate;
    private Double roomTemperature;
    private Double meltingTemperature;
    private Double sampleTemperature;
    private Double yieldStress;
    private Double intensityCoefficient;
    private Double strainRateCoefficient;
    private Double strainHardeningCoefficient;
    private Double thermalSofteningCoefficient;

    private StressStrain stressStrain;


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
        this.strainRate = strainRate;

        this.referenceStrainRate = referenceStrainRate;
        this.roomTemperature = roomTemperature;
        this.meltingTemperature = meltingTemperature;
        this.sampleTemperature = sampleTemperature;
        this.yieldStress = yieldStress;
        this.intensityCoefficient = intensityCoefficient;
        this.strainRateCoefficient = strainRateCoefficient;
        this.strainHardeningCoefficient = strainHardeningCoefficient;
        this.thermalSofteningCoefficient = thermalSofteningCoefficient;

        this.stressStrain = new StressStrain(
                this.computeTrueStress(),
                this.computeTrueStrain(),
                StressStrainMode.TRUE
        );
    }

    @Override
    public String getJson() {

        JSONObject rootObject = new JSONObject();

        rootObject.put("type", "johnsonCook");
        rootObject.put("name", this.getName());

        rootObject.put("materialYoungsModulus", this.materialYoungsModulus);
        rootObject.put("referenceYieldStress", this.referenceYieldStress);
        rootObject.put("strainRate", this.strainRate);

        rootObject.put("referenceStrainRate", this.referenceStrainRate);
        rootObject.put("roomTemperature", this.roomTemperature);
        rootObject.put("meltingTemperature", this.meltingTemperature);
        rootObject.put("sampleTemperature", this.sampleTemperature);
        rootObject.put("yieldStress", this.yieldStress);
        rootObject.put("intensityCoefficient", this.intensityCoefficient);
        rootObject.put("strainRateCoefficient", this.strainRateCoefficient);
        rootObject.put("strainHardeningCoefficient", this.strainHardeningCoefficient);
        rootObject.put("thermalSofteningCoefficient", this.thermalSofteningCoefficient);

        return rootObject.toJSONString();
    }

    public static ReferenceSample fromJson(JSONObject object, String loadedPath) {
        try {

            if (!object.get("type").equals("johnsonCook")) {
                return null;
            }

            Double materialYoungsModulus = (Double) object.get("materialYoungsModulus");
            Double referenceYieldStress = (Double) object.get("referenceYieldStress");
            Double strainRate = (Double) object.get("strainRate");

            Double referenceStrainRate = (Double) object.get("referenceStrainRate");
            Double roomTemperature = (Double) object.get("roomTemperature");
            Double meltingTemperature = (Double) object.get("meltingTemperature");
            Double sampleTemperature = (Double) object.get("sampleTemperature");
            Double yieldStress = (Double) object.get("yieldStress");
            Double intensityCoefficient = (Double) object.get("intensityCoefficient");
            Double strainRateCoefficient = (Double) object.get("strainRateCoefficient");
            Double strainHardeningCoefficient = (Double) object.get("strainHardeningCoefficient");
            Double thermalSofteningCoefficient = (Double) object.get("thermalSofteningCoefficient");

            return new ReferenceSampleJohnsonCook(
                    (String) object.get("name"),
                    loadedPath,

                    materialYoungsModulus,
                    referenceYieldStress,
                    strainRate,

                    referenceStrainRate,
                    roomTemperature,
                    meltingTemperature,
                    sampleTemperature,
                    yieldStress,
                    intensityCoefficient,
                    strainRateCoefficient,
                    strainHardeningCoefficient,
                    thermalSofteningCoefficient
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
