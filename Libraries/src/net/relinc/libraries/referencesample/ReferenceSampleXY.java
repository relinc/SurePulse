package net.relinc.libraries.referencesample;

import net.relinc.libraries.staticClasses.Converter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReferenceSampleXY extends ReferenceSample {

    private StressStrain stressStrain;

    public ReferenceSampleXY(String name, StressStrain stressStrain, String loadedPath) {
        super(name, loadedPath);
        this.stressStrain = stressStrain;

    }

    @Override
    public String getJson() {
        StressStrain engStressStrain = StressStrain.toEngineering(this.stressStrain);

        JSONObject rootObject = new JSONObject();

        rootObject.put("type", "xy");
        rootObject.put("name", this.getName());

        JSONArray stressData = new JSONArray();
        engStressStrain.getStress().forEach(d -> {
            stressData.add(d);
        });

        rootObject.put("stress", stressData);

        JSONArray strainData = new JSONArray();
        engStressStrain.getStrain().forEach(d -> {
            strainData.add(d);
        });

        rootObject.put("strain", strainData);



        return rootObject.toJSONString();
    }


    private List<Double> getStress(StressStrainMode mode) {
        if(mode == StressStrainMode.ENGINEERING) {
            return StressStrain.toEngineering(this.stressStrain).getStress();
        } else if(mode == StressStrainMode.TRUE) {
            return StressStrain.toTrue(this.stressStrain).getStress();
        } else {
            throw new RuntimeException("getStress not implmemented for this mode!");
        }
    }

    @Override
    public List<Double> getStress(StressStrainMode mode, StressUnit unit) {

        List<Double> stress = getStress(mode);

        if(unit == StressUnit.MPA) {
            return stress.stream().map(s -> Converter.MpaFromPa(s)).collect(Collectors.toList());
        } else if(unit == StressUnit.KSI) {
            return stress.stream().map(s -> Converter.ksiFromPa(s)).collect(Collectors.toList());
        } else {
            throw new RuntimeException("getStress not implemented for this unit!");
        }

    }

    @Override
    public List<Double> getStrain(StressStrainMode mode) {
        if(mode == StressStrainMode.TRUE) {
            return StressStrain.toTrue(this.stressStrain).getStrain();
        } else if(mode == StressStrainMode.ENGINEERING) {
            return StressStrain.toEngineering(this.stressStrain).getStrain();
        } else {
            throw new RuntimeException("getStrain not implemented for this stressStrain mode.");
        }
    }

    public static ReferenceSample fromJson(JSONObject object, String loadedPath) {

        try {

            if (!object.get("type").equals("xy")) {
                return null;
            }

            JSONArray stressArr = (JSONArray) object.get("stress");
            JSONArray strainArr = (JSONArray) object.get("strain");

            List<Double> stress = IntStream.range(0, stressArr.size()).mapToDouble(idx -> (Double) stressArr.get(idx)).boxed().collect(Collectors.toList());
            List<Double> strain = IntStream.range(0, strainArr.size()).mapToDouble(idx -> (Double) strainArr.get(idx)).boxed().collect(Collectors.toList());

            return new ReferenceSampleXY((String) object.get("name"), new StressStrain(stress, strain, StressStrainMode.ENGINEERING), loadedPath);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
