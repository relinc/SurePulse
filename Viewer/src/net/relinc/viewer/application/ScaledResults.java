package net.relinc.viewer.application;

import java.util.Arrays;
import java.util.Map;

import net.relinc.libraries.data.ReflectedPulse;
import net.relinc.libraries.sample.*;
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
	private LoadDisplacementSampleResults results;

	public ScaledResults(Sample s, int resultIdx) {
		this.sample = s;
		this.results = this.sample.getResults().get(resultIdx);
		boolean isStress = !isLoadDisplacement.get();
		boolean engineering = isEngineering.get();
		
		String stressUnit = getDisplayedLoadUnit();
		String strainUnit = getDisplayedDisplacementUnit();

		if (!isStress) {
			stress = this.results.getLoad(stressUnit);
			strain = this.results.getDisplacement(strainUnit);
			strainRate = SPOperations.getDerivative(this.results.time, strain); // Use the already scaled strain array.
		} else {
			// all hopkinson bar samples. If the loadDisplacement checkbox isn't checked, theyre all HopkinsonBarSamples
			
			double[] load;
			load = this.results.getEngineeringStress(stressUnit); // load is scaled.

			if (!engineering && !(sample instanceof TorsionSample || sample instanceof BrazilianTensileSample)) { // True Results
				HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample) s;
				try {
					stress = hopkinsonBarSample.getTrueStressFromEngStressAndEngStrain(load,
							this.results.getEngineeringStrain());
				} catch (Exception e) {
					e.printStackTrace();
				}
				strain = this.results.getTrueStrain();
				strainRate = SPOperations.getDerivative(this.results.time, strain);

			} else {
				stress = this.results.getEngineeringStress(stressUnit);
				strain = this.results.getEngineeringStrain();
				strainRate = SPOperations.getDerivative(this.results.time, strain);

			}
		}
		
		// apply time scale
		double[] time = new double[this.results.time.length];
		for (int i = 0; i < time.length; i++) {
			time[i] = this.results.time[i] * timeUnits.getMultiplier();
		}
		this.time = time;
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
			frontFaceForce = results.getFrontFaceForce();
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
			backFaceForce = results.getBackFaceForce();
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
