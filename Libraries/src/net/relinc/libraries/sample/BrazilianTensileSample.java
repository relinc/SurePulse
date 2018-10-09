package net.relinc.libraries.sample;

import net.relinc.libraries.application.JsonReader;
import net.relinc.libraries.data.DescriptorDictionary;
import org.json.simple.JSONObject;

public class BrazilianTensileSample extends Sample {
    private double diameter;
    private double length;

    public BrazilianTensileSample() {
        super();
    }

    @Override
    public JSONObject getSpecificJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Diameter", diameter);
        jsonObject.put("Length", length);
        return jsonObject;
    }

    @Override
    public void setSpecificParametersJSON(JSONObject jsonObject) {
        JsonReader json = new JsonReader(jsonObject);
        json.get("Diameter").ifPresent(ob -> this.setDiameter((Double)ob));
        json.get("Length").ifPresent(ob -> this.setLength((Double)ob));
    }

    @Override
    public void setSpecificParameters(String des, String val) {

    }

    @Override
    public int addSpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i) {
        return i;
    }

    @Override
    public String getParametersForPopover(boolean selected2) {
        return "";
    }

    @Override
    public String getFileExtension() {
        return getSampleConstants().getExtension();
    }

    public static SampleConstants getSampleConstants() {
        return new SampleConstants(
                "Brazilian Tensile",
                "Brazilian Tensile Sample",
                "/net/relinc/libraries/images/Steel Cylinder.jpg",
                ".sambrten"
        );
    }

    public double getDiameter() {
        return diameter;
    }

    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }


    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }
}
