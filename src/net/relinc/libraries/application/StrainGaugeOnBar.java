package net.relinc.libraries.application;

import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

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
	
	public StrainGaugeOnBar(String filePath){
		super(filePath);
		String fileString = SPOperations.readStringFromFile(filePath);
		setParametersFromString(fileString);
	}
	
	public String getNameForFile(){
		return genericName + "-" + specificName;
	}
	
	@Override
	public String stringForFile(){
		String contents = super.stringForFile();
		contents += distanceToSampleDescrip + ":" + Double.toString(distanceToSample) + SPSettings.lineSeperator;
		contents += specificNameDescrip + ":" + specificName + SPSettings.lineSeperator;
		return contents;
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
        return resistance / (voltageCalibrated * gaugeFactor *((resistance + shuntResistance)));
    }

	public double[] getStrain(double[] voltage) {
		double[] strain = new double[voltage.length];
		for(int i = 0; i < strain.length; i++){
			strain[i] = voltage[i] * getVoltageFactor();
		}
		return strain;
	}
	
}
