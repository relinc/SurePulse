package net.relinc.viewer.application;

import java.util.ArrayList;
import java.util.List;

import net.relinc.processor.sample.Sample;
import net.relinc.processor.staticClasses.SPOperations;

public class RegionOfInterest {
	public double beginROITime;
	public double endROITime;

	//avg
	public double averageEngineeringStress;
	public double averageTrueStress;
	public double averageEngineeringStrain;
	public double averageTrueStrain;
	public double averageEngineeringStrainRate;
	public double averageTrueStrainRate;

	//integrals
	public double averageEngineeringStressVsTimeIntegral;
	public double averageTrueStressVsTimeIntegral;
	public double averageEngineeringStrainVsTimeIntegral;
	public double averageTrueStrainVsTimeIntegral;
	public double averageEngineeringStressVsStrainIntegral;
	public double averageTrueStressVsStrainIntegral;
	public double averageEngineeringStrainRateVsTimeIntegral;
	public double averageTrueStrainRateVsTimeIntegral;
	
	//avg
	public double averageMaxEngineeringStress;
	public double averageMaxTrueStress;
	public double averageMaxEngineeringStrain;
	public double averageMaxTrueStrain;
	public double averageMaxEngineeringStrainRate;
	public double averageMaxTrueStrainRate;



	public void renderROIResults(List<Sample> inputSamples, boolean loadDisplacement, Sample specificSample){
		//if sample.placeholder = false -> normal behavior on sample list. else on individual sample
		
		if(inputSamples == null || inputSamples.size() == 0)
			return;

		List<Sample> samples = inputSamples;
		if(specificSample != null && !specificSample.placeHolderSample){
			samples = new ArrayList<Sample>();
			samples.add(specificSample);
		}
		
		double div = (double)samples.size();

		//avg
		double sumEngStress = 0;
		double sumEngStrain = 0;
		double sumTrueStress = 0;
		double sumTrueStrain = 0;
		double sumEngStrainRate = 0;
		double sumTrueStrainRate = 0;
		//integral
		double sumEngStressVsTimeIntegrals = 0;
		double sumTrueStressVsTimeIntegrals = 0;
		double sumEngStrainVsTimeIntegrals = 0;
		double sumTrueStrainVsTimeIntegrals = 0;
		double sumEngStressVsStrainIntegrals = 0;
		double sumTrueStressVsStrainIntegrals = 0;
		double sumEngStrainRateVsTimeIntegrals = 0;
		double sumTrueStrainRateVsTimeIntegrals = 0;
		//max
		double sumEngStressMaxes = 0;
		double sumEngStrainMaxes = 0;
		double sumTrueStressMaxes = 0;
		double sumTrueStrainMaxes = 0;
		double sumEngStrainRateMaxes = 0;
		double sumTrueStrainRateMaxes = 0;



		for(Sample s : samples){
			//if its the placeholder, use the specific sample begin/end times. Else, the ROI begin/end.
			double beginTime = (specificSample != null && !specificSample.placeHolderSample) ? s.getBeginROITime() : beginROITime;
			double endTime = (specificSample != null && !specificSample.placeHolderSample) ? s.getEndROITime() : endROITime;
			int begin = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, beginTime);
			int end = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, endTime);
			double div2 = (double)(end - begin + 1);
			double[] engStrain = s.results.getEngineeringStrain();
			double[] engStress = s.results.getEngineeringStress();
			double[] trueStress = s.results.getTrueStress();
			double[] trueStrain = s.results.getTrueStrain();
			
			double[] engStrainRate = SPOperations.getDerivative(s.results.time, engStrain);
			double[] trueStrainRate = SPOperations.getDerivative(s.results.time, trueStrain);
			
			//avg
			double sumEngStressTemp = 0;
			double sumEngStrainTemp = 0;
			double sumTrueStressTemp = 0;
			double sumTrueStrainTemp = 0;
			double sumEngStrainRateTemp = 0;
			double sumTrueStrainRateTemp = 0;
			//max
			double maxEngStressTemp = Double.MIN_VALUE;
			double maxEngStrainTemp = Double.MIN_VALUE;
			double maxTrueStressTemp = Double.MIN_VALUE;
			double maxTrueStrainTemp = Double.MIN_VALUE;
			double maxEngStrainRateTemp = Double.MIN_VALUE;
			double maxTrueStrainRateTemp = Double.MIN_VALUE;

			for(int i = begin; i <= end; i++){
				sumEngStressTemp += engStress[i];
				sumTrueStressTemp += trueStress[i];
				sumEngStrainTemp += engStrain[i];
				sumTrueStrainTemp += trueStrain[i];
				sumEngStrainRateTemp += engStrainRate[i];
				sumTrueStrainRateTemp += trueStrainRate[i];
				if(engStress[i] > maxEngStressTemp)
					maxEngStressTemp = engStress[i];
				if(trueStress[i] > maxTrueStressTemp)
					maxTrueStressTemp = trueStress[i];
				if(engStrain[i] > maxEngStrainTemp)
					maxEngStrainTemp = engStrain[i];
				if(trueStrain[i] > maxTrueStrainTemp)
					maxTrueStrainTemp = trueStrain[i];
				if(engStrainRate[i] > maxEngStrainRateTemp)
					maxEngStrainRateTemp = engStrainRate[i];
				if(trueStrainRate[i] > maxTrueStrainRateTemp)
					maxTrueStrainRateTemp = trueStrainRate[i];
			}
			sumEngStress += sumEngStressTemp / div2;
			sumTrueStress += sumTrueStressTemp / div2;
			sumEngStrain += sumEngStrainTemp / div2;
			sumTrueStrain += sumTrueStrainTemp / div2;
			sumEngStrainRate += sumEngStrainRateTemp / div2;
			sumTrueStrainRate += sumTrueStrainRateTemp / div2;

			double[] integral = SPOperations.integrate(s.results.time, engStress, begin, end);
			sumEngStressVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, trueStress, begin, end);
			sumTrueStressVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, engStrain, begin, end);
			sumEngStrainVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, trueStrain, begin, end);
			sumTrueStrainVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(engStrain, engStrain, begin, end);
			sumEngStressVsStrainIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(trueStrain, trueStress, begin, end);
			sumTrueStressVsStrainIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, engStrainRate, begin, end);
			sumEngStrainRateVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, trueStrainRate, begin, end);
			sumTrueStrainRateVsTimeIntegrals += integral[integral.length - 1];
			
			sumEngStressMaxes += maxEngStressTemp;
			sumEngStrainMaxes += maxEngStrainTemp;
			sumTrueStressMaxes += maxTrueStressTemp;
			sumTrueStrainMaxes += maxTrueStrainTemp;
			sumEngStrainRateMaxes += maxEngStrainRateTemp;
			sumTrueStrainRateMaxes += maxTrueStrainRateTemp;
		}
		averageEngineeringStress = sumEngStress / div;
		averageTrueStress = sumTrueStress / div;
		averageEngineeringStrain = sumEngStrain / div;
		averageTrueStrain = sumTrueStrain / div;
		averageEngineeringStrainRate = sumEngStrainRate / div;
		averageTrueStrainRate = sumTrueStrainRate / div;

		averageEngineeringStressVsTimeIntegral = sumEngStressVsTimeIntegrals / div;
		averageTrueStressVsTimeIntegral = sumTrueStressVsTimeIntegrals / div;
		averageEngineeringStrainVsTimeIntegral = sumEngStrainVsTimeIntegrals / div;
		averageTrueStrainVsTimeIntegral = sumTrueStrainVsTimeIntegrals / div;
		averageEngineeringStressVsStrainIntegral = sumEngStressVsStrainIntegrals / div;
		averageTrueStressVsStrainIntegral = sumTrueStressVsStrainIntegrals / div;
		averageEngineeringStrainRateVsTimeIntegral = sumEngStrainRateVsTimeIntegrals / div;
		averageTrueStrainRateVsTimeIntegral = sumTrueStrainRateVsTimeIntegrals / div;
		
		averageMaxEngineeringStress = sumEngStressMaxes / div;
		averageMaxTrueStress = sumTrueStressMaxes / div;
		averageMaxEngineeringStrain = sumEngStrainMaxes / div;
		averageMaxTrueStrain = sumTrueStrainMaxes / div;
		averageMaxEngineeringStrainRate = sumEngStrainRateMaxes / div;
		averageMaxTrueStrainRate = sumTrueStrainRateMaxes / div;

	}
}
