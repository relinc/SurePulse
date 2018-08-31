package net.relinc.libraries.application;

import com.oracle.javafx.jmx.json.JSONException;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class StrainGauge {
	public String genericName;
	public double gaugeFactor;
	public double resistance;
	public double shuntResistance;
	public double length;
	public double voltageCalibrated;
	
	public String genericNameDescrip = "Generic Name";
	public String gaugeFactorDescrip = "Gauge Factor";
	public String resistanceDescrip = "Resistance";
	public String shuntResistanceDescrip = "Shunt Resistance";
	public String lengthDescrip = "Length";
	public String voltageCalibatedDescrip = "Voltage Calibrated";
	
	public StrainGauge(String genName, double GaugeFactor, double Resistance, double ShuntResistance, double Length, double VoltageCalibrated){
		genericName = genName;
		gaugeFactor = GaugeFactor;
		resistance = Resistance;
		shuntResistance = ShuntResistance;
		length = Length;
		voltageCalibrated = VoltageCalibrated;
	}
	public StrainGauge(String path)  {
		String file_contents = SPOperations.readStringFromFile(path);
		if(path.contains(".json")) {
			this.setParametersFromJSONString(file_contents);
		} else {
			setParametersFromString(file_contents);
		}
	}
	public StrainGauge(StrainGauge copy) {
		genericName = copy.genericName;
		gaugeFactor = copy.gaugeFactor;
		resistance = copy.resistance;
		shuntResistance = copy.shuntResistance;
		length = copy.length;
		voltageCalibrated = copy.voltageCalibrated;
	}
	public void setParametersFromJSONString(String file) {

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) jsonParser.parse(file);
		} catch(org.json.simple.parser.ParseException e) {
			//System.out.println("ERROR!");
		}
		setVariableJSON(jsonObject);
	}

	public void setVariableJSON(JSONObject jsonObject) {
		genericName = (String)jsonObject.get(genericNameDescrip);
		gaugeFactor = (Double)jsonObject.get(gaugeFactorDescrip);
		resistance = (Double)jsonObject.get(resistanceDescrip);
		shuntResistance = (Double)jsonObject.get(shuntResistanceDescrip);
		length = (Double) jsonObject.get(lengthDescrip);
		voltageCalibrated = (Double)jsonObject.get(voltageCalibatedDescrip);
	}

	public void setParametersFromString(String file){
		String[] lines = file.split(SPSettings.lineSeperator);
		for(String str : lines){
			setVariable(str);
		}
	}
	private void setVariable(String str) {
		
		if(str.split(":").length < 2)
			return;
		String description = str.split(":")[0];
		String value = str.split(":")[1];
		if(description.equals(genericNameDescrip))
			genericName = value;
		else if(description.equals(gaugeFactorDescrip))
			gaugeFactor = Double.parseDouble(value);
		else if(description.equals(resistanceDescrip))
			resistance = Double.parseDouble(value);
		else if(description.equals(shuntResistanceDescrip))
			shuntResistance = Double.parseDouble(value);
		else if(description.equals(lengthDescrip))
			length = Double.parseDouble(value);
		else if(description.equals(voltageCalibatedDescrip))
			voltageCalibrated = Double.parseDouble(value);
		//else 
			//System.out.println("Failed to set variable: " + description + " To: " + value);
	}

	
	public String stringForFile(){


		String contents = getJSONObject().toString();
		return contents;

	}
	public JSONObject getJSONObject(){
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("Version",1);
		jsonObject.put("Description", "SPFX StrainGauge");
		jsonObject.put( genericNameDescrip, genericName);
		jsonObject.put( gaugeFactorDescrip, gaugeFactor);
		jsonObject.put( resistanceDescrip, resistance);
		jsonObject.put( shuntResistanceDescrip, shuntResistance);
		jsonObject.put( lengthDescrip, length);
		jsonObject.put( voltageCalibatedDescrip, voltageCalibrated);
		return jsonObject;
	}
	
	
}
