package net.relinc.libraries.sample;


import net.relinc.libraries.application.JsonReader;
import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import org.json.simple.JSONObject;

import java.util.stream.IntStream;


public class TensionRectangularSample extends HopkinsonBarSample {
	
	private double width;
	private double height;
	private String widthDescriptor = "Width";
	private String heightDescriptor = "Height";
	
	public TensionRectangularSample() {
		super();
	}
	
	
	public void setHoppySpecificParameters(String des, String val) {
		if(des.equals(widthDescriptor))
			setWidth(Double.parseDouble(val));
		if(des.equals(heightDescriptor))
			setHeight(Double.parseDouble(val));
	}

	@Override
	public void setHoppySpecificParametersJSON(JSONObject jsonObject) {
		JsonReader json = new JsonReader(jsonObject);
		json.get(widthDescriptor).ifPresent(ob -> this.setWidth((Double)ob));
		json.get(heightDescriptor).ifPresent(ob -> this.setHeight((Double)ob));
	}

	@Override
	public JSONObject getHoppySpecificJSON() {
		JSONObject jsonObject = new JSONObject();
		if( getWidth() > 0 ) {
			jsonObject.put(widthDescriptor, getWidth());
		}
		if( getHeight() > 0 ) {
			jsonObject.put(heightDescriptor, getHeight());
		}

		return jsonObject;
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
	public double[] getLoadFromTrueStressAndDisplacement(double[] trueStress, double[] displacement) {
		// double[] load = new double[trueStress.length];
		return IntStream.range(0, trueStress.length).mapToDouble(idx -> {
			double engStrain = displacement[idx] / length;
			double engStress = trueStress[idx] / (1 + engStrain);
			return engStress * getInitialCrossSectionalArea();
		}).toArray();
	}

	@Override
	public double getInitialCrossSectionalArea(){
		return width * height;
	}
	
	@Override 
	public double getHopkinsonBarTransmissionPulseSign(){
		return 1.0;
	}
	
	@Override 
	public double getHopkinsonBarReflectedPulseSign(){
		return -1.0;
	}


	@Override
	public String getParametersForPopover(boolean metric) {
		String des = "";
		if(metric){
			des += "Length: " + SPOperations.round(Converter.mmFromM(length), 3) + " mm\n";
			des += "Width: " + SPOperations.round(Converter.mmFromM(width), 3) + " mm\n";
			des += "Height: " + SPOperations.round(Converter.mmFromM(height), 3) + " mm\n";
		}
		else{
			des += "Length: " + SPOperations.round(Converter.InchFromMeter(length), 3) + " in\n";
			des += "Width: " + SPOperations.round(Converter.InchFromMeter(width), 3) + " in\n";
			des += "Height: " + SPOperations.round(Converter.InchFromMeter(height), 3) + " in\n";
		}
		return des + getCommonParametersForPopover(metric);
	}


	@Override
	public int addHoppySpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i) {
		
		double width = Converter.InchFromMeter(getWidth());
		double height = Converter.InchFromMeter(getHeight());
		
		if(SPSettings.metricMode.get()){
			width = Converter.mmFromM(getWidth());
			height = Converter.mmFromM(getHeight());
		}
		
		d.descriptors.add(i++, new Descriptor("Width", Double.toString(SPOperations.round(width, 3))));
		d.descriptors.add(i++, new Descriptor("Height", Double.toString(SPOperations.round(height, 3))));
		return i;
	}

	@Override
	public double getCurrentSampleLength(double displacement)
	{
		return this.length + displacement;
	}

	@Override
	public String getFileExtension() {
		return getSampleConstants().getExtension();
	}

	public static SampleConstants getSampleConstants() {
		return new SampleConstants(
				"Tension Rectangular",
				"Tension Rectangular Sample", 
				"/net/relinc/libraries/images/Tensile Icon.png",
				".samtrec"
				);
	}
}
