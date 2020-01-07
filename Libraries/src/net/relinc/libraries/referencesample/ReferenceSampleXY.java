package net.relinc.libraries.referencesample;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReferenceSampleXY extends ReferenceSample {

    private StressStrain stressStrain;

    public ReferenceSampleXY(String name, StressStrain stressStrain) {
        super(name);
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

    public static ReferenceSample fromJson(JSONObject object) {

        try {

            if (!object.get("type").equals("xy")) {
                return null;
            }

            JSONArray stressArr = (JSONArray) object.get("stress");
            JSONArray strainArr = (JSONArray) object.get("strain");


            System.out.println(stressArr.get(0));

            List<Double> stress = IntStream.range(0, stressArr.size()).mapToDouble(idx -> (Double) stressArr.get(idx)).boxed().collect(Collectors.toList());
            List<Double> strain = IntStream.range(0, strainArr.size()).mapToDouble(idx -> (Double) strainArr.get(idx)).boxed().collect(Collectors.toList());

            return new ReferenceSampleXY((String) object.get("name"), new StressStrain(stress, strain, StressStrainMode.ENGINEERING));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
