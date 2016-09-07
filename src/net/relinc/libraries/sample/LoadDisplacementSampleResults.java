package net.relinc.libraries.sample;

import java.util.Arrays;
import java.util.function.DoublePredicate;

import javax.management.relation.Relation;

import net.relinc.libraries.data.*;//DataLocation;
import net.relinc.libraries.staticClasses.SPMath;
//import net.relinc.processor.data.DataSubset;
//import net.relinc.processor.data.Displacement;
//import net.relinc.processor.data.EngineeringStrain;
//import net.relinc.processor.data.Force;
//import net.relinc.processor.data.LoadCell;
//import net.relinc.processor.data.ReflectedPulse;
//import net.relinc.processor.data.TransmissionPulse;
//import net.relinc.processor.data.TrueStrain;
import net.relinc.libraries.staticClasses.SPOperations;

public class LoadDisplacementSampleResults {

	public double[] time;
	public double[] load;
	public double[] displacement;

	public DataLocation loadDataLocation;
	public DataLocation displacementDataLocation;

	Sample sample;

	public LoadDisplacementSampleResults(Sample sample) {
		this.sample = sample;
		loadDataLocation = sample.getDefaultStressLocation(); // force
		displacementDataLocation = sample.getDefaultStrainLocation(); // could
																		// be
																		// strain,
																		// need
																		// length
																		// then
	}

	private void renderData(double[] inputLoad, double[] loadTime, double[] displacementData,
			double[] displacementTime) {
		double displacementTimeDuration = displacementTime[displacementTime.length - 1];
		double loadTimeDuration = loadTime[loadTime.length - 1];

		if (loadTimeDuration < displacementTimeDuration) {
			displacementTime = trimArrayAfter(displacementTime, loadTimeDuration);
			displacementData = Arrays.copyOfRange(displacementData, 0, displacementTime.length);
		} else {
			loadTime = trimArrayAfter(loadTime, displacementTimeDuration);
			inputLoad = Arrays.copyOfRange(inputLoad, 0, loadTime.length);
		}

		// then interpolate based from the more sparse index
		if (loadTime.length < displacementTime.length) {
			// get strain time values from stress values
			time = loadTime;
		} else {
			time = displacementTime;
		}

		// then fill the stress and strain arrays
		try {
			load = interpolateValues(inputLoad, loadTime, time);
			displacement = interpolateValues(displacementData, displacementTime, time);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private double[] interpolateValues(double[] data, double[] timeForData, double[] timeNeeded) throws Exception {
		double[] newData = new double[timeNeeded.length];
		if (data.length != timeForData.length) {
			throw new Exception("Data array and time array must be the same length");
		}
		for (int i = 0; i < newData.length; i++) {
			int idx = SPOperations.findFirstIndexGreaterorEqualToValue(timeForData, timeNeeded[i]);

			double interpolatedData;

			if (idx < 0) {
				interpolatedData = linearApprox(data, timeForData, timeNeeded, i, data.length - 6, 5);
			} else if (timeForData[idx] == timeNeeded[i]) {
				interpolatedData = data[idx];
			} else if (idx == 0) {
				interpolatedData = data[0];
			} else {
				if (idx < timeForData.length - 1 && timeForData[idx + 1] > timeForData[idx]) {
					interpolatedData = linearApprox(data, timeForData, timeNeeded, i, idx, 1);
				} else {
					interpolatedData = data[idx];
					System.out.println("Rare Occurance " + idx);
				}
			}
			newData[i] = interpolatedData;
		}
		return newData;
	}

	private double linearApprox(double[] data, double[] timeForData, double[] timeNeeded, int indexOfTimeNeeded,
			int indexOfData, int distInterpolated) {
		return data[indexOfData] + ((data[indexOfData + distInterpolated] - data[indexOfData])
				/ (timeForData[indexOfData + distInterpolated] - timeForData[indexOfData]))
				* (timeNeeded[indexOfTimeNeeded] - timeForData[indexOfData]);
	}

	private double[] trimArrayAfter(double[] data, double value) {
		int lastIndexKept = data.length - 1; //change: by default, it should keep all points.
		for (int i = 0; i < data.length; i++) {
			if (data[i] > value) {
				lastIndexKept = i - 1;
				break;
			}
		}
		double[] result = new double[lastIndexKept + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = data[i];
		}
		return result;
	}

	public void render() {
		double[] displacement = null;
		double[] displacementTime = null;
		double[] load = null;
		double[] loadTime = null;

		DataSubset displacementData = sample.getDataSubsetAtLocation(displacementDataLocation);
		if (displacementData != null){
			displacementTime = displacementData.getTrimmedTime();

			if (displacementData instanceof EngineeringStrain && sample instanceof HopkinsonBarSample) {
				HopkinsonBarSample hoppy = (HopkinsonBarSample)sample;
				displacement = hoppy.getDisplacementFromEngineeringStrain(displacementData.getUsefulTrimmedData());
				//displacement = displacementData.getUsefulTrimmedData(); //this was an error. fixed 4-4-2016
			} else if (displacementData instanceof TrueStrain && sample instanceof HopkinsonBarSample) {
				HopkinsonBarSample hoppy = (HopkinsonBarSample)sample;
				displacement = hoppy.getDisplacementFromEngineeringStrain(
						hoppy.getEngineeringStrainFromTrueStrain(displacementData.getUsefulTrimmedData()));
			} else if (displacementData instanceof ReflectedPulse) {
				HopkinsonBarSample hoppySample = (HopkinsonBarSample)sample; //if it has a reflected pulse, then its a hoppy bar sample.
				displacement = hoppySample.getDisplacementFromEngineeringStrain(
						hoppySample.getEngineeringStrainFromIncidentBarReflectedPulseStrain(displacementData.getTrimmedTime(),
								displacementData.getUsefulTrimmedData()));
			}
			else if(displacementData instanceof Displacement){
				displacement = displacementData.getUsefulTrimmedData();
			}
			else if(displacementData instanceof LagrangianStrain && sample instanceof HopkinsonBarSample){
				HopkinsonBarSample hoppy = (HopkinsonBarSample)sample;
				double[] engStrain = SPMath.getEngStrainFromLagrangianStrain(displacementData.getUsefulTrimmedData());
				displacement = hoppy.getDisplacementFromEngineeringStrain(engStrain);
			}
			else{
				System.err.println("Strain type Not Implemented in render results: " + displacementData);
			}
		}

		//render the loadData
		DataSubset loadData = sample.getDataSubsetAtLocation(loadDataLocation);
		if (loadData != null){
			loadTime = loadData.getTrimmedTime();

			if (loadData instanceof Force)
				load = loadData.getUsefulTrimmedData();
			else if (loadData instanceof LoadCell)
				load = loadData.getUsefulTrimmedData();
			else if (loadData instanceof TransmissionPulse) {
				TransmissionPulse pulse = (TransmissionPulse)loadData;
				double[] barStrain = loadData.getUsefulTrimmedData();
				HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)sample;
				if(pulse.oneWaveRadioButton.isSelected() || !(sample.getCurrentDisplacementDatasubset() instanceof ReflectedPulse))
					load = hopkinsonBarSample.getForceFromTransmissionBarStrain(barStrain);
				else if(pulse.twoWaveRadioButton.isSelected()){
					load = hopkinsonBarSample.getFrontFaceForce();
					//need to get time array from the reflected pulse.
					loadTime = hopkinsonBarSample.getCurrentDisplacementDatasubset().getTrimmedTime();
					
					//The load data set number of points is set to the minimum of incident and reflected pulse. If incident was shorted, then we need to trim time.
					loadTime = Arrays.copyOfRange(loadTime, 0, load.length);
				}
				else if(pulse.threeWaveRadioButton.isSelected()){
					double[] load1 = hopkinsonBarSample.getForceFromTransmissionBarStrain(barStrain);
					double[] load2 = hopkinsonBarSample.getFrontFaceForce();
					//we know they are the same timestep, so we will grab trim by the shortest one.
					if(load1.length > load2.length){
						//load1 needs to be trimmed.
						load1 = Arrays.copyOfRange(load1, 0 , load2.length);
						//the time needs to get trimmed too. This assumes that the timesteps are the same between strain gauges.
						loadTime = Arrays.copyOfRange(load1, 0 , load2.length);
					}
					else if(load2.length > load1.length){
						//load2 needs to be trimmed
						load2 = Arrays.copyOfRange(load2, 0 , load1.length);
					}
					
					
					if(load1.length != load2.length){
						System.err.println("Error in 3 wave method calculation. Arrays lengths are not the same.");
						load = null;
						return;
					}
					load = new double[load1.length];
					for(int i = 0; i < load.length; i++)
						load[i] = (load1[i] + load2[i]) / 2;
				}
				else 
					System.err.println("Neither one, two, or three wave was selected");
			} else {
				// TODO: Throw exception
				System.out.println("Not implemented");
			}
		}
		
		if(load == null && displacement == null){
			//neither of them loaded, return
			return;
		}
		else if(displacement == null){
			//render a zeroed displacement so load vs time can be viewed.
			displacement = new double[load.length]; //zeros
			displacementTime = loadTime;
		}
		else if(load == null){
			load = new double[displacement.length];
			loadTime = displacementTime;
		}
		

		renderData(load, loadTime, displacement, displacementTime);
	}

	public double[] getEngineeringStrain() {
		if(!(sample instanceof HopkinsonBarSample))
			return null; //these result classes could be abstracted out a bit.
		HopkinsonBarSample hoppy = (HopkinsonBarSample)sample;
		double[] engStrain = new double[displacement.length];
		for (int i = 0; i < engStrain.length; i++) {
			engStrain[i] = displacement[i] / hoppy.getLength();
		}
		return engStrain;
	}

	public double[] getEngineeringStress() {
		//must be hopkinson bar sample to get engineering stress.
		HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)sample;
		return hopkinsonBarSample.getEngineeringStressFromForce(load);
	}

	public double[] getTrueStrain() {
		return sample.getTrueStrainFromEngineeringStrain(getEngineeringStrain());
	}

	public double[] getTrueStress() {// pa
		HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)sample;
		return hopkinsonBarSample.getTrueStressFromEngStressAndEngStrain(getEngineeringStress(), getEngineeringStrain());
	}

	public double[] getEngineeringStress(String units) {
		double[] engStress = getEngineeringStress();//pa
		double[] convertedStress = new double[engStress.length];
		double converterMultiplier = 1.0;
		if (units.equals("MPa"))
			converterMultiplier = 1 / Math.pow(10, 6);
		if (units.equals("ksi"))
			converterMultiplier = 1 / 6894757.293178;
		if(converterMultiplier == 1.0)
			System.err.println("CONVERTER MULTIPLIER NOT APPLIED");
		for (int i = 0; i < convertedStress.length; i++) {
			convertedStress[i] = engStress[i] * converterMultiplier;
		}
		return convertedStress;
	}

	public double[] getLoad(String string) {
		double scaledLoad[] = new double[load.length];
		double multiplier = 1;
		if(string.equals("Lbf"))
			multiplier = 1 / 4.44822;
		for(int i = 0; i < scaledLoad.length; i++){
			scaledLoad[i] = load[i] * multiplier;
		}
		return scaledLoad;
	}

	public double[] getDisplacement(String string) {
		double[] scaledDisplacement = new double[displacement.length];
		double multiplier = 1;
		if(string.equals("in"))
			multiplier = 1.0 / .0254;
		else if(string.equals("mm"))
			multiplier = Math.pow(10,3);
		for(int i = 0; i < scaledDisplacement.length; i++){
			scaledDisplacement[i] = displacement[i] * multiplier;
		}
		return scaledDisplacement;
	}

	// public double[] getEngineeringStress(String units){
	// double[] convertedStress = new double[engineeringStress.length];
	// double converterMultiplier = 1.0;
	// if(units.equals("MPa"))
	// converterMultiplier = 1 / Math.pow(10, 6);
	// if(units.equals("ksi"))
	// converterMultiplier = 1 / 6894757.293178;
	// for(int i = 0; i < convertedStress.length; i++){
	// convertedStress[i] = engineeringStress[i] * converterMultiplier;
	// }
	// return convertedStress;
	// }
	
	public double getNumberOfReflections(){
		if(sample == null || sample.getDensity() == 0 || sample.getYoungsModulus() == 0 || !(sample instanceof HopkinsonBarSample))
			return 0.0;
		HopkinsonBarSample s = (HopkinsonBarSample)sample;
		DataSubset displacementData = s.getDataSubsetAtLocation(s.results.displacementDataLocation);
		double timeStep = displacementData.Data.timeData[1] - displacementData.Data.timeData[0];
		return timeStep * (displacementData.getEnd() - displacementData.getBegin()) * s.getWavespeed() / (2 * s.length);
	}
	
	public double[] getDisplacementRate(){
		return SPOperations.getDerivative(time, displacement);
	}

}
