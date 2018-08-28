package net.relinc.viewer.application;

import java.util.Arrays;
import java.util.Map;

import net.relinc.libraries.sample.HopkinsonBarSample;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.viewer.GUI.CommonGUI;

public class ScaledResults extends CommonGUI{
	private double[] time;
	private double[] stress;
	private double[] strain;
	private double[] strainRate;
	private double[] frontFaceForce;
	private double[] backFaceForce;


	public ScaledResults(Sample s) {
		boolean isStress = !isLoadDisplacement.get();
		boolean english = isEnglish.get();
		boolean engineering = isEngineering.get();
		
		if(isStress && !(s instanceof HopkinsonBarSample))
			System.out.println("Invalid input to ScaledResults\n\n");
		
		// String timeUnit = getDisplayedTimeUnit();
		String stressUnit = getDisplayedLoadUnit();
		String strainUnit = getDisplayedDisplacementUnit();
		// String strainRateUnit = getDisplayedStrainRateUnit();

		if (!isStress) {
			stress = s.results.getLoad(stressUnit);
			strain = s.results.getDisplacement(strainUnit);
			strainRate = SPOperations.getDerivative(s.results.time, strain); // Use the already scaled strain array.
		} else {
			// all hopkinson bar samples. If the loadDisplacement checkbox isn't checked, theyre all HopkinsonBarSamples
			
			double[] load;
			load = s.results.getEngineeringStress(stressUnit); // load is scaled.

			if (!engineering) { // True Results
				HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample) s;
				try {
					stress = hopkinsonBarSample.getTrueStressFromEngStressAndEngStrain(load,
							s.results.getEngineeringStrain());
				} catch (Exception e) {
					e.printStackTrace();
				}
				strain = s.results.getTrueStrain();
				strainRate = SPOperations.getDerivative(s.results.time, strain);

			} else {
				stress = s.results.getEngineeringStress(stressUnit);
				strain = s.results.getEngineeringStrain();
				strainRate = SPOperations.getDerivative(s.results.time, strain);

			}
		}
		
		// apply time scale
		double[] time = new double[s.results.time.length];
		for (int i = 0; i < time.length; i++) {
			time[i] = s.results.time[i] * timeUnits.getMultiplier();
		}
		this.time = time;
		
		if (s.isFaceForceGraphable()) {
			HopkinsonBarSample hoppy = (HopkinsonBarSample) s;
			
			renderFrontFaceForce(hoppy);
			renderBackFaceForce(hoppy);

			if (english) {
				frontFaceForce = Arrays.stream(frontFaceForce).map(d -> Converter.LbfFromN(d)).toArray();
				backFaceForce = Arrays.stream(backFaceForce).map(d -> Converter.LbfFromN(d)).toArray();
			}
		}
	}
	
	private void renderFrontFaceForce(HopkinsonBarSample hoppy)
	{
		Map<String, double[]> forceData = hoppy.getFrontFaceForceInterpolated();
		double[] data = forceData.get("force");
		double[] timeData = forceData.get("time");
		//frontFaceForce = hoppy.getFrontFaceForce();
		try {
			frontFaceForce = 
					LoadDisplacementSampleResults.interpolateValues(data, timeData, hoppy.results.time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void renderBackFaceForce(HopkinsonBarSample hoppy)
	{
		Map<String, double[]> data = hoppy.getBackFaceForceInterpolated();
		double[] force = data.get("force");
		double[] time = data.get("time");
		try {
			backFaceForce = 
					LoadDisplacementSampleResults.interpolateValues(force, time, hoppy.results.time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Returns load or stress, depending on isLoadDisplacement
	public double[] getLoad()
	{
		return stress;
	}
	
	// Returns displacement or strain, depending on isLoadDisplacement
	public double[] getDisplacement()
	{
		return strain;
	}
	
	public double[] getStrainRate()
	{
		return strainRate;
	}
	
	public double[] getFrontFaceForce()
	{
		return frontFaceForce;
	}
	
	public double[] getBackFaceForce()
	{
		return backFaceForce;
	}
	
	public double[] getTime()
	{
		return time;
	}

}
