package net.relinc.libraries.sample;

import java.util.Arrays;

import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class TorsionSample extends Sample {
	private double POISSON_RATIO = .33;
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
	
	public double getStrainRate(double reflectedStrain) {
		return 4 * (this.getAverageDiameter() / 2) * this.getWavespeed() / (this.barSetup.IncidentBar.diameter / 2 * this.getLength()) * reflectedStrain;
	}
	
	public double getLoad(double barStrain) {
		return this.getTorque(barStrain) / (this.getAverageDiameter() / 2.0);
	}
	
	private double getTorque(double strain) {
		return this.getShearModulus() * getPolarMomentOfCylinder(this.barSetup.IncidentBar.getRadius()) * strain / this.barSetup.IncidentBar.getRadius();
	}
	
	private double getStressFromTorque(double torque) {
		double momentOfInertia = this.getPolarMomentOfTube();
		return torque / momentOfInertia * ((this.getOuterDiameter() + this.getInnerDiameter()) / 2);
	}
	
	public double getStressFromLoad(double load) {
		double torque = load * (this.getAverageDiameter() / 2.0);
		return this.getStressFromTorque(torque);
	}
	
	@Override
	public double getWavespeed() {
		return Math.pow(this.getShearModulus() / this.barSetup.IncidentBar.density, .5);
	}
	
	private double getPolarMomentOfTube() {
		return getPolarMomentOfCylinder(this.getOuterRadius()) - getPolarMomentOfCylinder(this.getInnerRadius());
	}
	
	private static double getPolarMomentOfCylinder(double radius) {
		return Math.PI * Math.pow(radius, 4) / 2;
	}
	
	public double getAverageDiameter()
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
		return getShearModulus(this.getYoungsModulus(), POISSON_RATIO);
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

	public double getYoungsModulus() {
		return youngsModulus;
	}

	public void setYoungsModulus(double youngsModulus) {
		this.youngsModulus = youngsModulus;
	}

	public double[] getDisplacement(double[] time, double[] reflectedBarStrain) {
		double[] strainRate = Arrays.stream(reflectedBarStrain)
				.map(s -> -s)
				.map(s -> this.getStrainRate(s))
				.toArray();
		double[] strain = SPOperations.integrate(time, strainRate);
		return Arrays.stream(strain).map(s -> s * this.getAverageDiameter() / 2.0).toArray();
	}
	
	public double getStrainFromDisplacement(double displacement) {
		return displacement / (this.getAverageDiameter() / 2.0);
	}
	
}
