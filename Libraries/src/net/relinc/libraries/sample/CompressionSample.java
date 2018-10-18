package net.relinc.libraries.sample;

import net.relinc.libraries.application.JsonReader;
import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.staticClasses.*;//Converter;
import org.json.simple.JSONObject;

public class CompressionSample extends HopkinsonBarSample {

	private double diameter;
	
	
	public CompressionSample() {
		super();
	}
	
	public void setHoppySpecificParameters(String des, String val) {
		if(des.equals("Diameter")) {
			setDiameter(Double.parseDouble(val));
		}
	}

	@Override
	public void setHoppySpecificParametersJSON(JSONObject jsonObject) {
		new JsonReader(jsonObject).get("Diameter").ifPresent(ob -> this.setDiameter((Double)ob));
	}


	@Override
	public JSONObject getHoppySpecificJSON() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("Diameter", diameter);
		return jsonObject;
	}

	
	public void setDiameter(double i) {
		diameter = i;
	}
	public double getDiameter(){
		return diameter;
	}
	
	@Override // This method could be static. Doesn't use any parameters.
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
		double area = Math.pow(getDiameter() / 2,2) * Math.PI;
		return area;
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
		}
		else{
			des += "Length: " + SPOperations.round(Converter.InchFromMeter(length),3) + " in\n";
			des += "Diameter: " + SPOperations.round(Converter.InchFromMeter(diameter),3) + " in\n";
		}
		des += common;
		return des;
	}

	@Override
	public int addHoppySpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i) {
		
		double diameter = Converter.InchFromMeter(getDiameter());
		
		if(SPSettings.metricMode.get()){
			diameter = Converter.mmFromM(getDiameter());
		}
		
		d.descriptors.add(i++, new Descriptor("Diameter", Double.toString(SPOperations.round(diameter, 3))));
		return i;
	}
	
	@Override
	public double getCurrentSampleLength(double displacement)
	{
		return this.length - displacement;
	}
	
	@Override
	public String getFileExtension() {
		return getSampleConstants().getExtension();
	}
	
	public static SampleConstants getSampleConstants() {
		return new SampleConstants(
				"Compression",
				"Compression Sample",
				"/net/relinc/libraries/images/Steel Cylinder.jpg", 
				".samcomp"
				);
	}
}
