package net.relinc.libraries.sample;

import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.staticClasses.SPSettings;

public class TorsionSample extends Sample {
	private double innerDiameter;
	private double outerDiameter;
	private double length;
	private double youngsModulus;	
	
	public TorsionSample()
	{

	}
	
	@Override
	public String getSpecificString() {
		String thisShouldBeJson = "innerDiameter" + delimiter + this.getInnerDiameter() + SPSettings.lineSeperator;
		thisShouldBeJson += "outerDiameter" + delimiter + this.getOuterDiameter() + SPSettings.lineSeperator;
		thisShouldBeJson += "length" + delimiter + this.getLength() + SPSettings.lineSeperator;
		// Young's modulus is handled in parent ("Sample" class)
		return thisShouldBeJson;
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
		case "youngsModulus":
			this.setYoungsModulus(Double.parseDouble(val));
			break;
		}
	}

	@Override
	public int addSpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i) {
		// TODO need to add all params
		return i;
	}

	@Override
	public String getParametersForPopover(boolean selected2) {
		return "";
	}
	
	public double getMeanRadius()
	{
		return (this.getInnerDiameter() + this.getOuterDiameter()) / 4;
	}
	
	public double getSampleThickness()
	{
		return (this.getOuterDiameter() - this.getInnerDiameter()) / 2;
	}
	
	private static double getShearModulus(double youngsModulus, double poissonRatio)
	{
		return youngsModulus / 2.0 / (1 + poissonRatio);
	}
	
	public double getShearModulus()
	{
		return getShearModulus(this.getYoungsModulus(), .33);
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

	public void setOuterDiameter(double outerDiameter) {
		this.outerDiameter = outerDiameter;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getYoungsModulus() {
		return youngsModulus;
	}

	public void setYoungsModulus(double youngsModulus) {
		this.youngsModulus = youngsModulus;
	}
}
