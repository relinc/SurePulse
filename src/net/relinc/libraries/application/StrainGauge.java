package net.relinc.libraries.application;

import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;


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
	public StrainGauge(String path){
		String file = SPOperations.readStringFromFile(path);
		setParametersFromString(file);
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
		String contents = "SPFX StrainGauge Verison:1" + SPSettings.lineSeperator;
		contents += genericNameDescrip + ":" + genericName + SPSettings.lineSeperator;
		contents += gaugeFactorDescrip + ":" + Double.toString(gaugeFactor) + SPSettings.lineSeperator;
		contents += resistanceDescrip + ":" + Double.toString(resistance) + SPSettings.lineSeperator;
		contents += shuntResistanceDescrip + ":" + Double.toString(shuntResistance) + SPSettings.lineSeperator;
		contents += lengthDescrip + ":" + Double.toString(length) + SPSettings.lineSeperator;
		contents += voltageCalibatedDescrip + ":" + Double.toString(voltageCalibrated) + SPSettings.lineSeperator;
		
		return contents;
	}
	
	
}
