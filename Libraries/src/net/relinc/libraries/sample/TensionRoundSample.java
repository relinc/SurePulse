package net.relinc.libraries.sample;

import net.relinc.libraries.application.JsonReader;
import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import org.json.simple.JSONObject;

import java.util.stream.IntStream;

public class TensionRoundSample extends HopkinsonBarSample {

	private double diameter;

	public TensionRoundSample() {
		super();
	}
	
	public void setHoppySpecificParameters(String des, String val) {
		if(des.equals("Diameter"))
			setDiameter(Double.parseDouble(val));
	}

	@Override
	public void setHoppySpecificParametersJSON(JSONObject jsonObject) {
		new JsonReader(jsonObject).get("Diameter").ifPresent(ob -> this.setDiameter((Double)ob));
	}
	
	@Override
	public JSONObject getHoppySpecificJSON() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("Diameter", getDiameter());
		return jsonObject;
	}
	
	public double getDiameter() {
		return diameter;
	}

	public void setDiameter(double diameter) {
		this.diameter = diameter;
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
		return Math.pow((getDiameter() / 2),2) * Math.PI;
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
			des += "Diameter: " + SPOperations.round(Converter.mmFromM(diameter), 3) + " mm\n";
		}
		else{
			des += "Length: " + SPOperations.round(Converter.InchFromMeter(length), 3) + " in\n";
			des += "Diameter: " + SPOperations.round(Converter.InchFromMeter(diameter), 3) + " in\n";
		}
		return des + getCommonParametersForPopover(metric);
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
		return this.length + displacement;
	}
	
	@Override
	public String getFileExtension() {
		return getSampleConstants().getExtension();
	}

	public static SampleConstants getSampleConstants() {
		return new SampleConstants(
				"Tension Round",
				"Tension Round Sample", 
				"/net/relinc/libraries/images/Tensile Round Sample.png", 
				".samtrnd"
				);
	}
}
