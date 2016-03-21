package net.relinc.libraries.sample;

import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.staticClasses.*;//Converter;
//import net.relinc.processor.staticClasses.SPOperations;
//import net.relinc.processor.staticClasses.SPSettings;

public class CompressionSample extends HopkinsonBarSample {

	private double diameter;
	
	public CompressionSample() {
		
	}
	
	public void setSpecificParameters(String des, String val) {
		if(des.equals("Diameter")) {
			setDiameter(Double.parseDouble(val));
		}
	}

	@Override
	public String getSpecificString() {
		return getDiameter() > 0 ? "Diameter" + delimiter + getDiameter() + SPSettings.lineSeperator : "";
	}
	
	public void setDiameter(double i) {
		diameter = i;
	}
	public double getDiameter(){
		return diameter;
	}

	@Override
	public double[] getTrueStressFromEngStressAndEngStrain(double[] engStress, double[] engStrain){
		//eng stress and strain must be equal length and time-matched. 
		double[] trueStress = new double[engStrain.length];
		for(int i = 0; i < trueStress.length; i++){
			trueStress[i] = engStress[i] * (1 - engStrain[i]); //+ because area is getting larger.
		}
		return trueStress;
	}
	
	@Override
	public double getInitialCrossSectionalArea(){
		return Math.pow(getDiameter() / 2,2) * Math.PI;
	}
	
	@Override 
	public double getHopkinsonBarTransmissionPulseSign(){
		return -1.0;
	}
	
	@Override 
	public double getHopkinsonBarReflectedPulseSign(){
		return 1.0;
	}

	//@Override
	public String getParametersForPopover(boolean metric) {
		String common = getCommonParametersForPopover(metric);
		String des = "";
		if(metric){
			des += "Length: " + SPOperations.round(Converter.mmFromM(length),3) + " mm\n";
			des += "Diameter: " + SPOperations.round(Converter.mmFromM(diameter),3) + " mm\n";
			des += common;
		}
		else{
			des += "Length: " + SPOperations.round(Converter.InchFromMeter(length),3) + " in\n";
			des += "Diameter: " + SPOperations.round(Converter.InchFromMeter(diameter),3) + " in\n";
			des += common;
		}
		return des;
	}

	@Override
	public int addSpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i) {
		
		double diameter = Converter.InchFromMeter(getDiameter());
		
		if(SPSettings.metricMode.get()){
			diameter = Converter.mmFromM(getDiameter());
		}
		
		d.descriptors.add(i++, new Descriptor("Diameter", Double.toString(SPOperations.round(diameter, 3))));
		return i;
	}
	
}
