package net.relinc.libraries.application;

import java.util.ArrayList;

//import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.relinc.libraries.staticClasses.SPSettings;
import org.json.simple.parser.JSONParser;


public class Bar {
	
	public ArrayList<StrainGaugeOnBar> strainGauges = new ArrayList<StrainGaugeOnBar>();

	public String name;
	public double length;
	public double density;
	public double youngsModulus;
	public double diameter;
	public double speedLimit;
	public double yield;
	private double poissonsRatio;
	
	public String nameDescrip = "Name";
	public String lengthDescrip = "Length";
	public String densityDescrip = "Density";
	public String youngsModulusDescrip = "Young's Modulus";
	public String diameterDescrip = "Diameter";
	public String speedLimitDescrip = "Speed Limit";
	public String yieldDescrip = "Yield";
	public String poissonsRatioDescrip = "Poisson's Ratio";
	
	public String splitter = ":";
	
	
	public String stringForFile() {

		JSONObject jsonObject = new JSONObject();

		jsonObject.put( nameDescrip, name );
		jsonObject.put( lengthDescrip, length);
		jsonObject.put( densityDescrip, density);
		jsonObject.put( youngsModulusDescrip, youngsModulus);
		jsonObject.put( diameterDescrip, diameter);
		jsonObject.put( speedLimitDescrip, speedLimit);
		jsonObject.put( yieldDescrip, yield);
		jsonObject.put( poissonsRatioDescrip, poissonsRatio);
		jsonObject.put("version",1);
		jsonObject.put("description","SUREPulse Single Bar Setup Version");


		String contents = jsonObject.toString();
		return contents;


	}
	
	public void setParametersFromString(String input){
		for(String line : input.split(SPSettings.lineSeperator)){
			setParameter(line);
		}
	}

	private void setParameter(String line) {
		if(line.split(splitter).length < 2)
			return;
		String des = line.split(splitter)[0];
		String val = line.split(splitter)[1];
		if(des.equals(nameDescrip))
			name = val;
		if(des.equals(lengthDescrip))
			length = Double.parseDouble(val);
		if(des.equals(densityDescrip))
			density = Double.parseDouble(val);
		if(des.equals(youngsModulusDescrip))
			youngsModulus = Double.parseDouble(val);
		if(des.equals(diameterDescrip))
			diameter = Double.parseDouble(val);
		if(des.equals(speedLimitDescrip))
			speedLimit = Double.parseDouble(val);
		if(des.equals(yieldDescrip))
			yield = Double.parseDouble(val);
		if(des.equals(poissonsRatioDescrip))
			poissonsRatio = Double.parseDouble(val);
		
	}
	
	public double getRadius() {
		return this.diameter / 2;
	}

	public void parseJSONtoParameters(String input) {
		JSONObject jsonObject = null;
		JSONParser jsonParser = new JSONParser();

		try {
			jsonObject = (JSONObject) jsonParser.parse(input);
		} catch (org.json.simple.parser.ParseException e) {
			//TODO throw e
		}

		setParametersJSON(jsonObject);
	}

	private void setParametersJSON(JSONObject jsonObject) {
		name = (String)jsonObject.get(nameDescrip);
		length = (Double)jsonObject.get(lengthDescrip);
		density = (Double)jsonObject.get(densityDescrip);
		youngsModulus = (Double)jsonObject.get(youngsModulusDescrip);
		diameter = (Double)jsonObject.get(diameterDescrip);
		speedLimit = (Double)jsonObject.get(speedLimitDescrip);
		yield = (Double)jsonObject.get(yieldDescrip);
		poissonsRatio = (Double)jsonObject.get(poissonsRatioDescrip);
	}

	public double getArea() {
		return Math.PI * Math.pow(diameter / 2,2);
	}
	
	public double getWaveSpeed(){
		return Math.pow(youngsModulus / density, .5);
	}

	public StrainGaugeOnBar getStrainGauge(String strainGaugeName) {
		for(StrainGaugeOnBar sgOnBar : strainGauges){
			if(sgOnBar.getNameForFile().equals(strainGaugeName))
				return sgOnBar;
		}
		return null;
	}

	public boolean removeStrainGauge(String specificname) {
		StrainGaugeOnBar sgToRemove = null;
		for(StrainGaugeOnBar sg : strainGauges){
			if(sg.specificName.equals(specificname)){
				sgToRemove = sg;
				break;
			}
		}
		if(sgToRemove != null){
			strainGauges.remove(sgToRemove);
			return true;
		}
		return false;
	}

	public double calculateSpeedLimit() {
		return Math.pow(Math.pow(yield / youngsModulus, 2) * 4 * getEnergyMultiplier() / (density * Math.pow(diameter / 2, 2) * Math.PI * getWaveSpeed()), .5);
	}
	
	public double getEnergyMultiplier()
    {
        return (.5 * Math.pow(diameter / 2, 2) * Math.PI * getWaveSpeed() * youngsModulus +
            .5 * density * Math.pow(diameter / 2, 2) * Math.PI * Math.pow(youngsModulus / density, 1.5));
    }

	public double getExpectedPulse(StrikerBar strikerBar, StrainGaugeOnBar sg) {
		double impliedTime = 2 * strikerBar.getLength() / getWaveSpeed();
		return Math.pow(strikerBar.getEnergy() / (getEnergyMultiplier() * impliedTime), .5) / sg.getVoltageFactor();
	}
	
	public double getPolarMomentOfIntertia() {
		return Math.PI * Math.pow(this.getRadius(), 4) / 2.0;
	}
	
	public double getShearModulus()
	{
		return this.youngsModulus / 2.0 / (1 + this.getPoissonsRatio());
	}
	
	public double getShearWaveSpeed() {
		return Math.pow(this.getShearModulus() / this.density, .5);
	}

	public double getPoissonsRatio() {
		return poissonsRatio;
	}

	public void setPoissonsRatio(double poissonsRatio) {
		this.poissonsRatio = poissonsRatio;
	}

}
