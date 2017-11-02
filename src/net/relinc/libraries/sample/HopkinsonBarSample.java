package net.relinc.libraries.sample;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.relinc.libraries.data.DataFile;
import net.relinc.libraries.data.DataLocation;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.data.IncidentPulse;
import net.relinc.libraries.data.ReflectedPulse;
import net.relinc.libraries.data.TransmissionPulse;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public abstract class HopkinsonBarSample extends Sample {
	
	public abstract double getInitialCrossSectionalArea();
	public abstract double getHopkinsonBarTransmissionPulseSign();
	public abstract double getHopkinsonBarReflectedPulseSign();
	public abstract double[] getTrueStressFromEngStressAndEngStrain(double[] engStress, double[] engStrain);
	public abstract int addHoppySpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i);
	public abstract String getHoppySpecificString();
	public abstract void setHoppySpecificParameters(String des, String val);
	public abstract double getCurrentSampleLength(double displacement);
	protected double length;
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	
	@Override 
	public int addSpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i){
		double lengthUnits = SPSettings.metricMode.get() ? Converter.mmFromM(getLength()) : Converter.InchFromMeter(getLength());
		d.descriptors.add(i++, new Descriptor("Length", Double.toString(SPOperations.round(lengthUnits, 3))));
		i = addHoppySpecificParametersToDecriptorDictionary(d, i);
		return i;
	}
	
	@Override
	public String getSpecificString(){
		return "Length"+delimiter+getLength()+SPSettings.lineSeperator + getHoppySpecificString();
	}
	
	@Override
	public void setSpecificParameters(String des, String val){
		if(des.equals("Length"))
			setLength(Double.parseDouble(val));
		setHoppySpecificParameters(des, val);
	}
	
	public double[] getDisplacementFromEngineeringStrain(double[] engStrain) {
		if(length <= 0)
			System.err.println("THIS SHOUDN'T HAPPEN!!!!!!!");
		double[] displacement = new double[engStrain.length];
		for(int i = 0; i < displacement.length; i++){
			displacement[i] = engStrain[i] * length;
		}
		return displacement;
	}
	
	public double[] getEngineeringStressFromForce(double[] force){
		double[] stressValues = new double[force.length];
		for(int i = 0; i < stressValues.length; i++){
			stressValues[i] = force[i] / getInitialCrossSectionalArea(); //method is above
		}
		return stressValues;
	}
	
	public double[] getForceFromTransmissionBarStrain(double[] barStrain) {
		double[] force = new double[barStrain.length];
		for(int i = 0; i < barStrain.length; i++){
			force[i] = getHopkinsonBarTransmissionPulseSign() * barStrain[i] * barSetup.TransmissionBar.youngsModulus * barSetup.TransmissionBar.getArea();
		}
		return force;
	}
	
	public double[] getEngineeringStrainFromIncidentBarReflectedPulseStrain(double[] time, double[] reflectedStrain) {
		// Calculate strain rate first
		double[] strainRate = new double[reflectedStrain.length];
		double strainRateMultiplier = 2 * barSetup.IncidentBar.getWaveSpeed() / (length);
		for(int i = 0; i < strainRate.length; i++){
			strainRate[i] = strainRateMultiplier * getHopkinsonBarReflectedPulseSign() * reflectedStrain[i]; //method sets sign of pulse.
		}
		// Then work backward to strain
		double[] strain = new double[strainRate.length];
		for(int i = 0; i < strain.length; i++){

			if(i==0){
				strain[0]=0;
			}
			else{
				strain[i] = strain[i - 1] + strainRate[i] * (time[i] - time[i - 1]);
			}


		}
		return strain;
	}
	
	public Map<String, double[]> getFrontFaceForceInterpolated()
	{
		ReflectedPulse reflectedPulse = null;//(ReflectedPulse) getCurrentDisplacementDatasubset();
		IncidentPulse incidentPulse = null;
		try{
			reflectedPulse = (ReflectedPulse)getCurrentDisplacementDatasubset();
			DataLocation reflectedLocation =  getLocationOfDataSubset(reflectedPulse);
			//find incident in same datafile.
			DataFile file = DataFiles.get(reflectedLocation.dataFileIndex);
			for(DataSubset subset : file.dataSubsets){
				if(subset instanceof IncidentPulse){
					incidentPulse = (IncidentPulse)subset;
					break;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
		double sign = getTransmissionPulseSign();
		HashMap<String, double[]> map = new HashMap<String, double[]>();
		double[] force = reflectedPulse.getFrontFaceForce(barSetup.IncidentBar, incidentPulse.getUsefulTrimmedData(), sign); 
		map.put("force", force);
		// This is copied from ChartsGUI
		map.put("time", Arrays.copyOfRange(reflectedPulse.getTrimmedTime(), 0, force.length));
		return map;
	}
	
	public double[] getFrontFaceForce() {
		ReflectedPulse reflectedPulse = null;//(ReflectedPulse) getCurrentDisplacementDatasubset();
		IncidentPulse incidentPulse = null;
		try{
			reflectedPulse = (ReflectedPulse)getCurrentDisplacementDatasubset();
			DataLocation reflectedLocation =  getLocationOfDataSubset(reflectedPulse);
			//find incident in same datafile.
			DataFile file = DataFiles.get(reflectedLocation.dataFileIndex);
			for(DataSubset subset : file.dataSubsets){
				if(subset instanceof IncidentPulse){
					incidentPulse = (IncidentPulse)subset;
					break;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
		double sign = getTransmissionPulseSign();
		return reflectedPulse.getFrontFaceForce(barSetup.IncidentBar, incidentPulse.getUsefulTrimmedData(), sign);
	}
	
	public Map<String, double[]> getBackFaceForceInterpolated() {
		double sign = getTransmissionPulseSign();

		TransmissionPulse transmissionPulse = (TransmissionPulse) getCurrentLoadDatasubset();

		double[] force = transmissionPulse.getBackFaceForcePulse(barSetup.TransmissionBar, sign);
		HashMap<String, double[]> map = new HashMap<String, double[]>();
		map.put("force", force);
		map.put("time", Arrays.copyOfRange(transmissionPulse.getTrimmedTime(), 0, force.length));
		return map;
	}
	
	public double getTransmissionPulseSign(){
		return (this instanceof CompressionSample || this instanceof ShearCompressionSample) ? -1 : 1;
	}
}
