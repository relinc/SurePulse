package net.relinc.libraries.application;

import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class StrainGaugeOnBar extends StrainGauge{

	public double distanceToSample;
	public String specificName;
	
	public String distanceToSampleDescrip = "Distance To Sample";
	public String specificNameDescrip = "Specific Name";
	public StrainGaugeOnBar(String filePath, double DistanceToSample, String specificname) {
		super(filePath);

		distanceToSample = DistanceToSample;
		specificName = specificname;
	}
	public StrainGaugeOnBar(StrainGauge sg, double DistanceToSample, String specificname){
		super(sg);

		distanceToSample = DistanceToSample;
		specificName = specificname;
	}
	
	public StrainGaugeOnBar(String filePath){
		super(filePath);
		String fileString = SPOperations.readStringFromFile(filePath);
		if(filePath.contains(".json")) {
			setParametersFromJSONStrainGaugeOnBar(fileString);
		} else {
			setParametersFromString(fileString);
		}
	}
	
	public String getNameForFile(){
		return genericName + "-" + specificName;
	}
	
	@Override
	public String stringForFile(){

		return getJSONObject().toString();

	}

	@Override
	public JSONObject getJSONObject() {
		JSONObject jsonObject=super.getJSONObject();
		jsonObject.put(distanceToSampleDescrip,distanceToSample);
		jsonObject.put(specificNameDescrip,specificName);
		return jsonObject;
	}

	public void setParametersFromJSONStrainGaugeOnBar(String file) {
		//super.setParametersFromJSONString(file);

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) jsonParser.parse(file);
		} catch (org.json.simple.parser.ParseException e) {
			//TODO throw exception
		}
		setVariableJSONStrainGaugeOnBar(jsonObject);
	}

	public void setVariableJSONStrainGaugeOnBar(JSONObject jsonObject) {
		Object temp_obj=jsonObject.get(distanceToSampleDescrip);

		if(temp_obj  != null ) {
			distanceToSample =(Double)temp_obj; //Double.parseDouble((String)temp_obj);
		}
		temp_obj=jsonObject.get(specificNameDescrip);
		if( temp_obj != null ) {
			specificName = (String)temp_obj;

		}
	}

	@Override
	public void setParametersFromString(String file){
		super.setParametersFromString(file);
		String[] lines = file.split(SPSettings.lineSeperator);
		for(String str : lines)
			setVariable(str);
	}
	
	public void setVariable(String str){
		if(str.split(":").length < 2)
			return;
		String des = str.split(":")[0];
		String val = str.split(":")[1];
		if(des.equals(distanceToSampleDescrip))
			distanceToSample = Double.parseDouble(val);
		else if(des.equals(specificNameDescrip))
			specificName = val;
	}
	
	public double getVoltageFactor()
    {
        return resistance / (voltageCalibrated * gaugeFactor * ((resistance + shuntResistance)));
    }
	
//	public double getVoltageFactor2(){ //this is the previous, it is the inverse.
//		return voltageCalibrated / (resistance/(gaugeFactor*(resistance+shuntResistance)));
//	}

	public double[] getStrain(double[] voltage) {
		double[] strain = new double[voltage.length];
		for(int i = 0; i < strain.length; i++){
			strain[i] = voltage[i] * getVoltageFactor();
		}
		return strain;
	}
	
}
