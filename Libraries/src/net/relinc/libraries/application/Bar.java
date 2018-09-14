package net.relinc.libraries.application;

import java.util.ArrayList;

import net.relinc.libraries.staticClasses.SPSettings;


public class Bar {
	
	public ArrayList<StrainGaugeOnBar> strainGauges = new ArrayList<StrainGaugeOnBar>();

	public String name;
	public double length;
	public double density;
	public double youngsModulus;
	public double diameter;
	public double speedLimit;
	public double yield;
	private double poissonsRatio=.33;
	
	public String nameDescrip = "Name";
	public String lengthDescrip = "Length";
	public String densityDescrip = "Density";
	public String youngsModulusDescrip = "Young's Modulus";
	public String diameterDescrip = "Diameter";
	public String speedLimitDescrip = "Speed Limit";
	public String yieldDescrip = "Yield";
	public String poissonsRatioDescrip = "Poisson's Ratio";
	
	public String splitter = ":";
	
	
	public String stringForFile() {
		String contents = "SUREPulse Single Bar Setup Version:1" + SPSettings.lineSeperator;
		contents += nameDescrip + splitter + name + SPSettings.lineSeperator;
		contents += lengthDescrip + splitter + Double.toString(length) + SPSettings.lineSeperator;
		contents += densityDescrip + splitter + Double.toString(density) + SPSettings.lineSeperator;
		contents += youngsModulusDescrip + splitter + Double.toString(youngsModulus) + SPSettings.lineSeperator;
		contents += diameterDescrip + splitter + Double.toString(diameter) + SPSettings.lineSeperator;
		contents += speedLimitDescrip + splitter + Double.toString(speedLimit) + SPSettings.lineSeperator;
		contents += yieldDescrip + splitter + Double.toString(yield) + SPSettings.lineSeperator;
		contents += poissonsRatioDescrip + splitter + Double.toString(poissonsRatio) + SPSettings.lineSeperator;
		return contents;
	}
	
	public void setParametersFromString(String input){
		for(String line : input.split(SPSettings.lineSeperator)){
			setParameter(line);
		}
	}

	private void setParameter(String line) {
		if(line.split(splitter).length < 2)
			return;
		String des = line.split(splitter)[0];
		String val = line.split(splitter)[1];
		if(des.equals(nameDescrip))
			name = val;
		if(des.equals(lengthDescrip))
			length = Double.parseDouble(val);
		if(des.equals(densityDescrip))
			density = Double.parseDouble(val);
		if(des.equals(youngsModulusDescrip))
			youngsModulus = Double.parseDouble(val);
		if(des.equals(diameterDescrip))
			diameter = Double.parseDouble(val);
		if(des.equals(speedLimitDescrip))
			speedLimit = Double.parseDouble(val);
		if(des.equals(yieldDescrip))
			yield = Double.parseDouble(val);
		if(des.equals(poissonsRatioDescrip))
			poissonsRatio = Double.parseDouble(val);
		
	}
	
	public double getRadius() {
		return this.diameter / 2;
	}

	public double getArea() {
		return Math.PI * Math.pow(diameter / 2,2);
	}
	
	public double getWaveSpeed(){
		return Math.pow(youngsModulus / density, .5);
	}

	public StrainGaugeOnBar getStrainGauge(String strainGaugeName) {
		for(StrainGaugeOnBar sgOnBar : strainGauges){
			if(sgOnBar.getNameForFile().equals(strainGaugeName))
				return sgOnBar;
		}
		return null;
	}

	public boolean removeStrainGauge(String specificname) {
		StrainGaugeOnBar sgToRemove = null;
		for(StrainGaugeOnBar sg : strainGauges){
			if(sg.specificName.equals(specificname)){
				sgToRemove = sg;
				break;
			}
		}
		if(sgToRemove != null){
			strainGauges.remove(sgToRemove);
			return true;
		}
		return false;
	}

	public double calculateSpeedLimit() {
		return Math.pow(Math.pow(yield / youngsModulus, 2) * 4 * getEnergyMultiplier() / (density * Math.pow(diameter / 2, 2) * Math.PI * getWaveSpeed()), .5);
	}
	
	public double getEnergyMultiplier()
    {
        return (.5 * Math.pow(diameter / 2, 2) * Math.PI * getWaveSpeed() * youngsModulus +
            .5 * density * Math.pow(diameter / 2, 2) * Math.PI * Math.pow(youngsModulus / density, 1.5));
    }

	public double getExpectedPulse(StrikerBar strikerBar, StrainGaugeOnBar sg) {
		double impliedTime = 2 * strikerBar.getLength() / getWaveSpeed();
		return Math.pow(strikerBar.getEnergy() / (getEnergyMultiplier() * impliedTime), .5) / sg.getVoltageFactor();
	}
	
	public double getPolarMomentOfIntertia() {
		return Math.PI * Math.pow(this.getRadius(), 4) / 2.0;
	}
	
	public double getShearModulus()
	{
		return this.youngsModulus / 2.0 / (1 + this.getPoissonsRatio());
	}
	
	public double getShearWaveSpeed() {
		return Math.pow(this.getShearModulus() / this.density, .5);
	}

	public double getPoissonsRatio() {
		return poissonsRatio;
	}

	public void setPoissonsRatio(double poissonsRatio) {
		this.poissonsRatio = poissonsRatio;
	}

}
