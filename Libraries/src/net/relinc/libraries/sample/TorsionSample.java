package net.relinc.libraries.sample;

import java.util.Arrays;

import net.relinc.libraries.application.JsonReader;
import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import org.json.simple.JSONObject;

public class TorsionSample extends Sample {
	private double innerDiameter;
	private double outerDiameter;
	private double length;
	
	public TorsionSample()
	{
		super();
	}

	@Override
	public JSONObject getSpecificJSON(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("innerDiameter", this.getInnerDiameter());
		jsonObject.put("outerDiameter", this.getOuterDiameter());
		jsonObject.put("length", this.getLength());
		return jsonObject;
	}

	@Override
	public void setSpecificParametersJSON(JSONObject jsonObject) {
		JsonReader json = new JsonReader(jsonObject);
		json.get("innerDiameter").ifPresent(ob -> this.setInnerDiameter((Double)ob));
		json.get("outerDiameter").ifPresent(ob -> this.setOuterDiameter((Double)ob));
		json.get("length").ifPresent(ob -> this.setLength((Double)ob));
	}

	@Override
	public void setSpecificParameters(String des, String val) {
		switch(des) {
		case "innerDiameter": 
			this.setInnerDiameter(Double.parseDouble(val));
			break;
		case "outerDiameter":
			this.setOuterDiameter(Double.parseDouble(val));
			break;
		case "length":
			this.setLength(Double.parseDouble(val));
			break;
		}
	}

	@Override
	public int addSpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i) {
		double length;
		double innerDiameter;
		double outerDiameter;
		
		if(SPSettings.metricMode.get()) {
			length = SPOperations.round(Converter.mmFromM(this.getLength()), 3);
			innerDiameter = SPOperations.round(Converter.mmFromM(this.getInnerDiameter()), 3);
			outerDiameter = SPOperations.round(Converter.mmFromM(this.getOuterDiameter()), 3);
		} else {
			length = SPOperations.round(Converter.InchFromMeter(this.getLength()), 3);
			innerDiameter = SPOperations.round(Converter.InchFromMeter(this.getInnerDiameter()), 3);
			outerDiameter = SPOperations.round(Converter.InchFromMeter(this.getOuterDiameter()), 3);
		}
		
		d.descriptors.add(i++, new Descriptor("Length", Double.toString(length)));
		d.descriptors.add(i++, new Descriptor("Inner Diameter", Double.toString(innerDiameter)));
		d.descriptors.add(i++, new Descriptor("Outer Diameter", Double.toString(outerDiameter)));
		return i;
	}

	@Override
	public String getParametersForPopover(boolean metric) {
		String common = getCommonParametersForPopover(metric);
		String des = "";
		if(metric) {
			des += "Length: " + SPOperations.round(Converter.mmFromM(length),3) + " mm\n";
			des += "Inner Diameter: " + SPOperations.round(Converter.mmFromM(this.getInnerDiameter()), 3) + " mm\n";
			des += "Outer Diameter: " + SPOperations.round(Converter.mmFromM(this.getOuterDiameter()), 3) + " mm\n";
		} else {
			des += "Length: " + SPOperations.round(Converter.InchFromMeter(length),3) + " in\n";
			des += "Inner Diameter: " + SPOperations.round(Converter.InchFromMeter(this.getInnerDiameter()), 3) + " in\n";
			des += "Outer Diameter: " + SPOperations.round(Converter.InchFromMeter(this.getOuterDiameter()), 3) + " in\n";	
		}
		des += common;
		return des;
	}
	
	public double getStrainRate(double reflectedStrain) {
		return 2 * this.getAverageDiameter()  * this.barSetup.IncidentBar.getShearWaveSpeed() /
				(this.barSetup.IncidentBar.diameter * this.getLength()) * reflectedStrain;
	}
	
	public double getLoad(double barStrain) {
		return this.getTorque(barStrain) / (this.getAverageDiameter() / 2.0);
	}
	
	private double getTorque(double strain) {
		return this.barSetup.IncidentBar.getShearModulus() * getPolarMomentOfCylinder(this.barSetup.IncidentBar.getRadius()) * strain / this.barSetup.IncidentBar.getRadius();
	}
	
	private double getStressFromTorque(double torque) {
		double momentOfInertia = this.getPolarMomentOfTube();
		return torque / momentOfInertia * ((this.getOuterDiameter() + this.getInnerDiameter()) / 4);
	}
	
	public double getStressFromLoad(double load) {
		double torque = load * (this.getAverageDiameter() / 2.0);
		return this.getStressFromTorque(torque);
	}
	
	private double getPolarMomentOfTube() {
		return getPolarMomentOfCylinder(this.getOuterRadius()) - getPolarMomentOfCylinder(this.getInnerRadius());
	}
	
	private static double getPolarMomentOfCylinder(double radius) {
		return Math.PI * Math.pow(radius, 4) / 2;
	}
	
	public double getAverageDiameter()
	{
		return (this.getInnerDiameter() + this.getOuterDiameter()) / 2;
	}
	
	public double getInnerDiameter() {
		return innerDiameter;
	}

	public void setInnerDiameter(double innerDiameter) {
		this.innerDiameter = innerDiameter;
	}

	public double getOuterDiameter() {
		return outerDiameter;
	}
	
	private double getOuterRadius() {
		return this.getOuterDiameter() / 2;
	}
	
	private double getInnerRadius() {
		return this.getInnerDiameter() / 2;
	}

	public void setOuterDiameter(double outerDiameter) {
		this.outerDiameter = outerDiameter;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double[] getDisplacement(double[] time, double[] reflectedBarStrain) {
		double[] strainRate = Arrays.stream(reflectedBarStrain)
				.map(s -> -s)
				.map(s -> this.getStrainRate(s))
				.toArray();
		double[] strain = SPOperations.integrate(time, strainRate);
		return Arrays.stream(strain).map(s -> s * this.getAverageDiameter() / 2.0).toArray();
	}
	public double[] getDisplacementFromDICStrain(double [] strain)
	{
		return Arrays.stream(strain).map(s->s*2.0*this.getAverageDiameter()/2.0).toArray(); // 2 to get to gamma strain divide by 2 to get radius
	}
	
	public double getStrainFromDisplacement(double displacement) {
		return displacement / (this.getAverageDiameter() / 2.0);
	}
	
	@Override
	public String getFileExtension() {
		return getSampleConstants().getExtension();
	}

	public static SampleConstants getSampleConstants() {
		return new SampleConstants(
				"Torsion",
				"Torsion Sample", 
				"/net/relinc/libraries/images/Torsion Icon.png", 
				".samtor"
				);
	}
}
