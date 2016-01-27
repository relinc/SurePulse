package net.relinc.processor.sample;


import net.relinc.processor.data.Descriptor;
import net.relinc.processor.data.DescriptorDictionary;
import net.relinc.processor.staticClasses.Converter;
import net.relinc.processor.staticClasses.SPOperations;
import net.relinc.processor.staticClasses.SPSettings;


public class TensionRectangularSample extends HopkinsonBarSample {
	
	private double width;
	private double height;
	private String widthDescriptor = "Width";
	private String heightDescriptor = "Height";
	
	public TensionRectangularSample() {
		
	}
	
	
	public void setSpecificParameters(String des, String val) {
		if(des.equals(widthDescriptor))
			setWidth(Double.parseDouble(val));
		if(des.equals(heightDescriptor))
			setHeight(Double.parseDouble(val));
	}
	
	@Override
	public String getSpecificString() {
		String s = "";
		if(getWidth() > 0)
			s += widthDescriptor + delimiter + getWidth() + SPSettings.lineSeperator;
		if(getHeight() > 0)
			s += heightDescriptor + delimiter + getHeight() + SPSettings.lineSeperator;
		return s;
	}
	
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	
	public double getHeight(){
		return height;
	}
	public void setHeight(double h){
		height = h;
	}
	
	@Override
	public double[] getTrueStressFromEngStressAndEngStrain(double[] engStress, double[] engStrain)  {
		//eng stress and strain must be equal length and time-matched. 
		double[] trueStress = new double[engStress.length];
		for(int i = 0; i < trueStress.length; i++){
			trueStress[i] = engStress[i] * (1 + engStrain[i]);
		}
		return trueStress;
	}
	
	@Override
	public double getInitialCrossSectionalArea(){
		return width * height;
	}

	@Override
	public DescriptorDictionary createAllParametersDecriptorDictionary() {
		DescriptorDictionary d = descriptorDictionary;
		int lastIndex = addCommonRequiredSampleParametersToDescriptionDictionary(d);
		
		double length = Converter.InchFromMeter(getLength());
		double width = Converter.InchFromMeter(getWidth());
		double height = Converter.InchFromMeter(getHeight());
		
		if(SPSettings.metricMode.get()){
			length = Converter.mmFromM(getLength());
			width = Converter.mmFromM(getWidth());
			height = Converter.mmFromM(getHeight());
		}
		
		d.descriptors.add(lastIndex++, new Descriptor("Length", Double.toString(SPOperations.round(length, 3))));
		d.descriptors.add(lastIndex++, new Descriptor("Width", Double.toString(SPOperations.round(width, 3))));
		d.descriptors.add(lastIndex++, new Descriptor("Height", Double.toString(SPOperations.round(height, 3))));
		return d;
	}
	
	@Override 
	public double getHopkinsonBarTransmissionPulseSign(){
		return 1.0;
	}
	
	@Override 
	public double getHopkinsonBarReflectedPulseSign(){
		return -1.0;
	}


}
