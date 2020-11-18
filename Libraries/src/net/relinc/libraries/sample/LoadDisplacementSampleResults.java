package net.relinc.libraries.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import net.relinc.libraries.data.*;//DataLocation;
import net.relinc.libraries.staticClasses.SPMath;
import net.relinc.libraries.staticClasses.SPOperations;

public class LoadDisplacementSampleResults {

	public double[] time;
	public double[] load;
	public double[] displacement;

	private DataLocation loadDataLocation;
	private DataLocation displacementDataLocation;

	Sample sample; // super marginal to have references that are cycles. Sample references this class, which references Sample.

	enum WaveType {
		ONE, TWO, THREE, NA
	}

	WaveType waveType;

	public LoadDisplacementSampleResults(Sample sample) {
		this(sample, WaveType.NA, sample.getDefaultStressLocation(), sample.getDefaultStrainLocation());
	}

	public LoadDisplacementSampleResults(Sample sample, WaveType waveType, DataLocation loadDataLocation, DataLocation displacementDataLocation) {
		this.sample = sample;
		this.loadDataLocation = loadDataLocation;
		this.displacementDataLocation = displacementDataLocation;
		this.waveType = waveType;
	}

	public static List<LoadDisplacementSampleResults> createResults(Sample sample, DataLocation loadDataLocation, DataLocation displacementDataLocation) {
		List<LoadDisplacementSampleResults> retval = new ArrayList<>();



		if(sample instanceof HopkinsonBarSample) {
			DataSubset loadData = sample.getDataSubsetAtLocation(sample.getDefaultStressLocation());
			if (loadData != null) {
				if (loadData instanceof TransmissionPulse && sample instanceof HopkinsonBarSample) {
					TransmissionPulse pulse = (TransmissionPulse) loadData;
					HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample) sample;
					if (pulse.oneWaveCheckBox.isSelected() || !(sample.getDataSubsetAtLocation(sample.getDefaultStrainLocation()) instanceof ReflectedPulse)) {
						retval.add(new LoadDisplacementSampleResults(sample, WaveType.ONE, loadDataLocation, displacementDataLocation));
					}
					if(pulse.twoWaveCheckBox.isSelected()) {
						retval.add(new LoadDisplacementSampleResults(sample, WaveType.TWO, loadDataLocation, displacementDataLocation));
					}
					if(pulse.threeWaveCheckBox.isSelected()) {
						retval.add(new LoadDisplacementSampleResults(sample, WaveType.THREE, loadDataLocation, displacementDataLocation));
					}
				}
			}
		}


		if(retval.isEmpty()) {
			retval.add(new LoadDisplacementSampleResults(sample, WaveType.NA,  loadDataLocation, displacementDataLocation));
		}

		return retval;
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

	public static double[] interpolateValues(double[] yData, double[] xData, double[] xNeeded) {

		if(yData.length != xData.length) {
			throw new RuntimeException("X and Y arrays are not the same length! Sad!");
		}

		double[] yNeeded = new double[xNeeded.length];

		for(int i = 0; i < xNeeded.length; i++){
			if(xNeeded[i] < xData[0]) {
				// interpolate from first 2 points
				double x1 = xData[0];
				double y1 = yData[0];
				double x2 = xData[1];
				double y2 = yData[1];

				double m = (y2 - y1) / (x2 - x1);
				double b = y1 - (y2 - y1) / (x2 - x1) * x1;

				yNeeded[i] = m * xNeeded[i] + b;


			} else if(xNeeded[i] >= xData[xData.length - 1]) {
				// interpolate from last 2 points

				double x1 = xData[xData.length - 2];
				double y1 = yData[xData.length - 2];
				double x2 = xData[xData.length - 1];
				double y2 = yData[xData.length - 1];

				double m = (y2 - y1) / (x2 - x1);
				double b = y1 - (y2 - y1) / (x2 - x1) * x1;

				yNeeded[i] = m * xNeeded[i] + b;

			} else {
				// interpolate between 2 points.
				int idx = SPOperations.findFirstIndexGreaterThanValue(xData, xNeeded[i]);
				double x1 = xData[idx - 1];
				double y1 = yData[idx - 1];
				double x2 = xData[idx];
				double y2 = yData[idx];

				double m = (y2 - y1) / (x2 - x1);
				double b = y1 - (y2 - y1) / (x2 - x1) * x1;

				yNeeded[i] = m * xNeeded[i] + b;

			}

		}

		return yNeeded;
	}

	public static double linearApprox(double[] data, double[] timeForData, double[] timeNeeded, int indexOfTimeNeeded,
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
						Sample.getEngineeringStrainFromTrueStrain(displacementData.getUsefulTrimmedData()));
			} else if (displacementData instanceof ReflectedPulse && sample instanceof HopkinsonBarSample) {
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
			else if(displacementData instanceof ReflectedPulse && sample instanceof TorsionSample)
			{
				TorsionSample torsionSample = (TorsionSample)sample;
				displacement = torsionSample.getDisplacement(displacementData.getTrimmedTime(), displacementData.getUsefulTrimmedData());
			}
			else if(displacementData instanceof LagrangianStrain && sample instanceof TorsionSample)
			{
				TorsionSample torsionSample = (TorsionSample)sample;
				double[] engStrain = SPMath.getEngStrainFromLagrangianStrain(displacementData.getUsefulTrimmedData());
				displacement = torsionSample.getDisplacementFromDICStrain(engStrain);
			}
			else if(displacementData instanceof ReflectedPulse && sample instanceof BrazilianTensileSample)
			{
				// Strain cannot be calculated from reflected pulse, set to all 0's.
				displacement = IntStream.range(0, displacementData.getTrimmedTime().length).mapToDouble(i -> 0).toArray();
			}
			else if(displacementData instanceof LagrangianStrain && sample instanceof BrazilianTensileSample)
			{
				BrazilianTensileSample brazilianTensileSample = (BrazilianTensileSample)sample;
				double[] engStrain = SPMath.getEngStrainFromLagrangianStrain(displacementData.getUsefulTrimmedData());
				displacement = Arrays.stream(engStrain).map(s -> brazilianTensileSample.getDisplacementFromStrain(s)).toArray();
			}
			else{
				System.err.println("Strain type Not Implemented in renderXY results: " + displacementData);
			}
		}

		//renderXY the loadData
		DataSubset loadData = sample.getDataSubsetAtLocation(loadDataLocation);
		if (loadData != null){
			loadTime = loadData.getTrimmedTime();

			if (loadData instanceof Force)
				load = loadData.getUsefulTrimmedData();
			else if (loadData instanceof LoadCell)
				load = loadData.getUsefulTrimmedData();
			else if (loadData instanceof TransmissionPulse && sample instanceof HopkinsonBarSample) {
				TransmissionPulse pulse = (TransmissionPulse)loadData;
				double[] barStrain = loadData.getUsefulTrimmedData();
				HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)sample;
				if(this.waveType.equals(WaveType.ONE))
					load = hopkinsonBarSample.getForceFromTransmissionBarStrain(barStrain);
				else if(this.waveType.equals(WaveType.TWO)){
					load = hopkinsonBarSample.getFrontFaceForce((ReflectedPulse)getCurrentDisplacementDatasubset());
					//need to get time array from the reflected pulse.
					loadTime = getCurrentDisplacementDatasubset().getTrimmedTime();
					
					//The load data set number of points is set to the minimum of incident and reflected pulse. If incident was shorted, then we need to trim time.
					loadTime = Arrays.copyOfRange(loadTime, 0, load.length);
				}
				else if(this.waveType.equals(WaveType.THREE)){
					double[] load1 = hopkinsonBarSample.getForceFromTransmissionBarStrain(barStrain);
					double[] load2 = hopkinsonBarSample.getFrontFaceForce((ReflectedPulse)getCurrentDisplacementDatasubset());
					//we know they are the same timestep, so we will grab trim by the shortest one.
					if(load1.length > load2.length){
						//load1 needs to be trimmed.
						load1 = Arrays.copyOfRange(load1, 0 , load2.length);
					}
					else if(load2.length > load1.length){
						//load2 needs to be trimmed
						load2 = Arrays.copyOfRange(load2, 0 , load1.length);
					}

					loadTime = getCurrentDisplacementDatasubset().getTrimmedTime();
					loadTime = Arrays.copyOfRange(loadTime, 0, Math.min(load1.length, load2.length));
					
					
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
			} else if(sample instanceof TorsionSample) {
				TorsionSample torsionSample = (TorsionSample)sample;
				load = Arrays.stream(loadData.getUsefulTrimmedData())
						.map(s -> torsionSample.getLoad(s))
						.toArray();
			}
			else if(sample instanceof BrazilianTensileSample) {
				BrazilianTensileSample brazilianTensileSample = (BrazilianTensileSample)sample;
				load = brazilianTensileSample.getForceFromTransmissionBarStrain(loadData.getUsefulTrimmedData());
			}
			else {
				throw new RuntimeException("Sample type is not implemented! " + sample.getClass());
			}
		}
		
		if(load == null && displacement == null){
			//neither of them loaded, return
			return;
		}
		else if(displacement == null){
			//renderXY a zeroed displacement so load vs time can be viewed.
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
		if(sample instanceof TorsionSample) {
			TorsionSample torsionSample = (TorsionSample)sample;
			return Arrays.stream(displacement).map(d -> torsionSample.getStrainFromDisplacement(d)).toArray();
		}
		else if(sample instanceof BrazilianTensileSample) {
			BrazilianTensileSample brazilianTensileSample = (BrazilianTensileSample)sample;
			return Arrays.stream(displacement).map(d -> brazilianTensileSample.getStrainFromDisplacement(d)).toArray();
		}
		else if(sample instanceof HopkinsonBarSample)
		{
			HopkinsonBarSample hoppy = (HopkinsonBarSample)sample;
			double[] engStrain = new double[displacement.length];
			for (int i = 0; i < engStrain.length; i++) {
				engStrain[i] = displacement[i] / hoppy.getLength();
			}
			return engStrain;
		}
		else {
			return null;
		}
	}

	public double[] getEngineeringStress() {
		
		if(sample instanceof TorsionSample) {
			TorsionSample torsionSample = (TorsionSample)sample;
			return Arrays.stream(load).map(l -> torsionSample.getStressFromLoad(l)).toArray();
		}
		else if(sample instanceof BrazilianTensileSample) {
			BrazilianTensileSample brazilianTensileSample = (BrazilianTensileSample)sample;
			return Arrays.stream(load).map(l -> brazilianTensileSample.getStressFromLoad(l)).toArray();
		}
		else {
			//must be hopkinson bar sample to get engineering stress.
			HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)sample;
			return hopkinsonBarSample.getEngineeringStressFromForce(load);
		}
		
	}

	public double[] getTrueStrain() {
		if(sample instanceof TorsionSample || sample instanceof BrazilianTensileSample) {
			return this.getEngineeringStrain();
		}
		// It is assumed that any sample asking for true strain is a HopkinsonBarSample.
		HopkinsonBarSample s = (HopkinsonBarSample)sample;
		return Arrays.stream(displacement).map(d -> Math.abs(Math.log(s.getCurrentSampleLength(d) / s.length))).toArray();
	}

	public double[] getTrueStress() {// pa
		if(sample instanceof TorsionSample || sample instanceof BrazilianTensileSample) {
			return this.getEngineeringStress();
		}
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
	
	public double getNumberOfReflections(){
		if(sample == null || sample.getDensity() == 0 || sample.getYoungsModulus() == 0 || !(sample instanceof HopkinsonBarSample))
			return 0.0;
		HopkinsonBarSample s = (HopkinsonBarSample)sample;
		DataSubset displacementData = s.getDataSubsetAtLocation(displacementDataLocation);
		double timeStep = displacementData.getModifiedTime()[1] - displacementData.getModifiedTime()[0];
		return timeStep * (displacementData.getEnd() - displacementData.getBegin()) * s.getWavespeed() / (2 * s.length);
	}
	
	public double[] getDisplacementRate(){
		return SPOperations.getDerivative(time, displacement);
	}

	public DataSubset getCurrentDisplacementDatasubset(){
		return sample.getDataSubsetAtLocation(displacementDataLocation);
	}

	public DataLocation getCurrentDisplacementLocation()
	{
		return displacementDataLocation;
	}

	public DataSubset getCurrentLoadDatasubset(){
		return sample.getDataSubsetAtLocation(loadDataLocation);
	}

	public DataLocation getCurrentLoadLocation()
	{
		return loadDataLocation;
	}

	public boolean isFaceForceGraphable(){
		return getCurrentLoadDatasubset() instanceof TransmissionPulse
				&& getCurrentDisplacementDatasubset() instanceof ReflectedPulse
				&& !(sample instanceof TorsionSample)
				&& !(sample instanceof BrazilianTensileSample);
	}

	public double[] getFrontFaceForce()
	{
		ReflectedPulse reflectedPulse = (ReflectedPulse)getCurrentDisplacementDatasubset();
		// TODO: this is assumes sample is HopkinsonBarSample...
		Map<String, double[]> forceData = ((HopkinsonBarSample)sample).getFrontFaceForceInterpolated(reflectedPulse);
		double[] data = forceData.get("force");
		double[] timeData = forceData.get("time");
		try {
			return LoadDisplacementSampleResults.interpolateValues(data, timeData, time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new double[]{};
	}

	public double[] getBackFaceForce()
	{
		// TODO: this is assumes sample is HopkinsonBarSample...
		Map<String, double[]> data = ((HopkinsonBarSample)sample).getBackFaceForceInterpolated((TransmissionPulse) getCurrentLoadDatasubset());
		double[] force = data.get("force");
		double[] time = data.get("time");
		try {
			return LoadDisplacementSampleResults.interpolateValues(force, time, time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new double[]{};
	}

	public DataLocation getLoadDataLocation() {
		return this.loadDataLocation;
	}

	public DataLocation getDisplacementDataLocation() {
		return this.displacementDataLocation;
	}

	public String getChartLegendPostFix() {
		switch (this.waveType) {
			case NA:
				return "";
			case ONE:
				return " WaveType=1";
			case TWO:
				return " WaveType=2";
			case THREE:
				return " WaveType=3";
		}
		return "";
	}

}
