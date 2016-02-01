package net.relinc.libraries.sample;

import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class ShearCompressionSample extends HopkinsonBarSample {

	private double gaugeHeight, gaugeWidth;
	
	public ShearCompressionSample() {
		
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
			trueStress[i] = engStress[i] * (1 - engStrain[i]);
		}
		return trueStress;
	}

	@Override
	public double getInitialCrossSectionalArea(){
		return gaugeHeight * gaugeWidth; //TODO: This is incorrect
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
	
	@Override 
	public double getHopkinsonBarTransmissionPulseSign(){
		return -1.0;
	}
	
	@Override 
	public double getHopkinsonBarReflectedPulseSign(){
		return 1.0;
	}

}
