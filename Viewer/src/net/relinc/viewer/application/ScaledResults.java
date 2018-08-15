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
	private Sample sample;

	public ScaledResults(Sample s) {
		this.sample = s;
		boolean isStress = !isLoadDisplacement.get();
		boolean engineering = isEngineering.get();
		
		if(isStress && !(s instanceof HopkinsonBarSample))
			System.out.println("Invalid input to ScaledResults\n\n");
		
		String stressUnit = getDisplayedLoadUnit();
		String strainUnit = getDisplayedDisplacementUnit();

		if (!isStress) {
			stress = s.results.getLoad(stressUnit);
			strain = s.results.getDisplacement(strainUnit);
			strainRate = SPOperations.getDerivative(s.results.time, strain); // Use the already scaled strain array.
		} else {
			// all hopkinson bar samples. If the loadDisplacement checkbox isn't checked, theyre all HopkinsonBarSamples
			HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample) s;
			double[] load;
			load = s.results.getEngineeringStress(stressUnit); // load is scaled.

			if (!engineering) { // True Results
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
	}
	
	private void renderFrontFaceForce(HopkinsonBarSample hoppy)
	{
		Map<String, double[]> forceData = hoppy.getFrontFaceForceInterpolated();
		double[] data = forceData.get("force");
		double[] timeData = forceData.get("time");
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
		if(frontFaceForce == null) {
			// Lazy evaluation to avoid unneeded computation.
			renderFrontFaceForce((HopkinsonBarSample)sample);
			if (isEnglish.get()) {
				frontFaceForce = Arrays.stream(frontFaceForce).map(d -> Converter.LbfFromN(d)).toArray();
			}
		}
		return frontFaceForce;
	}
	
	public double[] getBackFaceForce()
	{
		if(backFaceForce == null) {
			// Lazy evaluation to avoid unneeded computation.
			renderBackFaceForce((HopkinsonBarSample)sample);
			if (isEnglish.get()) {
				backFaceForce = Arrays.stream(backFaceForce).map(d -> Converter.LbfFromN(d)).toArray();
			}
		}
		return backFaceForce;
	}
	
	public double[] getTime()
	{
		return time;
	}

}
