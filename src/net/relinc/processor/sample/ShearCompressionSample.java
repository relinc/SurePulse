package net.relinc.processor.sample;

import net.relinc.processor.data.Descriptor;
import net.relinc.processor.data.DescriptorDictionary;
import net.relinc.processor.staticClasses.Converter;
import net.relinc.processor.staticClasses.SPOperations;
import net.relinc.processor.staticClasses.SPSettings;

public class ShearCompressionSample extends Sample {

	private double gaugeHeight, gaugeWidth;
	
	public ShearCompressionSample() {
		//setSampleType("Shear Compression Sample");
	}
	

	
	@Override
	public void setSpecificParameters(String des, String val) {
		if(des.equals("Gauge Height"))
			setGaugeHeight((Double.parseDouble(val)));
		if(des.equals("Gauge Width"))
			setGaugeWidth(Double.parseDouble(val));
	}

	@Override
	public String getSpecificString() {
		String specificString = "";
		if(getGaugeHeight() > 0)
			specificString = "Gauge Height"+delimiter+getGaugeHeight()+SPSettings.lineSeperator;
		if(getGaugeWidth() > 0)
			specificString +="Gauge Width"+delimiter+getGaugeWidth()+SPSettings.lineSeperator;
		return specificString;
	}

	public double getGaugeHeight() {
		return gaugeHeight;
	}

	public void setGaugeHeight(double gaugeHeight) {
		this.gaugeHeight = gaugeHeight;
	}

	public double getGaugeWidth() {
		return gaugeWidth;
	}

	public void setGaugeWidth(double gaugeWidth) {
		this.gaugeWidth = gaugeWidth;
	}
	
	@Override
	public double[] getTrueStressFromEngStressAndEngStrain(double[] engStress, double[] engStrain) {
		//eng stress and strain must be equal length and time-matched. 
		double[] trueStress = new double[engStrain.length];
		for(int i = 0; i < trueStress.length; i++){
			trueStress[i] = engStress[i] * (1 + engStrain[i]);
		}
		return trueStress;
	}

//	@Override
//	public double[] getEngineeringStressFromForce(double[] force) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	public double getInitialCrossSectionalArea(){
		return gaugeHeight * gaugeWidth; //TODO: This is incorrect
	}
	
	@Override
	public double[] getEngineeringStressFromForce(double[] force){
		double[] stressValues = new double[force.length];
		for(int i = 0; i < stressValues.length; i++){
			stressValues[i] = force[i] / getInitialCrossSectionalArea(); //method is above
		}
		return stressValues;
	}



	@Override
	public DescriptorDictionary createAllParametersDecriptorDictionary() {
		DescriptorDictionary d = descriptorDictionary;
		int lastIndex = addCommonRequiredSampleParametersToDescriptionDictionary(d);
		
		double length = Converter.InchFromMeter(getLength());
		double gaugeHeight = Converter.InchFromMeter(getGaugeHeight());
		double gaugeWidth = Converter.InchFromMeter(getGaugeWidth());
		
		if(SPSettings.metricMode.get()){
			length = Converter.mmFromM(getLength());
			gaugeHeight = Converter.mmFromM(getGaugeHeight());
			gaugeWidth = Converter.mmFromM(getGaugeWidth());
		}
		
		d.descriptors.add(lastIndex++, new Descriptor("Length", Double.toString(SPOperations.round(length, 3))));
		d.descriptors.add(lastIndex++, new Descriptor("Gauge Height", Double.toString(SPOperations.round(gaugeHeight, 3))));
		d.descriptors.add(lastIndex++, new Descriptor("Gauge Width", Double.toString(SPOperations.round(gaugeWidth, 3))));
		return d;
	}

}
