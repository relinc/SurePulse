package net.relinc.processor.sample;

import java.util.Arrays;

import net.relinc.processor.data.DataLocation;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.data.Displacement;
import net.relinc.processor.data.EngineeringStrain;
import net.relinc.processor.data.Force;
import net.relinc.processor.data.LoadCell;
import net.relinc.processor.data.ReflectedPulse;
import net.relinc.processor.data.TransmissionPulse;
import net.relinc.processor.data.TrueStrain;
import net.relinc.processor.staticClasses.SPOperations;

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

		System.out.println("load Time length: " + loadTime.length);
		System.out.println("Displacement Time Length: " + displacementTime.length);
		
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

	// THIS IS DANGEROUS, WE THINK
	// private double[] interpolate(double[] data, double[] timeForData,
	// double[] timeNeeded) {
	// double[] newData = new double[timeNeeded.length];
	// int lastIdx = 0;
	// for(int i = 0; i < newData.length; i++){
	// int idx = SPOperations.findFirstIndexGreaterorEqualToValue(timeForData,
	// timeNeeded[i]);
	// //TODO: Check index validity.
	//// if(lastIdx == idx)
	//// System.out.println("Same index twice in a row: " + idx);
	//// lastIdx = idx;
	// newData[i] = data[idx];
	// }
	// return newData;
	// }

	// THIS IS ACTUAL INTERPOLATION
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
		if (displacementData == null)
			return;

		displacementTime = displacementData.getTrimmedTime();

		if (displacementData instanceof EngineeringStrain) {
			displacement = sample.getDisplacementFromEngineeringStrain(displacementData.getUsefulTrimmedData());
			displacement = displacementData.getUsefulTrimmedData();
		} else if (displacementData instanceof TrueStrain) {
			displacement = sample.getDisplacementFromEngineeringStrain(
					sample.getEngineeringStrainFromTrueStrain(displacementData.getUsefulTrimmedData()));
		} else if (displacementData instanceof ReflectedPulse) {
			HopkinsonBarSample hoppySample = (HopkinsonBarSample)sample; //if it has a reflected pulse, then its a hoppy bar sample.
			displacement = hoppySample.getDisplacementFromEngineeringStrain(
					hoppySample.getEngineeringStrainFromIncidentBarReflectedPulseStrain(displacementData.getTrimmedTime(),
							displacementData.getUsefulTrimmedData()));
		}
		else if(displacementData instanceof Displacement){
			displacement = displacementData.getUsefulTrimmedData();
		}
		else{
			System.out.println("Not Implemented.");
		}

		DataSubset loadData = sample.getDataSubsetAtLocation(loadDataLocation);
		if (loadData == null)
			return; // TODO exception would be prudent here
		loadTime = loadData.getTrimmedTime();

		if (loadData instanceof Force)
			load = loadData.getUsefulTrimmedData();
		else if (loadData instanceof LoadCell)
			load = loadData.getUsefulTrimmedData();
		else if (loadData instanceof TransmissionPulse) {
			double[] barStrain = loadData.getUsefulTrimmedData();
			HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)sample;
			load = hopkinsonBarSample.getForceFromTransmissionBarStrain(barStrain);
		} else {
			// TODO: Throw exception
			System.out.println("Not implemented");
		}

		renderData(load, loadTime, displacement, displacementTime);
	}

	public double[] getEngineeringStrain() {
		double[] engStrain = new double[displacement.length];
		for (int i = 0; i < engStrain.length; i++) {
			engStrain[i] = displacement[i] / sample.getLength();
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
		double[] engStress = getEngineeringStress();
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

}
